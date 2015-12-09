package com.linhphan.blogradio.ui.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.linhphan.androidboilerplate.api.FileDownloadWorker;
import com.linhphan.androidboilerplate.api.JSoupDownloadWorker;
import com.linhphan.androidboilerplate.callback.DownloadCallback;
import com.linhphan.androidboilerplate.util.AppUtil;
import com.linhphan.androidboilerplate.util.Logger;
import com.linhphan.androidboilerplate.util.MathUtil;
import com.linhphan.androidboilerplate.util.TimerUtil;
import com.linhphan.blogradio.R;
import com.linhphan.blogradio.api.paser.JSoupDirectBlogParser;
import com.linhphan.blogradio.api.paser.JSoupBlogListParser;
import com.linhphan.blogradio.data.BlogRadioModel;
import com.linhphan.blogradio.service.MusicService;
import com.linhphan.blogradio.ui.adapter.BlogRadioAdapter;
import com.linhphan.blogradio.util.MessageCode;


import java.util.ArrayList;



public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, DownloadCallback, BlogRadioAdapter.OnItemClickedCallback,
        AbsListView.OnScrollListener, SeekBar.OnSeekBarChangeListener, Handler.Callback {

    private String HOST = "http://blogradio.vn/blog-radio/287?";

    private FloatingActionButton mFab;
    private ListView mLvBlogRadioList;
    private SeekBar mSb;
    private TextView mTxtElapsedTime;
    private TextView mTxtDuration;


    private BlogRadioAdapter mAdapter;

    private ArrayList<BlogRadioModel> mBlogList;
    private int mCurrentPlayedBlogIndex = -1;
    private int mCurrentPageIndex = 1;
    private boolean mIsLoadingMore = false;

    private MusicService mMusicSrv;
    private Handler mBaseHandler;

    protected ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (mMusicSrv == null)
                mMusicSrv = ((MusicService.MusicBinder) service).getMusicService();
            mMusicSrv.onBind();
            mMusicSrv.setupHandler(mBaseHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(getTag(), "service is disconnected");
            mMusicSrv.onUnbind();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        String url = HOST + mCurrentPageIndex;
        JSoupDownloadWorker worker = new JSoupDownloadWorker(this, true, this);
        worker.setParser(new JSoupBlogListParser())
                .setRequestCode(DownloadCallback.GET_BLOG_LIST_REQUEST_CODE)
                .execute(url);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mBaseHandler = new Handler(this);

        setupActionbar();
        setupFloatingButton();
        getWidgets();
        registerEventHandler();


    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent musicServiceIntent = new Intent(this, MusicService.class);
        boolean isBound = bindService(musicServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        startService(musicServiceIntent);

        if (isBound) {
            Logger.d(getTag(), "binding service return true");
        } else {
            Logger.d(getTag(), "binding service return false");
        }
    }

    @Override
    protected void onPause() {
        if (mMusicSrv != null) {
            mMusicSrv.onUnbind();
            unbindService(serviceConnection);
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.action_volume:
                AppUtil.getInstance().openVolumeSystem(this);
                break;

            case R.id.action_about_me:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //==================== seek bar's callback =====================================================
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekTo(seekBar.getProgress());
    }

    //==================== async task download callback===========================================
    @Override
    public void onDownloadSuccessfully(Object data, int requestCode) {

        switch (requestCode) {
            case GET_DIRECT_BLOG_REQUEST_CODE:
                if (data instanceof String) { //data was returned from JSoupDirectParser
                    String url = (String) data;
                    if (mMusicSrv != null) {
                        mMusicSrv.play(url);
                        ActionBar actionBar = getSupportActionBar();
                        if (actionBar != null && mCurrentPlayedBlogIndex > -1) {
                            setTitle(mBlogList.get(mCurrentPlayedBlogIndex).getBlogNumber());
                        }
                    }

                }
                break;

            case GET_DIRECT_URL_TO_DOWNLOAD_REQUEST_CODE:
                if (data instanceof String) {
                    String url = (String) data;
                    final FileDownloadWorker worker = new FileDownloadWorker(this, true, this);
                    worker.setRequestCode(DOWNLOAD_FILE_REQUEST_CODE)
                            .setHorizontalProgressbar()
                            .setDialogCancelable()
                            .setDialogCancelCallback("Hide", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .execute(url);// TODO: 12/8/15 need to pass a file name for the file will be downloaded.
                }
                break;

            case DOWNLOAD_FILE_REQUEST_CODE:
                Snackbar.make(mLvBlogRadioList, "file was downloaded successfully", Snackbar.LENGTH_INDEFINITE)
                        .show();
                break;

            case GET_BLOG_LIST_REQUEST_CODE:
                if (data instanceof ArrayList) {//data was returned from JSoupBlogListParser
                    @SuppressWarnings("unchecked")
                    ArrayList<BlogRadioModel> blogRadioList = (ArrayList<BlogRadioModel>) data;
                    mBlogList = blogRadioList;
                    mAdapter.setList(mBlogList);
                }
                break;

            case GET_MORE_BLOG_LIST_REQUEST_CODE:
                mIsLoadingMore = false;
                if (data instanceof ArrayList) {//data was returned from JSoupBlogListParser
                    @SuppressWarnings("unchecked")
                    ArrayList<BlogRadioModel> blogRadioList = (ArrayList<BlogRadioModel>) data;
                    if (blogRadioList.size() > 0) {
                        int firstVisiblePosition = mLvBlogRadioList.getFirstVisiblePosition();
                        mBlogList.addAll(blogRadioList);
                        mAdapter.setList(mBlogList);
                        mLvBlogRadioList.setSelection(firstVisiblePosition);
                    }
                }
                break;
        }
    }

    @Override
    public void onDownloadFailed(Exception e, int requestCode) {
        mIsLoadingMore = false;
        e.printStackTrace();
    }

    //==================== list view's callback ====================================================
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, "clicked position "+ position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mFab.getLayoutParams();
        switch (scrollState){
            case SCROLL_STATE_TOUCH_SCROLL:
                if (mFab.getTranslationY() < params.bottomMargin + mFab.getHeight()){
                    mFab.animate().cancel();
                    mFab.animate().translationY(params.bottomMargin + mFab.getHeight()).setDuration(200);
                }
                break;

            case SCROLL_STATE_IDLE:
                mFab.animate().cancel();
                mFab.animate().translationY(mFab.getScrollY());
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (!mIsLoadingMore && totalItemCount > 0 && firstVisibleItem + visibleItemCount == totalItemCount) {
            mCurrentPageIndex++;
            mIsLoadingMore = true;
            String url = HOST + mCurrentPageIndex;
            JSoupDownloadWorker worker = new JSoupDownloadWorker(this, false, this);
            worker.setParser(new JSoupBlogListParser())
                    .setRequestCode(DownloadCallback.GET_MORE_BLOG_LIST_REQUEST_CODE)
                    .execute(url);
            Toast.makeText(this, "load more songs at page " + mCurrentPageIndex, Toast.LENGTH_SHORT).show();
        }
    }

    //===================== on item clicked callbacks ==============================================
    @Override
    public void onPlayButtonClicked(int position, String url) {
        JSoupDownloadWorker jSoupDownloadWorker = new JSoupDownloadWorker(HomeActivity.this, true, HomeActivity.this);
        jSoupDownloadWorker.setParser(new JSoupDirectBlogParser())
                .setRequestCode(DownloadCallback.GET_DIRECT_BLOG_REQUEST_CODE)
                .execute(url);
        mCurrentPlayedBlogIndex = position;
        mFab.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPauseButtonClicked(int position, String url) {
        pause();
    }

    @Override
    public void onDownloadButtonClicked(int position, String url) {
        JSoupDownloadWorker jSoupDownloadWorker = new JSoupDownloadWorker(HomeActivity.this, true, HomeActivity.this);
        jSoupDownloadWorker.setParser(new JSoupDirectBlogParser())
                .setRequestCode(DownloadCallback.GET_DIRECT_URL_TO_DOWNLOAD_REQUEST_CODE)
                .execute(url);
    }

    //===================== handler's callback ==============================================
    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == MessageCode.SONG_CHANGED.ordinal()) {
            return true;
        } else if (msg.what == MessageCode.TIMING.ordinal()) {
            String data = (String) msg.obj;
            if (data != null && !data.isEmpty()) {
                String[] arr = data.split("-");
                int position = Integer.parseInt(arr[0]);
                int duration = Integer.parseInt(arr[1]);
                mSb.setProgress(MathUtil.calculatePercentage(position, duration));
                mTxtElapsedTime.setText(TimerUtil.convertTime2String(position));
                mTxtDuration.setText(TimerUtil.convertTime2String(duration));
            }
            return true;

        } else if (msg.what == MessageCode.BUFFERING.ordinal()) {
            mSb.setSecondaryProgress((Integer) msg.obj);
            return true;


        } else if (msg.what == MessageCode.PAUSED.ordinal()) {
            return true;


        } else if (msg.what == MessageCode.PLAYING.ordinal()) {
            return true;


        } else if (msg.what == MessageCode.DESTROYED.ordinal()) {
            finish();
            return true;
        }
        return false;
    }

    //===================== media player's method ==============================================
    public void seekTo(int position) {
        if (mMusicSrv != null)
            mMusicSrv.seekTo(position);
    }

    public void pause() {
        if (mMusicSrv != null) {
            mMusicSrv.pause();
        }
        mFab.setVisibility(View.INVISIBLE);
    }


    //===================== other methods ==============================================
    private String getTag() {
        return getClass().getName();
    }

    private void getWidgets() {
        mLvBlogRadioList = (ListView) findViewById(R.id.lv_blog_radio_list);
        mSb = (SeekBar) findViewById(R.id.sb);
        mTxtElapsedTime = (TextView) findViewById(R.id.txt_elapsed_time);
        mTxtDuration = (TextView) findViewById(R.id.txt_duration);

        mLvBlogRadioList.setEmptyView(findViewById(R.id.txt_empty));
        mAdapter = new BlogRadioAdapter(mBlogList, this);
        mLvBlogRadioList.setAdapter(mAdapter);
    }

    private void registerEventHandler() {
        mLvBlogRadioList.setOnItemClickListener(this);
        mLvBlogRadioList.setOnScrollListener(this);
        mSb.setOnSeekBarChangeListener(this);
    }

    private void setupActionbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupFloatingButton() {
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setVisibility(View.INVISIBLE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicSrv != null) {
                    mMusicSrv.pause();
                }
                v.setVisibility(View.INVISIBLE);
                View selectedItem = mLvBlogRadioList.getChildAt(mCurrentPlayedBlogIndex);
                if (selectedItem != null) {
                    ImageButton btnPlay = (ImageButton) selectedItem.findViewById(R.id.img_btn_play);
                    ImageButton btnPause = (ImageButton) selectedItem.findViewById(R.id.img_btn_pause);
                    if (btnPlay != null && btnPause != null) {
                        btnPlay.setVisibility(View.VISIBLE);
                        btnPause.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }
}

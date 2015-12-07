package com.linhphan.blogradio.ui.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.linhphan.androidboilerplate.api.JSoupDownloadWorker;
import com.linhphan.androidboilerplate.callback.DownloadCallback;
import com.linhphan.androidboilerplate.util.Logger;
import com.linhphan.blogradio.R;
import com.linhphan.blogradio.api.paser.SamplePaser;
import com.linhphan.blogradio.service.MusicService;

public class HomeActivity extends AppCompatActivity implements DownloadCallback{

    protected MusicService mMusicSrv;
    private boolean isServiceBound;
    protected Handler mBaseHandler;

    protected ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (mMusicSrv == null)
                mMusicSrv = ((MusicService.MusicBinder) service).getMusicService();
            mMusicSrv.onBind();
//            mMusicSrv.setupHandler(mBaseHandler);
            isServiceBound = true;
            JSoupDownloadWorker jSoupDownloadWorker = new JSoupDownloadWorker(HomeActivity.this, true, HomeActivity.this);
            jSoupDownloadWorker.setParser(new SamplePaser());
            jSoupDownloadWorker.execute("http://blogradio.vn/blog-radio/blog-radio-419-dung-chay-tron-anh-nua-duoc-khong-em/6633");
            Logger.d(getTag(), "service is connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(getTag(), "service is disconnected");
            isServiceBound = false;
            mMusicSrv.onUnbind();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


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
    public void onDownloadSuccessfully(Object data) {
        if (data instanceof String){
            String url = (String) data;
            if (mMusicSrv != null){
                mMusicSrv.play(url);
            }
        }
    }

    @Override
    public void onDownloadFailed(Exception e) {
        e.printStackTrace();
    }

    private String getTag(){
        return getClass().getName();
    }
}

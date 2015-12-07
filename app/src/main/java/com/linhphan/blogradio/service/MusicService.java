package com.linhphan.blogradio.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.linhphan.androidboilerplate.util.Logger;
import com.linhphan.blogradio.ui.activity.HomeActivity;
import com.linhphan.blogradio.util.MessageCode;
import com.linhphan.blogradio.util.MusicServiceState;

import java.io.IOException;

/**
 * Created by linhphan on 10/22/15.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener, AudioManager.OnAudioFocusChangeListener {

    public static final String NOTIFY_PREVIOUS = "uit.linh.online.music.previous";
    public static final String NOTIFY_REMOVE = "uit.linh.online.music.delete";
    public static final String NOTIFY_PAUSE = "uit.linh.online.music.onPause";
    public static final String NOTIFY_PLAY = "uit.linh.online.music.onPlay";
    public static final String NOTIFY_NEXT = "uit.linh.online.music.next";
    public static final String NOTIFY_OPEN_MAIN_ACTIVITY = "uit.linh.online.music.open.main";
    private final MusicManagerReceiver notificationBroadcast = new MusicManagerReceiver();
    private static final int NOTIFY_ID = 11111;
    private static final int MAX_ATTEMPT = 3;//the maximum times try to attempt try request a song.

    private MediaPlayer mp;
    private MusicBinder mMusicBinder;
    private MusicServiceState mServiceState;
    private boolean mIsBound = false;
    private Handler mHandler;
    private CustomRunnable mRunnable;
    private int mAttempted = 0;

    public class MusicBinder extends Binder {
        public MusicService getMusicService() {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(getTag(), "the service is started by flag " + String.valueOf(flags));
        if (mp == null) {
            initMediaPlayer();
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NOTIFY_PREVIOUS);
        intentFilter.addAction(NOTIFY_PAUSE);
        intentFilter.addAction(NOTIFY_PLAY);
        intentFilter.addAction(NOTIFY_REMOVE);
        intentFilter.addAction(NOTIFY_NEXT);
        intentFilter.addAction(NOTIFY_OPEN_MAIN_ACTIVITY);
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(notificationBroadcast, intentFilter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mMusicBinder == null)
            mMusicBinder = new MusicBinder();
        mIsBound = true;
        Logger.d(getTag(), "the service is bound");
        return mMusicBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        mIsBound = true;
        Logger.d(getTag(), "the service is rebound");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mIsBound = false;
        terminateThread();
        Logger.d(getTag(), "the service is unbound");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mp.release();
        mServiceState = MusicServiceState.destroy;
        unregisterReceiver(notificationBroadcast);
        //reset the first category which is showed in MainContent
//        Utils.putIntToSharedPreferences(getBaseContext(), Constants.SELECTED_MENU_ITEM_KEY, R.id.menu_item_hot_vi);
        Logger.d(getTag(), "the service is destroyed");
    }

    //=================== media player callback ====================================================
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (mIsBound)
            notifyMediaPlayerBuffer(percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mServiceState = MusicServiceState.completed;
        next();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Logger.d(getTag(), "media player has been error what:" + String.valueOf(what) + ", extra: " + String.valueOf(extra));
        mServiceState = MusicServiceState.error;
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mServiceState = MusicServiceState.prepared;
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        int result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            Toast.makeText(getApplicationContext(), "your audio device has problem", Toast.LENGTH_LONG).show();
        } else {
            mp.start();
            mServiceState = MusicServiceState.playing;
//            ContentManager contentManager = ContentManager.getInstance();
//            contentManager.setDurationAt(contentManager.getCurrentPlayingSongPosition(), mp.getDuration());
            retrieveElapseTimePeriodically();
            showCustomNotification(getApplicationContext());
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    //================== AsyncTask callback ========================================================


    //================== audio manager callback ====================================================
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Logger.e("audio focus", "AUDIOFOCUS_GAIN");
                // resume playback
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                Logger.e("audio focus", "AUDIOFOCUS_LOSS");
//                release();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Logger.e("audio focus", "AUDIOFOCUS_LOSS_TRANSIENT");
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Logger.e("audio focus", "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");

                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                mp.setVolume(0.2f, 0.2f);
                break;
        }
    }
    //================== end =======================================================================

    private String getTag() {
        return getClass().getName();
    }

    public void setupHandler(Handler handler) {
        this.mHandler = handler;
//        ContentManager contentManager = ContentManager.getInstance();
//        Logger.d(getTag(), "the current position song: " + contentManager.getCurrentPlayingSongPosition());
        retrieveElapseTimePeriodically();
    }

    public void onBind() {
        mIsBound = true;
    }

    public void onUnbind() {
        mIsBound = false;
        terminateThread();
    }

    //==============================================================================================
    private void notifyCurrentSongHasChanged() {
        if (mHandler != null) {
            Message message = mHandler.obtainMessage();
            message.what = MessageCode.SONG_CHANGED.ordinal();
            mHandler.sendMessage(message);
        }
    }

    private void notifyMediaPlayerTiming(String msg) {
        if (mHandler != null) {
            Message message = mHandler.obtainMessage();
            message.what = MessageCode.TIMING.ordinal();
            message.obj = msg;
            mHandler.sendMessage(message);
        }
    }

    private void notifyMediaPlayerBuffer(int percentage) {
        if (mHandler != null) {
            Message message = mHandler.obtainMessage();
            message.what = MessageCode.BUFFERING.ordinal();
            message.obj = percentage;
            mHandler.sendMessage(message);
        }
    }

    private void notifyMediaPlayerPaused(boolean paused) {
        if (mHandler != null) {
            Message message = mHandler.obtainMessage();
            if (paused)
                message.what = MessageCode.PAUSED.ordinal();
            else message.what = MessageCode.PLAYING.ordinal();
            mHandler.sendMessage(message);
        }
    }

    //==============================================================================================
    private void initMediaPlayer() {
        if (mp == null) {
            mp = new MediaPlayer();
        }
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mp.setOnPreparedListener(this);
        mp.setOnErrorListener(this);
        mp.setOnCompletionListener(this);
        mp.setOnBufferingUpdateListener(this);
        mp.setOnSeekCompleteListener(this);
        mServiceState = MusicServiceState.idle;
    }

    public void next() {
        stop();


    }

    public void pre() {
        stop();


    }

    public void pause() {
        if (mServiceState == MusicServiceState.prepared || mServiceState == MusicServiceState.playing) {
            mp.pause();
            mServiceState = MusicServiceState.paused;
        }
        terminateThread();
        notifyMediaPlayerPaused(true);
        showCustomNotification(getApplicationContext());
    }

    private void stop() {
        pause();
        if (mServiceState == MusicServiceState.paused) {
            mp.stop();
            mServiceState = MusicServiceState.stopped;
        }
    }

    public void seekTo(int percentage) {
        if (mServiceState == MusicServiceState.playing || mServiceState == MusicServiceState.prepared ||
                mServiceState == MusicServiceState.paused || mServiceState == MusicServiceState.completed) {
            int duration = mp.getDuration();
            int position = (percentage * duration) / 100;
            mp.seekTo(position);
        } else if (mServiceState == MusicServiceState.stopped) {

        }

    }

    public void play() {
    }

    /**
     * plays the song at special position
     *
     * @param position the position of the song in list.
     */
    public void play(int position) {
    }

    public void play(String url) {
        Logger.d(getTag(), "try to play the song at " + url);
        Logger.d(getTag(), "try to play the song at " + mServiceState);
        if (url == null) return;

        if (mServiceState == MusicServiceState.playing || mServiceState == MusicServiceState.prepared ||
                mServiceState == MusicServiceState.paused || mServiceState == MusicServiceState.completed) {
            mp.stop();
            mp.reset();

        } else if (mServiceState == MusicServiceState.stopped) {
            mp.reset();

        } else if (mServiceState == MusicServiceState.destroy) {

            initMediaPlayer();
        } else if (mServiceState == MusicServiceState.preparing) {
            return;
        }

        try {
            mp.setDataSource(getBaseContext(), Uri.parse(url));// TODO: 12/2/15  
            mp.prepareAsync();
            mServiceState = MusicServiceState.preparing;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean isPlaying() {
        return mServiceState == MusicServiceState.playing;
    }

    //==============================================================================================

    /**
     * create a new thread to retrieve the timing of the playing media player
     */
    private void retrieveElapseTimePeriodically() {
        if (mRunnable == null)
            mRunnable = new CustomRunnable();
        mRunnable.terminate(false);
        Thread thread = new Thread(mRunnable);//the thread which run to get periodically time position of the song
        thread.start();
    }

    /**
     * terminate the thread which run to retrieve the timing of the playing media player
     */
    private void terminateThread() {
        if (mRunnable != null)
            mRunnable.terminate(true);
    }

    class CustomRunnable implements Runnable {
        private boolean isTerminate = false;

        @Override
        public void run() {
            while (!isTerminate && mIsBound) {
                try {
                    Logger.d(getTag(), "music state: " + mServiceState);
                    if (mServiceState == MusicServiceState.playing && mHandler != null) {
                        String msg = String.valueOf(mp.getCurrentPosition()) + "-" + String.valueOf(mp.getDuration());
                        notifyMediaPlayerTiming(msg);
                        notifyMediaPlayerPaused(false);
                        Logger.d(getTag(), "send to mHandler: " + String.valueOf(mp.getCurrentPosition()) + "-" + String.valueOf(mp.getDuration()));
                    } else {
                        break;
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void terminate(boolean terminate) {
            this.isTerminate = terminate;
        }
    }

    private void showCustomNotification(Context context) {
//        if (context == null) return;
//
//        ContentManager contentManager = ContentManager.getInstance();
//        SongModel currentSong = contentManager.getCurrentPlayingSong();
//        if (currentSong == null) return;
//        Notification notification = new NotificationCompat.Builder(context)
//                .setSmallIcon(R.mipmap.ic_music_launcher)//the icon will be displayed on status bar
//                .setContentTitle(currentSong.getTitle()).build();
//
//        //== normal content view for notification
//        RemoteViews smallView = new RemoteViews(getApplication().getPackageName(), R.layout.small_notificattion);
//        setListeners(smallView);
//        notification.priority = Notification.PRIORITY_MAX;
//        notification.contentView = smallView;
//        notification.contentView.setTextViewText(R.id.txt_title, currentSong.getTitle());
//        notification.contentView.setTextViewText(R.id.txt_artist, currentSong.getArtist());
//
//        if (mServiceState == MusicServiceState.prepared || mServiceState == MusicServiceState.playing) {
//            notification.contentView.setViewVisibility(R.id.img_btn_play, View.GONE);
//            notification.contentView.setViewVisibility(R.id.img_btn_pause, View.VISIBLE);
//        } else {
//            notification.contentView.setViewVisibility(R.id.img_btn_play, View.VISIBLE);
//            notification.contentView.setViewVisibility(R.id.img_btn_pause, View.GONE);
//        }
//
//        //== big content view for notification
//        if (AppUtil.getInstance().isSupportBigNotification()) {
//            RemoteViews bigView = new RemoteViews(getApplication().getPackageName(), R.layout.big_notification);
//            setListeners(bigView);
//            notification.bigContentView = bigView;
//            notification.bigContentView.setTextViewText(R.id.txt_title, currentSong.getTitle());
//            notification.bigContentView.setTextViewText(R.id.txt_artist, currentSong.getArtist());
//
//            if (mServiceState == MusicServiceState.prepared || mServiceState == MusicServiceState.playing) {
//                notification.bigContentView.setViewVisibility(R.id.img_btn_play, View.GONE);
//                notification.bigContentView.setViewVisibility(R.id.img_btn_pause, View.VISIBLE);
//
//            } else {
//                notification.bigContentView.setViewVisibility(R.id.img_btn_play, View.VISIBLE);
//                notification.bigContentView.setViewVisibility(R.id.img_btn_pause, View.GONE);
//            }
//
//        }
//
//        notification.flags |= Notification.FLAG_ONGOING_EVENT;
////        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        startForeground(NOTIFY_ID, notification);

    }

    private void setListeners(RemoteViews view) {
//        Intent previous = new Intent(NOTIFY_PREVIOUS);
//        Intent remove = new Intent(NOTIFY_REMOVE);
//        Intent pause = new Intent(NOTIFY_PAUSE);
//        Intent next = new Intent(NOTIFY_NEXT);
//        Intent play = new Intent(NOTIFY_PLAY);
//        Intent openMainActivity = new Intent(NOTIFY_OPEN_MAIN_ACTIVITY);
//
//        PendingIntent pPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
//        view.setOnClickPendingIntent(R.id.btn_pre, pPrevious);
//
//        PendingIntent pRemove = PendingIntent.getBroadcast(getApplicationContext(), 0, remove, PendingIntent.FLAG_UPDATE_CURRENT);
//        view.setOnClickPendingIntent(R.id.btn_remove, pRemove);
//
//        PendingIntent pPause = PendingIntent.getBroadcast(getApplicationContext(), 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
//        view.setOnClickPendingIntent(R.id.img_btn_pause, pPause);
//
//        PendingIntent pNext = PendingIntent.getBroadcast(getApplicationContext(), 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
//        view.setOnClickPendingIntent(R.id.img_btn_next, pNext);
//
//        PendingIntent pPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
//        view.setOnClickPendingIntent(R.id.img_btn_play, pPlay);
//
//        PendingIntent pOpen = PendingIntent.getBroadcast(getApplicationContext(), 0, openMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
//        view.setOnClickPendingIntent(R.id.img_notification, pOpen);
    }

    public void stopForeground() {
        stopForeground(true);
    }

    private class MusicManagerReceiver extends BroadcastReceiver {
        private boolean isHandsetUnpluggedRecently = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        pause();
                        isHandsetUnpluggedRecently = true;
                        Logger.d(getClass().getName(), "the handset is unplugged");
                        break;

                    case 1:
                        if (isHandsetUnpluggedRecently) {
                            play();
                            isHandsetUnpluggedRecently = false;
                        }
                        Logger.d(getClass().getName(), "the handset is plugged");
                        break;

                    default:
                        Logger.d(getClass().getName(), "I have no idea what the headset state is");
                        break;
                }
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
                KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                if (keyEvent == null) return;
                if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                    return;

                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_HEADSETHOOK:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        break;
                    case KeyEvent.KEYCODE_MEDIA_STOP:
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        break;
                }
            } else {
                if (intent.getAction().equals(MusicService.NOTIFY_PLAY)) {
                    play();
                } else if (intent.getAction().equals(MusicService.NOTIFY_PAUSE)) {
                    pause();
                } else if (intent.getAction().equals(MusicService.NOTIFY_NEXT)) {
                    next();
                } else if (intent.getAction().equals(MusicService.NOTIFY_PREVIOUS)) {
                    pre();
                } else if (intent.getAction().equals(MusicService.NOTIFY_REMOVE)) {
                    stop();
                    stopForeground();
                    MusicService.this.stopSelf();
                    Message message = mHandler.obtainMessage();
                    message.what = MessageCode.DESTROYED.ordinal();
                    mHandler.sendMessage(message);

                } else if (intent.getAction().equals(MusicService.NOTIFY_OPEN_MAIN_ACTIVITY)) {
                    Intent i = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);// close the status bar
                    context.sendBroadcast(i);

                    Intent in = new Intent(context, HomeActivity.class);
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(in);
                }
            }
        }
    }

    private class HandsetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    }

}

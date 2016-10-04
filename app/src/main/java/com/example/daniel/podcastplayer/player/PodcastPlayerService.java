package com.example.daniel.podcastplayer.player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.URLUtil;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.activity.PlayerActivity;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.data.FileManager;
import com.example.daniel.podcastplayer.data.Podcast;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static android.drm.DrmStore.Action.PLAY;
import static android.provider.ContactsContract.Intents.Insert.ACTION;


public class PodcastPlayerService extends Service {

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_START = "action_start";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_FINISH = "action_finish";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FORWARD = "action_forward";

    private final static int skipForward = 30000;   //30 seconds
    private final static int rewind = 10000;   //10 seconds
    private final static int notificationId = 212221;

    private MediaPlayer mp = null;
    private final IBinder binder = new PlayerBinder();
    private Episode episode = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()){
            case(ACTION_PLAY):
                resumePlayback();
                break;
            case(ACTION_PAUSE):
                pausePlayback();
                break;
            case(ACTION_FORWARD):
                forwardPlayback();
                break;
            case(ACTION_REWIND):
                rewindPlayback();
                break;
        }
        return START_STICKY;
    }

    private Notification buildNotif(boolean paused){
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), PlayerActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        File image = new File(getApplicationInfo().dataDir + "/Artwork", episode.getPodcastId() + ".png");
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
        android.support.v4.app.NotificationCompat.Builder notif = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notif)
                .setLargeIcon(bitmap)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(episode.getEpTitle())
                .setTicker(episode.getEpTitle())
                .setContentText(DbHelper.getInstance(this).getPodcast(episode.getPodcastId()).getPodcastArtist())
                .setContentIntent(pi)
                .setOngoing(true);
        notif.addAction(R.drawable.ic_fast_rewind_black_24dp, "", PendingIntent.getService(this,0,
                new Intent(this, PodcastPlayerService.class).setAction(ACTION_REWIND),0));
        if (paused)
            notif.addAction(R.drawable.ic_play_arrow_black_24dp, "", PendingIntent.getService(this,0,
                    new Intent(this, PodcastPlayerService.class).setAction(ACTION_PLAY),0));
        else
            notif.addAction(R.drawable.ic_pause_black_24dp, "", PendingIntent.getService(this,0,
                new Intent(this, PodcastPlayerService.class).setAction(ACTION_PAUSE),0));
        notif.addAction(R.drawable.ic_fast_forward_black_24dp, "", PendingIntent.getService(this,0,
                new Intent(this, PodcastPlayerService.class).setAction(ACTION_FORWARD),0));
        return notif.build();
    }

    public PodcastPlayerService(){}

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(audioChangeReceiver,
                new IntentFilter(AudioManager.ACTION_HEADSET_PLUG));
        LocalBroadcastManager.getInstance(this).registerReceiver(fileChangeReceiver,
                new IntentFilter(FileManager.ACTION_DELETE));
        //IntentFilter mediaFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        //mediaFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        //registerReceiver(controlReceiver, mediaFilter);
        //AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        //am.registerMediaButtonEventReceiver(RemoteControlReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finishPlayback(false);
        unregisterReceiver(audioChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fileChangeReceiver);
        //unregisterReceiver(controlReceiver);
    }

    public void finishPlayback(boolean finished){
        Log.d("PPS_SERVICE", "Finish playback");
        stopForeground(true);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                .cancel(notificationId);
        if (mp!=null){
            saveProgress(finished);
            mp.stop();
            mp.release();
            mp = null;
            episode = null;
        }
    }

    public void startPlayback(Episode e, Context context){
        startPlayback(e,context,true);
        startForeground(notificationId, buildNotif(false));
    }

    //start=false is just for prepare, and not start playback
    public void startPlayback(Episode e, final Context context, final boolean start){
        if (mp == null) {
            mp = new MediaPlayer();
            mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mpParam) {
                    LocalBroadcastManager.getInstance(PodcastPlayerService.this)
                            .sendBroadcast(new Intent(ACTION_FINISH));
                    FileManager.deleteFile(context,episode);
                }
            });
        }
        if (episode==null || !episode.getEpURL().equals(e.getEpURL()))
            try {
                saveProgress();
                mp.reset();
                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        setPlaybackParams();
                        startPlayback(start); }
                });
                mp.setDataSource(context,
                        Uri.parse(FileManager.getEpisodeFile(context, e).getAbsolutePath()));
                mp.prepareAsync();
            } catch (IOException excep) {
                excep.printStackTrace();
            }
            //if same episode, already prepared so resume playback
        else if (episode.getEpURL().equals(e.getEpURL()) && start){
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_PLAY));
            mp.start();
        }
        this.episode = e;
    }

    public void setPlaybackParams(){
        if (Build.VERSION.SDK_INT >= 23) {
            PlaybackParams params = new PlaybackParams();
            if (episode!=null) {
                float speed = PreferenceManager.getDefaultSharedPreferences(PodcastPlayerService.this)
                        .getFloat(String.valueOf(episode.getPodcastId()) + getString(R.string.speed_setting)
                                , -1);
                if (speed == -1)
                    speed = PreferenceManager.getDefaultSharedPreferences(PodcastPlayerService.this)
                            .getFloat(getString(R.string.default_speed_setting),-1);
                if (speed > 0)
                    params.setSpeed(speed);
            } else params.setSpeed(1.0f);
            params.setPitch(1.0f);
            mp.setPlaybackParams(params);
        }
    }

    private void startPlayback(boolean start){
        if (episode != null) {
            if (start) {
                mp.start();
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_PLAY));
            }
            int listened = DbHelper.getInstance(getApplicationContext()).getEpisodeListened(episode.getEpURL());
            Log.d("PPS_SERVICE", String.valueOf(listened));
            if (listened > 0)
                mp.seekTo(listened);
        }
    }

    public void resumePlayback() {
        mp.start();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_PLAY));
        ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(notificationId, buildNotif(false));
    }

    public void pausePlayback(){
        saveProgress();
        mp.pause();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_PAUSE));
        ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(notificationId, buildNotif(true));
    }

    public void forwardPlayback() {
        if (mp.getCurrentPosition() + skipForward <= mp.getDuration())
            mp.seekTo(mp.getCurrentPosition() + skipForward);
        else mp.seekTo(mp.getDuration());
    }

    public void rewindPlayback(){
        if (mp.getCurrentPosition() - rewind >= 0)
            mp.seekTo(mp.getCurrentPosition()-rewind);
        else mp.seekTo(0);
    }

    public boolean isPlaying(){
        if (mp == null) return false;
        else
            return mp.isPlaying();
    }

    public boolean isStarted(){
        return (mp!=null);
    }

    public Episode getEpisode(){
        return episode;
    }

    public int getProgress(){
        if (mp != null){
            return mp.getCurrentPosition();  //return in seconds
        }
        return -1;
    }

    public void setProgress(int progress){
        mp.seekTo(progress);
        if (mp.isPlaying())
            mp.start();
    }

    private void saveProgress(){ saveProgress(false);}

    private void saveProgress(boolean finished){
        if (episode != null) {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.file_setting),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            if (finished) {
                editor.putInt(getApplicationContext().getString(R.string.listened_setting), -1);
                DbHelper.getInstance(getApplicationContext()).updateEpisode(episode.getEpURL(), 0);
            }
            else {
                editor.putInt(getApplicationContext().getString(R.string.listened_setting) , getProgress());
                DbHelper.getInstance(getApplicationContext()).updateEpisode(episode.getEpURL(), getProgress());
            }
            editor.putString(getApplicationContext().getString(R.string.episode_listen_setting)
                    ,episode.getEpURL());
            editor.apply();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class PlayerBinder extends Binder{
        public PodcastPlayerService getService()
        {
            return PodcastPlayerService.this;
        }
    }

    //TODO implement Audio Focus
    //TODO include bluetooth controls
    private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("RECEIVER_BROADCAST","MEDIA ACTION");
            if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())){
                KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event != null)
                    switch (event.getAction()){
                        case(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE):
                            pausePlayback();
                            break;
                        case(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD):
                            forwardPlayback();
                            break;
                    }
            }
        }
    };

    private BroadcastReceiver audioChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG) && mp != null
                    && intent.getIntExtra("state",4)==0)
                mp.pause();
        }
    };

    private BroadcastReceiver fileChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(FileManager.ACTION_DELETE)){
                String name = intent.getStringExtra(FileManager.EP_KEY_EXTRA);
                Log.d("PPS_SERVICE","Delete episode " + name );
                if (name.equals(URLUtil.guessFileName(episode.getEpURL(), null,null)))
                    finishPlayback(true);
            }
        }
    };
}

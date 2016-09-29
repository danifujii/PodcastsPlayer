package com.example.daniel.podcastplayer.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.webkit.URLUtil;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.activity.PlayerActivity;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.data.Podcast;

import java.io.File;
import java.io.IOException;

import static android.provider.ContactsContract.Intents.Insert.ACTION;


public class PodcastPlayerService extends Service {

    private final static int skipForward = 30000;   //30 seconds
    private final static int rewind = 10000;   //10 seconds
    private final static int notificationId = 212221;

    private MediaPlayer mp = null;
    private final IBinder binder = new PlayerBinder();
    private Episode episode = null;

    private BroadcastReceiver audioChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG) && mp != null
                    && intent.getIntExtra("state",4)==0)
                mp.pause();
        }
    };

    private Notification buildNotif(){
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), PlayerActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        //TODO ajustar bien esto, no se muestra muy bien
        Notification notif = new NotificationCompat.Builder(this)
                .setContentTitle("Playing")
                .setTicker("Ticker")
                .setContentText("Content")
                .setContentIntent(pi).build();
        return notif;
    }

    //private Context context = null; //TODO esto cambiar dsp si el service cuenta como context
    //private static PodcastPlayerService mInstance = new PodcastPlayerService();
    public PodcastPlayerService(){}

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(audioChangeReceiver,new IntentFilter(AudioManager.ACTION_HEADSET_PLUG));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        if (mp!=null){
            saveProgress();
            mp.release();
        }
        unregisterReceiver(audioChangeReceiver);
    }

    public void startPlayback(Episode e, Context context){
        startPlayback(e,context,true);
    }

    //start=false is just for prepare, and not start playback
    public void startPlayback(Episode e, Context context, final boolean start){
        if (mp == null) {
            mp = new MediaPlayer();
            mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        if (episode==null || !episode.getEpURL().equals(e.getEpURL()))
            try {
                saveProgress();
                mp.reset();
                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        if (Build.VERSION.SDK_INT >= 23) {  //TODO now SeekBar and time should update faster (maybe?)
                            PlaybackParams params = new PlaybackParams();
                            params.setPitch(1.0f);
                            params.setSpeed(2.0f);
                            mp.setPlaybackParams(params);
                        }
                        startPlayback(start); }
                });
                mp.setDataSource(context,
                        Uri.parse(getEpisodeFile(URLUtil.guessFileName(e.getEpURL(),null,null)
                                , context).getAbsolutePath()));
                mp.prepareAsync();
            } catch (IOException excep) {
                excep.printStackTrace();
            }
            //if same episode, already prepared so resume playback
        else if (episode.getEpURL().equals(e.getEpURL()) && start) mp.start();
        this.episode = e;
    }

    private void startPlayback(boolean start){
        if (episode != null) {
            if (start)
                mp.start();
            int listened = DbHelper.getInstance(getApplicationContext()).getEpisodeListened(episode.getEpURL());
            if (listened > 0)
                mp.seekTo(listened);
        }
    }

    public void resumePlayback() { mp.start(); }

    public void pausePlayback(){
        saveProgress();
        mp.pause();
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
        mp.start();
    }

    private File getEpisodeFile(String filename, Context context){
        File f = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/com.example.daniel.podcastplayer/files/"
                        + context.getFilesDir().getAbsolutePath() + "/" + filename);
        return f;
    }

    private void saveProgress(){
        if (episode != null) {
            DbHelper.getInstance(getApplicationContext()).updateEpisode(episode.getEpURL(), getProgress());
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.file_setting),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getApplicationContext().getString(R.string.episode_listen_setting)
                    ,episode.getEpURL());
            editor.putInt(getApplicationContext().getString(R.string.listened_setting)
                    , getProgress());
            editor.apply();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        startForeground(notificationId, buildNotif());
        return binder;
    }

    public class PlayerBinder extends Binder{
        public PodcastPlayerService getService()
        {
            return PodcastPlayerService.this;
        }
    }

    //TODO implement Audio Focus
}

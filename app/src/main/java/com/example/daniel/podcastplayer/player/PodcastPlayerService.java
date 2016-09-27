package com.example.daniel.podcastplayer.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;

import java.io.File;
import java.io.IOException;


public class PodcastPlayerService {

    private final static int skipForward = 30000;   //30 seconds
    private final static int rewind = 10000;   //10 seconds

    private MediaPlayer mp = null;
    private Episode episode = null;

    private Context context = null; //TODO esto cambiar dsp si el service cuenta como context
    private static PodcastPlayerService mInstance = new PodcastPlayerService();
    private PodcastPlayerService(){}

    public static PodcastPlayerService getInstance(){
        return mInstance;
    }

    public void startPlayback(Episode e, Context context){
        startPlayback(e,context,true);
    }

    //start=false is just for prepare, and not start playback
    public void startPlayback(Episode e, Context context, final boolean start){
        if (mp == null) {
            mp = new MediaPlayer();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        if (episode==null || !episode.getEpURL().equals(e.getEpURL()))
            try {
                saveProgress();
                mp.reset();
                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) { startPlayback(start); }
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
        this.context = context;
    }

    private void startPlayback(boolean start){
        if (episode != null) {
            if (start)
                mp.start();
            int listened = DbHelper.getInstance(context).getEpisodeListened(episode.getEpURL());
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

    public void releasePlayer(){
        //TODO use this in every activity that could be stopped with this playing: Podcast and Main Act for sure
        if (mp != null) {
            saveProgress();
            mp.release();
            mp = null;
        }
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
            DbHelper.getInstance(context).updateEpisode(episode.getEpURL(), getProgress());
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.file_setting),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(context.getString(R.string.episode_listen_setting),episode.getEpURL());
            editor.putInt(context.getString(R.string.listened_setting), getProgress());
            editor.apply();
        }
    }
}

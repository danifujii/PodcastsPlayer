package com.example.daniel.podcastplayer.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.webkit.URLUtil;

import com.example.daniel.podcastplayer.data.Episode;

import java.io.File;
import java.io.IOException;


public class PodcastPlayerService {

    private MediaPlayer mp = null;
    private Episode episode = null;

    private static PodcastPlayerService mInstance = new PodcastPlayerService();
    private PodcastPlayerService(){}

    public static PodcastPlayerService getInstance(){
        return mInstance;
    }

    public void startPlayback(Episode e, Context context){
        if (mp == null) {
            mp = new MediaPlayer();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        if (episode==null || !episode.getEpURL().equals(e.getEpURL()))
            try {
                mp.reset();
                mp.setDataSource(context,
                        Uri.parse(getEpisodeFile(URLUtil.guessFileName(e.getEpURL(),null,null)
                                , context).getAbsolutePath()));
                mp.prepareAsync();
            } catch (IOException excep) {
                excep.printStackTrace();
            }
        mp.start();
        this.episode = e;
    }

    public void resumePlayback() { mp.start(); }

    public void pausePlayback(){
        mp.pause();
    }

    public boolean isPlaying(){
        if (mp == null) return false;
        else
            return mp.isPlaying();
    }

    public void releasePlayer(){
        //TODO use this in every activity that could be stopped with this playing: Podcast and Main Act for sure
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }

    public Episode getEpisode(){
        return episode;
    }

    public int getProgress(){
        if (mp != null){
            return mp.getCurrentPosition();
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
}

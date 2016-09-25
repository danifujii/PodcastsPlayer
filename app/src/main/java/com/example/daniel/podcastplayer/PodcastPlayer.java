package com.example.daniel.podcastplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;


public class PodcastPlayer {

    private MediaPlayer mp = null;
    private String filename = "";

    private static PodcastPlayer mInstance = new PodcastPlayer();
    private PodcastPlayer(){}

    public static PodcastPlayer getInstance(){
        return mInstance;
    }

    public void startPlayback(String filename, Context context){
        if (mp == null) {
            mp = new MediaPlayer();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        if (!this.filename.equals(filename))
            try {
                mp.reset();
                mp.setDataSource(context,
                        Uri.parse(getEpisode(filename, context).getAbsolutePath()));
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        mp.start();
        this.filename = filename;
    }

    public void pausePlayback(){
        mp.pause();
    }

    public boolean isPlaying(){
        if (mp == null) return false;
        else
            return mp.isPlaying();
    }

    public void releasePlayer(){
        if (mp != null)
            mp.release();
    }

    public File getEpisode(String filename, Context context){
        File f = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/com.example.daniel.podcastplayer/files/"
                        + context.getFilesDir().getAbsolutePath() + "/" + filename);
        return f;
    }
}

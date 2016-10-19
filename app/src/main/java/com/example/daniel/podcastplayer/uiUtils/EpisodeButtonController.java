package com.example.daniel.podcastplayer.uiUtils;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.activity.ServiceActivity;
import com.example.daniel.podcastplayer.adapter.EpisodeAdapter;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.data.FileManager;
import com.example.daniel.podcastplayer.download.Downloader;
import com.example.daniel.podcastplayer.player.PodcastPlayerService;


public class EpisodeButtonController implements View.OnClickListener{
    private ImageButton button;
    private ServiceActivity act;
    private Episode ep;

    private int buttonColor = Color.BLACK;

    public EpisodeButtonController(ImageButton button, ServiceActivity activity, Episode episode, int color){
        this.button = button;
        this.act = activity;
        this.ep = episode;
        this.buttonColor = color;

        if (!Downloader.isDownloading(episode.getEpURL())){
            if (FileManager.getEpisodeFile(activity,episode).exists())
                changeImageButton(Icons.PLAY.ordinal());
            else changeImageButton(Icons.DOWNLOAD.ordinal());
        }
        else
            changeImageButton(Icons.CANCEL.ordinal());
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!Downloader.isDownloading(ep.getEpURL())) {         //no esta descargando actualmente
            if (FileManager.getEpisodeFile(act,ep).exists()){                              //si encuentra el archivo, puede reproducir
                PodcastPlayerService player = act.getService();
                if (player != null) {
                    player.startPlayback(ep, act);
                    act.setupPlayerUI();
                }
            }else {                                             //si no esta, comenzar descarga
                if (Downloader.isConnected(v.getContext(), false)) {
                    Downloader.explicitDownloadEpisode(v.getContext(), ep, this);
                    changeImageButton(Icons.CANCEL.ordinal());
                } else Snackbar.make(v, act.getString(R.string.error_no_connection),
                        Snackbar.LENGTH_LONG).show();
            }
        } else{
            Downloader.cancelDownload(act, ep.getEpURL());
            changeImageButton(Icons.DOWNLOAD.ordinal());
        }
    }

    public enum Icons {DOWNLOAD, CANCEL, PLAY}

    public void changeImageButton(int type){
        Icons icon = Icons.values()[type];
        switch (icon){
            case DOWNLOAD:{
                button.setImageDrawable(
                        ColorPicker.getColoredDrawable(act, buttonColor, BitmapFactory.decodeResource(button.getResources()
                                , R.drawable.ic_file_download_black_24dp)));
                break;
            }
            case CANCEL:{
                button.setImageDrawable(
                        ColorPicker.getColoredDrawable(act, buttonColor, BitmapFactory.decodeResource(button.getResources()
                                , R.drawable.ic_close_black_24dp)));
                break;
            }
            case PLAY:{
                button.setImageDrawable(
                        ColorPicker.getColoredDrawable(act, buttonColor, BitmapFactory.decodeResource(button.getResources()
                                , R.drawable.ic_play_circle_outline_black_24dp)));
                break;
            }
        }
    }
}

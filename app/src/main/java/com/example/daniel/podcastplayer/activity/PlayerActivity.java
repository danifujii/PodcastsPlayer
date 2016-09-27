package com.example.daniel.podcastplayer.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.player.PlayerSheetManager;
import com.example.daniel.podcastplayer.player.PodcastPlayerService;

import java.io.File;

public class PlayerActivity extends AppCompatActivity {

    private PodcastPlayerService service;
    private SeekBar progressBar;
    private TextView progressTV;

    //Progress text
    private final static String divider = "/";
    private String length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
    }

    private void setupPlayerUI(){
        progressTV = (TextView) findViewById(R.id.player_progress_tv);
        progressBar = (SeekBar)findViewById(R.id.player_progress_bar);
        final ImageButton play = (ImageButton)findViewById(R.id.player_play_button);

        service = PodcastPlayerService.getInstance();
        Episode e = service.getEpisode();

        ImageView artwork = (ImageView)findViewById(R.id.player_artwork_iv);
        File image = new File(getApplicationInfo().dataDir + "/Artwork", e.getPodcastId() + ".png");
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
        artwork.setImageBitmap(bitmap);

        TextView epTV = (TextView) findViewById(R.id.player_ep_tv);
        if (epTV != null)
            epTV.setText(e.getEpTitle());

        if (play != null){
            if (service.isPlaying())
                play.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_pause_black_48dp));
            else play.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_play_arrow_black_48dp));

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeButtonIcon(play);
                    if (service.isPlaying())
                        service.pausePlayback();
                    else
                        service.resumePlayback();
                }
            });
        }

        //TODO Cuando termina la reproducción, no funciona muy bien el progress bar ni se cambia el boton.
        progressBar.setMax(service.getEpisode().getLength());
        progressBar.setProgress(service.getProgress());
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    service.setProgress(progress);
                    progressTV.setText(getTime(service.getProgress()) + divider + length);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        new Thread(new Runnable() { //TODO preguntar si esta bien este approach
            @Override
            public void run() {
                while (progressBar.getProgress() < progressBar.getMax()){
                    if (service.isPlaying()) {
                        progressBar.setProgress(progressBar.getProgress() + 1);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressTV.setText(getTime(service.getProgress()) + divider + length);
                                if (service.getProgress() == service.getEpisode().getLength())
                                    changeButtonIcon(play);
                            }
                        });
                    }
                    try { Thread.sleep(1000); }
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
            }
        }).start();

        ImageButton rewindButton = (ImageButton) findViewById(R.id.player_rewind_button);
        if (rewindButton !=  null)
            rewindButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    service.rewindPlayback();
                    progressBar.setProgress(service.getProgress());
                    progressTV.setText(getTime(service.getProgress()) + divider + length);
                }
            });

        ImageButton forwardButton = (ImageButton) findViewById(R.id.player_forward_button);
        if (forwardButton != null)
            forwardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    service.forwardPlayback();
                    progressBar.setProgress(service.getProgress());
                    progressTV.setText(getTime(service.getProgress()) + divider + length);
                }
            });

        length = getTime(service.getEpisode().getLength());
        if (progressTV != null)
            progressTV.setText(getTime(service.getProgress()) + divider + length);

        int color = ColorPicker.getArtworkColor(bitmap);
        //Toolbar toolbar = (Toolbar)findViewById(R.id.player_act_toolbar);
        //toolbar.setTitle(DbHelper.getInstance(this).getPodcast(e.getPodcastId()).getPodcastName());
        //toolbar.setBackgroundColor(color);
        findViewById(R.id.ep_bg_view).setBackgroundColor(ColorPicker.getDarkerColor(color));
        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setStatusBarColor(ColorPicker.getDarkerColor(color));
    }

    @Override
    protected void onStart() {
        super.onStart();

        setupPlayerUI();
    }

    private String getTime(int miliSeconds){
        int minutes = (miliSeconds / 1000) / 60;
        int seconds = (miliSeconds / 1000) % 60;
        StringBuilder builder = new StringBuilder();
        if (minutes > 60){
            int hours = minutes / 60;
            minutes = minutes % 60;
            builder.append(hours);
            builder.append(':');
        }
        if (minutes < 10) builder.append('0');
        builder.append(minutes);
        builder.append(":");
        if (seconds < 10) builder.append('0');
        builder.append(seconds);
        return builder.toString();
    }

    private void changeButtonIcon(ImageButton playButton){
        if (service.isPlaying())
            playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_play_arrow_black_48dp));
        else
            playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_pause_black_48dp));
    }
}

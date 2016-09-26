package com.example.daniel.podcastplayer.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.daniel.podcastplayer.R;
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
        service = PodcastPlayerService.getInstance();
        Episode e = service.getEpisode();

        ImageView artwork = (ImageView)findViewById(R.id.player_artwork_iv);
        if (artwork != null){
            File image = new File(getApplicationInfo().dataDir + "/Artwork", e.getPodcastId() + ".png");
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
            artwork.setImageBitmap(bitmap);
        }

        final ImageButton play = (ImageButton)findViewById(R.id.player_play_button);
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

        TextView epTV = (TextView) findViewById(R.id.player_ep_tv);
        if (epTV != null)
            epTV.setText(e.getEpTitle());

        progressBar = (SeekBar)findViewById(R.id.player_progress_bar);
        progressBar.setMax(service.getEpisode().getLength());
        progressBar.setProgress(service.getProgress() / 1000);
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    service.setProgress(progress * 1000);
                    progressTV.setText(getTime(service.getProgress() / 1000) + divider + length);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //TODO capaz cambiar esta mezcla entre miliseconds del MediaPlayer y mi length en seconds
        new Thread(new Runnable() { //TODO preguntar si esta bien este approach
            @Override
            public void run() {
                while (progressBar.getProgress() < progressBar.getMax() && service.isPlaying()){
                        progressBar.setProgress(progressBar.getProgress() + 1);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressTV.setText(getTime(service.getProgress() / 1000) + divider + length);
                            }
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                }
            }
        }).start();

        progressTV = (TextView) findViewById(R.id.player_progress_tv);
        length = getTime(service.getEpisode().getLength());
        if (progressTV != null)
            progressTV.setText(getTime(service.getProgress() / 1000) + divider + length);
    }

    @Override
    protected void onStart() {
        super.onStart();

        setupPlayerUI();
    }

    private String getTime(int allSeconds){
        int minutes = allSeconds / 60;
        int seconds = allSeconds % 60;
        StringBuilder builder = new StringBuilder();
        if (minutes > 60){
            int hours = minutes / 60;
            minutes = minutes % 60;
            builder.append(hours);
            builder.append(':');
            builder.append(minutes);
        }
        else
            builder.append(minutes);
        builder.append(":");
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

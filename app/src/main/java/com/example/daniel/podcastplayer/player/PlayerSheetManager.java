package com.example.daniel.podcastplayer.player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.activity.PlayerActivity;
import com.example.daniel.podcastplayer.activity.ServiceActivity;
import com.example.daniel.podcastplayer.data.Episode;

import java.io.File;

public class PlayerSheetManager {

    private ServiceActivity container;

    public PlayerSheetManager(ServiceActivity act){
        container = act;
    }

    //containter activity has the UI
    public void setSheetInterface(Episode e){
        ViewGroup splayerLayout = (ViewGroup)container.findViewById(R.id.splayer_layout);
        if (splayerLayout != null){
            splayerLayout.setVisibility(View.VISIBLE);

            File image = new File(container.getApplicationInfo().dataDir + "/Artwork", e.getPodcastId() + ".png");
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
            ImageView artworkIV = (ImageView)container.findViewById(R.id.splayer_art_iv);
            if (artworkIV != null)
                artworkIV.setImageBitmap(bitmap);

            TextView episodeTV = (TextView)container.findViewById(R.id.splayer_ep_tv);
            if (episodeTV != null)
                episodeTV.setText(e.getEpTitle());

            setPlayButton();

            splayerLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(new Intent(v.getContext(),PlayerActivity.class));
                }
            });
        }
    }

    private void setPlayButton(){
        final ImageButton playButton = (ImageButton)container.findViewById(R.id.splayer_play_button);
        if (playButton != null){
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PodcastPlayerService player = container.getService();
                    if (player != null)
                        if (player.isPlaying()) {
                            player.pausePlayback();
                            playButton.setImageBitmap(BitmapFactory.decodeResource(v.getResources(),
                                    R.drawable.ic_play_arrow_black_24dp));
                        }else {
                            player.resumePlayback();
                            playButton.setImageBitmap(BitmapFactory.decodeResource(v.getResources(),
                                    R.drawable.ic_pause_black_24dp));
                        }

                }
            });

            //Setup the icon accordingly
            PodcastPlayerService player = container.getService();
            if (player != null && player.isPlaying()) {
                playButton.setImageBitmap(BitmapFactory.decodeResource(container.getResources(),
                      R.drawable.ic_pause_black_24dp));
            }
            else {
                playButton.setImageBitmap(BitmapFactory.decodeResource(container.getResources(),
                        R.drawable.ic_play_arrow_black_24dp));
            }
        }
    }

    public BroadcastReceiver getHandler() { return handler; }

    private BroadcastReceiver handler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case(PodcastPlayerService.ACTION_FINISH):
                    ((ImageButton)container.findViewById(R.id.splayer_play_button))
                        .setImageBitmap(BitmapFactory.decodeResource(container.getResources(),
                                R.drawable.ic_play_arrow_black_24dp));
                    break;
                case(PodcastPlayerService.ACTION_PLAY):
                    ((ImageButton)container.findViewById(R.id.splayer_play_button))
                            .setImageBitmap(BitmapFactory.decodeResource(container.getResources(),
                                    R.drawable.ic_pause_black_24dp));
                    break;
                case(PodcastPlayerService.ACTION_PAUSE):
                    ((ImageButton)container.findViewById(R.id.splayer_play_button))
                            .setImageBitmap(BitmapFactory.decodeResource(container.getResources(),
                                    R.drawable.ic_play_arrow_black_24dp));
                    break;
            }
        }
    };
}

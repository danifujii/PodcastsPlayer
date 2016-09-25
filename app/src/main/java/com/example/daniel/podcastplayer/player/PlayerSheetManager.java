package com.example.daniel.podcastplayer.player;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.data.Episode;

import java.io.File;

/**
 * Created by Daniel on 25/9/2016.
 */

public class PlayerSheetManager {
    //containter activity has the UI
    public void setSheetInterface(Episode e, Activity container){
        if (container.findViewById(R.id.splayer) != null){
            File image = new File(container.getApplicationInfo().dataDir + "/Artwork", e.getPodcastId() + ".png");
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
            ImageView artworkIV = (ImageView)container.findViewById(R.id.splayer_art_iv);
            if (artworkIV != null)
                artworkIV.setImageBitmap(bitmap);

            TextView episodeTV = (TextView)container.findViewById(R.id.splayer_ep_tv);
            if (episodeTV != null)
                episodeTV.setText(e.getEpTitle());

            setPlayButton(container);
        } else Log.d("Player sheet man", "Is null");
    }

    private void setPlayButton(Activity container){
        final ImageButton playButton = (ImageButton)container.findViewById(R.id.splayer_play_button);
        if (playButton != null){
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PodcastPlayerService player = PodcastPlayerService.getInstance();
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
        }
    }
}

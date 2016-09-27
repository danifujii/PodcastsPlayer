package com.example.daniel.podcastplayer.activity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.adapter.EpisodeAdapter;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.data.Podcast;
import com.example.daniel.podcastplayer.download.Downloader;
import com.example.daniel.podcastplayer.player.PlayerSheetManager;
import com.example.daniel.podcastplayer.player.PodcastPlayerService;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PodcastActivity extends AppCompatActivity {

    private BroadcastReceiver downloadReceiver;
    private Downloader.DownloadReceiver dr;
    private Podcast p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast);

        long podcastId = Long.valueOf(getIntent().getExtras().getString(DbHelper.Tbls.COLUMN_ID));
        p = DbHelper.getInstance(this).getPodcast(podcastId);

        //Set podcast data
        TextView title = (TextView)findViewById(R.id.pod_title_tv);
        if (title != null) title.setText(p.getPodcastName());

        TextView artist = (TextView)findViewById(R.id.pod_artist_tv);
        if (artist != null) artist.setText(p.getPodcastArtist());

        File image = new File(getApplicationInfo().dataDir + "/Artwork", p.getPodcastId() + ".png");
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
        if (bitmap != null) {
            ImageView iv = (ImageView)findViewById(R.id.pod_artwork);
            if (iv!=null) iv.setImageBitmap(bitmap);
        }

        Button b = (Button) findViewById(R.id.subscribe_button);
        if (b != null) {
            b.setText(getString(R.string.unsubscribe_button));
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAlertDialog();
                }
            });
        }

        List<Episode> episodes = DbHelper.getInstance(this).getEpisodes(podcastId);
        RecyclerView epsRV = (RecyclerView)findViewById(R.id.episodes_rv);
        if (epsRV != null){
            epsRV.setLayoutManager(new LinearLayoutManager(this));
            epsRV.setAdapter(new EpisodeAdapter(episodes));
        }

        ImageView playerArtwork = (ImageView)findViewById(R.id.splayer_art_iv);
        if (playerArtwork != null){
            playerArtwork.setImageBitmap(bitmap);
        }

        PodcastPlayerService service = PodcastPlayerService.getInstance();
        if (service.isStarted())
            (new PlayerSheetManager()).setSheetInterface(service.getEpisode(),this);
    }

    private void showAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.unsubs_title_dialog));
        builder.setMessage(getString(R.string.unsubs_message_dialog));
        builder.setPositiveButton(R.string.unsubscribe_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DbHelper.getInstance(PodcastActivity.this).deletePodcast(p.getPodcastId());
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {} });
        builder.create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDownloadReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(downloadReceiver);
    }

    private void setDownloadReceiver(){
        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction()))
                    Log.d("POD ACT","Download completed");
            }
        };
        registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

}

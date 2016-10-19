package com.example.daniel.podcastplayer.activity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.adapter.EpisodeAdapter;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.data.FileManager;
import com.example.daniel.podcastplayer.data.Podcast;
import com.example.daniel.podcastplayer.download.Downloader;
import com.example.daniel.podcastplayer.player.PodcastPlayerService;
import com.example.daniel.podcastplayer.uiUtils.ColorPicker;

import java.io.File;
import java.util.List;

public class PodcastActivity extends ServiceActivity {

    private Podcast p;
    private RecyclerView epsRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast);

        int podcastId = Integer.valueOf(getIntent().getExtras().getString(DbHelper.Tbls.COLUMN_ID));
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
        epsRV = (RecyclerView)findViewById(R.id.episodes_rv);
        if (epsRV != null){
            epsRV.setLayoutManager(new LinearLayoutManager(this));
            epsRV.setAdapter(new EpisodeAdapter(episodes));
        }

        ImageView playerArtwork = (ImageView)findViewById(R.id.splayer_art_iv);
        if (playerArtwork != null){
            playerArtwork.setImageBitmap(bitmap);
        }

        if (bound && service.isStarted())
            manager.setSheetInterface(service.getEpisode());

        int artworkColor = ColorPicker.getArtworkColor(bitmap);
        setTitle(p.getPodcastName());
        if (getSupportActionBar()!= null)
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(artworkColor));
            //Toolbar toolbar = (Toolbar)findViewById(R.id.podcast_act_toolbar);
            //toolbar.setTitle(p.getPodcastName());
        if (artworkColor != 0x000000) {
            //((ViewGroup)findViewById(R.id.header_layout)).setBackgroundColor(artworkColor);
            //  toolbar.setBackgroundColor(artworkColor);
             if (Build.VERSION.SDK_INT >= 21)
                getWindow().setStatusBarColor(ColorPicker.getDarkerColor(artworkColor));
        }
    }

    private void showAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.unsubs_title_dialog));
        builder.setMessage(getString(R.string.unsubs_message_dialog));
        builder.setPositiveButton(R.string.unsubscribe_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DbHelper.getInstance(PodcastActivity.this).deletePodcast(p.getPodcastId());
                FileManager.deletePodcast(PodcastActivity.this, p);
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {} });
        builder.create().show();
    }

    @Override
    public void setupPlayerUI() {
        findViewById(R.id.splayer_layout).setVisibility(View.GONE);
        if (service.isStarted())
            manager.setSheetInterface(service.getEpisode());
        else {
            SharedPreferences sp = getSharedPreferences(getString(R.string.file_setting),
                    Context.MODE_PRIVATE);
            int duration = sp.getInt(getString(R.string.listened_setting), -1);
            String epUrl = sp.getString(getString(R.string.episode_listen_setting), "");
            if (!epUrl.isEmpty() && duration > 0) {
                Episode e = DbHelper.getInstance(this).getEpisode(epUrl);
                service.startPlayback(e, this, false);
                manager.setSheetInterface(service.getEpisode());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PodcastPlayerService.ACTION_FINISH);
        filter.addAction(Downloader.ACTION_DOWNLOADED);
        filter.addAction(FileManager.ACTION_DELETE);
        registerReceiver(receiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(localReceiver, filter);
        if (bound)
            setupPlayerUI();

        //Do this in case a download finishes while app is not active,
        //avoiding the broadcast receivers to work
        Downloader.updateDownloads(this);
        epsRV.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
    }


    private BroadcastReceiver localReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case (Downloader.ACTION_DOWNLOADED): {
                    List<Episode> episodes = DbHelper.getInstance(PodcastActivity.this)
                            .getEpisodes(p.getPodcastId());
                    epsRV.setAdapter(new EpisodeAdapter(episodes));
                    break;
                }
                case (PodcastPlayerService.ACTION_FINISH):{
                    epsRV.getAdapter().notifyDataSetChanged();
                    break;
                }
                case (FileManager.ACTION_DELETE):{
                    epsRV.getAdapter().notifyDataSetChanged();
                }
            }
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                epsRV.getAdapter().notifyDataSetChanged();
                Downloader.removeDownload(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1));
            }
        }
    };
}

package com.example.daniel.podcastplayer.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.example.daniel.podcastplayer.download.ResultParser;
import com.example.daniel.podcastplayer.download.Downloader;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class PodcastSearchActivity extends AppCompatActivity
        implements Downloader.OnImageDownloadReceiver, Downloader.OnEpisodeParsedReceiver,
        Downloader.OnPodcastParsedReceiver{

    private RecyclerView rv;
    private Button subsButton;
    private ImageView artwork;

    private List<Episode> parsedEpisodes = null;
    private String desc;
    private Podcast podcast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast_search);

        podcast = (Podcast)getIntent().getParcelableExtra(Podcast.class.getName());

        TextView titleTV = (TextView)findViewById(R.id.pod_title_tv);
        if (titleTV != null) titleTV.setText(podcast.getPodcastName());

        TextView artistTV = (TextView)findViewById(R.id.pod_artist_tv);
        if (artistTV != null) artistTV.setText(podcast.getPodcastArtist());

        artwork = (ImageView)findViewById(R.id.pod_artwork);
        if (artwork!=null) artwork.setImageBitmap(podcast.getArtwork());

        //Download the feed data
        if (podcast.getFeedUrl() != null)       //if opened from category, no feedURL is found
            Downloader.parseEpisodes(podcast.getFeedUrl(), podcast.getPodcastId() ,this);
        else Downloader.parsePodcasts(podcast.getPodcastName(), rv, this);

        //Setup the Subscribe button
        subsButton = (Button)findViewById(R.id.subscribe_button);
        if (subsButton != null) {
            subsButton.setEnabled(false);
            changeSubButtonText(DbHelper.getInstance(this).existsPodcast(podcast.getPodcastId()));
            subsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DbHelper db = DbHelper.getInstance(v.getContext());
                    boolean isSubscribed = false;
                    if (((Button)v).getText().equals(getString(R.string.unsubscribe_button))) {
                        db.deletePodcast(podcast.getPodcastId());
                        FileManager.deletePodcast(PodcastSearchActivity.this, podcast);
                    }
                    else {
                        db.insertPodcast(podcast);
                        isSubscribed = true;
                        saveArtwork();
                        if (parsedEpisodes != null) {
                            for (Episode e : parsedEpisodes)
                                db.insertEpisode(e, podcast.getPodcastId());
                        }
                    }
                    changeSubButtonText(isSubscribed);
                }
            });
        }

        //Hide ImageButton from Episode layout
        findViewById(R.id.ep_download_button).setVisibility(View.INVISIBLE);

        //Dowload high res image
        try{
            Downloader.downloadImage(new URL(podcast.getArtworkURL()),this);
        } catch (MalformedURLException me) { me.printStackTrace(); }
    }

    //Get the appropiate text if user is or not subscribed. If it is, then you should show Unsubs.
    private void changeSubButtonText(boolean isSubscribed){
        String text = (isSubscribed) ?
                getString(R.string.unsubscribe_button) : getString(R.string.subscribe_button);
        subsButton.setText(text);
    }

    public void saveArtwork(){
        try {
            File dir = new File(getApplicationInfo().dataDir + "/Artwork");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File image = new File(dir, String.valueOf(podcast.getPodcastId()) + ".png");
            if (!image.exists()) {      //don't save if it already exists
                FileOutputStream fOut = new FileOutputStream(image);
                if (artwork != null) {
                    Bitmap art = ((BitmapDrawable) artwork.getDrawable()).getBitmap();
                    art.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                }
                fOut.flush();
                fOut.close();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void receiveImage(Bitmap bitmap) {
        if (artwork!=null){
            artwork.setImageBitmap(bitmap);
            //in case sub button was pressed before download finished
            if (subsButton.getText().equals(getString(R.string.unsubscribe_button)))
                saveArtwork();
        }
    }

    @Override
    public void receiveEpisodes(List<Episode> episodes) {
        findViewById(R.id.latest_ep_tv).setVisibility(View.VISIBLE);
        if (episodes.size() > 0){
            ((TextView)findViewById(R.id.ep_title_tv)).setText(episodes.get(0).getEpTitle());
            ((TextView)findViewById(R.id.ep_date_tv)).
                    setText(EpisodeAdapter.getDateFormat(episodes.get(0).getEpDate()));
        }
        TextView descTV = (TextView)findViewById(R.id.pod_desc_tv);
        findViewById(R.id.description_tv).setVisibility(View.VISIBLE);
        desc = ResultParser.getInstance().getDesc();    //Esto fue modificado durante el parsing que
        // ocurre en AsyncTask
        descTV.setText(desc);
        parsedEpisodes = episodes;
        subsButton.setEnabled(true);
    }

    @Override
    public void receivePodcasts(List<Podcast> podcasts) {
        if (podcasts.size() > 0) {
            Downloader.parseEpisodes(podcasts.get(0).getFeedUrl(),
                    podcasts.get(0).getPodcastId(), this);
            podcast = podcasts.get(0);  //this is now the complete one
            try{
                Downloader.downloadImage(new URL(podcast.getArtworkURL()),this);
            } catch (MalformedURLException e){ e.printStackTrace(); }
        }
    }
}

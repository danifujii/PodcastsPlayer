package com.example.daniel.podcastplayer;

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

import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;
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

public class PodcastSearchActivity extends AppCompatActivity implements Downloader.DownloadReceiver{

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

        //NetworkInfo networkInfo = ((ConnectivityManager)
          //      getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        //if (networkInfo != null && networkInfo.isConnected())
            //Download the feed data
            new AsyncTask<URL,Void,List<Episode>>(){
                @Override
                protected List<Episode> doInBackground(URL... params) {
                    HttpURLConnection conn = null;
                    InputStream stream = null;
                    try{
                        conn = (HttpURLConnection) (params[0]).openConnection();
                        conn.connect();
                        stream = conn.getInputStream();
                    } catch (IOException e){e.printStackTrace();}
                    List<Episode> result = ResultParser.getInstance().parseFeed(stream);
                    desc = ResultParser.getInstance().getDesc();
                    try {stream.close();}
                    catch (IOException e) {e.printStackTrace();}
                    if (conn != null) conn.disconnect();
                    return result;
                }

                @Override
                protected void onPostExecute(List<Episode> episodes) {
                    super.onPostExecute(episodes);
                    //EpisodeAdapter adapter = new EpisodeAdapter(episodes);
                    //rv.setAdapter(adapter);
                    if (episodes.size() > 0){
                        ((TextView)findViewById(R.id.ep_title_tv)).setText(episodes.get(0).getEpTitle());
                        ((TextView)findViewById(R.id.ep_date_tv)).setText(episodes.get(0).getEpDate());
                    }
                    TextView descTV = (TextView)findViewById(R.id.pod_desc_tv);
                    descTV.setText(desc);
                    parsedEpisodes = episodes;
                }
            }.execute(podcast.getFeedUrl());

        //Setup the Subscribe button
        subsButton = (Button)findViewById(R.id.subscribe_button);
        if (subsButton != null) {
            changeSubButtonText(DbHelper.getInstance(this).existsPodcast(podcast.getPodcastId()));

            subsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DbHelper db = DbHelper.getInstance(v.getContext());
                    boolean isSubscribed = false;
                    if (((Button)v).getText().equals(getString(R.string.unsubscribe_button)))
                        db.deletePodcast(podcast.getPodcastId());
                    else {
                        db.insertPodcast(podcast);
                        isSubscribed = true;
                        saveArtwork();
                        //TODO consider if episodes where not downloaded before subscribing
                        if (parsedEpisodes != null) {
                            Log.d("PODCAST SEARCH", String.valueOf(parsedEpisodes.size()));
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
            //TODO remove Artwork directory hardcoded. Save such name in a static string somewhere
            File dir = new File(getApplicationInfo().dataDir + "/Artwork");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File image = new File(dir, String.valueOf(podcast.getPodcastId()) + ".png");
            FileOutputStream fOut = new FileOutputStream(image);

            if (artwork != null) {
                Bitmap art = ((BitmapDrawable) artwork.getDrawable()).getBitmap();
                art.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            }
            fOut.flush();
            fOut.close();
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
}

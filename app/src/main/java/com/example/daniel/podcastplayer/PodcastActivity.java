package com.example.daniel.podcastplayer;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.daniel.podcastplayer.adapter.EpisodeAdapter;
import com.example.daniel.podcastplayer.adapter.PodResAdapter;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.data.PodcastRes;
import com.example.daniel.podcastplayer.data.ResultParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class PodcastActivity extends AppCompatActivity {

    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast);

        PodcastRes podcast = (PodcastRes)getIntent().getParcelableExtra(PodcastRes.class.getName());

        TextView titleTV = (TextView)findViewById(R.id.pod_title_tv);
        if (titleTV != null) titleTV.setText(podcast.getPodcastName());

        TextView artistTV = (TextView)findViewById(R.id.pod_artist_tv);
        if (artistTV != null) artistTV.setText(podcast.getPodcastArtist());

        ImageView artwork = (ImageView)findViewById(R.id.pod_artwork);
        if (artwork!=null) artwork.setImageBitmap(podcast.getArtwork());

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
                if (conn != null) conn.disconnect();
                return result;
            }

            @Override
            protected void onPostExecute(List<Episode> episodes) {
                super.onPostExecute(episodes);
                EpisodeAdapter adapter = new EpisodeAdapter(episodes);
                rv.setAdapter(adapter);
            }
        }.execute(podcast.getFeedUrl());

        rv = (RecyclerView)findViewById(R.id.episodes_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }
}

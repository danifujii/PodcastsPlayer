package com.example.daniel.podcastplayer.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Daniel on 22/9/2016.
 */

public class PodcastRes {

    private String podcastName;
    private String podcastArtist;
    private Bitmap artwork;

    private RecyclerView searchRecyclerView;

    public PodcastRes(JSONObject json, RecyclerView rv){
        searchRecyclerView = rv;
        try {
            podcastName = json.getString("trackName");
            podcastArtist = json.getString("artistName");
            //Download the artwork
            new AsyncTask<String,Void,Bitmap>(){
                @Override
                protected Bitmap doInBackground(String... params) {
                    return downloadImage(params[0]);
                }
                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    artwork = bitmap;
                    searchRecyclerView.getAdapter().notifyDataSetChanged();
                }
            }.execute(json.getString("artworkUrl100"));
        }catch (JSONException je) { je.printStackTrace(); }
    }

    public Bitmap downloadImage(String url){
        InputStream is = null;
        HttpURLConnection conn = null;
        Bitmap result = null;
        try {
            conn = (HttpURLConnection) (new URL(url)).openConnection();
            conn.connect();
            is = conn.getInputStream();
            result = BitmapFactory.decodeStream(is);
        }
        catch (IOException e) { e.printStackTrace(); }
        finally{
            if (is != null)
                try {is.close(); }
                catch(IOException e){ e.printStackTrace(); }
            if (conn != null) conn.disconnect();
        }
        return result;
    }

    public String getPodcastName() {
        return podcastName;
    }

    public String getPodcastArtist() {
        return podcastArtist;
    }

    public Bitmap getArtwork(){
        return artwork;
    }
}

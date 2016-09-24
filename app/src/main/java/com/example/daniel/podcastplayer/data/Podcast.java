package com.example.daniel.podcastplayer.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Podcast implements Parcelable{

    private long podcastId;
    private String podcastName;
    private String podcastArtist;
    private Bitmap artwork;
    private URL feedUrl;
    private RecyclerView searchRecyclerView;

    public Podcast(JSONObject json, RecyclerView rv){
        searchRecyclerView = rv;
        try {
            podcastName = json.getString("trackName");
            podcastArtist = json.getString("artistName");
            podcastId = json.getLong("collectionId");
            feedUrl = new URL(json.getString("feedUrl"));
            //Download the artwork
            downloadImage(json);
        }catch (JSONException | MalformedURLException je) { je.printStackTrace(); }
    }

    public void downloadImage(JSONObject json) throws JSONException{
        new AsyncTask<String,Void,Bitmap>(){
            @Override
            protected Bitmap doInBackground(String... params) {
                String url = params[0];
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
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                artwork = bitmap;
                searchRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }.execute(json.getString("artworkUrl100"));
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

    public URL getFeedUrl(){ return feedUrl; }

    public long getPodcastId() {
        return podcastId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private Podcast(Parcel in){
        podcastId = in.readLong();
        podcastName = in.readString();
        podcastArtist = in.readString();
        try { feedUrl = new URL(in.readString()); }
        catch (MalformedURLException me) { me.printStackTrace(); }
        artwork = Bitmap.CREATOR.createFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(podcastId);
        dest.writeString(podcastName);
        dest.writeString(podcastArtist);
        dest.writeString(feedUrl.toString());
        if (artwork != null)
            artwork.writeToParcel(dest,flags);
    }

    public static final Parcelable.Creator<Podcast> CREATOR
            = new Parcelable.Creator<Podcast>() {
        public Podcast createFromParcel(Parcel in) {
            return new Podcast(in);
        }

        public Podcast[] newArray(int size) {
            return new Podcast[size];
        }
    };
}

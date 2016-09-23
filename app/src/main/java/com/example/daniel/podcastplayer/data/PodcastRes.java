package com.example.daniel.podcastplayer.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PodcastRes implements Parcelable{

    private String podcastName;
    private String podcastArtist;
    private Bitmap artwork;
    private URL feedUrl;
    private RecyclerView searchRecyclerView;

    public PodcastRes(JSONObject json, RecyclerView rv){
        searchRecyclerView = rv;
        try {
            podcastName = json.getString("trackName");
            podcastArtist = json.getString("artistName");
            feedUrl = new URL(json.getString("feedUrl"));
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
        }catch (JSONException | MalformedURLException je) { je.printStackTrace(); }
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

    public URL getFeedUrl(){ return feedUrl; }

    @Override
    public int describeContents() {
        return 0;
    }

    private PodcastRes(Parcel in){
        podcastName = in.readString();
        podcastArtist = in.readString();
        try { feedUrl = new URL(in.readString()); }
        catch (MalformedURLException me) { me.printStackTrace(); }
        artwork = Bitmap.CREATOR.createFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(podcastName);
        dest.writeString(podcastArtist);
        dest.writeString(feedUrl.toString());
        artwork.writeToParcel(dest,flags);
    }

    public static final Parcelable.Creator<PodcastRes> CREATOR
            = new Parcelable.Creator<PodcastRes>() {
        public PodcastRes createFromParcel(Parcel in) {
            return new PodcastRes(in);
        }

        public PodcastRes[] newArray(int size) {
            return new PodcastRes[size];
        }
    };
}

package com.example.daniel.podcastplayer.download;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.adapter.EpisodeAdapter;
import com.example.daniel.podcastplayer.adapter.PodResAdapter;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.data.Podcast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static android.R.attr.bitmap;

/**
 * Created by Daniel on 23/9/2016.
 */

public class Downloader {

    public interface OnImageDownloadReceiver { void receiveImage(Bitmap bitmap); }
    public interface OnEpisodeParsedReceiver{ void receiveEpisodes(List<Episode> episodes); }
    public interface OnPodcastParsedReceiver{ void receivePodcasts(List<Podcast> podcast); }
    public static final String ACTION_DOWNLOADED = "action_downloaded";

    public static void downloadImage(URL url, final OnImageDownloadReceiver re){
        new AsyncTask<URL,Void,Bitmap>(){
            @Override
            protected Bitmap doInBackground(URL... params) {
                URL url = params[0];
                InputStream is = null;
                HttpURLConnection conn = null;
                Bitmap result = null;
                try {
                    conn = (HttpURLConnection) url.openConnection();
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
                re.receiveImage(bitmap);
            }
        }.execute(url);
    }

    public static void parseEpisodes(URL url, final int podcastId, final OnEpisodeParsedReceiver er){
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
                List<Episode> result = ResultParser.getInstance().parseFeed(stream, podcastId);
                try {stream.close();}
                catch (IOException e) {e.printStackTrace();}
                if (conn != null) conn.disconnect();
                return result;
            }

            @Override
            protected void onPostExecute(List<Episode> episodes) {
                super.onPostExecute(episodes);
                er.receiveEpisodes(episodes);
            }
        }.execute(url);
    }

    public static void parsePodcasts(String term, final RecyclerView rv, final OnPodcastParsedReceiver receiver){
        new AsyncTask<String,Void,List<Podcast>>(){
            @Override
            protected List<Podcast> doInBackground(String... params) {
                BufferedReader reader = null;
                HttpURLConnection huc = null;
                try {
                    URL myurl = new URL("https://itunes.apple.com/search?term=" + params[0] + "&media=podcast");
                    huc = (HttpURLConnection) myurl.openConnection();
                    huc.connect();
                    InputStream stream = huc.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder buffer = new StringBuilder();
                    String line = "";
                    while ((line = reader.readLine())!=null)
                        buffer.append(line);
                    return  ResultParser.getInstance().parseSearch(buffer.toString(),rv);
                } catch(IOException ioe) { ioe.printStackTrace(); }
                finally {
                    if (huc != null) huc.disconnect();
                    try { if (reader != null) reader.close(); }
                    catch (IOException ie){ ie.printStackTrace(); }
                }
                return null;
            }
            @Override
            protected void onPostExecute(List<Podcast> podcasts) {
                super.onPostExecute(podcasts);
                receiver.receivePodcasts(podcasts);
            }
        }.execute(term);

    }

    //Update podcasts episode, syncing and seeing if new episodes are available
    public static void updatePodcasts(final Context context){
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                for (Podcast p : DbHelper.getInstance(context).getPodcasts()){
                    OnEpisodeParsedReceiver receiver = new OnEpisodeParsedReceiver() {
                        @Override
                        public void receiveEpisodes(List<Episode> episodes) {
                            DbHelper db = DbHelper.getInstance(context);
                            if (episodes.size()>0) {
                                int podcastId = episodes.get(0).getPodcastId();
                                Episode last = db.getLastEpisode(podcastId);
                                for (Episode e : episodes) {
                                    if (!e.getEpURL().equals(last.getEpURL()))
                                        db.insertEpisode(e, podcastId);
                                    else break;
                                }
                            }
                        }
                    };
                    parseEpisodes(p.getFeedUrl(), p.getPodcastId(), receiver);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_DOWNLOADED));
            }
        }.execute();
    }
}

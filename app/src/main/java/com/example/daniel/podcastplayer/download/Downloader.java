package com.example.daniel.podcastplayer.download;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.webkit.URLUtil;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.R.attr.bitmap;

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
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url);
        //.execute(url);
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
                List<Episode> result = new ArrayList<Episode>();
                if (stream != null) {                           //can be null if connection fails
                    result = ResultParser.getInstance().parseFeed(stream, podcastId);
                    try {
                        stream.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (conn != null) conn.disconnect();
                return result;
            }

            @Override
            protected void onPostExecute(List<Episode> episodes) {
                super.onPostExecute(episodes);
                er.receiveEpisodes(episodes);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url);
        //.execute(url);
    }

    public static void parsePodcasts(String term, final RecyclerView rv, final OnPodcastParsedReceiver receiver){
        new AsyncTask<String,Void,List<Podcast>>(){
            @Override
            protected List<Podcast> doInBackground(String... params) {
                BufferedReader reader = null;
                HttpURLConnection huc = null;
                try {
                    URL myurl = new URL(getCorrectURL("https://itunes.apple.com/search?term=" + params[0] + "&media=podcast"));
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
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,term.replace(' ','+'));
        //.execute(term);
    }

    public static void parseCategory(int category, final RecyclerView rv, final OnPodcastParsedReceiver receiver){
        new AsyncTask<String,Void,List<Podcast>>(){
            @Override
            protected List<Podcast> doInBackground(String... params) {
                HttpURLConnection huc = null;
                InputStream stream = null;
                try {
                    URL myurl = new URL(getCorrectURL("https://itunes.apple.com/us/rss/topaudiopodcasts/genre="
                            + params[0] + "/limit=50/xml"));
                    huc = (HttpURLConnection) myurl.openConnection();
                    huc.connect();
                    stream = huc.getInputStream();
                    return  ResultParser.getInstance().parseTopCategory(stream,rv);
                } catch(IOException ioe) { ioe.printStackTrace(); }
                finally {
                    if (huc != null) huc.disconnect();
                    try { if (stream != null) stream.close(); }
                    catch (IOException ie){ ie.printStackTrace(); }
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<Podcast> podcasts) {
                super.onPostExecute(podcasts);
                receiver.receivePodcasts(podcasts);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(category));
        //.execute(String.valueOf(category));
    }

    //Update podcasts episode, syncing and seeing if new episodes are available
    public static void updatePodcasts(final Context context){
        DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                final Set<String> prefs = PreferenceManager.getDefaultSharedPreferences(context)
                        .getStringSet(context.getString(R.string.automatic_download_pref),null);
                for (Podcast p : DbHelper.getInstance(context).getPodcasts()){
                    OnEpisodeParsedReceiver receiver = new OnEpisodeParsedReceiver() {
                        @Override
                        public void receiveEpisodes(List<Episode> episodes) {
                            DbHelper db = DbHelper.getInstance(context);
                            if (episodes.size()>0) {
                                int podcastId = episodes.get(0).getPodcastId();
                                Episode last = db.getLastEpisode(podcastId);
                                for (Episode e : episodes) {
                                    if (!e.getEpURL().equals(last.getEpURL())) {
                                        db.insertEpisode(e, podcastId);
                                        if (prefs.contains(String.valueOf(e.getPodcastId()))        //if automatic donwload is active, download episode
                                                && isCharging(context))
                                            downloadEpisode(context, e);
                                    }
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
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        //.execute();
    }

    public static void downloadEpisode(Context context, Episode ep){  //if the user was explicit on downloading an episode
            DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        //TODO just in WIFI
            Uri uri = Uri.parse(ep.getEpURL());
            mgr.enqueue(new DownloadManager.Request(uri)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                    .setTitle("Downloading episode")
                    .setDescription(ep.getEpTitle())
                    .setDestinationInExternalFilesDir(context
                            , context.getFilesDir().getAbsolutePath()
                            , URLUtil.guessFileName(ep.getEpURL(), null, null)));
    }

    public static boolean isCharging(Context context){
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        return isCharging;
    }

    private static String getCorrectURL(String og){
        //TODO complete with other special characters
        return og.replace("%","%25");
    }
}
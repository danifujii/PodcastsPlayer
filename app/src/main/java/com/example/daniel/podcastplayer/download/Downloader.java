package com.example.daniel.podcastplayer.download;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Podcast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.R.attr.bitmap;

/**
 * Created by Daniel on 23/9/2016.
 */

public class Downloader {

    public interface DownloadReceiver{
        void receiveImage(Bitmap bitmap);
    }

    public static void downloadImage(URL url, final DownloadReceiver re){
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

    //Update podcasts episode, syncing and seeing if new episodes are available
    public static void updatePodcasts(Context context){
        for (Podcast p : DbHelper.getInstance(context).getPodcasts())
            new AsyncTask<Void,Void,Void>(){
                @Override
                protected Void doInBackground(Void... params) {
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                }
            }.execute();
    }
}

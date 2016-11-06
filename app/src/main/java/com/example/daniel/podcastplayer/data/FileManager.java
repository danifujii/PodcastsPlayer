package com.example.daniel.podcastplayer.data;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;

import com.example.daniel.podcastplayer.player.PlayerQueue;

import java.io.File;
import java.io.IOException;

public class FileManager {
    public static final String ACTION_DELETE = "action_delete";
    public static final String EP_KEY_EXTRA = "ep_key_extra";

    public static final int FULL_SIZE = 1;
    public static final int HALF_SIZE = 2;
    public static final int THIRD_SIZE = 4;
    public static final int FOURTH_SIZE = 8;

    public static String episodePath(Context context, Episode e){
        return context.getFilesDir().getAbsolutePath() + "/" + String.valueOf(e.getPodcastId()) + "/";
    }

    public static File getEpisodeFile(Context context, Episode e){
        return new File(
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/com.example.daniel.podcastplayer/files/"
                        + episodePath(context, e) + e.getFilename());
    }

    public static boolean deleteFile(Context c, Episode e){
        File ep = FileManager.getEpisodeFile(c,e);
        if (ep.exists()) {
            DbHelper.getInstance(c).updateEpisodeNew(e.getEpURL(), false);
            Intent i = new Intent(ACTION_DELETE);
            i.putExtra(EP_KEY_EXTRA, e.getFilename());
            LocalBroadcastManager.getInstance(c).sendBroadcast(i);
            PlayerQueue.getInstance(c).removeEpisode(e, c);
            return ep.delete();
        }
        return false;
    }

    public static void deletePodcast(Context c, Podcast p){
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Android/data/com.example.daniel.podcastplayer/files/"
                + c.getFilesDir().getAbsolutePath() + "/" + String.valueOf(p.getPodcastId()));
        try{
            deleteFile(dir, c);
        } catch (IOException e){ e.printStackTrace(); }
    }

    private static void deleteFile(File f, Context context) throws IOException{
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                if (c.delete()) {
                    Intent i = new Intent(ACTION_DELETE);
                    i.putExtra(EP_KEY_EXTRA, c.getName());
                    context.sendBroadcast(i);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                }
            }
        }
    }

    public static Bitmap getBitmap(Context c, int podcastId, int size){
        File image = new File(c.getApplicationInfo().dataDir + "/Artwork", String.valueOf(podcastId) + ".png");

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = size;

        return BitmapFactory.decodeFile(image.getAbsolutePath(),bitmapOptions);
    }
}

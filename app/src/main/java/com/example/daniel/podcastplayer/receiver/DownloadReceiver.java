package com.example.daniel.podcastplayer.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;


public class DownloadReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
            Log.d("DOWNLOAD RECEIVER","HERE");
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
            Cursor c = manager.query(q);

            if(c.moveToFirst() && c.getCount() > 0) {
                Log.d("DOWNLOAD RECEIVER", c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
            }
        }
    }
}

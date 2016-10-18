package com.example.daniel.podcastplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.daniel.podcastplayer.download.Downloader;


public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)){
            NetworkInfo networkInfo = ((ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (networkInfo!=null && networkInfo.getTypeName().equals("WIFI")) {
                Downloader.updatePodcasts(context);
            }
        }
    }
}

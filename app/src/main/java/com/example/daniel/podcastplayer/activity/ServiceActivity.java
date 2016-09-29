package com.example.daniel.podcastplayer.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import com.example.daniel.podcastplayer.player.PodcastPlayerService;

/**
 * Created by Daniel on 28/9/2016.
 */

public abstract class ServiceActivity extends AppCompatActivity{
    protected PodcastPlayerService service;
    protected boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindService(new Intent(this,PodcastPlayerService.class),connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound)
            unbindService(connection);
    }

    public PodcastPlayerService getService() {
        if (bound)
            return service;
        else return null;
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            PodcastPlayerService.PlayerBinder binder = (PodcastPlayerService.PlayerBinder)serviceBinder;
            service = binder.getService();
            bound = true;
            setupPlayerUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    public abstract void setupPlayerUI();   //method to be called to set the UI one the MediaPlayer is ready
}

package com.example.daniel.podcastplayer.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.ArraySet;
import android.util.Log;
import android.widget.TextView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.activity.PlayerActivity;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerQueue {

    private List<Episode> queue = new ArrayList<>();
    private Episode current;
    private static PlayerQueue instance = null;
    private PlayerQueue(){}

    public static PlayerQueue getInstance(Context context) {
        if (instance == null){
            instance = new PlayerQueue();
            //Loading the queue
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> result = prefs.getStringSet(context.getString(R.string.queueSet), null);

            if (result != null) {
                Episode[] partial = new Episode[result.size()];
                for (String s : result) {
                    //Episodes have format order@epURL
                    int atIndex = s.indexOf('@');
                    int order = Integer.valueOf(s.substring(0, atIndex));
                    partial[order] = DbHelper.getInstance(context).getEpisode(s.substring(atIndex + 1, s.length()));
                }
                for (Episode e : partial)
                    instance.addEpisode(e, context);
            }
        }
        return instance;
    }

    public void addEpisode(Episode e, Context context){ //context is used to save list
        if (e != null) {
            for (Episode ep : queue)
                if (ep.getEpURL().equals(e.getEpURL()))
                    return;
            queue.add(e);
            saveQueue(context);
        }
    }

    public void removeEpisode(int position, Context context){
        if (position >= 0 && position < queue.size()) {
            queue.remove(position);
            saveQueue(context);
        }
    }

    public void removeEpisode(Episode e, Context context){
        if (current!=null && current.getEpURL().equals(e.getEpURL())) {
            current = null;
            return;
        }
        for (int i = 0 ; i < queue.size(); i++)
            if (queue.get(i).getEpURL().equals(e.getEpURL())) {
                queue.remove(i);
                saveQueue(context);
                break;
            }
    }

    public Episode getEpisode(int position){
        if (position>=0 && position < queue.size())
            return queue.get(position);
        return null;
    }

    //This is used when current one is finished
    public Episode getNextEpisode(Context context){
        if(queue.size() > 0){
            Episode e = queue.get(0);
            queue.remove(0);

            current = null;     //setCurrent is called from the service
            saveQueue(context);
            return e;
        }
        return null;
    }

    public List<Episode> getQueue() { return queue; }

    public void moveEpisode(int fromPos, int toPos, Context context){
        if (fromPos < toPos)
            for (int i = fromPos; i < toPos; i++)
                Collections.swap(queue, i, i+1);
        else for (int i = fromPos; i > toPos; i--)
            Collections.swap(queue, i, i-1);
        saveQueue(context);
    }

    public void setCurrent(Episode e, Context context){
        //If this episode was in the queue, remove it from there
        int position = -1;
        for (int i = 0 ; i < queue.size(); i++)
            if (queue.get(i).getEpURL().equals(e.getEpURL())) {
                position = i;
                break;
            }
        if (position >= 0)
            queue.remove(position);
        //Save the one playing before next in the queue
        if (current != null)
            queue.add(0, current);
        current = e;
        saveQueue(context);
    }

    private void saveQueue(Context context){
        Set<String> queueSet = new HashSet<>(queue.size());
        for (int i = 0 ; i < queue.size(); i++){
            queueSet.add(String.valueOf(i) + "@" + queue.get(i).getEpURL());
        }
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putStringSet(context.getString(R.string.queueSet), queueSet);
        editor.apply();
    }

    //TODO Clear queue
}

package com.example.daniel.podcastplayer.player;

import android.util.Log;

import com.example.daniel.podcastplayer.data.Episode;

import java.util.ArrayList;
import java.util.List;

public class PlayerQueue {
    private static PlayerQueue instance = new PlayerQueue();
    private PlayerQueue(){}
    public static PlayerQueue getInstance() { return instance; }

    private List<Episode> queue = new ArrayList<>();

    public void addEpisode(Episode e){
        for (Episode ep : queue)
            if (ep.getEpURL().equals(e.getEpURL()))
                return;
        queue.add(e);
    }

    public void removeEpisode(int position){
        if (position >= 0 && position < queue.size())
            queue.remove(position);
    }

    public Episode getEpisode(int position){
        if (position>=0 && position < queue.size())
            return queue.get(position);
        return null;
    }

    public List<Episode> getQueue() { return queue; }

    //TODO Clear queue
    //TODO Reorder queue
}

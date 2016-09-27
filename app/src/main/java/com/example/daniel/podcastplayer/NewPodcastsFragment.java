package com.example.daniel.podcastplayer;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.daniel.podcastplayer.activity.PodcastActivity;
import com.example.daniel.podcastplayer.adapter.EpisodeAdapter;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewPodcastsFragment extends Fragment {

    private RecyclerView rv;

    public NewPodcastsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_podcasts, container, false);

        rv = (RecyclerView)v.findViewById(R.id.new_episodes_rv);
        rv.setLayoutManager(new LinearLayoutManager(v.getContext()));
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        List<Episode> latest = DbHelper.getInstance(getContext()).getLatestEpisodes();
        rv.setAdapter(new EpisodeAdapter(latest));
    }
}

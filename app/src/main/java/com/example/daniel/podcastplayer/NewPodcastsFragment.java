package com.example.daniel.podcastplayer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.daniel.podcastplayer.adapter.ImageAdapter;
import com.example.daniel.podcastplayer.data.DbHelper;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewPodcastsFragment extends Fragment {


    public NewPodcastsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_podcasts, container, false);
        return v;
    }

}

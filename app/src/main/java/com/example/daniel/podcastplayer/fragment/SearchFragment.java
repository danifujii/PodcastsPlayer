package com.example.daniel.podcastplayer.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.adapter.CategoryAdapter;
import com.example.daniel.podcastplayer.adapter.PodResAdapter;
import com.example.daniel.podcastplayer.data.Podcast;
import com.example.daniel.podcastplayer.download.Downloader;

import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements Downloader.OnPodcastParsedReceiver{

    private RecyclerView rv;
    private ProgressBar progressBar;
    private HashMap<String,Integer> categoriesId;

    public SearchFragment() {} // Required empty public constructor


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        initCategoriesMap();
        rv = (RecyclerView)v.findViewById(R.id.search_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar = (ProgressBar)v.findViewById(R.id.search_progress_bar);

        rv.setAdapter(new CategoryAdapter(categoriesId));
        return v;
    }

    public RecyclerView getRecyclerView(){ return rv; }

    public void setDownloadingUI(){
        rv.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.empty_tv).setVisibility(View.GONE);
    }

    @Override
    public void receivePodcasts(List<Podcast> podcast) {
        progressBar.setVisibility(View.GONE);
        if (podcast.size() > 0) {
            rv.setAdapter(new PodResAdapter(podcast));
            rv.setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.empty_tv).setVisibility(View.GONE);
        } else getActivity().findViewById(R.id.empty_tv).setVisibility(View.VISIBLE);
    }

    private void initCategoriesMap(){
        categoriesId = new HashMap<>();
        categoriesId.put(getString(R.string.cat_arts)       ,1301);
        categoriesId.put(getString(R.string.cat_comedy)     ,1303);
        categoriesId.put(getString(R.string.cat_education)  ,1304);
        categoriesId.put(getString(R.string.cat_kids)       ,1305);
        categoriesId.put(getString(R.string.cat_health)     ,1307);
        categoriesId.put(getString(R.string.cat_tv)         ,1309);
        categoriesId.put(getString(R.string.cat_music)      ,1310);
        categoriesId.put(getString(R.string.cat_news)       ,1311);
        categoriesId.put(getString(R.string.cat_religion)   ,1314);
        categoriesId.put(getString(R.string.cat_science)    ,1315);
        categoriesId.put(getString(R.string.cat_sports)     ,1316);
        categoriesId.put(getString(R.string.cat_technology) ,1318);
        categoriesId.put(getString(R.string.cat_business)   ,1321);
        categoriesId.put(getString(R.string.cat_games)      ,1323);
        categoriesId.put(getString(R.string.cat_society)    ,1324);
        categoriesId.put(getString(R.string.cat_gov)        ,1325);
    }

}

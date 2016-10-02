package com.example.daniel.podcastplayer.fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.adapter.CategoryAdapter;
import com.example.daniel.podcastplayer.adapter.EpisodeAdapter;
import com.example.daniel.podcastplayer.adapter.PodResAdapter;
import com.example.daniel.podcastplayer.data.Podcast;
import com.example.daniel.podcastplayer.download.Downloader;
import com.example.daniel.podcastplayer.download.ResultParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment implements Downloader.OnPodcastParsedReceiver{

    private RecyclerView rv;
    private ProgressBar progressBar;
    private HashMap<String,Integer> categoriesId;

    public SearchFragment() { /*Required empty*/ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        SearchView sv = (SearchView)v.findViewById(R.id.search_view);
        if (sv != null) {
            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    NetworkInfo networkInfo = ((ConnectivityManager)
                            getActivity().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        setDownloadingUI();
                        Downloader.parsePodcasts(query.replace(' ', '+'), rv, SearchFragment.this);
                    } else Snackbar.make(getView(), getString(R.string.error_no_connection),Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                @Override public boolean onQueryTextChange(String newText) { return false; }
            });
        }
        initCategoriesMap();
        rv = (RecyclerView)v.findViewById(R.id.search_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(new CategoryAdapter(categoriesId,this));
        progressBar = (ProgressBar)v.findViewById(R.id.search_progress_bar);
        return v;
    }

    public RecyclerView getRecyclerView() { return rv; }

    public void setDownloadingUI(){
        rv.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void receivePodcasts(List<Podcast> podcast) {
        rv.setAdapter(new PodResAdapter(podcast));
        progressBar.setVisibility(View.GONE);
        rv.setVisibility(View.VISIBLE);
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

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
import android.widget.SearchView;

import com.example.daniel.podcastplayer.R;
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
import java.util.List;

public class SearchFragment extends Fragment implements Downloader.OnPodcastParsedReceiver{

    private RecyclerView rv;

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
                        Downloader.parsePodcasts(query.replace(' ', '+'), rv, SearchFragment.this);
                    } else Snackbar.make(getView(), getString(R.string.error_no_connection),Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                @Override public boolean onQueryTextChange(String newText) { return false; }
            });
        }

        rv = (RecyclerView)v.findViewById(R.id.search_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        return v;
    }


    @Override
    public void receivePodcasts(List<Podcast> podcast) {
        rv.setAdapter(new PodResAdapter(podcast));
    }
}

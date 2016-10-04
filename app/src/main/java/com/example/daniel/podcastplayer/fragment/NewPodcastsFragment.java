package com.example.daniel.podcastplayer.fragment;


import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.activity.PodcastActivity;
import com.example.daniel.podcastplayer.adapter.EpisodeAdapter;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.download.Downloader;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewPodcastsFragment extends Fragment {

    private RecyclerView rv;

    public NewPodcastsFragment() {} // Required empty public constructor

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
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(Downloader.ACTION_DOWNLOADED);
        getActivity().registerReceiver(receiver,filter);
        setRecyclerViewInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    public void setRecyclerViewInfo(){
        List<Episode> latest = DbHelper.getInstance(getContext()).getLatestEpisodes();
        if (latest.isEmpty()) {
            getView().findViewById(R.id.np_message_tv).setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        }
        else
            rv.setAdapter(new EpisodeAdapter(latest));
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case (DownloadManager.ACTION_DOWNLOAD_COMPLETE):
                    rv.getAdapter().notifyDataSetChanged();
                    Downloader.removeDownload(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1));
                    break;
                case (Downloader.ACTION_DOWNLOADED): {
                    setRecyclerViewInfo();
                    break;
                }
            }
        }
    };
}

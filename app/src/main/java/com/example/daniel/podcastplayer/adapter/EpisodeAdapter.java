package com.example.daniel.podcastplayer.adapter;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.podcastplayer.PodcastActivity;
import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.data.PodcastRes;

import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>{

    private List<Episode> data;

    public EpisodeAdapter(List<Episode> data){
        this.data = data;
    }

    @Override
    public EpisodeAdapter.EpisodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.episode_layout, parent, false);

        return new EpisodeAdapter.EpisodeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final EpisodeAdapter.EpisodeViewHolder holder, int position) {
        Episode item = data.get(position);
        holder.nameTV.setText(item.getEpTitle());
        holder.dateTV.setText(item.getEpDate());

        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadManager mgr = (DownloadManager)v.getContext().getSystemService(Context.DOWNLOAD_SERVICE);

                Episode ep = data.get(holder.getAdapterPosition());
                Uri uri = Uri.parse(ep.getEpURL());
                mgr.enqueue(new DownloadManager.Request(uri)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                    .setTitle("Downloading episode")
                    .setDescription(ep.getEpTitle())
                    .setDestinationInExternalFilesDir(v.getContext()
                            ,v.getContext().getApplicationInfo().dataDir
                            ,URLUtil.guessFileName(ep.getEpURL(),null,null)));

            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class EpisodeViewHolder extends RecyclerView.ViewHolder{
        protected TextView nameTV;
        protected TextView dateTV;
        protected ImageButton downloadButton;

        public EpisodeViewHolder(View itemView) {
            super(itemView);
            dateTV = (TextView)itemView.findViewById(R.id.ep_date_tv);
            nameTV = (TextView)itemView.findViewById(R.id.ep_title_tv);
            downloadButton = (ImageButton)itemView.findViewById(R.id.ep_download_button);
        }
    }

}

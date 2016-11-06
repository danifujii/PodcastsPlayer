package com.example.daniel.podcastplayer.adapter;

import android.app.Service;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.activity.PlayerActivity;
import com.example.daniel.podcastplayer.activity.ServiceActivity;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.data.FileManager;
import com.example.daniel.podcastplayer.player.PlayerQueue;

import java.io.File;

public class QueueItemAdapter extends RecyclerView.Adapter<QueueItemAdapter.QueueViewHolder>{

    private ServiceActivity activity;

    public QueueItemAdapter(ServiceActivity act){
        activity = act;
    }

    @Override
    public QueueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.queue_item_layout, parent, false);

        return new QueueViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(QueueViewHolder holder, int position) {
        final Episode e = PlayerQueue.getInstance(holder.artworkIV.getContext()).getEpisode(position);

        if (e != null) {
            holder.artworkIV.setImageBitmap(FileManager.getBitmap(holder.artworkIV.getContext(),
                    e.getPodcastId(), FileManager.THIRD_SIZE));
            holder.titleTV.setText(e.getEpTitle());
            holder.remainingTV.setText(EpisodeAdapter.getRemaining(e.getLength() - e.getListened(), holder.remainingTV.getContext()));
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.getService().startPlayback(e, activity);
                    activity.setupPlayerUI();
                }
            });
        }
    }

    public void moveItem(int fromPos, int toPos){
        PlayerQueue.getInstance(activity).moveEpisode(fromPos, toPos, activity);
        notifyItemMoved(fromPos, toPos);
    }

    @Override
    public int getItemCount() {
        return PlayerQueue.getInstance(activity).getQueue().size();
    }

    public static class QueueViewHolder extends RecyclerView.ViewHolder{
        protected ViewGroup layout;
        protected ImageView artworkIV;
        protected TextView titleTV;
        protected TextView remainingTV;

        public QueueViewHolder(View itemView){
            super(itemView);
            layout = (ViewGroup)itemView.findViewById(R.id.queue_layout);
            artworkIV = (ImageView)itemView.findViewById(R.id.queue_artwork_iv);
            titleTV = (TextView)itemView.findViewById(R.id.queue_title_tv);
            remainingTV = (TextView)itemView.findViewById(R.id.queue_remaining_tv);
        }
    }
}

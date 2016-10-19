package com.example.daniel.podcastplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.data.FileManager;
import com.example.daniel.podcastplayer.player.PlayerQueue;

import java.io.File;

public class QueueItemAdapter extends RecyclerView.Adapter<QueueItemAdapter.QueueViewHolder>{

    @Override
    public QueueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.queue_item_layout, parent, false);

        return new QueueViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(QueueViewHolder holder, int position) {
        Episode e = PlayerQueue.getInstance().getEpisode(position);

        if (e != null) {
            holder.artworkIV.setImageBitmap(FileManager.getBitmap(holder.artworkIV.getContext(), e.getPodcastId()));
            holder.titleTV.setText(e.getEpTitle());
            holder.remainingTV.setText(EpisodeAdapter.getRemaining(e.getLength() - e.getListened(), holder.remainingTV.getContext()));
        }
    }

    @Override
    public int getItemCount() {
        return PlayerQueue.getInstance().getQueue().size();
    }

    public static class QueueViewHolder extends RecyclerView.ViewHolder{
        protected ImageView artworkIV;
        protected TextView titleTV;
        protected TextView remainingTV;

        public QueueViewHolder(View itemView){
            super(itemView);
            artworkIV = (ImageView)itemView.findViewById(R.id.queue_artwork_iv);
            titleTV = (TextView)itemView.findViewById(R.id.queue_title_tv);
            remainingTV = (TextView)itemView.findViewById(R.id.queue_remaining_tv);
        }
    }
}

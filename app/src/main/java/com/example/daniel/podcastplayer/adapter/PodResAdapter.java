package com.example.daniel.podcastplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.data.PodcastRes;

import java.util.List;

public class PodResAdapter extends RecyclerView.Adapter<PodResAdapter.PodResViewHolder> {
    private List<PodcastRes> data;

    public PodResAdapter(List<PodcastRes> data){
        this.data = data;
    }

    @Override
    public PodResViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.result_layout, parent, false);

        return new PodResViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PodResViewHolder holder, int position) {
        PodcastRes item = data.get(position);

        holder.artistTV.setText(item.getPodcastArtist());
        holder.nameTV.setText(item.getPodcastName());
        holder.artworkIV.setImageBitmap(item.getArtwork());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class PodResViewHolder extends RecyclerView.ViewHolder{
        protected ImageView artworkIV;
        protected TextView nameTV;
        protected TextView artistTV;

        public PodResViewHolder(View itemView) {
            super(itemView);
            artworkIV = (ImageView)itemView.findViewById(R.id.artwork_image_view);
            nameTV = (TextView)itemView.findViewById(R.id.pod_title_tv);
            artistTV = (TextView)itemView.findViewById(R.id.pod_artist_tv);
        }
    }
}

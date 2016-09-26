package com.example.daniel.podcastplayer.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.daniel.podcastplayer.activity.PodcastSearchActivity;
import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.data.Podcast;

import java.util.List;

public class PodResAdapter extends RecyclerView.Adapter<PodResAdapter.PodResViewHolder> {
    private List<Podcast> data;

    public PodResAdapter(List<Podcast> data){
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
    public void onBindViewHolder(final PodResViewHolder holder, int position) {
        Podcast item = data.get(position);

        holder.artistTV.setText(item.getPodcastArtist());
        holder.nameTV.setText(item.getPodcastName());
        holder.artworkIV.setImageBitmap(item.getArtwork());

        holder.resultLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                Intent i = new Intent(v.getContext(), PodcastSearchActivity.class);
                i.putExtra(Podcast.class.getName(),data.get(pos));
                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class PodResViewHolder extends RecyclerView.ViewHolder{
        protected ImageView artworkIV;
        protected TextView nameTV;
        protected TextView artistTV;
        protected LinearLayout resultLayout;

        public PodResViewHolder(View itemView) {
            super(itemView);
            artworkIV = (ImageView)itemView.findViewById(R.id.search_artwork_image_view);
            nameTV = (TextView)itemView.findViewById(R.id.search_pod_title_tv);
            artistTV = (TextView)itemView.findViewById(R.id.search_pod_artist_tv);
            resultLayout = (LinearLayout)itemView.findViewById(R.id.search_result_layout);
        }
    }
}

package com.example.daniel.podcastplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.activity.ServiceActivity;
import com.example.daniel.podcastplayer.download.Downloader;
import com.example.daniel.podcastplayer.fragment.SearchFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CatViewHolder>{

    private List<String> cats;
    private HashMap<String,Integer> catsId;
    private Downloader.OnPodcastParsedReceiver activity;

    public CategoryAdapter(HashMap<String,Integer> categories, Downloader.OnPodcastParsedReceiver receiver){
        cats = new ArrayList<>();
        for(String s : categories.keySet())
            cats.add(s);
        Collections.sort(cats);
        activity = receiver;
        catsId = categories;
    }

    @Override
    public CatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.category_layout, parent, false);
        return new CatViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return cats.size();
    }

    @Override
    public void onBindViewHolder(CatViewHolder holder, final int position) {
        holder.catTV.setText(cats.get(position));
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SearchFragment)activity).setDownloadingUI();
                Downloader.parseCategory(catsId.get(cats.get(position)),
                        ((SearchFragment)activity).getRecyclerView(),activity);
            }
        });
    }

    public static class CatViewHolder extends RecyclerView.ViewHolder {
        protected TextView catTV;
        protected LinearLayout layout;
        public CatViewHolder(View itemView) {
            super(itemView);
            catTV = (TextView)itemView.findViewById(R.id.category_tv);
            layout = (LinearLayout)itemView.findViewById(R.id.category_layout);
        }
    }
}

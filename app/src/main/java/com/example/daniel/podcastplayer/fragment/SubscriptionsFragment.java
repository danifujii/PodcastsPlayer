package com.example.daniel.podcastplayer.fragment;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.activity.PodcastActivity;
import com.example.daniel.podcastplayer.adapter.ImageAdapter;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Podcast;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubscriptionsFragment extends Fragment {

    private GridView gv;

    public SubscriptionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_subscriptions, container, false);
        gv = (GridView)v.findViewById(R.id.subs_gv);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = ((ImageAdapter)parent.getAdapter()).getCursor();
                c.moveToPosition(position);
                Intent i = new Intent(view.getContext(), PodcastActivity.class);
                i.putExtra(DbHelper.Tbls.COLUMN_ID, c.getString(c.getColumnIndex(DbHelper.Tbls.COLUMN_ID)));
                startActivity(i);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO cuando esta vacio, el primer podcast suscripto no aparece en el OnResume
        Cursor c = DbHelper.getInstance(getContext()).getPodcastsCursor();

        if (c.getCount() > 0)
            gv.setAdapter(new ImageAdapter(getContext(), c));
        else{
            c.close();
            gv.setVisibility(View.GONE);
            getView().findViewById(R.id.podcasts_message_tv).setVisibility(View.VISIBLE);
        }
    }
}

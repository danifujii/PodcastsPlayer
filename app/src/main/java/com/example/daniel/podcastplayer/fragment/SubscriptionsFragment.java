package com.example.daniel.podcastplayer.fragment;


import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.activity.PodcastActivity;
import com.example.daniel.podcastplayer.adapter.ImageAdapter;
import com.example.daniel.podcastplayer.data.DbHelper;


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

                //Shared element transition
                if (Build.VERSION.SDK_INT >= 21) {
                    ActivityOptions transitionOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                            view, getString(R.string.transition_artwork));
                    startActivity(i, transitionOptions.toBundle());
                } else startActivity(i);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Cursor c = DbHelper.getInstance(getContext()).getPodcastsCursor();
        if (c.getCount() > 0) {
            gv.setAdapter(new ImageAdapter(getContext(), c));
        }
        else{
            c.close();
            gv.setVisibility(View.GONE);
            getView().findViewById(R.id.podcasts_message_tv).setVisibility(View.VISIBLE);
        }
    }
}

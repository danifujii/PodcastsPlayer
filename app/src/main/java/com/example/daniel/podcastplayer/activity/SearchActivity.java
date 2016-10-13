package com.example.daniel.podcastplayer.activity;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.adapter.CategoryAdapter;
import com.example.daniel.podcastplayer.adapter.PodResAdapter;
import com.example.daniel.podcastplayer.data.Podcast;
import com.example.daniel.podcastplayer.download.Downloader;
import com.example.daniel.podcastplayer.fragment.SearchFragment;

import java.util.HashMap;
import java.util.List;

public class SearchActivity extends AppCompatActivity{
    public final static String EXTRA_RESULT_ACT = "extra_result_act"; //search boolean in extras to see if this has to display result
    public final static String EXTRA_CAT = "extra_cat";
    public final static String EXTRA_CAT_NAME = "extra_cat_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        SearchFragment frag = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);
        if (getIntent().getBooleanExtra(EXTRA_RESULT_ACT, false)) {
            frag.setDownloadingUI();
            int cat = getIntent().getIntExtra(EXTRA_CAT, -1);
            if (cat > 0) {
                setTitle(getIntent().getStringExtra(EXTRA_CAT_NAME));
                Downloader.parseCategory(cat, frag.getRecyclerView(), frag);
            }
        }
    }
}

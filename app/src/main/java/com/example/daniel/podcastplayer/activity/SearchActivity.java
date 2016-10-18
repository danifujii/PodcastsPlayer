package com.example.daniel.podcastplayer.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.download.Downloader;
import com.example.daniel.podcastplayer.fragment.SearchFragment;

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

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

import java.util.HashMap;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements Downloader.OnPodcastParsedReceiver{
    public final static String EXTRA_RESULT_ACT = "extra_result_act"; //search boolean in extras to see if this has to display result
    public final static String EXTRA_CAT = "extra_cat";
    public final static String EXTRA_CAT_NAME = "extra_cat_name";

    private RecyclerView rv;
    private ProgressBar progressBar;
    private HashMap<String,Integer> categoriesId;


    //TODO mejorar esto. Se usa solo para mostrar resultados de Categoria!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //SearchView sv = (SearchView)findViewById(R.id.search_view);
        initCategoriesMap();
        rv = (RecyclerView)findViewById(R.id.search_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        progressBar = (ProgressBar)findViewById(R.id.search_progress_bar);

        if (getIntent().getBooleanExtra(EXTRA_RESULT_ACT,false)){
            setDownloadingUI();
            int cat = getIntent().getIntExtra(EXTRA_CAT, -1);
            if (cat > 0) {
                setTitle(getIntent().getStringExtra(EXTRA_CAT_NAME));
                Downloader.parseCategory(cat, rv, this);
            }
        }else {
            setTitle(getString(R.string.tab_search));
            //rv.setAdapter(new CategoryAdapter(categoriesId, this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);


        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.search_menu), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (getSupportActionBar() != null){
                    getSupportActionBar().setBackgroundDrawable(
                            new ColorDrawable(ContextCompat.getColor(SearchActivity.this, R.color.lightGray)));
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (getSupportActionBar() != null){
                    getSupportActionBar().setBackgroundDrawable(
                            new ColorDrawable(ContextCompat.getColor(SearchActivity.this, R.color.colorPrimary)));
                }
                return true;
            }
        });

        if (getIntent().getBooleanExtra(EXTRA_RESULT_ACT,false))
            menu.findItem(R.id.search_menu).setVisible(false);
        else {
            android.support.v7.widget.SearchView mSearchView =
                    (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search_menu));
            if (mSearchView != null)
                mSearchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        NetworkInfo networkInfo = ((ConnectivityManager)
                                getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                        if (networkInfo != null && networkInfo.isConnected()) {
                            setDownloadingUI();
                            Downloader.parsePodcasts(query.replace(' ', '+'), rv, SearchActivity.this);
                        } else Snackbar.make(findViewById(R.id.search_layout),
                                getString(R.string.error_no_connection), Snackbar.LENGTH_SHORT).show();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
        }
        return true;
    }

    public RecyclerView getRecyclerView() { return rv; }

    public void setDownloadingUI(){
        rv.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void receivePodcasts(List<Podcast> podcast) {
        rv.setAdapter(new PodResAdapter(podcast));
        progressBar.setVisibility(View.GONE);
        rv.setVisibility(View.VISIBLE);
    }

    private void initCategoriesMap(){
        categoriesId = new HashMap<>();
        categoriesId.put(getString(R.string.cat_arts)       ,1301);
        categoriesId.put(getString(R.string.cat_comedy)     ,1303);
        categoriesId.put(getString(R.string.cat_education)  ,1304);
        categoriesId.put(getString(R.string.cat_kids)       ,1305);
        categoriesId.put(getString(R.string.cat_health)     ,1307);
        categoriesId.put(getString(R.string.cat_tv)         ,1309);
        categoriesId.put(getString(R.string.cat_music)      ,1310);
        categoriesId.put(getString(R.string.cat_news)       ,1311);
        categoriesId.put(getString(R.string.cat_religion)   ,1314);
        categoriesId.put(getString(R.string.cat_science)    ,1315);
        categoriesId.put(getString(R.string.cat_sports)     ,1316);
        categoriesId.put(getString(R.string.cat_technology) ,1318);
        categoriesId.put(getString(R.string.cat_business)   ,1321);
        categoriesId.put(getString(R.string.cat_games)      ,1323);
        categoriesId.put(getString(R.string.cat_society)    ,1324);
        categoriesId.put(getString(R.string.cat_gov)        ,1325);
    }
}

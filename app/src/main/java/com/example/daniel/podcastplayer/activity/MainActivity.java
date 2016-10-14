package com.example.daniel.podcastplayer.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.daniel.podcastplayer.fragment.HomeFragment;
import com.example.daniel.podcastplayer.fragment.SearchFragment;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.download.Downloader;
import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.player.PodcastPlayerService;

public class MainActivity extends ServiceActivity{

    private SearchFragment search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this,PodcastPlayerService.class)
                .setAction(PodcastPlayerService.ACTION_START));
        Downloader.updatePodcasts(this);

        if (getSupportActionBar() != null)
            getSupportActionBar().setElevation(0);

        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.add(R.id.fragment_layout, new HomeFragment());
        trans.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        final android.support.v7.widget.SearchView mSearchView =
                (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search_menu));
        if (mSearchView != null)
            mSearchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    NetworkInfo networkInfo = ((ConnectivityManager)
                            getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        if (search != null) {
                            search.setDownloadingUI();
                            Downloader.parsePodcasts(query.replace(' ', '+'), search.getRecyclerView()
                                    , search);
                        }
                    } else Snackbar.make(findViewById(R.id.activity_main),
                            getString(R.string.error_no_connection), Snackbar.LENGTH_LONG).show();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.search_menu), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                findViewById(R.id.splayer_layout).setVisibility(View.GONE);
                if (getSupportActionBar() != null){
                    getSupportActionBar().setBackgroundDrawable(
                            new ColorDrawable(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark)));
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (getSupportActionBar() != null){
                    getSupportActionBar().setBackgroundDrawable(
                            new ColorDrawable(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)));
                }
                onBackPressed();
                search = null;
                setupPlayerUI();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case (R.id.search_menu):{
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_layout, search = new SearchFragment())
                        .addToBackStack(null)
                        .commit();
                break;
            }
            case(R.id.settings_menu):{
                startActivity(new Intent(MainActivity.this,SettingsActivity.class));
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,PodcastPlayerService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bound && search == null)
            setupPlayerUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void setupPlayerUI() {
        findViewById(R.id.splayer_layout).setVisibility(View.GONE);
        if (service.isStarted())
            manager.setSheetInterface(service.getEpisode());
        else {
            SharedPreferences sp = getSharedPreferences(getString(R.string.file_setting),
                    Context.MODE_PRIVATE);
            int duration = sp.getInt(getString(R.string.listened_setting), -1);
            String epUrl = sp.getString(getString(R.string.episode_listen_setting), "");
            if (!epUrl.isEmpty() && duration > 0) {
                Episode e = DbHelper.getInstance(this).getEpisode(epUrl);
                service.startPlayback(e, this, false);
                manager.setSheetInterface(service.getEpisode());
            }
        }
    }
}

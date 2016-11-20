package com.example.daniel.podcastplayer.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.download.Downloader;
import com.example.daniel.podcastplayer.fragment.HomeFragment;
import com.example.daniel.podcastplayer.fragment.SearchFragment;
import com.example.daniel.podcastplayer.player.PodcastPlayerService;
import com.google.firebase.crash.FirebaseCrash;

public class MainActivity extends ServiceActivity{

    private final static String homeFragmentTag = "homeFragmentTag";

    private SearchFragment search;
    private Toolbar toolbar;
    private int searchButtonX;
    private int searchButtonY;

    private boolean rotated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Comenzarlo aca asi sigue corriendo en toda la ejecución, independiente de las actividades
        startService(new Intent(this,PodcastPlayerService.class));

        Downloader.updatePodcasts(this);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setElevation(0);
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        if (getSupportFragmentManager().findFragmentByTag(homeFragmentTag) == null) {
            trans.add(R.id.fragment_layout, new HomeFragment(), homeFragmentTag);
            trans.commit();
        }

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                FirebaseCrash.report(e);
                finish();
            }
        });

        rotated = false;
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
                            mSearchView.clearFocus();
                        }
                    } else Snackbar.make(findViewById(R.id.activity_main),
                            getString(R.string.error_no_connection), Snackbar.LENGTH_LONG).show();
                    //InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    //imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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
                    //animateToolbar(R.color.colorPrimary, R.color.colorPrimaryDark);
                    if (Build.VERSION.SDK_INT >= 21)
                        animateToolbar(R.color.colorPrimary, R.color.colorPrimaryDark);
                    else
                        getSupportActionBar().setBackgroundDrawable(
                            new ColorDrawable(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark)));
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (getSupportActionBar() != null){
                    if (Build.VERSION.SDK_INT >= 21)
                        animateToolbarShrink(R.color.colorPrimaryDark, R.color.colorPrimary);
                    else
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
                int[] location = new int[2];
                findViewById(R.id.search_menu).getLocationOnScreen(location);
                searchButtonX = location[0] + 80;
                searchButtonY = location[1];

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
        if (!rotated)
            stopService(new Intent(this,PodcastPlayerService.class));
    }

    @Override
    public void onBackPressed() {
        rotated = false;
        //evitar back normal, que llama a onDestroy cuando estoy en HomeFrag, para que no se pare el servicio
        if (search == null)
            moveTaskToBack(true);
        else super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bound && search == null)
            setupPlayerUI();
    }

    @Override
    public void setupPlayerUI() {
        findViewById(R.id.splayer_layout).setVisibility(View.GONE);
        if (service.isStarted())
            manager.setSheetInterface(service.getEpisode());
        else
            {
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

    @TargetApi(21)
    private void animateToolbar(final int fromColor, final int toColor){
        final View mRevealView = findViewById(R.id.reveal);
        View mRevealBackgroundView = findViewById(R.id.revealBackground);

        Animator animator;
            animator = ViewAnimationUtils.createCircularReveal(
                mRevealView,
                searchButtonX, searchButtonY,
                0, toolbar.getWidth());

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                    mRevealView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, toColor));
            }
        });

            mRevealBackgroundView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, fromColor));
        //animator.setStartDelay(100);
        animator.setDuration(300);
        animator.start();
        mRevealView.setVisibility(View.VISIBLE);
    }

    @TargetApi(21)
    private void animateToolbarShrink(final int fromColor, int toColor){
        final View mRevealView = findViewById(R.id.reveal);
        View mRevealBackgroundView = findViewById(R.id.revealBackground);

        Animator animator;
        animator = ViewAnimationUtils.createCircularReveal(
                mRevealView,
                searchButtonX, searchButtonY,
                toolbar.getWidth(), 0);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mRevealView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, fromColor));
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mRevealView.setVisibility(View.INVISIBLE);
            }
        });

        mRevealBackgroundView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, toColor));
        //animator.setStartDelay(100);
        animator.setDuration(300);
        animator.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        rotated = true;
    }
}

package com.example.daniel.podcastplayer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.fragment.NewPodcastsFragment;
import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.fragment.SearchFragment;
import com.example.daniel.podcastplayer.fragment.SubscriptionsFragment;
import com.example.daniel.podcastplayer.player.PlayerSheetManager;
import com.example.daniel.podcastplayer.player.PodcastPlayerService;

public class MainActivity extends ServiceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager viewPager = (ViewPager)findViewById(R.id.viewPager);
        viewPager.setAdapter(new TabPagerAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(1);
        TabLayout tl = (TabLayout) findViewById(R.id.tabLayout);
        tl.setupWithViewPager(viewPager);
        startService(new Intent(this,PodcastPlayerService.class));  //TODO it has to run continously, but when do we stop it?
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bound)
            setupPlayerUI();
    }

    @Override
    public void setupPlayerUI() {
        if (service.isStarted())
            setPlayerSheet(service);
        else {
            SharedPreferences sp = getSharedPreferences(getString(R.string.file_setting),
                    Context.MODE_PRIVATE);
            int duration = sp.getInt(getString(R.string.listened_setting), -1);
            String epUrl = sp.getString(getString(R.string.episode_listen_setting), "");
            if (!epUrl.isEmpty() && duration > 0) {
                Episode e = DbHelper.getInstance(this).getEpisode(epUrl);
                service.startPlayback(e, this, false);
            }
            setPlayerSheet(service);
        }
    }

    private void setPlayerSheet(PodcastPlayerService pps){
        findViewById(R.id.splayer_layout).setVisibility(View.VISIBLE);
        PlayerSheetManager psm = new PlayerSheetManager();
        psm.setSheetInterface(pps.getEpisode(),this);
    }

    private class TabPagerAdapter extends FragmentStatePagerAdapter {
        private static final int numPages = 3;

        public TabPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public int getCount() {
            return numPages;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0: return new SearchFragment();
                case 1: return new NewPodcastsFragment();
                case 2: return new SubscriptionsFragment();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position){
                case 0: return getString(R.string.tab_discover);
                case 1: return getString(R.string.tab_new);
                case 2: return getString(R.string.tab_podcasts);
            }
            return null;
        }
    }
}

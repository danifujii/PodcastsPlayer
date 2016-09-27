package com.example.daniel.podcastplayer.activity;

import android.support.annotation.IdRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TableLayout;

import com.example.daniel.podcastplayer.NewPodcastsFragment;
import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.SearchFragment;
import com.example.daniel.podcastplayer.SubscriptionsFragment;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.player.PlayerSheetManager;
import com.example.daniel.podcastplayer.player.PodcastPlayerService;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private FrameLayout fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //if (findViewById(R.id.fragment_layout) != null)
        //    getSupportFragmentManager().beginTransaction()
        //            .add(R.id.fragment_layout, new NewPodcastsFragment()).commit();


        viewPager = (ViewPager)findViewById(R.id.viewPager);
        viewPager.setAdapter(new TabPagerAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(1);
        TabLayout tl = (TabLayout) findViewById(R.id.tabLayout);
        tl.setupWithViewPager(viewPager);

    }

    @Override
    protected void onResume() {
        super.onResume();

        PodcastPlayerService pps = PodcastPlayerService.getInstance();
        if (pps.isStarted()){
            findViewById(R.id.splayer_layout).setVisibility(View.VISIBLE);
            PlayerSheetManager psm = new PlayerSheetManager();
            psm.setSheetInterface(pps.getEpisode(),this);
        }
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

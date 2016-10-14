package com.example.daniel.podcastplayer.fragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.fragment.NewPodcastsFragment;
import com.example.daniel.podcastplayer.fragment.SubscriptionsFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private int currentFragment = -1;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        View view = getView();
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new TabPagerAdapter(getChildFragmentManager()));
        TabLayout tl = (TabLayout) view.findViewById(R.id.tabLayout);
        if (currentFragment >= 0)
            viewPager.setCurrentItem(currentFragment);
        tl.setupWithViewPager(viewPager);
    }

    @Override
    public void onPause() {
        super.onPause();

        currentFragment = ((ViewPager) getView().findViewById(R.id.viewPager)).getCurrentItem();
    }

    private class TabPagerAdapter extends FragmentStatePagerAdapter {
        private static final int numPages = 2;

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
                case 0: return new NewPodcastsFragment();
                case 1: return new SubscriptionsFragment();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position){
                case 0: return getString(R.string.tab_new);
                case 1: return getString(R.string.tab_podcasts);
            }
            return null;
        }
    }
}

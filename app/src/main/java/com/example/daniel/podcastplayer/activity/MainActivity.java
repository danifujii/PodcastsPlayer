package com.example.daniel.podcastplayer.activity;

import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.example.daniel.podcastplayer.NewPodcastsFragment;
import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.SearchFragment;
import com.example.daniel.podcastplayer.SubscriptionsFragment;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.player.PlayerSheetManager;
import com.example.daniel.podcastplayer.player.PodcastPlayerService;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

public class MainActivity extends AppCompatActivity {

    private BottomBar bottomBar;
    private FrameLayout fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_layout) != null)
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_layout, new NewPodcastsFragment()).commit();

        bottomBar = (BottomBar)findViewById(R.id.bottom_bar);
        bottomBar.setDefaultTab(R.id.tab_new);
        if (bottomBar!=null)
            bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
                @Override
                public void onTabSelected(@IdRes int tabId) {
                    switch(tabId){
                        case R.id.tab_new:{
                            if (findViewById(R.id.fragment_layout) != null)
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_layout, new NewPodcastsFragment()).commit();
                            break;
                        }
                        case R.id.tab_search:{
                            if (findViewById(R.id.fragment_layout) != null)
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_layout, new SearchFragment()).commit();
                            break;
                        }
                        case R.id.tab_subscriptions:{
                            if (findViewById(R.id.fragment_layout) != null)
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_layout, new SubscriptionsFragment()).commit();
                            break;
                        }
                        default:{

                        }
                    }
                }
            });
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
}

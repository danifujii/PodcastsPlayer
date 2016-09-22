package com.example.daniel.podcastplayer;

import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

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
                            break;
                        }
                        default:{

                        }
                    }
                }
            });
    }
}

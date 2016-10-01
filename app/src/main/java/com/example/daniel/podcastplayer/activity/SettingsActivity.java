package com.example.daniel.podcastplayer.activity;

import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Podcast;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle(getString(R.string.title_act_pref));
    }

    public static class PrefsFragment extends PreferenceFragment{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_layout);

            MultiSelectListPreference automaticList =
                    (MultiSelectListPreference) findPreference(getString(R.string.automatic_download_pref));

            List<Podcast> podcasts = DbHelper.getInstance(getActivity()).getPodcasts();
            CharSequence[] entries = new CharSequence[podcasts.size()];
            CharSequence[] entryValues = new CharSequence[podcasts.size()];
            for(int i = 0; i < podcasts.size(); i++){
                entries[i] = podcasts.get(i).getPodcastName();                      //Human readable entries are the names
                entryValues[i] = String.valueOf(podcasts.get(i).getPodcastId());    //Preferences saved by IDs
            }
            automaticList.setEntries(entries);
            automaticList.setEntryValues(entryValues);
        }
    }
}

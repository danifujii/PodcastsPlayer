package com.example.daniel.podcastplayer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.daniel.podcastplayer.adapter.PodResAdapter;
import com.example.daniel.podcastplayer.data.ResultParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SearchFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private RecyclerView rv;

    public SearchFragment() { /*Required empty*/ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        SearchView sv = (SearchView)v.findViewById(R.id.search_view);
        if (sv != null) {
            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    new AsyncTask<String,Void,String>(){
                        @Override
                        protected String doInBackground(String... params) {
                            return getSearchResult(params[0]);
                        }
                        @Override
                        protected void onPostExecute(String s) {
                            super.onPostExecute(s);
                            rv.setAdapter(new PodResAdapter(ResultParser.getInstance().parseSearch(s,rv)));
                        }
                    }.execute(query.replace(' ','+'));
                    return true;
                }

                @Override public boolean onQueryTextChange(String newText) { return false; }
            });
        }

        rv = (RecyclerView)v.findViewById(R.id.search_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        return v;
    }

    public String getSearchResult(String term){
        NetworkInfo networkInfo = ((ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            BufferedReader reader = null;
            HttpURLConnection huc = null;
            try {
                URL myurl = new URL("https://itunes.apple.com/search?term=" + term + "&media=podcast");
                huc = (HttpURLConnection) myurl.openConnection();
                huc.connect();
                InputStream stream = huc.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine())!=null)
                    buffer.append(line);
                return  buffer.toString();
            } catch(IOException ioe) { ioe.printStackTrace(); }
            finally {
                if (huc != null) huc.disconnect();
                try { if (reader != null) reader.close(); }
                catch (IOException ie){ ie.printStackTrace(); }
            }
        } else
            //TODO Snackbar, say there is no connection
            Log.d("NO INTERNET","Sorry, there is no connection. Try again later");
        return null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

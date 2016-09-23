package com.example.daniel.podcastplayer.data;

import android.util.Log;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Episode {

    private String epTitle = "";
    private String epDate = "";
    private String epURL = "";

    public Episode(){
    }

    public String getEpTitle() {
        return epTitle;
    }

    public String getEpDate() {
        return epDate;
    }

    public String getEpURL() {
        return epURL;
    }

    public void setEpTitle(String epTitle) {
        this.epTitle = epTitle;
    }

    public void setEpDate(String epDate) {
        try {
            epDate = epDate.substring(0, epDate.length()-6);
            Log.d("HERE",epDate);
            SimpleDateFormat ogFormat = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
            Date ogDate = ogFormat.parse(epDate);
            SimpleDateFormat epFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
            this.epDate = epFormat.format(ogDate);
        } catch (ParseException e){e.printStackTrace();}
    }

    public void setEpURL(String epURL) {
        this.epURL = epURL;
    }
}

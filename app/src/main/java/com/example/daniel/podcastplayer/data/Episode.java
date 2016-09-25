package com.example.daniel.podcastplayer.data;

import android.util.Log;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Episode {

    private String epTitle;
    private String epDate;
    private String epURL;
    private long length;

    public Episode(){   }

    public String getEpTitle() {
        return epTitle;
    }

    public String getEpDate() {
        return epDate;
    }

    public String getEpURL() {
        return epURL;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setEpTitle(String epTitle) {
        this.epTitle = epTitle;
    }

    public void setEpURL(String epURL) {
        this.epURL = epURL;
    }

    public void setEpDate(String epDate) {
        try {
            SimpleDateFormat ogFormat = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
            //TODO change this, and just look, from the end, the first space and then delete
            Date ogDate = ogFormat.parse(epDate.substring(0, epDate.length()-6));
            SimpleDateFormat epFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
            this.epDate = epFormat.format(ogDate);
        } catch (ParseException e){e.printStackTrace();}
    }

}

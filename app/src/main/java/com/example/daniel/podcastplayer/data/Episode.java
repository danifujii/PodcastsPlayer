package com.example.daniel.podcastplayer.data;

import android.util.Log;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Episode {

    private String epTitle;
    private String epDate;
    private String epURL;
    private int length;
    private long podcastId;
    private boolean downloaded; //used for UI purposes mainly

    public Episode(long podcast){
        podcastId = podcast;
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

    public int getLength() {
        return length;
    }

    public long getPodcastId() { return podcastId; }

    public boolean getDownloaded() { return downloaded; }

    public void setLength(int length) {
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

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }
}

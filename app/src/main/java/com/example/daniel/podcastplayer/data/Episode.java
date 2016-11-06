package com.example.daniel.podcastplayer.data;

import android.webkit.URLUtil;

public class Episode {

    private String epTitle;
    private String epDate;
    private String epURL;
    private int length;
    private int listened;
    private int podcastId;
    private boolean downloaded; //used for UI purposes mainly
    private boolean newEp;      //not listened episode
    private String description;

    public Episode(int podcast){
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

    public int getPodcastId() { return podcastId; }

    public boolean getDownloaded() { return downloaded; }

    public int getListened() { return listened; }

    public boolean getNewEp() { return newEp; }

    public String getDescription() { return description; }

    public String getFilename() { return String.valueOf(epURL.hashCode()) + URLUtil.guessFileName(epURL, null, null); }

    public void setListened(int listened){ this.listened = listened; }

    public void setLength(int length) {
        this.length = length;
    }

    public void setEpTitle(String epTitle) {
        this.epTitle = epTitle;
    }

    public void setEpURL(String epURL) {
        this.epURL = epURL;
    }

    public void setEpDate(String epDate) { this.epDate = epDate; }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public void setNewEp(boolean newEp) { this.newEp = newEp; }

    public void setDescription(String descr) { this.description = descr; }
}

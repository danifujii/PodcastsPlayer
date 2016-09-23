package com.example.daniel.podcastplayer.data;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ResultParser {

    private static ResultParser instance = new ResultParser();

    private ResultParser(){}

    public static ResultParser getInstance(){ return instance; }

    public List<PodcastRes> parseSearch(String json, RecyclerView rv){
        List<PodcastRes> result = new ArrayList<>();
        try {
            JSONObject parentObject = new JSONObject(json);
            JSONArray resultArray = parentObject.getJSONArray("results");
            for (int i = 0 ; i < resultArray.length(); i++)
                result.add(new PodcastRes(resultArray.getJSONObject(i),rv));
        }
        catch(JSONException je) { je.printStackTrace(); }

        return result;
    }

    public List<Episode> parseFeed(InputStream is){
        List<Episode> result = new ArrayList<>();
        try{
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document d = builder.parse(is);

            NodeList episodes = d.getElementsByTagName("item");
            for (int i = 0; i < episodes.getLength(); i++) {
                Episode e = new Episode();
                Element n = (Element)episodes.item(i);
                e.setEpTitle(n.getElementsByTagName("title").item(0).getTextContent());
                e.setEpDate(n.getElementsByTagName("pubDate").item(0).getTextContent());
                Element url = (Element)n.getElementsByTagName("enclosure").item(0);
                e.setEpURL(url.getAttribute("url"));

                result.add(e);
            }
            is.close();
        } catch (Exception e) { e.printStackTrace(); }

        return result;
    }
}

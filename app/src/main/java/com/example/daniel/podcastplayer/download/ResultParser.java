package com.example.daniel.podcastplayer.download;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.data.Podcast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.R.attr.duration;

public class ResultParser {

    private static ResultParser instance = new ResultParser();
    private String desc;
    private ResultParser(){}

    public static ResultParser getInstance(){ return instance; }

    public List<Podcast> parseSearch(String json, RecyclerView rv){
        List<Podcast> result = new ArrayList<>();
        try {
            JSONObject parentObject = new JSONObject(json);
            JSONArray resultArray = parentObject.getJSONArray("results");
            for (int i = 0 ; i < resultArray.length(); i++)
                result.add(new Podcast(resultArray.getJSONObject(i),rv));
        }
        catch(JSONException je) { je.printStackTrace(); }

        return result;
    }

    public List<Episode> parseFeed(InputStream is, long podcastId){
        return parseFeed(is, Integer.MAX_VALUE, podcastId);
    }

    private List<Episode> parseFeed(InputStream is, int limit, long podcastId){
        List<Episode> result = new ArrayList<>();
        try{
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document d = builder.parse(is);

            //Get podcast description from RSS
            NodeList descList = d.getElementsByTagName("description");
            if (descList.getLength() > 0)
                desc = descList.item(0).getTextContent();

            //Parse episodes from RSS
            NodeList episodes = d.getElementsByTagName("item");
            for (int i = 0; i < episodes.getLength(); i++) {
                Episode e = new Episode(podcastId);
                Element n = (Element)episodes.item(i);
                e.setEpTitle(n.getElementsByTagName("title").item(0).getTextContent());
                e.setEpDate(getDate(n.getElementsByTagName("pubDate").item(0).getTextContent()));
                e.setLength(getSeconds(n.getElementsByTagName("itunes:duration").item(0)
                        .getTextContent()));
                Element url = (Element)n.getElementsByTagName("enclosure").item(0);
                //e.setLength(Integer.valueOf(url.getAttribute("length")));
                e.setEpURL(url.getAttribute("url"));
                if (url.getAttribute("type").matches("audio/(.*)"))
                    result.add(e);
                else Log.d("TAG","NO Matches"); //TODO mostrar error al usuario, indicando que el formato no es soportado


                if (result.size() == limit) break;
            }
        } catch (Exception e) { e.printStackTrace(); }

        return result;
    }

    private int getSeconds(String duration){
        int result = 0;
        for (int i = 2 ; i >= 0 ; i--){
            result = result + getTimeComponent(duration) * (int)Math.pow(60,i);
            duration = duration.substring(duration.indexOf(':')+1);
            Log.d("ResultParser",duration);
        }
        return result;
    }

    //get either hour, minute or second, adding until finding a :
    private int getTimeComponent(String time){
        int index = 0;
        StringBuilder aux = new StringBuilder();
        while (index < time.length() && time.charAt(index)!=':'){
            aux.append(time.charAt(index));
            index++;
        }
        return Integer.parseInt(aux.toString());
    }

    public String getDesc() {
        return desc;
    }

    //Remove the timezone data and hour
    private String getDate(String pubDate){
        Log.d("TAG2", pubDate);
        int index = pubDate.length() - 1;
        int ammount = 2;
        while (ammount > 0){
            index--;
            if (pubDate.charAt(index)==' ')
                ammount = ammount - 1;
        }

        String result = pubDate.substring(5,index);//.replace(' ','-');
        SimpleDateFormat ogFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);
        Date d = null;
        try{
            d = ogFormat.parse(result);
        } catch(ParseException e){ e.printStackTrace(); }
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return dbFormat.format(d);
    }
}

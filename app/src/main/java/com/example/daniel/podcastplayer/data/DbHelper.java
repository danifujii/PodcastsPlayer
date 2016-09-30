package com.example.daniel.podcastplayer.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "PodcastPlayerMov.db";
    private static DbHelper mInstance;

    //CREATION SCRIPTS
    private static final String CREATE_PODCAST = "CREATE TABLE " + Tbls.NAME_PODCAST + " ( "
            + Tbls.COLUMN_ID        + " integer PRIMARY KEY NOT NULL UNIQUE, "
            + Tbls.COLUMN_TITLE     + " text NOT NULL, "
            + Tbls.COLUMN_ARTIST    + " text NOT NULL, "
            + Tbls.COLUMN_FEED      + " text NOT NULL )";
    private static final String CREATE_EPISODES = "CREATE TABLE " + Tbls.NAME_EPISODE + " ( "
            + Tbls.COLUMN_ID        + " integer PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, "
            + Tbls.COLUMN_TITLE     + " text NOT NULL, "
            + Tbls.COLUMN_DATE      + " date NOT NULL, "
            + Tbls.COLUMN_EP_URL    + " text NOT NULL, "
            + Tbls.COLUMN_LENGTH    + " integer NOT NULL, "
            + Tbls.COLUMN_DOWNLOADED+ " boolean NOT NULL, "
            + Tbls.COLUMN_LISTENED  + " integer NOT NULL, "
            + Tbls.COLUMN_FK_POD    + " integer NOT NULL, "
            + " CONSTRAINT FK_Podcast FOREIGN KEY (" + Tbls.COLUMN_FK_POD + ") REFERENCES "
            + Tbls.NAME_PODCAST + "(" + Tbls.COLUMN_ID + ") ON DELETE CASCADE)";

    private DbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    public static DbHelper getInstance(Context context){
        if (mInstance == null)
            mInstance = new DbHelper(context);
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PODCAST);
        db.execSQL(CREATE_EPISODES);
    }

    public void insertPodcast(Podcast p){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Tbls.COLUMN_ID, p.getPodcastId());
        values.put(Tbls.COLUMN_TITLE, p.getPodcastName());
        values.put(Tbls.COLUMN_ARTIST, p.getPodcastArtist());
        values.put(Tbls.COLUMN_FEED, p.getFeedUrl().toString());
        db.insert(Tbls.NAME_PODCAST, null, values);
        db.close();
    }

    public void insertEpisode(Episode e, long podcastId){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Tbls.COLUMN_TITLE,e.getEpTitle());
        values.put(Tbls.COLUMN_DATE,e.getEpDate());
        values.put(Tbls.COLUMN_EP_URL,e.getEpURL());
        values.put(Tbls.COLUMN_LENGTH,e.getLength());
        values.put(Tbls.COLUMN_DOWNLOADED,false);
        values.put(Tbls.COLUMN_LISTENED,"0");
        values.put(Tbls.COLUMN_FK_POD,podcastId);
        db.insert(Tbls.NAME_EPISODE, null, values);
        db.close();
    }

    public boolean existsPodcast(long podcastId){
        return (getWritableDatabase().rawQuery("SELECT 1 FROM "
            + Tbls.NAME_PODCAST + " WHERE " + Tbls.COLUMN_ID + "='" + String.valueOf(podcastId) + "'"
                ,null).getCount())>0;
    }

    public Podcast getPodcast(long podcastId){
        Cursor c =getWritableDatabase().rawQuery("SELECT * FROM "
                + Tbls.NAME_PODCAST + " WHERE " + Tbls.COLUMN_ID + "='"
                + String.valueOf(podcastId) + "'"
                , null);
        if (c.moveToFirst()) {
            Podcast p = buildPodcast(c);
            c.close();
            return p;
        }
        return null;
    }

    public Cursor getPodcastsCursor(){
        return getWritableDatabase().rawQuery("SELECT * FROM "
            + Tbls.NAME_PODCAST + " ORDER BY " + Tbls.COLUMN_TITLE, null);
    }

    public List<Podcast> getPodcasts(){
        List<Podcast> result = new ArrayList<>();

        Cursor c = getPodcastsCursor();
        while (c.moveToNext()){
            result.add(buildPodcast(c));
        }
        c.close();
        return result;
    }

    public Podcast buildPodcast(Cursor c){
        Podcast p = null;
        if (c.getCount() > 0){
            p = new Podcast();
            p.setPodcastId(c.getLong(c.getColumnIndex(DbHelper.Tbls.COLUMN_ID)));
            p.setPodcastName(c.getString(c.getColumnIndex(DbHelper.Tbls.COLUMN_TITLE)));
            p.setPodcastArtist(c.getString(c.getColumnIndex(DbHelper.Tbls.COLUMN_ARTIST)));
            try { p.setFeedUrl(new URL(c.getString(c.getColumnIndex(DbHelper.Tbls.COLUMN_FEED)))); }
            catch (MalformedURLException me) { me.printStackTrace(); }
        }
        return p;
    }

    public List<Episode> getEpisodes(long podcastId){
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM "
                + Tbls.NAME_EPISODE + " WHERE " + Tbls.COLUMN_FK_POD + "='"
                + String.valueOf(podcastId) + "' ORDER BY " + Tbls.COLUMN_DATE + " DESC",
                null);
        return buildEpisodes(c);
    }

    public Episode getEpisode(String epUrl){
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + Tbls.NAME_EPISODE
                + " WHERE " + Tbls.COLUMN_EP_URL + "='" + epUrl + "'", null);
        List<Episode> result = buildEpisodes(c);
        if (result.size() > 0)
            return result.get(0);
        return null;
    }

    public int getEpisodeListened(String epUrl){
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + Tbls.NAME_EPISODE
            + " WHERE " + Tbls.COLUMN_EP_URL + "='" + epUrl + "'", null);
        if (c.moveToFirst()){
            return c.getInt(c.getColumnIndex(Tbls.COLUMN_LISTENED));
        }
        return -1;
    }

    public Episode getLastEpisode(int podcastId){
        return getEpisodes(podcastId).get(0);
    }

    //Get latest, non listened episodes from each podcast
    public List<Episode> getLatestEpisodes(){
        Cursor c = getReadableDatabase().rawQuery("SELECT e1.* FROM " + Tbls.NAME_EPISODE + " e1"
                + " JOIN (SELECT " + Tbls.COLUMN_ID + ", MAX( " + Tbls.COLUMN_DATE + ") date "
                + " FROM " +Tbls.NAME_EPISODE + " WHERE " + Tbls.COLUMN_LISTENED + "<" + Tbls.COLUMN_LENGTH
                + " GROUP BY " + Tbls.COLUMN_FK_POD +" ) e2"
                + " ON e1." + Tbls.COLUMN_ID+"=e2."+Tbls.COLUMN_ID
                + " AND e1."+ Tbls.COLUMN_DATE+"=e2.date", null);
        return buildEpisodes(c);
    }

    private List<Episode> buildEpisodes(Cursor c)
    {
        List<Episode> episodes = new ArrayList<>();
        while (c.moveToNext()){
            Episode e = new Episode(c.getLong(c.getColumnIndex(DbHelper.Tbls.COLUMN_FK_POD)));
            e.setEpTitle(c.getString(c.getColumnIndex(DbHelper.Tbls.COLUMN_TITLE)));
            e.setEpDate(c.getString(c.getColumnIndex(DbHelper.Tbls.COLUMN_DATE)));
            e.setLength(c.getInt(c.getColumnIndex(DbHelper.Tbls.COLUMN_LENGTH)));
            e.setEpURL(c.getString(c.getColumnIndex(DbHelper.Tbls.COLUMN_EP_URL)));
            e.setListened(c.getInt(c.getColumnIndex(Tbls.COLUMN_LISTENED)));
            episodes.add(e);
        }
        c.close();
        return episodes;
    }

    public void deletePodcast(long podcastId){
        delete(Tbls.NAME_PODCAST, Tbls.COLUMN_ID, String.valueOf(podcastId));
        delete(Tbls.NAME_EPISODE, Tbls.COLUMN_FK_POD, String.valueOf(podcastId));
    }

    private void delete(String tableName, String columnName, String value){
        String[] selectionArgs = { value };
        SQLiteDatabase db = getWritableDatabase();
        db.delete(tableName,
                columnName + "=?",
                selectionArgs);
        db.close();
    }

    public void updateEpisode(String epURL, int listened){
        ContentValues cv = new ContentValues();
        cv.put(Tbls.COLUMN_LISTENED,String.valueOf(listened));
        getWritableDatabase().update(Tbls.NAME_EPISODE, cv,
                Tbls.COLUMN_EP_URL + "='" + epURL + "'", null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tbls.NAME_PODCAST);
        db.execSQL("DROP TABLE IF EXISTS " + Tbls.NAME_EPISODE);
        onCreate(db);
    }

    public static class Tbls{
        public static String NAME_PODCAST = "PODCAST";
        public static String NAME_EPISODE = "EPISODE";

        public static String COLUMN_ID = "_id";
        public static String COLUMN_TITLE = "title";
        public static String COLUMN_ARTIST = "artist";
        public static String COLUMN_ARTWORK = "artwork";
        public static String COLUMN_FEED = "feedUrl";

        public static String COLUMN_DATE = "date";
        public static String COLUMN_EP_URL = "epUrl";
        public static String COLUMN_LENGTH = "length";
        public static String COLUMN_DOWNLOADED = "downloaded";
        public static String COLUMN_LISTENED = "listened";
        public static String COLUMN_FK_POD = "podcastId";
    }
}

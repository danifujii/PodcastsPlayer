package com.example.daniel.podcastplayer.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Daniel on 23/9/2016.
 */

public class DbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "PodcastPlayerMov.db";
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

    public boolean existsPodcast(long podcastId){
        return (getWritableDatabase().rawQuery("SELECT 1 FROM "
            + Tbls.NAME_PODCAST + " WHERE " + Tbls.COLUMN_ID + "='" + String.valueOf(podcastId) + "'"
                ,null).getCount())>0;
    }

    public Cursor getPodcasts(){
        return getWritableDatabase().rawQuery("SELECT * FROM "
            + Tbls.NAME_PODCAST, null);
    }

    public void deletePodcast(long podcastId){
        delete(Tbls.NAME_PODCAST, Tbls.COLUMN_ID, String.valueOf(podcastId));
    }

    public void delete(String tableName, String columnName, String value){
        String[] selectionArgs = { value };
        SQLiteDatabase db = getWritableDatabase();
        db.delete(tableName,
                columnName + "=?",
                selectionArgs);
        db.close();
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

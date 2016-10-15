package com.example.daniel.podcastplayer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.data.DbHelper;

import java.io.File;

public class ImageAdapter extends CursorAdapter{

    public ImageAdapter(Context context, Cursor cursor){
        super(context,cursor,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView iv = new ImageView(context);
        iv.setAdjustViewBounds(true);
        if (Build.VERSION.SDK_INT >= 21){
            iv.setTransitionName(context.getString(R.string.transition_artwork));
        }
        return iv;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(DbHelper.Tbls.COLUMN_ID));
        File image = new File(context.getApplicationInfo().dataDir + "/Artwork", id + ".png");
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
        if (bitmap != null)
            ((ImageView)view).setImageBitmap(bitmap);
    }
}

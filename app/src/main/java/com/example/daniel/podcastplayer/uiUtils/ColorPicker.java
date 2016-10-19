package com.example.daniel.podcastplayer.uiUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;

/**
 * Created by Daniel on 27/9/2016.
 */

public class ColorPicker {

    public static int getArtworkColor(Bitmap artwork)
    {
        Palette p = Palette.from(artwork).generate();
        int def = 0x000000;
        int artColor = p.getVibrantColor(def);
        if (def != artColor)
            return artColor;
        else{
            artColor = p.getDarkVibrantColor(def);
            if (def != artColor)
                return artColor;
            else {
                artColor = p.getDarkMutedColor(def);
                return (artColor != def) ? artColor : p.getMutedColor(def);
            }
        }
    }

    public static int getDarkerColor(int color){
        float[] hsv = new float[3];
        Color.colorToHSV(color,hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public static Drawable getColoredDrawable(Context context, int color, Bitmap bitmap){
        Drawable original = new BitmapDrawable(context.getResources(),bitmap);
        original.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return original;
    }
}

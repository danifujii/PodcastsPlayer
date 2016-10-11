package com.example.daniel.podcastplayer.player;

import android.app.Dialog;
import android.preference.PreferenceManager;
import android.renderscript.Double2;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.data.Episode;

/**
 * Created by Daniel on 3/10/2016.
 */

public class SpeedDialogManager {

    private static final Double delta = 0.1;
    private static final String limit = "3.0x";
    private static final String bottom = "0.1x";

    public static void setSpeedDialog(View view, int podcastId){
        final TextView speedTV = (TextView)view.findViewById(R.id.speed_tv);
        float speed = PreferenceManager.getDefaultSharedPreferences(view.getContext())
                .getFloat(String.valueOf(podcastId) + view.getContext().getString(R.string.speed_setting), -1);
        if (speed > 0){
            String speedText = String.valueOf(speed) + "x";
            speedTV.setText(speedText);
        } else {
            speed = PreferenceManager.getDefaultSharedPreferences(view.getContext())
                    .getFloat(view.getContext().getString(R.string.default_speed_setting), -1);
            if (speed > 0){
                String speedText = String.valueOf(speed) + "x";
                speedTV.setText(speedText);
            }
        }
        ImageButton plusButton = (ImageButton)view.findViewById(R.id.plus_button);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedTV.setText(getChanged(speedTV.getText().toString(), +delta));
            }
        });
        ImageButton minusButton = (ImageButton)view.findViewById(R.id.minus_button);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedTV.setText(getChanged(speedTV.getText().toString(), -delta));
            }
        });
    }

    private static String getChanged(String ogSpeed, Double inputDelta){
        if (ogSpeed.equals(limit) && inputDelta > 0) return ogSpeed;
        if (ogSpeed.equals(bottom) && inputDelta < 0) return ogSpeed;
        else{
            String sansX = ogSpeed.substring(0, ogSpeed.length()-1);
            Integer decimal = Integer.valueOf(sansX.substring(sansX.indexOf('.')+1,sansX.indexOf('.')+2));    //first digit of decimal part
            Integer integer = Integer.valueOf(sansX.substring(0,1));
            if (inputDelta > 0){
                if (decimal==9)     //se pasaria en uno
                    return String.valueOf(integer+1) + ".0x";
                else return integer.toString() + "." + String.valueOf(decimal+1) + "x";
            } else{
                if (decimal==0)     //"underflow"
                    return String.valueOf(integer-1) + ".9x";
                else return integer.toString() + "." + String.valueOf(decimal-1) + "x";
            }
        }
    }

}

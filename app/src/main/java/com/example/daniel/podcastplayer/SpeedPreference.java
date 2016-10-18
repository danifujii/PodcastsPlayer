package com.example.daniel.podcastplayer;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.example.daniel.podcastplayer.player.SpeedDialogManager;

/**
 * Created by Daniel on 3/10/2016.
 */

public class SpeedPreference extends DialogPreference {

    private TextView speedTV;

    public SpeedPreference(Context context, AttributeSet atrs){
        super(context,atrs);

        setDialogLayoutResource(R.layout.speed_dialog_layout);
        setPositiveButtonText(R.string.ok_button);
        setNegativeButtonText(R.string.cancel_button);

        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        SpeedDialogManager.setSpeedDialog(view,-1);
        speedTV = (TextView)view.findViewById(R.id.speed_tv);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        //super.onDialogClosed(positiveResult);
        if (positiveResult) {
            String speed = speedTV.getText().toString();
            speed = speed.substring(0,speed.length()-1);    //remove the x at the end
            persistFloat(Float.valueOf(speed));
        }
    }
}

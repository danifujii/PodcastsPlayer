package com.example.daniel.podcastplayer.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.data.FileManager;
import com.example.daniel.podcastplayer.fragment.QueueDialog;
import com.example.daniel.podcastplayer.player.PodcastPlayerService;
import com.example.daniel.podcastplayer.player.SpeedDialogManager;
import com.example.daniel.podcastplayer.uiUtils.ColorPicker;

public class PlayerActivity extends ServiceActivity implements View.OnTouchListener{

    private SeekBar progressBar;
    private TextView progressTV;

    //Progress text
    private final static String divider = " / ";
    private String length;
    private static boolean active = false;  //active is used to avoid checking the player playing
                                            // if the activity is not active
    //Swipe Down
    private ViewGroup layout;

    private int previousFingerPosition = 0;
    private int defaultViewHeight;
    private boolean isClosing = false;
    private boolean isScrollingDown = false;

    private QueueDialog queueDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        manager = null; //No player sheet in here, so no sense to update such UI

        layout = (ViewGroup) findViewById(R.id.activity_player);
        layout.setOnTouchListener(this);
    }

    public void setupPlayerUI(){
        progressTV = (TextView) findViewById(R.id.player_progress_tv);
        progressBar = (SeekBar)findViewById(R.id.player_progress_bar);
        final ImageButton play = (ImageButton)findViewById(R.id.player_play_button);

        if (bound){

            if (queueDialog != null)
                queueDialog.dismiss();

            final Episode e = service.getEpisode();
            if (e == null){
                finish();
                return;
            }

            ImageView artwork = (ImageView)findViewById(R.id.player_artwork_iv);
            Bitmap bitmap = FileManager.getBitmap(this, e.getPodcastId());
            artwork.setImageBitmap(bitmap);
            int color = ColorPicker.getArtworkColor(bitmap);
            int darkerColor = ColorPicker.getDarkerColor(color);

            TextView epTV = (TextView) findViewById(R.id.player_ep_tv);
            if (epTV != null)
                epTV.setText(e.getEpTitle());
            ((TextView)findViewById(R.id.player_pod_tv))
                    .setText(DbHelper.getInstance(this).getPodcast(e.getPodcastId()).getPodcastName());

            if (play != null) {
                if (service.isPlaying())
                    play.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_pause_black_48dp));
                else
                    play.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_play_arrow_black_48dp));
                    //play.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_play_arrow_black_48dp));

                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (service.isPlaying())
                            service.pausePlayback();
                        else
                            service.resumePlayback();
                        changeButtonIcon(play);
                    }
                });
            }

            progressBar.setMax(service.getEpisode().getLength());
            progressBar.setProgress(service.getProgress());
            progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        service.setProgress(progress);
                        progressTV.setText(getTime(service.getProgress()) + divider + length);
                    }
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            active = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (progressBar.getProgress() < progressBar.getMax()){
                        if (active && service.isPlaying()) {
                            progressBar.setProgress(service.getProgress() + 1);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressTV.setText(getTime(service.getProgress()) + divider + length);
                                    if (service.getProgress() == service.getEpisode().getLength())
                                        changeButtonIcon(play);
                                }
                            });
                        }
                        try { Thread.sleep(1000); }
                        catch (InterruptedException e) { e.printStackTrace(); }
                    }
                }
            }).start();

            ImageButton rewindButton = (ImageButton) findViewById(R.id.player_rewind_button);
            if (rewindButton !=  null)
                rewindButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        service.rewindPlayback();
                        progressBar.setProgress(service.getProgress());
                        progressTV.setText(getTime(service.getProgress()) + divider + length);
                    }
                });

            ImageButton forwardButton = (ImageButton) findViewById(R.id.player_forward_button);
            if (forwardButton != null)
                forwardButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        service.forwardPlayback();
                        progressBar.setProgress(service.getProgress());
                        progressTV.setText(getTime(service.getProgress()) + divider + length);
                    }
                });

            length = getTime(service.getEpisode().getLength());
            if (progressTV != null)
                progressTV.setText(getTime(service.getProgress()) + divider + length);

            findViewById(R.id.player_ep_tv).setBackgroundColor(ColorPicker.getDarkerColor(color));
            findViewById(R.id.player_pod_tv).setBackgroundColor(ColorPicker.getDarkerColor(color));
            if (Build.VERSION.SDK_INT >= 21)
                getWindow().setStatusBarColor(darkerColor);

            ImageButton speedButton = (ImageButton) findViewById(R.id.speed_button);
            if (Build.VERSION.SDK_INT>=23)
                speedButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
                        View dialogView = getLayoutInflater().inflate(R.layout.speed_dialog_layout, null);
                        builder.setView(dialogView);
                        SpeedDialogManager.setSpeedDialog(dialogView, e.getPodcastId());
                        builder.setTitle(getString(R.string.default_speed_setting));
                        builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor =
                                        PreferenceManager.getDefaultSharedPreferences(PlayerActivity.this).edit();
                                String speed = ((TextView)((AlertDialog)dialog).findViewById(R.id.speed_tv)).getText().toString();
                                speed = speed.substring(0,speed.length()-1);    //remove the x at the end
                                editor.putFloat(String.valueOf(e.getPodcastId())+getString(R.string.speed_setting),
                                        Float.valueOf(speed));
                                editor.apply();
                                service.setPlaybackParams();
                            }
                        });
                        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {} });
                        builder.create().show();
                    }
                });
            else speedButton.setVisibility(View.GONE);

            findViewById(R.id.queue_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    queueDialog = new QueueDialog();
                    queueDialog.show(getFragmentManager(), queueDialog.getTag());
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        active = true;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PodcastPlayerService.ACTION_FINISH);
        intentFilter.addAction(PodcastPlayerService.ACTION_PLAY);
        intentFilter.addAction(PodcastPlayerService.ACTION_PAUSE);
        intentFilter.addAction(PodcastPlayerService.ACTION_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(handler, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(handler);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.slide_down);
    }

    @Override
    protected void onStart() {
        super.onStart();

        setupPlayerUI();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Get finger position on screen
        final int Y = (int) event.getRawY();

        // Switch on motion event type
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                // save default base layout height
                defaultViewHeight = layout.getHeight();

                // Init finger and view position
                previousFingerPosition = Y;
                //baseLayoutPosition = (int) layout.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                if(!isClosing){
                    int currentYPosition = (int) layout.getY();
                    if (previousFingerPosition < Y){

                        // First time android rise an event for "down" move
                        if(!isScrollingDown){
                            isScrollingDown = true;
                        }

                        // Change base layout size and position (must change position because view anchor is top left corner)
                        layout.setY(layout.getY() + (Y - previousFingerPosition));
                        layout.getLayoutParams().height = layout.getHeight() - (Y - previousFingerPosition);
                        layout.requestLayout();
                    }

                    // Update position
                    previousFingerPosition = Y;
                }
                break;
            case MotionEvent.ACTION_UP: {

                int currentYPosition = (int) layout.getY();

                if (currentYPosition < defaultViewHeight / 5) {
                    layout.setY(0);
                    layout.getLayoutParams().height = defaultViewHeight;
                    layout.requestLayout();
                }
                else{
                    closeActivity(currentYPosition);
                }
                break;
            }
        }
        return true;
    }

    private void closeActivity(int currentYPosition){
        isClosing = true;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenHeight = size.y;
        ObjectAnimator positionAnimator = ObjectAnimator.ofFloat(layout, "y", currentYPosition
                , screenHeight+layout.getHeight());
        positionAnimator.setDuration(600);
        positionAnimator.addListener(new Animator.AnimatorListener()
        {
            @Override public void onAnimationStart(Animator animation)  {}
            @Override public void onAnimationRepeat(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animator) {
                finish();
            }
        });
        positionAnimator.start();
    }

    private String getTime(int miliSeconds){
        int minutes = (miliSeconds / 1000) / 60;
        int seconds = (miliSeconds / 1000) % 60;
        StringBuilder builder = new StringBuilder();
        if (minutes >= 60){
            int hours = minutes / 60;
            minutes = minutes % 60;
            builder.append(hours);
            builder.append(':');
        }
        if (minutes < 10) builder.append('0');
        builder.append(minutes);
        builder.append(":");
        if (seconds < 10) builder.append('0');
        builder.append(seconds);
        return builder.toString();
    }

    private void changeButtonIcon(ImageButton playButton){
        if (bound && service.isPlaying())
            playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_pause_black_48dp));
        else
            playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_play_arrow_black_48dp));
    }

    private BroadcastReceiver handler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ImageButton playButton =((ImageButton) findViewById(R.id.player_play_button));
            switch (intent.getAction()){
                case(PodcastPlayerService.ACTION_FINISH):{
                    playButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_play_arrow_black_48dp));
                    progressTV.setText(length + divider + length);
                    finish();
                    break;
                }
                case(PodcastPlayerService.ACTION_PLAY):{
                    changeButtonIcon(playButton);
                    break;
                }
                case(PodcastPlayerService.ACTION_PAUSE):{
                    changeButtonIcon(playButton);
                    break;
                }
                case(PodcastPlayerService.ACTION_CHANGED):{
                    setupPlayerUI();
                    break;
                }
            }
        }
    };
}

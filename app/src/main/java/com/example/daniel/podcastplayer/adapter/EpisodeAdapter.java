package com.example.daniel.podcastplayer.adapter;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.daniel.podcastplayer.player.PlayerSheetManager;
import com.example.daniel.podcastplayer.player.PodcastPlayerService;
import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.data.Episode;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>{

    private List<Episode> data;
    private Activity activity;  //save the activity that uses this to set the player layout as visible

    public EpisodeAdapter(List<Episode> data){
        this.data = data;
    }

    @Override
    public EpisodeAdapter.EpisodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.episode_layout, parent, false);
        activity = (Activity)parent.getContext();

        return new EpisodeAdapter.EpisodeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final EpisodeAdapter.EpisodeViewHolder holder, int position) {
        Episode item = data.get(position);
        holder.nameTV.setText(item.getEpTitle());
        holder.dateTV.setText(getDateFormat(item.getEpDate()));

        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Episode ep = data.get(holder.getAdapterPosition());
                if (!ep.getDownloaded()){
                    DownloadManager mgr = (DownloadManager) v.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri uri = Uri.parse(ep.getEpURL());
                    mgr.enqueue(new DownloadManager.Request(uri)
                            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                            .setTitle("Downloading episode")
                            .setDescription(ep.getEpTitle())
                            .setDestinationInExternalFilesDir(v.getContext()
                                    , v.getContext().getFilesDir().getAbsolutePath()
                                    , URLUtil.guessFileName(ep.getEpURL(), null, null)));
                }
                else{
                    PodcastPlayerService player = PodcastPlayerService.getInstance();
                    //if (player.getEpisode() != ep || !player.isPlaying())    //avoid restarting an episode already playing
                        player.startPlayback(ep,activity);

                    PlayerSheetManager psm = new PlayerSheetManager();
                    psm.setSheetInterface(ep, activity);
                }
            }
        });

        Context c = holder.dateTV.getContext();
        if (existsFile(URLUtil.guessFileName(item.getEpURL(),null,null), c)) {
            holder.downloadButton.setImageBitmap(BitmapFactory.decodeResource(c.getResources(),
                    R.drawable.ic_play_circle_outline_black_24dp));
            item.setDownloaded(true);
        }
        else holder.downloadButton.setImageBitmap(BitmapFactory.decodeResource(c.getResources(),
                R.drawable.ic_file_download_black_24dp));
    }

    private void setPlayerSheet(final Episode e){
        //TODO change this, now there is a PlayerSheetManager to handle this UI
        activity.findViewById(R.id.splayer_layout).setVisibility(View.VISIBLE);
        ((TextView)activity.findViewById(R.id.splayer_ep_tv)).setText(e.getEpTitle());
        final ImageButton playButton = (ImageButton)activity.findViewById(R.id.splayer_play_button);
        //TODO change layout in PodcastActivity so that there is no RecyclerView at the end, because the button behind the Play/pause in the sheet
        // is capturing the play event too. Starting download or playback.
        if (playButton != null) {
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PodcastPlayerService player = PodcastPlayerService.getInstance();
                    if (player.isPlaying()) {
                        player.pausePlayback();
                        playButton.setImageBitmap(BitmapFactory.decodeResource(v.getResources(),
                                R.drawable.ic_play_arrow_black_24dp));
                    }else {
                        player.startPlayback(e , activity);
                        playButton.setImageBitmap(BitmapFactory.decodeResource(v.getResources(),
                                R.drawable.ic_pause_black_24dp));
                    }
                }
            });
            //TODO revisar esto ya que genera que el boton fuera del PlayerSheet pueda pausar la reproducción actual.
            playButton.performClick();
        }
    }

    private boolean existsFile(String fileName, Context context){
        //TODO see if you can change this hardcoded route
        File f = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Android/data/com.example.daniel.podcastplayer/files/"
                    + context.getFilesDir().getAbsolutePath() + "/" + fileName);
        Log.d("Episode adapter", Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Android/data/com.example.daniel.podcastplayer/files/"
                + context.getFilesDir().getAbsolutePath() + "/" + fileName);
        return f.isFile();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class EpisodeViewHolder extends RecyclerView.ViewHolder{
        protected TextView nameTV;
        protected TextView dateTV;
        protected ImageButton downloadButton;

        public EpisodeViewHolder(View itemView) {
            super(itemView);
            dateTV = (TextView)itemView.findViewById(R.id.ep_date_tv);
            nameTV = (TextView)itemView.findViewById(R.id.ep_title_tv);
            downloadButton = (ImageButton)itemView.findViewById(R.id.ep_download_button);
        }
    }

    public static String getDateFormat(String ogDate){
        SimpleDateFormat format = new java.text.SimpleDateFormat("E, MM dd yyyy", Locale.US);
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try{
            Date d = dbFormat.parse(ogDate);
            return format.format(d);
        } catch (ParseException e) { e.printStackTrace(); }
        return null;
    }

}

package com.example.daniel.podcastplayer.adapter;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.daniel.podcastplayer.activity.PlayerActivity;
import com.example.daniel.podcastplayer.activity.ServiceActivity;
import com.example.daniel.podcastplayer.data.FileManager;
import com.example.daniel.podcastplayer.download.Downloader;
import com.example.daniel.podcastplayer.player.PlayerSheetManager;
import com.example.daniel.podcastplayer.player.PodcastPlayerService;
import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.data.Episode;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>{

    private List<Episode> data;
    private ServiceActivity activity;  //save the activity that uses this to set the player layout as visible
    private boolean difPodcasts;

    public EpisodeAdapter(List<Episode> data, boolean differentPodcasts){
        this.difPodcasts = differentPodcasts;
        this.data = data;
    }

    public EpisodeAdapter(List<Episode> data){
        this.data = data;
    }

    @Override
    public EpisodeAdapter.EpisodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.episode_layout, parent, false);
        activity = (ServiceActivity)parent.getContext();

        return new EpisodeAdapter.EpisodeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final EpisodeAdapter.EpisodeViewHolder holder, int position) {
        final Context c = holder.dateTV.getContext();
        final Episode item = data.get(position);
        holder.nameTV.setText(item.getEpTitle());
        holder.dateTV.setText(getDateFormat(item.getEpDate()));
        holder.remainingTV.setVisibility(View.GONE);

        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Episode ep = data.get(holder.getAdapterPosition());
                if (!Downloader.isDownloading(ep.getEpURL())) {         //no esta descargando actualmente
                    if (FileManager.getEpisodeFile(holder.downloadButton.getContext(),
                            ep).exists()){                              //si encuentra el archivo, puede reproducir
                        PodcastPlayerService player = activity.getService();
                        if (player != null) {
                            player.startPlayback(ep, activity);
                            activity.setupPlayerUI();
                        }
                    }else {                                             //si no esta, comenzar descarga
                        if (Downloader.isConnected(v.getContext(), false)) {
                            Downloader.explicitDownloadEpisode(v.getContext(), ep
                                    , EpisodeAdapter.this, holder.downloadButton);
                            changeImageButton(holder.downloadButton, Icons.CANCEL.ordinal());
                        } else Snackbar.make(v, activity.getString(R.string.error_no_connection),
                                Snackbar.LENGTH_LONG).show();
                    }
                } else{
                    Downloader.cancelDownload(holder.downloadButton.getContext(), ep.getEpURL());
                    changeImageButton(holder.downloadButton, Icons.DOWNLOAD.ordinal());
                }
            }
        });

        if (FileManager.getEpisodeFile(c,item).exists()) {
            holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(c);
                    builder.setMessage(c.getString(R.string.delete_message))
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FileManager.deleteFile(c, item);
                                changeImageButton(holder.downloadButton, Icons.DOWNLOAD.ordinal());
                                holder.remainingTV.setVisibility(View.INVISIBLE);
                            }
                        })
                        .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {}
                        })
                            .create().show();
                    return true;
                }
            });
        }

        if (!Downloader.isDownloading(item.getEpURL())){
            if (FileManager.getEpisodeFile(c,item).exists()) {
                changeImageButton(holder.downloadButton, Icons.PLAY.ordinal());
                holder.remainingTV.setVisibility(View.VISIBLE);
                holder.remainingTV.setText(getRemaining(item.getLength()-item.getListened(),c));
            }
            else changeImageButton(holder.downloadButton, Icons.DOWNLOAD.ordinal());
        }
        else
            changeImageButton(holder.downloadButton, Icons.CANCEL.ordinal());

        if (difPodcasts) {
            holder.artworkIV.setVisibility(View.VISIBLE);
            holder.artworkIV.setImageBitmap(FileManager.getBitmap(c, item.getPodcastId()));
        }
    }

    public void removeItem(int position){
        data.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, data.size());
    }

    public void addItem(Episode e, int pos){
        data.add(pos, e);
        notifyItemInserted(pos);
    }

    public Episode getItem(int position){
        return data.get(position);
    }

    public enum Icons {DOWNLOAD, CANCEL, PLAY}
    public void changeImageButton(ImageButton im, int type){
        Icons icon = Icons.values()[type];
        switch (icon){
            case DOWNLOAD:{
                im.setImageBitmap(BitmapFactory.decodeResource(
                        im.getResources(), R.drawable.ic_file_download_black_24dp));
                break;
            }
            case CANCEL:{
                im.setImageBitmap(BitmapFactory.decodeResource(
                        im.getResources(), R.drawable.ic_close_black_24dp));
                break;
            }
            case PLAY:{
                im.setImageBitmap(BitmapFactory.decodeResource(
                        im.getResources(), R.drawable.ic_play_circle_outline_black_24dp));
                break;
            }
        }
    }



    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class EpisodeViewHolder extends RecyclerView.ViewHolder{
        protected TextView nameTV;
        protected TextView dateTV;
        protected TextView remainingTV;
        protected ImageButton downloadButton;
        protected ImageView artworkIV;
        protected ViewGroup layout;

        public EpisodeViewHolder(View itemView) {
            super(itemView);
            dateTV = (TextView)itemView.findViewById(R.id.ep_date_tv);
            nameTV = (TextView)itemView.findViewById(R.id.ep_title_tv);
            remainingTV = (TextView)itemView.findViewById(R.id.remaining_tv);
            downloadButton = (ImageButton)itemView.findViewById(R.id.ep_download_button);
            artworkIV = (ImageView)itemView.findViewById(R.id.pod_artwork_iv);
            layout = (ViewGroup)itemView.findViewById(R.id.episode_adap_layout);
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

    private String getRemaining(int remaining, Context context){
        int seconds = remaining / 1000;
        String minsText = context.getString(R.string.mins);
        int mins = seconds /60;
        if (seconds <= 60 || mins == 1) return "1 " + minsText.substring(0,minsText.length()-1);
        if (mins < 60)
            return String.valueOf(mins) + " " + minsText;
        else{
            String hoursText = context.getString(R.string.hours);
            int hours = mins / 60;
            if (hours == 1) return "1 " + hoursText.substring(0,hoursText.length()-1);
            else return String.valueOf(mins/60) + " " + hoursText;
        }
    }
}

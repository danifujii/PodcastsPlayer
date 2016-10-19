package com.example.daniel.podcastplayer.fragment;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.activity.ServiceActivity;
import com.example.daniel.podcastplayer.adapter.EpisodeAdapter;
import com.example.daniel.podcastplayer.data.DbHelper;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.data.FileManager;
import com.example.daniel.podcastplayer.download.Downloader;
import com.example.daniel.podcastplayer.download.JuhaTagHandler;
import com.example.daniel.podcastplayer.player.PlayerQueue;
import com.example.daniel.podcastplayer.uiUtils.ColorPicker;
import com.example.daniel.podcastplayer.uiUtils.EpisodeButtonController;

import java.io.File;


public class EpisodeBottomSheet extends BottomSheetDialogFragment{

    private Episode episode;
    private ServiceActivity act;
    private EpisodeButtonController controller;

    public void setEpisode(Episode e, ServiceActivity activity){
        episode = e;
        act = activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.episode_desc_layout, container, false);

        if (episode != null) {
            int color = ColorPicker.getDarkerColor(ColorPicker.getArtworkColor(FileManager.getBitmap(getContext(), episode.getPodcastId())));

            TextView descrTV = (TextView) v.findViewById(R.id.description_tv);
            descrTV.setText(Html.fromHtml(episode.getDescription(),null,new JuhaTagHandler()));
            descrTV.setMovementMethod(LinkMovementMethod.getInstance());
            descrTV.setLinkTextColor(color);
            ((TextView)v.findViewById(R.id.episode_show_notes_tv)).setTextColor(color);

            ((TextView) v.findViewById(R.id.episode_title_tv)).setText(episode.getEpTitle());
            ((TextView) v.findViewById(R.id.episode_show_tv)).setText(
                    DbHelper.getInstance(getContext()).getPodcast(episode.getPodcastId()).getPodcastName());

            v.findViewById(R.id.episode_header_layout)
                    .setBackgroundColor(color);

            ImageButton playButton = (ImageButton)v.findViewById(R.id.episode_play_button);
            controller = new EpisodeButtonController(playButton, act, episode, Color.WHITE);
            updateButtons(v);

            ImageButton deleteButton = (ImageButton)v.findViewById(R.id.episode_delete_button);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(getContext().getString(R.string.delete_message))
                            .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FileManager.deleteFile(getContext(), episode);
                                    getDialog().cancel();
                                }
                            })
                            .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) {}
                            })
                            .create().show();
                }
            });

            ImageButton queueButton = (ImageButton)v.findViewById(R.id.episode_queue_buttton);
            queueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlayerQueue.getInstance().addEpisode(episode);
                    Toast.makeText(getContext(), getString(R.string.added), Toast.LENGTH_SHORT).show();
                }
            });
        }

        return v;
    }

    private void updateButtons(View v){
        File epFile = FileManager.getEpisodeFile(getContext(), episode);
        if (!epFile.exists() || Downloader.isDownloading(episode.getEpURL())) {
            v.findViewById(R.id.episode_delete_button).setVisibility(View.GONE);
            v.findViewById(R.id.episode_queue_buttton).setVisibility(View.GONE);
        }
        else{
            TextView remainingTV = (TextView)v.findViewById(R.id.episode_remain_tv);
            remainingTV.setVisibility(View.VISIBLE);
            remainingTV.setText(EpisodeAdapter.getRemaining(episode.getLength()-episode.getListened(),getContext()));
            v.findViewById(R.id.episode_delete_button).setVisibility(View.VISIBLE);
            v.findViewById(R.id.episode_queue_buttton).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(downloadReceiver);
    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
                updateButtons(getView());
                controller.changeImageButton(EpisodeButtonController.Icons.PLAY.ordinal());
            }
        }
    };
}

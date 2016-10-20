package com.example.daniel.podcastplayer.fragment;

import android.animation.Animator;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.example.daniel.podcastplayer.R;
import com.example.daniel.podcastplayer.activity.PlayerActivity;
import com.example.daniel.podcastplayer.activity.ServiceActivity;
import com.example.daniel.podcastplayer.adapter.EpisodeAdapter;
import com.example.daniel.podcastplayer.adapter.QueueItemAdapter;
import com.example.daniel.podcastplayer.data.Episode;
import com.example.daniel.podcastplayer.player.PlayerQueue;

public class QueueDialog extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.queue_dialog_layout,null);
        builder.setView(view)
                .setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog dialog = builder.create();
        return dialog;
    }


    @Override
    public void onResume() {
        super.onResume();

        final RecyclerView rv = (RecyclerView)getDialog().findViewById(R.id.queue_rv);
        if (rv != null){
            rv.setLayoutManager(new LinearLayoutManager(getActivity()));
            rv.setAdapter(new QueueItemAdapter((ServiceActivity)getActivity()));     //este casting deja de funcionar si en algun momento se usa este Dialog fuera de un Service

            ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN
                    , ItemTouchHelper.RIGHT) {
                @Override
                public boolean isLongPressDragEnabled() {
                    return true;
                }

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    ((QueueItemAdapter)recyclerView.getAdapter()).moveItem(viewHolder.getAdapterPosition(),
                            target.getAdapterPosition());
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    PlayerQueue.getInstance(getActivity()).removeEpisode(viewHolder.getAdapterPosition(), getActivity());
                    rv.getAdapter().notifyDataSetChanged();
                }

                @Override
                public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                        View itemView = viewHolder.itemView;
                        float height = (float)itemView.getBottom() - (float)itemView.getTop();
                        float width = height / 3;

                        if (dX > 0){
                            Paint p = new Paint();
                            p.setColor(ContextCompat.getColor(getActivity(),R.color.green_done));
                            RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                            c.drawRect(background,p);
                            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_done_white_24dp);
                            RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                            c.drawBitmap(icon,null,icon_dest,p);
                        }
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            };
            ItemTouchHelper helper = new ItemTouchHelper(simpleCallback);
            helper.attachToRecyclerView(rv);
        }
    }
}

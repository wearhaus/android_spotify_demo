package com.example.steven.spautify.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.steven.spautify.R;
import com.example.steven.spautify.ViewPlaylistActivity;
import com.example.steven.spautify.WPlayerViewHolder;
import com.example.steven.spautify.musicplayer.Artst;
import com.example.steven.spautify.musicplayer.Playlst;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Steven on 4/28/2016.
 */
public class ArtstAdapter extends RecyclerView.Adapter<WPlayerViewHolder>
        implements DynamicRecycleListFragment.ItemTouchHelperAdapter {
    private MusicLibFragment mFragment;
    private ArrayList<Artst> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder


    // Provide a suitable constructor (depends on the kind of dataset)
    public ArtstAdapter(MusicLibFragment frag, ArrayList<Artst> dataset) {
        this.mFragment = frag;
        mDataset = dataset;
    }

    public void changeData(ArrayList<Artst> dataset) {
        mDataset = dataset;
        notifyDataSetChanged();
    }

    @Override
    public void onItemDismiss(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
    }


    // Create new views (invoked by the layout manager)
    @Override
    public WPlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_queue_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        WPlayerViewHolder vh = new WPlayerViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(WPlayerViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        final Artst p = mDataset.get(position);

        //holder.mContainer.setBackgroundColor(Color.argb(0, 0, 0, 0));
        holder.mContainer.setBackgroundResource(R.drawable.button_bg_toolbar_default);


        holder.mTitleView.setText("" + p.name);
//        holder.mAuthorView.setText("" + p.); // field may not be present

        if (p.artworkUrl != null) {
            Picasso.with(holder.mContainer.getContext()).load(p.artworkUrl).into(holder.mImageView);
        } else {
            holder.mImageView.setImageResource(0);
        }
        holder.mSourceSplashView.setImageResource(p.source.sourceSplashRes);


        holder.mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Playlst.mPlaylstCache.put(p.playlstId, p);
//
//                Activity act = mFragment.getActivity();
//                if (act != null) {
//                    Intent intent = new Intent(act, ViewPlaylistActivity.class);
//                    intent.putExtra(ViewPlaylistActivity.TAG_ID, p.playlstId);
//                    act.startActivity(intent);
//
//                }
            }
        });

        holder.mExtendedMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mFragment.getActivity())
                        .setTitle("Actions will appear here")
                        .setNeutralButton("Action A", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

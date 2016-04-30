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
import com.example.steven.spautify.ViewAlbumActivity;
import com.example.steven.spautify.WPlayerViewHolder;
import com.example.steven.spautify.musicplayer.SpotifyApi;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Album;

/**
 * Created by Steven on 4/28/2016.
 */
public class AlbmAdapter extends RecyclerView.Adapter<WPlayerViewHolder> implements DynamicRecycleListFragment.ItemTouchHelperAdapter {
    private MusicLibFragment mFragment;
    private ArrayList<Album> mDataset;


    // Provide a suitable constructor (depends on the kind of dataset)
    public AlbmAdapter(MusicLibFragment frag, ArrayList<Album> dataset) {
        this.mFragment = frag;
        mDataset = dataset;
    }

    public void changeData(ArrayList<Album> dataset) {
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

        final Album a = mDataset.get(position);

        //holder.mContainer.setBackgroundColor(Color.argb(0, 0, 0, 0));
        holder.mContainer.setBackgroundResource(R.drawable.button_bg_toolbar_default);


        holder.mTitleView.setText("" + a.name);
        holder.mAuthorView.setText("" + a.artists.get(0).name);

//            Picasso.with(holder.mContainer.getContext()).load(a.images.get(0).url).into(holder.mImageView);

        if (mFragment.showArtwork()) {
            Picasso.with(holder.mContainer.getContext()).load(a.images.get(0).url).into(holder.mImageView);
        } else {
            holder.mImageView.setVisibility(View.GONE);
            // this way goneSpace's bounds matter
        }

//            holder.mSourceSplashView.setImageResource(p.source.sourceSplashRes);


        holder.mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SpotifyApi.mAlbumCache.put(a.id, a);

                Activity act = mFragment.getActivity();
                if (act != null) {
                    Intent intent = new Intent(act, ViewAlbumActivity.class);
                    intent.putExtra(MusicLibFragment.TAG_ID, a.id);
                    act.startActivity(intent);

                }
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

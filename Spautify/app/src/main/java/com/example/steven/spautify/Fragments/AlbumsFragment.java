package com.example.steven.spautify.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.steven.spautify.R;
import com.example.steven.spautify.ViewAlbumActivity;
import com.example.steven.spautify.ViewPlaylistActivity;
import com.example.steven.spautify.musicplayer.Playlst;
import com.example.steven.spautify.musicplayer.SpotifyWebApiHandler;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Album;

/**
 * Created by Steven on 2/10/2016.
 */
public abstract class AlbumsFragment extends DynamicRecycleListFragment {



    @Override
    protected RecyclerView.Adapter createNewAdapter(Context context, ArrayList mList) {
        return new PLAdapter((ArrayList<Album>) mList);
    }

    @Override
    protected boolean canDragAndDrop() {
        return false;
    }





    //////////////////



//    public static class PLItem {
//        public Playlst playlst;
//        //public Type type;
//        public PLItem (Playlst s) {
//            playlst = s;
//        }
//
//    }

    public class PLAdapter extends RecyclerView.Adapter<PlaylistsFragment.PLViewHolder>
            implements ItemTouchHelperAdapter {
        private ArrayList<Album> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder


        // Provide a suitable constructor (depends on the kind of dataset)
        public PLAdapter(ArrayList<Album> dataset) {
            mDataset = dataset;
        }

        public void changeData(ArrayList<Album> dataset){
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
        public PlaylistsFragment.PLViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_queue_item, parent, false);
            // set the view's size, margins, paddings and layout parameters

            PlaylistsFragment.PLViewHolder vh = new PlaylistsFragment.PLViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(PlaylistsFragment.PLViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element

            final Album a = mDataset.get(position);

            //holder.mContainer.setBackgroundColor(Color.argb(0, 0, 0, 0));
            holder.mContainer.setBackgroundResource(R.drawable.button_bg_toolbar_default);


            holder.mTitleView.setText("" + a.name);
            holder.mAuthorView.setText("" + a.artists.get(0).name);

            Picasso.with(holder.mContainer.getContext()).load(a.images.get(0).url).into(holder.mImageView);


            holder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    SpotifyWebApiHandler.loadAlbum(a, new SpotifyWebApiHandler.LoadCallback() {
//                        @Override
//                        public void callback() {
//                            // whe callback comes back, the full thing is stored in the cache
//
//                        }
//                    });

                    SpotifyWebApiHandler.mAlbumCache.put(a.id, a);

                    Activity act = getActivity();
                    if (act != null) {
                        Intent intent = new Intent(act, ViewAlbumActivity.class);
                        intent.putExtra(ViewAlbumActivity.TAG_ID, a.id);
                        act.startActivity(intent);

                    }
                }
            });

            holder.mImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
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





}

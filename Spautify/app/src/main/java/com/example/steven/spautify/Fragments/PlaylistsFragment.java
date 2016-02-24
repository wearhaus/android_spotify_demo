package com.example.steven.spautify.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.steven.spautify.R;
import com.example.steven.spautify.ViewPlaylistActivity;
import com.example.steven.spautify.musicplayer.Playlst;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.SpotifyWebApiHandler;
import com.example.steven.spautify.musicplayer.WPlayer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Steven on 2/10/2016.
 */
public abstract class PlaylistsFragment extends DynamicRecycleListFragment {



    @Override
    protected RecyclerView.Adapter createNewAdapter(Context context, ArrayList mList) {
        return new PLAdapter((ArrayList<Playlst>) mList);
    }

    @Override
    protected boolean canDragAndDrop() {
        return false;
    }





    //////////////////


    public static class PLViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mContainer;
        public TextView mTitleView;
        public TextView mAuthorView;
        public ImageButton mImageButton;
        public ImageView mImageView;

        public PLViewHolder(View v) {
            super(v);
            mContainer =  itemView.findViewById(R.id.container);
            mTitleView = (TextView) itemView.findViewById(R.id.track_title);
            mAuthorView = (TextView) itemView.findViewById(R.id.track_author);
            mImageButton = (ImageButton) itemView.findViewById(R.id.stuffs);
            mImageView = (ImageView) itemView.findViewById(R.id.img);

            mAuthorView.setSelected(true);
            mAuthorView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            mAuthorView.setSingleLine(true);
            mTitleView.setSelected(true);
            mTitleView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            mTitleView.setSingleLine(true);
        }

    }

//    public static class PLItem {
//        public Playlst playlst;
//        //public Type type;
//        public PLItem (Playlst s) {
//            playlst = s;
//        }
//
//    }

    public class PLAdapter extends RecyclerView.Adapter<PLViewHolder>
            implements ItemTouchHelperAdapter {
        private ArrayList<Playlst> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder


        // Provide a suitable constructor (depends on the kind of dataset)
        public PLAdapter(ArrayList<Playlst> dataset) {
            mDataset = dataset;
        }

        public void changeData(ArrayList<Playlst> dataset){
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
        public PLViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_queue_item, parent, false);
            // set the view's size, margins, paddings and layout parameters

            PLViewHolder vh = new PLViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(PLViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element

            final Playlst p = mDataset.get(position);

            //holder.mContainer.setBackgroundColor(Color.argb(0, 0, 0, 0));
            holder.mContainer.setBackgroundResource(R.drawable.button_bg_toolbar_default);


            holder.mTitleView.setText("" + p.name);
            //holder.mAuthorView.setText("" + p.owner.display_name); is null
            if (p.is_public) {
                holder.mAuthorView.setText("public");
            } else {
                holder.mAuthorView.setText("private");
            }


            Picasso.with(holder.mContainer.getContext()).load(p.images.get(0).url).into(holder.mImageView);


            holder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    SpotifyWebApiHandler.loadPlaylist(p, new SpotifyWebApiHandler.LoadCallback() {
//                        @Override
//                        public void callback() {
//                            // whe callback comes back, the full thing is stored in the cache
//                            .
//                        }
//                    });



                    SpotifyWebApiHandler.mPlaylstCache.put(p.id, p);

                    Activity act = getActivity();
                    if (act != null) {
                        Intent intent = new Intent(act, ViewPlaylistActivity.class);
                        intent.putExtra(ViewPlaylistActivity.TAG_ID, p.id);
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

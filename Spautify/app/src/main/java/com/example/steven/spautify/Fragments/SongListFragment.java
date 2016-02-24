package com.example.steven.spautify.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
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
import com.example.steven.spautify.ViewAlbumActivity;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.SpotifyWebApiHandler;
import com.example.steven.spautify.musicplayer.WPlayer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Steven on 2/5/2016.
 */
public abstract class SongListFragment extends DynamicRecycleListFragment {



    @Override
    protected RecyclerView.Adapter createNewAdapter(Context context, ArrayList mList) {
        return new QueueAdapter((ArrayList<SngItem>) mList);
    }


    public enum ClickType {
        Queue(),
        SearchResult(),
        Lib(),
        LibAlbum(),
    }
    /** If true, then */
    protected abstract ClickType getClickType();

    @Override
    protected boolean canDragAndDrop() {
        return getClickType() == ClickType.Queue;
    }

    private void onQueueItemSelected(final int position, final Sng sng) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("" + sng.name)
                .setPositiveButton("Play", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        WPlayer.playItemInQueue(position, sng);
                    }
                })
                .setNeutralButton("Remove from queue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        WPlayer.removeFromQueue(position, sng);
                    }
                })
                .setNeutralButton("Open Album", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        openAlbumActivity(sng);
                    }
                })
                .setNegativeButton("Back", null);

        AlertDialog alert = builder.create();
        alert.show();
    }



    private void addtoQueueOrPlayNow(final Sng song) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("" + song.name)
                .setPositiveButton("Play Now", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        WPlayer.playSingleClearQueue(song);
                    }
                })
                .setNeutralButton("Add to Queue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        WPlayer.addtoEndOfQueue(song);
                    }
                })

                .setNegativeButton("Back", null);

        if (getClickType() != ClickType.LibAlbum) {
            builder.setNeutralButton("Open Album", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    openAlbumActivity(song);
                }
            });
        }

        AlertDialog alert = builder.create();
        alert.show();


    }

    private void openAlbumActivity(Sng song) {
        Activity act = getActivity();
        if (act != null) {

            Intent intent = new Intent(act, ViewAlbumActivity.class);
            intent.putExtra(ViewAlbumActivity.TAG_ID, song.album_id);
            act.startActivity(intent);

        }

    }



    //////////////////


    public static class QueueViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mContainer;
        public TextView mTitleView;
        public TextView mAuthorView;
        public ImageButton mImageButton;
        public ImageView mImageView;

        public QueueViewHolder(View v) {
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

    public static class SngItem {
        public Sng sng;
        public Type type;
        public SngItem (Sng s, Type t) {
            sng = s;
            type = t;
        }

        public enum Type {
            QueueBack,
            Current,
            Queue,
            NotInQueue,
        }



    }

    public class QueueAdapter extends RecyclerView.Adapter<QueueViewHolder>
            implements ItemTouchHelperAdapter {
        private ArrayList<SngItem> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder


        // Provide a suitable constructor (depends on the kind of dataset)
        public QueueAdapter(ArrayList<SngItem> dataset) {
            mDataset = dataset;
        }

        public void changeData(ArrayList<SngItem> dataset){
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
//            if (fromPosition < toPosition) {
//                for (int i = fromPosition; i < toPosition; i++) {
//                    Collections.swap(mDataset, i, i + 1);
//                }
//            } else {
//                for (int i = fromPosition; i > toPosition; i--) {
//                    Collections.swap(mDataset, i, i - 1);
//                }
//            }
//            notifyItemMoved(fromPosition, toPosition);

            WPlayer.swapQueueItems(fromPosition, toPosition, mDataset.get(fromPosition).sng);
        }



        // Create new views (invoked by the layout manager)
        @Override
        public QueueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_queue_item, parent, false);
            // set the view's size, margins, paddings and layout parameters

            QueueViewHolder vh = new QueueViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(QueueViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element



            final SngItem s = mDataset.get(position);
            //final String shortId = SpotifyController.getSpotifyIdFromUri(s.sng.spotifyUri);
            //Track t = SpotifyWebApiHandler.getTrackOnlyIfCached(shortId);

            if (s.type == SngItem.Type.Current) {
                holder.mContainer.setBackgroundColor(Color.argb(50, 0, 125, 250));
            } else {
                holder.mContainer.setBackgroundResource(R.drawable.button_bg_toolbar_default);
                //holder.mContainer.setBackgroundColor(Color.argb(0, 0, 0, 0));
            }

            holder.mTitleView.setText("" + s.sng.name);
            holder.mAuthorView.setText("" + s.sng.artistPrimary);


            //Picasso.with(holder.mContainer.getContext()).setIndicatorsEnabled(true);
            Picasso.with(holder.mContainer.getContext()).load(s.sng.album_image.url).into(holder.mImageView);



            holder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getClickType() == ClickType.Queue) {
                        WPlayer.playItemInQueue(position, s.sng);
                    } else if (getClickType() == ClickType.SearchResult) {
                        WPlayer.playSingleClearQueue(s.sng);
                    } else if (getClickType() == ClickType.Lib || getClickType() == ClickType.LibAlbum) {
                        // for now...
                        WPlayer.playSingleClearQueue(s.sng);
                    }
                }
            });

            holder.mImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // We shouldn't allow add to Queue, only jump queue to this... which is a weird operation
                    // Especially considering this same Sng might appear multiple times...
                    // To be superior than spotify, we ought to preserve QueueBack

                    if (getClickType() == ClickType.Queue) {
                        onQueueItemSelected(position, s.sng);
                    } else if (getClickType() == ClickType.SearchResult) {
                        addtoQueueOrPlayNow(s.sng);
                    } else if (getClickType() == ClickType.Lib || getClickType() == ClickType.LibAlbum) {
                        // for now...
                        addtoQueueOrPlayNow(s.sng);
                    }

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

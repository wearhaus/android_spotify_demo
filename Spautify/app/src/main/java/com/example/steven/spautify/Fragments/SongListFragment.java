package com.example.steven.spautify.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.steven.spautify.R;
import com.example.steven.spautify.ViewAlbumActivity;
import com.example.steven.spautify.WPlayerViewHolder;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.Source;
import com.example.steven.spautify.musicplayer.WPlayer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Steven on 2/5/2016.
 */
public abstract class SongListFragment extends DynamicRecycleListFragment {
//        extends DynamicRecycleListFragment<
//        SngItemAdapter.SngItem, WPlayerViewHolder, SngItemAdapter> {


    @Override
    protected RecyclerView.Adapter createNewAdapter(Context context, ArrayList mList) {
        return new SngItemAdapter(this, (ArrayList<SngItem>) mList);
    }


    public enum ClickType {
        Queue(),
        SearchResult(),
        Lib(),
        /** A lib, but we are looking at it's spotify album, so dont offer to look at album*/
        LibAlbum(),
    }
    /** If true, then */
    protected abstract ClickType getClickType();

    /** Default off due to OOM issues*/
    protected boolean showAlbum() {
        return false;
    }

    @Override
    protected boolean canDragAndDrop() {
        return getClickType() == ClickType.Queue;
    }


    private enum DialogItem {
        PlayInQueue("Play"),
        PlayFromLib("Play"),
        RemoveFromQueue("Remove from queue"),
        AddToQueue("Add to queue"),
        PlayNextLib("Play next"),
        OpenSpotifyAlbum("Open album"),
        //OpenInSoundCloud("Open in SoundCloud"),
        Back("Back"),

        ;

        private String text;
        DialogItem(String s) {
            text = s;
        }

    }


    private void onItemMenuSelected(final int position, final Sng sng) {

        ArrayList<DialogItem> dialogItems = new ArrayList<>();

        if (getClickType() == SongListFragment.ClickType.Queue) {
            dialogItems.add(DialogItem.PlayInQueue); // TODO detect if current song or not by reading position
            dialogItems.add(DialogItem.RemoveFromQueue);

        } else {
            dialogItems.add(DialogItem.PlayFromLib);
            dialogItems.add(DialogItem.AddToQueue);


        }

        if (sng.source == Source.Spotify) {
            if (getClickType() != SongListFragment.ClickType.LibAlbum) {
                dialogItems.add(DialogItem.OpenSpotifyAlbum);
            }
        } else if (sng.source == Source.Soundcloud) {
            //dialogItems.add(DialogItem.OpenInSoundCloud);
        }


        dialogItems.add(DialogItem.Back);



        final ArrayList<DialogItem> dialogItemsFinalized = dialogItems; // yeah java...
        CharSequence[] dialogNames = new CharSequence[dialogItems.size()];
        for (int i = 0; i < dialogItems.size(); i++) {
            dialogNames[i] = dialogItems.get(i).text;
        }

        // Null here means we end up ignoring the root's layout width and height attr, so we
        // need to manually specify it here, unless we figure out how to properly get a ViewGroup it inflate with
        View titleView = LayoutInflater.from(getActivity()).inflate(R.layout.recycler_queue_item_dialog_title, null);
        {
            WPlayerViewHolder vh = new WPlayerViewHolder(titleView);
            vh.setMarquee(true);

//            float heightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
//            vh.mContainer.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
//            vh.mContainer.getLayoutParams().height = (int) heightPx;

            vh.mTitleView.setText("" + sng.name);
            vh.mAuthorView.setText(sng.getFormattedArtistAlbumString());
            vh.mSourceSplashView.setImageResource(sng.source.sourceSplashRes);
            vh.mExtendedMenuButton.setVisibility(View.GONE);
            Picasso.with(vh.mContainer.getContext()).load(sng.artworkUrl).into(vh.mImageView);


        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCustomTitle(titleView)
                .setItems(dialogNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (dialogItemsFinalized.get(which)) {
                            case PlayFromLib:
                                WPlayer.playSingleClearQueue(sng); break;

                            case PlayInQueue:
                                WPlayer.playItemInQueue(position, sng); break;

                            case AddToQueue:
                                WPlayer.addtoEndOfQueue(sng); break;

                            case PlayNextLib:
                                WPlayer.addToFrontOfQueue(sng); break;

                            case RemoveFromQueue:
                                WPlayer.removeFromQueue(position, sng); break;

                            case OpenSpotifyAlbum:
                                openSpotifyAlbumActivity(sng); break;

                            case Back:
                                break; // do nothing
                        }


                    }
                });

        AlertDialog alert = builder.create();
        alert.show();


    }


    private void openSpotifyAlbumActivity(Sng song) {
        Activity act = getActivity();
        if (act != null) {
            Intent intent = new Intent(act, ViewAlbumActivity.class);
            intent.putExtra(ViewAlbumActivity.TAG_ID, song.spotifyAlbumId);
            act.startActivity(intent);
        }

    }



    //////////////////

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







    ////////////////////

    class SngItemAdapter extends RecyclerView.Adapter<WPlayerViewHolder>
            implements DynamicRecycleListFragment.ItemTouchHelperAdapter {


        private SongListFragment songListFragment;
        private ArrayList<SongListFragment.SngItem> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder


        // Provide a suitable constructor (depends on the kind of dataset)
        public SngItemAdapter(SongListFragment songListFragment, ArrayList<SongListFragment.SngItem> dataset) {
            this.songListFragment = songListFragment;
            mDataset = dataset;
        }

        public void changeData(ArrayList<SongListFragment.SngItem> dataset) {
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

            final SongListFragment.SngItem s = mDataset.get(position);

            if (s.type == SongListFragment.SngItem.Type.Current) {
                holder.mContainer.setBackgroundColor(Color.argb(50, 0, 125, 250));
            } else {
                holder.mContainer.setBackgroundResource(R.drawable.button_bg_toolbar_default);
                //holder.mContainer.setBackgroundColor(Color.argb(0, 0, 0, 0));
            }

            holder.mTitleView.setText("" + s.sng.name);
            holder.mAuthorView.setText(s.sng.getFormattedArtistAlbumString());


            holder.mSourceSplashView.setImageResource(s.sng.source.sourceSplashRes);

            //Picasso.with(holder.mContainer.getContext()).setIndicatorsEnabled(true);

            if (songListFragment.showAlbum()) {
                Picasso.with(holder.mContainer.getContext()).load(s.sng.artworkUrl).into(holder.mImageView);
            } else {
                //holder.mImgGoneSpace.getLayoutParams().
                holder.mImageView.setVisibility(View.GONE);
            }


            holder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (songListFragment.getClickType() == SongListFragment.ClickType.Queue) {
                        WPlayer.playItemInQueue(position, s.sng);
                    } else if (songListFragment.getClickType() == SongListFragment.ClickType.SearchResult) {
                        WPlayer.playSingleClearQueue(s.sng);
                    } else if (songListFragment.getClickType() == SongListFragment.ClickType.Lib || songListFragment.getClickType() == SongListFragment.ClickType.LibAlbum) {
                        // TODO auto-load rest of album/playlist/artist page into queue, or an id
                        // to load more form when the time comes.
                        WPlayer.playSingleClearQueue(s.sng);
                    }
                }
            });

            holder.mExtendedMenuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    songListFragment.onItemMenuSelected(position, s.sng);
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

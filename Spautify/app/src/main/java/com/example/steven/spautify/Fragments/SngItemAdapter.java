package com.example.steven.spautify.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.steven.spautify.R;
import com.example.steven.spautify.ViewAlbumActivity;
import com.example.steven.spautify.ViewArtistActivity;
import com.example.steven.spautify.WPlayerViewHolder;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.Source;
import com.example.steven.spautify.musicplayer.WPlayer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Steven on 4/28/2016.
 */
class SngItemAdapter extends RecyclerView.Adapter<WPlayerViewHolder> implements DynamicRecycleListFragment.ItemTouchHelperAdapter {


    private MusicLibFragment mFragment;
    private ArrayList<SngItem> mDataset;
    private MusicLibType mLibType;


    // Provide a suitable constructor (depends on the kind of dataset)
    public SngItemAdapter(MusicLibFragment frag, ArrayList<SngItem> dataset, MusicLibType muli) {
        this.mFragment = frag;
        mDataset = dataset;
        mLibType = muli;
    }

    public void changeData(ArrayList<SngItem> dataset) {
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

        final SngItem s = mDataset.get(position);

        if (s.type == SngItem.Type.Current) {
            holder.mContainer.setBackgroundColor(Color.argb(50, 0, 125, 250));
        } else {
            holder.mContainer.setBackgroundResource(R.drawable.button_bg_toolbar_default);
            //holder.mContainer.setBackgroundColor(Color.argb(0, 0, 0, 0));
        }

        if (!s.sng.streamable) {
            holder.mErrorView.setVisibility(View.VISIBLE);
            holder.mAuthorView.setAlpha(0.5f);
            holder.mTitleView.setAlpha(0.5f);
        } else {
            holder.mErrorView.setVisibility(View.GONE);
            holder.mAuthorView.setAlpha(1f);
            holder.mTitleView.setAlpha(1f);

        }
        holder.mTitleView.setText("" + s.sng.name);

        holder.mAuthorView.setText(s.sng.getFormattedArtistAlbumString());
        holder.mSourceSplashView.setImageResource(s.sng.source.sourceSplashRes);

        //Picasso.with(holder.mContainer.getContext()).setIndicatorsEnabled(true);

        if (mFragment.showArtwork()) {
            Picasso.with(holder.mContainer.getContext())
                    .load(s.sng.artworkUrl)
                    .placeholder(R.drawable.track_grey)
                    .error(R.drawable.track_grey)
                    .into(holder.mImageView);
        } else {
            holder.mImageView.setVisibility(View.GONE);
            // this way goneSpace's bounds matter
        }


        holder.mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLibType == MusicLibType.SongInQueue) {
                    if (s.sng.streamable) {
                        WPlayer.playItemInQueue(position, s.sng);
                    } else {
                        Toast.makeText(mFragment.getActivity(), "Track unavailable for streaming", Toast.LENGTH_SHORT).show();
                    }
                } else  {
//                        WPlayer.playSingleClearQueue(s.sng);
                    onItemMenuSelected(position, s.sng);
                }
            }
        });

        if (mLibType == MusicLibType.SongInQueue) {
            holder.mExtendedMenuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemMenuSelected(position, s.sng);
                }
            });
        } else {
            holder.mExtendedMenuButton.setVisibility(View.GONE);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }




    ///////////////////
    //////////////////



    private enum DialogItem {
        PlayInQueue("Play"),
        //        PlayFromLib("Play"),
        PlayKeepQueueLib("Play"),
        RemoveFromQueue("Remove from queue"),
        AddToQueue("Add to end of queue"),
        PlayNextLib("Play next"),
        OpenSpotifyAlbum("View album"),
        OpenArtist("View artist"),
        //OpenInSoundCloud("Open in SoundCloud"),
        Back("Cancel"),

        ;

        private String text;
        DialogItem(String s) {
            text = s;
        }

    }


    private void onItemMenuSelected(final int position, final Sng sng) {

        ArrayList<DialogItem> dialogItems = new ArrayList<>();

        if (mLibType == MusicLibType.SongInQueue) {

            if (sng.streamable) {
                dialogItems.add(DialogItem.PlayInQueue);
            }
            // TODO detect if current song or not by reading position
            dialogItems.add(DialogItem.RemoveFromQueue);

        } else {
            if (sng.streamable) {
                dialogItems.add(DialogItem.PlayKeepQueueLib);
                dialogItems.add(DialogItem.PlayNextLib);
                dialogItems.add(DialogItem.AddToQueue);
            }

        }

        if (sng.source == Source.Spotify) {
            if (mLibType != MusicLibType.SongInLibAlbum && sng.spotifyAlbumId != null) {
                dialogItems.add(DialogItem.OpenSpotifyAlbum);
            }
        } else if (sng.source == Source.Soundcloud) {
            //dialogItems.add(DialogItem.OpenInSoundCloud);
        }

        if (mLibType != MusicLibType.SongInLibArtst && sng.artstId != null) {
            dialogItems.add(DialogItem.OpenArtist);
        }


        dialogItems.add(DialogItem.Back);



        final ArrayList<DialogItem> dialogItemsFinalized = dialogItems; // yeah java...
        CharSequence[] dialogNames = new CharSequence[dialogItems.size()];
        for (int i = 0; i < dialogItems.size(); i++) {
            dialogNames[i] = dialogItems.get(i).text;
        }

        // Null here means we end up ignoring the root's layout width and height attr, so we
        // need to manually specify it here, unless we figure out how to properly get a ViewGroup it inflate with
        View titleView = LayoutInflater.from(mFragment.getActivity()).inflate(R.layout.recycler_queue_item_dialog_title, null);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(mFragment.getActivity())
                .setCustomTitle(titleView)
                .setItems(dialogNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (dialogItemsFinalized.get(which)) {
//                            case PlayFromLib:
                            case PlayKeepQueueLib:
                                WPlayer.playSingleKeepQueue(sng); break;

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

                            case OpenArtist:
                                openArtistActivity(sng); break;

                            case Back:
                                break; // do nothing
                        }


                    }
                });

        AlertDialog alert = builder.create();
        alert.show();


    }


    private void openSpotifyAlbumActivity(Sng song) {
        Activity act = mFragment.getActivity();
        if (act != null) {
            Intent intent = new Intent(act, ViewAlbumActivity.class);
            intent.putExtra(MusicLibFragment.TAG_ID, song.spotifyAlbumId);
            act.startActivity(intent);
        }

    }

    private void openArtistActivity(Sng song) {
        Activity act = mFragment.getActivity();
        if (act != null) {
            Intent intent = new Intent(act, ViewArtistActivity.class);
            intent.putExtra(MusicLibFragment.TAG_ID, song.artstId);
            act.startActivity(intent);
        }

    }


}

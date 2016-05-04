package com.example.steven.spautify.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.example.steven.spautify.musicplayer.Artst;
import com.example.steven.spautify.musicplayer.Playlst;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Album;

/**
 * Created by Steven on 4/28/2016.
 */
public abstract class MusicLibFragment extends DynamicRecycleListFragment {

    public static final String TAG_ID = "arg_tag_id";

    /** This needs to be set/unchanging from when onCreateView gets called
     * To change after that, just remove the fragment and create a new one. */
    public abstract MusicLibType getMusicLibType();

    protected boolean showArtwork() {
        return true;
    }

    @Override
    protected RecyclerView.Adapter createNewAdapter(Context context, ArrayList list) {
        switch (getMusicLibType()) {
            case Song:
            case SongInLibAlbum:
            case SongInQueue:
            case SongInLibArtst:
                return new SngItemAdapter(this, (ArrayList<SngItem>) list, getMusicLibType());

            case Playlist:
                return new PlaylstAdapter(this, (ArrayList<Playlst>) list);

            case Artist:
                return new ArtstAdapter(this, (ArrayList<Artst>) list);

            case Album:
                // TODO update adapter to non spotify-only
                return new AlbmAdapter(this, (ArrayList<Album>) list);

        }

        return null;

    }


    @Override
    protected boolean canDragAndDrop() {
        return false;
    }

    @Override
    protected boolean canSwipeToRefresh() {
        return false;
    }

}

package com.example.steven.spautify;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.steven.spautify.Fragments.ViewPlaylistFragment;
import com.example.steven.spautify.musicplayer.Playlst;
import com.example.steven.spautify.musicplayer.SpotifyWebApiHandler;

/**
 * Created by Steven on 2/10/2016.
 */
public class ViewPlaylistActivity extends LeafActivityWithPlayerBar {
    public static final String TAG_ID = "arg_playlst_id";
    public static final String TAG_SET_ACTIVITY_TITLE = "arg_set_act_title";

    private String mPlaylistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPlaylistId = getIntent().getExtras().getString(TAG_ID);
        // get mUserId before we call super.conCreate, since that creates the fragment.

        super.onCreate(savedInstanceState);

        onCreateAfterInflation();

        Playlst p = SpotifyWebApiHandler.mPlaylstCache.get(mPlaylistId);

        if (p != null) {
            setTitle("Playlist: " +  p.name);
        } else {
            setTitle("Playlist");
        }
    }



    @Override
    protected Fragment getFragmentForShellActivity() {
        return ViewPlaylistFragment.newInstance(mPlaylistId);
    }

    @Override
    protected boolean isParentDefinedInManifest() {
        return false;
    }

}
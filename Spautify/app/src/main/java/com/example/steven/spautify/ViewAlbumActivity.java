package com.example.steven.spautify;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.steven.spautify.Fragments.ViewAlbumFragment;
import com.example.steven.spautify.musicplayer.SpotifyWebApiHandler;

import kaaes.spotify.webapi.android.models.Album;

/**
 * Created by Steven on 2/10/2016.
 */
public class ViewAlbumActivity extends LeafActivityWithPlayerBar {
    public static final String TAG_ID = "arg_playlst_id";

    private String mAlbumId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAlbumId = getIntent().getExtras().getString(TAG_ID);
        // get mUserId before we call super.conCreate, since that creates the fragment.

        super.onCreate(savedInstanceState);

        onCreateAfterInflation();

        Album a = SpotifyWebApiHandler.mAlbumCache.get(mAlbumId);

        if (a != null) {
            setTitle("Album: " + a.name);
        } else {
            setTitle("Album");
        }
    }



    @Override
    protected Fragment getFragmentForShellActivity() {
        return ViewAlbumFragment.newInstance(mAlbumId);
    }

    @Override
    protected boolean isParentDefinedInManifest() {
        return false;
    }


}
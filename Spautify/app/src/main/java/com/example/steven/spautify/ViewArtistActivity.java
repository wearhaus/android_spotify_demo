package com.example.steven.spautify;

import android.app.Fragment;
import android.os.Bundle;

import com.example.steven.spautify.Fragments.MusicLibFragment;
import com.example.steven.spautify.Fragments.ViewArtstFragment;
import com.example.steven.spautify.Fragments.ViewPlaylistFragment;
import com.example.steven.spautify.musicplayer.Artst;
import com.example.steven.spautify.musicplayer.Playlst;

/**
 * Created by Steven on 2/10/2016.
 */
public class ViewArtistActivity extends LeafActivityWithPlayerBar {

    private String mArtstId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mArtstId = getIntent().getExtras().getString(MusicLibFragment.TAG_ID);
        // get mUserId before we call super.conCreate, since that creates the fragment.

        super.onCreate(savedInstanceState);

        onCreateAfterInflation();

        Artst p = Artst.mArtstCache.get(mArtstId);

        if (p != null) {
            setTitle("Artist: " +  p.name);
        } else {
            setTitle("Artist");
        }
        // TODO should be set from fragment, not in activity, since fragment handles loading the playlist
    }



    @Override
    protected Fragment getFragmentForShellActivity() {
        return ViewArtstFragment.newInstance(mArtstId);
    }

    @Override
    protected boolean isParentDefinedInManifest() {
        return false;
    }

}
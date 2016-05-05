package com.example.steven.spautify;

import android.app.Fragment;
import android.os.Bundle;

import com.example.steven.spautify.fragments.MusicLibFragment;
import com.example.steven.spautify.fragments.ViewSongsFragment;

/**
 * Created by Steven on 2/10/2016.
 */
public class ViewSongsActivity extends LeafActivityWithPlayerBar {

    private String mId;
    private int mMusicLibTypeOrdinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mId = getIntent().getExtras().getString(MusicLibFragment.TAG_ID);
        mMusicLibTypeOrdinal = getIntent().getExtras().getInt(MusicLibFragment.TAG_TYPE_ORDINAL);
        // populate these before super

        super.onCreate(savedInstanceState);
        onCreateAfterInflation();
    }



    @Override
    protected Fragment getFragmentForShellActivity() {
        return ViewSongsFragment.newInstance(mId, mMusicLibTypeOrdinal);
    }

    @Override
    protected boolean isParentDefinedInManifest() {
        return false;
    }

}
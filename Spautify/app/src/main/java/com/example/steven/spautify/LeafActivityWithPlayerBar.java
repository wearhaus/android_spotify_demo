package com.example.steven.spautify;

import android.support.annotation.NonNull;

/**
 * Created by Steven on 2/23/2016.
 */
public abstract class LeafActivityWithPlayerBar extends LeafActivity {

    @Override
    protected boolean careForWPlayerState() {
        return true;
    }

    @NonNull
    @Override
    protected Layout getLayoutType() {
        return Layout.Shell_WithToolbarEmptyLeafAndPlayerBar;
    }

    @Override
    protected void onWPlayerChanged() {
        refreshMusicPlayerState();
    }
}

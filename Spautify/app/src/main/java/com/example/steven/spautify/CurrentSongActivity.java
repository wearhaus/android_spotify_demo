package com.example.steven.spautify;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.steven.spautify.fragments.CurrentSongFragment;

public class CurrentSongActivity extends LeafActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onCreateAfterInflation();
    }

    @Override
    protected Fragment getFragmentForShellActivity() {
        return new CurrentSongFragment();
    }


    @NonNull
    @Override
    protected Layout getLayoutType() {
        return Layout.Shell_WithToolbarEmptyLeaf;
    }

    @Override
    protected boolean isParentDefinedInManifest() {
        return false;
    }

}

package com.example.steven.spautify;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.steven.spautify.Fragments.CurrentSongFragment;

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

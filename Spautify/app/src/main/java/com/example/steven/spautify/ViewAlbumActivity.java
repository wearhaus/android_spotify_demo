//package com.example.steven.spautify;
//
//import android.app.Fragment;
//import android.os.Bundle;
//
//import com.example.steven.spautify.fragments.MusicLibFragment;
//import com.example.steven.spautify.fragments.ViewAlbumFragment;
//import com.example.steven.spautify.musicplayer.SpotifyApi;
//
//import kaaes.spotify.webapi.android.models.Album;
//
///**
// * Created by Steven on 2/10/2016.
// */
//public class ViewAlbumActivity extends LeafActivityWithPlayerBar {
//
//    private String mAlbumId;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        mAlbumId = getIntent().getExtras().getString(MusicLibFragment.TAG_ID);
//        // get mUserId before we call super.conCreate, since that creates the fragment.
//
//        super.onCreate(savedInstanceState);
//
//        onCreateAfterInflation();
//
//        Album a = SpotifyApi.mAlbumCache.get(mAlbumId);
//
//        if (a != null) {
//            setTitle("Album: " + a.name);
//        } else {
//            setTitle("Album");
//        }
//    }
//
//
//
//    @Override
//    protected Fragment getFragmentForShellActivity() {
//        return ViewAlbumFragment.newInstance(mAlbumId);
//    }
//
//    @Override
//    protected boolean isParentDefinedInManifest() {
//        return false;
//    }
//
//
//}
package com.example.steven.spautify;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steven.spautify.Fragments.SongListFragment;
import com.example.steven.spautify.Fragments.SongSearchResultFragment;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.SoundCloudApiController;
import com.example.steven.spautify.musicplayer.SpotifyApiController;
import com.example.steven.spautify.musicplayer.WMusicProvider;
import com.example.steven.spautify.musicplayer.WPlayer;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Steven on 4/21/2016.
 */

/**
 * Created by Steven on 7/16/2015.
 */
public class SearchActivity extends LeafActivity {
    private static final String FRAGMENT_ID_RESULT = "search_result_frag";

    private static final boolean AUTO_SEARCH_DURING_TYPING = false;


    private View mSearchResultContainer;
    private View mSearchResultContainer2;
    private TextView mSearchText;
    private TextView mDisabledText;
    private ImageView mSearchCancel;
    private ImageView mSearchStart;

    private SongSearchResultFragment mFragResult;

    private MusicPlayerBar mMusicPlayerBar;


    private View mSearchOptionsLayout1;
    private View mSearchOptionsLayout2;
    private CheckBox mCheckBoxSpotify;
    private CheckBox mCheckBoxSoundCloud;
    private CheckBox mCheckBoxTracks;
    private CheckBox mCheckBoxAlbums;
    private CheckBox mCheckBoxArtist;
    private CheckBox mCheckBoxPlaylist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("FriendsActivity", "FriendsActivity onCreate");
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_search);

        // Needed here since we are using our own custom xml file instead of activity_blank_toolbar.xml, which lets the Super class automate the basic inflations.
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        onCreateAfterInflation();


        FragmentManager fm = getFragmentManager();



        // Just create a new one
        mFragResult = new SongSearchResultFragment();

        fm.beginTransaction()
                .replace(R.id.search_result_container, mFragResult, FRAGMENT_ID_RESULT)
                .commit();


        mDisabledText = (TextView) findViewById(R.id.disabled_text);

        mSearchResultContainer = findViewById(R.id.search_result_container);
        mSearchText = (TextView) findViewById(R.id.search_text);
        mSearchCancel = (ImageView) findViewById(R.id.cancel_button);
        mSearchStart = (ImageView) findViewById(R.id.search_button);




        mSearchOptionsLayout1 = findViewById(R.id.search_options);
        mSearchOptionsLayout2 = findViewById(R.id.search_options_2);

        View.OnClickListener ccc = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.option_spotify:
                        if (SpotifyApiController.getAuthState() == WMusicProvider.AuthState.LoggedIn) {
                            //mCheckBoxSoundCloud.setChecked(!mCheckBoxSpotify.isChecked());
                        } else {
                            //mCheckBoxSoundCloud.setChecked(true);
                            mCheckBoxSpotify.setChecked(false);
                            Toast toast = Toast.makeText(SearchActivity.this, "Log into Spotify to access Spotify music", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        break;
                    case R.id.option_soundcloud:
                        if (SpotifyApiController.getAuthState() == WMusicProvider.AuthState.LoggedIn) {
                            //mCheckBoxSpotify.setChecked(!mCheckBoxSoundCloud.isChecked());
                        } else {
//                            mCheckBoxSoundCloud.setChecked(true);
//                            mCheckBoxSpotify.setChecked(false);
                        }
                        break;


                    case R.id.option_tracks:
                    case R.id.option_album:
                    case R.id.option_artists:
                    case R.id.option_playlists:
                        // No turn off, only turn on
                        mCheckBoxTracks.setChecked(v.getId() == R.id.option_tracks);
                        mCheckBoxAlbums.setChecked(v.getId() == R.id.option_album);
                        mCheckBoxArtist.setChecked(v.getId() == R.id.option_artists);
                        mCheckBoxPlaylist.setChecked(v.getId() == R.id.option_playlists);
                        break;

                }


            }
        };

        mCheckBoxSpotify = (CheckBox) findViewById(R.id.option_spotify);
        mCheckBoxSoundCloud = (CheckBox) findViewById(R.id.option_soundcloud);
        mCheckBoxSpotify.setOnClickListener(ccc);
        mCheckBoxSoundCloud.setOnClickListener(ccc);

        mCheckBoxTracks = (CheckBox) findViewById(R.id.option_tracks);
        mCheckBoxAlbums = (CheckBox) findViewById(R.id.option_album);
        mCheckBoxArtist = (CheckBox) findViewById(R.id.option_artists);
        mCheckBoxPlaylist = (CheckBox) findViewById(R.id.option_playlists);
        mCheckBoxTracks.setOnClickListener(ccc);
        mCheckBoxAlbums.setOnClickListener(ccc); // don't exist on SoundCloud
        mCheckBoxArtist.setOnClickListener(ccc);
        mCheckBoxPlaylist.setOnClickListener(ccc);

        //mCheckBoxArtist.setVisibility(View.GONE); // TODO add support for
        //mCheckBoxPlaylist.setVisibility(View.GONE); // TODO add support for

        mMusicPlayerBar = (MusicPlayerBar) findViewById(R.id.music_player_bar);

        mCheckBoxSpotify.setChecked(SpotifyApiController.getAuthState() == WMusicProvider.AuthState.LoggedIn);
        mCheckBoxSoundCloud.setChecked(!mCheckBoxSpotify.isChecked());
        mCheckBoxTracks.setChecked(true);
    }


    @Override
    protected boolean isParentDefinedInManifest() {
        return true;
    }

    @Override
    protected boolean careForWPlayerState() {
        return true;
    }

    @Override
    protected void onWPlayerChangedUIThread() {

        refreshUI();
    }


    private void refreshUI() {
        if (WPlayer.getState() == WPlayer.WPlayerState.Off) {
            mSearchResultContainer.setVisibility(View.GONE);
            mSearchText.setVisibility(View.GONE);
            mSearchCancel.setVisibility(View.GONE);

            mDisabledText.setVisibility(View.VISIBLE);
            mDisabledText.setText("Player is off");

            mMusicPlayerBar.setVisibility(View.GONE);
            mSearchOptionsLayout1.setVisibility(View.GONE);
            mSearchOptionsLayout2.setVisibility(View.GONE);


        } else {

            mDisabledText.setVisibility(View.GONE);
            mSearchOptionsLayout1.setVisibility(View.VISIBLE);
            mSearchOptionsLayout2.setVisibility(View.VISIBLE);


            mSearchCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startAniCloseSearch();
                    mSearchText.setText("");
                    mFragResult.setResultingCancelled();
//                if (AUTO_SEARCH_DURING_TYPING && mhhh != null) {
//                    mhhh.cancel();
//                    mhhh = null;
//                }
                }
            });

            mSearchStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startAniOpenSearch();

                    if (mCheckBoxSpotify.isChecked()) {
                        searchSpotifyApi("" + mSearchText.getText());
                    } else {
                        searchSoundCloudApi("" + mSearchText.getText());
                    }

                    mFragResult.setResultingLoading();
                }
            });




//            if (AUTO_SEARCH_DURING_TYPING) {
//                mSearchText.addTextChangedListener(mTextWatcher);
//            }

            mSearchText.setVisibility(View.VISIBLE);
            if (mSearchOpened) {
                mSearchCancel.setVisibility(View.VISIBLE);
            } else {
                mSearchCancel.setVisibility(View.GONE);
            }

            if (WPlayer.getCurrentSng() == null) {
                mMusicPlayerBar.setVisibility(View.GONE);
            } else {
                mMusicPlayerBar.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    protected void onPause() {
        mMusicPlayerBar.onActivityPaused();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
        mMusicPlayerBar.onActivityResumed();
    }

    private boolean mSearchOpened = false;

    private void startAniOpenSearch() {
        if (!mSearchOpened) {
            mSearchOpened = true;
            mSearchCancel.setVisibility(View.VISIBLE);


        }

    }

    private void startAniCloseSearch() {
        if (mSearchOpened) {
            mSearchOpened = false;

            mSearchCancel.setVisibility(View.GONE);



        }

    }


//    private void doSearch() {
//
//
//    }
//
//
//    private TextWatcher mTextWatcher = new TextWatcher() {
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            Log.i("FA", "onTextChanged: " + s);
//            if (s.length() > 0) {
////                startAniOpenSearch();
////                searchSpotifyApi("" + mSearchText.getText());
////
////
////                mFragResult.setResultingLoading();
////                // Nope.. NEED to use a search button since this isn't going to our API backend, its using Spotify's,
//                // So we don't want to get our App throttled/banned from too much redundant activity.
//
//
//            } else {
//                mFragResult.setResultingLoading();
//                startAniCloseSearch();
//            }
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) {}
//    };






    @NonNull
    @Override
    protected Layout getLayoutType() {
        return Layout.Custom;
    }

    private void searchSoundCloudApi(final String query) {
        searchSoundCloudApi(query, 0, 10);
    }
    private void searchSoundCloudApi(final String query, final int offset, final int limit) {

        // this only does track title, not author, or smart features like soundclouds
        SoundCloudApiController.searchTrack(query, new SoundCloudApiController.GotItemArray() {
            @Override
            public void gotItem(SoundCloudApiController.TrackJson[] trackJsons) {

                ArrayList<SongListFragment.SngItem> songs = new ArrayList<>();
                for (SoundCloudApiController.TrackJson t : trackJsons) {
                    Log.d("createSearchResults", t.title);
                    songs.add(new SongListFragment.SngItem(new Sng(t), SongListFragment.SngItem.Type.NotInQueue));
                }


                mFragResult.setResult(songs, new SongSearchResultFragment.SetResultNextPage() {
                    @Override
                    public void requestNextPage() {
                        searchSoundCloudApi(query, offset+limit, limit);
                    }
                }, (offset>0));

            }

            @Override
            public void failure() {
                mFragResult.setResultingError("Server Error!");
            }
        }, offset, limit);

    }

    private void searchSpotifyApi(final String query) {
        // calls mFragResult
        // mFragResult.setResultingError("Server Error!");


        Log.i("createSearchResults", "Search Query: " + query);
        SpotifyApiController.getTempApi().searchTracks(query, new Callback<TracksPager>() {
            @Override
            public void success(final TracksPager trackspager, Response response) {

                ArrayList<SongListFragment.SngItem> songs = new ArrayList<>();
                for (Track t : trackspager.tracks.items) {
                    Log.d("createSearchResults", t.name + "" + ", " + t.uri + ", " + t.album.name);
                    songs.add(new SongListFragment.SngItem(new Sng(t), SongListFragment.SngItem.Type.NotInQueue));
                }

                mFragResult.setResult(songs, null, false);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Track failure", error.toString());
                mFragResult.setResultingError("Server Error!");
            }

        });


    }










}
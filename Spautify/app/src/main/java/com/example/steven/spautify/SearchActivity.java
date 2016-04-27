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

import com.example.steven.spautify.Fragments.AlbumSearchResultFragment;
import com.example.steven.spautify.Fragments.DynamicRecycleListFragment;
import com.example.steven.spautify.Fragments.PlaylistSearchResultFragment;
import com.example.steven.spautify.Fragments.SearchResultFragmentInterface;
import com.example.steven.spautify.Fragments.SongListFragment;
import com.example.steven.spautify.Fragments.SongSearchResultFragmentNEW;
import com.example.steven.spautify.musicplayer.Playlst;
import com.example.steven.spautify.musicplayer.SCRetrofitService;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.SoundCloudApi;
import com.example.steven.spautify.musicplayer.SpotifyApi;
import com.example.steven.spautify.musicplayer.WMusicProvider;
import com.example.steven.spautify.musicplayer.WPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit2.Call;

/**
 * Created by Steven on 4/21/2016.
 */

/**
 * Created by Steven on 7/16/2015.
 */
public class SearchActivity<S extends DynamicRecycleListFragment & SearchResultFragmentInterface, JR> extends LeafActivity {
    private static final String FRAGMENT_ID_RESULT = "search_result_frag";

    private static final boolean AUTO_SEARCH_DURING_TYPING = false;
    private static final String TAG = "SearchActivity";


    private View mSearchResultContainer;
    private View mSearchResultContainer2;
    private TextView mSearchText;
    private TextView mDisabledText;
    private ImageView mSearchCancel;
    private ImageView mSearchStart;

    private S mFragResult;

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
                        if (SpotifyApi.getAuthState() == WMusicProvider.AuthState.LoggedIn) {
                            //mCheckBoxSoundCloud.setChecked(!mCheckBoxSpotify.isChecked());
                        } else {
                            //mCheckBoxSoundCloud.setChecked(true);
                            mCheckBoxSpotify.setChecked(false);
                            Toast toast = Toast.makeText(SearchActivity.this, "Log into Spotify to access Spotify music", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        break;
                    case R.id.option_soundcloud:
                        if (SpotifyApi.getAuthState() == WMusicProvider.AuthState.LoggedIn) {
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

        mCheckBoxSpotify.setChecked(SpotifyApi.getAuthState() == WMusicProvider.AuthState.LoggedIn);
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
                    doSearch();
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

    @NonNull
    @Override
    protected Layout getLayoutType() {
        return Layout.Custom;
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
    /** If a search is to be cancelled, until all APIs support cancelling requests (Spotify uses retrofit1 which doesn't)
     * then we use this to check if a new request was issued, and if a returning request doesn't match this, then ignore results.
     * Important since we may switch to a different Fragment type resulting in the app crashing otherwise*/
    private int mSearchHash = 0;
    private SearchType mSearchType = null;


    private void doSearch() {
        if (!mSearchOpened) {
            mSearchOpened = true;
            mSearchCancel.setVisibility(View.VISIBLE);
        }

        mSearchHash++;

        SearchType newSearchType;

        if (mCheckBoxAlbums.isChecked()) {
            newSearchType = SearchType.Album;
        } else if (mCheckBoxPlaylist.isChecked()) {
            newSearchType = SearchType.Playlist;
        } else if (mCheckBoxArtist.isChecked()) {
            newSearchType = SearchType.Artist;
        } else {
            newSearchType = SearchType.Song;
        }

        if (mSearchType != newSearchType) {
            mSearchType = newSearchType;
            Log.i("search", "Switching type of result fragment");
            switch (mSearchType) {
                case Album:
                    mFragResult = (S) new AlbumSearchResultFragment();
                    break;
                case Artist:
                case Playlist:
                    mFragResult = (S) new PlaylistSearchResultFragment();
                    break;
                case Song:
                    mFragResult = (S) new SongSearchResultFragmentNEW();
                    break;

            }

            FragmentManager fm = getFragmentManager();
            fm.beginTransaction()
                    .replace(R.id.search_result_container, mFragResult, FRAGMENT_ID_RESULT)
                    .commit();


        }

        if (mCheckBoxSpotify.isChecked()) {
            searchSpotifyApi(mSearchType, "" + mSearchText.getText(), mSearchHash, 0, 12);
        } else {
            searchSoundCloudApi(mSearchType, "" + mSearchText.getText(), mSearchHash, 0, 12);
        }



        mFragResult.setResultingLoading();
    }

    private void startAniCloseSearch() {
        if (mSearchOpened) {
            mSearchOpened = false;
            mSearchCancel.setVisibility(View.GONE);
        }
    }


    private enum SearchType {
        Song(),
        Playlist(),
        Artist(),
        Album();
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






    /** Search through Soundcloud Api using some crazy type casting to keep code as dry as possible.
     * */
    private void searchSoundCloudApi(final SearchType st, final String query, final int hash, final int offset, final int limit) {
        // Breaks with spaces.
        String q = query.replace(" ", "-");
        // limit max is 200, default is 10

        Map<String, String> options = new HashMap<>();
        options.put(SCRetrofitService.CLIENT_ID, SoundCloudApi.CLIENT_ID);
        options.put(SCRetrofitService.OFFSET, ""+offset);
        options.put(SCRetrofitService.LIMIT, ""+limit);
        options.put(SCRetrofitService.QUERY, query);
        options.put(SCRetrofitService.PAGINATE, ""+1);

        Call<JR> cc = null;
        switch (st) {
            case Song:
                cc = (Call<JR>) SoundCloudApi.getApiService().searchTracks(options);
                break;
            case Album:
            case Artist:
            case Playlist:
                options.put(SCRetrofitService.COMPACT, "compact");
                cc = (Call<JR>) SoundCloudApi.getApiService().searchPlaylists(options);
                break;

        }

        // crazy casting time:
        cc.enqueue(new retrofit2.Callback<JR>() {

            @Override
            public void onResponse(Call<JR> call, retrofit2.Response<JR> response) {
                if (hash != mSearchHash) return;
                // TODO for production, add try catch in case some errors happen with the 3rd party's side
                Log.w("search", ""+response.body());

                ArrayList<JR> jrList = new ArrayList<>();
                switch (st) {
                    case Song:
                        jrList = (ArrayList<JR>) new ArrayList<SongListFragment.SngItem>();
                        for (SoundCloudApi.TrackJson t : ((SoundCloudApi.SearchTrackJson) response.body()).collection) {
                            ((ArrayList<SongListFragment.SngItem>) jrList).add(new SongListFragment.SngItem(new Sng(t), SongListFragment.SngItem.Type.NotInQueue));
                        }
                        break;
                    case Playlist:
                        jrList = (ArrayList<JR>) new ArrayList<Playlst>();
                        for (SoundCloudApi.PlaylistJson t : ((SoundCloudApi.SearchPlaylistJson) response.body()).collection) {
                            ((ArrayList<Playlst>) jrList).add(new Playlst(t));
                        }
                        break;
                }



                mFragResult.setResult(jrList, new DynamicRecycleListFragment.SearchResultNextPage() {
                    @Override
                    public void requestNextPage() {
                        searchSoundCloudApi(st, query, hash, offset+limit, limit);
                    }
                }, (offset>0));
            }

            @Override
            public void onFailure(Call<JR> call, Throwable t) {
                Log.e(TAG, "Server Error: " + t);
                if (hash != mSearchHash) return;
                mFragResult.setResultingError("Server Error!");
            }
        });
    }

//    private void searchSoundCloudApi(final SearchType st, final String query, final int hash, final int offset, final int limit) {
//
//        // this only does track title, not author, or smart features like soundclouds
//        SoundCloudApiController.searchTrack(query, new SoundCloudApiController.GotTrackArray() {
//            @Override
//            public void gotItem(SoundCloudApiController.TrackJson[] trackJsons) {
//                if (hash != mSearchHash) return;
//
//                ArrayList<SongListFragment.SngItem> songs = new ArrayList<>();
//                for (SoundCloudApiController.TrackJson t : trackJsons) {
//                    songs.add(new SongListFragment.SngItem(new Sng(t), SongListFragment.SngItem.Type.NotInQueue));
//                }
//
//
//                mFragResult.setResult(songs, new DynamicRecycleListFragment.SearchResultNextPage() {
//                    @Override
//                    public void requestNextPage() {
//                        searchSoundCloudApi(st, query, hash, offset+limit, limit);
//                    }
//                }, (offset>0));
//
//            }
//
//            @Override
//            public void failure() {
//                if (hash != mSearchHash) return;
//                mFragResult.setResultingError("Server Error!");
//            }
//        }, offset, limit);
//    }




    private void searchSpotifyApi(final SearchType st, final String query, final int hash, final int offset, final int limit) {
        Log.i("createSearchResults", "Search Query: " + query);

        // TODO: since Spotify is retrofit1, didn't bother keeping code super dry here with casting...

        Map<String, Object> options = new HashMap<>();
        options.put(SCRetrofitService.OFFSET, ""+offset);
        options.put(SCRetrofitService.LIMIT, ""+limit);
        ////////

        Call<JR> cc = null;
        switch (st) {
            case Song:
                SpotifyApi.getTempApi().searchTracks(query, options, new Callback<TracksPager>() {
                    @Override
                    public void success(final TracksPager trackspager, Response response) {
                        if (hash != mSearchHash) return;

                        ArrayList<SongListFragment.SngItem> songs = new ArrayList<>();
                        for (Track t : trackspager.tracks.items) {
                            Log.d("createSearchResults", t.name + "" + ", " + t.uri + ", " + t.album.name);
                            songs.add(new SongListFragment.SngItem(new Sng(t), SongListFragment.SngItem.Type.NotInQueue));
                        }

                        mFragResult.setResult(songs, new DynamicRecycleListFragment.SearchResultNextPage() {
                            @Override
                            public void requestNextPage() {
                                searchSpotifyApi(st, query, hash, offset+limit, limit);
                            }
                        }, (offset>0));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "Server Error: " + error);
                        if (hash != mSearchHash) return;
                        mFragResult.setResultingError("Server Error!");
                    }

                });


                break;
            case Album:
            case Artist:
            case Playlist:

                SpotifyApi.getTempApi().searchPlaylists(query, options, new Callback<PlaylistsPager>() {
                    @Override
                    public void success(final PlaylistsPager playlistsPager, Response response) {
                        if (hash != mSearchHash) return;

                        ArrayList<Playlst> list = new ArrayList<>();
                        for (PlaylistSimple t : playlistsPager.playlists.items) {
                            list.add(new Playlst(t));
                        }

                        mFragResult.setResult(list, new DynamicRecycleListFragment.SearchResultNextPage() {
                            @Override
                            public void requestNextPage() {
                                searchSpotifyApi(st, query, hash, offset+limit, limit);
                            }
                        }, (offset>0));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "Server Error: " + error);
                        if (hash != mSearchHash) return;
                        mFragResult.setResultingError("Server Error!");
                    }

                });

                break;

        }


    }










}
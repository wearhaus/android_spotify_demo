package com.example.steven.spautify;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.steven.spautify.Fragments.AlbumSearchResultFragment;
import com.example.steven.spautify.Fragments.DynamicRecycleListFragment;
import com.example.steven.spautify.Fragments.PlaylistSearchResultFragment;
import com.example.steven.spautify.Fragments.SearchResultFragmentInterface;
import com.example.steven.spautify.Fragments.SearchType;
import com.example.steven.spautify.Fragments.SongListFragment;
import com.example.steven.spautify.Fragments.SongSearchResultFragmentNEW;
import com.example.steven.spautify.musicplayer.Playlst;
import com.example.steven.spautify.musicplayer.SCRetrofitService;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.SoundCloudApi;
import com.example.steven.spautify.musicplayer.Source;
import com.example.steven.spautify.musicplayer.SpotifyApi;
import com.example.steven.spautify.musicplayer.WPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final boolean AUTO_SEARCH_DURING_TYPING = false;
    private static final String TAG = "SearchActivity";


    private TextView mSearchText;
    private TextView mDisabledText;
    private ImageView mSearchCancel;
    private ImageView mSearchStart;

    private MusicPlayerBar mMusicPlayerBar;


//    private View mSearchOptionsLayout1;
    private View mSearchOptionsLayout2;

//    private CheckBox mCheckBoxSpotify;
//    private CheckBox mCheckBoxSoundCloud;
    private CheckBox mCheckBoxTracks;
    private CheckBox mCheckBoxAlbums;
    private CheckBox mCheckBoxArtist;
    private CheckBox mCheckBoxPlaylist;


    private MyViewPageAdapter mViewPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "FriendsActivity onCreate");
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_search);

        // Needed here since we are using our own custom xml file instead of activity_blank_toolbar.xml, which lets the Super class automate the basic inflations.
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        onCreateAfterInflation();


        mDisabledText = (TextView) findViewById(R.id.disabled_text);

//        mSearchResultContainer = findViewById(R.id.search_result_container);
        mSearchText = (TextView) findViewById(R.id.search_text);
        mSearchCancel = (ImageView) findViewById(R.id.cancel_button);
        mSearchStart = (ImageView) findViewById(R.id.search_button);


        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        FragmentManager fm = getFragmentManager();


        mSearchType = SearchType.Song; // CAnt be null or else adapter freaks out about null Fragments from getItem
        mViewPagerAdapter = new MyViewPageAdapter(fm);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(mViewPagerListener);
        mTabLayout.setupWithViewPager(mViewPager);






//        mSearchOptionsLayout1 = findViewById(R.id.search_options);
        mSearchOptionsLayout2 = findViewById(R.id.search_options_2);

        View.OnClickListener ccc = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
//                    case R.id.option_spotify:
//                        if (SpotifyApi.getAuthState() == WMusicProvider.AuthState.LoggedIn) {
//                            //mCheckBoxSoundCloud.setChecked(!mCheckBoxSpotify.isChecked());
//                        } else {
//                            //mCheckBoxSoundCloud.setChecked(true);
//                            mCheckBoxSpotify.setChecked(false);
//                            Toast toast = Toast.makeText(SearchActivity.this, "Log into Spotify to access Spotify music", Toast.LENGTH_SHORT);
//                            toast.show();
//                        }
//
//                        break;
//                    case R.id.option_soundcloud:
//                        if (SpotifyApi.getAuthState() == WMusicProvider.AuthState.LoggedIn) {
//                            //mCheckBoxSpotify.setChecked(!mCheckBoxSoundCloud.isChecked());
//                        } else {
////                            mCheckBoxSoundCloud.setChecked(true);
////                            mCheckBoxSpotify.setChecked(false);
//                        }
//                        break;


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

//        mCheckBoxSpotify = (CheckBox) findViewById(R.id.option_spotify);
//        mCheckBoxSoundCloud = (CheckBox) findViewById(R.id.option_soundcloud);
//        mCheckBoxSpotify.setOnClickListener(ccc);
//        mCheckBoxSoundCloud.setOnClickListener(ccc);

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

//        mCheckBoxSpotify.setChecked(SpotifyApi.getAuthState() == WMusicProvider.AuthState.LoggedIn);
//        mCheckBoxSoundCloud.setChecked(!mCheckBoxSpotify.isChecked());
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
//            mSearchResultContainer.setVisibility(View.GONE);
            mSearchText.setVisibility(View.GONE);
            mSearchCancel.setVisibility(View.GONE);

            mDisabledText.setVisibility(View.VISIBLE);
            mDisabledText.setText("Player is off");

            mMusicPlayerBar.setVisibility(View.GONE);
//            mSearchOptionsLayout1.setVisibility(View.GONE);
            mSearchOptionsLayout2.setVisibility(View.GONE);


        } else {

            mDisabledText.setVisibility(View.GONE);
//            mSearchOptionsLayout1.setVisibility(View.VISIBLE);
            mSearchOptionsLayout2.setVisibility(View.VISIBLE);


            mSearchCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startAniCloseSearch();
                    mSearchText.setText("");
                    // TODO cancel queries
                    mViewPagerAdapter.setResultAllCancelled();
//                if (AUTO_SEARCH_DURING_TYPING && mhhh != null) {
//                    mhhh.cancel();
//                    mhhh = null;
//                }
                }
            });

            mSearchStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doSearch("" + mSearchText.getText());
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

    // TODO this concept will expand to inlcude the storage for the actual data we load,
    // so when 2 have 3+ frags in the tabs, we can search on all 3, and not worry about data loss or null fragments
    /** The suffix to add to end of tab title, "" means nothing, Set back to "" when a tab is selected*/
    private String[] mTabTitleSuffix = new String[]{"",""};


    private void doSearch(String query) {
        if (query == null || query.length() <= 0) return;
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
            // so we need a new adapter:
            mViewPagerAdapter.notifyDataSetChanged();
            // notify plus overriding getItemPosition in the adapter allows us to change the fragments

//            FragmentManager fm = getFragmentManager();
//            mViewPagerAdapter = new MyViewPageAdapter(fm);
//            mViewPager.setAdapter(mViewPagerAdapter);
//            mTabLayout.setupWithViewPager(mViewPager);

        }

        if (Source.Spotify.isPlaybackAuthed()) {
            S frag = mViewPagerAdapter.getFragmentBySource(Source.Spotify);
            if (frag != null) {
                frag.setResultNewQuery();
            }
            searchSpotifyApi(mSearchType, query, mSearchHash, 0, 12);
        }

        if (Source.Soundcloud.isPlaybackAuthed()) {
            S frag = mViewPagerAdapter.getFragmentBySource(Source.Soundcloud);
            if (frag != null) {
                frag.setResultNewQuery();
            }
            searchSoundCloudApi(mSearchType, query, mSearchHash, 0, 12);
        }

    }

    private void startAniCloseSearch() {
        if (mSearchOpened) {
            mSearchOpened = false;
            mSearchCancel.setVisibility(View.GONE);
        }
    }




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
////                mFragResult.setResultNewQuery();
////                // Nope.. NEED to use a search button since this isn't going to our API backend, its using Spotify's,
//                // So we don't want to get our App throttled/banned from too much redundant activity.
//
//
//            } else {
//                mFragResult.setResultNewQuery();
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
        setTabSuffix(Source.Soundcloud, "...");

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


                setTabSuffix(Source.Soundcloud, jrList);
                S frag = mViewPagerAdapter.getFragmentBySource(Source.Soundcloud);
                if (frag != null) {
                    DynamicRecycleListFragment.SearchResultNextPage nextPage = null;
                    if (jrList.size() >= limit) {
                        nextPage = new DynamicRecycleListFragment.SearchResultNextPage() {
                            @Override
                            public void requestNextPage() {
                                searchSoundCloudApi(st, query, hash, offset + limit, limit);
                            }
                        };
                    }
                    frag.setResult(jrList, nextPage, (offset > 0));
                }
                // TODO store data nstead of store inside frag.  This means setResult adding is deprecated
            }

            @Override
            public void onFailure(Call<JR> call, Throwable t) {
                Log.e(TAG, "Server Error: " + t);
                if (hash != mSearchHash) return;
                setTabSuffix(Source.Soundcloud, "");
                S frag = mViewPagerAdapter.getFragmentBySource(Source.Soundcloud);
                if (frag != null) {
                    frag.setResultError("Server Error!");
                }
            }

        });
    }




    private void searchSpotifyApi(final SearchType st, final String query, final int hash, final int offset, final int limit) {
        Log.i("createSearchResults", "Search Query: " + query);

        // TODO: since Spotify is retrofit1, didn't bother keeping code super dry here with casting...

        Map<String, Object> options = new HashMap<>();
        options.put(SCRetrofitService.OFFSET, ""+offset);
        options.put(SCRetrofitService.LIMIT, ""+limit);
        ////////
        setTabSuffix(Source.Spotify, "...");

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

                        setTabSuffix(Source.Spotify, songs);
                        S frag = mViewPagerAdapter.getFragmentBySource(Source.Spotify);
                        if (frag != null) {
                            DynamicRecycleListFragment.SearchResultNextPage nextPage = null;
                            if (songs.size() >= limit) {
                                nextPage = new DynamicRecycleListFragment.SearchResultNextPage() {
                                    @Override
                                    public void requestNextPage() {
                                        searchSpotifyApi(st, query, hash, offset + limit, limit);
                                    }
                                };
                            }
                            frag.setResult(songs, nextPage, (offset > 0));
                        }



                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "Server Error: " + error);
                        if (hash != mSearchHash) return;
                        setTabSuffix(Source.Spotify, "");
                        S frag = mViewPagerAdapter.getFragmentBySource(Source.Spotify);
                        if (frag != null) {
                            frag.setResultError("Server Error!");
                        }
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

                        setTabSuffix(Source.Spotify, list);
                        S frag = mViewPagerAdapter.getFragmentBySource(Source.Spotify);
                        if (frag != null) {
                            DynamicRecycleListFragment.SearchResultNextPage nextPage = null;
                            if (list.size() >= limit) {
                                nextPage = new DynamicRecycleListFragment.SearchResultNextPage() {
                                    @Override
                                    public void requestNextPage() {
                                        searchSpotifyApi(st, query, hash, offset + limit, limit);
                                    }
                                };
                            }
                            frag.setResult(list, nextPage, (offset > 0));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "Server Error: " + error);
                        if (hash != mSearchHash) return;
                        setTabSuffix(Source.Spotify, "");
                        S frag = mViewPagerAdapter.getFragmentBySource(Source.Spotify);
                        if (frag != null) {
                            frag.setResultError("Server Error!");
                        }
                    }

                });

                break;

        }


    }






    /**
     * TODO when we have a 3rd provider, we need to worry about the lists being deloaded...
     * This would work by storing the values here, and trying to refresh a frag when possible, but if null do nothing.
     * Then whenever getItem is called (aka create fragment), we have the adapter catch it up to anything that happened by
     * calling setResult with the appropriate value.  That means that we have to store loading, error, mList all here for
     * each source, and have them abstract the searchtype away.  This will be undertajen
     *
     * Also, when a provider is not authed (ex/ spotify logged in to), perhaps not provide that tab at all.
     */
    private class MyViewPageAdapter extends FragmentPagerAdapter {
        private final int size = 2;
        Fragment[] instantiatedFrags = new Fragment[size];

        public MyViewPageAdapter(FragmentManager fm) {
            super(fm);
            Log.e(TAG, "create MyViewPageAdapter");
        }

        @Override
        public Fragment getItem(int i) {
            Log.e(TAG, "getItem: " + i);
            // NOte: these are independent of position; ONLY inited search type.
            // TODO do we care about the 'proper' way to pass args to fragments (via bundle?)
            Fragment frag = null;
            switch (mSearchType) {
                case Album:
                    frag = new AlbumSearchResultFragment();
                    break;
                case Artist:
                    frag = new PlaylistSearchResultFragment(); ////// TODO TODO TODO
                    break;
                case Playlist:
                    frag = new PlaylistSearchResultFragment();
                    break;
                case Song:
                    frag = new SongSearchResultFragmentNEW();
                    break;

            }

            Bundle args = new Bundle();
            Source source;
            if (i == 0) {
                source = Source.Spotify;
            } else {
                source = Source.Soundcloud;
            }
            args.putString(SearchResultFragmentInterface.TAG_SOURCE_PREFIX, source.prefix);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public long getItemId(int position) {
            // need to mark items with it's searchtype, so if we switch, the adapter knows it's old
            // we dont lookup the item, since this is only assigned at items, creation, and when we want to check if we are to
            // create a new item, it checks if an item with this itemid exists.  To switch search Types, we need to invalidate
            // all ids that were created by using the universal search type in the itemid
            return super.getItemId(position) + (mSearchType.ordinal()*100);
        }

        @Override
        public int getItemPosition(Object object) {
            S frag = (S) object;

            if (mSearchType == frag.getSearchType()) {
                Log.e(TAG, "getItemPosition: same class");
                return super.getItemPosition(object);
            } else {
                Log.e(TAG, "getItemPosition: POSITION_NONE");
                // This lets the adapter know it has to recreate the fragment instead of keeping it around, allowing getItem to create a new one
                return POSITION_NONE;
            }
        }

        @Override
        public int getCount() {
            return size;
        }

        @Override
        public String getPageTitle(int position) {
            String suffix = mTabTitleSuffix[position];
            if (suffix == null) suffix = "";
            switch (position) {
                case 0:  return "Spotify" + suffix;
                case 1:  return "SoundCloud" + suffix;
                // TODO figure out how to include waiting dots in tablayout; may require some ultra-hacking though,
                // or not using TabLayout's setupWithViewPager
            }

            // To change the name of the tab, you need to call adapter.notifyDataSetChanged(), but this also results in
            // getItem being called again and new Fragments being created
            // Update: it seems calling setupWithViewPager again will cause the titles to be regrabbed
            return null;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment frag = (Fragment) super.instantiateItem(container, position);
            Log.e(TAG, "instantiateItem: " + ((S) frag).getSearchType() + ",  " + getItemId(position));
            instantiatedFrags[position] = frag;
            return frag;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.d(TAG, "destroyItem: " + position);
            instantiatedFrags[position] = null;
            super.destroyItem(container, position, object);
        }

        /** Our custom method to retrieve a fragment without forcibly creating a new one.
         * Is null if the item hasn't yet been created.
         * See: http://stackoverflow.com/questions/8785221/retrieve-a-fragment-from-a-viewpager*/
        @Nullable
        public Fragment getInstantiatedItem(int position) {
            Log.d(TAG, "getInstantiatedItem: " + position + ", " + instantiatedFrags[position]);
            return instantiatedFrags[position];
        }

        void setResultAllCancelled() {
            for (Fragment f : instantiatedFrags) {
                if (f != null) {
                    ((SearchResultFragmentInterface) f).setResultCancelled();
                }
            }
        }
//        void setResultAllLoading() {
//            for (Fragment f : instantiatedFrags) {
//                if (f != null) {
//                    ((SearchResultFragmentInterface) f).setResultNewQuery();
//                }
//            }
//        }

        S getFragmentBySource(Source s) {
            if (s == Source.Spotify) return (S) instantiatedFrags[0];
            if (s == Source.Soundcloud) return (S) instantiatedFrags[1];
            return null;
        }

    }

    private void setTabSuffix(Source source, @NonNull List l) {
        if (l.size() > 0) {
            setTabSuffix(source, l.size() + "+");
        } else {
            setTabSuffix(source, "0");
        }
    }
    private void setTabSuffix(Source source, String s) {
        int position;
        if (source == Source.Spotify) {
            position = 0;
        } else {
            position = 1;
        }


        if (s == "" || mViewPager.getCurrentItem() != position) {
            if (s != "") s = " (" + s + ")";
            mTabTitleSuffix[position] = s;
            mTabLayout.setupWithViewPager(mViewPager);
        }
    }


    ViewPager.OnPageChangeListener mViewPagerListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            // use mViewPager.getCurrentItem() to get current position outside of here.
            // if the tab is selected, dont need the title to display any action
            if (mTabTitleSuffix[position] != "") {
                mTabTitleSuffix[position] = "";
                mTabLayout.setupWithViewPager(mViewPager);
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };



}
package com.example.steven.spautify;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.steven.spautify.fragments.MusicLibType;
import com.example.steven.spautify.fragments.SearchResultFragment;
import com.example.steven.spautify.fragments.SngItem;
import com.example.steven.spautify.musicplayer.Artst;
import com.example.steven.spautify.musicplayer.Playlst;
import com.example.steven.spautify.musicplayer.SCRetrofitService;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.SoundCloudApi;
import com.example.steven.spautify.musicplayer.Source;
import com.example.steven.spautify.musicplayer.SpotifyApi;
import com.example.steven.spautify.musicplayer.WPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
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
public class SearchActivity<JR> extends LeafActivity {

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


        mLibType = MusicLibType.Song; // CAnt be null or else adapter freaks out about null Fragments from getItem
        mViewPagerAdapter = new MyViewPageAdapter(fm);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(mViewPagerListener);
        mTabLayout.setupWithViewPager(mViewPager);



        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doSearch();
                    return true;
                }
                return false;
            }
        });



        mSearchOptionsLayout2 = findViewById(R.id.search_options_2);

        View.OnClickListener ccc = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
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


        mCheckBoxTracks = (CheckBox) findViewById(R.id.option_tracks);
        mCheckBoxAlbums = (CheckBox) findViewById(R.id.option_album);
        mCheckBoxArtist = (CheckBox) findViewById(R.id.option_artists);
        mCheckBoxPlaylist = (CheckBox) findViewById(R.id.option_playlists);
        mCheckBoxTracks.setOnClickListener(ccc);
        mCheckBoxAlbums.setOnClickListener(ccc); // don't exist on SoundCloud
        mCheckBoxArtist.setOnClickListener(ccc);
        mCheckBoxPlaylist.setOnClickListener(ccc);


        mMusicPlayerBar = (MusicPlayerBar) findViewById(R.id.music_player_bar);

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
            mSearchText.setVisibility(View.GONE);
            mSearchCancel.setVisibility(View.GONE);

            mDisabledText.setVisibility(View.VISIBLE);
            mDisabledText.setText("Player is off");

            mMusicPlayerBar.setVisibility(View.GONE);
            mSearchOptionsLayout2.setVisibility(View.GONE);


        } else {

            mDisabledText.setVisibility(View.GONE);
            mSearchOptionsLayout2.setVisibility(View.VISIBLE);


            mSearchCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startAniCloseSearch();
                    mSearchText.setText("");

                    for (Source source : SearchableSources) {
                        int i = getTabIndex(source);
                        mTabData[i] = null;
                        mTabIsLoading[i] = false;
                        mTabNextPage[i] = null;
                        mTabError[i] = null;

                        updateFragUI(source);
                    }

                    mSearchNum++; // increment num so we can ignore any pending requests
                    // TODO cancel queries

                }
            });

            mSearchStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check if no view has focus:

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
    private int mSearchNum = 0;
    private MusicLibType mLibType = null;

    // TODO this concept will expand to inlcude the storage for the actual data we load,
    // so when 2 have 3+ frags in the tabs, we can search on all 3, and not worry about data loss or null fragments
    /** The suffix to add to end of tab title, "" means nothing, Set back to "" when a tab is selected*/
    private String[] mTabTitleSuffix = new String[]{"",""};


    private static final Source[] SearchableSources = new Source[] {Source.Spotify, Source.Soundcloud};
    private Boolean[] mTabIsLoading = new Boolean[]{false, false};
    private SearchResultFragment.SearchResultNextPage[] mTabNextPage = new SearchResultFragment.SearchResultNextPage[]{null, null};
    private ArrayList[] mTabData = new ArrayList[]{null, null};
    private String[] mTabError = new String[]{null, null};




    private void doSearch() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        String query = "" + mSearchText.getText();
        if (query == null || query.length() <= 0) {
            return;
        }
        if (!mSearchOpened) {
            mSearchOpened = true;
            mSearchCancel.setVisibility(View.VISIBLE);
        }

        mSearchNum++;

        MusicLibType newSearchType;

        if (mCheckBoxAlbums.isChecked()) {
            newSearchType = MusicLibType.Album;
        } else if (mCheckBoxPlaylist.isChecked()) {
            newSearchType = MusicLibType.Playlist;
        } else if (mCheckBoxArtist.isChecked()) {
            newSearchType = MusicLibType.Artist;
        } else {
            newSearchType = MusicLibType.Song;
        }



        for (Source source : SearchableSources) {
            int i = getTabIndex(source);
            mTabData[i] = null;
            mTabIsLoading[i] = false;
            mTabNextPage[i] = null;
            mTabError[i] = null;
        }

        if (mLibType != newSearchType) {
            mLibType = newSearchType;
            Log.i(TAG, "Switching type of result fragment");
            // so we need a new adapter:
            mViewPagerAdapter.notifyDataSetChanged();
            // notify plus overriding getItemPosition in the adapter allows us to change the fragments
            // this often times blocks UI thread until it's done, resulting in the code below (calls to search, etc.)
            // to be AFTER they switch, so we have to null everything first
        }


        for (Source source : SearchableSources) {
            int i = getTabIndex(source);
            mTabData[i] = null;
            if (source.isPlaybackAuthed() && !(source == Source.Soundcloud && mLibType == MusicLibType.Album)) {
                // TODO add check for SoundCloud albums here
                mTabIsLoading[i] = true;
                setTabSuffix(source, "...");
                updateFragUI(source);

                searchApi(source, mLibType, query, mSearchNum, 0, 12);
            }


        }

    }

    private void startAniCloseSearch() {
        if (mSearchOpened) {
            mSearchOpened = false;
            mSearchCancel.setVisibility(View.GONE);
        }
    }




    private void searchApi(final Source source, final MusicLibType libType, final String query, final int hash, final int offset, final int limit) {
        switch (source) {
            case Spotify:
                searchSpotifyApi(libType, query, hash, offset, limit);
                break;
            case Soundcloud:
                searchSoundCloudApi(libType, query, hash, offset, limit);
                break;
        }
    }



    /** Search through Soundcloud Api using some crazy type casting to keep code as dry as possible.
     * */
    private void searchSoundCloudApi(final MusicLibType libType, final String query, final int hash, final int offset, final int limit) {
        // Breaks with spaces.
        String q = query.replace(" ", "-");
        // limit max is 200, default is 10


        Map<String, String> options = new HashMap<>();
        options.put(SCRetrofitService.CLIENT_ID, SoundCloudApi.CLIENT_ID);
        options.put(SCRetrofitService.OFFSET, ""+offset);
        options.put(SCRetrofitService.LIMIT, ""+limit);
        options.put(SCRetrofitService.QUERY, query);
        options.put(SCRetrofitService.PAGINATE, ""+1);




        Call<JR> cc;
        switch (libType) {
            case Song:
                cc = (Call<JR>) SoundCloudApi.getApiService().searchTracks(options);
                break;
            case Artist:
                cc = (Call<JR>) SoundCloudApi.getApiService().searchUsers(options);
                break;
            case Playlist:
                options.put(SCRetrofitService.COMPACT, "compact");
                cc = (Call<JR>) SoundCloudApi.getApiService().searchPlaylists(options);
                break;
            case Album: // Albums do not exit on SoundCloud, so this response is ignored.
            default:
                return;

        }
        // crazy casting time:
        cc.enqueue(new retrofit2.Callback<JR>() {

            @Override
            public void onResponse(Call<JR> call, retrofit2.Response<JR> response) {
                if (hash != mSearchNum) return;
                if (response.code() != 200) {
                    Log.e(TAG, "Server Error: " + response.raw());
                    searchApiError(hash, "Server Error", Source.Soundcloud);
                    return;
                }
                // TODO for production, add try catch in case some errors happen with the 3rd party's side.  404 errors still result in onResponse getting called

                ArrayList jrList = new ArrayList<>();
                switch (libType) {
                    case Song:
                        jrList =  new ArrayList<SngItem>();
                        for (SoundCloudApi.TrackJson t : ((SoundCloudApi.PagedTrackJson) response.body()).collection) {
//                            if (t.streamable) {
                            // To ignore unsearchable results, we need to tally these so offset for NextPage doesnt cause a few duplicates
                            // and that we also dont
                            ((ArrayList<SngItem>) jrList).add(new SngItem(new Sng(t), SngItem.Type.NotInQueue));
                        }
                        break;

                    case Artist:
                        jrList =  new ArrayList<Artst>();
                        for (SoundCloudApi.UserJson t : ((SoundCloudApi.PagedUserJson) response.body()).collection) {
                            ((ArrayList<Artst>) jrList).add(new Artst(t));
                        }
                        break;

                    case Playlist:
                        jrList =  new ArrayList<Playlst>();
                        for (SoundCloudApi.PlaylistJson t : ((SoundCloudApi.PagedPlaylistJson) response.body()).collection) {
                            ((ArrayList<Playlst>) jrList).add(new Playlst(t));
                        }
                        break;
                }


                searchApiGot(Source.Soundcloud, libType, query, hash, offset, limit, jrList);
            }

            @Override
            public void onFailure(Call<JR> call, Throwable t) {
                Log.e(TAG, "Server Error: " + t);
                searchApiError(hash, "Server Error", Source.Soundcloud);
            }

        });
    }

    private void searchSpotifyApi(final MusicLibType libType, final String query, final int hash, final int offset, final int limit) {
        Log.i("createSearchResults", "Search Query: " + query);

        // TODO: since Spotify is retrofit1, didn't bother keeping code super dry here with casting...

        Map<String, Object> options = new HashMap<>();
        options.put(SCRetrofitService.OFFSET, ""+offset);
        options.put(SCRetrofitService.LIMIT, ""+limit);
        ////////

        switch (libType) {
            case Song:
                SpotifyApi.getTempApi().searchTracks(query, options, new Callback<TracksPager>() {
                    @Override
                    public void success(final TracksPager trackspager, Response response) {
                        if (hash != mSearchNum) return;

                        ArrayList<SngItem> list = new ArrayList<>();
                        for (Track t : trackspager.tracks.items) {
                            list.add(new SngItem(new Sng(t), SngItem.Type.NotInQueue));
                        }

                        searchApiGot(Source.Spotify, libType, query, hash, offset, limit, list);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "Server Error: " + error);
                        searchApiError(hash, "Server Error", Source.Spotify);
                    }

                });

                break;

            case Album:
                // API search albums returns albumssimple rather than Album, which doesnt contain Artist.  So
                // to display artist, we would need to do a get on each ablum separately
//                SpotifyApi.getTempApi().searchAlbums(query, options, new Callback<AlbumsPager>() {
                return;

            case Artist:
                SpotifyApi.getTempApi().searchArtists(query, options, new Callback<ArtistsPager>() {
                    @Override
                    public void success(final ArtistsPager aaa, Response response) {
                        if (hash != mSearchNum) return;

                        ArrayList<Artst> list = new ArrayList<>();
                        for (Artist t : aaa.artists.items) {
                            list.add(new Artst(t));
                        }

                        searchApiGot(Source.Spotify, libType, query, hash, offset, limit, list);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "Server Error: " + error);
                        searchApiError(hash, "Server Error", Source.Spotify);
                    }

                });
                break;

            case Playlist:

                SpotifyApi.getTempApi().searchPlaylists(query, options, new Callback<PlaylistsPager>() {
                    @Override
                    public void success(final PlaylistsPager playlistsPager, Response response) {
                        if (hash != mSearchNum) return;

                        ArrayList<Playlst> list = new ArrayList<>();
                        for (PlaylistSimple t : playlistsPager.playlists.items) {
                            list.add(new Playlst(t));
                        }

                        searchApiGot(Source.Spotify, libType, query, hash, offset, limit, list);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "Server Error: " + error);
                        searchApiError(hash, "Server Error", Source.Spotify);
                    }

                });

                break;

        }
    }

    private void searchApiGot(final Source source, final MusicLibType libType, final String query, final int hash, final int offset, final int limit, ArrayList list) {
        final int i = getTabIndex(source);
        if (mTabData[i] != null) {
            mTabData[i].addAll(list);
        } else {
            mTabData[i] = list;
        }
        mTabError[i] = null;
        mTabIsLoading[i] = false;
        if (list.size() >= limit) {
            // if we got all limit# items, then there is probably more loadable.
            mTabNextPage[i] = new SearchResultFragment.SearchResultNextPage() {
                @Override
                public void requestNextPage() {
                    // increment offset
                    mTabIsLoading[i] = true;
                    mTabNextPage[i] = null;
                    updateFragUI(source);

                    searchApi(source, libType, query, hash, offset + limit, limit);
                }
            };
        } else {
            mTabNextPage[i] = null;
        }
        if (list.size() > 0) {
            setTabSuffix(source, list.size() + "+");
        } else {
            setTabSuffix(source, "0");
        }
        updateFragUI(source);
    }



    private void searchApiError(int hash, String Error, Source source) {
        if (hash != mSearchNum) return;
        setTabSuffix(source, "");

        int i = getTabIndex(source);
        mTabData[i] = null;
        mTabError[i] = "Server Error!";
        mTabIsLoading[i] = false;
        mTabNextPage[i] = null;

        updateFragUI(source);
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
            SearchResultFragment frag = new SearchResultFragment();

            Bundle args = new Bundle();
            Source source;
            if (i == 0) {
                source = Source.Spotify;
            } else {
                source = Source.Soundcloud;
            }
            args.putString(SearchResultFragment.TAG_SOURCE_PREFIX, source.prefix);
            args.putInt(SearchResultFragment.TAG_LIB_TYPE_ORDINAL, mLibType.ordinal());

            frag.setArguments(args);

            // TODO, here read all the data into the frag when we store it in this activity instead
            // Setting here won't work
            // NO, dont do that here, we havent' called onCreateView yet... lets have the frag call us!
//            Log.e(TAG, "at getItem:  mTabIsLoading[getTabIndex(source): " + mTabIsLoading[getTabIndex(source)]);
//            if (mTabIsLoading[getTabIndex(source)]) {
//                frag.setResultNewQuery();
//            }
            return frag;
        }

        @Override
        public long getItemId(int position) {
            // need to mark items with it's searchtype, so if we switch, the adapter knows it's old
            // we dont lookup the item, since this is only assigned at items, creation, and when we want to check if we are to
            // create a new item, it checks if an item with this itemid exists.  To switch search Types, we need to invalidate
            // all ids that were created by using the universal search type in the itemid
            return super.getItemId(position) + (mLibType.ordinal()*100);
        }

        @Override
        public int getItemPosition(Object object) {
            SearchResultFragment frag = (SearchResultFragment) object;

            if (mLibType == frag.getMusicLibType()) {
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
            SearchResultFragment frag = (SearchResultFragment) super.instantiateItem(container, position);
            Log.e(TAG, "instantiateItem: " + frag.getMusicLibType() + ",  " + getItemId(position));
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


        SearchResultFragment getFragmentBySource(Source s) {
            if (s == Source.Spotify) return (SearchResultFragment) instantiatedFrags[0];
            if (s == Source.Soundcloud) return (SearchResultFragment) instantiatedFrags[1];
            return null;
        }

    }


    private int getTabIndex(Source source) {
        if (source == Source.Spotify) {
            return 0;
        } else {
            return 1;
        }
    }

    private void setTabSuffix(Source source, String s) {
        int position = getTabIndex(source);


        if (s == "" || mViewPager.getCurrentItem() != position) {
            if (s != "") s = " (" + s + ")";
            mTabTitleSuffix[position] = s;
            mTabLayout.setupWithViewPager(mViewPager);
        }
    }


    private void updateFragUI(Source s) {
        Log.i(TAG, "updateFragUI " + s);
        SearchResultFragment f = mViewPagerAdapter.getFragmentBySource(s);
        if (f != null) {
            updateFragUI(f);
        } else {
            // Fragment doesn't exist yet.  No UI to update
        }
    }



    /** Call to update when the frag is ready for it (such as recreated)*/
    public void updateFragUI(SearchResultFragment f) {
        int i = getTabIndex(f.getSource());
        Log.w(TAG, "   updateFragUI " + i + ", " + mTabData[i] + ", " + mTabNextPage[i] + ", " + mTabIsLoading[i] + ", "  + mTabError[i]);
        f.setResult(mTabData[i], mTabNextPage[i], mTabIsLoading[i], mTabError[i]);
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
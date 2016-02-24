package com.example.steven.spautify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.steven.spautify.Fragments.MyPlaylistsFragment;
import com.example.steven.spautify.Fragments.MySavedAlbumsFragment;
import com.example.steven.spautify.Fragments.QueueFragment;
import com.example.steven.spautify.Fragments.SongListFragment;
import com.example.steven.spautify.Fragments.SongSearchResultFragment;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.SpotifyWebApiHandler;
import com.example.steven.spautify.musicplayer.WMusicProvider;
import com.example.steven.spautify.musicplayer.WPlayer;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Steven on 7/16/2015.
 */
public class MusicActivity extends BluetoothActivityMOD {
    // TO be leaf in the end

    protected static final String FRAGMENT_ID_RESULT = "search_result_frag";


    private MyViewPageAdapter mViewPagerAdapter;
    private ViewPager mViewPager;

    //private View mLoadingContainer;
    private View mSearchResultContainer;
    //private View mTabAndViewpagerContainer;
    //private RelativeLayout mSearchBar;
    private TextView mSearchText;
    private TextView mDisabledText;
    private ImageView mSearchCancel;
    private ImageView mSearchStart;

    private Button mSettings;
    private Button mCreatePlayer;

    private TabLayout mTabLayout;

    private SongSearchResultFragment mFragResult;

    private static final int ANI_DURATION_MS = 150;


    private MusicPlayerBar mMusicPlayerBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("FriendsActivity", "FriendsActivity onCreate");
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_music);

        // Needed here since we are using our own custom xml file instead of activity_blank_toolbar.xmllbar.xml, which lets the Super class automate the basic inflations.
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);




        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        FragmentManager fm = getFragmentManager();

        mViewPagerAdapter = new MyViewPageAdapter(fm);
        mViewPager.setAdapter(mViewPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);



        // Just create a new one
        mFragResult = new SongSearchResultFragment();

        fm.beginTransaction()
                .replace(R.id.search_result_container, mFragResult, FRAGMENT_ID_RESULT)
                .commit();


        //mLoadingContainer = findViewById(R.id.loading_container);
        //mLoadingContainer.setVisibility(View.GONE);

        mDisabledText = (TextView) findViewById(R.id.disabled_text);
        mSettings = (Button) findViewById(R.id.launch_settings);
        mCreatePlayer = (Button) findViewById(R.id.create_player);

        mSearchResultContainer = findViewById(R.id.search_result_container);
        mSearchText = (TextView) findViewById(R.id.search_text);
        mSearchCancel = (ImageView) findViewById(R.id.cancel_button);
        mSearchStart = (ImageView) findViewById(R.id.search_button);


        mMusicPlayerBar = (MusicPlayerBar) findViewById(R.id.music_player_bar);

        //refreshUI();
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
        if (WPlayer.getState() == WPlayer.State.Off) {
            mSearchResultContainer.setVisibility(View.GONE);
            mSearchText.setVisibility(View.GONE);
            mSearchCancel.setVisibility(View.GONE);

            mDisabledText.setVisibility(View.VISIBLE);
            mDisabledText.setText("Player is off");
            mViewPager.setVisibility(View.GONE);
            mTabLayout.setVisibility(View.GONE);
            mSettings.setVisibility(View.VISIBLE);

            mMusicPlayerBar.setVisibility(View.GONE);


            if (SpotifyWebApiHandler.getAuthState() == WMusicProvider.AuthState.LoggedIn) {
                mCreatePlayer.setVisibility(View.VISIBLE);

                // Then for now, we auto create the player

                mCreatePlayer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WPlayer.createPlayer();
                    }
                });

            } else {
                mCreatePlayer.setVisibility(View.GONE);

            }

            mSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MusicActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
            });



        } else {




            mDisabledText.setVisibility(View.GONE);
            mSettings.setVisibility(View.GONE);
            mCreatePlayer.setVisibility(View.GONE);


            mSearchCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startAniCloseSearch();
                    mSearchText.setText("");
//                if (mhhh != null) {
//                    mhhh.cancel();
//                    mhhh = null;
//                }
                }
            });

            mSearchStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startAniOpenSearch();
                    searchSpotifyApi("" + mSearchText.getText());
                    mFragResult.setResultingLoading();
                }
            });





//            mSearchText.addTextChangedListener(mTextWatcher);

            mSearchText.setVisibility(View.VISIBLE);
            if (mSearchOpened) {
                mViewPager.setVisibility(View.GONE);
                mTabLayout.setVisibility(View.GONE);
                mSearchCancel.setVisibility(View.VISIBLE);
            } else {
                mViewPager.setVisibility(View.VISIBLE);
                mTabLayout.setVisibility(View.VISIBLE);
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

            mViewPager.clearAnimation();
//            mSearchCancel.clearAnimation();

            mSearchResultContainer.setVisibility(View.VISIBLE);
            mSearchCancel.setVisibility(View.VISIBLE);



            int h = mViewPager.getHeight() + mTabLayout.getHeight();

            for (final View v : new View[] { mViewPager, mTabLayout}) {
                v.animate()
                        .translationY(h)
                        .alpha(0.0f)
                        .setDuration(ANI_DURATION_MS)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                if (mSearchOpened) {
                                    Log.e("SSSS", "Finsihed opening, and now setting both views to invisible");
                                    // Check needed since even a cancel still ends up having this be called.
                                    v.setVisibility(View.GONE);
                                }
                            }
                        });

            }



//            if (mSearchCancel.getTranslationX() >= -0.1) {
//                mSearchCancel.setTranslationX(mSearchCancel.getWidth());
//            }
//
//            mSearchCancel.animate()
//                    .translationX(0)
//                    .alpha(1.0f)
//                    .setDuration(ANI_DURATION_MS);
        }

    }

    private void startAniCloseSearch() {
        if (mSearchOpened) {
            mSearchOpened = false;

            mTabLayout.clearAnimation();
            mViewPager.clearAnimation();

//            mSearchCancel.clearAnimation();
            mSearchCancel.setVisibility(View.GONE);

            mTabLayout.setVisibility(View.VISIBLE);
            mViewPager.setVisibility(View.VISIBLE);


            //int h = mViewPager.getHeight() + mTabLayout.getHeight();

            mTabLayout.animate()
                    .translationY(0)
                    .alpha(1.0f)
                    .setDuration(ANI_DURATION_MS);

            mViewPager.animate()
                    .translationY(0)
                    .alpha(1.0f)
                    .setDuration(ANI_DURATION_MS)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            // So even after this is set, a previous animation finishing will call it's old listener (don't know why,)
                            // So we use this boolean check to make sure the correct one gets used.
                            if (!mSearchOpened) {
                                mSearchResultContainer.setVisibility(View.GONE);
//                                mSearchCancel.setVisibility(View.GONE);
                            }

                        }
                    });





//            mSearchCancel.animate()
//                    .translationX(-mSearchCancel.getWidth())
//                    .alpha(0.0f)
//                    .setDuration(ANI_DURATION_MS);

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



    /**
     *
     * Overview/Chat View Pager Stuff
     *
     * FragmentPagerAdapter keeps all frags in mem
     * FragmentStatePagerAdapter may destroy frags not on screen to fre up mem.
     *
     * FragmentStatePagerAdapter causes unavoidable and mysterious bug:
     * 			11-05 16:28:44.298: E/FragmentManager(12126): Fragement no longer exists for key f0: index 1
     * 		when screen is rotated.  (Yes, that fragement typo is from Fragment Manager Source Code, not my typo...)
     */
    private static class MyViewPageAdapter extends FragmentPagerAdapter {
        public MyViewPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:  return new MyPlaylistsFragment();
                case 1:  return new MySavedAlbumsFragment();
                //case 2:  return new QueueFragment();
                default: return new QueueFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public String getPageTitle(int position) {
            switch (position) {
                //case 0:  return "Queue";
                case 0:  return "Playlists";
                case 1:  return "Albums";
                default: return "Queue";
            }
        }

    }



    @NonNull
    @Override
    protected Layout getLayoutType() {
        return Layout.Custom;
    }



    private void searchSpotifyApi(final String query) {
        // calls mFragResult
        // mFragResult.setResultingError("Server Error!");


        Log.i("createSearchResults", "Search Query: " + query);
        SpotifyWebApiHandler.getTempApi().searchTracks(query, new Callback<TracksPager>() {
            @Override
            public void success(final TracksPager trackspager, Response response) {

                ArrayList<SongListFragment.SngItem> songs = new ArrayList<>();
                for (Track t : trackspager.tracks.items) {
                    Log.d("createSearchResults", t.name + "" + ", " + t.uri + ", " + t.album.name);
                    songs.add(new SongListFragment.SngItem(new Sng(t), SongListFragment.SngItem.Type.NotInQueue));
                }

                mFragResult.setResult(songs);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Track failure", error.toString());
                mFragResult.setResultingError("Server Error!");
            }

        });


    }










}
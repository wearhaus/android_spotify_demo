package com.example.steven.spautify;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import com.example.steven.spautify.fragments.MyPlaylistsFragment;
import com.example.steven.spautify.fragments.MySavedAlbumsFragment;
import com.example.steven.spautify.fragments.QueueFragment;
import com.example.steven.spautify.musicplayer.WPlayer;


/**
 * Created by Steven on 7/16/2015.
 */
public class MusicActivity extends NavBarRootActivityMOD {
    // TO be leaf in the end

    private MyViewPageAdapter mViewPagerAdapter;
    private ViewPager mViewPager;

    private ImageButton mSearchButton;
    private Button mCreatePlayer;
    private TextView mDisabledText;

    private TabLayout mTabLayout;

    private MusicPlayerBar mMusicPlayerBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("FriendsActivity", "FriendsActivity onCreate");
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_music);

        // Needed here since we are using our own custom xml file instead of activity_blank_toolbar.xmllbar.xml, which lets the Super class automate the basic inflations.
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        onCreateAfterInflation();

        mSearchButton = (ImageButton) findViewById(R.id.toolbar_search_button);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });




        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        FragmentManager fm = getFragmentManager();

        mViewPagerAdapter = new MyViewPageAdapter(fm);
        mViewPager.setAdapter(mViewPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);

        mDisabledText = (TextView) findViewById(R.id.disabled_text);

        mCreatePlayer = (Button) findViewById(R.id.create_player);


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
        if (WPlayer.getState() == WPlayer.WPlayerState.Off) {

            mDisabledText.setVisibility(View.VISIBLE);
            mDisabledText.setText("Player is off");
            mViewPager.setVisibility(View.GONE);
            mTabLayout.setVisibility(View.GONE);

            mMusicPlayerBar.setVisibility(View.GONE);


            mCreatePlayer.setVisibility(View.VISIBLE);
            mCreatePlayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WPlayer.createPlayer();
                }
            });


            //if (SpotifyApiController.getAuthState() == WMusicProvider.AuthState.LoggedIn) {



        } else {

            mDisabledText.setVisibility(View.GONE);
            mCreatePlayer.setVisibility(View.GONE);
            mViewPager.setVisibility(View.VISIBLE);
            mTabLayout.setVisibility(View.VISIBLE);

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









}
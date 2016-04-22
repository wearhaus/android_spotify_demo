package com.example.steven.spautify;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.Notifier;
import com.example.NotifierSimple;
import com.example.steven.spautify.musicplayer.SpotifyApiController;
import com.example.steven.spautify.musicplayer.WMusicProvider;
import com.example.steven.spautify.musicplayer.WPlayer;

/**
 *
 */
abstract class BluetoothActivityMOD extends AppCompatActivity {
    private static final String TAG = "BluetoothActivity";
    protected static final String FRAGMENT_ID = "ba_fragment";

    private NotifierSimple.ListenerSimple mArcConnStateListener;
    protected boolean mIsResumed = false;

    protected Toolbar mToolbar;

    private MusicPlayerBar mMusicPlayerBar;

    private Notifier.Listener<WPlayer.Notif> mWPSL;


    private void whenNoWPlayer() {
        return; // For now, we don't redirect off anything due to lack fo music player
//        Log.e(TAG, "whenNoWPlayer()");
//
//        Intent intent;
////        if (isLoginActivity()) {
//            intent = new Intent(this, LaunchActivity.class);
////            intent.putExtra(OnboardingActivity.INTENT_EXTRA_GO_TO_MAIN, false);
////        } else {
////            intent = new Intent(this, OnboardingActivity.class);
////            //intent.putExtra(OnboardingActivity.INTENT_EXTRA_GO_TO_MAIN, true);
////        }
//
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        if (!WearhausApp.getArcLink().isArcConnected()) {
//            whenNoWPlayer();
//            finish();
//            return;
//        }

        if (exitWhenNoWPlayer() && WPlayer.getState() == WPlayer.WPlayerState.Off) {
            whenNoWPlayer();
            return;
        }

//        // Check to make sure the user is logged in if we are not LoginActivity.  Rare, but may occur
//        if (!isLoginActivity() && AccountController.getMyUserAsUser() == null) {
//            Util.loge(TAG, " No user logged in.  Redirecting to Login Activity");
//            Intent intent = new Intent(this, LoginActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            finish();
//            return;
//        }

        if (getLayoutType().isShell) {

            switch (getLayoutType()) {
                case Shell_NoToolbar:
                    // Note: Not used yet
                    setContentView(R.layout.activity_blank_no_toolbar);
                    break;

                case Shell_WithToolbarEditProfile:
                    setContentView(R.layout.activity_blank_toolbar_editprofile);
                    break;

                case Shell_WithToolbarEmptyLeaf:
                    setContentView(R.layout.activity_blank_toolbar);
                    break;

                case Shell_WithToolbarEmptyLeafAndPlayerBar:
                    setContentView(R.layout.activity_blank_toolbar_player_bar);
                    mMusicPlayerBar = (MusicPlayerBar) findViewById(R.id.music_player_bar);
                    break;

                case Shell_WithToolbarNavBar:
                    setContentView(R.layout.activity_navbar);
                    break;


            }

            if (getLayoutType() != Layout.Shell_NoToolbar) {
                mToolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(mToolbar);
            }

            Fragment potentialFrag = getFragmentForShellActivity();

            FragmentManager fm = getFragmentManager();
            Fragment f = fm.findFragmentByTag(FRAGMENT_ID);
            if (f == null) {
                f = potentialFrag;
                fm.beginTransaction()
                        .replace(R.id.main_fragment_layout, f, FRAGMENT_ID)
                        .commit();
            }


        }





    }



    @Override
    protected void onResume() {
        super.onResume();
        mIsResumed = true;

        //Util.logd("BluetoothActivity", "onResume()");

        if (exitWhenNoWPlayer() && WPlayer.getState() == WPlayer.WPlayerState.Off) {
            whenNoWPlayer();
            return;
        }

//        if (!WearhausApp.getArcLink().isArcConnected()) {
//            whenNoArc();
//            // Needed in case app is paused, bluetooth turned off, then app brought back to front
//            return;
//        }

        // when paused, we remove listeners, and add them back here.
//        if (mArcConnStateListener == null) {
//            mArcConnStateListener = new NotifierSimple.ListenerSimple() {
//
//                @Override
//                public void onChange() {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            onArcConnectionStateChanged(WearhausApp.getArcLink().getArcState());
//                        }
//                    });
//                }
//            };
//            WearhausApp.getArcLink().getArcStateNotifier().registerListener(mArcConnStateListener);
//        }




        if (careForWPlayerState()&& mWPSL == null) {
            mWPSL = new WPlayerStateListener();
            WPlayer.getNotifier().registerListener(mWPSL);
        }

        refreshMusicPlayerState();

    }

    protected void refreshMusicPlayerState() {
        if (mMusicPlayerBar != null) {
            mMusicPlayerBar.onActivityResumed();

            if (SpotifyApiController.getAuthState() != WMusicProvider.AuthState.LoggedIn || WPlayer.getCurrentSng() == null) {
                mMusicPlayerBar.setVisibility(View.GONE);
            } else {
                mMusicPlayerBar.setVisibility(View.VISIBLE);
            }
        }

    }


    @Override
    protected void onPause() {
        mIsResumed = false;
//        if (mArcConnStateListener != null) {
//            WearhausApp.getArcLink().getArcStateNotifier().unregisterListener(mArcConnStateListener);
//            mArcConnStateListener = null;
//        }


        if (mWPSL != null) {
            WPlayer.getNotifier().unregisterListener(mWPSL);
            mWPSL = null;
        }

        if (mMusicPlayerBar != null) {
            mMusicPlayerBar.onActivityPaused();
        }



        super.onPause();
    }



    /**
     * From Root means that this is called from NearbyStationsActivity, so the caller will be left stopped in the background.
     *
     * The class given must extend BluetoothActivity
     */
    protected <T extends LeafActivity> void launchLeaf(Class<T> newActivityClass) {
        Intent intent = new Intent(this, newActivityClass);
        startActivity(intent);
    }



    private class WPlayerStateListener implements Notifier.Listener<WPlayer.Notif> {

        @Override
        public void onChange(WPlayer.Notif type) {
            //onSyncStateChanged();
            onWPlayerChanged();

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    onWPlayerChangedUIThread();
                    //onSyncStateChangedUIThread();
                }
            });

            if (exitWhenNoWPlayer() && WPlayer.getState() == WPlayer.WPlayerState.Off) {
                whenNoWPlayer();
            }
        }
    }



    protected void onWPlayerChanged(){ }
    protected void onWPlayerChangedUIThread(){ }


    /**
     * ONLY implement if Layout type is a shell.
     * Return the fragment that this activity will create.  This will allow the implementer Activity
     * to not handle framgents or UI on onCreate.  But if onCreatePostInflated() function exists, that still
     * must be called at the end of onCreate().
     * Return value can be set to null so this does nothing for any Activity that wants more control
     * over Fragments.
     */
    protected Fragment getFragmentForShellActivity() {
        return null;
    }

    protected abstract @NonNull Layout getLayoutType();



    protected enum Layout {
        Shell_WithToolbarNavBar         (true, R.layout.activity_navbar),
        Shell_WithToolbarEmptyLeaf      (true, R.layout.activity_blank_toolbar),
        Shell_WithToolbarEmptyLeafAndPlayerBar      (true, R.layout.activity_blank_toolbar),
        Shell_WithToolbarEditProfile    (true, R.layout.activity_blank_toolbar_editprofile),
        //Shell_WithToolbarSearch    (true, R.layout.activity_blank_toolbar_editprofile),
        Shell_NoToolbar                 (true, R.layout.activity_blank_no_toolbar),

        Custom(false, 0),
        ;

        Layout(boolean b, int r) {
            isShell = b;
            resid = r;
        }
        boolean isShell;
        int resid;

    }


    /** Default false, since fragments usually only care.  But this is here
     * for the few activities that handle this.*/
    protected boolean careForWPlayerState() { return false; }


    /** Default false, since fragments usually only care.  But this is here
     * for the few activities that handle this.
     * TODO this may be false for everything if things continue this way...*/
    protected boolean exitWhenNoWPlayer() { return false; }

}

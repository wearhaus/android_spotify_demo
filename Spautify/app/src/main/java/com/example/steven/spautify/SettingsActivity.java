package com.example.steven.spautify;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.steven.spautify.fragments.SettingsFragment;
import com.example.steven.spautify.musicplayer.SpotifyApi;
import com.example.steven.spautify.musicplayer.WMusicProvider;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class SettingsActivity extends LeafActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onCreateAfterInflation();
    }

    @Override
    protected Fragment getFragmentForShellActivity() {
        return new SettingsFragment();
    }


    @NonNull
    @Override
    protected BluetoothActivityMOD.Layout getLayoutType() {
        return Layout.Shell_WithToolbarEmptyLeaf;
    }

    @Override
    protected boolean isParentDefinedInManifest() {
        return false;
    }






    // Request code that will be used to verify if the result comes from correct activity.  Can be any integer
    private static final int REQUEST_CODE = 1163;


    public void onSpotifyAuth(boolean forceDialog) {


        AuthenticationClient.openLoginActivity(SettingsActivity.this, REQUEST_CODE, SpotifyApi.startAuth(forceDialog));

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d("MainActivity", "onActivityResult: " + requestCode);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            Log.d("MainActivity", "onActivityResult response:" + response.getType());
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                //mTextView.setText("Logged in");

                SpotifyApi.onGetAccessToken(response.getAccessToken(), new SpotifyApi.AuthCallback() {
                    @Override
                    public void callback(WMusicProvider.AuthState authState) {
                        refreshAuthUI();
                    }
                });

                //WPlayer.createPlayer();
                //WPlayer.getNotifier().registerListener(mPlaybackListener);

                refreshAuthUI();


            } else if (response.getType() == AuthenticationResponse.Type.ERROR) {
                Log.e("MainActivity", " error auth" + response.getError());

                refreshAuthUI();

            }
        }
    }





    private void refreshAuthUI() {
        // stragne we aren't using listeners, but this, I suppose, is simpler and has same general behavior anyways.

        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentByTag(FRAGMENT_ID);
        if (f != null) {
            ((SettingsFragment) f).refreshAuthUI();
        }

    }



}

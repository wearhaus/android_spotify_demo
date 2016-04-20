package com.example.steven.spautify;

import android.app.Application;

import com.example.steven.spautify.musicplayer.SoundCloudApiHandler;
import com.example.steven.spautify.musicplayer.SpotifyWebApiHandler;
import com.example.steven.spautify.musicplayer.WPlayer;

/**
 * Created by Steven on 12/18/2015.
 */
public class AppApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();



        /*
        try {
            Signature[] sigs = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES).signatures;
            for (Signature sig : sigs) {
                Log.i("MyApp", "Signature hashcode : " + sig.hashCode());
            }
        } catch (Exception e) {

        }
        */

        WPlayer.init(getApplicationContext());

        //SpotifyController.init(getApplicationContext());
        SpotifyWebApiHandler.init();

        SoundCloudApiHandler.init();


    }

}

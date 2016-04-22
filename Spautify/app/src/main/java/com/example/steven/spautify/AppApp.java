package com.example.steven.spautify;

import android.app.Application;

import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.SoundCloudApiController;
import com.example.steven.spautify.musicplayer.SpotifyApiController;
import com.example.steven.spautify.musicplayer.WPlayer;

/**
 * Created by Steven on 12/18/2015.
 */
public class AppApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        //LeakCanary.install(this);



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
        SpotifyApiController.init();

        SoundCloudApiController.init();

        Sng.init();


    }

}

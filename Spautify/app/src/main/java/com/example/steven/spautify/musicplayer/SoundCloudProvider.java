package com.example.steven.spautify.musicplayer;

import android.content.Context;

/**
 * Created by Steven on 4/19/2016.
 */
public class SoundCloudProvider extends StreamUrlProvider {

    /**
     * @param c must be the Application Context
     *          //@param wp must be the WPlayer that creates and holds this
     */
    public SoundCloudProvider(Context c) {
        super(c);
    }

    @Override
    public String getStreamUrl(Sng sng) {
        return sng.soundCloudJson.stream_url + "?client_id=" + SoundCloudApi.CLIENT_ID;
    }
}

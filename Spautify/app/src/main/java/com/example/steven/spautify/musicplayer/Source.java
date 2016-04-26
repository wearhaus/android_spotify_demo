package com.example.steven.spautify.musicplayer;

import com.example.steven.spautify.R;

/**
 * Created by Steven on 4/21/2016.
 */
public enum Source {
    Spotify(SpotifyProvider.class, "sp", R.drawable.spotify_icon),
    Blank(BlankProvider.class, "bl", 0),
    Soundcloud(SoundCloudProvider.class, "sc", R.drawable.soundcloud_icon_small),
    iTunes(null, "it", 0);


    Class providerClass;
    String prefix;
    public int sourceSplashRes;
    Source(Class pc, String p, int ss) {
        providerClass = pc;
        prefix = p;
        sourceSplashRes = ss;
    }

    /** Valid for Songs, Playlists, and Albums */
    public static Source getSource(String id) {
        if (id != null) {
            for (Source s : Source.values()) {
                if (id.startsWith(s.prefix)) return s;
            }
        }
        return null;
    }

    /** Valid for Songs, Playlists, and Albums */
    public static String get3rdPartyId(String id) {
        if (id == null || id.length() <= 2) return null;
        return id.substring(2);
    }
}
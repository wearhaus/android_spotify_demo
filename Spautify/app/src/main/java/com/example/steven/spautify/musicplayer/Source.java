package com.example.steven.spautify.musicplayer;

import com.example.steven.spautify.R;

/**
 * Created by Steven on 4/21/2016.
 */
public enum Source {
    Spotify(SpotifyProvider.class, "sp", R.drawable.spotify_icon),
    Blank(BlankProvider.class, "bl", 0),
    /**Represents something unable to be loaded or is forbidden.*/
    Null(null, "nn", 0),
    Soundcloud(SoundCloudProvider.class, "sc", R.drawable.soundcloud_icon_small),
    iTunes(null, "it", 0);


    Class providerClass;
    public String prefix;
    public int sourceSplashRes;
    Source(Class pc, String p, int ss) {
        providerClass = pc;
        prefix = p;
        sourceSplashRes = ss;
    }

//    public WMusicProvider.AuthState getSourcePlaybackAuthState() {
    public boolean isPlaybackAuthed() {
        return isPlaybackAuthedErrorString() == null;
    }

    /** Null means good, String means not good as well as human reason why. */
    public String isPlaybackAuthedErrorString() {
        if (this == Spotify) {
            if (SpotifyApi.getAuthState() == WMusicProvider.AuthState.LoggedIn) {
                return null;
            } else {
                return "Not logged into Spotify";
            }
        }
        if (this == Soundcloud) return null; // do need for authentication for playback
        return ""; // source not yet supported
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

    public static Source getSourceByPrefix(String prefix) {
        for (Source s : values()) {
            if (s.prefix.equals(prefix)) return s;
        }
        return null;
    }
}
package com.example.steven.spautify.musicplayer;

import android.util.LruCache;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.UserPublic;

/**
 * Created by Steven on 2/10/2016.
 */
public class Playlst {

    public Source source;
    /** Our assigned id */
    public String playlstId;
    public String name;
    public String creatorName;
    public String artworkUrl;

    // sometimes not visible

    public String spotifyId;

    public PlaylistSimple spotifyObject;
    /**TODO this is dangerous since this can be very very large*/
    public SoundCloudApi.PlaylistJson soundcloudObject;


    public Playlst(PlaylistSimple p) {
        source = Source.Spotify;
        playlstId = source.prefix + p.id;
        spotifyId = p.id;
        name = p.name;
        creatorName = "";
        spotifyObject = p;
        // some fields like owner may be null

        try {
            artworkUrl = p.images.get(0).url;
        } catch (NullPointerException e) {}
    }


    public Playlst(SoundCloudApi.PlaylistJson p) {
        source = Source.Soundcloud;
        playlstId = source.prefix + p.id;

        name = p.title;
        creatorName = p.user != null ? p.user.username : "";
        artworkUrl = p.artwork_url;

        soundcloudObject = p;
    }




    public static LruCache<String, Playlst> mPlaylstCache = new LruCache<>(50); // Temp, may not be the best way to handle





}

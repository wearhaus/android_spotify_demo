package com.example.steven.spautify.musicplayer;

import android.util.Log;
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


    /** Will probably be small resolution, such as 100x100, or even smaller*/
    public String artworkUrl;
    /** If a higher res is available, this will be filled out, otherwise, it'll mirror artworkUrlHighRes*/
    public String artworkUrlHighRes;

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

        if (p.images != null && !p.images.isEmpty()) {
            // as of now, order is largest to smallest
            artworkUrl = p.images.get(p.images.size()-1).url;
            artworkUrlHighRes = p.images.get(0).url;
        }
    }


    public Playlst(SoundCloudApi.PlaylistJson p) {
        source = Source.Soundcloud;
        playlstId = source.prefix + p.id;

        name = p.title;
        creatorName = p.user != null ? p.user.username : "";

        artworkUrl = p.artwork_url;
        if (artworkUrl != null) {
            // default is large, which is 100x100
            artworkUrlHighRes = artworkUrl.replace("large.jpg", "t500x500.jpg");
        } else {
            artworkUrlHighRes = artworkUrl;
        }

        // Note: it would be an error to have source marked but the corresponding object null
        soundcloudObject = p;
    }




    public static LruCache<String, Playlst> mPlaylstCache = new LruCache<>(50); // Temp, may not be the best way to handle





}

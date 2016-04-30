package com.example.steven.spautify.musicplayer;

import android.util.LruCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPublic;

/**
 * Created by Steven on 2/11/2016.
 */
public class Artst {

    private static final String TAG = "Artst";

    // Fields always present
    /** our custom defined artstId field, separating out sources.  Will have 2 character prefix defining the source */
    public String artstId;
    public Source source;
    public String name;
    /** may be null TODO for bot spotify and soundcloud the field is not loaded in the simple versions*/
    public String artworkUrl;

    // Fields available depending on source type
    public String spotifyId; // https://developer.spotify.com/web-api/user-guide/#spotify-uris-and-ids
    public ArtistSimple spotifyArtistSimple;
    public Artist spotifyArtistFull;

    public int soundCloudId;
    public SoundCloudApi.UserJson soundCloudJson;




    public Artst(ArtistSimple as) {
        spotifyId = as.id;
        source = Source.Spotify;
        artstId = source.prefix + as.id;

        name = as.name;
        artworkUrl = null;

        spotifyArtistSimple = as;
    }

    public Artst(Artist as) {
        spotifyId = as.id;
        source = Source.Spotify;
        artstId = source.prefix + as.id;
        name = as.name;

        if (as.images != null && !as.images.isEmpty()) {
            artworkUrl = as.images.get(0).url;
        }

        spotifyArtistFull = as;
    }

    public Artst(SoundCloudApi.UserJson t) {
        source = Source.Soundcloud;
        soundCloudId = t.id;
        artstId = source.prefix + t.id;

        name = t.username;
        artworkUrl = t.avatar_url;

        soundCloudJson = t;
    }


    /** TODO temp*/
    public static LruCache<String, Artst> mArtstCache = new LruCache<>(50);




}

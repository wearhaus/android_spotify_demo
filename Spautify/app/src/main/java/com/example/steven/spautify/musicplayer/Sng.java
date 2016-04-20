package com.example.steven.spautify.musicplayer;


import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Steven on 1/8/2016.
 *
 * Temp song that gets queued.  To be replaced with a different class in the future
 */
public class Sng {
    // Fields always present
    public Source source;
    public String name;
    public String artistPrimaryName;
    public String albumName;
    public String artworkUrl;
    public int durationInMs;

    // Fields available depending on source type
    public String spotifyUri;
    public String spotifyAlbumId;
    public ArrayList<String> spotifyArtistIds;
    //public Image spotifyAlbumImage;

    public int soundCloudId;
    public SoundCloudApiHandler.TrackJson soundCloudJson;




    public Sng(Track t) {
        gotTrackInit(t);
    }

    public Sng(SoundCloudApiHandler.TrackJson t) {
        source = Source.Soundcloud;
        soundCloudId = t.id;

        name = t.title;
        artistPrimaryName = t.user.username;
        albumName = null;
        durationInMs = t.duration;
        artworkUrl = t.artwork_url;

        soundCloudJson = t;
    }


    private void gotTrackInit(Track t) {
        spotifyUri = t.uri;
        source = Source.Spotify;
        name = t.name;
        artistPrimaryName = t.artists.get(0).name;
        albumName = t.album.name;
        durationInMs = (int) t.duration_ms;
        artworkUrl = t.album.images.get(0).url;

        spotifyAlbumId = t.album.id;
        spotifyArtistIds = new ArrayList<>();
        for (ArtistSimple art : t.artists) {
            spotifyArtistIds.add(art.id);
        }

    }

    @Override
    public String toString() {
        Track t = SpotifyWebApiHandler.getTrackOnlyIfCached(spotifyUri);
        if (t != null) return t.name;
        return spotifyUri;
    }

    public enum Source {
        Spotify(SpotifyProvider.class),
        Blank(BlankProvider.class),
        Soundcloud(SoundCloudProvider.class),
        iTunes(null);


        Class providerClass;
        Source(Class pc) {
            providerClass = pc;

        }
    }
}

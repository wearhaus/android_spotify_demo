package com.example.steven.spautify.musicplayer;


import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Steven on 1/8/2016.
 *
 * Temp song that gets queued.  To be replaced with a different class in the future
 */
public class Sng {
    public String spotifyUri;

    public Source source;
    public String name;
    public String artistPrimary;
    public String album_name;
    public String album_id;
    public Image album_image;


    public int durationInMs;


    /*public Sng(String spotifyUri) {
        this.spotifyUri = spotifyUri;
        source = Source.Spotify;
        Track t = SpotifyWebApiHandler.getTrackOnlyIfCached(spotifyUri);
        if (t != null) {
            gotTrackInit(t);
        }
    }*/


    public Sng(Track t) {
        gotTrackInit(t);
    }


    private void gotTrackInit(Track t) {
        spotifyUri = t.uri;
        source = Source.Spotify;
        name = t.name;
        artistPrimary = t.artists.get(0).name;
        album_name = t.album.name;
        album_id = t.album.id;
        durationInMs = (int) t.duration_ms;
        album_image = t.album.images.get(0);
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
        Soundcloud(null),
        iTunes(null);


        Class providerClass;
        Source(Class pc) {
            providerClass = pc;

        }
    }
}

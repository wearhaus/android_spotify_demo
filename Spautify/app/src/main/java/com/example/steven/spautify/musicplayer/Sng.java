package com.example.steven.spautify.musicplayer;


import com.example.steven.spautify.R;

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

    /** Equality check that checks for ids of songs.
     *
     * TODO ultimately should use our assigned ids, which would probably be "sp" + spotifyUri, "sc" + soundCloudId, etc.
     * That would also mean that the same song on different services remain as separate entities (probably for the best anyways)
     */
    public boolean equalsId(Sng other) {
        if (other == null) return false;
        if (source == other.source) {
            if (source == Source.Spotify) {
                return spotifyUri == other.spotifyUri;
            } else if (source == Source.Spotify) {
                return soundCloudId == other.soundCloudId;
            }
        }
        return false;

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

    public int getSourceSplashImageRes() {
        if (source == Sng.Source.Spotify) {
            return R.drawable.spotify_icon;
        } else if (source == Sng.Source.Soundcloud) {
            return R.drawable.soundcloud_icon_small;
        } else {
            return 0;
        }
    }

    public String getFormattedArtistAlbumString() {
        String s = getAlbumName();
        if (s != null) return artistPrimaryName + " / " + getAlbumName();
        return artistPrimaryName;

    }


    public String getAlbumName() {
        if (albumName != null && !albumName.isEmpty()) {
            return albumName;
        } else if (source == Sng.Source.Spotify) {
            return "from Spotify";
        } else if (source == Sng.Source.Soundcloud) {
            return "from SoundCloud";
        }
        return null;
    }

    @Override
    public String toString() {
        if (source == Source.Spotify) {
            Track t = SpotifyWebApiHandler.getTrackOnlyIfCached(spotifyUri);
            if (t != null) return t.name;
            return spotifyUri;
        } else if (source == Source.Soundcloud) {
            return ""+soundCloudId;
        }
        return "what??";
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

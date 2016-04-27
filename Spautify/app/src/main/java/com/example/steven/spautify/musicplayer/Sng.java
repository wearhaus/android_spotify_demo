package com.example.steven.spautify.musicplayer;


import android.util.Log;
import android.util.LruCache;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Steven on 1/8/2016.
 *
 * Temp song that gets queued.  To be replaced with a different class in the future
 */
public class Sng {
    private static final String TAG = "Sng";

    // Fields always present
    /** our custom defined sngId field, separating out sources.  Will have 2 character prefix defining the source */
    public String sngId;
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
    public SoundCloudApi.TrackJson soundCloudJson;




    public Sng(Track t) {
        spotifyUri = t.uri;
        source = Source.Spotify;
        sngId = source.prefix + t.id;


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

    public Sng(SoundCloudApi.TrackJson t) {
        source = Source.Soundcloud;
        soundCloudId = t.id;
        sngId = source.prefix + t.id;

        name = t.title;
        artistPrimaryName = t.user.username;
        albumName = null;
        durationInMs = t.duration;
        artworkUrl = t.artwork_url;

        soundCloudJson = t;
    }

    public Sng() {
        source = Source.Null;
        sngId = source.prefix;
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
            } else if (source == Source.Soundcloud) {
                return soundCloudId == other.soundCloudId;
            }
        }
        return false;

    }




    public String getFormattedArtistAlbumString() {
        String s = getAlbumName();
        if (s != null) return artistPrimaryName + " / " + getAlbumName();
        return artistPrimaryName;

    }


    public String getAlbumName() {
        if (albumName != null && !albumName.isEmpty()) {
            return albumName;
        } else if (source == Source.Spotify) {
            return "from Spotify";
        } else if (source == Source.Soundcloud) {
            return "from SoundCloud";
        }
        return null;
    }

//    @Override
//    public String toString() {
//        if (source == Source.Spotify) {
//            Track t = SpotifyApiController.getTrackOnlyIfCached(spotifyUri);
//            if (t != null) return t.name;
//            return spotifyUri;
//        } else if (source == Source.Soundcloud) {
//            return ""+soundCloudId;
//        }
//        return "what??";
//    }




    /*
        Static stuff
     */



    static LruCache<String, Sng> mSngCache;

    public static void init() {
        mSngCache = new LruCache<>(300); // 150 track entries max limit.
    }


    public interface GetSongListener {
        /** Note: sng should also have been put into Sng cache.  TODO */
        public void gotSong(Sng sng);
        public void failed(String songId);

    }

    /** Gets the sng async, saves into cache, and calls the provided listener with
     * the results.  This abstracts out which source the Sng is from. */
    public static void cacheSng(Sng sng) {
        mSngCache.put(sng.sngId, sng);

    }

    public static void getSng(final String songId, final GetSongListener l) {


        if (mSngCache.get(songId) != null) {
            if (l != null) l.gotSong(mSngCache.get(songId));
            return;
        } else {
            Source s = Source.getSource(songId);
            if (s == Source.Spotify) {
                SpotifyApi.getTrackBySongId(songId, l);

                return;
            } else if (s == Source.Soundcloud) {


                SoundCloudApi.getTrackByIdOnline(Integer.parseInt(Source.get3rdPartyId(songId)), new SoundCloudApi.GotItem<SoundCloudApi.TrackJson>() {
                    @Override
                    public void gotItem(SoundCloudApi.TrackJson trackJson) {
                        Sng sng = new Sng(trackJson);
                        mSngCache.put(songId, sng);
                        if (l != null) l.gotSong(sng);
                    }

                    @Override
                    public void failure(String e) {
                        Log.e(TAG, "Failure getting sng: " + e);
                        if (l != null) l.failed(songId);

                    }
                });

                return;
            }
        }

        if (l != null) l.failed(songId);
    }

    /** Does returns immediately and does not attempt to search onlinev*/
    public static Sng getSngNow(String songId) {
        return mSngCache.get(songId);
    }
}

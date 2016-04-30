package com.example.steven.spautify.musicplayer;


import android.util.Log;
import android.util.LruCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    /* Shouldn't be null, but may possibly be... Lookup track on it's source to resolve*/
    public String artstId;
    public int durationInMs;
    /** Usually true, but its possible to find songs that aren't streamable in SoundCloud, and if in aplaylist, we dont want to cause
     * confusion  by not showing it.  Instead we just tell the user its not streamable, and skip it if they try to play it.
     * If we ignore it, the MediaPlayer will through an error in 30seconds that we would catch and tel lthe UI.*/
    public boolean streamable;

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

        if (t.artists != null && !t.artists.isEmpty()) {
            artstId = source.prefix + t.artists.get(0).id;
        }
        // TODO if more than 1 artist....


        name = t.name;
        artistPrimaryName = t.artists.get(0).name;
        albumName = t.album.name;
        durationInMs = (int) t.duration_ms;

        if (t.album != null && t.album.images != null && !t.album.images.isEmpty()) {
            artworkUrl = t.album.images.get(0).url;
        }

        spotifyAlbumId = t.album.id;
        spotifyArtistIds = new ArrayList<>();
        for (ArtistSimple art : t.artists) {
            spotifyArtistIds.add(art.id);
        }
        streamable = true;
    }

    public Sng(SoundCloudApi.TrackJson t) {
        source = Source.Soundcloud;
        soundCloudId = t.id;
        sngId = source.prefix + t.id;
        artstId = source.prefix + t.user_id;

        name = t.title;
        artistPrimaryName = t.user.username;
        albumName = null;
        durationInMs = t.duration;
        artworkUrl = t.artwork_url;

        soundCloudJson = t;
        streamable = soundCloudJson.streamable;
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

    @Override
    public String toString() {
        return name;
    }




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
                Map<String, String> options = new HashMap<>();
                options.put(SCRetrofitService.CLIENT_ID, SoundCloudApi.CLIENT_ID);

                Call<SoundCloudApi.TrackJson> cc = SoundCloudApi.getApiService().getTrack(Integer.parseInt(Source.get3rdPartyId(songId)), options);
                cc.enqueue(new Callback<SoundCloudApi.TrackJson>() {
                       @Override
                       public void onResponse(Call<SoundCloudApi.TrackJson> call, Response<SoundCloudApi.TrackJson> response) {
                           if (response.code() == 200) {
                               Sng sng = new Sng(response.body());
                               mSngCache.put(songId, sng);
                               if (l != null) l.gotSong(sng);
                           } else {
                               if (l != null) l.failed(songId);
                           }
                       }

                       @Override
                       public void onFailure(Call<SoundCloudApi.TrackJson> call, Throwable t) {
                           Log.e(TAG, "Failure getting sng: " + t);
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

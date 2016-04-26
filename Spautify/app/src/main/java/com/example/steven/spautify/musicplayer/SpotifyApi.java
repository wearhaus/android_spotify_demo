package com.example.steven.spautify.musicplayer;

import android.util.Log;
import android.util.LruCache;

import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Steven on 12/15/2015.
 *
 *
 * Playback of more than 30seconds requires Spotify paid account to be logged in,
 * so we will, for now, restrict all access to SpotifyApi unless an account is authed.
 *
 * TODO handle issues with this
 */
public class SpotifyApi {

    private static kaaes.spotify.webapi.android.SpotifyApi mApi;

    // TODO to be deprecated and replaced with SngCache
//    private static LruCache<String, Track> mTrackCache;
//    public static LruCache<String, Playlst> mPlaylstCache; // Temp, may not be the best way to handle
    public static LruCache<String, Album> mAlbumCache; // Temp, may not be the best way to handle
    //public static ExtremeAlbum mExtremAlbumTEMP;

    public static final int LIMIT = 100;

    /** Temp class to pass in song list to playlist view.  Ideally, these would be
     * handled with caches and stuff, but this is a demo*/
    public static class ExtremePlaylst {
        public Playlst playlst;
        public ArrayList<Sng> songs;
    }
    public static class ExtremeAlbum {
        public Album album;
        public ArrayList<Sng> songs;
    }

    private final static String TAG = "SpotifyProvider";

    // Authentication MUST be done with an onActivityResult... no exceptions

    public static final String CLIENT_ID = "01989efaabca45e793d7842e29864db8";
    public static final String REDIRECT_URI = "spautify-asdf-test://callback";


    private static String sAccessToken;
    private static String sUserId;
    //private static ArrayList<Playlst> sUserPlaylists;
    //private static ArrayList<Album> sUserSavedAlbums;
    //private static ArrayList<Artist> sUserFollowingArtists;
    private static WMusicProvider.AuthState sAuthState;

    public static void init() {
        sAuthState = WMusicProvider.AuthState.NotLoggedIn;
    }

    public static WMusicProvider.AuthState getAuthState() {
        return sAuthState;
    }

    public static AuthenticationRequest startAuth(boolean forceDialog) {
        sAuthState = WMusicProvider.AuthState.Loading;


        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(
                SpotifyApi.CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                SpotifyApi.REDIRECT_URI);
        builder.setScopes(new String[]{
                "user-read-private",
                "streaming",
                "playlist-read-private",
                "user-library-read",
                "user-follow-read",
                "playlist-modify-public"
        });

        builder.setShowDialog(forceDialog); // Show dialog forces the pop-up window to always appear.  It can be used to log out of Spotify.
        AuthenticationRequest request = builder.build();
        return request;
    }


    public static void onGetAccessToken(String accessToken, AuthCallback acb) {

        sAccessToken = accessToken;


        mApi = new kaaes.spotify.webapi.android.SpotifyApi();
//        mPlaylstCache = new LruCache<>(50);
        mAlbumCache = new LruCache<>(50);

        mAuthCallback = acb;

        mApi.setAccessToken(accessToken);

        mApi.getService().getMe(
                new Callback<UserPrivate>() {
                    @Override
                    public void success(UserPrivate userPrivate, Response response) {
                        Log.i("stuffs", " " + userPrivate.email); // null
                        Log.i("stuffs", " " + userPrivate.id);
                        Log.i("stuffs", " " + userPrivate.country);
                        Log.i("stuffs", " " + userPrivate.birthdate); // null
                        Log.i("stuffs", " " + userPrivate.uri);
                        Log.i("stuffs", " " + userPrivate.display_name);
                        //Log.i("stuffs", " " + userPrivate.followers); // note: followers not supported yet...

                        for (Image i : userPrivate.images) {
                            Log.i("stuffs", " " + i.url + ",  " + i.height + ",  " + i.width);
                        }

                        sUserId = userPrivate.id;
                        gatherCheck();

                        //mApi.getService().getPlaylist(

//                        Map<String, Object> options = new HashMap<>();
//                        options.put(SpotifyService.OFFSET, offset);
//                        options.put(SpotifyService.LIMIT, limit);

//                        mApi.getService().getPlaylists(sUserId, new Callback<Pager<PlaylistSimple>>() {
//                            @Override
//                            public void success(Pager<PlaylistSimple> psp, Response response) {
//
//
//                                ArrayList<Playlst> pl = new ArrayList<Playlst>();
//
//                                for (PlaylistSimple p : psp.items) {
//                                    pl.add(new Playlst(p));
//                                }
//
//                                sUserPlaylists = pl;
//                                gatherCheck();
//
//                            }
//
//                            @Override
//                            public void failure(RetrofitError error) {
//                                Log.e("failure", error.toString());
//                                onDeAuth();
//                                mAuthCallback.callback(sAuthState);
//                                mAuthCallback = null;
//                            }
//
//                        });
//
//                        mApi.getService().getMySavedAlbums(new Callback<Pager<SavedAlbum>>() {
//                            @Override
//                            public void success(Pager<SavedAlbum> psa, Response response) {
//
//
//                                ArrayList<Album> l = new ArrayList<>();
//
//                                for (SavedAlbum p : psa.items) {
//                                    l.add(p.album);
//                                }
//
//                                sUserSavedAlbums = l;
//                                gatherCheck();
//
//                            }
//
//                            @Override
//                            public void failure(RetrofitError error) {
//                                Log.e("failure", error.toString());
//                                onDeAuth();
//                                mAuthCallback.callback(sAuthState);
//                                mAuthCallback = null;
//                            }
//
//                        });




                        // The api is broken for the following command:
                        // we must use the other version instead....
//                        mApi.getService().getFollowedArtists(new Callback<ArtistsCursorPager>() {
//                            @Override
//                            public void success(ArtistsCursorPager artistsCursorPager, Response response) {
//                                ArrayList<Artist> pl = new ArrayList<>();
//                                for (Artist a : artistsCursorPager.artists.items) {
//                                    pl.add(a);
//                                }
//                                sUserFollowingArtists = pl;
//                                gatherCheck();
//                            }
//
//                            @Override
//                            public void failure(RetrofitError error) {
//                                Log.e("failure", error.toString());
//                                onDeAuth();
//                                mAuthCallback.callback(sAuthState);
//                                mAuthCallback = null;
//                            }
//                        });

//                        ArtistsCursorPager artistsCursorPager = mApi.getService().getFollowedArtists();
//
//                        ArrayList<Artist> pl = new ArrayList<>();
//                        for (Artist a : artistsCursorPager.artists.items) {
//                            pl.add(a);
//                        }
//                        sUserFollowingArtists = pl;
//                        gatherCheck();



                        /////


                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e("failure", error.toString());
                        onDeAuth();
                        mAuthCallback.callback(sAuthState);
                        mAuthCallback = null;
                    }
                });



        //tests();
    }


    private static AuthCallback mAuthCallback;

    private static void gatherCheck() {

        if (mAuthCallback != null) {

            if (sAccessToken != null
                    && sUserId != null
                    //&& sUserPlaylists != null
                    //&& sUserSavedAlbums != null
                    //&& sUserFollowingArtists != null
                    ) {

                sAuthState = WMusicProvider.AuthState.LoggedIn;
                mAuthCallback.callback(sAuthState);
                mAuthCallback = null;
            }
            // still trying... wait until next gather check

        } else {
            // a failure happened, ignore all further gather checks until another onGetAccessToken is attempted
        }


    }

    public static void loadPlaylist(final Playlst p, final LoadCallback l) {

//        SpotifyWebApiHandler.getTempApi().getPlaylistTracks(p.owner.id, p.id, new Callback<Pager<PlaylistTrack>>() {
//            @Override
//            public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
//
//                ArrayList<Sng> ss = new ArrayList<>();
//
//                for (PlaylistTrack pt : playlistTrackPager.items) {
//                    //SpotifyWebApiHandler.addTrackToCache(pt.track);
//                    // This will be useless if this playlist is larger than the cache can hold.
//
//                    ss.add(new Sng(pt.track));
//                }
//
//
//                ExtremePlaylst ep = new ExtremePlaylst();
//                ep.playlst = p;
//                ep.songs = ss;
//                mPlaylstCache.put(p.id, ep);
//                l.callback();
//
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Log.e("failure", error.toString());
//
//                l.callback();
//            }
//        });

    }


//    public static void loadAlbum(final Album a, final LoadCallback l) {
//        mApi.getService().getAlbumTracks(a.id, new Callback<Pager<Track>>() {
//            @Override
//            public void success(Pager<Track> trackPager, Response response) {
//                ArrayList<Sng> ss = new ArrayList<>();
//
//                for (Track pt : trackPager.items) {
//                    //SpotifyWebApiHandler.addTrackToCache(pt.track);
//                    // This will be useless if this playlist is larger than the cache can hold.
//                    pt.album = a; // Since it doesnt load this, so we add it in for easy obj instantiation
//
//                    ss.add(new Sng(pt));
//                }
//
//                mExtremAlbumTEMP = new ExtremeAlbum();
//                mExtremAlbumTEMP.album = a;
//                mExtremAlbumTEMP.songs = ss;
//
//                l.callback();
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Log.e("failure", error.toString());
//
//                l.callback();
//            }
//        });
//    }


    public interface LoadCallback {
        void callback();
    }

    public interface AuthCallback {
        void callback(WMusicProvider.AuthState authState);
    }

    public static void onDeAuth() {
        sUserId = null;
        sAccessToken = null;
        //sUserPlaylists = null;
        sAuthState = WMusicProvider.AuthState.NotLoggedIn;
    }


    static String getAccessToken() {
        return sAccessToken;
    }
//    public static ArrayList<Playlst> getUserPlaylists() {
//        return sUserPlaylists;
//    }
//    public static ArrayList<Album> getSavedAlbums() {
//        return sUserSavedAlbums;
//    }
//    public static ArrayList<Artist> getFollowedArtists() {
//        return sUserFollowingArtists;
//    }

    public static void tests() {

        SpotifyService spotify = mApi.getService();


        SpotifyApi.getTempApi().getMe(
                new Callback<UserPrivate>() {
                    @Override
                    public void success(UserPrivate userPrivate, Response response) {
                        Log.i("stuffs", " " + userPrivate.email); // null
                        Log.i("stuffs", " " + userPrivate.id);
                        Log.i("stuffs", " " + userPrivate.country);
                        Log.i("stuffs", " " + userPrivate.birthdate); // null
                        Log.i("stuffs", " " + userPrivate.uri);
                        Log.i("stuffs", " " + userPrivate.display_name);
                        //Log.i("stuffs", " " + userPrivate.followers); // note: followers not supported yet...

                        for (Image i : userPrivate.images) {
                            Log.i("stuffs", " " + i.url + ",  " + i.height + ",  " + i.width);
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e("failure", error.toString());
                    }
                });




        //spotify.getAlbum("2dIGnmEIy1WZIcZCFSj6i8", new Callback<Album>() {
        //    @Override
        //    public void success(Album albumName, Response response) {
        //        Log.d("Album success", albumName.name + albumName);
        //    }
        //
        //    @Override
        //    public void failure(RetrofitError error) {
        //        Log.d("Album failure", error.toString());
        //    }
        //});

        //Log.d("getFollowedArtists", "getFollowedArtists:" +
        //        mApi.getService().getFollowedArtists().artists.describeContents()
        //);


        //Log.d("getFollowedArtists", "getFollowedArtists:" +
        //        mApi.getService().searchTracks("Test search Query")
        //);

//        mApi.getService().searchTracks("The", new Callback<TracksPager>() {
//            @Override
//            public void success(TracksPager trackspager, Response response) {
//                Log.d("TracksPager success", "" + trackspager.tracks.items);
//                for (Track t : trackspager.tracks.items) {
//                    Log.d("", t.name + "" + ", " + t.uri + ", " + t.albumName.name);
//
//                }
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Log.d("Track failure", error.toString());
//            }
//
//        });
//
//        testGetTrack("Here Comes The Sun", "The Beatles (Remastered)", "Abbey Road");
//        testGetTrack("Welcome to Vulf Records", "Vulfpeck", "Thrill of the Arts");




    }


    public static String getUserId() {
        return sUserId;
    }

    public static SpotifyService getTempApi() {
        return mApi.getService();
    }


    private static void testGetTrack(String trackName, final String effectiveArtist, final String albumName) {


        mApi.getService().searchTracks(trackName, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager trackspager, Response response) {
                Log.d("TracksPager success", "" + trackspager.tracks.items);
                for (Track t : trackspager.tracks.items) {

                    boolean alb = strEquals(t.album.name, albumName, 0);
                    Log.d("", t.album.name + ", " + albumName);

                    boolean art = false;
                    for (ArtistSimple a : t.artists) {
                        Log.d("", a.name + ", " + effectiveArtist);
                        if (strEquals(a.name, effectiveArtist, 0)) {
                            art = true;
                            break;
                        }
                    }


                    Log.d("", t.name + "" + ", " + t.uri + ", " + t.album.name + ",   alb:"+alb + " art:"+art);

                    if (alb && art) {
                        Log.w("testGetTrack", "Matches!  SpotifyUri=" + t.uri);
                        return;
                    }


                }

                Log.w("testGetTrack", "Never matched");
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Track failure", error.toString());
            }

        });

    }

    private static boolean strEquals(String a, String b, int errorMargin) {
        if (a != null) {
            return a.equals(b);
        }
        return b == null;

    }


    //public static SpotifyService getService() {
    //    return mApi.getService();
    //}


    /** Checks cache, and if not in cache, requests Spotify's Server for the track.
     * trackUri ex/ "spotify:track:65IxnRl41h2UI7sd31TPu5"
     * trackId ex/ "65IxnRl41h2UI7sd31TPu5"
     * */
    public static void getTrackBySongId(final String songId, final Sng.GetSongListener l) {

        final String trackUri = Source.get3rdPartyId(songId);
        if (getAuthState() != WMusicProvider.AuthState.LoggedIn) {
            l.failed(songId);
            return;
        }

        mApi.getService().getTrack(getSpotifyIdFromUri(trackUri), new Callback<Track>() {
            @Override
            public void success(Track track, Response response) {
                Sng sng = new Sng(track);
                Sng.mSngCache.put(sng.songId, sng);
                if (l != null) l.gotSong(sng);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Track failure", error.toString());
                if (l != null) l.failed(songId);
            }

        });

    }



    /** Removes any prefixing "spotify:track:" which messes up the search (redundant)*/
    public static String getSpotifyIdFromUri(String uri) {
        if (uri == null) return null;
        return uri.replace("spotify:track:", "");
    }


}

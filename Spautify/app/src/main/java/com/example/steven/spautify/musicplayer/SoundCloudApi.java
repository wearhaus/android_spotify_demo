package com.example.steven.spautify.musicplayer;

import com.example.steven.spautify.HTTP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Steven on 4/19/2016.
 */
public class SoundCloudApi {
    public static final String CLIENT_ID = "5916491062a0fd0196366d76c22ac36e";
    static final String CLIENT_ID_PARAM = "client_id="+CLIENT_ID;
    static final String API_URL = "https://api.soundcloud.com";

    /** TODO may be unsafe storing secret here!  Here only temporarily for dev*/
    static final String ClientSecret = "452f2dac8e6c52831010b0479b9c5d31";


    // In the end, there won't be a cache for just SoundCliud tracks, but instead Sng objects
    //private static LruCache<Integer, TrackJson> mTrackCache;

    /* https://developers.soundcloud.com/docs/api/guide#playing
     * When using a custom player you must:

     Credit the uploader as the creator of the sound
     Credit SoundCloud as the source by including one of the logos found here
     Link to the SoundCloud URL containing the work
     If the sound is private link to the profile of the creator*/


    private static WMusicProvider.AuthState sAuthState;
    private static SCRetrofitService mApi;
    public static void init() {
        sAuthState = WMusicProvider.AuthState.NotLoggedIn;

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL + "/")
                .client(client) // this allows us to set the logging level
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mApi = retrofit.create(SCRetrofitService.class);

    }

    public static WMusicProvider.AuthState getAuthState() {
        return sAuthState;
    }

    private static void tests() {
        GotItem gg = new GotItem<TrackJson>() {
            @Override
            public void gotItem(TrackJson trackJson) {

            }

            @Override
            public void failure(String e) {

            }
        };

        getTrackByIdOnline(156909581, gg);
        getTrackByIdOnline(3, gg);
        //getTrackByIdOnline(156909581, gg);


//        searchTrack("hello", null);
//        searchTrack("drowning in sleep", null);
//        searchTrack("drowning-in-sleep", null);
//        searchTrack("Float", null);

    }





    public static void getTrackByIdOnline(int Id, GotItem<TrackJson> listener) {
        H_GetTrack hhh = new H_GetTrack(Id, listener);
        hhh.execute();
    }

    public static SCRetrofitService getApiService() {
        return mApi;
    }

    public static void searchTrack(String query, final GotTrackArray listener, int offset, int limit) {
        // Breaks with spaces.
        String q = query.replace(" ", "-");
        // limit max is 200, default is 10
//        H_SearchTrack hhh = new H_SearchTrack(q, listener, offset, limit);
//        hhh.execute();

        Map<String, String> options = new HashMap<>();
        options.put(SCRetrofitService.OFFSET, ""+offset);
        options.put(SCRetrofitService.LIMIT, ""+limit);
        options.put(SCRetrofitService.QUERY, query);
        options.put(SCRetrofitService.CLIENT_ID, CLIENT_ID);
        options.put(SCRetrofitService.PAGINATE, ""+1);

        Call<SearchTrackJson> cc = mApi.searchTracks(options);
        cc.enqueue(new Callback<SearchTrackJson>() {

            @Override
            public void onResponse(Call<SearchTrackJson> call, Response<SearchTrackJson> response) {
                listener.gotItem(response.body().collection);
            }

            @Override
            public void onFailure(Call<SearchTrackJson> call, Throwable t) {
                listener.failure();
            }
        });
    }



    public interface GotItem<I> {
        void gotItem(I i);
        void failure(String e);
    }

    public interface GotTrackArray {
        void gotItem(TrackJson[] tj);
        void failure();
    }

    public interface GotPlaylistArray {
        void gotItem(PlaylistJson[] pj);
        void failure();
    }

    public static void searchPlaylists(String query, final GotPlaylistArray listener, int offset, int limit) {
        String q = query.replace(" ", "-");

        Map<String, String> options = new HashMap<>();
        options.put(SCRetrofitService.OFFSET, ""+offset);
        options.put(SCRetrofitService.LIMIT, ""+limit);
        options.put(SCRetrofitService.QUERY, query);
        options.put(SCRetrofitService.CLIENT_ID, CLIENT_ID);
        options.put(SCRetrofitService.PAGINATE, ""+1);

        Call<SearchPlaylistJson> cc = mApi.searchPlaylists(options);
        cc.enqueue(new Callback<SearchPlaylistJson>() {

            @Override
            public void onResponse(Call<SearchPlaylistJson> call, Response<SearchPlaylistJson> response) {
                listener.gotItem(response.body().collection);
            }

            @Override
            public void onFailure(Call<SearchPlaylistJson> call, Throwable t) {
                listener.failure();
            }
        });

    }


    private static class H_SearchTrack extends HTTP<SearchTrackJson> {
        // json is TrackJson[] when no linked_partitioning/offset/limit
        // json is SearchTrackJson otherwise
        private GotTrackArray listener;

        public H_SearchTrack(String q, GotTrackArray l, int offset, int limit) {
            super(Method.GET,
                    API_URL + "/tracks/?linked_partitioning=1&q=" + q + "&" + CLIENT_ID_PARAM +  "&offset=" + offset + "&limit=" + limit,
                    //TrackJson[].class
                    SearchTrackJson.class
            );
            listener = l;
        }


        @Override
        protected void onResponse(SearchTrackJson jr) {
            listener.gotItem(jr.collection);
        }

        @Override
        protected void onAnyError() {
            listener.failure();

        }
    }

    private static class H_GetTrack extends HTTP<TrackJson> {
        private GotItem listener;

        public H_GetTrack(int trackId, GotItem l) {
            super(Method.GET,
                    API_URL + "/tracks/" + trackId  +"?" + CLIENT_ID_PARAM,
                    TrackJson.class);
            listener = l;
        }

        @Override
        protected void onResponse(TrackJson jr) {
            listener.gotItem(jr);
        }

        @Override
        protected void onAnyError() {
            listener.failure("network error");
        }
    }


    public static void searchPlaylist(String q, GotTrackArray l, int offset, int limit) {




    }


    public static class TrackJson {
        // Schema as of April 2016

        public String kind; // ex/"track",
        public int id; // ex/13158665,
        public String created_at; // ex/"2011/04/06 15:37:43 +0000",
        public int user_id; // ex/3699101,
        public int duration; // ex/18109,
        public boolean commentable; // ex/true,
        public String state; // ex/"finished",
        public int original_content_size; // ex/201483,
        public String last_modified; // ex/"2013/02/18 19:18:11 +0000",
        public String sharing; // ex/"public",
        public String tag_list; // ex/"soundcloud:source=iphone-record",
        public String permalink; // ex/"munching-at-tiannas-house",
        public boolean streamable; // ex/true,
        public String embeddable_by; // ex/"all",
        public boolean downloadable; // ex/false,
        public String purchase_url; // ex/null,
        public String label_id; // ex/null,
        public String purchase_title; // ex/null,
        public String genre; // ex/null,
        public String title; // ex/"Munching at Tiannas house",
        public String description; // ex/null,
        public String label_name; // ex/null,
        public String release; // ex/null,
        public String track_type; // ex/"recording",
        public String key_signature; // ex/"Cmaj",
        public String isrc; // ex/null,
        public String video_url; // ex/null,
        public String bpm; // ex/null,
        public String release_year; // ex/null,
        public String release_month; // ex/null,
        public String release_day; // ex/null,
        public String original_format; // ex/"m4a",
        public String license; // ex/"all-rights-reserved",
        public String uri; // ex/"https://api.soundcloud.com/tracks/13158665",
        public SoundCloudApi.UserSimpleJson user;
        public SoundCloudApi.CreatedWithJson created_with;
        public String permalink_url; // ex/"http://soundcloud.com/alex-stevenson/munching-at-tiannas-house",
        public String artwork_url; // ex/null,
        public String waveform_url; // ex/"https://w1.sndcdn.com/fxguEjG4ax6B_m.png",
        public String stream_url; // ex/"https://api.soundcloud.com/tracks/13158665/stream",
        public int playback_count; // ex/7520,
        public int download_count; // ex/134,
        public int favoritings_count; // ex/2,
        public int comment_count; // ex/6,
        public String attachments_uri; // ex/"https://api.soundcloud.com/tracks/13158665/attachments"

    }


    public static class UserSimpleJson {
        public int id; // ex/3699101,
        public String kind; // ex/"user",
        public String permalink; // ex/"alex-stevenson",
        public String username; // ex/"Alex Stevenson",
        public String last_modified; // ex/"2011/06/13 23:58:44 +0000",
        public String uri; // ex/"https://api.soundcloud.com/users/3699101",
        public String permalink_url; // ex/"http://soundcloud.com/alex-stevenson",
        public String avatar_url; // ex/"https://i1.sndcdn.com/avatars-000004193858-jnf2pd-large.jpg"
    }

    public static class CreatedWithJson {
        public int id; // ex/124,
        public String kind; // ex/"app",
        public String name; // ex/"SoundCloud iOS",
        public String uri; // ex/"https://api.soundcloud.com/apps/124",
        public String permalink_url; // ex/"http://developers.soundcloud.com/",
        public String external_url; // ex/"http://itunes.com/app/soundcloud"
    }

    /**Note: potentially 100kb size in son format!  crazy*/
    public static class PlaylistJson {

        public int duration; //154516,
        public String release_day; //null,
        public String permalink_url; //"http://soundcloud.com/jwagener/sets/field-recordings",
        public String genre; //"",
        public String permalink; //"field-recordings",
        public String purchase_url; //null,
        public String release_month; //null,
        public String description; //"a couple of field recordings to test http://soundiverse.com.\r\n\r\nrecorded with the fire recorder: http://soundcloud.com/apps/fire",
        public String uri; //"https://api.soundcloud.com/playlists/405726",
        public String label_name; //"",
        public String tag_list; //"",
        public String release_year; //null,
        public int track_count; //5,
        public int user_id; //3207,
        public String last_modified; //"2012/06/28 18:09:17 +0000",
        public String license; //"all-rights-reserved",

        /** NOTE: This is not paginated; it is the entire list*/
        public ArrayList<TrackJson> tracks; //[  ],
        public String playlist_type; //"other",
        public int id; //405726,
        public boolean downloadable; //true,
        public String sharing; //"public",
        public String created_at; //"2010/11/02 09:24:50 +0000",
        public String release; //"",
        public String kind; //"playlist",
        public String title; //"Field Recordings",
        public String type; //"other",
        public String purchase_title; //null,
        public String artwork_url; //"https://i1.sndcdn.com/artworks-000025801802-1msl1i-large.jpg",
        public String ean; //"",
        public boolean streamable; //true,
        public UserSimpleJson user; //{  },
        public String embeddable_by; //"me",
        public String label_id; //null

    }



    public static class UserJson {
        public int id; // 3207,
        public String kind; // "user",
        public String permalink; // "jwagener",
        public String username; // "Johannes Wagener",
        public String last_modified; // "2016/04/18 13:58:15 +0000",
        public String uri; // "https://api.soundcloud.com/users/3207",
        public String permalink_url; // "http://soundcloud.com/jwagener",
        public String avatar_url; // "https://i1.sndcdn.com/avatars-000214493195-8h8dpe-large.jpg",
        public String country; // "Germany",
        public String first_name; // "Johannes",
        public String last_name; // "Wagener",
        public String full_name; // "Johannes Wagener",
        public String description; // "",
        public String city; // "Berlin",
        public String discogs_name; // null,
        public String myspace_name; // null,
        public String website; // "http://johannes.wagener.cc",
        public String website_title; // "johannes.wagener.cc",
        public boolean online; // false,
        public int track_count; // 55,
        public int playlist_count; // 3,
        public String plan; // "Free",
        public int public_favorites_count; // 236,
        public String subscriptions; // [],
        public int followers_count; // 2139,
        public int followings_count; // 346

    }



    public static class SearchTrackJson {
        public TrackJson[] collection; //
        public String next_href; // ex/ ttps://api.soundcloud.com/tracks?linked_partitioning=1&client_id=5916491062a0fd0196366d76c22ac36e&offset=10&q=cool&limit=10
    }


    public static class SearchPlaylistJson {
        public PlaylistJson[] collection; //
        public String next_href; // ex/ ttps://api.soundcloud.com/tracks?linked_partitioning=1&client_id=5916491062a0fd0196366d76c22ac36e&offset=10&q=cool&limit=10
    }

    public static class SearchUserJson {
        public UserJson[] collection; //
        public String next_href; // ex/ ttps://api.soundcloud.com/tracks?linked_partitioning=1&client_id=5916491062a0fd0196366d76c22ac36e&offset=10&q=cool&limit=10
    }


}

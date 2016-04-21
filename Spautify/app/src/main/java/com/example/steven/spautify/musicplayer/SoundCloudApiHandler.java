package com.example.steven.spautify.musicplayer;

import android.util.ArrayMap;
import android.util.LruCache;

import com.example.steven.spautify.HTTP;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Steven on 4/19/2016.
 */
public class SoundCloudApiHandler {
    static final String CLIENT_ID = "5916491062a0fd0196366d76c22ac36e";
    static final String CLIENT_ID_PARAM = "client_id="+CLIENT_ID;
    static final String API_URL = "https://api.soundcloud.com";

    /** TODO may be unsafe storing secret here!  Here only temporarily for dev*/
    static final String ClientSecret = "452f2dac8e6c52831010b0479b9c5d31";


    private static LruCache<Integer, TrackJson> mTrackCache;

    /* https://developers.soundcloud.com/docs/api/guide#playing
     * When using a custom player you must:

     Credit the uploader as the creator of the sound
     Credit SoundCloud as the source by including one of the logos found here
     Link to the SoundCloud URL containing the work
     If the sound is private link to the profile of the creator*/


    private static WMusicProvider.AuthState sAuthState;
    public static void init() {
        sAuthState = WMusicProvider.AuthState.LoggedIn;

        mTrackCache = new LruCache<Integer, TrackJson>(150); // 150 track entries max limit.
        // Don't need account logged in?
        //tests();
    }

    private static void tests() {
        GotItem gg = new GotItem<TrackJson>() {
            @Override
            public void gotItem(TrackJson trackJson) {

            }

            @Override
            public void failure() {

            }
        };

        getTrack(156909581, gg);
        getTrack(3, gg);
        //getTrack(156909581, gg);


        searchTrack("hello", null);
        searchTrack("drowning in sleep", null);
        searchTrack("drowning-in-sleep", null);
        searchTrack("Float", null);

    }





    public static void getTrack(int Id, GotItem<TrackJson> listener) {
        if (mTrackCache.get(Id) != null) {
            if (listener != null) listener.gotItem(mTrackCache.get(Id));
        } else {
            H_GetTrack hhh = new H_GetTrack(Id, listener);
            hhh.execute();
        }
    }



    public static void searchTrack(String query, GotItemArray listener) {
        // Breaks with spaces.
        String q = query.replace(" ", "-");
        H_SearchTrack hhh = new H_SearchTrack(q, listener);
        hhh.execute();
    }



    public interface GotItem<I> {
        void gotItem(I i);
        void failure();
    }

    public interface GotItemArray {
        void gotItem(TrackJson[] tj);
        void failure();
    }


    private static class H_SearchTrack extends HTTP<TrackJson[]> {
        private GotItemArray listener;

        public H_SearchTrack(String q, GotItemArray l) {
            super(Method.GET,
                    API_URL + "/tracks/?q=" + q + "&" + CLIENT_ID_PARAM,
                    TrackJson[].class);
            listener = l;
        }


        @Override
        protected void onResponse(TrackJson[] jr) {
            for (TrackJson tj : jr) {
                mTrackCache.put(tj.id, tj);
            }
            if (listener != null) listener.gotItem(jr);
        }

        @Override
        protected void onAnyError() {
            if (listener != null) listener.failure();

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
            mTrackCache.put(jr.id, jr);
            if (listener != null) listener.gotItem(jr);
        }

        @Override
        protected void onAnyError() {
            if (listener != null) listener.failure();

        }
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
        public UserJson user;
        public CreatedWithJson created_with;
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


    public static class UserJson {
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


}

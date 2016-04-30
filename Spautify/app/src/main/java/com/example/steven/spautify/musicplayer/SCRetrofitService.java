package com.example.steven.spautify.musicplayer;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;


/**
 * Created by Steven on 4/25/2016.
 */
public interface SCRetrofitService {
    String LIMIT = "limit";
    String OFFSET = "offset";
    String QUERY = "q";
    String CLIENT_ID = "client_id";
    /**Note: This field can change the resulting json for searches to be a different object
     * aka, changes from list of songs (which is not really gson-able) to a SearchTrackJson object*/
    String PAGINATE = "linked_partitioning";
    /** For playlists, returns it without tracks, or with just track ids: values: 'compact' or 'id'*/
    String COMPACT = "representation";


    //////// Search

    @GET("tracks/")
    /**requires q param*/
    Call<SoundCloudApi.PagedTrackJson> searchTracks(@QueryMap Map<String, String> options);

    @GET("playlists/")
    /**requires q param*/
    Call<SoundCloudApi.PagedPlaylistJson> searchPlaylists(@QueryMap Map<String, String> options);

    @GET("users/")
    /**requires q param*/
    Call<SoundCloudApi.PagedUserJson> searchUsers(@QueryMap Map<String, String> options);



    ////// Get

    @GET("tracks/{id}")
    Call<SoundCloudApi.TrackJson> getTrack(@Path("id") int id, @QueryMap Map<String, String> options);

    @GET("playlists/{id}")
    Call<SoundCloudApi.PlaylistJson> getPlaylist(@Path("id") int id, @QueryMap Map<String, String> options);

    @GET("users/{id}")
    Call<SoundCloudApi.UserJson> getUser(@Path("id") int id, @QueryMap Map<String, String> options);

    @GET("users/{id}/tracks/")
    Call<SoundCloudApi.PagedTrackJson> getUserTracks(@Path("id") int id, @QueryMap Map<String, String> options);

    //playlists?linked_partitioning=1&client_id=5916491062a0fd0196366d76c22ac36e&offset=0&q=undertale&limit=10

}

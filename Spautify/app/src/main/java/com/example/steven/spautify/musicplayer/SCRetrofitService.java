package com.example.steven.spautify.musicplayer;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;


/**
 * Created by Steven on 4/25/2016.
 */
public interface SCRetrofitService {
    String LIMIT = "limit";
    String OFFSET = "offset";
    String QUERY = "q";
    String CLIENT_ID = "client_id";
    String PAGINATE = "linked_partitioning";



    @GET("tracks/")
    Call<SoundCloudApi.SearchTrackJson> searchTracks(@QueryMap Map<String, String> options);


    @GET("playlists/")
    Call<SoundCloudApi.SearchPlaylistJson> searchPlaylists(@QueryMap Map<String, String> options);

    //playlists?linked_partitioning=1&client_id=5916491062a0fd0196366d76c22ac36e&offset=0&q=undertale&limit=10

}

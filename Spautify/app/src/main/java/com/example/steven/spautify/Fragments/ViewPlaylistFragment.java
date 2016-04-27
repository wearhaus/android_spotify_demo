package com.example.steven.spautify.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.steven.spautify.R;
import com.example.steven.spautify.ViewPlaylistActivity;
import com.example.steven.spautify.musicplayer.Playlst;
import com.example.steven.spautify.musicplayer.SCRetrofitService;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.SoundCloudApi;
import com.example.steven.spautify.musicplayer.SoundCloudProvider;
import com.example.steven.spautify.musicplayer.Source;
import com.example.steven.spautify.musicplayer.SpotifyApi;
import com.example.steven.spautify.musicplayer.WMusicProvider;
import com.example.steven.spautify.musicplayer.WPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit2.Call;

/**
 * Created by Steven on 2/10/2016.
 *
 * It is an error to not have a TAG_ID argument
 */
public class ViewPlaylistFragment extends SongListFragment {

    private String mPlaylstId;
    private Source mSource;
    private ArrayList<SongListFragment.SngItem> mList;
    private Button mPlayButton;

    private Playlst mPlaylst;

    // TODO if we detect WPlayer's autosource matches our mPlaylstId, then display that here and mark the current song




    /** The proper way to create a new Fragment with a passed arg. */
    public static ViewPlaylistFragment newInstance(String id) {
        ViewPlaylistFragment frag = new ViewPlaylistFragment();

        Bundle args = new Bundle();
        args.putString(ViewPlaylistActivity.TAG_ID, id);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlaylstId = getArguments().getString(ViewPlaylistActivity.TAG_ID);
        mSource = Source.getSource(mPlaylstId);
    }


    @Override
    protected int getHeaderXml() {
        return R.layout.view_playlist_header;
    }


    private boolean unAuthed = false;

    @Override
    protected void updateList() {
        if (unAuthed && SpotifyApi.getAuthState() == WMusicProvider.AuthState.LoggedIn) {
            unAuthed = false;
            init();
        }
        super.updateList();
    }

    private void init() {
        if (mSource == Source.Spotify && SpotifyApi.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
            unAuthed = true;
            mPlayButton.setVisibility(View.GONE);
            return;
        }

        mPlaylst = Playlst.mPlaylstCache.get(mPlaylstId);
        mPlayButton.setVisibility(View.VISIBLE);


        mList = new ArrayList<>();
        mPageLoadedCount = 0;

        loadData(0);


        mPlayButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ArrayList<Sng> songlist = new ArrayList<>();

                        for (SngItem si : mList) {
                            songlist.add(si.sng);
                        }

                        WPlayer.playManyClearQueue(songlist);

                        //WPlayer.playpause();
                    }
                });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mPlayButton = (Button) view.findViewById(R.id.play_button);

        init();

        return view;
    }

    @Override
    protected boolean paginated() {
        return true;
    }

    @Override
    protected void loadData(int offset) {
        Log.d("ggg", "loadData  " + offset);

        if (mSource == Source.Spotify) {
            loadDataSpotify(offset);
        } else if (mSource == Source.Soundcloud) {
            loadDataSoundCloud(offset);
        }


    }


    private void loadDataSoundCloud(int offset) {

        if (mPlaylst == null || mPlaylst.soundcloudObject.tracks == null || mPlaylst.soundcloudObject.track_count > mPlaylst.soundcloudObject.tracks.size()) {
            Log.e("loadDataSoundcloud", "Dont have all soundcloud track data in object");

            // NOTE: http://stackoverflow.com/questions/36360202/soundcloud-api-urls-timing-out-and-then-returning-error-403-on-about-50-of-trac
            // Tracks, anytime, can be hidden or deleted or made private, so they may return a 403 error
            // This behavior here also assumes that we cant paginate playlist content.

            setRefreshing(true);
            mPageIsLoading = true;

            Map<String, String> options = new HashMap<>();
            options.put(SCRetrofitService.CLIENT_ID, SoundCloudApi.CLIENT_ID);
            options.put(SCRetrofitService.OFFSET, ""+offset);
            options.put(SCRetrofitService.LIMIT, ""+getPageSize()); // dont need to say paginated here it seems

            Call<SoundCloudApi.PlaylistJson> ccc = SoundCloudApi.getApiService().getPlaylist(mPlaylst.soundcloudObject.id, options);
            ccc.enqueue(new retrofit2.Callback<SoundCloudApi.PlaylistJson>() {
                @Override
                public void onResponse(Call<SoundCloudApi.PlaylistJson> call, retrofit2.Response<SoundCloudApi.PlaylistJson> response) {
                    mPlaylst = new Playlst(response.body());
                    ArrayList<SngItem> ss = new ArrayList<>();
                    // tracks are not numbered, so we have to be careful about adding them
                    for (SoundCloudApi.TrackJson tj : response.body().tracks) {
                        if (tj != null && tj.streamable) {
                            ss.add(new SngItem(new Sng(tj), SngItem.Type.NotInQueue));
                            //Sng.cacheSng(
                        } else {
                            String g = null; g.length();
                            // use https://api.soundcloud.com/playlists/10205283?limit=100&offset=0&client_id=5916491062a0fd0196366d76c22ac36e
                            //ss.add(new SngItem(new Sng(), SngItem.Type.NotInQueue));
                            Log.e("failure", "track was null/incomplete in soundcloud GetPlaylist.  Ist it private/region locked/removed?");
                        }
                    }
                    //TODO cache, also note that we are not going to cache the playlist obj with the song objects


                    setRefreshing(false);
                    mPageIsLoading = false;

                    mList.addAll(ss);
                    mPageLoadedCount = mList.size();
                    mPageTotalAbleToBeLoaded = mPlaylst.soundcloudObject.track_count;
                    updateList();
                }

                @Override
                public void onFailure(Call<SoundCloudApi.PlaylistJson> call, Throwable t) {
                    Log.e("failure", ""+t);

                    setRefreshing(false);
                    mPageIsLoading = false;
                }
            });


        } else {

            // discoverd problem was that representation was ignored unless the slash was between palylists and ?
            // strange how it didnt break any other paramsl only representation..


            ArrayList<SngItem> ss = new ArrayList<>();
            for (SoundCloudApi.TrackJson tj : mPlaylst.soundcloudObject.tracks) {
                ss.add(new SngItem(new Sng(tj), SngItem.Type.NotInQueue));
            }
            //TODO cache

            mList.addAll(ss);
            mPageLoadedCount = mList.size();
            mPageTotalAbleToBeLoaded = mPlaylst.soundcloudObject.track_count;
            setRefreshing(false);
            mPageIsLoading = false;


            updateList();

        }


    }
    private void loadDataSpotify(int offset) {

        if (SpotifyApi.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
            return;
        }
        if (mPlaylst == null) return; // TODO

        // TODO this ought to cache the songs or something in case this is fragment is closed and reopened.

        setRefreshing(true);
        mPageIsLoading = true;


        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, offset);
        options.put(SpotifyService.LIMIT, getPageSize());


        SpotifyApi.getTempApi().getPlaylistTracks(mPlaylst.spotifyObject.owner.id, mPlaylst.spotifyId, options, new Callback<Pager<PlaylistTrack>>() {
            @Override
            public void success(Pager<PlaylistTrack> ptp, Response response) {

                ArrayList<SngItem> ss = new ArrayList<>();

                Log.d("ggg", "limit " + ptp.limit);
                Log.d("ggg", "offset  " + ptp.offset);
                Log.d("ggg", "total  " + ptp.total);

                for (PlaylistTrack pt : ptp.items) {
                    ss.add(new SngItem(new Sng(pt.track), SngItem.Type.NotInQueue));
                }
                // TODO cache

                mPageLoadedCount = ptp.offset + ptp.limit;
                mPageTotalAbleToBeLoaded = ptp.total;

                setRefreshing(false);
                mPageIsLoading = false;

                // TODO is it better to add to list then just 'change' list
                // or should we call mRecyclerAdapter.add?  What ismore efficient?
                mList.addAll(ss);
                updateList();

            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("failure", error.toString());

                setRefreshing(false);
                mPageIsLoading = false;
            }
        });
    }



    @Override
    protected SongListFragment.ClickType getClickType() {
        return ClickType.Lib;
    }

    @Override
    protected List getList() {
        return mList;
    }

    protected int getPageSize() {
        return 100;
    }

    @Override
    protected String checkIfBad() {
        if (WPlayer.getState() == WPlayer.WPlayerState.Off) {
            return "Player is off";
        } else if (mSource == Source.Spotify && SpotifyApi.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
            return "No Spotify account found";
        }
        // No, we want the refreshing to tell us it's loading
//        else if (mPageIsLoading) {
//            return "loading";
//        }
        else if (!mPageIsLoading && mList == null) {
            return "unable to load playlist";
        } else if (!mPageIsLoading && mList.size() <= 0) {
            return "playlist is empty";
        }
        return null;
    }

    @Override
    protected boolean canSwipeToRefresh() {
        return false;
    }


}

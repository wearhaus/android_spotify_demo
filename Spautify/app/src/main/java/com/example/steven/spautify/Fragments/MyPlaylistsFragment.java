package com.example.steven.spautify.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.steven.spautify.musicplayer.Playlst;
import com.example.steven.spautify.musicplayer.SpotifyApi;
import com.example.steven.spautify.musicplayer.WMusicProvider;
import com.example.steven.spautify.musicplayer.WPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Steven on 2/10/2016.
 */
public class MyPlaylistsFragment extends MusicLibFragment {

    private ArrayList<Playlst> mList;
    private String mUserId;

    @Override
    public MusicLibType getMusicLibType() {
        return MusicLibType.Playlist;
    }

    @Override
    protected List getList() {
        return mList;
    }

    private boolean unAuthed = false;

    @Override
    protected void updateList() {
        if (unAuthed && SpotifyApi.getAuthState() == WMusicProvider.AuthState.LoggedIn) {
            init();
        }
        super.updateList();
    }


    private void init() {
        unAuthed = SpotifyApi.getAuthState() != WMusicProvider.AuthState.LoggedIn;
        if (unAuthed) {
            return;
        }

        mUserId = SpotifyApi.getUserId();
        mList = new ArrayList<>();
        mPageLoadedCount = 0;
        loadData(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);

        init();


        return view;
    }

    @Override
    protected String checkIfBad() {
        if (WPlayer.getState() == WPlayer.WPlayerState.Off) {
            return "Player is off";
        } else if (SpotifyApi.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
            return "No Spotify account found";
        } else if (getList() == null) {
            return "list is empty";
        }
        return null;
    }

    @Override
    protected boolean canSwipeToRefresh() {
        return false;
    }



    @Override
    protected void onSwipeRefresh() {
        updateList();
    }


    @Override
    protected boolean paginated() {
        return false;
    }

    protected int getPageSize() {
        return 50;
    }


    @Override
    protected void loadData(int offset) {
        Log.d("ggg", "loadData  " + offset);
        // TODO this ought to cache the songs or something in case this is fragment is closed and reopened.

        if (SpotifyApi.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
            return;
        }

        setRefreshing(true);
        mPageIsLoading = true;


        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, offset);
        options.put(SpotifyService.LIMIT, getPageSize());


        SpotifyApi.getTempApi().getPlaylists(mUserId, options, new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> psp, Response response) {


                Log.d("ggg", "limit " + psp.limit);
                Log.d("ggg", "offset  " + psp.offset);
                Log.d("ggg", "total  " + psp.total);

                ArrayList<Playlst> pl = new ArrayList<>();

                for (PlaylistSimple p : psp.items) {
                    pl.add(new Playlst(p));
                }

                mPageLoadedCount = psp.offset + psp.limit;
                mPageTotalAbleToBeLoaded = psp.total;

                setRefreshing(false);
                mPageIsLoading = false;

                // TODO is it better to add to list then just 'change' list
                // or should we call mRecyclerAdapter.add?  What is more efficient?
                mList.addAll(pl);
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
}

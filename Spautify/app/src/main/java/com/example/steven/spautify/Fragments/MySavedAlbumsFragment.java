package com.example.steven.spautify.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.steven.spautify.musicplayer.Playlst;
import com.example.steven.spautify.musicplayer.SpotifyWebApiHandler;
import com.example.steven.spautify.musicplayer.WPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.SavedAlbum;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Steven on 2/11/2016.
 */
public class MySavedAlbumsFragment extends AlbumsFragment {


    private ArrayList<Album> mList;
    private String mUserId;

    @Override
    protected List getList() {
        return mList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mList = new ArrayList<>();
        mPageLoadedCount = 0;
        loadData(0);
        // TODO this should be cached

        return view;
    }

    @Override
    protected String checkIfBad() {
        if (WPlayer.getState() == WPlayer.State.Off) {
            return "Player is off";
        } else if (getList() == null) {
            return "list is null";
        }
        return null;
    }

    @Override
    protected boolean canSwipeToRefresh() {
        return false;
    }

    @Override
    protected void onSwipeRefresh() {
    }
    @Override
    protected boolean paginated() {
        return true;
    }


    @Override
    protected int getPageSize() {
        return 50;
    }

    @Override
    protected void loadData(int offset) {
        Log.d("ggg", "loadData  " + offset);
        // TODO this ought to cache the songs or something in case this is fragment is closed and reopened.

        mLoadingContainer.setVisibility(View.VISIBLE);
        mPageIsLoading = true;


        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, offset);
        options.put(SpotifyService.LIMIT, getPageSize());


        SpotifyWebApiHandler.getTempApi().getMySavedAlbums(options, new Callback<Pager<SavedAlbum>>() {
            @Override
            public void success(Pager<SavedAlbum> psa, Response response) {


                ArrayList<Album> l = new ArrayList<>();
                for (SavedAlbum p : psa.items) {
                    l.add(p.album);
                }

                mPageLoadedCount = psa.offset + psa.limit;
                mPageTotalAbleToBeLoaded = psa.total;

                mLoadingContainer.setVisibility(View.GONE);
                mPageIsLoading = false;

                // TODO is it better to add to list then just 'change' list
                // or should we call mRecyclerAdapter.add?  What is more efficient?
                mList.addAll(l);
                updateList();

            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("failure", error.toString());

                mLoadingContainer.setVisibility(View.GONE);
                mPageIsLoading = false;
            }
        });

    }
}

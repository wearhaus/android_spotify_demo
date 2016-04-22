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
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.SpotifyApiController;
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

/**
 * Created by Steven on 2/10/2016.
 */
public class ViewPlaylistFragment extends SongListFragment {

    private String mPlaylistId;
    private ArrayList<SongListFragment.SngItem> mList;
    private Button mPlayButton;

    private Playlst mPlaylist;




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

        mPlaylistId = getArguments().getString(ViewPlaylistActivity.TAG_ID);

    }


    @Override
    protected int getHeaderXml() {
        return R.layout.view_playlist_header;
    }


    private boolean unAuthed = false;

    @Override
    protected void updateList() {
        if (unAuthed && SpotifyApiController.getAuthState() == WMusicProvider.AuthState.LoggedIn) {
            unAuthed = false;
            init();
        }
        super.updateList();
    }

    private void init() {
        if (SpotifyApiController.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
            unAuthed = true;
            mPlayButton.setVisibility(View.GONE);
            return;
        }

        mPlaylist = SpotifyApiController.mPlaylstCache.get(mPlaylistId);
        mPlayButton.setVisibility(View.VISIBLE);

        if (mPlaylist != null) {
            mList = new ArrayList<>();
            mPageLoadedCount = 0;

            loadData(0);
        }

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

        if (SpotifyApiController.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
            return;
        }
        // TODO this ought to cache the songs or something in case this is fragment is closed and reopened.

        setRefreshing(true);
        mPageIsLoading = true;


        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, offset);
        options.put(SpotifyService.LIMIT, getPageSize());


        SpotifyApiController.getTempApi().getPlaylistTracks(mPlaylist.owner.id, mPlaylist.id, options, new Callback<Pager<PlaylistTrack>>() {
            @Override
            public void success(Pager<PlaylistTrack> ptp, Response response) {

                ArrayList<SngItem> ss = new ArrayList<>();

                Log.d("ggg", "limit " + ptp.limit);
                Log.d("ggg", "offset  " + ptp.offset);
                Log.d("ggg", "total  " + ptp.total);

                for (PlaylistTrack pt : ptp.items) {
                    ss.add(new SngItem(new Sng(pt.track), SngItem.Type.NotInQueue));
                }

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

    @Override
    protected int getPageSize() {
        return 100;
    }

    @Override
    protected String checkIfBad() {
        if (WPlayer.getState() == WPlayer.WPlayerState.Off) {
            return "Player is off";
        } else if (SpotifyApiController.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
            return "No Spotify account found";
        } else if (mPageIsLoading) {
            return "loading";
        } else if (mList == null) {
            return "unable to load playlist";
        } else if (mList.size() <= 0) {
            return "playlist is empty";
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

}

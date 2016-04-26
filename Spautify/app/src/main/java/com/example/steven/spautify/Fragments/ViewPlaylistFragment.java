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

        if (mPlaylst != null) {
            mList = new ArrayList<>();
            mPageLoadedCount = 0;

            loadData(0);
        } else {
            // TODO create the getters in Playlst
            Log.e("ViewPlaylistFragment", "Playlst is not in cache, can't render it");
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

        if (mSource == Source.Spotify) {
            loadDataSpotify(offset);
        }


    }


    private void loadDataSpotify(int offset) {

        if (SpotifyApi.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
            return;
        }
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


}

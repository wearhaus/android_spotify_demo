//package com.example.steven.spautify.fragments;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//
//import com.example.steven.spautify.R;
//import com.example.steven.spautify.musicplayer.Sng;
//import com.example.steven.spautify.musicplayer.SpotifyApi;
//import com.example.steven.spautify.musicplayer.WMusicProvider;
//import com.example.steven.spautify.musicplayer.WPlayer;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import kaaes.spotify.webapi.android.SpotifyService;
//import kaaes.spotify.webapi.android.models.Album;
//import kaaes.spotify.webapi.android.models.Pager;
//import kaaes.spotify.webapi.android.models.Track;
//import retrofit.Callback;
//import retrofit.RetrofitError;
//import retrofit.client.Response;
//
///**
// * Created by Steven on 2/10/2016.
// */
//public class ViewAlbumFragment extends MusicLibFragment {
//
//    private String mAlbumId;
//    private ArrayList<SngItem> mList;
//    private Button mPlayButton;
//    private boolean mSetActivityTitle = false;
//
//    private Album mAlbum;
//
//    @Override
//    public MusicLibType getMusicLibType() {
//        return MusicLibType.SongInLibAlbum;
//    }
//
//    @Override
//    protected boolean showArtwork() {
//        return false;
//    }
//
//    /** The proper way to create a new Fragment with a passed arg. */
//    public static ViewAlbumFragment newInstance(String id) {
//        ViewAlbumFragment frag = new ViewAlbumFragment();
//
//        Bundle args = new Bundle();
//        args.putString(TAG_ID, id);
//        frag.setArguments(args);
//
//        return frag;
//    }
//
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        mAlbumId = getArguments().getString(MusicLibFragment.TAG_ID);
//    }
//
//    private boolean unAuthed = false;
//
//    @Override
//    protected void updateList() {
//        if (unAuthed && SpotifyApi.getAuthState() == WMusicProvider.AuthState.LoggedIn) {
//            unAuthed = false;
//            init();
//        }
//        super.updateList();
//    }
//
//    private void init() {
//        if (SpotifyApi.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
//            unAuthed = true;
//            return;
//        }
//
//        mAlbum = SpotifyApi.mAlbumCache.get(mAlbumId);
//
//        if (mAlbum != null) {
//            mList = new ArrayList<>();
//            loadData(0);
//        } else {
//
//            SpotifyApi.getTempApi().getAlbum(mAlbumId, new Callback<Album>() {
//                @Override
//                public void success(Album album, Response response) {
//                    mList = new ArrayList<>();
//                    mAlbum = album;
//
//                    loadData(0);
//                    if (mSetActivityTitle) {
//                        Activity act = getActivity();
//                        if (act != null) {
//                            getActivity().setTitle("Album:" + mAlbum.name);
//                        }
//                    }
//                }
//                @Override
//                public void failure(RetrofitError error) {
//
//                }
//            });
//        }
//    }
//
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//
//        init();
//    }
//
//
//    @Override
//    protected int getHeaderXml() {
//        return R.layout.view_playlist_header;
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = super.onCreateView(inflater, container, savedInstanceState);
//
//        mPlayButton = (Button) view.findViewById(R.id.play_button);
//
//
//        mPlayButton.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        ArrayList<Sng> songlist = new ArrayList<>();
//
//                        for (SngItem si : mList) {
//                            songlist.add(si.sng);
//                        }
//
//                        WPlayer.playManyClearQueue(songlist);
//
//                        //WPlayer.playpause();
//                    }
//                });
//
//        return view;
//    }
//
//
//
//    @Override
//    protected List getList() {
//        return mList;
//    }
//
//    @Override
//    protected String checkIfBad() {
//        if (WPlayer.getState() == WPlayer.WPlayerState.Off) {
//            return "Player is off";
//        } else if (SpotifyApi.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
//            return "No Spotify account found";
//        } else if (!mPageIsLoading && mList == null) {
//            return "unable to load album";
//        } else if (!mPageIsLoading && mList.size() <= 0) {
//            return "album is empty";
//        }
//        return null;
//    }
//
//
//    @Override
//    protected boolean paginated() {
//        return true;
//    }
//
//    protected int getPageSize() {
//        return 20;
//    }
//
//    @Override
//    protected void loadData(int offset) {
//        Log.d("ggg", "loadData  " + offset);
//
//        if (SpotifyApi.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
//            return;
//        }
//
//        Activity act = getActivity();
//        if (act!= null) act.setTitle("Album: " + mAlbum.name);
//
//        // TODO this ought to cache the songs or something in case this is fragment is closed and reopened.
//
//        setLoading(true);
//        mPageIsLoading = true;
//
//
//        Map<String, Object> options = new HashMap<>();
//        options.put(SpotifyService.OFFSET, offset);
//        options.put(SpotifyService.LIMIT, getPageSize());
//
//
//        SpotifyApi.getTempApi().getAlbumTracks(mAlbumId, options, new Callback<Pager<Track>>() {
//            @Override
//            public void success(Pager<Track> ptp, Response response) {
//
//                ArrayList<SngItem> ss = new ArrayList<>();
//
//                Log.d("ggg", "limit " + ptp.limit);
//                Log.d("ggg", "offset  " + ptp.offset);
//                Log.d("ggg", "total  " + ptp.total);
//
//                for (Track pt : ptp.items) {
//                    pt.album = mAlbum; // since it isn't reloaded every time for us
//                    ss.add(new SngItem(new Sng(pt), SngItem.Type.NotInQueue));
//                }
//
//                mPageLoadedCount = ptp.offset + ptp.limit;
//                mPageTotalAbleToBeLoaded = ptp.total;
//
//                setLoading(false);
//                mPageIsLoading = false;
//
//                // TODO is it better to add to list then just 'change' list
//                // or should we call mRecyclerAdapter.add?  What ismore efficient?
//                mList.addAll(ss);
//                updateList();
//
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Log.e("failure", error.toString());
//
//                setLoading(false);
//                mPageIsLoading = false;
//            }
//        });
//
//
//
//    }
//
//}

//package com.example.steven.spautify.fragments;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.example.steven.spautify.R;
//import com.example.steven.spautify.musicplayer.Playlst;
//import com.example.steven.spautify.musicplayer.SCRetrofitService;
//import com.example.steven.spautify.musicplayer.Sng;
//import com.example.steven.spautify.musicplayer.SoundCloudApi;
//import com.example.steven.spautify.musicplayer.Source;
//import com.example.steven.spautify.musicplayer.SpotifyApi;
//import com.example.steven.spautify.musicplayer.WMusicProvider;
//import com.example.steven.spautify.musicplayer.WPlayer;
//import com.squareup.picasso.Picasso;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.Unbinder;
//import kaaes.spotify.webapi.android.SpotifyService;
//import kaaes.spotify.webapi.android.models.Pager;
//import kaaes.spotify.webapi.android.models.PlaylistTrack;
//import retrofit.Callback;
//import retrofit.RetrofitError;
//import retrofit.client.Response;
//import retrofit2.Call;
//
///**
// * Created by Steven on 2/10/2016.
// *
// * It is an error to not have a TAG_ID argument
// */
//public class ViewPlaylistFragment extends MusicLibFragment {
//
//    private String mPlaylstId;
//    private Source mSource;
//    private ArrayList<SngItem> mList;
//
//    @BindView(R.id.play_button)             Button mPlayButton;
//    @BindView(R.id.img)                     ImageView mImageView;
//    @BindView(R.id.artist_title)            TextView mArtistTitleView;
//    @BindView(R.id.artist_name)             TextView mArtistNameView;
//
//    private Playlst mPlaylst;
//    private Unbinder mUnbinder;
//
//    @Override
//    public MusicLibType getMusicLibType() {
//        return MusicLibType.Song;
//    }
//
//    // TODO if we detect WPlayer's autosource matches our mPlaylstId, then display that here and mark the current song
//
//
//
//
//    /** The proper way to create a new Fragment with a passed arg. */
//    public static ViewPlaylistFragment newInstance(String id) {
//        ViewPlaylistFragment frag = new ViewPlaylistFragment();
//
//        Bundle args = new Bundle();
//        args.putString(TAG_ID, id);
//        frag.setArguments(args);
//
//        return frag;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mPlaylstId = getArguments().getString(TAG_ID);
//        mSource = Source.getSource(mPlaylstId);
//    }
//
//
//    @Override
//    protected int getHeaderXml() {
//        return R.layout.view_artist_header;
//    }
//
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
//        if (mSource == Source.Spotify && SpotifyApi.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
//            unAuthed = true;
//            mPlayButton.setVisibility(View.GONE);
//            return;
//        }
//
//        mPlaylst = Playlst.mPlaylstCache.get(mPlaylstId);
//        mPlayButton.setVisibility(View.VISIBLE);
//
//
//        mList = new ArrayList<>();
//        mPageLoadedCount = 0;
//
//        refreshHeaderUI();
//
//        loadData(0);
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
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = super.onCreateView(inflater, container, savedInstanceState);
//
//        mUnbinder = ButterKnife.bind(this, view);
//
//        init();
//
//        return view;
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        mUnbinder.unbind();
//    }
//
//    .
//    public void playAll(View v) {
//
//        ArrayList<Sng> songlist = new ArrayList<>();
//
//        for (SngItem si : mList) {
//            songlist.add(si.sng);
//        }
//
//        WPlayer.playManyClearQueue(songlist);
//
//        //WPlayer.playpause();
//    }
//
//
//    private void refreshHeaderUI() {
//        // this never fires off any loads, it just reacts to current data
//        if (mPlaylst == null) {
//            mPlayButton.setVisibility(View.INVISIBLE);
//            mArtistNameView.setVisibility(View.INVISIBLE);
//            mArtistTitleView.setVisibility(View.INVISIBLE);
//            mImageView.setVisibility(View.INVISIBLE);
//
//        } else {
//            mPlayButton.setVisibility(View.VISIBLE);
//            mArtistNameView.setVisibility(View.VISIBLE);
//            mArtistTitleView.setVisibility(View.VISIBLE);
//            mImageView.setVisibility(View.VISIBLE);
//
//            if (mSource == Source.Spotify) {
//                mArtistTitleView.setText("Spotify Artist");
//            } else if (mSource == Source.Soundcloud) {
//                mArtistTitleView.setText("SoundCloud User");
//            }
//
//            mArtistNameView.setText(mPlaylst.name);
//            if (mPlaylst.artworkUrl != null) {
//                Picasso.with(getActivity())
//                        .load(mPlaylst.artworkUrlHighRes)
//                        .placeholder(R.drawable.arc_guest_g)
//                        .error(R.drawable.arc_guest_g)
//                        .into(mImageView);
//            } else {
//                mImageView.setImageResource(0);
//            }
//
//
//            Activity act = getActivity();
//            if (act != null) {
//                act.setTitle("Playlist: " + mPlaylst.name);
//            }
//
//        }
//
//
//    }
//
//
//
//    @Override
//    protected boolean paginated() {
//        return true;
//    }
//
//    @Override
//    protected void loadData(int offset) {
//        Log.d("ggg", "loadData  " + offset);
//
//        if (mSource == Source.Spotify) {
//            loadDataSpotify(offset);
//        } else if (mSource == Source.Soundcloud) {
//            loadDataSoundCloud(offset);
//        }
//
//
//    }
//
//
//    private void loadDataSoundCloud(int offset) {
//
//        if (mPlaylst == null || mPlaylst.soundcloudObject.tracks == null || mPlaylst.soundcloudObject.track_count > mPlaylst.soundcloudObject.tracks.size()) {
//            Log.e("loadDataSoundcloud", "Dont have all soundcloud track data in object");
//
//            // NOTE: http://stackoverflow.com/questions/36360202/soundcloud-api-urls-timing-out-and-then-returning-error-403-on-about-50-of-trac
//            // Tracks, anytime, can be hidden or deleted or made private, so they may return a 403 error
//            // This behavior here also assumes that we cant paginate playlist content.
//
//            setLoading(true);
//            mPageIsLoading = true;
//
//            Map<String, String> options = new HashMap<>();
//            options.put(SCRetrofitService.CLIENT_ID, SoundCloudApi.CLIENT_ID);
//            options.put(SCRetrofitService.OFFSET, ""+offset);
//            options.put(SCRetrofitService.LIMIT, ""+getPageSize()); // dont need to say paginated here it seems
//
//            Call<SoundCloudApi.PlaylistJson> ccc = SoundCloudApi.getApiService().getPlaylist(mPlaylst.soundcloudObject.id, options);
//            ccc.enqueue(new retrofit2.Callback<SoundCloudApi.PlaylistJson>() {
//                @Override
//                public void onResponse(Call<SoundCloudApi.PlaylistJson> call, retrofit2.Response<SoundCloudApi.PlaylistJson> response) {
//                    mPlaylst = new Playlst(response.body());
//                    ArrayList<SngItem> ss = new ArrayList<>();
//                    // tracks are not numbered, so we have to be careful about adding them
//                    for (SoundCloudApi.TrackJson tj : response.body().tracks) {
//                        if (tj != null) {  //  && tj.streamable
//                            ss.add(new SngItem(new Sng(tj), SngItem.Type.NotInQueue));
//                            //Sng.cacheSng(
//                        } else {
//                            String g = null; g.length();
//                            // use https://api.soundcloud.com/playlists/10205283?limit=100&offset=0&client_id=5916491062a0fd0196366d76c22ac36e
//                            //ss.add(new SngItem(new Sng(), SngItem.Type.NotInQueue));
//                            // when streamable is false,
//                            Log.e("failure", "track was null/incomplete in soundcloud GetPlaylist.  Ist it private/region locked/removed?");
//                        }
//                    }
//                    //TODO cache, also note that we are not going to cache the playlist obj with the song objects
//
//
//                    setLoading(false);
//                    mPageIsLoading = false;
//
//                    mList.addAll(ss);
//                    mPageLoadedCount = mList.size();
//                    mPageTotalAbleToBeLoaded = mPlaylst.soundcloudObject.track_count;
//                    refreshHeaderUI();
//                    updateList();
//                }
//
//                @Override
//                public void onFailure(Call<SoundCloudApi.PlaylistJson> call, Throwable t) {
//                    Log.e("failure", ""+t);
//                    setLoading(false);
//                    mPageIsLoading = false;
//                }
//            });
//
//
//        } else {
//
//            // discoverd problem was that representation was ignored unless the slash was between palylists and ?
//            // strange how it didnt break any other paramsl only representation..
//
//
//            ArrayList<SngItem> ss = new ArrayList<>();
//            for (SoundCloudApi.TrackJson tj : mPlaylst.soundcloudObject.tracks) {
//                ss.add(new SngItem(new Sng(tj), SngItem.Type.NotInQueue));
//            }
//            //TODO cache
//
//            mList.addAll(ss);
//            mPageLoadedCount = mList.size();
//            mPageTotalAbleToBeLoaded = mPlaylst.soundcloudObject.track_count;
//            setLoading(false);
//            mPageIsLoading = false;
//
//
//            updateList();
//
//        }
//    }
//    private void loadDataSpotify(int offset) {
//
//        if (SpotifyApi.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
//            return;
//        }
//        if (mPlaylst == null) return; // TODO
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
//        SpotifyApi.getTempApi().getPlaylistTracks(mPlaylst.spotifyObject.owner.id, mPlaylst.spotifyId, options, new Callback<Pager<PlaylistTrack>>() {
//            @Override
//            public void success(Pager<PlaylistTrack> ptp, Response response) {
//
//                ArrayList<SngItem> ss = new ArrayList<>();
//
//                Log.d("ggg", "limit " + ptp.limit);
//                Log.d("ggg", "offset  " + ptp.offset);
//                Log.d("ggg", "total  " + ptp.total);
//
//                for (PlaylistTrack pt : ptp.items) {
//                    ss.add(new SngItem(new Sng(pt.track), SngItem.Type.NotInQueue));
//                }
//                // TODO cache
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
//    }
//
//
//
//    @Override
//    protected List getList() {
//        return mList;
//    }
//
//    protected int getPageSize() {
//        return 100;
//    }
//
//    @Override
//    protected String checkIfBad() {
//        if (WPlayer.getState() == WPlayer.WPlayerState.Off) {
//            return "Player is off";
//        } else if (mSource == Source.Spotify && SpotifyApi.getAuthState() != WMusicProvider.AuthState.LoggedIn) {
//            return "No Spotify account found";
//        }
//        // No, we want the refreshing to tell us it's loading
////        else if (mPageIsLoading) {
////            return "loading";
////        }
//        else if (!mPageIsLoading && mList == null) {
//            return "unable to load playlist";
//        } else if (!mPageIsLoading && mList.size() <= 0) {
//            return "playlist is empty";
//        }
//        return null;
//    }
//
//}

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
//import com.example.steven.spautify.musicplayer.Artst;
//import com.example.steven.spautify.musicplayer.SCRetrofitService;
//import com.example.steven.spautify.musicplayer.Sng;
//import com.example.steven.spautify.musicplayer.SoundCloudApi;
//import com.example.steven.spautify.musicplayer.Source;
//import com.example.steven.spautify.musicplayer.SpotifyApi;
//import com.example.steven.spautify.musicplayer.WPlayer;
//import com.squareup.picasso.Picasso;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import kaaes.spotify.webapi.android.models.Artist;
//import kaaes.spotify.webapi.android.models.Track;
//import kaaes.spotify.webapi.android.models.Tracks;
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
//public class ViewArtstFragment extends MusicLibFragment {
//
//    private String mArtstId;
//    private Source mSource;
//    private ArrayList<SngItem> mList;
//    private Button mPlayButton;
//    private ImageView mImageView;
//    private TextView mArtistTitleView;
//    private TextView mArtistNameView;
//
//    private Artst mArtst;
//
//    @Override
//    public MusicLibType getMusicLibType() {
//        return MusicLibType.SongInLibArtst;
//    }
//
//    // TODO if we detect WPlayer's autosource matches our mARtistId, then display that here and mark the current song
//
//    // TODO this doesn't support Spotify Artists not being filled out or null.  This should be fixed.
//
//
//    /** The proper way to create a new Fragment with a passed arg. */
//    public static ViewArtstFragment newInstance(String id) {
//        ViewArtstFragment frag = new ViewArtstFragment();
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
//        mArtstId = getArguments().getString(TAG_ID);
//        mSource = Source.getSource(mArtstId);
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
//        if (unAuthed && mSource.isPlaybackAuthed()) {
//            unAuthed = false;
//            init();
//        }
//        super.updateList();
//    }
//
//    private void refreshArtistUI() {
//        // this never fires off any loads, it just reacts to current data
//
//        if (mArtst == null) {
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
//            mArtistNameView.setText(mArtst.name);
//            if (mArtst.artworkUrl != null) {
//                Picasso.with(getActivity())
//                        .load(mArtst.artworkUrlHighRes)
//                        .placeholder(R.drawable.arc_guest_g)
//                        .error(R.drawable.arc_guest_g)
//                        .into(mImageView);
//            } else {
//                mImageView.setImageResource(0);
//            }
//
//            mPlayButton.setOnClickListener(
//                    new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            ArrayList<Sng> songlist = new ArrayList<>();
//                            for (SngItem si : mList) {
//                                songlist.add(si.sng);
//                            }
//                            WPlayer.playManyClearQueue(songlist);
//                            //WPlayer.playpause();
//                        }
//                    });
//
//            Activity act = getActivity();
//            if (act != null) {
//                act.setTitle("Artist: " + mArtst.name);
//            }
//
//        }
//
//
//    }
//
//    private void init() {
//        if (!mSource.isPlaybackAuthed()) {
//            unAuthed = true;
//            mArtst = null;
//            refreshArtistUI();
//            return;
//        }
//
//        mArtst = Artst.mArtstCache.get(mArtstId);
//
//        refreshArtistUI();
//
//        mList = new ArrayList<>();
//        mPageLoadedCount = 0;
//
//        loadData(0);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = super.onCreateView(inflater, container, savedInstanceState);
//
//        mPlayButton = (Button) view.findViewById(R.id.play_button);
//        mArtistNameView = (TextView) view.findViewById(R.id.artist_name);
//        mArtistTitleView = (TextView) view.findViewById(R.id.artist_title);
//        mImageView = (ImageView) view.findViewById(R.id.img);
//
//        init();
//
//        return view;
//    }
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
//        if (!mSource.isPlaybackAuthed()) return;
//
//        if (mSource == Source.Spotify) {
//            if (mArtst == null || mArtst.spotifyArtistFull == null) {
//                loadArtstSpotify();
//            } else {
//                loadDataSpotify(offset);
//            }
//
//        } else if (mSource == Source.Soundcloud) {
//            if (mArtst == null) {
//                loadArtstSoundCloud();
//            } else {
//                loadDataSoundCloud(offset);
//            }
//        }
//
//
//
//    }
//
//    private void loadArtstSoundCloud() {
//        setLoading(true);
//        mPageIsLoading = true;
//
//        Map<String, String> options = new HashMap<>();
//        options.put(SCRetrofitService.CLIENT_ID, SoundCloudApi.CLIENT_ID);
//        int id = Integer.parseInt(Source.get3rdPartyId(mArtstId));
//
//        Call<SoundCloudApi.UserJson> ccc = SoundCloudApi.getApiService().getUser(id, options);
//        ccc.enqueue(new retrofit2.Callback<SoundCloudApi.UserJson>() {
//            @Override
//            public void onResponse(Call<SoundCloudApi.UserJson> call, retrofit2.Response<SoundCloudApi.UserJson> response) {
//                mArtst = new Artst(response.body());
//                refreshArtistUI();
//
//                // Start real load data
//                loadDataSoundCloud(0);
//            }
//
//            @Override
//            public void onFailure(Call<SoundCloudApi.UserJson> call, Throwable t) {
//                Log.e("failure", ""+t);
//                setLoading(false);
//                mPageIsLoading = false;
//            }
//        });
//
//    }
//
//    private void loadArtstSpotify() {
//        setLoading(true);
//        mPageIsLoading = true;
//
//
//
//        SpotifyApi.getTempApi().getArtist(Source.get3rdPartyId(mArtstId), new Callback<Artist>() {
//            @Override
//            public void success(Artist aaa, Response response) {
//                mArtst = new Artst(aaa);
//                refreshArtistUI();
//                loadDataSpotify(0);
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Log.e("failure", error.toString());
//                setLoading(false);
//                mPageIsLoading = false;
//            }
//        });
//
//    }
//
//
//    private void loadDataSoundCloud(int offset) {
//
//        setLoading(true);
//        mPageIsLoading = true;
//        // soundcloud offers a track_count int
//
//        Map<String, String> options = new HashMap<>();
//        options.put(SCRetrofitService.CLIENT_ID, SoundCloudApi.CLIENT_ID);
//        options.put(SCRetrofitService.OFFSET, ""+offset);
//        options.put(SCRetrofitService.LIMIT, ""+getPageSize()); // dont need to say paginated here it seems
//        options.put(SCRetrofitService.PAGINATE, "1");
//        int id = Integer.parseInt(Source.get3rdPartyId(mArtstId));
//
//        Call<SoundCloudApi.PagedTrackJson> ccc = SoundCloudApi.getApiService().getUserTracks(id, options);
//        ccc.enqueue(new retrofit2.Callback<SoundCloudApi.PagedTrackJson>() {
//            @Override
//            public void onResponse(Call<SoundCloudApi.PagedTrackJson> call, retrofit2.Response<SoundCloudApi.PagedTrackJson> response) {
//                ArrayList<SngItem> ss = new ArrayList<>();
//                // tracks are not numbered, so we have to be careful about adding them
//                for (SoundCloudApi.TrackJson tj : response.body().collection) {
//                    ss.add(new SngItem(new Sng(tj), SngItem.Type.NotInQueue));
//                }
//                //TODO cache
//
//                setLoading(false);
//                mPageIsLoading = false;
//
//                mList.addAll(ss);
//                mPageLoadedCount = mList.size();
//                mPageTotalAbleToBeLoaded = mArtst.soundCloudJson.track_count;
//                updateList();
//            }
//
//            @Override
//            public void onFailure(Call<SoundCloudApi.PagedTrackJson> call, Throwable t) {
//                Log.e("failure", ""+t);
//                setLoading(false);
//                mPageIsLoading = false;
//            }
//        });
//
//
//
//
//
//
//    }
//    private void loadDataSpotify(final int offset) {
//
//        setLoading(true);
//        mPageIsLoading = true;
//
//
////        Map<String, Object> options = new HashMap<>();
////        options.put(SpotifyService.OFFSET, offset);
////        options.put(SpotifyService.LIMIT, getPageSize());
//
//        // Top tracks is not paginated.  To get more, we would load from albums
//
//
//        SpotifyApi.getTempApi().getArtistTopTrack(Source.get3rdPartyId(mArtstId), "US", new Callback<Tracks>() {
//            @Override
//            public void success(Tracks ts, Response response) {
//                ArrayList<SngItem> ss = new ArrayList<>();
//
//                for (Track pt : ts.tracks) {
//                    ss.add(new SngItem(new Sng(pt), SngItem.Type.NotInQueue));
//                }
//                // TODO cache
//
//                mPageLoadedCount = offset + getPageSize();
//                mPageTotalAbleToBeLoaded = ts.tracks.size();
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
//        } else if (mSource.isPlaybackAuthed()) {
//            return mSource.isPlaybackAuthedErrorString();
//        } else if (!mPageIsLoading && mList == null) {
//            return "unable to load playlist";
//        } else if (!mPageIsLoading && mList.size() <= 0) {
//            return "playlist is empty";
//        }
//        return null;
//    }
//
//}

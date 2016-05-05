package com.example.steven.spautify.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.steven.spautify.MusicLibHeader;
import com.example.steven.spautify.R;
import com.example.steven.spautify.musicplayer.Artst;
import com.example.steven.spautify.musicplayer.Playlst;
import com.example.steven.spautify.musicplayer.SCRetrofitService;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.SoundCloudApi;
import com.example.steven.spautify.musicplayer.Source;
import com.example.steven.spautify.musicplayer.SpotifyApi;
import com.example.steven.spautify.musicplayer.WPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit2.Call;

/**
 * Created by Steven on 2/10/2016.
 *
 * For viewing things that contain lists of songs
 */
public class ViewSongsFragment extends MusicLibFragment {

    /** Still contains source prefix*/
    protected String mId;
    protected Source mSource;
    private ArrayList<SngItem> mList;


    private Object mObject;
    protected MusicLibHeader mHeader;
    protected MusicLibType mObjType;
    protected MusicLibType mSongType;

    private boolean unAuthed = false;

    @Override
    public MusicLibType getMusicLibType() {
        return mSongType;
    }

    @Override
    protected int getHeaderXml() {
        return R.layout.music_lib_paginated_header;
    }

    // TODO if we detect WPlayer's autosource matches our mId, then display that here and mark the current song


    /** CAll from any child during their static newInstance */
    public static ViewSongsFragment newInstance(String id, int objTypeInt) {
        ViewSongsFragment frag = new ViewSongsFragment();
        Bundle args = new Bundle();

        args.putString(TAG_ID, id);
        args.putInt(TAG_TYPE_ORDINAL, objTypeInt);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getArguments().getString(TAG_ID);
        mObjType = MusicLibType.values()[getArguments().getInt(TAG_TYPE_ORDINAL)];
        switch (mObjType) {
            case Song:
            case SongInQueue:
            case SongInLibAlbum:
            case SongInLibArtst:
                throw new RuntimeException("Bad object type.  Songs can't contain lists of songs");
            case Playlist:
                mSource = Source.getSource(mId);
                mSongType = MusicLibType.Song;
                break;
            case Artist:
                mSource = Source.getSource(mId);
                mSongType = MusicLibType.SongInLibArtst;
                break;
            case Album:
                mSource = Source.Spotify; // TODO once album object exists, then mId will include the prefix for source
                mSongType = MusicLibType.SongInLibAlbum;
                break;
        }
        //mSource = Source.getSource(mId);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mHeader = (MusicLibHeader) view.findViewById(R.id.header);
        mHeader.setPlayAll(new MusicLibHeader.PlayAll() {
               @Override
               public void playAll() {
                   // clone list so modifications here dont affect queue
                   ArrayList < Sng > songlist = new ArrayList<>();
                   for (SngItem si : mList) {
                       songlist.add(si.sng);
                   }
                   WPlayer.playManyClearQueue(songlist);
               }
           });

        init();

        return view;
    }




    @Override
    protected void updateList() {
        if (unAuthed && mSource.isPlaybackAuthed()) {
            unAuthed = false;
            init();
        }
        super.updateList();
    }


    private void init() {
        if (!mSource.isPlaybackAuthed()) {
            unAuthed = true;
            mObject = null;
            updateHeader();
            return;
        }

        switch (mObjType) {
            case Playlist:
                mObject = Playlst.mPlaylstCache.get(mId);
                break;
            case Artist:
                mObject = Artst.mArtstCache.get(mId);  break;
            case Album:
                // For albums, it'll be spotifyId for albums, no source prefix or spotify uri stuff
                // TODO once Album object exists, this will change
                mObject = SpotifyApi.mAlbumCache.get(mId);  break;
        }

        updateHeader();

        mList = new ArrayList<>();
        mPageLoadedCount = 0;

        loadData(0);
    }

    private void updateHeader() {
        String title = null;
        switch (mObjType) {
            case Playlist:
                if (mObject != null) {
                    mHeader.updatePlaylst((Playlst) mObject);
                    title = "Playlist: " + ((Playlst) mObject).name;
                } else {
                    mHeader.updateNull();
                    title = "Playlist";
                }
                break;
            case Artist:
                if (mObject != null) {
                    mHeader.updateArtst((Artst) mObject);
                    title = "Artist: " + ((Artst) mObject).name;
                } else {
                    mHeader.updateNull();
                    title = "Artist";
                }
                break;
            case Album:
                if (mObject != null) {
                    mHeader.updateAlbum((Album) mObject);
                    title = "Album: " + ((Album) mObject).name;
                } else {
                    mHeader.updateNull();
                    title = "Album";
                }
                break;
        }

        Activity act = getActivity();
        if (act != null) {
            act.setTitle(title);
        }
    }



    @Override
    protected boolean paginated() {
        return true;
    }

    @Override
    protected void loadData(int offset) {
        Log.d("ggg", "loadData  " + offset);

        if (!mSource.isPlaybackAuthed()) return;


        boolean loadNew = mObject == null;
        if (mSource == Source.Spotify && mObjType == MusicLibType.Artist) {
            loadNew = (mObject == null || ((Artst) mObject).spotifyArtistFull == null);
        }

        if (loadNew) {
            if (mSource == Source.Spotify) {
                loadNewObjectSpotify();
            } else if (mSource == Source.Soundcloud) {
                loadNewObjectSoundCloud();
            }
        } else {

            if (mSource == Source.Spotify) {
                loadDataSpotify(offset);
            } else if (mSource == Source.Soundcloud) {
                loadDataSoundCloud(offset);
            }
        }


    }

    private void loadNewObjectSoundCloud() {
        setLoading(true);
        mPageIsLoading = true;

        Map<String, String> options = new HashMap<>();
        options.put(SCRetrofitService.CLIENT_ID, SoundCloudApi.CLIENT_ID);
        int id = Integer.parseInt(Source.get3rdPartyId(mId));


        Call ccc = SoundCloudApi.getApiService().getUser(id, options);
        switch (mObjType) {
            case Playlist:
                ccc = SoundCloudApi.getApiService().getPlaylist(id, options);
                break;
            case Artist:
                ccc = SoundCloudApi.getApiService().getUser(id, options);
                break;
            case Album:
                return;
        }




        ccc.enqueue(new retrofit2.Callback() {
            @Override
            public void onResponse(Call call, retrofit2.Response response) {
                switch (mObjType) {
                    case Playlist:
                        mObject = new Playlst(((retrofit2.Response<SoundCloudApi.PlaylistJson>) response).body());
                        break;

                    case Artist:
                        mObject = new Artst(((retrofit2.Response<SoundCloudApi.UserJson>) response).body());
                        break;

                    case Album:
                        return;
                }

                updateHeader();

                // Start real load data
                loadDataSoundCloud(0);
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.e("failure", ""+t);
                setLoading(false);
                mPageIsLoading = false;
            }
        });

    }

    private void loadNewObjectSpotify() {
        setLoading(true);
        mPageIsLoading = true;

        // will be uri for playlist
        String spotifyId = Source.get3rdPartyId(mId);



        Callback callback = new Callback() {
            @Override
            public void success(Object aaa, Response response) {
                switch (mObjType) {
                    case Playlist:
                        mObject = new Playlst((PlaylistSimple) aaa);  break;

                    case Artist:
                        mObject = new Artst((Artist) aaa);  break;

                    case Album:
                        mObject = aaa;  break;
                }
                updateHeader();
                loadDataSpotify(0);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("failure", error.toString());
                setLoading(false);
                mPageIsLoading = false;
            }
        };


        switch (mObjType) {
            case Playlist:
                String userId = Playlst.getSpotifyUserId(spotifyId);
                String playlistId = Playlst.getSpotifyPlaylistId(spotifyId);
                SpotifyApi.getTempApi().getPlaylist(userId, playlistId, callback);
                return;

            case Artist:
                SpotifyApi.getTempApi().getArtist(spotifyId, callback);
                return;

            case Album:
                // dont truncate mID until after TODO albm object created
                SpotifyApi.getTempApi().getAlbum(mId, callback);
                return;
        }




    }


    private void loadDataSoundCloud(int offset) {
        setLoading(true);
        mPageIsLoading = true;

        Map<String, String> options = new HashMap<>();
        options.put(SCRetrofitService.CLIENT_ID, SoundCloudApi.CLIENT_ID);
        options.put(SCRetrofitService.OFFSET, ""+offset);
        options.put(SCRetrofitService.LIMIT, ""+getPageSize()); // dont need to say paginated here it seems
        options.put(SCRetrofitService.PAGINATE, "1");
        int id = Integer.parseInt(Source.get3rdPartyId(mId));


        Call ccc;
        switch (mObjType) {
            case Playlist:
                ccc = SoundCloudApi.getApiService().getPlaylist(id, options);
                break;
            case Artist:
                ccc = SoundCloudApi.getApiService().getUserTracks(id, options); // SoundCloudApi.PagedTrackJson
                break;
            default:
                return;
        }


        ccc.enqueue(new retrofit2.Callback() {
            @Override
            public void onResponse(Call call, retrofit2.Response response) {
                ArrayList<SngItem> ss = new ArrayList<>();
                // got response, now cast to right type and save data into our mList

                switch (mObjType) {
                    case Playlist:
                        for (SoundCloudApi.TrackJson tj : ((retrofit2.Response<SoundCloudApi.PlaylistJson>) response).body().tracks) {
                            ss.add(new SngItem(new Sng(tj), SngItem.Type.NotInQueue));
                        }
                        mPageTotalAbleToBeLoaded = ((Playlst) mObject).soundcloudObject.track_count;
                        break;
                    case Artist:
                        for (SoundCloudApi.TrackJson tj : ((retrofit2.Response<SoundCloudApi.PagedTrackJson>) response).body().collection) {
                            ss.add(new SngItem(new Sng(tj), SngItem.Type.NotInQueue));
                        }
                        mPageTotalAbleToBeLoaded = ((Artst) mObject).soundCloudJson.track_count;
                        break;
                    default:
                        return;
                }
                //TODO cache

                setLoading(false);
                mPageIsLoading = false;

                mList.addAll(ss);
                mPageLoadedCount = mList.size();
                updateList();
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.e("failure", ""+t);
                setLoading(false);
                mPageIsLoading = false;
            }
        });






    }
    private void loadDataSpotify(final int offset) {

        setLoading(true);
        mPageIsLoading = true;


        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, offset);
        options.put(SpotifyService.LIMIT, getPageSize());

        // Top tracks is not paginated.  To get more, we would load from albums


        Callback callback = new Callback() {
            @Override
            public void success(Object ttt, Response response) {
                ArrayList<SngItem> ss = new ArrayList<>();


                switch (mObjType) {
                    case Playlist:
                        for (PlaylistTrack pt : ((Pager<PlaylistTrack>) ttt).items) {
                            ss.add(new SngItem(new Sng(pt.track), SngItem.Type.NotInQueue));
                        }
                        mPageTotalAbleToBeLoaded = ((Pager<PlaylistTrack>) ttt).total;
                        break;
                    case Artist:
                        for (Track pt : ((Tracks) ttt).tracks) {
                            ss.add(new SngItem(new Sng(pt), SngItem.Type.NotInQueue));
                        }
                        mPageTotalAbleToBeLoaded = ((Tracks) ttt).tracks.size();
                        break;
                    case Album:
                        for (Track pt : ((Pager<Track>) ttt).items) {
                            pt.album = (Album) mObject; // since it isn't reloaded every time for us
                            ss.add(new SngItem(new Sng(pt), SngItem.Type.NotInQueue));
                        }
                        mPageTotalAbleToBeLoaded = ((Pager<Track>) ttt).items.size();
                        break;
                }
                // TODO cache

                // TODO, also note this doesnt let us start list at any pos other than 0.
                mPageLoadedCount = offset + getPageSize();


                setLoading(false);
                mPageIsLoading = false;

                mList.addAll(ss);
                updateList();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("failure", error.toString());

                setLoading(false);
                mPageIsLoading = false;
            }
        };



        switch (mObjType) {
            case Playlist:
                //  returns Pager<PlaylistTrack>
                String spotifyUri = Source.get3rdPartyId(mId);
                String userId = Playlst.getSpotifyUserId(spotifyUri);
                String playlistId = Playlst.getSpotifyPlaylistId(spotifyUri);
                SpotifyApi.getTempApi().getPlaylistTracks(userId, playlistId, options, callback);
                return;
            case Artist:
                // returns Tracks, only like the top 10, not paginated.  In region US
                SpotifyApi.getTempApi().getArtistTopTrack(Source.get3rdPartyId(mId), "US", callback);
                return;
            case Album:
                // TODO when Albm object created, change
                SpotifyApi.getTempApi().getAlbumTracks(mId, options, callback);
                return;
        }



    }



    @Override
    protected List getList() {
        return mList;
    }

    protected int getPageSize() {
        return 50; // album max is 50
    }

    @Override
    protected String checkIfBad() {
        if (WPlayer.getState() == WPlayer.WPlayerState.Off) {
            return "Player is off";
        } else if (mSource.isPlaybackAuthed()) {
            return mSource.isPlaybackAuthedErrorString();
        } else if (!mPageIsLoading && mList == null) {
            return "unable to load playlist";
        } else if (!mPageIsLoading && mList.size() <= 0) {
            return "playlist is empty";
        }
        return null;
    }

}

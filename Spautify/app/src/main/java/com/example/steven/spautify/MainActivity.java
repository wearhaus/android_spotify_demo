package com.example.steven.spautify;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.Notifier;
import com.example.NotifierSimple;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.SpotifyProvider;
import com.example.steven.spautify.musicplayer.SpotifyWebApiHandler;
import com.example.steven.spautify.musicplayer.WPlayer;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity {





    //private Button mSong1;
    //private Button mSong2;
    //private Button mSong3;
    //private Button mSong4;
    //private Button mPlayPause;
    private TextView mTextView;
    //private TextView mSongText;
    //private TextView mSongText2;
    //private TextView mSongTextTime;
    private EditText mQuery;
    private Button mSearchButton;
    private View mControls;

    private ImageButton mAuthSpotify;
    //private Button mAuthSpotifyLogout;


    private RecyclerView mRecyclerView;
    private QueueAdapter mQueueAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    private MusicPlayerBar mSpotifyBar;


    // Request code that will be used to verify if the result comes from correct activity.  Can be any integer
    private static final int REQUEST_CODE = 1163;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //mSong3 = (Button) findViewById(R.id.song3);
        //mSong4 = (Button) findViewById(R.id.song4);
        //mPlayPause = (Button) findViewById(R.id.play);
        //mTextView = (TextView) findViewById(R.id.text);
        mTextView = (TextView) findViewById(R.id.text_dbg);
        //mSongText2 = (TextView) findViewById(R.id.song_text_2);
        //mSongTextTime = (TextView) findViewById(R.id.song_text_time);
        mQuery = (EditText) findViewById(R.id.ms);
        mSearchButton = (Button) findViewById(R.id.search);
        mControls =  findViewById(R.id.controls);

        mRecyclerView = (RecyclerView) findViewById(R.id.queue_recycler);
        //mRecyclerView.setHasFixedSize(true); we change list size, so can't use
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mQueueAdapter = new QueueAdapter(getQueueData());
        mRecyclerView.setAdapter(mQueueAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mQueueAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);


        mSpotifyBar = (MusicPlayerBar) findViewById(R.id.spotify_bar);


        mAuthSpotify = (ImageButton) findViewById(R.id.auth);
        //mAuthSpotifyLogout = (Button) findViewById(R.id.auth_logout);


        mAuthSpotify.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doSpotifyAuth(false);
                    }
                });

        mSearchButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createSearchResults(mQuery.getText().toString());
                    }
                });



        refreshAuthUI();
    }

    /** Brute force way to load test songs.  Shouldn't be used in production*/
    private void loadSpotifyUris(final ArrayList<String> shortIds) {
        songs = new Sng[shortIds.size()];
        gotCount = 0;

        for (int i = 0; i < shortIds.size(); i++) {
            final int n = i;
            SpotifyWebApiHandler.getTrackByUri(shortIds.get(n), new SpotifyWebApiHandler.GetTrackListener() {
                @Override
                public void gotTrack(Track track) {
                    gotCount++;
                    songs[n] = new Sng(track);
                    if (gotCount >= shortIds.size()) {
                        WPlayer.playManyClearQueue(new ArrayList<Sng>(Arrays.asList(songs)));
                    }
                }

                @Override
                public void error(String error) {
                    gotCount++;
                }
            });

        }

    }
    private Sng[] songs;
    private PlaylistSimple[] pls;
    private int gotCount;


    private void createSearchResults(final String query) {
        Log.i("createSearchResults", "Search Query: " + query);
        SpotifyWebApiHandler.getTempApi().searchTracks(query, new Callback<TracksPager>() {
            @Override
            public void success(final TracksPager trackspager, Response response) {
                for (Track t : trackspager.tracks.items) {
                    Log.d("createSearchResults", t.name + "" + ", " + t.uri + ", " + t.album.name);
                }

                final CharSequence[] items = new CharSequence[trackspager.tracks.items.size()];
                /*final Sng[] */
                songs = new Sng[trackspager.tracks.items.size()];
                int i = 0;
                for (Track t : trackspager.tracks.items) {
                    Sng s = new Sng(t);
                    items[i] = s.name + ", " + s.artistPrimary;
                    songs[i++] = s;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Search Results for '" + query + "'")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                //handleDfuFileSelected(files[item]);
                                Log.i("createSearchResults", "User chose: " + pls[item]);

                                dialog.dismiss();
                                SpotifyWebApiHandler.addTrackToCache(trackspager.tracks.items.get(item));
                                addtoQueueOrPlayNow(songs[item]);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();

            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Track failure", error.toString());
            }

        });
    }


    private void createPlaylistResults(final String query) {


        SpotifyWebApiHandler.getTempApi().getPlaylists(query, new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> psp, Response response) {
                gotPlaylists(psp);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("failure", error.toString());
            }
        });
    }

    private void createMyPlaylistResults() {

        SpotifyWebApiHandler.getTempApi().getPlaylists(SpotifyWebApiHandler.getUserId(), new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> psp, Response response) {
                gotPlaylists(psp);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("failure", error.toString());
            }
        });
    }


    private void gotPlaylists(Pager<PlaylistSimple> psp) {

        Log.i("stuffs", " " + psp.next);
        Log.i("stuffs", " " + psp.previous);
        //Log.i("stuffs", " " + userPrivate.followers); // note: followers not supported yet...

        for (PlaylistSimple i : psp.items) {
            Log.i("stuffs", " " + i.name + ",  " + i.snapshot_id + ",  " + i.owner);
        }


        /////

        final CharSequence[] items = new CharSequence[psp.items.size()];
        pls = new PlaylistSimple[psp.items.size()];
        int i = 0;
        for (PlaylistSimple p : psp.items) {
            //Sng s = new Sng(t);
            items[i] = p.name + ", " + p.owner.display_name;
            pls[i++] = p;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Results for playlists")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        //handleDfuFileSelected(files[item]);
                        Log.i("createSearchResults", "User chose: " + pls[item]);



                        // So playing a playlist that is 10,000 long (spotify max) would probably cause OOM juts loading, if not, slow UI.
                        // We may want to consider a way to load in fragments.... FIXED already, since spotify does this already.  a max of 100 songs per internet
                        // request right?  limit=20 by default, but limit=100 max, and offset is usable.
                        dialog.dismiss();
                        //SpotifyWebApiHandler.addTrackToCache(trackspager.tracks.items.get(item));
                        //addtoQueueOrPlayNow(songs[item]);


                        SpotifyWebApiHandler.getTempApi().getPlaylistTracks(pls[item].owner.id, pls[item].id, new Callback<Pager<PlaylistTrack>>() {
                            @Override
                            public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {

                                ArrayList<Sng> ss = new ArrayList<>();

                                for (PlaylistTrack pt : playlistTrackPager.items) {
                                    //SpotifyWebApiHandler.addTrackToCache(pt.track);
                                    // This will be useless if this playlist is larger than the cache can hold.

                                    ss.add(new Sng(pt.track));
                                }

                                //loadSpotifyUris(ss);
                                WPlayer.playManyClearQueue(ss);


                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.e("failure", error.toString());
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }


    private void addtoQueueOrPlayNow(final Sng song) {


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("" + song.name)
                .setPositiveButton("Play Now", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        WPlayer.playSingleClearQueue(song);
                    }
                })
                .setNeutralButton("Add to Queue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        WPlayer.addtoEndOfQueue(song);
                    }
                })
                .setNegativeButton("Cancel", null);

        AlertDialog alert = builder.create();
        alert.show();


    }

    private void onQueueItemSelected(final int position, final Sng sng) {


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("?")
                .setPositiveButton("Play", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        WPlayer.playItemInQueue(position, sng);
                    }
                })
                .setNeutralButton("Remove from queue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        WPlayer.removeFromQueue(position, sng);
                    }
                })
                .setNegativeButton("Cancel", null);

        AlertDialog alert = builder.create();
        alert.show();


    }



    private NotifierSimple.ListenerSimple mAuthListener = new NotifierSimple.ListenerSimple() {
        @Override
        public void onChange() {
            refreshAuthUI();
        }
    };

    private Notifier.Listener mPlaybackListener = new Notifier.Listener<WPlayer.Notif>() {
        @Override
        public void onChange(WPlayer.Notif type) {
            if (type == WPlayer.Notif.PlaybackAndQueue || type == WPlayer.Notif.Queue) {
                updateQueueView();
            }
        }
    };


    private void refreshAuthUI() {

        // strange order blocking player until spotify is authed
//        switch (SpotifyProvider.getAuthState()) {
//            case LoggedIn:
//                mControls.setVisibility(View.VISIBLE);
//                mAuthSpotify.setVisibility(View.GONE);
//                break;
//            default:
//                mControls.setVisibility(View.GONE);
//                mAuthSpotify.setVisibility(View.VISIBLE);
//        }

    }


    private void doSpotifyAuth(boolean forceDialog) {
//        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(SpotifyProvider.CLIENT_ID,
//                AuthenticationResponse.Type.TOKEN,
//                SpotifyProvider.REDIRECT_URI);
//        builder.setScopes(new String[]{"user-read-private", "streaming", "playlist-read-private"});
//
//        builder.setShowDialog(forceDialog); // Show dialog forces the pop-up window to always appear.  It can be used to log out of Spotify.
//        AuthenticationRequest request = builder.build();
//
//        AuthenticationClient.openLoginActivity(MainActivity.this, REQUEST_CODE, request);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d("MainActivity", "onActivityResult: " + requestCode);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            Log.d("MainActivity", "onActivityResult response:" + response.getType());
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                //mTextView.setText("Logged in");

//                SpotifyProvider.onGetAccessToken(response.getAccessToken());

                WPlayer.createPlayer();
                WPlayer.getNotifier().registerListener(mPlaybackListener);

                refreshAuthUI();


            } else if (response.getType() == AuthenticationResponse.Type.ERROR) {
                Log.e("MainActivity", " error auth" + response.getError());

                refreshAuthUI();

            }
        }
    }




    @Override
    protected void onDestroy() {
        // Spotify.destroyPlayer(this);
        // Probably need to unregister during pause and stuff.
        WPlayer.getNotifier().unregisterListener(mPlaybackListener);
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        mSpotifyBar.onActivityPaused();
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
        mSpotifyBar.onActivityResumed();
        //if (mPlayer != null) {
        //    mPlayer.getPlayerState(MainActivity.this);
        //}

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                final Intent intent = NavUtils.getParentActivityIntent(this);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                NavUtils.navigateUpTo(this, intent);
                return true;

            case R.id.action_settings:
                doSpotifyAuth(true);
                return true;


            case R.id.action_kill_player:

                return true;

            case R.id.action_list1: {
                ArrayList<String> l = new ArrayList<String>();

                // Playlist of short clips
                l.add("spotify:track:6llaeUEQcqJgaBXp5Fpw2c"); // Little Mermaid Fanfare
                l.add("spotify:track:6CgmuJattewz4Ka501hHep"); // reptar
                l.add("spotify:track:1EROCm4a4jEl21Wc34siDa"); // history of glory
                l.add("spotify:track:5BB0Jzw60KyfSTyjJqtely"); // nanana
                l.add("spotify:track:4aSAA0QQSUUX3YUiXsdtbn"); // just
                l.add("spotify:track:0NF9Cb1PDy4M3gzC0jJaaG"); // Handel
                l.add("spotify:track:6Lhq4Y3TrqoSb7Cm5xYnDb"); // Passion Pit
                l.add("spotify:track:2gzDs0aRWhNdDVvFTL28gS"); // Walk The Moon


                loadSpotifyUris(l);

                return true;
            }

            case R.id.action_list2: {
                ArrayList<String> l = new ArrayList<String>();

                // Playlist of short clips
                //l.add("spotify:track:325xUomC1NKDerrKIn0zfD"); // Goldberg Variations
                //l.add("spotify:track:6V3GWmZ1dRPwXcm4LBuCki"); // Garfunkel and Oates
                //l.add("spotify:track:7odDNol8PVK2HnEwBA2rOm"); // Grand Budapest Hotel
                l.add("spotify:track:36xN3MgBV6VgnT1Khi9b5s"); // beethoven
                l.add("spotify:track:04g3KHv3tuxR2632NMi3sh"); // oyoste aina
                l.add("spotify:track:7HwezGLNAb50XAPydwMUZI"); // dance, west side story
                //l.add("spotify:track:4Td4k0pKvX1rOm697YOzQu"); // grieg holberg
                //l.add("spotify:track:4YqFNGrhCZcDhR32rN5a1N"); // bernstein ballet var III
                //l.add("spotify:track:5YMIPOePWrWjjZUQUOriaV"); // turkish
                l.add("spotify:track:7afyyi8zn4FzWbcJ70F56V"); // penny lane
                l.add("spotify:track:2C2vGTKDOIBqll0ndVOpKW"); // chopin
                //l.add("spotify:track:6JV4vNn2wjaF7Z0A5fiMcC"); // FF Victory


                //l.add("spotify:track:3t7Yqa7lcO9gSTyKGPRIjx"); // Firebird
                l.add("spotify:track:6Z4Ln4dDI7xQqiOFJcuSRX"); // Brahms Paganini
                //l.add("spotify:track:222FCwE4QE7zrolZ270s1P"); // Back to the Future
                //l.add("spotify:track:51RXhZbTM27y8DUYuQjWG7"); // Muse Intro
                //l.add("spotify:track:12l8e8JfVOgX7jQewjyNbU"); // They Might Be Giants
                l.add("spotify:track:0TPGxWLOyxFSVRlqIViDsU"); // Chopin C Sharp Minor Prelude
                //l.add("spotify:track:6llaeUEQcqJgaBXp5Fpw2c"); // Little Mermaid Fanfare
                //l.add("spotify:track:6JV4vNn2wjaF7Z0A5fiMcC"); // FF Victory
                l.add("spotify:track:6cCGMwE6YDYcMmfdrkGkEH"); // Pictures at an Exhibition: Promenade
                l.add("spotify:track:5CJymOUq9zSLgGqwh6ToRW"); // Foster The People
                l.add("spotify:track:5h3nmlsWm9Gq12hgL3YmDI"); // The Sound Of Music
                l.add("spotify:track:1dwwuPHdbO6dVi0Xyoqdhi"); // Ravel Valse


                loadSpotifyUris(l);

                return true;
            }

            case R.id.action_playlists_search:
                createPlaylistResults(mQuery.getText().toString());
                return true;

            case R.id.action_playlists_me:
                createMyPlaylistResults();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }




    private void updateQueueView() {
        // any changes to queue call this
        mQueueAdapter.changeData(getQueueData());
    }


    private ArrayList<SngItem> getQueueData() {
        // TODO this could be more efficient
        ArrayList<SngItem> l = new ArrayList<>();

        if (WPlayer.getState() == WPlayer.State.Off) {
            return l;
        }

        for (Sng s : WPlayer.getQueueBack()) {
            l.add(new SngItem(s, SngItem.Type.QueueBack));
        }

        if (WPlayer.getCurrentSng() != null) {
            l.add(new SngItem(WPlayer.getCurrentSng(), SngItem.Type.Current));
        }
        for (Sng s : WPlayer.getQueue()) {
            l.add(new SngItem(s, SngItem.Type.Queue));
        }

        return l;
    }


    public static class QueueViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mContainer;
        public TextView mTitleView;
        public TextView mAuthorView;
        public ImageButton mImageButton;

        public QueueViewHolder(View v) {
            super(v);
            mContainer =  itemView.findViewById(R.id.container);
            mTitleView = (TextView) itemView.findViewById(R.id.track_title);
            mAuthorView = (TextView) itemView.findViewById(R.id.track_author);
            mImageButton = (ImageButton) itemView.findViewById(R.id.stuffs);

        }

    }

    static class SngItem {
        public Sng sng;
        public Type type;
        SngItem (Sng s, Type t) {
            sng = s;
            type = t;
        }

        public enum Type {
            QueueBack,
            Current,
            Queue,
        }



    }

    public class QueueAdapter extends RecyclerView.Adapter<QueueViewHolder>
            implements ItemTouchHelperAdapter {
        private ArrayList<SngItem> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder


        // Provide a suitable constructor (depends on the kind of dataset)
        public QueueAdapter(ArrayList<SngItem> dataset) {
            mDataset = dataset;
        }

        public void changeData(ArrayList<SngItem> dataset){
            mDataset = dataset;
            notifyDataSetChanged();
        }

        @Override
        public void onItemDismiss(int position) {
            mDataset.remove(position);
            notifyItemRemoved(position);
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mDataset, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mDataset, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }



        // Create new views (invoked by the layout manager)
        @Override
        public QueueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_queue_item, parent, false);
            // set the view's size, margins, paddings and layout parameters

            QueueViewHolder vh = new QueueViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(QueueViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element



            final SngItem s = mDataset.get(position);
            //final String shortId = SpotifyController.getSpotifyIdFromUri(s.sng.spotifyUri);
            //Track t = SpotifyWebApiHandler.getTrackOnlyIfCached(shortId);

            if (s.type == SngItem.Type.Current) {
                holder.mContainer.setBackgroundColor(Color.argb(50, 0, 125, 250));
            } else {
                holder.mContainer.setBackgroundColor(Color.argb(0, 0, 0, 0));
            }

            holder.mTitleView.setText("" + s.sng.name);
            holder.mAuthorView.setText("" + s.sng.artistPrimary);

            /*if (t != null) {
                holder.mTitleView.setText("" + t.name);
                holder.mAuthorView.setText("" + t.artists.get(0).name);

            } else {

                holder.mTitleView.setText("" + s.sng.spotifyUri + " -" + s.type);
                holder.mAuthorView.setText("??");

                SpotifyWebApiHandler.getTrackByUri(shortId, new SpotifyWebApiHandler.GetTrackListener() {
                    @Override
                    public void gotTrack(Track track) {
                        updateQueueView();
                    }

                    @Override
                    public void error(String error) {

                    }
                });


            }*/


            holder.mImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // We shouldn't allow add to Queue, only jump queue to this... which is a weird operation
                    // Especially considering this same Sng might appear multiple times...
                    // To be superior than spotify, we ought to preserve QueueBack

                    onQueueItemSelected(position, s.sng);

                }
            });

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }



    public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

        private final ItemTouchHelperAdapter mAdapter;

        public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }

    }

    public interface ItemTouchHelperAdapter {
        void onItemMove(int fromPosition, int toPosition);
        void onItemDismiss(int position);
    }

    // https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.5j61b2qv3





}


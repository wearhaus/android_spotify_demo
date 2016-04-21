package com.example.steven.spautify.musicplayer;

import android.content.Context;
import android.util.Log;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.PlayConfig;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Steven on 1/22/2016.
 */
public class SpotifyProvider extends WMusicProvider {
    private final static String TAG = "SpotifyProvider";



    private PlayerState mCurrentPlayerState;
    private Track mCurrentTrack;

    /** Null only when an error has occurred or not yet done initializing*/
    private Player mPlayer;

    private State mProviderState;


    public SpotifyProvider(Context c) {
        super(c);
        mProviderState = State.AuthLoading;


        Config playerConfig = new Config(c, SpotifyWebApiHandler.getAccessToken(), SpotifyWebApiHandler.CLIENT_ID);
        mPlayer = Spotify.getPlayer(playerConfig, c, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                Log.i(TAG, "onInitialized:" + player);


                mPlayer.addConnectionStateCallback(mConnStateCallback);
                mPlayer.addPlayerNotificationCallback(mPlayerNotifCallback);

                mProviderState = State.PlayerInited;
                WPlayer.fpProviderStateNotif(SpotifyProvider.this);

                //requestPlayerState();
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Could not initialize Spotify Player: " + throwable.getMessage());

                mPlayer = null;
                mProviderState = State.Error;
                WPlayer.fpProviderStateNotif(SpotifyProvider.this);
            }
        });


    }

    @Override
    boolean constructorAsync() {
        return true;
    }



    @Override
    void closeProvider(Context c) {

        if (mPlayer != null) {
            mPlayer.removeConnectionStateCallback(mConnStateCallback);
            mPlayer.removePlayerNotificationCallback(mPlayerNotifCallback);
        }
        Spotify.destroyPlayer(c);
        mPlayer = null;

    }

    @Override
    void standby() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }


    @Override
    State getProviderState() {
        return mProviderState;
    }


    @Override
    void playSong(Sng song) {
        Log.d(TAG, "Playing song: " + song.spotifyUri);
        if (mPlayer != null) {
            mPlayer.play(song.spotifyUri);
            mProviderState = State.LoadingSong;
            // TODO how does spotify notify us that its done loading?
            WPlayer.fpProviderStateNotif(this); // TODO redundant, state already set to loading by WPlayer's call to playSong
        }

    }

    @Override
    void skipToNext() {
        if (mPlayer != null) {
            mPlayer.skipToNext();
        }
    }


    @Override
    void pause() {
        Log.d(TAG, "pause");
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    @Override
    void resume() {
        Log.d(TAG, "resume");
        if (mPlayer != null) {
            mPlayer.resume();
        }
    }


    @Override
    void setPosition(int newpos) {

        PlayConfig pc = PlayConfig.createFor(mCurrentPlayerState.trackUri);
        pc.withInitialPosition(newpos);

        mPlayer.play(pc);



        //mPlayer.resume();
        //seekTo(seekBar.getProgress());
        // This sometimes has a flicker where it plays an instance of the old part of the song AND the seekbar flickers to the position it used to be.


    }

    @Override
    void requestPositionUpdate() {
        if (mPlayer != null) {
            mPlayer.getPlayerState(mPlayerStateCallBack);
        }
    }


    //////////////////////

    /** Results in a callback to mPlayerNotifCallback which can trigger our mPlaybackNotifier which is external of this class*/
    //private void requestPlayerState() {
    //    mPlayer.getPlayerState(mPlayerStateCallBack);
    //}

    private PlayerStateCallback mPlayerStateCallBack = new PlayerStateCallback() {
        @Override
        public void onPlayerState(PlayerState playerState) {
            onPlayerStateHandle(playerState);
        }
    };


    private void onPlayerStateHandle(PlayerState playerState) {
        mCurrentPlayerState = playerState;
        // TODO read spotify docs to see how to handle LoadingSong state
        if (mCurrentPlayerState.playing && mProviderState == State.LoadingSong) {
            mProviderState = State.SongReady;
            WPlayer.fpProviderStateNotif(this);
        } else {
            WPlayer.fpGotNewPositionInMs(playerState.positionInMs, this);
        }

        // Update: So we don't want to ever READ the state of Spotify, except for double checking.  Instead,

    }



    ////////////////////


    private ConnectionStateCallback mConnStateCallback = new ConnectionStateCallback() {
        @Override
        public void onLoggedIn() {
            Log.d(TAG, "User logged in");
        }

        @Override
        public void onLoggedOut() {
            Log.d(TAG, "User logged out");
        }

        @Override
        public void onLoginFailed(Throwable error) {
            Log.d(TAG, "Login failed");
        }

        @Override
        public void onTemporaryError() {
            Log.d(TAG, "Temporary error occurred");
        }

        @Override
        public void onConnectionMessage(String message) {
            Log.d(TAG, "Received connection message: " + message);
        }
    };





    private PlayerNotificationCallback mPlayerNotifCallback = new PlayerNotificationCallback() {
        @Override
        public void onPlaybackEvent(EventType eventType, com.spotify.sdk.android.player.PlayerState playerState) {
            Log.d(TAG, "Playback event received: " + eventType.name());

            Sng s;
            switch (eventType) {

                case TRACK_CHANGED:

                    // triggered by
                    //   - natural end of a song in a middle of a queue
                    //   - natural end of last song in queue (where END_OF_CONTEXT is also called)
                    //   - by skip next???
                    //   - by skip prev???
                    //   - by play/pause??
                    //   - by Player.play, when it just started
                    break;

                case END_OF_CONTEXT:
                    // End of queue hit, this is called, followed immediately by NotPlaying, TrackEnd, TrackChanged

                    WPlayer.fpEndOfSong(SpotifyProvider.this);
                    // Player handles switching spotify Uri's for us.

                case SKIP_NEXT:
                    // This automatically ends up calling END_OF_CONTEXT if there is no more in Spotify's internal queue,
                    // so we really have nothing to do here
                    break;
                case SKIP_PREV:
                    // triggered when we receive a button press for skip prev.
                    break;
            }


            onPlayerStateHandle(playerState);

            if (mProviderState == State.Error) {
                // TODO for errors that are recoverable?
                mProviderState = State.PlayerInited;
                WPlayer.fpProviderStateNotif(SpotifyProvider.this);
            }
        }

        @Override
        public void onPlaybackError(ErrorType errorType, String s) {
            Log.d(TAG, "Playback error received: " + errorType.name());

            mProviderState = State.ErrorWithCurrentSong;
            WPlayer.fpProviderStateNotif(SpotifyProvider.this);

        }
    };







}

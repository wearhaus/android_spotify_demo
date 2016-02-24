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

    private State mPlayerSetupState;


    public SpotifyProvider(Context c) {
        super(c);
        mPlayerSetupState = State.Loading;


        Config playerConfig = new Config(c, SpotifyWebApiHandler.getAccessToken(), SpotifyWebApiHandler.CLIENT_ID);
        mPlayer = Spotify.getPlayer(playerConfig, c, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                Log.i(TAG, "onInitialized:" + player);


                mPlayer.addConnectionStateCallback(mConnStateCallback);
                mPlayer.addPlayerNotificationCallback(mPlayerNotifCallback);

                mPlayerSetupState = State.PlayerReady;
                WPlayer.fpProviderState(SpotifyProvider.this, false);

                //requestPlayerState();
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Could not initialize Spotify Player: " + throwable.getMessage());

                mPlayer = null;
                mPlayerSetupState = State.Error;

                WPlayer.fpProviderState(SpotifyProvider.this, false);
            }
        });



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
        return mPlayerSetupState;
    }


    @Override
    void playSong(Sng song) {
        Log.d(TAG, "Playing song: " + song.spotifyUri);
        mPlayer.play(song.spotifyUri);
    }

    @Override
    void skipToNext() {
        mPlayer.skipToNext();
    }

    @Override
    void skipToPrevious() {
        mPlayer.skipToPrevious();
    }

    @Override
    void pause() {
        Log.d(TAG, "pause");
        mPlayer.pause();
    }

    @Override
    void resume() {
        Log.d(TAG, "resume");
        mPlayer.resume();
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
        mPlayer.getPlayerState(mPlayerStateCallBack);
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
        WPlayer.fpGotNewPositionInMs(playerState.positionInMs, this);
        //mPlaybackNotifier.notifyListeners();/////////////

        //final String trackUri = playerState.trackUri;

        // Update: So we don't want to ever READ the state of Spotify, except for double checking.  Instead,
        // we will read the state from

        /*if (trackUri != null && !trackUri.isEmpty()) {
            if (mCurrentTrack != null && !mCurrentTrack.uri.equals(trackUri)) {
                // Outdated track, we don't want this anymore
                mCurrentTrack = null;
            }

            SpotifyWebApiHandler.getTrackByUri(trackUri, new SpotifyWebApiHandler.GetTrackListener() {
                @Override
                public void gotTrack(Track track) {
                    if (track.uri.equals(trackUri)) {
                        mCurrentTrack = track;

                        mPlaybackNotifier.notifyListeners();/////////////////
                    } else {
                        mCurrentTrack = null;
                    }
                }

                @Override
                public void error(String error) {
                    mCurrentTrack = null;
                }
            });


        }*/
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
                    // End of queue hit, this is called, followed immediately by Paused, TrackEnd, TrackChanged

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

            if (mPlayerSetupState == State.Error) {
                // TODO for errors that are recoverable?
                mPlayerSetupState = State.PlayerReady;
                WPlayer.fpProviderState(SpotifyProvider.this, true);
            }
        }

        @Override
        public void onPlaybackError(ErrorType errorType, String s) {
            Log.d(TAG, "Playback error received: " + errorType.name());

            mPlayerSetupState = State.ErrorWithCurrentSong;
            WPlayer.fpProviderState(SpotifyProvider.this, false);

        }
    };







}

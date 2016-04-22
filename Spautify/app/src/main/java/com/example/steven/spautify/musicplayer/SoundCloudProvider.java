package com.example.steven.spautify.musicplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Steven on 4/19/2016.
 */
public class SoundCloudProvider extends WMusicProvider {
    private final static String TAG = "SoundCloudProvider";

    private State mPlayerSetupState;

    /** A new MediaPlayer object exists for each song, so even if 2 SoundClouds are in a row,
     * we release() the first one and create a new one for the 2nd song.*/
    private MediaPlayer mMediaPlayer;
    private boolean mMediaPlayerStarted;

    /**
     * @param c must be the Application Context
     *          //@param wp must be the WPlayer that creates and holds this
     */
    public SoundCloudProvider(Context c) {
        super(c);

        // No auth states or anything; we just stream directly
        mPlayerSetupState = State.PlayerInited;
        //WPlayer.fpProviderStateNotif(SoundCloudProvider.this, false);
    }

    @Override
    boolean constructorAsync() {
        return false;
    }

    @Override
    void standby() {
        Log.w(TAG, "standby");
        closeMediaPlayer();
    }

    @Override
    void closeProvider(Context c) {
        closeMediaPlayer();
    }

    @Override
    State getProviderState() {
        return mPlayerSetupState;
    }

    @Override
    void playSong(Sng song) {
        Log.w(TAG, " playSong ");
        // need a new media player object if song changes
        closeMediaPlayer(false); // Dont notif yet.
        createMediaPlayer();

        try {
            mMediaPlayer.setDataSource(song.soundCloudJson.stream_url + "?client_id=" + SoundCloudApiController.CLIENT_ID);
            mMediaPlayer.prepareAsync();

            mPlayerSetupState = State.LoadingSong;
            WPlayer.fpProviderStateNotif(this); // TODO redundant, state already set to loading by WPlayer's call to playSong



        } catch (IOException e) {
            e.printStackTrace();

            mPlayerSetupState = State.ErrorWithCurrentSong;
            WPlayer.fpProviderStateNotif(SoundCloudProvider.this);
        }


    }

    private void createMediaPlayer() {
        Log.w(TAG, " createMediaPlayer " + mMediaPlayer);
        if (mMediaPlayer == null) {
            mPlayerSetupState = State.PlayerInited;
            mMediaPlayerStarted = false;
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "onError");
                    mPlayerSetupState = State.ErrorWithCurrentSong;
                    WPlayer.fpProviderStateNotif(SoundCloudProvider.this);

                    mMediaPlayer.reset();
                    return true; // so it'll then immediately call onComplete if false
                    // Note: any error can be auto fixed with .reset()
                }
            });

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.i(TAG, "onCompletion");

                    mMediaPlayerStarted = false;
                    // if they want to start playing again (aka no new song in queue and they press play again)
                    // this flag is needed to be set

                    WPlayer.fpEndOfSong(SoundCloudProvider.this);
                }
            });
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i(TAG, "onPrepared");
                    mPlayerSetupState = State.SongReady;
                    WPlayer.fpProviderStateNotif(SoundCloudProvider.this);
                    mMediaPlayer.start();
                    mMediaPlayerStarted = true;
                }
            });

        }

    }

    private void closeMediaPlayer() {
        closeMediaPlayer(true);
    }
    private void closeMediaPlayer(boolean notif) {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mMediaPlayerStarted = false;
            if (notif) {
                mPlayerSetupState = State.PlayerInited;
                WPlayer.fpProviderStateNotif(this);
            }
        }
    }

    @Override
    void skipToNext() {
        closeMediaPlayer();
        WPlayer.fpEndOfSong(SoundCloudProvider.this);
    }


    @Override
    void pause() {
        if (mMediaPlayer != null && mMediaPlayerStarted) {
            mMediaPlayer.pause();
        }
    }

    @Override
    void resume() {
        if (mMediaPlayer != null) {
            if (mMediaPlayerStarted) {
                mMediaPlayer.start(); // resume if already started

            } else {
                mPlayerSetupState = State.LoadingSong;
                WPlayer.fpProviderStateNotif(this); // TODO redundant, state already set to loading by WPlayer's call to playSong

                mMediaPlayer.reset();
                mMediaPlayer.prepareAsync();

            }
        }

    }

    @Override
    void setPosition(int newpos) {
        if (mMediaPlayer != null && mMediaPlayerStarted) {
            mMediaPlayer.seekTo(newpos);
            // TODO what if it is paused?
        }
    }

    @Override
    void requestPositionUpdate() {
        if (mMediaPlayer != null && mMediaPlayerStarted) {
            WPlayer.fpGotNewPositionInMs(mMediaPlayer.getCurrentPosition(), this);
        }
    }
}

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
    private boolean  mMediaPlayerPlaying;

    /**
     * @param c must be the Application Context
     *          //@param wp must be the WPlayer that creates and holds this
     */
    public SoundCloudProvider(Context c) {
        super(c);

        Log.w(TAG, "new SoundCloudProvider");

        // No auth states or anything; we just stream directly
        mPlayerSetupState = State.PlayerReady;
        //WPlayer.fpProviderState(SoundCloudProvider.this, false);
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
        Log.w(TAG, " getProviderState " + mPlayerSetupState);
        return mPlayerSetupState;
    }

    @Override
    void playSong(Sng song) {
        Log.w(TAG, " playSong ");
        // need a new media player object if song changes
        closeMediaPlayer();
        createMediaPlayer();

        try {
            mMediaPlayer.setDataSource(song.soundCloudJson.stream_url + "?client_id=" + SoundCloudApiHandler.CLIENT_ID);
            mMediaPlayer.prepareAsync();



        } catch (IOException e) {
            e.printStackTrace();

            mPlayerSetupState = State.ErrorWithCurrentSong;
            WPlayer.fpProviderState(SoundCloudProvider.this, true);
        }


    }

    private void createMediaPlayer() {
        Log.w(TAG, " createMediaPlayer " + mMediaPlayer);
        if (mMediaPlayer == null) {
            mPlayerSetupState = State.PlayerReady;
            mMediaPlayerPlaying = false;
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "onError");
                    mPlayerSetupState = State.ErrorWithCurrentSong;
                    WPlayer.fpProviderState(SoundCloudProvider.this, true);

                    mMediaPlayer.reset();
                    return true; // so it'll then immediately call onComplete if false
                    // Note: any error can be auto fixed with .reset()
                }
            });

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.i(TAG, "onCompletion");

                    WPlayer.fpEndOfSong(SoundCloudProvider.this);
                }
            });
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i(TAG, "onPrepared");
                    mMediaPlayer.start();
                    mMediaPlayerPlaying = true;
                }
            });

        }

    }

    private void closeMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mMediaPlayerPlaying = false;
        }
    }

    @Override
    void skipToNext() {
        closeMediaPlayer();
        WPlayer.fpEndOfSong(SoundCloudProvider.this);
    }

    @Override
    void skipToPrevious() {
        // TODO
    }

    @Override
    void pause() {
        if (mMediaPlayer != null && mMediaPlayerPlaying) {
            mMediaPlayer.pause();
        }
    }

    @Override
    void resume() {
        if (mMediaPlayer != null && mMediaPlayerPlaying) {
            mMediaPlayer.start();
        }

    }

    @Override
    void setPosition(int newpos) {
        if (mMediaPlayer != null && mMediaPlayerPlaying) {
            mMediaPlayer.seekTo(newpos);
        }
    }

    @Override
    void requestPositionUpdate() {
        if (mMediaPlayer != null && mMediaPlayerPlaying) {
            Log.e(TAG, "ms: " + mMediaPlayer.getCurrentPosition());
            WPlayer.fpGotNewPositionInMs(mMediaPlayer.getCurrentPosition(), this);
        }
    }
}

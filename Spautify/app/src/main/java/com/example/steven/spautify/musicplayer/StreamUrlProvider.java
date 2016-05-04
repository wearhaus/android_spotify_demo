package com.example.steven.spautify.musicplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Steven on 4/22/2016.
 *
 * Provider that does not contain an SDK, yet provides a RESTful stream url
 *
 * Implement getStreamUrl and you're good!
 */
public abstract class StreamUrlProvider extends WMusicProvider {
    private final static String TAG = "StreamUrlProvider";


    public abstract String getStreamUrl(Sng sng);


    private State mPlayerSetupState;

    /** A new MediaPlayer object exists for each song, so even if 2 SoundClouds are in a row,
     * we release() the first one and create a new one for the 2nd song.*/
    private MediaPlayer mMediaPlayer;
    private boolean mMediaPlayerStarted;
    private boolean mErrored = false;
    /** Only stored in case we errored, for when we try again*/
    private String mUrl;
    /** Only a temp var used for requestPositionUpdate when PlaybackCompleted or SkipToNext stopped*/
    private int mSongDuration;
    private boolean mReachEndOnce;
    private Context mAppContext;

    /**
     * @param c must be the Application Context
     *          //@param wp must be the WPlayer that creates and holds this
     */
    public StreamUrlProvider(Context c) {
        super(c);
        mAppContext = c;

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
        closeMediaPlayer();
        createMediaPlayer();

        try {
            mUrl = getStreamUrl(song);
            mSongDuration = song.durationInMs;
            mReachEndOnce = false;
            mMediaPlayer.setDataSource(mAppContext, Uri.parse(mUrl));
            mMediaPlayer.prepareAsync();

            mPlayerSetupState = State.LoadingSong;
            WPlayer.fpProviderStateNotif(this); // TODO redundant, state already set to loading by WPlayer's call to playSong



        } catch (IOException e) {
            e.printStackTrace();
            mPlayerSetupState = State.ErrorWithCurrentSong;
            WPlayer.fpProviderStateNotif(StreamUrlProvider.this);
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
                    WPlayer.fpProviderStateNotif(StreamUrlProvider.this);
                    mErrored = true;

                    mMediaPlayer.reset(); // idle state, before loading
                    return true; // so it'll then immediately call onComplete if false
                    // Note: any error can be auto fixed with .reset()
                }
            });

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.i(TAG, "onCompletion");

                    mMediaPlayerStarted = false;
                    mReachEndOnce = true;
                    mMediaPlayer.stop();
                    // if they want to start playing again (aka no new song in queue and they press play again)
                    // this flag is needed to be set

                    WPlayer.fpEndOfSong(StreamUrlProvider.this);
                }
            });
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i(TAG, "onPrepared");
                    mPlayerSetupState = State.SongReady;
                    WPlayer.fpProviderStateNotif(StreamUrlProvider.this);
                    mMediaPlayer.start();
                    mMediaPlayerStarted = true;
                }
            });

        }

    }

    private void closeMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mErrored = false;
            mReachEndOnce = false;
            mMediaPlayerStarted = false;
            mUrl = null;
            mSongDuration = 0;
            mPlayerSetupState = State.PlayerInited;

            //WPlayer.fpProviderStateNotif(this);
            // This is a horrible error to call notif here since we are no longer the intended provider.
            // This resulted in a hard-to-find bug where internalPlay() was called again halfway through it's first
            // execution on the same thread (so no synchronizing would fix anything) resulting in 2 calls to createProvider
            // causing the check of prov == mCurrentProvider to fail due to 2 instances of a provider existing
        }
    }

    @Override
    void skipToNext() {
        //closeMediaPlayer();
        // Standby will close for us, here, the song is not necessarily unloaded yet.
        if (mMediaPlayer != null && mMediaPlayerStarted) {
            mMediaPlayer.stop();
            mMediaPlayerStarted = false;
            mReachEndOnce = true;
        }
        WPlayer.fpEndOfSong(StreamUrlProvider.this);
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

            } else if (mPlayerSetupState != State.LoadingSong) {
                if (mErrored) {
                    // if we errored, try loading song again.
                    try {
                        mMediaPlayer.setDataSource(mUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                        mPlayerSetupState = State.ErrorWithCurrentSong;
                        WPlayer.fpProviderStateNotif(StreamUrlProvider.this);
                    }
                } else {
                    // must've been from stop() due to skipToNext() or PlaybackCompleted, so song is loaded
                }

                mPlayerSetupState = State.LoadingSong;
                WPlayer.fpProviderStateNotif(this); // TODO redundant, state already set to loading by WPlayer's call to playSong

                mMediaPlayer.prepareAsync();
                // will result in a call to setOnPreparedListener

            }
        }

    }

    @Override
    void setPosition(int newpos) {
        if (mMediaPlayer != null && mMediaPlayerStarted) {
            mMediaPlayer.seekTo(newpos);
            mMediaPlayer.start(); // in case paused
        }
    }

    @Override
    void requestPositionUpdate() {
        if (mMediaPlayer != null && mMediaPlayerStarted) {
            WPlayer.fpGotNewPositionInMs(mMediaPlayer.getCurrentPosition(), this);
        } else if (mReachEndOnce) {
            WPlayer.fpGotNewPositionInMs(mSongDuration, this);
        } else {
            WPlayer.fpGotNewPositionInMs(0, this);
        }
    }
}

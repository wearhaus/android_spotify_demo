package com.example.steven.spautify.musicplayer;

import android.content.Context;

/**
 * Created by Steven on 1/22/2016.
 *
 * Must call super on constructor
 */
public abstract class WMusicProvider {


    public enum AuthState {
        /** Probably on a per-device basis, like how facebook works.*/
        NotLoggedIn,
        // Since an activity needs to wait for onActivityResult, we won't have the tryingtologin state here.
        /** Still authenticating user*/
        Loading,
        LoggedIn,
        Error;
    }


    public enum State {
        /**Default state; This state also lets WPlayer know that this player can be closed*/
        NoPlayer,
        /** Still authenticating user*/
        Loading,
        PlayerReady,
        /** And error has occurred with just this song and this can probably be fixed with skipping to next song.*/
        ErrorWithCurrentSong,
        /** When in this state, any call other than closeProvider will result in undefined behavior (most likely NPE)*/
        Error;
    }

    //protected WPlayer wplayer;


    /**
     * @param c must be the Application Context
     * //@param wp must be the WPlayer that creates and holds this*/
    WMusicProvider(Context c/*, WPlayer wp*/) {
        //wplayer = wp;
    }


    /**
     * Called when this player is no longer the current provider.  Essentially pause and maybe free up memory.
     */
    abstract void standby();
    /**
     * Close and free up everything to the state before the proivder was created.
     *
     * @param c must be same context provided in the constructor
     */
    abstract void closeProvider(Context c);


    abstract State getProviderState();



    abstract void playSong(Sng song);
    abstract void skipToNext();
    abstract void skipToPrevious();
    abstract void pause();
    abstract void resume();
    /** A drag is started, probably just pause it. */
    //abstract void startDragPosition();
    /** Set position to this and start playing again. */
    abstract void setPosition(int newpos);
    abstract void requestPositionUpdate();





}

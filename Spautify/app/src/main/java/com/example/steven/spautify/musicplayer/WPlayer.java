package com.example.steven.spautify.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;


import com.example.Notifier;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Steven on 1/22/2016.
 *
 * To add a new Music Provider, change Sng.Source to use the new enum, add the new proivder to this calls' init(), and manually handle Auth outside of this package
 */
public class WPlayer {

    private final static String TAG = "WPlayer";


    /**
     * The WPlayer object is created when music is requested, and destroyed (all references nulled) when closed completely.
     * Closes if open when Arc state transitions to a state not allowing for playback, or maybe we also provide a button
     * for the user to close player, like an X button, more than just a Stop button.
     *
     * WPlayer and WProviders are instanced so we don't load them when we don't need them.  WProvider instances
     * live in the WPlayer instance.  Auth for Providers are handled
     * statically though, through their Provider class.  This class checks an Enum to see if authed or not.
     */


    /**
     * 3rd idea:
     *
     * What if this is static, but all the providers remain as they are?
     * This business of having to null check every single time we want to interact with this or set up notifers
     * is annoying and very tedious.  When we'd close the player, we just instead close all the privders and
     * set a state here to be Off or something
     *
     */



    /**
     * There is a user visible queue, which does not include the current playing songs or done songs, but to support a Back button
     * that can go back actual tracks, we may want an internal queue of what was played in what order back then.
     * So a Queue: visible, editable list of Sng that will be played soon.
     *  and QueueHistory: list of Sng that have finished or been skipped.  Last member is the most recent.  Non-editable, Non-viewable.
     *              Used for when user presses Back.  May only contain like the last 5 songs, since most uses would be to only go back a few in case a skip was accidental, etc.
     *
     *
     * updateQueue():
     *    when a queue is loaded/changed, we can call spotify.clearQueue() then for() { spotify.queue() }.  Although if the queue is getting large, this can be
     *    really inefficient.  Consider a limit of like 50 or 100 of stuff actually loaded into Spotify Player itself.
     *    back?  How would that work?  Spotify might have their own internal one?
     *
     *
     * Actions that affect queues:
     *    Play button single song: immediately start playing song.  If a queue/current song exists, then add currentSng to queueHistory and leave queue intact.  If no queue, leave empty.
     *    Play button for Playlist: Delete queueHistory and queue.  Then spotify.play the new list of tracks.
     *    Add to Queue single: adds to end of queue. updateQueue()
     *    Add to Queue playlist: adds entire playlist to end of current queue. updateQueue()
     *    Skip: should all be handled by Spotify com.example.steven.spautify.player.
     *    Back: do tests, not sure how we ought to handle this, if we even need the queueHistory.  I'm guessing that clearQueue() also clears what Back would go to?
     *    Remove song(s) from queue: delete them from the queue.  updateQueue()
     *    relocate song in queue: delete/insert or similar primitive list function.  updateQueue()
     *
     *
     * TODO: if a playlist has 10,000 songs in it, how do we make it so it doesn't really lag or have OOMs??
     *
     * */



    /** App context*/
    private static Context mApp;

    public static void init(Context appContext) {
        mApp = appContext;

        SpotifyWebApiHandler.init();
    }


    public static void createPlayer() {
        Log.i(TAG, "start WPlayer");

        mQueue = new ArrayList<>();
        mQueueBack = new ArrayList<>();
        mAutoplayQueueAdditions = true;
        mPlaying = false;
        mShuffling = false;
        mRepeating = false;
        mCurrentSng = null;

        mProviders = new Hashtable<>();
        mScheduledHash = new Hashtable<>();
        mState = WPlayer.State.Initialized;


        Intent i = new Intent(mApp, WPlayerService.class );
        i.setAction(WPlayerService.ACTION_PLAY);
        mApp.startService(i);

        mNotifier.notifyListeners(Notif.PlaybackAndQueue);

    }



    public static void closePlayer() {
        // TODO for each open Provider, close

//        if (sWPlayer != null) {
//            for (Map.Entry<Class, WMusicProvider> entry : sWPlayer.mProviders.entrySet()) {
//                WMusicProvider wp = entry.getValue();
//                wp.closeProvider(mApp);
//            }
//
//            // TODO remove service, this requires a ref to it, which can only be had via binding (it seems that static ref is bad practice)
//            // Or we could have a notif sent that it listens to to shut itself down via it's stopSelf() call
//
//            sWPlayer = null;
//        }

        for (Map.Entry<Class, WMusicProvider> entry : mProviders.entrySet()) {
            WMusicProvider wp = entry.getValue();
            wp.closeProvider(mApp);
        }
        // Service closes itself so far


        mProviders.clear();
        mScheduledHash = null;
        mState = State.Off;
        mCurrentProvider = null;
        mCurrentSng = null;
        mCurrentErrorSng = null;
        mQueue = null;
        mQueueBack = null;






    }


    public enum State {
        /** Things aren't loaded/initialized.  Don't all any gets since those will hve unpredictable behavior in this state.*/
        Off,
        /** WPlayer ready, but no Provider has started loading yet*/
        Initialized,
        /** Only set to this state when the current provider is loading.  If another provider is loading but current isn't, this will reflect current's state.
         * This will also coincide with mPlaying = true, although we may change this decision later*/
        LoadingProvider,
        /** Temporary error loading current song.  Probably fixable via skipping to next song*/
        ErrorWithSong,
        /** Error loading current provider, such as Bad Auth, crash, etc.*/
        ErrorWithProvider,
        /** Provider is ready and UI should render normal playback details.*/
        Ready;
    }







//    protected ServiceConnection mServerConn = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder binder) {
//            Log.d(TAG, "onServiceConnected");
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            Log.d(TAG, "onServiceDisconnected");
//        }
//    };




    // Note: If an object is saved to a HastTable, any other refs to that object that change it also changes the version 'stored in the table',
    // since hastables really only store a pointer, not a clone.
    private static Hashtable<Class, WMusicProvider> mProviders;

    private static State mState = State.Off;

    /** One of the players.  This is an extra pointer for convenience.
     * Ex/ mSpotifyProvider field and this field will both point to the same thing.
     * Then we reassign just this field if we change sources*/
    private static WMusicProvider mCurrentProvider;
    private static Class mCurrentProviderClass;


    /** Null means Queue is finished, and player is idling/not playing anything*/
    private static Sng mCurrentSng;

    /**  Null unless a song has playback errors, in which case any attempt to play a song thats not this should remove the temp error state.*/
    private static Sng mCurrentErrorSng;

    /** False when playing, or is paused.  Is true basically only when com.example.steven.spautify.player just activated and not started, or reached end of queue.
     * This does not correlate to if playerState.trackUri exists or not, but rather if something should auto-com.example.steven.spautify.player when added to the end of a queue*/
    private static boolean mAutoplayQueueAdditions;


    // neither queue can ever be null, or contain null elements.
    private static ArrayList<Sng> mQueue;
    private static ArrayList<Sng> mQueueBack;

    /**Notifier used for any changes, including State changes and skips and queue changes.
     * Use the enum if only a few matter.  Errors or loading is folded into these, so
     * make sure to check State every time to make sure everything is good to render. */
    private static Notifier<Notif> mNotifier = new Notifier<>();

    public enum Notif {
        /** If the playback of the song changed but the queue has not changed. ex/ song positionInMs updates, or song was paused*/
        Playback,
        /** Just the playback position in ms; this will get called often if setPosBarAnimation is on.*/
        PlaybackPosition,
        /** Both current song and the queue changed. ex/ the song ended and a new song began*/
        PlaybackAndQueue,
        /**The queue was changed without affecting the current song/its playback status*/
        Queue;


        static Notif fuseNotifs(Notif a, Notif b) {
            if (a == PlaybackAndQueue) return PlaybackAndQueue;
            if (a == Playback || b == Queue) return PlaybackAndQueue;
            if (a == PlaybackPosition || b == Queue) return PlaybackAndQueue;
            if (a == PlaybackPosition || b == Playback) return Playback;
            if (a == b) return a;
            return fuseNotifs(b, a);
        }

    }

    private static boolean mPlaying;
    private static boolean mShuffling;
    private static boolean mRepeating;
    /** Updated periodically, TODO depending on device speed*/
    private static int positionInMs;

    private static ScheduledFuture mScheduledService;
    private static ScheduledExecutorService mScheduledExecutor = Executors.newScheduledThreadPool(1);
    private static Hashtable<Integer, Boolean> mScheduledHash;


    /** Any change in playback status, such as play/pause, new track, dragged position, etc.
     * If the queue has been edited, this will also get called.  Any changes to WPlayer.getState also
     * call this, so listeners can be set up when state is Off.*/
    public static Notifier getNotifier() {
        return mNotifier;
    }

    public static State getState() {
        return mState;
    }
    public static Sng getCurrentSng() {
        return mCurrentSng;
    }

    public static boolean getPlaying() {
        return mPlaying;
    }
    public static boolean getShuffling() {
        return mShuffling;
    }
    public static boolean getRepeating() {
        return mRepeating;
    }
    public static int getPositionInMs() {
        return positionInMs;
    }

    public static ArrayList<Sng> getQueue() {
        return mQueue;
    }

    public static ArrayList<Sng> getQueueBack() {
        return mQueueBack;
    }




    /** Returns true if mCurrentProvider.getProviderState() == WMusicProvider.State.PlayerReady*/
    private static boolean checkProviderReady() {
        Log.v(TAG, "mCurrentProvider: " + mCurrentProvider);
        //if (mCurrentProvider != null)Log.e(TAG, "     checkProviderReady: " + (mCurrentProvider.getProviderState() == WMusicProvider.State.PlayerReady));
        return mCurrentProvider != null && mCurrentProvider.getProviderState() == WMusicProvider.State.PlayerReady;
    }


    /** Plays Sng without any Queue changes.  Caller should handle any changes to queue.
     * Externally, make sure that the playing song is set to mCurrentSong.
     *
     * Note: Notifs should not happen in this call, instead do them the line after this method is called, since
     * this method doesn't know if the queue changed or not and we don't want redundant notifs.*/
    private static void internalPlay() {
        Log.d(TAG, "internalPlay");
        // TODO is this where we check for Provider type and maybe switch?
        // Create a state for LoadingProvider; creates a loading bar for UI

        if (mCurrentSng.source.providerClass != mCurrentProviderClass) {
            Log.d(TAG, "switching providers");
            // Need to switch providers
            WMusicProvider wp = mProviders.get(mCurrentSng.source.providerClass);
            if (mCurrentProvider != null) {
                mCurrentProvider.standby(); // Is null when first initialized
            }

            if (wp == null) {


                mState = State.LoadingProvider;
                try {
                    Log.d(TAG, "creating new provider");
                    Constructor con = mCurrentSng.source.providerClass.getConstructor(Context.class);
                    wp = (WMusicProvider) con.newInstance(mApp);
                    mProviders.put(mCurrentSng.source.providerClass, wp);
                    mCurrentProvider = wp;
                    mCurrentProviderClass = mCurrentSng.source.providerClass;
                    if (!wp.constructorAsync()) {
                        // force a refresh here
                        internalPlay();
                        return;
                    }

                    // Waits for fpProviderReady to be called. In th meantime, UI knows we have a LoadingProvider.
                    // If they want to do something else before that, it's fine.

                } catch (Exception e) {
                    Log.e(TAG, "Error creating Provider, " + e);
                    mState = State.ErrorWithProvider;
                    mCurrentProvider = null; // didn't even get loaded.
                    mCurrentProviderClass = null;
                }

                checkScheduled(); // turn off scheduled while player loading.



                return;


            } else {

                mCurrentProvider = wp;
                mCurrentProviderClass = mCurrentSng.source.providerClass;

            }

        }


        // now wp is set to the correct version and hopefully is ready

        if (checkProviderReady()) {

            mState = State.Ready;
            mPlaying = true;
            mCurrentErrorSng = null;
            mCurrentProvider.playSong(mCurrentSng);

            checkScheduled();

        } else if (mCurrentProvider.getProviderState() == WMusicProvider.State.Loading){
            Log.e(TAG, "Error, Provider already existed, but is still loading. Waiting for it to finish");
            mState = State.LoadingProvider;
            mPlaying = false;

        } else if (mCurrentProvider.getProviderState() == WMusicProvider.State.Error){
            Log.e(TAG, "Error, Provider is in error state");
            mState = State.ErrorWithProvider;
            mPlaying = false;

        } else if (mCurrentProvider.getProviderState() == WMusicProvider.State.ErrorWithCurrentSong){

            if (mCurrentErrorSng == null) {
                // new error!  set to error state
                Log.e(TAG, "Error, Provider has temporary error with current song");
                mState = State.ErrorWithSong;
                mPlaying = false;
                mCurrentErrorSng = mCurrentSng;

            } else if (mCurrentErrorSng == mCurrentSng) {
                // another attempt to play erroring song... Doing nothing
                // TODO
                Log.e(TAG, "Error, Provider has temporary error with current song, ignoring attempt to play again");
                mState = State.ErrorWithSong;
                mPlaying = false;


            } else {
                // new song attempt; this one probably has no error yet.  Try it.

                mState = State.Ready;
                mPlaying = true;
                mCurrentErrorSng = null;
                mCurrentProvider.playSong(mCurrentSng);


            }
            // TODO maybe have an auto-start next song after like 5 seconds?
        }

        checkScheduled(); // check after changing mPlaying

        //mNotifier.notifyListeners(notifType);  Notifs handled outside internalPlay()


    }


    /** Called when a Provider has been constructed and now asynchronously gets back to us that its finished.
     * This will start up the queued song
     * @param dontInterruptIfPlaying True if this method is being called mostly for notifs or to try again,  Only used when a temp song error is recovered.
     *
     * */
    static void fpProviderState(WMusicProvider prov, boolean dontInterruptIfPlaying) {
        Log.e(TAG, "fpProviderState: " + mCurrentProvider.getProviderState());
        if (!dontInterruptIfPlaying && prov == mCurrentProvider) {
            // So we don't actually use the success param; that info is already encoded into the provider state...

            Log.w(TAG, "fpProviderState: " + mCurrentProvider.getProviderState());

            internalPlay();
            mNotifier.notifyListeners(Notif.Playback);
            // so when person skips to next, previous, or just plays new song, we need to not have ErrorWithCurrentSong
            // not be permanent.  But, we also cant cause an infinite loop here.  Is the only way to use an extra flag here?
            // and to internal play?  Or should we set a mCurrentErroringSng at this moment?

        }
        // Else do nothing, we switched providers, so we don't care about this right now.
    }


    /** Toggles between playing and pausing*/
    public static void playpause() {

        if (checkProviderReady()) {
            mPlaying = !mPlaying;
            if (mPlaying) {
                mCurrentProvider.resume();
            } else {
                mCurrentProvider.pause();
            }
            mNotifier.notifyListeners(Notif.Playback);

            checkScheduled();
        } else {
            // TODO else: maybe try to reflect change or cancel Provider being loaded?

        }

    }

    public static void setShuffling(boolean b) {
        mShuffling = b;
        // shuffling is considered a playback setting, like playpause state.
        // We still need to notify since playback UI's may have a Shuffle button that needs to reflect the correct state.
        mNotifier.notifyListeners(Notif.Playback);
    }

    public static void setRepeating(boolean b) {
        mRepeating = b;
        mNotifier.notifyListeners(Notif.Playback);
    }

    public static void skipToNext() {
        // best way is to force provider to end of it's song so it naturally calls it's EndOfContext triggering wp
        if (checkProviderReady()) {
            mCurrentProvider.skipToNext();
            // This will trigger EndOfSong which handles queue stuff and notifs
        } else {
            // But if prov isn't good and so isn't playing anything, then just skip that step
            fpEndOfSong(mCurrentProvider);

        }
    }

    public static void skipToPrevious() {
        // best to not have provider handle since we want consistency on behavior across all providers

        mAutoplayQueueAdditions = false;
        if (mQueueBack.size() > 0) {
            if (mCurrentSng != null) {
                // TODO, is this efficient enough??
                mQueue.add(0, mCurrentSng);
            }
            mCurrentSng = mQueueBack.remove(mQueueBack.size() - 1);
            internalPlay();
            mNotifier.notifyListeners(Notif.PlaybackAndQueue);
        } else {
            if (checkProviderReady()) {
                mCurrentProvider.setPosition(0);
            }
            // pause and setpos to 0 for current song, which spotify com.example.steven.spautify.player does automatically
            mNotifier.notifyListeners(Notif.Playback);
        }



    }

    // fpSkipPrevReceived() {
    //    // so do nothing?
    //}



    public static void startDragPosition() {
        if (checkProviderReady()) {
            mCurrentProvider.pause();
            mPlaying = false;
            mNotifier.notifyListeners(Notif.Playback);
            checkScheduled(); // check after changing mPlaying
        }
    }

    /** Sets the new position of the song in ms*/
    public static void endDragPosition(int newpos) {
        if (checkProviderReady()) {
            mPlaying = true;
            mCurrentProvider.setPosition(newpos);
            mNotifier.notifyListeners(Notif.Playback);
            checkScheduled(); // check after changing mPlaying
        }
    }

    /** If a UI element wants to keep the positionInMS constantly updated, set to true.
     * When that is no longer requested, set to false, such as on pause.
     * Use a unique code per element instance, so the orders of calls dont mess up things
     *
     * Make sure State is not Off, otherwise throws an error.  Don't use WPlayer methods when state is Off.
     * When turned off, tyese are automatically removed*/
    public static void setPosBarAnimation(boolean b, int code) {
        // This may not be the best solution, but it at least is glitch free assuming codes are unique.

        // If a true is set, then we record that in the map.  If there's at least 1 code in the hash, then at least 1 UI element wants this.
        if (b) {
            mScheduledHash.put(code, b);
        } else {
            mScheduledHash.remove(code);
        }

        checkScheduled();
    }

    private static void checkScheduled() {
        Log.d(TAG, "checkScheduled()");
        if (checkProviderReady() && mPlaying && mScheduledHash.entrySet().size() > 0) {
            // This keeps our SeekBar and the time accurate
            if (mScheduledService == null) {
                int delay = 250; // TODO for slow devices, use only 1, for fast devices, even as low as 100 is fine.

                mScheduledService = mScheduledExecutor.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("sched", "running   playing?" + mPlaying);
                        try {
                            if (checkProviderReady()) {
                                mCurrentProvider.requestPositionUpdate();
                            }

                            checkScheduled();
                        } catch (Exception e) {
                            Log.e("sched", "Exception!!" + e);
                        }

                        // this will call notifiers which calls refreshUI over again
                    }
                }, 1, delay, TimeUnit.MILLISECONDS);

                Log.e("sched", "created");

            }


        } else {
            Log.e("sched", "checkScheduled To be cancelled");

            if (mScheduledService != null) {
                mScheduledService.cancel(false);
                mScheduledService = null;
            }
        }

    }



    public static void playManyClearQueue(ArrayList<Sng> sngs) {
        mQueue.clear();
        mQueueBack.clear();

        mQueue.addAll(sngs.subList(1, sngs.size()-1));
        mCurrentSng = sngs.get(0);
        mAutoplayQueueAdditions = false;

        internalPlay();

        mNotifier.notifyListeners(Notif.PlaybackAndQueue);
    }





    public static void playSingleClearQueue(Sng sng) {
        mQueue.clear();
        mQueueBack.clear();

        mCurrentSng = sng;
        mAutoplayQueueAdditions = false;
        internalPlay();

        mNotifier.notifyListeners(Notif.PlaybackAndQueue);
    }

    /** position is 0 indexed and includes the current playing one*/
    public static void playItemInQueue(int pos, Sng sng) {
        // queue may have been changed since pos was chosen, we double check the sng and also the range
        // this leaves the rare possibility of a glitch when the same song is queued 2+ times, and the queue changes between pos being set and this method being called.


        // TODO consider fixing offbyone (between pos being chosen by user and this method, the song may have changed, rare, but possible) stuff instead of just doing nothing
        if (pos < mQueueBack.size()) {

            if (!mQueueBack.get(pos).spotifyUri.equals(sng.spotifyUri)) {
                Log.e(TAG, "Error with playItemInQueue.  Mismatched sng");
                return;
            }

            // start inclusive, end exclusive
            ArrayList<Sng> qb = new ArrayList<>(mQueueBack.subList(pos + 1, mQueueBack.size()));




            if (mCurrentSng != null) {
                mQueue.add(0, mCurrentSng);
            }
            if (qb.size() > 0) {
                for (int i = qb.size() - 1; i >= 0; i--) {
                    mQueue.add(0, qb.get(i));
                }
            }
            mCurrentSng = mQueueBack.get(pos);
            mQueueBack = new ArrayList<>( mQueueBack.subList(0, pos) );

            internalPlay();
            mNotifier.notifyListeners(Notif.PlaybackAndQueue);

        } else if (pos == mQueueBack.size()) {
            // so basically they chose mCurrentSng again

            if (mCurrentSng != null) {
                if (!mCurrentSng.spotifyUri.equals(sng.spotifyUri)) {
                    Log.e(TAG, "Error with playItemInQueue.  Mismatched sng");
                    return;
                }

                internalPlay();
                // start current song over again from 0
                mNotifier.notifyListeners(Notif.Playback);
            }


        } else if (pos < mQueueBack.size() + 1 + mQueue.size()) {
            int newpos = pos - (mQueueBack.size() + 1);
            if (!mQueue.get(newpos).spotifyUri.equals(sng.spotifyUri)) {
                Log.e(TAG, "Error with playItemInQueue.  Mismatched sng");
                return;
            }

            if (mCurrentSng != null) {
                mQueueBack.add(mCurrentSng);
            }

            ArrayList<Sng> qq = new ArrayList<>(mQueue.subList(0, newpos));
            mQueueBack.addAll(qq);

            mCurrentSng = mQueue.get(newpos);

            mQueue = new ArrayList<>(mQueue.subList(newpos + 1, mQueue.size()));


            internalPlay();
            mNotifier.notifyListeners(Notif.PlaybackAndQueue);



        } else {
            Log.e(TAG, "Error with playItemInQueue.  Out of range");
            return;

        }

    }




    /** pos includes queueback.*/
    public static void removeFromQueue(int pos, Sng sng) {
        Notif n = removeFromQueueInternal(pos, sng);
        mNotifier.notifyListeners(n);
    }

    /** Does not call any Notifiers */
    private static Notif removeFromQueueInternal(int pos, Sng sng) {
        if (pos < mQueueBack.size()) {

            if (!mQueueBack.get(pos).spotifyUri.equals(sng.spotifyUri)) {
                Log.e(TAG, "Error with removeFromQueue.  Mismatched sng");
                return null;
            }

            mQueueBack.remove(pos);
            return Notif.Queue;


        } else if (pos == mQueueBack.size()) {
            // if null, then good

            mCurrentSng = null;
            mCurrentProvider.skipToNext();
            return null;
            // convenient way to erase current and start next song, nulls are dropped from list when found
            // No notif since the skip event will trigger EndOfSong's notif

        } else if (pos < mQueueBack.size() + 1 + mQueue.size()) {
            int newpos = pos - (mQueueBack.size() + 1);
            if (!mQueue.get(newpos).spotifyUri.equals(sng.spotifyUri)) {
                Log.e(TAG, "Error with removeFromQueue.  Mismatched sng");
                return null;
            }

            mQueue.remove(newpos);
            return Notif.Queue;

        } else {
            Log.e(TAG, "Error with removeFromQueue.  Out of range");
            return null;

        }


    }


    public static void addtoEndOfQueue(Sng song) {
        // TODO is a null check best way??

        // Depends on updateQueue working AND a marker for when we are at the end of a queue and a song has laready finished playing
        // This may not be easy to know, TEST what Spotify does

        if (mAutoplayQueueAdditions) {
            mCurrentSng = song;
            mAutoplayQueueAdditions = false;
            internalPlay();
            mNotifier.notifyListeners(Notif.PlaybackAndQueue);
        } else {
            mQueue.add(song);
            mNotifier.notifyListeners(Notif.Queue);
        }
    }


    public static void insertIntoQueue(int pos, Sng sng) {

        if (pos < mQueueBack.size()) {

            mQueueBack.add(pos, sng);
            mNotifier.notifyListeners(Notif.Queue);

        } else if (pos == mQueueBack.size()) {

            mQueueBack.add(pos, sng);
            mNotifier.notifyListeners(Notif.Queue);

        } else if (pos < mQueueBack.size() + 1 + mQueue.size()) {
            int newpos = pos - (mQueueBack.size() + 1);

            mQueue.add(newpos, sng);
            mNotifier.notifyListeners(Notif.Queue);
        } else {
            // if inserted at end of list, use this method again
            addtoEndOfQueue(sng);
        }
    }


    public static void swapQueueItems(int oldPos, int newPos, Sng draggedSng) {
        Notif n = removeFromQueueInternal(oldPos, draggedSng);

        //if (newPos > oldPos) newPos -= 1;

        if (newPos < mQueueBack.size()) {

            mQueueBack.add(newPos, draggedSng);
            mNotifier.notifyListeners(Notif.fuseNotifs(n, Notif.Queue));

        } else if (newPos == mQueueBack.size()) {

            mQueueBack.add(newPos, draggedSng);
            mNotifier.notifyListeners(Notif.fuseNotifs(n, Notif.Queue));

        } else if (newPos < mQueueBack.size() + 1 + mQueue.size()) {
            int newpos = newPos - (mQueueBack.size() + 1);

            mQueue.add(newpos, draggedSng);
            mNotifier.notifyListeners(Notif.fuseNotifs(n, Notif.Queue));
        } else {
            // if inserted at end of list, use this method again
            addtoEndOfQueue(draggedSng);
            mNotifier.notifyListeners(n); // too lazy...
        }




    }





    //// fp means called From Providers

    static void fpGotNewPositionInMs(int pos, WMusicProvider provider) {
        if (provider == mCurrentProvider) {
            //Log.v(TAG, "song pos: " + pos);
            positionInMs = pos;
            mNotifier.notifyListeners(Notif.PlaybackPosition);
        }
    }

    /** Provider will call this when the song ends either by natural or skipped means.
     * This reflects the state in WPlayer, the provider must handle it's internal stopping.*/
    static void fpEndOfSong(WMusicProvider provider) {
        if (provider == mCurrentProvider) {

            if (mQueue.size() > 0) {
                // only do this if we pop more off the current queue.
                if (mCurrentSng != null) {
                    mQueueBack.add(mCurrentSng);
                }

                mCurrentSng = mQueue.remove(0);
                mAutoplayQueueAdditions = false;

                internalPlay();
                mNotifier.notifyListeners(Notif.PlaybackAndQueue);


            } else {
                // finished!
                // leave current song as is,
                // TODO or set it to beginning of queueBack.
                //ArrayList<Sng> newq = new ArrayList<>();

                mAutoplayQueueAdditions = true;

                mPlaying = false;
                mNotifier.notifyListeners(Notif.Playback);

            }
        }

    }






}

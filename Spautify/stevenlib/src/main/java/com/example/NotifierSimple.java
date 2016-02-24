package com.example;

import java.util.Vector;

/**
 * Created by Steven on 12/17/2015.
 */
public class NotifierSimple {
    public NotifierSimple() {
        listeners = new Vector<ListenerSimple>();
        toAddList = new Vector<ListenerSimple>();
        toRemoveList = new Vector<ListenerSimple>();
    }

    public void unregisterAllListeners() {
        if (dlIterating) {
            toRemoveList.addAll(listeners);
        } else {
            listeners.clear();
            toAddList.clear();
            toRemoveList.clear();
        }
    }

    /**
     * Create a Listener and register it with the notifier.
     *
     */
    public static interface ListenerSimple {
        public void onChange();
    }


    private Vector<ListenerSimple> listeners;
    private Vector<ListenerSimple> toAddList;
    private Vector<ListenerSimple> toRemoveList;
    private boolean dlIterating = false; // If the callback of one listener changes the list, we need to handle this properly
    /**
     * Register the listener and calls onChange() right after any change the
     * current room.  The new room can be found with mUser.getRoom().
     * Note: After any call to exit, the mUser.getRoom() will be null!
     */
    public void registerListener(NotifierSimple.ListenerSimple dl) {
        if (dlIterating) {
            toAddList.add(dl);
        } else {
            listeners.add(dl);
        }
    }

    /**
     * Use this if you desire to register a listener in sequence.
     * This is identical to registerListener, except that it returns itself.
     */
    public NotifierSimple registerListenerChain(NotifierSimple.ListenerSimple dl) {
        registerListener(dl);
        return this;
    }

    /**
     * See registermUserRoomChangeListener
     */
    public void unregisterListener(NotifierSimple.ListenerSimple dl) {
        if (dlIterating) {
            toRemoveList.add(dl);
        } else {
            listeners.remove(dl);
        }
    }

    public void notifyListeners() {
        dlIterating = true;
        for (NotifierSimple.ListenerSimple dl : listeners) {
            dl.onChange();
        }
		/* So it is possible that a registered listener's callback's
		 * thread will cause a new register/unregister call, which
		 * would throw a ConcurrentModificationException because
		 * we can't iterate and modify the list at the same time.
		 * This will also prevent duplicate calling of
		 * any callbacks that get registered, since at registration,
		 * they should handle grabbing current room info, and we wouldn't
		 * want to call it again by just using a normal for loop (plus
		 * in that case, removal can cause unpredictable behavior).
		 *
		 * Our solution is to add it to a modification queue that
		 * get's processed after the iteration finishes
		 *
		 */

        if (toAddList.size() > 0) {
            for (NotifierSimple.ListenerSimple dl : toAddList) {
                listeners.add(dl);
            }
            toAddList.clear();
        }
        if (toRemoveList.size() > 0) {
            for (NotifierSimple.ListenerSimple dl : toRemoveList) {
                listeners.remove(dl);
            }
            toRemoveList.clear();
        }
        dlIterating = false;
    }
}

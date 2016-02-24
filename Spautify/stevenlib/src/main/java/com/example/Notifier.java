package com.example;

import java.util.Vector;


/**
 * Class used to handle cases where you want more than one listener to when an event will
 * happen, or the event will happen many times, and issues with concurrency, and adding/removing
 * listeners may become confusing.
 * 
 * TODO  I'm no longer liking this class anymore;  instead of having listeners pass on the response,
 * a much better and simpler way is to make the onChange have no params, and just have the listeners
 * manually get the vars for themselves, so we don't complicate things with types and classes
 * and annoying and useless things.
 * 
 * Java class that can be used for callback, multi-threaded style of programming.
 * This allows for one to create a Notifier which will send a callback (in the same
 * thread) whenever NotifyListeners(type) is called.  The creator of the Notifier object
 * can define (int) type to specify the type of callback.
 * 
 * A listener can be added by using the registerListener.
 * 
 * @author Steven
 * @param T is any class you want to distinguish the type of onChange call you want.
 */
public class Notifier<T> {
	public Notifier() { 
		listeners = new Vector<Listener<T>>();
		toAddList = new Vector<Listener<T>>();
		toRemoveList = new Vector<Listener<T>>();
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
    public static interface Listener<T> {
		public void onChange(T type);
	}


	private Vector<Listener<T>> listeners;
	private Vector<Listener<T>> toAddList;
	private Vector<Listener<T>> toRemoveList;
	private boolean dlIterating = false; // If the callback of one listener changes the list, we need to handle this properly
	/**
	 * Register the listener and calls onChange() right after any change the
	 * current room.  The new room can be found with mUser.getRoom().
	 * Note: After any call to exit, the mUser.getRoom() will be null!
	 */
	public void registerListener(Notifier.Listener<T> dl) {
		if (dlIterating) {
			toAddList.add(dl);
		} else {
			listeners.add(dl);
		}
	}

    //public int listenerCount() {
    //    return listeners.size();
    //}

	/**
	 * Use this if you desire to register a listener in sequence.
	 * This is identical to registerListener, except that it returns itself.
	 */
	public Notifier<T> registerListenerChain(Notifier.Listener<T> dl) {
		registerListener(dl);
		return this;
	}

	/**
	 * See registermUserRoomChangeListener
	 */
	public void unregisterListener(Notifier.Listener<T> dl) {
		if (dlIterating) {
			toRemoveList.add(dl);
		} else {
			listeners.remove(dl);
		}
	}

	public void notifyListeners(T type) {
		dlIterating = true;
		for (Notifier.Listener<T> dl : listeners) {
			dl.onChange(type);
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
			for (Notifier.Listener<T> dl : toAddList) {
				listeners.add(dl);
			}
			toAddList.clear();
		}
		if (toRemoveList.size() > 0) {
			for (Notifier.Listener<T> dl : toRemoveList) {
				listeners.remove(dl);
			}
			toRemoveList.clear();
		}
		dlIterating = false;
	}
}

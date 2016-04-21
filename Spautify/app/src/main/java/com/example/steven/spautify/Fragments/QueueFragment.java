package com.example.steven.spautify.Fragments;

import com.example.Notifier;
import com.example.steven.spautify.musicplayer.Sng;
import com.example.steven.spautify.musicplayer.WPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steven on 2/5/2016.
 */
public class QueueFragment extends SongListFragment {

    private Notifier.Listener<WPlayer.Notif> mWPSL;

    @Override
    protected ClickType getClickType() {
        return ClickType.Queue;
    }

    @Override
    protected List getList() {
        ArrayList<SngItem> l = new ArrayList<>();

        if (WPlayer.getState() == WPlayer.WPlayerState.Off) {
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


    @Override
    public void onResume() {
        super.onResume();
        if (mWPSL == null) {
            mWPSL = new WPlayerStateListener();
            WPlayer.getNotifier().registerListener(mWPSL);
        }
    }

    @Override
    public void onPause() {
        if (mWPSL != null) {
            WPlayer.getNotifier().unregisterListener(mWPSL);
            mWPSL = null;
        }
        super.onPause();
    }

    @Override
    protected String checkIfBad() {
        if (WPlayer.getState() == WPlayer.WPlayerState.Off) {
            return "Player is off";
        } else if (WPlayer.getCurrentSng() == null) {
            return "queue is empty";
        }
        return null;
    }

    @Override
    protected boolean canSwipeToRefresh() {
        return false;
    }

    @Override
    protected void onSwipeRefresh() {
    }

    @Override
    protected boolean paginated() {
        return false;
    }




    private class WPlayerStateListener implements Notifier.Listener<WPlayer.Notif> {

        @Override
        public void onChange(WPlayer.Notif type) {
            if (type == WPlayer.Notif.PlaybackAndQueue || type == WPlayer.Notif.Queue) {
                updateList();
            }
        }
    }
}

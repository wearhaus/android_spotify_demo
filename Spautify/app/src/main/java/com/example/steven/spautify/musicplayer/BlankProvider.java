package com.example.steven.spautify.musicplayer;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Steven on 2/1/2016.
 */
public class BlankProvider extends WMusicProvider {


    private ScheduledFuture mScheduledService;

    private boolean playing;
    private int pos = 0;


    // TODO make work for tests



    public BlankProvider(Context c) {
        super(c);
    }

    @Override
    boolean constructorAsync() {
        return false;
    }


    @Override
    void standby() {

    }

    @Override
    void closeProvider(Context c) {

    }

    @Override
    State getProviderState() {
        return null;
    }

    @Override
    void playSong(Sng song) {

    }

    @Override
    void skipToNext() {

    }

    @Override
    void skipToPrevious() {

    }

    @Override
    void pause() {

    }

    @Override
    void resume() {

    }

    @Override
    void setPosition(int newpos) {
        //checkScheduled();

    }

    @Override
    void requestPositionUpdate() {

    }



    private void checkScheduled() {
        if (playing) {
            if (mScheduledService == null) {
                int delay = 250;
                ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
                mScheduledService = service.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d("sched", "running");
                        if (playing) {
                            playing = false;
                            WPlayer.fpEndOfSong(BlankProvider.this);
                        }

                        checkScheduled();

                        // this will call notifiers which calls refreshUI over again
                    }
                }, 1, delay, TimeUnit.MILLISECONDS);

            }


        } else {

            if (mScheduledService != null) {
                mScheduledService.cancel(false);
                mScheduledService = null;
            }
        }

    }
}

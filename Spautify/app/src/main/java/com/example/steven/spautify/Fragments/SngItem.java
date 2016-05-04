package com.example.steven.spautify.fragments;

import com.example.steven.spautify.musicplayer.Sng;

/**
 * Created by Steven on 4/28/2016.
 *
 * Class wraps around a song to provide context for viwing the Queue
 */
public class SngItem {
    public Sng sng;
    public Type type;

    public SngItem(Sng s, Type t) {
        sng = s;
        type = t;
    }

    public enum Type {
        QueueBack,
        Current,
        Queue,
        //AutoSource,
        NotInQueue,
    }

}

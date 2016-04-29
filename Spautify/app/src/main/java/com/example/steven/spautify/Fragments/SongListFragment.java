//package com.example.steven.spautify.Fragments;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//
//import com.example.steven.spautify.R;
//import com.example.steven.spautify.ViewAlbumActivity;
//import com.example.steven.spautify.WPlayerViewHolder;
//import com.example.steven.spautify.musicplayer.Sng;
//import com.example.steven.spautify.musicplayer.Source;
//import com.example.steven.spautify.musicplayer.WPlayer;
//import com.squareup.picasso.Picasso;
//
//import java.util.ArrayList;
//
///**
// * Created by Steven on 2/5/2016.
// */
//public abstract class SongListFragment extends DynamicRecycleListFragment {
////        extends DynamicRecycleListFragment<
////        SngItemAdapter.SngItem, WPlayerViewHolder, SngItemAdapter> {
//
//
//    @Override
//    protected RecyclerView.Adapter createNewAdapter(Context context, ArrayList mList) {
//        return new SngItemAdapter(this, (ArrayList<SngItem>) mList);
//    }
//
//
//    public enum ClickType {
//        Queue(),
//        SearchResult(),
//        Lib(),
//        /** A lib, but we are looking at it's spotify album, so dont offer to look at album*/
//        LibAlbum(),
//    }
//    /** If true, then */
//    protected abstract ClickType getClickType();
//
//    /** Default off due to OOM issues*/
//    protected boolean showAlbum() {
//        return false;
//    }
//
//    @Override
//    protected boolean canDragAndDrop() {
//        return getClickType() == ClickType.Queue;
//    }
//
//
//
//
//
//    //////////////////
//
//
//    ////////////////////
//
//
//}

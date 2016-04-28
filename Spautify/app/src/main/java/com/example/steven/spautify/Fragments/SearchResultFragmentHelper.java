package com.example.steven.spautify.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.example.steven.spautify.ViewPlaylistActivity;
import com.example.steven.spautify.musicplayer.Source;
import com.example.steven.spautify.musicplayer.SpotifyApi;
import com.example.steven.spautify.musicplayer.WMusicProvider;

import java.util.ArrayList;

/**
 * Created by Steven on 4/25/2016.
 *
 * In order to keep code DRY and get around Java's ban on
 * extending 2 abstract classes, use this for any SearchResultFragment
 *
 * Use by creating an object of this class at the Fragment's creation (or instantly), and pass it in here.
 * Then during any method calls at all, refer to this class's method instead, so all the actual code
 * is contained here.  The actual fragment itself is full of 1 line redirections
 */
public class SearchResultFragmentHelper<I,
        V extends RecyclerView.ViewHolder,
        A extends RecyclerView.Adapter<V> & DynamicRecycleListFragment.ItemTouchHelperAdapter>  {

    private DynamicRecycleListFragment<I, V, A> mDyrel;

    SearchResultFragmentHelper(DynamicRecycleListFragment<I, V, A> dyrel, String sourcePrefix) {
        mDyrel = dyrel;
        mSource = Source.getSourceByPrefix(sourcePrefix);
    }





    protected ArrayList<SongListFragment.SngItem> resultingItems;
    private DynamicRecycleListFragment.SearchResultNextPage mNextPage;
    private String errorMsg;
    private Source mSource;

    /** @param add if true, add the given items nstead of replace.  When true, r must not be null*/
    public void setResult(@NonNull ArrayList<SongListFragment.SngItem> r, DynamicRecycleListFragment.SearchResultNextPage srnp, boolean add) {
        if (add && resultingItems != null) {
            // extra null check in case async came back awkwardly
            // TODO program in dynamic cancelling of the nextPage request (as in ignore result instead of calling this)
            resultingItems.addAll(r);
        } else {
            resultingItems = r;
        }
        errorMsg = null;
        mNextPage = srnp;

        mDyrel.mPageLoadedCount = resultingItems.size();
        mDyrel.mPageTotalAbleToBeLoaded =  mDyrel.mPageLoadedCount + (mNextPage != null ? 1 : 0); // if not null, at least 1 more thing loadable
        mDyrel.setRefreshing(false);

        mDyrel.updateList();
    }


    public void setResultNewQuery() {
        resultingItems = null;
        errorMsg = null;
        mNextPage = null;
        mDyrel.setRefreshing(true);
        mDyrel.mPageLoadedCount = 0;
        mDyrel.mPageTotalAbleToBeLoaded = 0;
        mDyrel.updateList();
    }

    public void setResultError(String e) {
        resultingItems = null;
        errorMsg = e;
        mNextPage = null;
        mDyrel.setRefreshing(false);
        mDyrel.mPageLoadedCount = 0;
        mDyrel.mPageTotalAbleToBeLoaded = 0;
        mDyrel.updateList();
    }

    public void setResultCancelled() {
        resultingItems = null;
        errorMsg = null;
        mNextPage = null;
        mDyrel.setRefreshing(false);
        mDyrel.mPageLoadedCount = 0;
        mDyrel.mPageTotalAbleToBeLoaded = 0;
        mDyrel.updateList();
    }




    protected String checkIfBad() {
        String badWhy = null;

        //if (!HTTPController.isThereInternet()) {
        //   badWhy = "Can't access servers";
        //} else
        if (errorMsg != null) {
            badWhy = errorMsg;
        } else if (mSource.isPlaybackAuthedErrorString() != null) {
            badWhy = mSource.isPlaybackAuthedErrorString();
        } else if (mDyrel.getList() == null) {
            badWhy = "";
        } else if (mDyrel.getList().size() <= 0) {
            badWhy = "No matches";
            mDyrel.setRefreshing(false);
        }

        return badWhy;
    }



    protected void loadData(int offset) {
        // we ignore offset, since thats already prepared outside of this fragment
        if (mNextPage != null) {
            mDyrel.setRefreshing(true);
            mNextPage.requestNextPage();
            mNextPage = null; // to prevent duplicate calls
        }
    }

}

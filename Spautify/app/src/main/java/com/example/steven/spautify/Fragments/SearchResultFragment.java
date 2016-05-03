package com.example.steven.spautify.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.steven.spautify.SearchActivity;
import com.example.steven.spautify.musicplayer.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steven on 4/25/2016.
 *
 * It is an error to attach this fragment without first setting args for
 * TAG_SOURCE_PREFIX and TAG_LIB_TYPE_ORDINAL
 */
public class SearchResultFragment extends MusicLibFragment {

    public static final String TAG_SOURCE_PREFIX = "arg_source_prefix";
    public static final String TAG_LIB_TYPE_ORDINAL = "arg_lib_type";






    protected ArrayList resultingItems;
    private SearchResultNextPage mNextPage;
    private String errorMsg;
    private Source mSource;
    private MusicLibType mLibType;


    @Override
    public MusicLibType getMusicLibType() {
        return mLibType;
    }

    public Source getSource() {
        return mSource;
    }

    @Override
    protected List getList() {
        return resultingItems;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSource = Source.getSourceByPrefix(getArguments().getString(TAG_SOURCE_PREFIX));
        mLibType = MusicLibType.values()[getArguments().getInt(TAG_LIB_TYPE_ORDINAL)];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        SearchActivity act = (SearchActivity) getActivity();
        if (act != null) {
            Log.i("onCreateView", "onCreateView");
            act.updateFragUI(this);
        }

        return v;
    }

    /** send the ENTIRE list every time*/
    public void setResult(ArrayList r, SearchResultNextPage srnp, boolean loading, String error) {
        resultingItems = r;

        errorMsg = error;
        mNextPage = srnp;

        if (r != null) {
            mPageLoadedCount = resultingItems.size();
            mPageTotalAbleToBeLoaded = mPageLoadedCount + (mNextPage != null ? 1 : 0); // if not null, at least 1 more thing loadable
        } else {
            mPageLoadedCount = 0;
            mPageTotalAbleToBeLoaded = 0;
        }
        Log.e("YYYYY", "setRefreshing to " + loading);
        setRefreshing(loading);

        updateList();
    }




    protected String checkIfBad() {
        if (errorMsg != null) {
            return errorMsg;

        } else if (mSource.isPlaybackAuthedErrorString() != null) {
            return mSource.isPlaybackAuthedErrorString();

        } else if (mSource.equals(Source.Soundcloud) && mLibType == MusicLibType.Album) {
            return "Search by Album not supported by SoundCloud";

        } if (getList() == null) {
            return "";

        } else if (getList().size() <= 0) {
            Log.e("YYYYY", "SETTTING FALSE");
            setRefreshing(false);
            return "No matches";

        } else {
            // No errors
            return null;
        }

    }


    @Override
    protected boolean paginated() {
        return true;
    }



    protected void loadData(int offset) {
        // we ignore offset, since thats already prepared outside of this fragment
        if (mNextPage != null) {
//            errorMsg = null;
//            mNextPage = null;
//            setRefreshing(true);
//            updateList();
            mNextPage.requestNextPage();
            mNextPage = null; // to prevent duplicate calls
        }
    }


    /** Interface class to be used if the fragment does not know how to loadData,
     * but the caller activity does.*/
    public interface SearchResultNextPage {
        /** Must call setResult... eventually due to loading bar being present
         * TODO how to handle cancelling due to another search sent.*/
        void requestNextPage();
    }

}

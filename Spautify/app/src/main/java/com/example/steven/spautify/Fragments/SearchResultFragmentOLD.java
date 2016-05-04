//package com.example.steven.spautify.Fragments;
//
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.example.steven.spautify.SearchActivity;
//import com.example.steven.spautify.musicplayer.Source;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by Steven on 4/25/2016.
// *
// * It is an error to attach this fragment without first setting args for
// * TAG_SOURCE_PREFIX and TAG_LIB_TYPE_ORDINAL
// */
//public class SearchResultFragmentOLD extends MusicLibFragment {
//
//    public static final String TAG_SOURCE_PREFIX = "arg_source_prefix";
//    public static final String TAG_LIB_TYPE_ORDINAL = "arg_lib_type";
//
//
//
//
//
//
//    protected ArrayList<SngItem> resultingItems;
//    private SearchResultFragment.SearchResultNextPage mNextPage;
//    private String errorMsg;
//    private Source mSource;
//    private MusicLibType mLibType;
//
//    @Override
//    public MusicLibType getMusicLibType() {
//        return mLibType;
//    }
//
//    public Source getSource() {
//        return mSource;
//    }
//
//    @Override
//    protected List getList() {
//        return resultingItems;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mSource = Source.getSourceByPrefix(getArguments().getString(TAG_SOURCE_PREFIX));
//        mLibType = MusicLibType.values()[getArguments().getInt(TAG_LIB_TYPE_ORDINAL)];
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View v = super.onCreateView(inflater, container, savedInstanceState);
//
////        SearchActivity act = (SearchActivity) getActivity();
////        if (act != null) {
////            //act.updateFragUI(this);
////        }
//
//        return v;
//    }
//
//    /** @param add if true, add the given items nstead of replace.  When true, r must not be null*/
//    public void setResult(@NonNull ArrayList<SngItem> r, SearchResultNextPage srnp, boolean add) {
//        if (add && resultingItems != null) {
//            // extra null check in case async came back awkwardly
//            // TODO program in dynamic cancelling of the nextPage request (as in ignore result instead of calling this)
//            resultingItems.addAll(r);
//        } else {
//            resultingItems = r;
//        }
//        errorMsg = null;
//        mNextPage = srnp;
//
//        mPageLoadedCount = resultingItems.size();
//        mPageTotalAbleToBeLoaded =  mPageLoadedCount + (mNextPage != null ? 1 : 0); // if not null, at least 1 more thing loadable
//        setLoading(false);
//
//        updateList();
//    }
//
//
//    public void setResultNewQuery() {
//        Log.e("yyyyyy", "setResultNewQuery");
//        resultingItems = null;
//        errorMsg = null;
//        mNextPage = null;
//        setLoading(true);
//        mPageLoadedCount = 0;
//        mPageTotalAbleToBeLoaded = 0;
//        updateList();
//    }
//
//    /** Does not change the result list, only the loading status to true*/
//    public void setOnlyLoading() {
//        errorMsg = null;
//        mNextPage = null;
//        setLoading(true);
//        updateList();
//    }
//
//    public void setResultError(String e) {
//        resultingItems = null;
//        errorMsg = e;
//        mNextPage = null;
//        setLoading(false);
//        mPageLoadedCount = 0;
//        mPageTotalAbleToBeLoaded = 0;
//        updateList();
//    }
//
//    public void setResultCancelled() {
//        resultingItems = null;
//        errorMsg = null;
//        mNextPage = null;
//        setLoading(false);
//        mPageLoadedCount = 0;
//        mPageTotalAbleToBeLoaded = 0;
//        updateList();
//    }
//
//
//
//
//    protected String checkIfBad() {
//        String badWhy = null;
//
//        //if (!HTTPController.isThereInternet()) {
//        //   badWhy = "Can't access servers";
//        //} else
//        if (errorMsg != null) {
//            badWhy = errorMsg;
//        } else if (mSource.isPlaybackAuthedErrorString() != null) {
//            badWhy = mSource.isPlaybackAuthedErrorString();
//        } else if (getList() == null) {
//            badWhy = "";
//        } else if (getList().size() <= 0) {
//            badWhy = "No matches";
//            setLoading(false);
//        }
//
//        return badWhy;
//    }
//
//
//    @Override
//    protected boolean paginated() {
//        return true;
//    }
//
//
//
//    protected void loadData(int offset) {
//        // we ignore offset, since thats already prepared outside of this fragment
//        if (mNextPage != null) {
//            setOnlyLoading();
//            mNextPage.requestNextPage();
//            mNextPage = null; // to prevent duplicate calls
//        }
//    }
//
//}

//package com.example.steven.spautify.Fragments;
//
//import android.support.annotation.NonNull;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by Steven on 2/5/2016.
// */
//public class SongSearchResultFragment extends SongListFragment {
//
//
//    @Override
//    protected ClickType getClickType() {
//        return ClickType.SearchResult;
//    }
//
//    @Override
//    protected List getList() {
//        return resultingItems;
//    }
//
//    private ArrayList<SngItem> resultingItems;
//    private SearchResultNextPage mNextPage;
//    private String errorMsg;
//
//
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
//        mPageTotalAbleToBeLoaded = mPageLoadedCount + (mNextPage != null ? 1 : 0); // if not null, at least 1 more thing loadable
//        setRefreshing(false);
//
//        updateList();
//    }
//
//
//    public void setResultNewQuery() {
//        resultingItems = null;
//        errorMsg = null;
//        mNextPage = null;
//        setRefreshing(true);
//        mPageLoadedCount = 0;
//        mPageTotalAbleToBeLoaded = 0;
//        updateList();
//    }
//
//    public void setResultError(String e) {
//        resultingItems = null;
//        errorMsg = e;
//        mNextPage = null;
//        setRefreshing(false);
//        mPageLoadedCount = 0;
//        mPageTotalAbleToBeLoaded = 0;
//        updateList();
//    }
//
//    public void setResultCancelled() {
//        resultingItems = null;
//        errorMsg = null;
//        mNextPage = null;
//        setRefreshing(false);
//        mPageLoadedCount = 0;
//        mPageTotalAbleToBeLoaded = 0;
//        updateList();
//    }
//
//
//
//
//    @Override
//    protected String checkIfBad() {
//        String badWhy = null;
//
//        //if (!HTTPController.isThereInternet()) {
//        //   badWhy = "Can't access servers";
//        //} else
//        if (errorMsg != null) {
//            badWhy = errorMsg;
//        } else if (getList() == null) {
//            badWhy = "";
//        } else if (getList().size() <= 0) {
//            badWhy = "No matches";
//            setRefreshing(false);
//        }
//
//        return badWhy;
//    }
//
//    @Override
//    protected boolean canSwipeToRefresh() {
//        return false;
//    }
//
//    @Override
//    protected boolean paginated() {
//        return true;
//    }
//
//    @Override
//    protected int getPageSize() {
//        return 10;
//    }
//
//    @Override
//    protected void loadData(int offset) {
//        // we ignore offset, since thats already prepared outside of this fragment
//        if (mNextPage != null) {
//            setRefreshing(true);
//            mNextPage.requestNextPage();
//        }
//    }
//}

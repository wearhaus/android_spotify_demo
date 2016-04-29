//package com.example.steven.spautify.Fragments;
//
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by Steven on 2/5/2016.
// */
//public class SongSearchResultFragment extends SongListFragment implements SearchResultFragmentInterface {
//
//    private SearchResultAbstract mHelper;
//
//    @Override
//    public MusicLibType getSearchType() {
//        return MusicLibType.Song;
//    }
//
//
//    @Override
//    protected ClickType getClickType() {
//        return ClickType.SearchResult;
//    }
//
//    @Override
//    protected List getList() {
//        return mHelper.resultingItems;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mHelper = new SearchResultAbstract(this, getArguments().getString(TAG_SOURCE_PREFIX));
//    }
//
//    /** @param add if true, add the given items nstead of replace.  When true, r must not be null*/
//    public void setResult(@NonNull ArrayList r, SearchResultNextPage srnp, boolean add) {
//        mHelper.setResult(r, srnp, add);
//    }
//
//    public void setResultNewQuery() {
//        mHelper.setResultNewQuery();
//    }
//
//    public void setResultError(String e) {
//        mHelper.setResultError(e);
//    }
//
//    public void setResultCancelled() {
//        mHelper.setResultCancelled();
//    }
//
//
//
//
//    @Override
//    protected String checkIfBad() {
//        return mHelper.checkIfBad();
//    }
//
//
//    @Override
//    protected boolean paginated() {
//        return true;
//    }
//
//    @Override
//    protected void loadData(int offset) {
//        mHelper.loadData(offset);
//    }
//}

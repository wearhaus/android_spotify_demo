package com.example.steven.spautify.Fragments;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steven on 2/5/2016.
 */
public class AlbumSearchResultFragment extends AlbumsFragment implements SearchResultFragmentInterface {

    private SearchResultFragmentHelper mHelper = new SearchResultFragmentHelper(this);


    @Override
    protected List getList() {
        return mHelper.resultingItems;
    }



    /** @param add if true, add the given items nstead of replace.  When true, r must not be null*/
    public void setResult(@NonNull ArrayList r, SearchResultNextPage srnp, boolean add) {
        mHelper.setResult(r, srnp, add);
    }

    public void setResultingLoading() {
        mHelper.setResultingLoading();
    }

    public void setResultingError(String e) {
        mHelper.setResultingError(e);
    }

    public void setResultingCancelled() {
        mHelper.setResultingCancelled();
    }




    @Override
    protected String checkIfBad() {
        return mHelper.checkIfBad();
    }

    @Override
    protected boolean canSwipeToRefresh() {
        return false;
    }

    @Override
    protected boolean paginated() {
        return true;
    }

    @Override
    protected void loadData(int offset) {
        mHelper.loadData(offset);
    }
}

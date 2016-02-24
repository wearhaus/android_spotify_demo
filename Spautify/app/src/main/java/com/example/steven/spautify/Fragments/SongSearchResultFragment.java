package com.example.steven.spautify.Fragments;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steven on 2/5/2016.
 */
public class SongSearchResultFragment extends SongListFragment {


    @Override
    protected ClickType getClickType() {
        return ClickType.SearchResult;
    }

    @Override
    protected List getList() {
        return resultingItems;
    }



    private ArrayList<SngItem> resultingItems;
    private String errorMsg;

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mListView.setDivider(new ColorDrawable(0x88424646)); // med grey trans50
        return v;
    }*/


    public void setResult(ArrayList<SngItem> r) {
        Log.w("SSSS", "setResult() " + r);
        resultingItems = r;
        errorMsg = null;
        if (r == null) {
            // made invisible only when "no matches" is displayed, or the adapter getView actuallu is called.
            // Since the delay between calling updateList() and the UI updating this list may
            // take up to several seconds of awkwardness
            mLoadingContainer.setVisibility(View.VISIBLE);
        } else {
            mLoadingContainer.setVisibility(View.GONE);
        }
        updateList();
    }

    public void setResultingLoading() {
        Log.w("SSSS", "setResultingLoading() ");
        setResult(null);
    }

    public void setResultingError(String e) {
        Log.w("SSSS", "setResultingError() " + e);
        resultingItems = null;
        errorMsg = e;
        mLoadingContainer.setVisibility(View.GONE);
        updateList();
    }




    @Override
    protected String checkIfBad() {
        String badWhy = null;

        //if (!HTTPController.isThereInternet()) {
        //   badWhy = "Can't access servers";
        //} else
        if (errorMsg != null) {
            badWhy = errorMsg;
        } else if (getList() == null) {
            badWhy = "";
        } else if (getList().size() <= 0) {
            badWhy = "No matches";
            mLoadingContainer.setVisibility(View.GONE);
        }

        return badWhy;
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
}

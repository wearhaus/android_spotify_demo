package com.example.steven.spautify.Fragments;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by Steven on 4/25/2016.
 *
 * Interface to be used with SearchResultFragmentHelper
 */
public interface SearchResultFragmentInterface<I> {


    /** @param add if true, add the given items nstead of replace.  When true, r must not be null*/
    void setResult(@NonNull ArrayList<I> r, DynamicRecycleListFragment.SearchResultNextPage srnp, boolean add);

    void setResultingLoading();

    void setResultingError(String e);

    void setResultingCancelled();


}

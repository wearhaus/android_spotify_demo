package com.example.steven.spautify.Fragments;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by Steven on 4/25/2016.
 *
 * Interface to be used with SearchResultFragmentHelper
 */
public interface SearchResultFragmentInterface<I> {

    public static final String TAG_SOURCE_PREFIX = "arg_source_prefix";

    /** @param add if true, add the given items nstead of replace.  When true, r must not be null*/
    void setResult(@NonNull ArrayList<I> r, DynamicRecycleListFragment.SearchResultNextPage srnp, boolean add);

    void setResultNewQuery();

    void setResultError(String e);

    void setResultCancelled();

    SearchType getSearchType();


}

package com.example.steven.spautify;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

/**
 * Created by Steven on 5/3/2016.
 *
 * https://code.google.com/p/android/issues/detail?id=77712
 * In response to google's refusal to allow SwipeRefreshLayout pattern from being used
 * for auto-loads as well as the normal gesture loads
 *
 * Isn't perfect since it's like 1 actionbar too low at when attached at start, and is only fixed by set false then set true...
 */
public class BetterSwipeRefreshLayout extends SwipeRefreshLayout {


    public BetterSwipeRefreshLayout(Context context) {
        super(context);
    }

    public BetterSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }




    private boolean mMeasured = false;
    private boolean mPreMeasureRefreshing = false;

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!mMeasured) {
            mMeasured = true;
            setRefreshing(mPreMeasureRefreshing);
        }
    }


    @Override
    public void setRefreshing(boolean refreshing) {
        if (mMeasured) {
            super.setRefreshing(refreshing);
        } else {
            mPreMeasureRefreshing = refreshing;
        }
    }
}

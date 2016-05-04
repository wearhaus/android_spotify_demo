package com.example.steven.spautify.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bartoszlipinski.recyclerviewheader.RecyclerViewHeader;
import com.example.steven.spautify.FGoogle;
import com.example.steven.spautify.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steven on 1/15/2016.
 *
 * Abstract class that displays a list of objects represented/referenced via Strings
 *
 * Regrabs data only on onResume().  So if the underlying data changes, another call to onResume is needed
 * or a call to updateList().  Call updateListJustOne(id) if the UI of one object needs to be updated, but the entire
 * list doesn't need to.
 */
public abstract class DynamicRecycleListFragment<
            I,  // Item data type
            V extends RecyclerView.ViewHolder, // view holder for that data type
            A extends RecyclerView.Adapter<V> & DynamicRecycleListFragment.ItemTouchHelperAdapter> // adapter, use a constructor of type I
        extends Fragment {

    //protected ListView mListView;
    //private ArrayAdapter<String> mListAdapter;

    //public abstract class AdapterAbstract<V> implements ItemTouchHelperAdapter {}

    protected RecyclerView mRecyclerView;
    //private QueueAdapter mAdapter;
    private A mAdapter;
    private LinearLayoutManager mLayoutManager;



    private TextView mNoticeText;
//    protected View mLoadingContainer;
    protected View mEverythingContainer;

    //private RelativeLayout mLayout;
    private SwipeRefreshLayout mSwipeRefresh;
//    private boolean mSwipeRefreshingPreMeasure = false;

    private ArrayList<I> mListCloned;

    protected boolean mPageIsLoading;
    /** means [0, mPageLoadedCount) have been loaded and inserted into mList */
    protected int mPageLoadedCount = 0;
    /** SEt to -1 pre-loading, then set whenever we know the max number for this paginated dataset*/
    protected int mPageTotalAbleToBeLoaded = -1;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mf_generic_list, container, false);

        mListCloned = new ArrayList<>();

        mRecyclerView = (RecyclerView)  view.findViewById(R.id.recylcer_list);
        //mRecyclerView.setHasFixedSize(true); we change list size, so can't use

        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        if (getHeaderXml() != 0) {
            // 3rd party lib to support old ListView-like header behavior and api
            RecyclerViewHeader header = RecyclerViewHeader.fromXml(view.getContext(), getHeaderXml());
            header.attachTo(mRecyclerView);
        }

        // vvv this creates the divider between items; took only a a couple hundred lines of code compared to the 1 line for ListViews
        mRecyclerView.addItemDecoration(new FGoogle(getActivity(), FGoogle.VERTICAL_LIST));


        mAdapter = createNewAdapter(view.getContext(), mListCloned);
        mRecyclerView.setAdapter(mAdapter);

        if (paginated()) {
            mRecyclerView.addOnScrollListener(mRecyclerViewOnScrollListener);
        }

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);




        mNoticeText = (TextView) view.findViewById(R.id.notice_text);
        mEverythingContainer = view.findViewById(R.id.everything_container);
//        mLoadingContainer = view.findViewById(R.id.spin_kit);

        /*
            So every onResume, we regrab the list.  There is no other way to get any changes to the list.
            This is good since if someone removes a song, it won't be immediately removed until onResume gets called
            again (unless you listen to the notifier for changes).
            ProviderLoading songs from the server doesn't cause a regrab of the list.  The list itself is cloned, so
            changes that happen while this is open don't need a notify.

         */


        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setColorSchemeResources(
                R.color.cyan,
                android.R.color.holo_blue_dark,
                android.R.color.holo_purple,
                R.color.cyan_halved_with_white);

        if (!canSwipeToRefresh()) {
            mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {
                    mSwipeRefresh.setRefreshing(true);

                    onSwipeRefresh();
                }

            });
            mSwipeRefresh.setEnabled(false);
        }


        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        updateList();
        super.onResume();
    }


    protected abstract List<I> getList();

    //protected abstract ArrayAdapter<String> createNewAdapter(Context context, int resourceId, List<String> mList);
    protected abstract A createNewAdapter(Context context, ArrayList<I> mList);

    /** Returns null if list is accessible, or String reason
     * why it can't get the list.*/
    protected abstract String checkIfBad();

    protected abstract boolean canSwipeToRefresh();

    protected abstract boolean canDragAndDrop();

    protected void onSwipeRefresh() {}

    protected void setLoading(boolean b) {
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setRefreshing(b);
        }

    }

    /** Regrab and rerender the entire list.*/
    protected void updateList() {
        updateList(null);
    }

    protected void updateListJustOne(String justUpdateThisId) {
        Log.d("DynamicListFragment", "updateListJustOne() " + justUpdateThisId);
        updateList(justUpdateThisId);
    }

    /**
     *
     * @param justUpdateThisId  If null, refreshes entire list by recalling getList().  Otherwise, just refreshes the one object(s)
     * with this id.  Reason for this is so we don't waste time refreshing the entire list, nor do we risk
     * keeping pointers to views possibly later than they should be kept.
     */
    private void updateList(final String justUpdateThisId) {
        // post, to make sure it is running on the UI thread
        Activity act = getActivity();
        if (act != null) act.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String badWhy = checkIfBad();
                if (badWhy == null) {
                    mNoticeText.setText("");
                    mNoticeText.setVisibility(View.GONE);
                    if (justUpdateThisId != null) {

                        // ref: http://stackoverflow.com/questions/2123083/android-listview-refresh-single-row

                        // Would take work to get working
//                        int start = mListView.getFirstVisiblePosition();
//                        for (int i=start, j= mListView.getLastVisiblePosition(); i<=j; i++) {
//
//                            if (justUpdateThisId.equals(mListView.getItemAtPosition(i))) {
//                                View view = mListView.getChildAt(i-start);
//                                //getView calls Adapter's getView to refresh the single item's view, even if we don't do anything with the returned view here.
//                                mListView.getAdapter().getView(i, view, mListView);
//                                break;
//                            }
//                        }

                    } else {
                        // list itself may be wrong; regrab it and clone it.
                        mListCloned.clear();
                        List<I> si = getList();
                        if (si != null) {
                            mListCloned.addAll(si);
                        }
                        mAdapter.notifyDataSetChanged();
                    }

                } else {

                    mNoticeText.setText(badWhy);
                    mNoticeText.setVisibility(View.VISIBLE);


                    mListCloned.clear();
                    mAdapter.notifyDataSetChanged();
                }


            }
        });
    }


    /** Returns 0 by default which means no header layout.
     * Override and return the resid of a layout which will finish inflating
     * at the end of super.onCreateView*/
    protected int getHeaderXml() {
        return 0;
    }

    protected abstract boolean paginated();

//    protected int getPageSize() {
//        return 50;
//    }


    private void loadNewPage() {
        if (mPageTotalAbleToBeLoaded == -1 || mPageLoadedCount >= mPageTotalAbleToBeLoaded) {
            return;
        }
        loadData(mPageLoadedCount);
    }

    /** Override if paginated.  Set mPageIsLoading in here, and then off when data is loaded, errored, or timedout
     * This will get called when the user scrolls.  Call yourself at the start of the fragment*/
    protected void loadData(int offset) {

    }




    public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

        private final ItemTouchHelperAdapter mAdapter;

        public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return canDragAndDrop();
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;

            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            //mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }

    }

    public interface ItemTouchHelperAdapter {
        void onItemMove(int fromPosition, int toPosition);
        void onItemDismiss(int position);
    }

    // https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.5j61b2qv3


    private RecyclerView.OnScrollListener mRecyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();


            if (!mPageIsLoading) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        //&& totalItemCount >= getPageSize()
                        ) {

                    Log.i("temp", "end of items in adapter");

                    loadNewPage();
                }
            }
        }
    };



}


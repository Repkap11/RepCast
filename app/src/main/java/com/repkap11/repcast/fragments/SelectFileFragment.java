package com.repkap11.repcast.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.repkap11.repcast.R;
import com.repkap11.repcast.model.adapters.FileListAdapter;
import com.repkap11.repcast.model.parcelables.JsonDirectory;


public class SelectFileFragment extends RepcastFragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = SelectFileFragment.class.getSimpleName();
    private static final String INSTANCE_STATE_DIR = "INSTANCE_STATE_DIR";
    private static final String INSTANCE_STATE_SCROLL_POS = "INSTANCE_STATE_SCROLL_POS";
    private FileListAdapter mAdapter;
    private JsonDirectory.JsonFileDir mDirectory;
    private int mScrollPosition = 0;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    public static SelectFileFragment newInstance(JsonDirectory.JsonFileDir dir) {
        SelectFileFragment fragment = new SelectFileFragment();
        fragment.mDirectory = dir;
        //Log.e(TAG, "Fragment Created");
        return fragment;
    }

    @Override
    public boolean onQuerySubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryChange(String string) {
        if (mAdapter == null) {
            return true;
        }
        setResultsEmptyString(string);
        mAdapter.getFilter().filter(string);
        return true;
    }

    @Override
    public Parcelable getParceable() {
        return mDirectory;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (RepcastFragment.DO_SAVE_STATE) {
                if (mDirectory == null) {
                    mDirectory = savedInstanceState.getParcelable(INSTANCE_STATE_DIR);
                    mScrollPosition = savedInstanceState.getInt(INSTANCE_STATE_SCROLL_POS);
                }
            }
        }
        mAdapter = new FileListAdapter(this, mDirectory);
        setShouldProgressBeShown(true);
        if (getActivity() != null) {
            mAdapter.updateContext(this);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_content, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.fragment_repcast_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                R.color.primary_dark,
                R.color.primary);

        setRetainInstance(DO_RETAIN_INSTANCE);
        initProgressAndEmptyMessage(rootView);
        setListAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //Log.e(TAG, "View Created:" + mScrollPosition);
        super.onViewCreated(view, savedInstanceState);
        if (mScrollPosition != 0) {
            getListView().setSelection(mScrollPosition);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mAdapter != null) {
            mAdapter.updateContext(this);
        }
    }

    @Override
    public void notifyNotRefreshing() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        super.notifyNotRefreshing();
    }

    @Override
    public void onDestroyView() {
        //Log.e(TAG,"onDestroyView");
        mScrollPosition = getListView().getFirstVisiblePosition();
        super.onDestroyView();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (RepcastFragment.DO_SAVE_STATE) {
            outState.putParcelable(INSTANCE_STATE_DIR, mDirectory);
            outState.putInt(INSTANCE_STATE_SCROLL_POS, getListView().getFirstVisiblePosition());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public String getName() {
        return mDirectory.name;
    }

    @Override
    public void onRefresh() {
        if (mAdapter != null) {
            setShouldProgressBeShown(true);
            mAdapter.refreshContent(this);
        }
    }
}

package com.repkap11.repcast.activities.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.SelectTorrentActivity;
import com.repkap11.repcast.model.TorrentListAdapter;


public class SelectTorrentFragment extends RepcastFragment {

    private static final String TAG = SelectTorrentFragment.class.getSimpleName();
    private static final String INSTANCE_STATE_QUERY = "INSTANCE_STATE_QUERY";
    private TorrentListAdapter mAdapter;
    private AbsListView mListView;
    private String mQuery;

    public SelectTorrentFragment() {
        Log.e(TAG, "Fragment Created");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mQuery = savedInstanceState.getString(INSTANCE_STATE_QUERY);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_selectfile, container, false);
        rootView.setKeepScreenOn(true);
        mListView = (AbsListView) rootView.findViewById(R.id.fragment_selectfile_list);
        setRetainInstance(true);

        if (mAdapter == null) {
            mAdapter = new TorrentListAdapter(mQuery);
        }
        mAdapter.updateContext((SelectTorrentActivity) getActivity());
        mListView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(INSTANCE_STATE_QUERY, mQuery);
        super.onSaveInstanceState(outState);
    }

    @Override
    public String getName() {
        return mQuery;
    }

    @Override
    public boolean onQuerySubmit(String query) {
        searchForTorrentsWithName(query);
        return true;
    }

    @Override
    public boolean onQueryChange(String newText) {
        return false;
    }

    public void searchForTorrentsWithName(String query) {
        mQuery = query;
        mAdapter = new TorrentListAdapter(query);
    }
}

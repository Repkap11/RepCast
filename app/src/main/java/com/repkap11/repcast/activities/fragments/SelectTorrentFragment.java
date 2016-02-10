package com.repkap11.repcast.activities.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.RepcastActivity;
import com.repkap11.repcast.model.JsonTorrent;
import com.repkap11.repcast.model.TorrentListAdapter;


public class SelectTorrentFragment extends RepcastFragment {

    private static final String TAG = SelectTorrentFragment.class.getSimpleName();
    private static final String INSTANCE_STATE_QUERY = "INSTANCE_STATE_QUERY";
    private TorrentListAdapter mAdapter;
    private AbsListView mListView;
    private JsonTorrent.JsonTorrentResult mTorrent;

    public SelectTorrentFragment() {
        Log.e(TAG, "Fragment Created");

    }

    public static SelectTorrentFragment newInstance(JsonTorrent.JsonTorrentResult dir) {
        SelectTorrentFragment fragment = new SelectTorrentFragment();
        fragment.mTorrent = dir;
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            //mQuery = savedInstanceState.getString(INSTANCE_STATE_QUERY);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_selectfile, container, false);
        rootView.setKeepScreenOn(true);
        mListView = (AbsListView) rootView.findViewById(R.id.fragment_selectfile_list);
        setRetainInstance(true);
        mAdapter = new TorrentListAdapter(mTorrent.name);
        mAdapter.updateContext((RepcastActivity) getActivity());
        mListView.setAdapter(mAdapter);
        //searchForTorrentsWithName(mTorrent.name);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //outState.putString(INSTANCE_STATE_QUERY, mQuery);
        super.onSaveInstanceState(outState);
    }

    @Override
    public String getName() {
        return mTorrent.name == null ? getResources().getString(R.string.add_torrent_initial_title) : mTorrent.name;
    }

    @Override
    public boolean onQuerySubmit(String query) {
        Log.e(TAG, "Stringing Query with string: " + query);
        searchForTorrentsWithName(query);
        return true;
    }

    @Override
    public boolean onQueryChange(String newText) {
        return false;
    }

    @Override
    public void doFragmentTransition(FragmentManager fm) {
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.add(this, "no tag 2");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void searchForTorrentsWithName(String query) {
        mTorrent.name = query;
        mAdapter = new TorrentListAdapter(query);
        RepcastActivity activity = (RepcastActivity) getActivity();
        mAdapter.updateContext(activity);
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }
        if (activity != null) {
            activity.setTitleBasedOnFragment();
        }
    }
}

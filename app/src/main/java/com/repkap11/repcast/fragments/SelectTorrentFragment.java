package com.repkap11.repcast.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.RepcastActivity;
import com.repkap11.repcast.model.parcelables.JsonTorrent;
import com.repkap11.repcast.model.adapters.TorrentListAdapter;


public class SelectTorrentFragment extends RepcastFragment {

    private static final String TAG = SelectTorrentFragment.class.getSimpleName();
    private static final String INSTANCE_STATE_QUERY = "INSTANCE_STATE_QUERY";
    private TorrentListAdapter mAdapter;
    private JsonTorrent.JsonTorrentResult mTorrent;

    public static SelectTorrentFragment newInstance(RepcastActivity.BackStackData data) {
        SelectTorrentFragment fragment = new SelectTorrentFragment();
        fragment.mTorrent = (JsonTorrent.JsonTorrentResult) data.data;
        Log.e(TAG, "Fragment Created");
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (RepcastFragment.DO_SAVE_STATE) {
                if (mTorrent == null) {
                    mTorrent = savedInstanceState.getParcelable(INSTANCE_STATE_QUERY);
                }
            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_content, container, false);
        SwipeRefreshLayout refresh = (SwipeRefreshLayout)rootView.findViewById(R.id.fragment_repcast_swipe_refresh);
        refresh.setEnabled(false);
        setRetainInstance(RepcastFragment.DO_RETAIN_INSTANCE);
        initProgressAndEmptyMessage(rootView);
        setListAdapter(mAdapter);

        //searchForTorrentsWithName(mTorrent.name);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mAdapter != null) {
            mAdapter.updateContext(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mAdapter != null) {
            //mAdapter.updateContext(null);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (RepcastFragment.DO_SAVE_STATE) {
            outState.putParcelable(INSTANCE_STATE_QUERY, mTorrent);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public String getName() {
        return mTorrent.name == null ? getResources().getString(R.string.add_torrent_initial_title) : mTorrent.name;
    }

    @Override
    public boolean onQuerySubmit(String query) {
        setResultsEmptyString(query);
        Log.e(TAG, "Stringing Query with string: " + query);
        searchForTorrentsWithName(query);
        return true;
    }

    @Override
    public boolean onQueryChange(String newText) {
        return false;
    }

    @Override
    public Parcelable getParceable() {
        return mTorrent;
    }

    public void searchForTorrentsWithName(String query) {
        mTorrent.name = query;
        mAdapter = new TorrentListAdapter(query, this);
        setShouldProgressBeShown(true);
        RepcastActivity activity = (RepcastActivity) getActivity();
        mAdapter.updateContext(this);
        setListAdapter(mAdapter);
        if (activity != null) {
            activity.setTitleBasedOnFragment();
        }
    }
}

package com.repkap11.repcast.activities.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Toast;

import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.RepcastActivity;
import com.repkap11.repcast.model.JsonTorrent;
import com.repkap11.repcast.model.JsonTorrentUploader;
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

        if (mAdapter == null && mQuery != null) {
            mAdapter = new TorrentListAdapter(mQuery);
            mAdapter.updateContext(this);
            mListView.setAdapter(mAdapter);
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(INSTANCE_STATE_QUERY, mQuery);
        super.onSaveInstanceState(outState);
    }

    @Override
    public String getName() {
        return mQuery == null ? getResources().getString(R.string.add_torrent_initial_title) : mQuery;
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
    public void doShowContent(FragmentManager fm, Parcelable data) {
        JsonTorrent.JsonTorrentResult torrent = (JsonTorrent.JsonTorrentResult) data;
        searchForTorrentsWithName(torrent.name);
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(this.getId(), this);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void searchForTorrentsWithName(String query) {
        mQuery = query;
        mAdapter = new TorrentListAdapter(query);
        RepcastActivity activity = (RepcastActivity) getActivity();
        mAdapter.updateContext(this);
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }
        if (activity != null) {
            activity.setTitleBasedOnFragment();
        }
    }

    public void uploadTorrent(JsonTorrent.JsonTorrentResult element) {
        Log.e(TAG, "Should start download of torrent " + element.name);
        String magnetLink64 = Base64.encodeToString(element.magnetLink.getBytes(), Base64.NO_WRAP);

        String url = "https://repkam09.com/dl/toradd/" + magnetLink64;
        JsonTorrentUploader uploader = new JsonTorrentUploader(this);
        uploader.execute(url);

    }

    public void torrentUploadComplete(Integer resultCode) {
        Toast.makeText(getActivity(), "Result:" + resultCode, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Got Result:" + resultCode);
    }
}

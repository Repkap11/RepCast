package com.repkap11.repcast.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.LocalPlayerActivity;
import com.repkap11.repcast.model.FileListAdapter;
import com.repkap11.repcast.model.JsonDirectory;


public class SelectFileFragment extends RepcastFragment {

    private static final String TAG = SelectFileFragment.class.getSimpleName();
    private static final String INSTANCE_STATE_DIR = "INSTANCE_STATE_DIR";
    private FileListAdapter mAdapter;
    private JsonDirectory.JsonFileDir mDirectory;
    private AbsListView mListView;

    @Override
    public boolean onQuerySubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryChange(String string) {
        mAdapter.getFilter().filter(string);
        return true;
    }

    @Override
    public void doShowContent(FragmentManager fm, Parcelable data) {
        SelectFileFragment newFragment = new SelectFileFragment();
        JsonDirectory.JsonFileDir dir = (JsonDirectory.JsonFileDir) data;
        newFragment.showListUsingDirectory(dir);
        FragmentTransaction transaction = fm.beginTransaction();
        if (!dir.isRoot) {
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        }
        transaction.replace(this.getId(), newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mDirectory = savedInstanceState.getParcelable(INSTANCE_STATE_DIR);
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
            mAdapter = new FileListAdapter(mDirectory.path64);
        }
        mAdapter.updateContext(this);
        mListView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(INSTANCE_STATE_DIR, mDirectory);
        super.onSaveInstanceState(outState);
    }

    public void showListUsingDirectory(JsonDirectory.JsonFileDir dir) {
        mDirectory = dir;
        mAdapter = new FileListAdapter(dir.path64);
        mAdapter.updateContext(this);
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }
    }

    @Override
    public String getName() {
        return mDirectory.name;
    }

    public void showFile(JsonDirectory.JsonFileDir dir) {
        Log.e(TAG, "Starting file:" + dir.name);
        Intent intent = new Intent();
        intent.setClass(getActivity(), LocalPlayerActivity.class);
        intent.putExtra("media", dir);
        intent.putExtra("shouldStart", false);
        Log.e(TAG, "About to cast:" + dir.path);
        startActivity(intent);
    }
}

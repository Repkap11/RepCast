package com.repkap11.chromecasturl.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.repkap11.chromecasturl.FileListAdapter;
import com.repkap11.chromecasturl.R;
import com.repkap11.chromecasturl.model.JsonDirectory;

public class SelectFileFragment extends Fragment {

    private static final String TAG = SelectFileFragment.class.getSimpleName();
    private FileListAdapter mAdapter;
    private AbsListView mListView;

    public SelectFileFragment() {
        Log.e(TAG, "Fragment Created");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_selectfile, container, false);
        rootView.setKeepScreenOn(true);
        mListView = (AbsListView) rootView.findViewById(R.id.fragment_selectfile_list);
        setRetainInstance(true);
        mAdapter.updateContext((SelectFileActivity) getActivity());
        mListView.setAdapter(mAdapter);
        return rootView;
    }

    public void showListUsingDirectory(JsonDirectory.JsonFileDir dir) {
        mAdapter = new FileListAdapter(dir.path64);



    }
}

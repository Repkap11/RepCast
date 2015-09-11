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

public class SelectFileFragment extends Fragment {

    private static final String TAG = SelectFileFragment.class.getSimpleName();
    private final FileListAdapter mAdapter;

    public SelectFileFragment() {
        Log.e(TAG, "Fragment Created");
        mAdapter = new FileListAdapter("https://repkam09.com/dl/dirget/");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_selectfile, container, false);
        rootView.setKeepScreenOn(true);
        AbsListView mListView = (AbsListView) rootView.findViewById(R.id.fragment_selectfile_list);
        mAdapter.updateContext(getActivity());
        mListView.setAdapter(mAdapter);
        setRetainInstance(true);
        return rootView;
    }
}

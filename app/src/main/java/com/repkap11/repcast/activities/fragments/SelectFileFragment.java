package com.repkap11.repcast.activities.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.repkap11.repcast.R;
import com.repkap11.repcast.model.FileListAdapter;
import com.repkap11.repcast.model.JsonDirectory;


public class SelectFileFragment extends com.repkap11.repcast.activities.fragments.RepcastFragment {

    private static final String TAG = SelectFileFragment.class.getSimpleName();
    private static final String INSTANCE_STATE_DIR = "INSTANCE_STATE_DIR";
    private FileListAdapter mAdapter;
    private JsonDirectory.JsonFileDir mDirectory;


    public static SelectFileFragment newInstance(JsonDirectory.JsonFileDir dir) {
        SelectFileFragment fragment = new SelectFileFragment();
        fragment.mDirectory = dir;
        Log.e(TAG, "Fragment Created");
        return fragment;
    }
    @Override
    public boolean onQuerySubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryChange(String string) {
        if (mAdapter == null){
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
            if (com.repkap11.repcast.activities.fragments.RepcastFragment.DO_SAVE_STATE) {
                if (mDirectory == null) {
                    mDirectory = savedInstanceState.getParcelable(INSTANCE_STATE_DIR);
                }
            }
        }
        mAdapter = new FileListAdapter(mDirectory.path64);
        setShouldProgressBeShown(true);
        if (getActivity() != null) {
            mAdapter.updateContext(this);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_content, container, false);
        setRetainInstance(DO_RETAIN_INSTANCE);
        initProgressAndEmptyMessage(rootView);
        setListAdapter(mAdapter);
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
        if (com.repkap11.repcast.activities.fragments.RepcastFragment.DO_SAVE_STATE) {
            outState.putParcelable(INSTANCE_STATE_DIR, mDirectory);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public String getName() {
        return mDirectory.name;
    }
}

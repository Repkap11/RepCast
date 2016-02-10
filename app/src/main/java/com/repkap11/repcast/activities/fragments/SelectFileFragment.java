package com.repkap11.repcast.activities.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.RepcastActivity;
import com.repkap11.repcast.model.FileListAdapter;
import com.repkap11.repcast.model.JsonDirectory;


public class SelectFileFragment extends RepcastFragment {

    private static final String TAG = SelectFileFragment.class.getSimpleName();
    private static final String INSTANCE_STATE_DIR = "INSTANCE_STATE_DIR";
    private FileListAdapter mAdapter;
    private JsonDirectory.JsonFileDir mDirectory;
    private AbsListView mListView;

    public static SelectFileFragment newInstance(JsonDirectory.JsonFileDir dir) {
        SelectFileFragment fragment = new SelectFileFragment();
        fragment.mDirectory = dir;
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
        mAdapter.getFilter().filter(string);
        return true;
    }

    @Override
    public void doFragmentTransition(FragmentManager fm) {
        FragmentTransaction transaction = fm.beginTransaction();
        if (!mDirectory.isRoot) {
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        }
        transaction.add(this,"no tag");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            //mDirectory = savedInstanceState.getParcelable(INSTANCE_STATE_DIR);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_selectfile, container, false);
        rootView.setKeepScreenOn(true);
        mListView = (AbsListView) rootView.findViewById(R.id.fragment_selectfile_list);
        setRetainInstance(true);
        mAdapter = new FileListAdapter(mDirectory.path64);
        mAdapter.updateContext((RepcastActivity) getActivity());
        mListView.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //outState.putParcelable(INSTANCE_STATE_DIR, mDirectory);
        super.onSaveInstanceState(outState);
    }

    @Override
    public String getName() {
        return mDirectory.name;
    }
}

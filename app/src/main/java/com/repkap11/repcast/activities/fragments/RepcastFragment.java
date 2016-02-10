package com.repkap11.repcast.activities.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;


public abstract class RepcastFragment extends Fragment {

    private static final String TAG = RepcastFragment.class.getSimpleName();

    public RepcastFragment() {
        Log.e(TAG, "Fragment Created");

    }
    public abstract String getName();

    public abstract boolean onQuerySubmit(String query);

    public abstract boolean onQueryChange(String newText);

    protected static void assertThat(boolean b) {
        if (!b){
            throw new RuntimeException("Assertion Failed");
        }
    }

    public abstract void doFragmentTransition(FragmentManager mFragmentManager);
}
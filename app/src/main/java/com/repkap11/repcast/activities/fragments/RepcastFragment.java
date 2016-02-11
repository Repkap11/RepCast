package com.repkap11.repcast.activities.fragments;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;


public abstract class RepcastFragment extends Fragment {

    private static final String TAG = RepcastFragment.class.getSimpleName();
    protected static final boolean DO_SAVE_STATE = false;
    protected static final boolean DO_RETAIN_INSTANCE = true;

    public RepcastFragment() {

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

    public abstract Parcelable getParceable();
}

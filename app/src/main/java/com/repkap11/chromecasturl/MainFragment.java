package com.repkap11.chromecasturl;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.media.MediaRouteSelector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.cast.CastDevice;

public class MainFragment extends Fragment {

    private MediaRouteSelector mMediaRouteSelector;
    private CastDevice mSelectedDevice;

    public MainFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        rootView.setKeepScreenOn(true);
        return rootView;
    }
}

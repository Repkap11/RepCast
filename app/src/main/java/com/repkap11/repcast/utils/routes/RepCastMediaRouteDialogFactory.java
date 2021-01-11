package com.repkap11.repcast.utils.routes;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.mediarouter.app.MediaRouteChooserDialogFragment;
import androidx.mediarouter.app.MediaRouteDialogFactory;

public class RepCastMediaRouteDialogFactory extends MediaRouteDialogFactory {
    private static final MediaRouteDialogFactory sRepDefault = new RepCastMediaRouteDialogFactory();
    private static final String TAG = RepCastMediaRouteDialogFactory.class.getSimpleName();

    public static MediaRouteDialogFactory getRepDefault() {
        return sRepDefault;
    }

    @NonNull
    @Override
    public MediaRouteChooserDialogFragment onCreateChooserDialogFragment() {
        Log.e(TAG, "RepCastMediaRouteDialogFactory getting dialog!!");
        MediaRouteChooserDialogFragment frag = new RepMediaRouteChooserDialogFragment();
        return frag;
    }
}

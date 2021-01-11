package com.repkap11.repcast.utils.routes;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.mediarouter.app.MediaRouteActionProvider;
import androidx.mediarouter.app.MediaRouteDialogFactory;


public class RepCastMediaRouteActionProvider extends MediaRouteActionProvider {

    private static final String TAG = RepCastMediaRouteActionProvider.class.getSimpleName();
    /**
     * Creates the action provider.
     *
     * @param context The context.
     */
    private MediaRouteDialogFactory mDialogFactory = RepCastMediaRouteDialogFactory.getRepDefault();

    public RepCastMediaRouteActionProvider(Context context) {
        super(context);
        Log.e(TAG, "MediaRouteDialogFactory created");
        //setDialogFactory(mDialogFactory);
        Log.e(TAG, "MediaRouteDialogFactory postCalled");

    }

    @Override
    public void setDialogFactory(@NonNull MediaRouteDialogFactory factory) {
        Log.e(TAG, "MediaRouteDialogFactory calling super setDialogFactory");
        super.setDialogFactory(mDialogFactory);
    }
}

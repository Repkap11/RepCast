package com.repkap11.repcast.utils.routes;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.app.MediaRouteDialogFactory;
import android.util.Log;


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

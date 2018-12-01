package com.repkap11.repcast.utils.routes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.app.MediaRouteChooserDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;

import com.repkap11.repcast.R;


public class RepCastMediaRouteChooserDialog extends MediaRouteChooserDialog {
    private static final String TAG = RepCastMediaRouteChooserDialog.class.getSimpleName();

    public RepCastMediaRouteChooserDialog(Context context) {
        super(context);
        Log.e(TAG, "RepCastMediaRouteChooserDialog Created");
    }

    public RepCastMediaRouteChooserDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().setDimAmount(0.2f);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

}

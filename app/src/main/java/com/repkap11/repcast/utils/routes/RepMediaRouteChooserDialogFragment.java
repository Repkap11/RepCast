package com.repkap11.repcast.utils.routes;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.MediaRouteChooserDialog;
import android.support.v7.app.MediaRouteChooserDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RepMediaRouteChooserDialogFragment extends MediaRouteChooserDialogFragment {
    @Override
    public MediaRouteChooserDialog onCreateChooserDialog(Context context, Bundle savedInstanceState) {
        return new RepCastMediaRouteChooserDialog(context);
    }
}

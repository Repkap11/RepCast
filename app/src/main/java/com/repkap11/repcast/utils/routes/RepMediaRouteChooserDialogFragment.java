package com.repkap11.repcast.utils.routes;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.mediarouter.app.MediaRouteChooserDialog;
import androidx.mediarouter.app.MediaRouteChooserDialogFragment;

public class RepMediaRouteChooserDialogFragment extends MediaRouteChooserDialogFragment {
    @Override
    public MediaRouteChooserDialog onCreateChooserDialog(Context context, Bundle savedInstanceState) {
        return new RepCastMediaRouteChooserDialog(context);
    }
}

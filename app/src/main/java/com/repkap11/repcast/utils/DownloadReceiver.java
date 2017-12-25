package com.repkap11.repcast.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by paul on 12/25/17.
 */

public class DownloadReceiver extends BroadcastReceiver {
    private static final String TAG = DownloadReceiver.class.getSimpleName();
    private long mDownloadId = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
        if (id != mDownloadId) {
            //Log.v(TAG, "Ingnoring unrelated download " + id);
            return;
        }
        //Log.e(TAG, "Download Broadcast:" + intent);
    }
}

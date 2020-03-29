package com.repkap11.repcast.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.repkap11.repcast.R;
import com.repkap11.repcast.model.parcelables.JsonDirectory;

/**
 * Created by paul on 12/22/17.
 */

public class DownloadDialogFragment extends DialogFragment {

    private static final String TAG = DownloadDialogFragment.class.getSimpleName();
    public static final String ARG_DIR = "ARG_DIR";


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.download_from_repcast, null, false);
        JsonDirectory.JsonFileDir dir = getArguments().getParcelable(ARG_DIR);
        alert.setMessage(dir.name);
        alert.setTitle(R.string.download_from_repcast_title);
        alert.setView(rootView);
        alert.setPositiveButton(R.string.download_from_repcast_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                JsonDirectory.JsonFileDir dir = getArguments().getParcelable(ARG_DIR);
                Log.e(TAG, "Start Download:" + dir.name + ":" + dir.path);
                downloadFile(dir);
            }
        });
        alert.setNegativeButton(R.string.download_from_repcast_copy_url, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                JsonDirectory.JsonFileDir dir = getArguments().getParcelable(ARG_DIR);
                Log.e(TAG, "Path copied to clipboard:" + dir.name + ":" + dir.path);
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(null, dir.path);
                clipboard.setPrimaryClip(clip);
           }
        });
        alert.setNeutralButton(R.string.download_from_repcast_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                Log.e(TAG, "NO");
            }
        });
        AlertDialog dialog = alert.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;

    }

    private void downloadFile(JsonDirectory.JsonFileDir dir) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(dir.path));

        // only download via WIFI
        request.setAllowedOverMetered(false);
        request.setDescription(getResources().getString(R.string.app_name));
        request.setTitle(dir.name);

        // we just want to download silently
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.allowScanningByMediaScanner();
        request.setDestinationInExternalPublicDir("/RepCast", dir.name);

        // enqueue this request
        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadID = downloadManager.enqueue(request);

        //IntentFilter downloadCompleteIntentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        //downloadCompleteIntentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        //downloadCompleteIntentFilter.addAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
        //BroadcastReceiver receiver = new DownloadReceiver();
        //getActivity().registerReceiver(receiver, downloadCompleteIntentFilter);

    }
}



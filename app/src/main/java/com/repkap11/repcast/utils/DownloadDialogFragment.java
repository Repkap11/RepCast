package com.repkap11.repcast.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.RepcastActivity;
import com.repkap11.repcast.model.parcelables.JsonDirectory;

/**
 * Created by paul on 12/22/17.
 */

public class DownloadDialogFragment extends DialogFragment {

    private static final String TAG = DownloadDialogFragment.class.getSimpleName();
    public static final String ARG_DIR = "ARG_DIR";


//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
//        View rootView = getActivity().getLayoutInflater().inflate(R.layout.download_from_repcast, null, false);
//        JsonDirectory.JsonFileDir dir = getArguments().getParcelable(ARG_DIR);
//        alert.setMessage(dir.name);
//        alert.setTitle(R.string.download_from_repcast_title);
//        alert.setView(rootView);
//        alert.setPositiveButton(R.string.download_from_repcast_yes, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                JsonDirectory.JsonFileDir dir = getArguments().getParcelable(ARG_DIR);
//                Log.e(TAG, "Start Download:" + dir.name + ":" + dir.path);
//                downloadFile(dir);
//            }
//        });
//        alert.setNegativeButton(R.string.download_from_repcast_copy_url, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                JsonDirectory.JsonFileDir dir = getArguments().getParcelable(ARG_DIR);
//                Log.e(TAG, "Path copied to clipboard:" + dir.name + ":" + dir.path);
//                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
//                ClipData clip = ClipData.newPlainText(null, dir.path);
//                clipboard.setPrimaryClip(clip);
//           }
//        });
//        alert.setNeutralButton(R.string.download_from_repcast_cancel, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                // what ever you want to do with No option.
//                Log.e(TAG, "NO");
//            }
//        });
//        AlertDialog dialog = alert.create();
//        dialog.setCanceledOnTouchOutside(false);
//        return dialog;
//
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.download_from_repcast, null, false);
        final JsonDirectory.JsonFileDir dir = getArguments().getParcelable(ARG_DIR);

        alert.setItems(new CharSequence[]
                        {
                                getResources().getString(R.string.download_from_repcast_yes),
                                getResources().getString(R.string.download_from_repcast_copy_url),
                                getResources().getString(R.string.download_from_repcast_force_share),
                                getResources().getString(R.string.download_from_repcast_cancel)
                        },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
//                            JsonDirectory.JsonFileDir dir = getArguments().getParcelable(ARG_DIR);
                            case 0:
                                Log.e(TAG, "Start Download:" + dir.name + ":" + dir.path);
                                ((RepcastActivity) getActivity()).downloadFile(dir);
                                break;
                            case 1:
                                Log.e(TAG, "Path copied to clipboard:" + dir.name + ":" + dir.path);
                                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText(null, dir.path);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(getActivity(), R.string.download_from_copied_to_clipboard, Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
                                ((RepcastActivity) getActivity()).showFile(dir, true);
                                break;
                            case 3:
                                Log.e(TAG, "NO");
                                break;
                        }
                    }
                });

//        alert.setMessage(dir.name);
        alert.setTitle(dir.name);
//        alert.setTitle(R.string.download_from_repcast_title);
//        alert.setView(rootView);
        AlertDialog dialog = alert.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;

    }

}



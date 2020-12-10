/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.repkap11.repcast.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.PopupMenu;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.CastException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.RepcastActivity;
import com.repkap11.repcast.application.CastApplication;
import com.repkap11.repcast.model.parcelables.JsonDirectory;
import com.repkap11.repcast.queue.QueueDataProvider;

import java.io.IOException;

/**
 * A collection of utility methods, all static.
 */
public class Utils {

    private static final String TAG = "Utils";
    private static final boolean DAD_TEST = false;
    private static final String BACKEND_NAME = "BACKEND";
    private static final String BACKEND_KEY = "backend_url";
    private static final String USE_DEFAULT_BACKEND_KEY = "use_default_backend";

    /*
     * Making sure public utility methods remain static
     */
    private Utils() {
    }

    @SuppressWarnings("deprecation")
    /**
     * Returns the screen/display size
     *
     */
    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        return new Point(width, height);
    }

    /**
     * Returns {@code true} if and only if the screen orientation is portrait.
     */
    public static boolean isOrientationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * Shows an error dialog with a given text message.
     */
    public static void showErrorDialog(Context context, String errorString) {
        new AlertDialog.Builder(context).setTitle(R.string.error)
                .setMessage(errorString)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    /**
     * Shows an "Oops" error dialog with a text provided by a resource ID
     */
    public static void showOopsDialog(Context context, int resourceId) {
        new AlertDialog.Builder(context).setTitle(R.string.oops)
                .setMessage(context.getString(resourceId))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setIcon(R.drawable.ic_action_alerts_and_states_warning)
                .create()
                .show();
    }

    /**
     * A utility method to handle a few types of exceptions that are commonly thrown by the cast
     * APIs in this library. It has special treatments for
     * {@link TransientNetworkDisconnectionException}, {@link NoConnectionException} and shows an
     * "Oops" dialog conveying certain messages to the user. The following resource IDs can be used
     * to control the messages that are shown:
     * <p/>
     * <ul>
     * <li><code>R.string.connection_lost_retry</code></li>
     * <li><code>R.string.connection_lost</code></li>
     * <li><code>R.string.failed_to_perform_action</code></li>
     * </ul>
     */
    public static void handleException(Context context, Exception e) {
        int resourceId;
        if (e instanceof TransientNetworkDisconnectionException) {
            // temporary loss of connectivity
            resourceId = R.string.connection_lost_retry;

        } else if (e instanceof NoConnectionException) {
            // connection gone
            resourceId = R.string.connection_lost;
        } else if (e instanceof RuntimeException ||
                e instanceof IOException ||
                e instanceof CastException) {
            // something more serious happened
            resourceId = R.string.failed_to_perform_action;
        } else {
            // well, who knows!
            resourceId = R.string.failed_to_perform_action;
        }
        Utils.showOopsDialog(context, resourceId);
    }

    /**
     * Gets the version of app.
     */
    public static String getAppVersionName(Context context) {
        String versionString = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    0 /* basic info */);
            versionString = info.versionName;
        } catch (Exception e) {
            // do nothing
        }
        return versionString;
    }

    /**
     * Shows a (long) toast.
     */
    public static void showToast(Context context, int resourceId) {
        Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_LONG).show();
    }

    public static boolean hitTest(View v, int x, int y) {
        final int tx = (int) (ViewCompat.getTranslationX(v) + 0.5f);
        final int ty = (int) (ViewCompat.getTranslationY(v) + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }

    /**
     * Show a popup to select whether the selected item should play immediately, be added to the
     * end of queue or be added to the queue right after the current item.
     */
    public static void showQueuePopup(final Context context, View view, final MediaInfo mediaInfo) {
        final VideoCastManager castManager = VideoCastManager.getInstance();
        final QueueDataProvider provider = QueueDataProvider.getInstance();
        if (!castManager.isConnected()) {
            Log.w(TAG, "showQueuePopup(): not connected to a cast device");
            return;
        }
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(
                provider.isQueueDetached() || provider.getCount() == 0
                        ? R.menu.detached_popup_add_to_queue
                        : R.menu.popup_add_to_queue, popup.getMenu());
        PopupMenu.OnMenuItemClickListener clickListener = new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return doProcessButtons(context, menuItem.getItemId(), mediaInfo);
            }
        };
        popup.setOnMenuItemClickListener(clickListener);
        popup.show();
    }


    public static boolean doProcessButtons(Context context, int i, final MediaInfo mediaInfo){
        final VideoCastManager castManager = VideoCastManager.getInstance();
        QueueDataProvider provider = QueueDataProvider.getInstance();
        MediaQueueItem queueItem = new MediaQueueItem.Builder(mediaInfo).setAutoplay(
                true).setPreloadTime(CastApplication.PRELOAD_TIME_S).build();
        MediaQueueItem[] newItemArray = new MediaQueueItem[]{queueItem};
        String toastMessage = null;
        try {
            if (provider.isQueueDetached() && provider.getCount() > 0) {
//                int i = menuItem.getItemId();
                if (i == R.id.action_play_now || i == R.id.action_add_to_queue) {
                    MediaQueueItem[] items = com.google.android.libraries.cast.companionlibrary.utils.Utils.rebuildQueueAndAppend(provider.getItems(), queueItem);
                    // temporary castManager.queueLoad(items, provider.getCount(),
                    // temporary        MediaStatus.REPEAT_MODE_REPEAT_OFF, null);
                    ((CastApplication) context.getApplicationContext()).loadQueue(items, provider.getCount());

                } else {
                    return false;
                }
            } else {
                if (provider.getCount() == 0) {
                    // temporary castManager.queueLoad(newItemArray, 0,
                    // temporary        MediaStatus.REPEAT_MODE_REPEAT_OFF, null);
                    ((CastApplication) context.getApplicationContext()).loadQueue(newItemArray, 0);
                } else {
                    int currentId = provider.getCurrentItemId();
//                    int i = menuItem.getItemId();
                    if (i == R.id.action_play_now) {
                        castManager.queueInsertBeforeCurrentAndPlay(queueItem, currentId, null);

                    } else if (i == R.id.action_play_next) {
                        int currentPosition = provider.getPositionByItemId(currentId);
                        if (currentPosition == provider.getCount() - 1) {
                            //we are adding to the end of queue
                            castManager.queueAppendItem(queueItem, null);
                        } else {
                            int nextItemId = provider.getItem(currentPosition + 1).getItemId();
                            castManager.queueInsertItems(newItemArray, nextItemId, null);
                        }
                        toastMessage = context.getString(
                                R.string.queue_item_added_to_play_next);

                    } else if (i == R.id.action_add_to_queue) {
                        castManager.queueAppendItem(queueItem, null);
                        toastMessage = context.getString(
                                R.string.queue_item_added_to_queue);

                    } else {
                        return false;
                    }
                }
            }
        } catch (NoConnectionException |
                TransientNetworkDisconnectionException e) {
            Log.e(TAG, "Failed to add item to queue or play remotely", e);
        }
        if (toastMessage != null) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
        }
        return true;
    }
    public static void showUpdateDialogIfNecessary(Activity activity) {
        try {
            SharedPreferences prefs = activity.getSharedPreferences("CHANGELOG", Context.MODE_PRIVATE);
            int currentVersionCode = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionCode;
            boolean hasShownPrevious = prefs.getBoolean("has-shown-prefs-" + (currentVersionCode - 1), false);
            boolean hasShownCurrent = prefs.getBoolean("has-shown-prefs-" + currentVersionCode, false);
            //Log.e(TAG, "hasShownPrevious:" + hasShownPrevious + " hasShownCurrent:" + hasShownCurrent);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("has-shown-prefs-" + currentVersionCode, true);
            editor.putBoolean("has-shown-prefs-" + (currentVersionCode - 1), true);
            if ((hasShownPrevious && !hasShownCurrent)) {
                AlertDialog d = new AlertDialog.Builder(activity)
                        .setTitle("Changelog: App Version " + currentVersionCode)
                        .setMessage(activity.getResources().getString(R.string.changelog_message))
                        .setCancelable(false)
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).show();
                //DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
                //int width = metrics.widthPixels;
                //int height = metrics.heightPixels;
                //Log.e(TAG, "Width:" + width + " Height:" + height);
                //d.getWindow().setLayout(width,height);
            }
            editor.apply();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static String getDefaultDirGetURL(Context context) {
        return context.getResources().getString(R.string.endpoint_dirget_default);
    }

    public static void setDirGetURL(Context context, String backend_url) {
        SharedPreferences prefs = context.getSharedPreferences(BACKEND_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(BACKEND_KEY, backend_url);
        editor.apply();
    }

    public static boolean getUseDefaultBackend(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BACKEND_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(USE_DEFAULT_BACKEND_KEY, true);
    }

    public static void setUseDefaultBackend(Context context, boolean useDefaultBackend) {
        SharedPreferences prefs = context.getSharedPreferences(BACKEND_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(USE_DEFAULT_BACKEND_KEY, useDefaultBackend);
        editor.apply();
    }

    public static void swapDefaultBackend(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BACKEND_NAME, Context.MODE_PRIVATE);
        boolean useDefaultBackend = prefs.getBoolean(USE_DEFAULT_BACKEND_KEY, true);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(USE_DEFAULT_BACKEND_KEY, !useDefaultBackend);
        editor.apply();
    }

    public static String getDirGetURL(Context context, boolean forceSecondary) {
        SharedPreferences prefs = context.getSharedPreferences(BACKEND_NAME, Context.MODE_PRIVATE);
        String backend_url = prefs.getString(BACKEND_KEY, null);
        if (backend_url == null) {
            //If the key is still the default (null), write the real default
            backend_url = getDefaultDirGetURL(context);
            //This is important if the default changes in the future, which it likely will
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(BACKEND_KEY, backend_url);
            editor.apply();
        }
        if (!forceSecondary) {
            if (getUseDefaultBackend(context)) {
                backend_url = getDefaultDirGetURL(context);
            }
        }
        return backend_url;
    }

    public static boolean backendSupportsFull(Context context) {
        return false;
        //This is all you need to do to put back the tab for add.
        //String endpoint = getDirGetURL(context, false);
        //return !DAD_TEST && endpoint.toLowerCase().contains("api.repkam09.com");
    }

    public static boolean backendSupportsLocalCast(Context context) {
        String endpoint = getDirGetURL(context, false);
        return !DAD_TEST && endpoint.toLowerCase().contains("api.repkam09.com");
    }
}

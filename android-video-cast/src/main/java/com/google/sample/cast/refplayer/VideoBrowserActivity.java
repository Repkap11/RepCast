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

package com.google.sample.cast.refplayer;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumer;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.widgets.MiniController;
import com.google.sample.cast.refplayer.queue.ui.QueueListViewActivity;
import com.google.sample.cast.refplayer.settings.CastPreference;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

public class VideoBrowserActivity extends AppCompatActivity {

    private static final String TAG = "VideoBrowserActivity";
    private VideoCastManager mCastManager;
    private VideoCastConsumer mCastConsumer;
    private MiniController mMini;
    private MenuItem mediaRouteMenuItem;
    private boolean mIsHoneyCombOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    private Toolbar mToolbar;

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VideoCastManager.checkGooglePlayServices(this);
        setContentView(R.layout.video_browser);

        mCastManager = VideoCastManager.getInstance();
        mCastConsumer = new VideoCastConsumerImpl() {

            @Override
            public void onFailed(int resourceId, int statusCode) {
                String reason = "Not Available";
                if (resourceId > 0) {
                    reason = getString(resourceId);
                }
                Log.e(TAG, "Action failed, reason:  " + reason + ", status code: " + statusCode);
            }

            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId,
                    boolean wasLaunched) {
                invalidateOptionsMenu();
            }

            @Override
            public void onDisconnected() {
                invalidateOptionsMenu();
            }

            @Override
            public void onConnectionSuspended(int cause) {
                Log.d(TAG, "onConnectionSuspended() was called with cause: " + cause);
                com.google.sample.cast.refplayer.utils.Utils.
                        showToast(VideoBrowserActivity.this, R.string.connection_temp_lost);
            }

            @Override
            public void onConnectivityRecovered() {
                com.google.sample.cast.refplayer.utils.Utils.
                        showToast(VideoBrowserActivity.this, R.string.connection_recovered);
            }

            @Override
            public void onCastDeviceDetected(final RouteInfo info) {
                if (!CastPreference.isFtuShown(VideoBrowserActivity.this) && mIsHoneyCombOrAbove) {
                    CastPreference.setFtuShown(VideoBrowserActivity.this);

                    Log.d(TAG, "Route is visible: " + info);
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mediaRouteMenuItem.isVisible()) {
                                Log.d(TAG, "Cast Icon is visible: " + info.getName());
                                showFtu();
                            }
                        }
                    }, 1000);
                }
            }
        };

        setupActionBar();
    }

    private void setupActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //mToolbar.setLogo(R.drawable.actionbar_logo_castvideos);
        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.browse, menu);

        mediaRouteMenuItem = mCastManager.
                addMediaRouterButton(menu, R.id.media_route_menu_item);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_show_queue).setVisible(mCastManager.isConnected());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        int i1 = item.getItemId();
        if (i1 == R.id.action_settings) {
            i = new Intent(VideoBrowserActivity.this, CastPreference.class);
            startActivity(i);

        } else if (i1 == R.id.action_show_queue) {
            i = new Intent(VideoBrowserActivity.this, QueueListViewActivity.class);
            startActivity(i);

        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showFtu() {
        Menu menu = mToolbar.getMenu();
        View view = menu.findItem(R.id.media_route_menu_item).getActionView();
        if (view != null && view instanceof MediaRouteButton) {
            new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(view))
                    .setContentTitle(R.string.touch_to_cast)
                    .build();
        }
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return mCastManager.onDispatchVolumeKeyEvent(event, CastApplication.VOLUME_INCREMENT)
                || super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
        mCastManager = VideoCastManager.getInstance();
        if (null != mCastManager) {
            mCastManager.addVideoCastConsumer(mCastConsumer);
            mCastManager.incrementUiCounter();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        mCastManager.decrementUiCounter();
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy is called");
        super.onDestroy();
    }

}

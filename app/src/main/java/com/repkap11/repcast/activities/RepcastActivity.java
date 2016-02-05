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

package com.repkap11.repcast.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumer;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.widgets.MiniController;
import com.repkap11.repcast.R;
import com.repkap11.repcast.UpdateAppTask;
import com.repkap11.repcast.activities.fragments.RepcastFragment;
import com.repkap11.repcast.application.CastApplication;
import com.repkap11.repcast.queue.ui.QueueListViewActivity;
import com.repkap11.repcast.utils.Utils;

public abstract class RepcastActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, SearchView.OnQueryTextListener {

    private static final String TAG = RepcastActivity.class.getSimpleName();
    private static final java.lang.String INSTANCE_STATE_INITIAL_STRING = "INSTANCE_STATE_INITIAL_STRING";
    private VideoCastManager mCastManager;
    private VideoCastConsumer mCastConsumer;
    private MiniController mMini;
    private MenuItem mediaRouteMenuItem;
    private boolean mIsHoneyCombOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    private Toolbar mToolbar;
    private SearchView mSearchView;
    private String mInitialSearchString = null;

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "Activity Created");
        super.onCreate(savedInstanceState);
        VideoCastManager.checkGooglePlayServices(this);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
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
                Utils.showToast(RepcastActivity.this, R.string.connection_temp_lost);
            }

            @Override
            public void onConnectivityRecovered() {
                Utils.showToast(RepcastActivity.this, R.string.connection_recovered);
            }

            @Override
            public void onCastDeviceDetected(final RouteInfo info) {
                if (!CastPreferenceActivity.isFtuShown(RepcastActivity.this) && mIsHoneyCombOrAbove) {
                    CastPreferenceActivity.setFtuShown(RepcastActivity.this);

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
    }

    protected void completeOnCreate(Bundle savedInstanceState){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitleBasedOnFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.browse, menu);
        mediaRouteMenuItem = mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        MenuItem updateApplicationMenuItem = menu.findItem(R.id.update_app_menu_button);
        updateApplicationMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                UpdateAppTask task = new UpdateAppTask(getApplicationContext(), true);
                task.execute();
                return true;
            }
        });
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            mSearchView = (SearchView) searchItem.getActionView();
            mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            mSearchView.setQuery(mInitialSearchString, false);
            mSearchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            if (!TextUtils.isEmpty(mInitialSearchString)) {
                mSearchView.setIconified(false);
                mSearchView.clearFocus();
            }
            mSearchView.setOnQueryTextListener(this);
        }
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
            i = new Intent(RepcastActivity.this, CastPreferenceActivity.class);
            startActivity(i);

        } else if (i1 == R.id.action_show_queue) {
            i = new Intent(RepcastActivity.this, QueueListViewActivity.class);
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
    public void showContent(Parcelable data){
        if (mSearchView != null) {
            mSearchView.setQuery(null, false);
            mSearchView.setIconified(true);
        }
        doShowContent(data);
    }
    protected abstract void doShowContent(Parcelable data);

    @Override
    public void onBackPressed() {
        Log.e(TAG, "Back stack count:" + getSupportFragmentManager().getBackStackEntryCount());
        if (!TextUtils.isEmpty(mSearchView.getQuery()) || !mSearchView.isIconified()) {
            mSearchView.setQuery(null, false);
            mSearchView.setIconified(true);
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            //This manages the back stack by itself.
            super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged() {
        setTitleBasedOnFragment();
    }

    public void setTitleBasedOnFragment() {
        RepcastFragment fragment = (RepcastFragment) getSupportFragmentManager().findFragmentById(R.id.activity_fragment_holder);
        if (fragment != null) {
            String name = fragment.getName();
            getSupportActionBar().setTitle(name);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.e(TAG, "Got onQueryTextSubmit");
        RepcastFragment fragment = (RepcastFragment) getSupportFragmentManager().findFragmentById(R.id.activity_fragment_holder);
        mSearchView.clearFocus();
        boolean result = fragment.getView().requestFocus();
        Log.e(TAG, "Took Focus:" + result);

        //This is the only way to stop the cursor in the TextView.
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mSearchView.clearFocus();
            }
        });
        if (fragment != null) {
            return fragment.onQuerySubmit(query);
        }


        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        View closeButton = mSearchView.findViewById(R.id.search_close_btn);
        if (closeButton != null) {
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSearchView.setQuery(null, false);
                    mSearchView.setIconified(true);
                    mSearchView.clearFocus();
                }
            });
        }
        RepcastFragment fragment = (RepcastFragment) getSupportFragmentManager().findFragmentById(R.id.activity_fragment_holder);
        if (fragment != null) {
            return fragment.onQueryChange(newText);
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INSTANCE_STATE_INITIAL_STRING, mSearchView.getQuery().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mInitialSearchString = savedInstanceState.getString(INSTANCE_STATE_INITIAL_STRING);
    }
}

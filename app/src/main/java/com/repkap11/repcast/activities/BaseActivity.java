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

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.FragmentManager;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.mediarouter.media.MediaRouter;

import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumer;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.widgets.MiniController;
import com.repkap11.repcast.R;
import com.repkap11.repcast.UpdateAppTask;
import com.repkap11.repcast.application.CastApplication;
import com.repkap11.repcast.queue.ui.QueueListViewActivity;
import com.repkap11.repcast.utils.ConfigureBackendDialogFragment;
import com.repkap11.repcast.utils.Utils;

public abstract class BaseActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, SearchView.OnQueryTextListener {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final String INSTANCE_STATE_INITIAL_STRING = "INSTANCE_STATE_INITIAL_STRING";
    private static final String INSTANCE_STATE_SEARCH_EXPANDED = "INSTANCE_STATE_SEARCH_EXPANDED";
    private VideoCastManager mCastManager;
    private VideoCastConsumer mCastConsumer;
    private MiniController mMini;
    private MenuItem mediaRouteMenuItem;
    private boolean mIsHoneyCombOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    protected Toolbar mToolbar;
    private SearchView mSearchView;
    private String mInitialSearchString = null;
    private boolean mIncludeSearch;
    private MenuItem mediaQueueItem;
    private boolean mIsSearchExpanded = false;
    private MenuItem mSearchItem;
    private boolean mSkipTextChange = false;
    private static final int REQUEST_CODE_ASK_FOR_WRITE_EXPERNAL_PERMISSION = 44;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setNavigationBarColor(Color.BLACK);
        }
    }
    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.e(TAG, "Activity Created");
        super.onCreate(savedInstanceState);
        VideoCastManager.checkGooglePlayServices(this);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        mCastManager = VideoCastManager.getInstance();
        if (savedInstanceState != null) {
            mIsSearchExpanded = savedInstanceState.getBoolean(INSTANCE_STATE_SEARCH_EXPANDED);
            mInitialSearchString = savedInstanceState.getString(INSTANCE_STATE_INITIAL_STRING);
        }
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
                Utils.showToast(BaseActivity.this, R.string.connection_temp_lost);
            }

            @Override
            public void onConnectivityRecovered() {
                Utils.showToast(BaseActivity.this, R.string.connection_recovered);
            }

            @Override
            public void onCastDeviceDetected(final MediaRouter.RouteInfo info) {
                if (!CastPreferenceActivity.isFtuShown(BaseActivity.this) && mIsHoneyCombOrAbove) {
                    CastPreferenceActivity.setFtuShown(BaseActivity.this);

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

    protected void completeOnCreate(Bundle savedInstanceState, boolean includeSearch) {
        mIncludeSearch = includeSearch;
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        //
         setTitleBasedOnFragment();
    }


    private void startUpdateAppProcedure() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_FOR_WRITE_EXPERNAL_PERMISSION);
                return;
            }
        }
        continueUpdateAppWithPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Log.e(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
            continueUpdateAppWithPermissions();
        } else {
            //Log.e(TAG, "Permissions not granted");
        }
    }

    private void continueUpdateAppWithPermissions() {
        new UpdateAppTask(getApplicationContext(), true).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.browse, menu);
        mediaRouteMenuItem = mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        mediaQueueItem = menu.findItem(R.id.action_show_queue);
        MenuItem updateApplicationMenuItem = menu.findItem(R.id.update_app_menu_button);
        updateApplicationMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //UpdateAppTask task = new UpdateAppTask(getApplicationContext(), true);
                //task.execute();
                startUpdateAppProcedure();
                return true;
            }
        });
        MenuItem configureBackendMenuItem = menu.findItem(R.id.configure_backend);
        configureBackendMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                openBackendDialog();
                return true;
            }
        });
        MenuItem swapBackendMenu = menu.findItem(R.id.swap_backend);
        String string_resource = getResources().getString(Utils.getUseDefaultBackend(this) ? R.string.swap_backend_secondary : R.string.swap_backend_default);
        swapBackendMenu.setTitle(getResources().getString(R.string.swap_backend, string_resource));
        swapBackendMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                swapBackend();
                return true;
            }
        });
        mSearchItem = menu.findItem(R.id.action_search);
        if (mSearchItem != null) {
            mSearchItem.setVisible(mIncludeSearch);
            MenuItemCompat.setOnActionExpandListener(mSearchItem, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    //Log.d(TAG, "onMenuItemActionExpand() called with: " + "item = [" + item + "]");
                    mIsSearchExpanded = true;
                    if (!mSearchView.isIconified()) {
                        //return false;
                    }
                    //Log.e(TAG, "Setting InitialValue:" + mInitialSearchString);
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mSearchView.setQuery(mInitialSearchString, false);
                        }
                    });
                    mediaRouteMenuItem.setVisible(false);
                    mediaQueueItem.setVisible(false);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    //Log.d(TAG, "onMenuItemActionCollapse() called with: " + "item = [" + item + "]");
                    mIsSearchExpanded = false;
                    mediaRouteMenuItem.setVisible(mCastManager.isAnyRouteAvailable());
                    mediaQueueItem.setVisible(mCastManager.isConnected());
                    return true;
                }
            });

            mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
            mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            if (mIsSearchExpanded) {
                //Log.e(TAG, "Expanding Search 1");
                MenuItemCompat.expandActionView(mSearchItem);
                mSearchView.setQuery(mInitialSearchString, false);
            }

            mSearchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

            mSearchView.setOnQueryTextListener(this);
        }
        return true;
    }

    private void swapBackend() {
        Utils.swapDefaultBackend(this);
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void openBackendDialog() {
        new ConfigureBackendDialogFragment().show(getFragmentManager(), "BACKEND");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_show_queue).setVisible(mCastManager.isConnected());
        if (mIsSearchExpanded) {
            //Log.e(TAG, "Expanding Search 2");
            //mSearchView.setIconified(false);
            //mSearchView.clearFocus();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        int i1 = item.getItemId();
        if (i1 == R.id.action_show_queue) {
            i = new Intent(BaseActivity.this, QueueListViewActivity.class);
            startActivity(i);

        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showFtu() {
        Menu menu = mToolbar.getMenu();
        View view = menu.findItem(R.id.media_route_menu_item).getActionView();
        if (view instanceof MediaRouteButton) {
            new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(view))
                    .setContentTitle(R.string.touch_to_cast)
                    .build();
        }
    }

    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return mCastManager.onDispatchVolumeKeyEvent(event, CastApplication.VOLUME_INCREMENT)
                || super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCastManager = VideoCastManager.getInstance();
        if (null != mCastManager) {
            mCastManager.addVideoCastConsumer(mCastConsumer);
            mCastManager.incrementUiCounter();
        }
    }

    @Override
    protected void onStop() {
        mCastManager.decrementUiCounter();
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy is called");
        super.onDestroy();
    }

    public void showContent(Parcelable data, int currentPosition) {
//        Log.e(TAG, "Paul showContent: index:"+currentPosition);
        if (mSearchView != null) {
            mInitialSearchString = "";
            mSearchView.setQuery(null, false);
            mSearchItem.collapseActionView();
        }
        doShowContent(data, currentPosition);
    }

    protected abstract void doShowContent(Parcelable data, int currentPosition);

    @Override
    public void onBackPressed() {
        //Log.e(TAG, "Back stack count:" + getSupportFragmentManager().getBackStackEntryCount());
        if (!TextUtils.isEmpty(mSearchView.getQuery()) || !mSearchView.isIconified()) {
            mInitialSearchString = "";
            mSearchView.setQuery(null, false);
            mSearchView.setIconified(true);
            return;
        }
        if (!handleOnBackPressed()) {
            finish();
        }

    }

    protected abstract boolean handleOnBackPressed();

    @Override
    public void onBackStackChanged() {
        setTitleBasedOnFragment();
    }

    public abstract void setTitleBasedOnFragment();


    @Override
    public boolean onQueryTextSubmit(String query) {
        mSearchView.clearFocus();
        mSkipTextChange = true;
        MenuItemCompat.collapseActionView(mSearchItem);
        mSkipTextChange = false;
        return onQuerySubmit(query);
    }

    protected abstract boolean onQuerySubmit(String query);

    @Override
    public boolean onQueryTextChange(String newText) {
        //Log.e(TAG, "Got onQueryTextChange");
        View closeButton = mSearchView.findViewById(R.id.search_close_btn);
        if (!mSkipTextChange) {
            onQueryChanged(newText);
            mInitialSearchString = newText;
        }
        return false;
    }

    protected abstract void onQueryChanged(String newText);

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSearchView != null) {
            outState.putString(INSTANCE_STATE_INITIAL_STRING, mSearchView.getQuery().toString());
            outState.putBoolean(INSTANCE_STATE_SEARCH_EXPANDED, mIsSearchExpanded);
        }
    }
}

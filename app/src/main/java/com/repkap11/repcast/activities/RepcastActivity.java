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
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.repkap11.repcast.R;
import com.repkap11.repcast.fragments.RepcastFragment;
import com.repkap11.repcast.fragments.SelectFileFragment;
import com.repkap11.repcast.fragments.SelectTorrentFragment;
import com.repkap11.repcast.model.adapters.RepcastPageAdapter;
import com.repkap11.repcast.model.parcelables.JsonDirectory;
import com.repkap11.repcast.model.parcelables.JsonTorrent;
import com.repkap11.repcast.model.rest.RepcastSyncChecker;
import com.repkap11.repcast.utils.DownloadDialogFragment;
import com.repkap11.repcast.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

;

public class RepcastActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    private static final String TAG = RepcastActivity.class.getSimpleName();
    private static final String INSTANCE_STATE_BACK_STACK_TORRENTS = "INSTANCE_STATE_BACK_STACK_TORRENTS";
    private static final String INSTANCE_STATE_BACK_STACK_FILES = "INSTANCE_STATE_BACK_STACK_FILES";
    private ViewPager mViewPager;
    private RepcastPageAdapter mPagerAdapter;
    private JsonDirectory.JsonFileDir mPendingPermissionDownload = null;
    Stack<Parcelable> mBackSelectFileFragments = new Stack<>();
    Stack<Parcelable> mBackTorrentFragments = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.showUpdateDialogIfNecessary(this);
        setContentView(R.layout.activity_repcast);
        if (savedInstanceState == null) {
            Log.i(TAG, "onCreate: New instance");
            mPagerAdapter = new RepcastPageAdapter(getSupportFragmentManager(), this, getApplicationContext());
        } else {
            Log.i(TAG, "onCreate: Restoring instance");
            mBackSelectFileFragments.addAll(Arrays.asList(savedInstanceState.getParcelableArray(INSTANCE_STATE_BACK_STACK_FILES)));
            mBackTorrentFragments.addAll(Arrays.asList(savedInstanceState.getParcelableArray(INSTANCE_STATE_BACK_STACK_TORRENTS)));
            mPagerAdapter = new RepcastPageAdapter(getSupportFragmentManager(), getApplicationContext(), (JsonDirectory.JsonFileDir) ((BackStackData)mBackSelectFileFragments.peek()).data, (JsonTorrent.JsonTorrentResult) ((BackStackData)mBackTorrentFragments.peek()).data);
        }
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(RepcastPageAdapter.FILE_INDEX);
        if (!Utils.backendSupportsFull(getApplicationContext())) {
            tabLayout.setVisibility(View.GONE);
        }
        completeOnCreate(savedInstanceState, true);

    }

    public static class BackStackData implements Parcelable {
        public static final Parcelable.Creator<BackStackData> CREATOR = new Parcelable.Creator<BackStackData>() {
            public BackStackData createFromParcel(Parcel in) {
                return new BackStackData(in);
            }

            @Override
            public BackStackData[] newArray(int size) {
                return new BackStackData[size];
            }
        };
        public int scrollPosition;
        public Parcelable data;

        public BackStackData() {
        }

        public BackStackData(Parcel in) {
            if (in != null) {
                scrollPosition = in.readInt();
                data = in.readParcelable(Parcel.class.getClassLoader());
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(scrollPosition);
            dest.writeParcelable(data, flags);
        }
    }

    @Override
    protected void doShowContent(Parcelable data, int curentScrollPosition) {
        int pageIndex = -1;
        if (data instanceof JsonDirectory.JsonFileDir) {
//            Log.e(TAG, "Showing File data of:" + ((JsonDirectory.JsonFileDir) data).name);
            pageIndex = RepcastPageAdapter.FILE_INDEX;
        }
        if (data instanceof JsonTorrent.JsonTorrentResult) {
            pageIndex = RepcastPageAdapter.TORRENT_INDEX;
        }
        if (pageIndex == -1) {
            Log.e(TAG, "Unexpected data type. Class:" + data.getClass() + " String:" + data);
        }
        BackStackData d = new BackStackData();
        d.data = data;
        d.scrollPosition = 0;
        mPagerAdapter.updatePageAtIndex(pageIndex, d);
        addFragmentToABackStack(d, curentScrollPosition);
        setTitleBasedOnFragment();
    }

    public void addFragmentToABackStack(BackStackData newFragmentData, int curentScrollPosition) {
        if (newFragmentData.data instanceof JsonDirectory.JsonFileDir) {
            if (!mBackSelectFileFragments.empty()) {
                ((BackStackData) mBackSelectFileFragments.peek()).scrollPosition = curentScrollPosition;
            }
            mBackSelectFileFragments.add(newFragmentData);
        } else if (newFragmentData.data instanceof JsonTorrent.JsonTorrentResult) {
            if (!mBackTorrentFragments.empty()) {
                ((BackStackData) mBackTorrentFragments.peek()).scrollPosition = curentScrollPosition;
            }
            mBackTorrentFragments.add(newFragmentData);
        }
    }

    public BackStackData removeFragmentFromABackStack(Class<? extends RepcastFragment> targetClass) {
        BackStackData oldFragmentData = null;
        if (targetClass.equals(SelectFileFragment.class)) {
            if (mBackSelectFileFragments.empty()) {
                return null;
            }
            mBackSelectFileFragments.pop();
            if (mBackSelectFileFragments.empty()) {
                return null;
            }
            oldFragmentData = (BackStackData) mBackSelectFileFragments.peek();
        } else if (targetClass.equals(SelectTorrentFragment.class)) {
            if (mBackTorrentFragments.empty()) {
                return null;
            }
            mBackTorrentFragments.pop();
            if (mBackTorrentFragments.empty()) {
                return null;
            }
            oldFragmentData =  (BackStackData)mBackTorrentFragments.peek();
        }
        return oldFragmentData;

    }

    @Override
    protected boolean handleOnBackPressed() {
        Log.e(TAG, "Handleing back press");
        //Return to the FILE_INDEX fragment
        int currentFragmentIndex = mViewPager.getCurrentItem();
        if (currentFragmentIndex == RepcastPageAdapter.TORRENT_INDEX) {
            mViewPager.setCurrentItem(RepcastPageAdapter.FILE_INDEX);
            return true;
        }
        //Clear the selectFileFragment search term
        RepcastFragment selectFileFragment = mPagerAdapter.getRegisteredFragment(RepcastPageAdapter.FILE_INDEX);
        if (!TextUtils.isEmpty(selectFileFragment.getResultEmptyString())) {
            selectFileFragment.onQueryChange(null);
            return true;
        }
        RepcastFragment currentFragment = mPagerAdapter.getRegisteredFragment(currentFragmentIndex);
        BackStackData previousFragmentData = removeFragmentFromABackStack(currentFragment.getClass());
        if (previousFragmentData == null) {
            return false;
        }
        mPagerAdapter.updatePageAtIndex(mViewPager.getCurrentItem(), previousFragmentData);
        setTitleBasedOnFragment();
        return true;
    }


    @Override
    public void setTitleBasedOnFragment() {
        RepcastFragment frag = mPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
        String name;
        if (frag == null) {
            name = ((JsonDirectory.JsonFileDir)((BackStackData)mBackSelectFileFragments.peek()).data).name;
        } else {
            name = frag.getName();
        }
//        Log.e(TAG, "setTitleBasedOnFragment: Did set to:"+ name);
        getSupportActionBar().setTitle(name);


    }

    @Override
    protected boolean onQuerySubmit(String query) {
        //Propagate query submit only to the current fragment.
        RepcastFragment fragment = mPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
        fragment.onQuerySubmit(query);
        return true;
    }

    @Override
    protected void onQueryChanged(String query) {
        //Propagate query changed to all fragments.
        RepcastFragment fragment = mPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
        fragment.onQueryChange(query);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setTitleBasedOnFragment();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void uploadMagnet(JsonTorrent.JsonTorrentResult element) {
        Log.e(TAG, "Should start download of torrent " + element.name);


        Intent intent = new Intent();
        intent.setClass(this, MagnetConfirmationActivity.class);
        intent.setData(Uri.parse(element.magnetLink));
        intent.putExtra(MagnetConfirmationActivity.EXTRA_MAGNET_RESULT, element);
        Log.e(TAG, "About to download:" + element.name);
        startActivity(intent);

    }

    public void showFile(JsonDirectory.JsonFileDir dir, boolean forceShare) {
        Log.e(TAG, "Starting file:" + dir.name);
        RepcastSyncChecker syncChecker = new RepcastSyncChecker(this, dir, forceShare);
        syncChecker.execute();
    }

    public void openDownloadDialog(JsonDirectory.JsonFileDir dir) {
        Log.e(TAG, "Opening Dialog for file:" + dir.name);
        DownloadDialogFragment dialog = new DownloadDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(DownloadDialogFragment.ARG_DIR, dir);
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "DOWNLOAD");


    }

    public void showFileWithURL(JsonDirectory.JsonFileDir dir, String url, boolean forceShare, boolean isVideo, boolean isAudio, float aspectRatio) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        Log.e(TAG, "Uri:" + uri);
        Log.e(TAG, "MimeType:" + dir.mimetype);
        intent.setDataAndType(uri, dir.mimetype);
        intent.putExtra(Intent.EXTRA_TITLE, dir.name);
        intent.putExtra(Intent.EXTRA_REFERRER_NAME, "Paul");

        if ((!forceShare) && (isVideo || isAudio)) {
            intent.putExtra(LocalPlayerActivity.EXTRA_ASPECT_RATIO, aspectRatio);
            intent.setClass(this, LocalPlayerActivity.class);
            startActivity(intent);
        }else {
            ComponentName cn = intent.resolveActivity(getPackageManager());
            if (cn != null) {
                if (forceShare) {
                    startActivity(Intent.createChooser(intent, "Select an App"));
                } else {
                    startActivity(intent);
                }
            } else {
                Toast.makeText(this, "No apps can open this file type.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArray(INSTANCE_STATE_BACK_STACK_FILES, mBackSelectFileFragments.toArray(new BackStackData[0]));
        outState.putParcelableArray(INSTANCE_STATE_BACK_STACK_TORRENTS, mBackTorrentFragments.toArray(new BackStackData[0]));
        super.onSaveInstanceState(outState);
    }


    public boolean haveStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error", "You have permission");
                return true;
            } else {

                Log.e("Permission error", "You have asked for permission");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //you dont need to worry about these stuff below api level 23
            Log.e("Permission error", "You already have the permission");
            return true;
        }
    }


    public void downloadFile(JsonDirectory.JsonFileDir dir) {

        if (!haveStoragePermission()) {
            mPendingPermissionDownload = dir;
            return;
        }


        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(dir.path));

        // only download via WIFI
        request.setAllowedOverMetered(false);
        request.setDescription(getResources().getString(R.string.app_name));
        request.setTitle(dir.name);

        // we just want to download silently
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.allowScanningByMediaScanner();
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, dir.name);

        // enqueue this request
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadID = downloadManager.enqueue(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mPendingPermissionDownload != null)
                downloadFile(mPendingPermissionDownload);
            mPendingPermissionDownload = null;
        }
    }
}
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

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.fragments.RepcastFragment;
import com.repkap11.repcast.activities.fragments.SelectFileFragment;
import com.repkap11.repcast.activities.fragments.SelectTorrentFragment;
import com.repkap11.repcast.model.JsonDirectory;
import com.repkap11.repcast.model.JsonTorrent;
import com.repkap11.repcast.model.JsonTorrentUploader;
import com.repkap11.repcast.model.RepcastPageAdapter;

import java.util.Stack;

public class RepcastActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    private static final String TAG = RepcastActivity.class.getSimpleName();
    private ViewPager mViewPager;
    private RepcastPageAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repcast);
        mPagerAdapter = new RepcastPageAdapter(getSupportFragmentManager(), this, getApplicationContext());
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(RepcastPageAdapter.FILE_INDEX);
        mViewPager.addOnPageChangeListener(this);


        completeOnCreate(savedInstanceState);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void doShowContent(Parcelable data) {
        int pageIndex = -1;
        if (data instanceof JsonDirectory.JsonFileDir) {
            Log.e(TAG, "Showing File data of:" + ((JsonDirectory.JsonFileDir) data).name);
            pageIndex = RepcastPageAdapter.FILE_INDEX;
        }
        if (data instanceof JsonTorrent.JsonTorrentResult) {
            pageIndex = RepcastPageAdapter.TORRENT_INDEX;
        }
        if (pageIndex == -1) {
            Log.e(TAG, "Unexpected data type. Class:" + data.getClass() + " String:" + data);
        }
        mPagerAdapter.updatePageAtIndex(pageIndex, data);
        addFragmentToABackStack(data);
        setTitleBasedOnFragment();
    }

    Stack<Parcelable> mBackSelectFileFragments = new Stack<>();
    Stack<Parcelable> mBackTorrentFragments = new Stack<>();

    public void addFragmentToABackStack(Parcelable newFragmentData) {
        if (newFragmentData instanceof JsonDirectory.JsonFileDir) {
            mBackSelectFileFragments.add(newFragmentData);
        } else if (newFragmentData instanceof JsonTorrent.JsonTorrentResult) {
            mBackTorrentFragments.add(newFragmentData);
        }
    }

    public Parcelable removeFragmentFromABackStack(Class<? extends RepcastFragment> targetClass) {
        Parcelable oldFragmentData = null;
        if (targetClass.equals(SelectFileFragment.class)) {
            if (mBackSelectFileFragments.empty()) {
                return null;
            }
            mBackSelectFileFragments.pop();
            if (mBackSelectFileFragments.empty()) {
                return null;
            }
            oldFragmentData = mBackSelectFileFragments.peek();
        } else if (targetClass.equals(SelectTorrentFragment.class)) {
            if (mBackTorrentFragments.empty()) {
                return null;
            }
            mBackTorrentFragments.pop();
            if (mBackTorrentFragments.empty()) {
                return null;
            }
            oldFragmentData = mBackTorrentFragments.peek();
        }
        return oldFragmentData;

    }

    @Override
    protected boolean handleOnBackPressed() {
        Log.e(TAG, "Handleing back press");
        RepcastFragment currentFragment = mPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
        Parcelable previousFragmentData = removeFragmentFromABackStack(currentFragment.getClass());
        if (previousFragmentData == null) {
            return false;
        }
        mPagerAdapter.updateFragment(previousFragmentData, mViewPager.getCurrentItem());
        setTitleBasedOnFragment();
        return true;
    }


    @Override
    public void setTitleBasedOnFragment() {
        RepcastFragment frag = mPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
        if (frag != null) {
            String name = frag.getName();
            getSupportActionBar().setTitle(name);
        }

    }

    @Override
    protected boolean onQuerySubmit(String query) {
        //Propagate query submit only to the current fragment.
        for (int i = 0; i < mPagerAdapter.getCount(); i++){
            RepcastFragment fragment = mPagerAdapter.getRegisteredFragment(i);
            fragment.onQuerySubmit(query);
        }
        return true;
    }

    @Override
    protected void onQueryChanged(String query) {
        //Propagate query changed to all fragments.
        for (int i = 0; i < mPagerAdapter.getCount(); i++){
            RepcastFragment fragment = mPagerAdapter.getRegisteredFragment(i);
            fragment.onQueryChange(query);
        }
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

    public void uploadTorrent(JsonTorrent.JsonTorrentResult element) {
        Log.e(TAG, "Should start download of torrent " + element.name);
        String magnetLink64 = Base64.encodeToString(element.magnetLink.getBytes(), Base64.NO_WRAP);

        String url = "https://repkam09.com/dl/toradd/" + magnetLink64;
        JsonTorrentUploader uploader = new JsonTorrentUploader(this);
        uploader.execute(url);
    }

    public void torrentUploadComplete(Integer resultCode) {
        Toast.makeText(this, "Result:" + resultCode, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Got Result:" + resultCode);
    }

    public void showFile(JsonDirectory.JsonFileDir dir) {
        Log.e(TAG, "Starting file:" + dir.name);
        Intent intent = new Intent();
        intent.setClass(this, LocalPlayerActivity.class);
        intent.putExtra("media", dir);
        intent.putExtra("shouldStart", false);
        Log.e(TAG, "About to cast:" + dir.path);
        startActivity(intent);
    }
}

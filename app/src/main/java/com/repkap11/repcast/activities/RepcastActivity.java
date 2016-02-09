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

import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.fragments.RepcastFragment;

public class RepcastActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    private static final String TAG = RepcastActivity.class.getSimpleName();
    private ViewPager mViewPager;
    private RepcastPageAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repcast);
        mPagerAdapter = new RepcastPageAdapter(getSupportFragmentManager(), getApplicationContext());
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(1);
        mViewPager.addOnPageChangeListener(this);
        completeOnCreate(savedInstanceState);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void doShowContent(Parcelable data) {
        RepcastFragment currentFragment = (RepcastFragment) mPagerAdapter.getItem(mViewPager.getCurrentItem());
        currentFragment.doShowContent(getSupportFragmentManager(), data);
    }

    @Override
    public void setTitleBasedOnFragment() {
        RepcastFragment currentFragment = (RepcastFragment) mPagerAdapter.getItem(mViewPager.getCurrentItem());
        String name = currentFragment.getName();
        getSupportActionBar().setTitle(name);
    }

    @Override
    protected boolean onQuerySubmit(String query) {
        //Propagate query submit only to the current fragment.
        for (int i = 0; i < mPagerAdapter.getCount(); i++){
            RepcastFragment fragment = (RepcastFragment)mPagerAdapter.getItem(i);
            fragment.onQuerySubmit(query);
        }
        return true;
    }

    @Override
    protected void onQueryChanged(String query) {
        //Propagate query changed to all fragments.
        for (int i = 0; i < mPagerAdapter.getCount(); i++){
            RepcastFragment fragment = (RepcastFragment)mPagerAdapter.getItem(i);
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
}

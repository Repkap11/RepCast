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
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.fragments.SelectFileFragment;
import com.repkap11.repcast.model.JsonDirectory;

public class SelectFileActivity extends RepcastActivity {

    private static final String TAG = SelectFileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectfile);
        completeOnCreate(savedInstanceState);
    }
    protected void completeOnCreate(Bundle savedInstanceState){
        super.completeOnCreate(savedInstanceState);
        SelectFileFragment frag = (SelectFileFragment) getSupportFragmentManager().findFragmentById(R.id.activity_fragment_holder);
        if (frag == null) {
            Log.e(TAG, "Adapter null");
            JsonDirectory.JsonFileDir dir = new JsonDirectory.JsonFileDir();
            dir.type = JsonDirectory.JsonFileDir.TYPE_DIR;
            dir.name = "Seedbox";
            dir.path = "IDGAF";
            dir.path64 = "";
            dir.isRoot = true;
            doShowContent(dir);
        }
    }

    @Override
    protected void doShowContent(Parcelable data) {
        SelectFileFragment newFragment = new SelectFileFragment();
        JsonDirectory.JsonFileDir dir = (JsonDirectory.JsonFileDir) data;
        newFragment.showListUsingDirectory(dir);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!dir.isRoot) {
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        }
        transaction.replace(R.id.activity_fragment_holder, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
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

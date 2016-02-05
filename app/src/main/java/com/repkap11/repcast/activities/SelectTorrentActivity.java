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
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.fragments.SelectTorrentFragment;
import com.repkap11.repcast.model.JsonTorrent;

public class SelectTorrentActivity extends RepcastActivity {

    private static final String TAG = SelectTorrentActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecttorrent);
        completeOnCreate(savedInstanceState);
    }

    protected void completeOnCreate(Bundle savedInstanceState) {
        super.completeOnCreate(savedInstanceState);
        VideoCastManager.checkGooglePlayServices(this);
        SelectTorrentFragment frag = (SelectTorrentFragment) getSupportFragmentManager().findFragmentById(R.id.activity_select_torrent_fragment_holder);
        if (frag == null) {
            Log.e(TAG, "Adapter null");
            JsonTorrent.JsonTorrentResult result = new JsonTorrent.JsonTorrentResult();
            result.name = "Runescape";
            doShowContent(result);
        }
    }

    @Override
    protected void doShowContent(Parcelable data) {
        SelectTorrentFragment newFragment = new SelectTorrentFragment();
        JsonTorrent.JsonTorrentResult torrent = (JsonTorrent.JsonTorrentResult) data;
        newFragment.searchForTorrentsWithName(torrent.name);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_select_torrent_fragment_holder, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void downloadTorrent(JsonTorrent.JsonTorrentResult element) {
        Log.e(TAG, "Should start download of torrent " + element.name);

    }
}

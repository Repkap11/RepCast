/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
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

package com.repkap11.repcast.cast.refplayer.queue.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.repkap11.repcast.R;
import com.repkap11.repcast.cast.refplayer.queue.QueueDataProvider;
import com.repkap11.repcast.cast.refplayer.settings.CastPreference;

import java.util.List;

/**
 * An activity to show the queue list
 */
public class QueueListViewActivity extends AppCompatActivity {

    private static final String FRAGMENT_LIST_VIEW = "list view";
    private static final String TAG = "QueueListViewActivity";
    private VideoCastConsumerImpl mCastConsumer;
    private VideoCastManager mCastManager;
    private View mEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.queue_activity);
        Log.d(TAG, "onCreate() was called");

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new QueueListViewFragment(), FRAGMENT_LIST_VIEW)
                    .commit();
        }
        setupActionBar();
        mCastManager = VideoCastManager.getInstance();
        mCastConsumer = new VideoCastConsumerImpl() {
            @Override
            public void onMediaQueueUpdated(List<MediaQueueItem> queueItems, MediaQueueItem item,
                    int repeatMode, boolean shuffle) {
                if (queueItems == null || queueItems.isEmpty()) {
                    mEmpty.setVisibility(View.VISIBLE);
                } else {
                    mEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onDisconnected() {
                mEmpty.setVisibility(View.VISIBLE);
            }

        };
        mCastManager.addVideoCastConsumer(mCastConsumer);
        mEmpty = findViewById(R.id.empty);
    }


    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.queue_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPause() {
        mCastManager.decrementUiCounter();
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.queue_menu, menu);

        mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        int i1 = item.getItemId();
        if (i1 == R.id.action_settings) {
            i = new Intent(QueueListViewActivity.this, CastPreference.class);
            startActivity(i);

        } else if (i1 == R.id.action_clear_queue) {
            QueueDataProvider.getInstance().removeAll();

        } else if (i1 == android.R.id.home) {
            finish();

        }
        return true;
    }

    @Override
    protected void onResume() {
        mCastManager = VideoCastManager.getInstance();
        if (null != mCastManager) {
            mCastManager.addVideoCastConsumer(mCastConsumer);
            mCastManager.incrementUiCounter();
            if (mCastManager.getMediaQueue() != null && !mCastManager.getMediaQueue().isEmpty()) {
                mEmpty.setVisibility(View.GONE);
            }
        }
        super.onResume();
    }
}

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

package com.repkap11.repcast.queue.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.CastException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.google.android.libraries.cast.companionlibrary.utils.Utils;
import com.repkap11.repcast.R;
import com.repkap11.repcast.application.CastApplication;
import com.repkap11.repcast.queue.QueueDataProvider;

/**
 * A fragment to show the list of queue items.
 */
public class QueueListViewFragment extends Fragment  implements
        QueueListAdapter.OnStartDragListener {

    private static final String TAG = "QueueListViewFragment";
    private RecyclerView mRecyclerView;
    private VideoCastManager mCastManager;
    private QueueDataProvider mProvider;
    private ItemTouchHelper mItemTouchHelper;

    public QueueListViewFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recycler_list_view, container, false);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        mProvider = QueueDataProvider.getInstance();

        QueueListAdapter adapter = new QueueListAdapter(getActivity(), this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.Callback callback = new QueueItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        adapter.setEventListener(new QueueListAdapter.EventListener() {
            @Override
            public void onItemViewClicked(View v) {
                int i = v.getId();
                if (i == R.id.container) {
                    Log.d(TAG, "onItemViewClicked() container " + v.getTag(R.string.queue_tag_item));
                    onContainerClicked(v);

                } else if (i == R.id.play_pause) {
                    Log.d(TAG, "onItemViewClicked() play-pause " + v.getTag(R.string.queue_tag_item));
                    onPlayPauseClicked(v);

                } else if (i == R.id.play_upcoming) {
                    mProvider.onUpcomingPlayClicked(v,
                            (MediaQueueItem) v.getTag(R.string.queue_tag_item));

                } else if (i == R.id.stop_upcoming) {
                    mProvider.onUpcomingStopClicked(v,
                            (MediaQueueItem) v.getTag(R.string.queue_tag_item));

                }
            }
        });

        mCastManager = VideoCastManager.getInstance();
    }

    private void onPlayPauseClicked(View view) {
        try {
            mCastManager.togglePlayback();
        } catch (CastException | TransientNetworkDisconnectionException |NoConnectionException e) {
            Log.e(TAG, "Failed to toggle playback status");
        }
    }

    private void onContainerClicked(View view) {
        MediaQueueItem item = (MediaQueueItem) view.getTag(R.string.queue_tag_item);
        try {
            if (mProvider.isQueueDetached()) {
                Log.d(TAG, "Is detached: itemId = " + item.getItemId());

                int currentPosition = mProvider.getPositionByItemId(item.getItemId());
                MediaQueueItem[] items = Utils.rebuildQueue(mProvider.getItems());
                ((CastApplication) getActivity().getApplicationContext())
                        .loadQueue(items, currentPosition);
                // temporary mCastManager.queueLoad(items, currentPosition, MediaStatus.REPEAT_MODE_REPEAT_OFF,
                // temporary        null);
            } else {
                int currentItemId = mProvider.getCurrentItemId();
                if (currentItemId == item.getItemId()) {
                    // we selected the one that is currently playing so we take the user to the
                    // full screen controller
                    mCastManager.onTargetActivityInvoked(getActivity());
                } else {
                    // a different item in the queue was selected so we jump there
                    mCastManager.queueJumpToItem(item.getItemId(), null);
                }
            }
        } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
            Log.e(TAG, "Failed to start playback of the new item");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }
}

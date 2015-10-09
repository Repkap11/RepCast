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

package com.google.sample.cast.refplayer.queue;

import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.libraries.cast.companionlibrary.cast.MediaQueue;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;

import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A singleton to manage the queue. Upon instantiation, it syncs up its own copy of the queue with
 * the one that the VideoCastManager holds. After that point, it maintains an up-to-date version of
 * the queue. UI elements get their data from this class. A boolean field, {@code mDetachedQueue} is
 * used to manage whether this changes to the queue coming from the cast framework should be
 * reflected here or not; when in "detached" mode, it means that its own copy of the queue is not
 * kept up to date with the one that the cast framework has. This is needed to preserve the queue
 * when the media session ends.
 */
public class QueueDataProvider {

    private static final String TAG = "QueueDataProvider";
    public static final int INVALID = -1;
    private final VideoCastManager mCastManager;
    private List<MediaQueueItem> mQueue = new CopyOnWriteArrayList<>();
    private static QueueDataProvider mInstance;
    private final Object mLock = new Object();
    private int mRepeatMode;
    private boolean mShuffle;
    private MediaQueueItem mCurrentIem;
    private MediaQueueItem mUpcomingItem;
    private OnQueueDataChangedListener mListener;
    private boolean mDetachedQueue = true;

    private QueueDataProvider(){
        mCastManager = VideoCastManager.getInstance();
        mUpcomingItem = mCastManager.getPreLoadingItem();
        MediaQueue mediaQueue = mCastManager.getMediaQueue();
        if (mediaQueue != null && mediaQueue.getQueueItems() != null) {
            mQueue = new CopyOnWriteArrayList<>(mediaQueue.getQueueItems());
            mRepeatMode = mediaQueue.getRepeatMode();
            mShuffle = mediaQueue.isShuffle();
            mCurrentIem = mediaQueue.getCurrentItem();
            mDetachedQueue = false;
        } else {
            mQueue = new CopyOnWriteArrayList<>();
            mRepeatMode = MediaStatus.REPEAT_MODE_REPEAT_OFF;
            mShuffle = false;
            mCurrentIem = null;
        }
        VideoCastConsumerImpl castConsumer = new VideoCastConsumerImpl() {
            @Override
            public void onMediaQueueUpdated(List<MediaQueueItem> queueItems, MediaQueueItem item,
                    int repeatMode, boolean shuffle) {
                if (queueItems == null) {
                    Log.d(TAG, "Queue is cleared");
                    mQueue = new CopyOnWriteArrayList<>();
                } else {
                    Log.d(TAG, "Queue is updated with a list of size: " + queueItems.size());
                    if (queueItems.size() > 0) {
                        mQueue = new CopyOnWriteArrayList<>(queueItems);
                        mDetachedQueue = false;
                    } else {
                        mQueue = new CopyOnWriteArrayList<>();
                        mDetachedQueue = true;
                    }
                }
                mRepeatMode = repeatMode;
                mShuffle = shuffle;
                mCurrentIem = item;
                if (mListener != null) {
                    mListener.onQueueDataChanged();
                }
                Log.d(TAG, "Queue was updated");
            }

            @Override
            public void onRemoteMediaPreloadStatusUpdated(MediaQueueItem item) {
                Log.d(TAG, "onRemoteMediaPreloadStatusUpdated() with item=" + item);
                mUpcomingItem = item;
                if (mListener != null) {
                    mListener.onQueueDataChanged();
                }
            }

            @Override
            public void onRemoteMediaPlayerStatusUpdated() {
                if (mListener != null) {
                    MediaStatus mediaStatus = mCastManager.getMediaStatus();
                    if (mediaStatus != null) {
                        int itemId = mediaStatus.getCurrentItemId();
                        mCurrentIem = mediaStatus.getQueueItemById(itemId);
                    }
                    mListener.onQueueDataChanged();
                }
            }

            @Override
            public void onDisconnected() {
                clearQueue();
                if (mListener != null) {
                    mListener.onQueueDataChanged();
                }
            }

            @Override
            public void onUpcomingPlayClicked(View view, MediaQueueItem upcomingItem) {
                QueueDataProvider.this.onUpcomingPlayClicked(view, upcomingItem);
            }

            @Override
            public void onUpcomingStopClicked(View view, MediaQueueItem upcomingItem) {
                QueueDataProvider.this.onUpcomingStopClicked(view, upcomingItem);
            }
        };
        mCastManager.addVideoCastConsumer(castConsumer);
    }

    public void onUpcomingStopClicked(View view, MediaQueueItem upcomingItem) {
        // need to truncate the queue on the remote device so that we can complete the playback of
        // the current item but not go any further. Alternatively, one could just stop the playback
        // here, if that was acceptable.
        int position = getPositionByItemId(upcomingItem.getItemId());
        int[] itemIds = new int[getCount() - position];
        for(int i = 0; i < itemIds.length; i++) {
            itemIds[i] = mQueue.get(i + position ).getItemId();
        }
        try {
            mCastManager.queueRemoveItems(itemIds, null);
        } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
            Log.e(TAG, "onUpcomingStopClicked(): Failed to remove items from queue", e);
        }
    }

    public void onUpcomingPlayClicked(View view, MediaQueueItem upcomingItem) {
        try {
            mCastManager.queueJumpToItem(upcomingItem.getItemId(), null);
        } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
            Log.e(TAG, "onUpcomingPlayClicked(): Failed to remove items from queue", e);
        }
    }

    public boolean isQueueDetached() {
        return mDetachedQueue;
    }

    public int getPositionByItemId(int itemId) {
        if (mQueue.isEmpty()) {
            return INVALID;
        }
        for(int i=0; i < mQueue.size(); i++) {
            if (mQueue.get(i).getItemId() == itemId) {
                return i;
            }
        }
        return INVALID;
    }

    public synchronized static QueueDataProvider getInstance() {
        if (mInstance == null) {
            mInstance = new QueueDataProvider();
        }
        return mInstance;
    }

    public void removeFromQueue(int position) {
        synchronized (mLock) {
            try {
                mCastManager.queueRemoveItem(mQueue.get(position).getItemId(), null);
            } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
                Log.e(TAG, "Failed to remove a queue item at position " + position, e);
            }
        }
    }

    public void removeAll() {
        synchronized (mLock) {
            if (mQueue.isEmpty()) {
                return;
            }
            try {
                int[] itemIds = new int[mQueue.size()];
                for(int i = 0; i < mQueue.size(); i++) {
                    itemIds[i] = mQueue.get(i).getItemId();
                }
                mCastManager.queueRemoveItems(itemIds, null);
                mQueue.clear();
            } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
                Log.e(TAG, "Failed to remove all items from the queue", e);
            }
        }
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        try {
            int itemId = mQueue.get(fromPosition).getItemId();
            mCastManager.queueMoveItemToNewIndex(itemId, toPosition, null);
            final MediaQueueItem item = mQueue.remove(fromPosition);
            mQueue.add(toPosition, item);
        } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
            Log.e(TAG, String.format("Failed to move a queue item from position %d to %d",
                    fromPosition, toPosition), e);
        }
    }

    public int getCount() {
        return mQueue.size();
    }

    public MediaQueueItem getItem(int position) {
        return mQueue.get(position);
    }

    public void clearQueue() {
        mQueue.clear();
        mDetachedQueue = true;
        mCurrentIem = null;
    }

    public int getRepeatMode() {
        return mRepeatMode;
    }

    public boolean isShuffleOn() {
        return mShuffle;
    }

    public MediaQueueItem getCurrentItem() {
        return mCurrentIem;
    }

    public int getCurrentItemId() {
        return mCurrentIem.getItemId();
    }

    public MediaQueueItem getUpcomingItem() {
        Log.d(TAG, "[upcoming] getUpcomingItem() returning " + mUpcomingItem);
        return mUpcomingItem;
    }

    public void setOnQueueDataChangedListener(OnQueueDataChangedListener listener) {
        mListener = listener;
    }

    public List<MediaQueueItem> getItems() {
        return mQueue;
    }

    public interface OnQueueDataChangedListener {
        void onQueueDataChanged();
    }
}

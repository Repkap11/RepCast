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

package com.google.sample.cast.refplayer;

import com.google.android.gms.cast.CastStatusCodes;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions
        .TransientNetworkDisconnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.player.VideoCastController;
import com.google.android.libraries.cast.companionlibrary.cast.player.VideoCastControllerActivity;

import android.app.Application;

import java.util.Locale;

/**
 * The {@link Application} for this demo application.
 */
public class CastApplication extends Application {

    public static final double VOLUME_INCREMENT = 0.05;
    public static final int PRELOAD_TIME_S = 20;

    /*
     * (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        String applicationId = getString(R.string.app_id);

        // initialize VideoCastManager
        VideoCastManager.
                initialize(this, applicationId, VideoCastControllerActivity.class, null).
                setVolumeStep(VOLUME_INCREMENT).
                enableFeatures(VideoCastManager.FEATURE_NOTIFICATION |
                        VideoCastManager.FEATURE_LOCKSCREEN |
                        VideoCastManager.FEATURE_WIFI_RECONNECT |
                        VideoCastManager.FEATURE_AUTO_RECONNECT |
                        VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
                        VideoCastManager.FEATURE_DEBUGGING);

        // this is the default behavior but is mentioned to make it clear that it is configurable.
        VideoCastManager.getInstance().setNextPreviousVisibilityPolicy(
                VideoCastController.NEXT_PREV_VISIBILITY_POLICY_DISABLED);

        // this is to set the launch options, the following values are the default values
        VideoCastManager.getInstance().setLaunchOptions(false, Locale.getDefault());

        // this is the default behavior but is mentioned to make it clear that it is configurable.
        VideoCastManager.getInstance().setCastControllerImmersive(true);
    }

    /**
     * Loading queue items. The only reason we are using this instead of using the VideoCastManager
     * directly is to get around an issue on receiver side for HLS + VTT for a queue; this will be
     * addressed soon and the following workaround will be removed.
     */
    public void loadQueue(MediaQueueItem[] items, int startIndex)
            throws TransientNetworkDisconnectionException, NoConnectionException {
        final VideoCastManager castManager = VideoCastManager.getInstance();
        castManager.addVideoCastConsumer(new VideoCastConsumerImpl() {
            @Override
            public void onMediaQueueOperationResult(int operationId, int statusCode) {
                if (operationId == VideoCastManager.QUEUE_OPERATION_LOAD) {
                    if (statusCode == CastStatusCodes.SUCCESS) {
                        castManager.setActiveTrackIds(new long[]{});
                    }
                    castManager.removeVideoCastConsumer(this);
                }
            }
        });
        castManager.queueLoad(items, startIndex, MediaStatus.REPEAT_MODE_REPEAT_OFF, null);
    }

}

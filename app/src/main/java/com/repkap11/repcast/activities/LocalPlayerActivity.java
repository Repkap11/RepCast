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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

import com.androidquery.AQuery;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.repkap11.repcast.R;
import com.repkap11.repcast.VideoProvider;
import com.repkap11.repcast.application.CastApplication;
import com.repkap11.repcast.queue.ui.QueueListViewActivity;
import com.repkap11.repcast.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class LocalPlayerActivity extends AppCompatActivity {

    private static final String TAG = "LocalPlayerActivity";
    private VideoView mVideoView;
    private TextView mTitleView;
    private TextView mDescriptionView;
    private TextView mStartText;
    private TextView mEndText;
    private SeekBar mSeekbar;
    private ImageView mPlayPause;
    private ProgressBar mLoading;
    private View mControllers;
    private View mContainer;
    private ImageView mCoverArt;
    private VideoCastManager mCastManager;
    private Timer mSeekbarTimer;
    private Timer mControllersTimer;
    private PlaybackLocation mLocation;
    private PlaybackState mPlaybackState;
    private final Handler mHandler = new Handler();
    private float mAspectRatio = 72f / 128;
    private AQuery mAquery;
    private MediaInfo mSelectedMedia;
    private boolean mControllersVisible;
    private int mDuration;
    protected MediaInfo mRemoteMediaInformation;
    private VideoCastConsumerImpl mCastConsumer;
    private TextView mAuthorView;
    private ImageButton mPlayCircle;

    public static final String EXTRA_ASPECT_RATIO = "EXTRA_ASPECT_RATIO";
    private boolean mVisable = true;
    private int mOrientation;
    private ImageButton mRotatePlayer;

    /*
     * indicates whether we are doing a local or a remote playback
     */
    public enum PlaybackLocation {
        LOCAL,
        REMOTE
    }

    /*
     * List of various states that we can be in
     */
    public enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setNavigationBarColor(Color.BLACK);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);
        mAquery = new AQuery(this);
        loadViews();
        mCastManager = VideoCastManager.getInstance();
        mCastManager.setCastControllerImmersive(false);
        setupControlsCallbacks();
        setupCastListener();
        String castPath;
        String mimeType = getIntent().getType();
        String title = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        mAspectRatio = getIntent().getFloatExtra(EXTRA_ASPECT_RATIO, mAspectRatio);
//        Log.e(TAG, "Paul Playing with aspect ratio:"+mAspectRatio);

        int trackType;
        int mediaType;
        if (mimeType.startsWith("video")) {
            trackType = MediaTrack.TYPE_VIDEO;
            mediaType = MediaMetadata.MEDIA_TYPE_MOVIE;
        } else if (mimeType.startsWith("audio")) {
            trackType = MediaTrack.TYPE_AUDIO;
            mediaType = MediaMetadata.MEDIA_TYPE_MUSIC_TRACK;
        } else {
            trackType = MediaTrack.TYPE_UNKNOWN;
            mediaType = MediaMetadata.MEDIA_TYPE_GENERIC;
        }
        try {
            URL url = new URI(getIntent().getDataString()).toURL();
            castPath = url.toExternalForm();
            if (title == null) {
                title = new File(url.getPath()).getName();
            }
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
            finish();
            return;
        }
        Log.e(TAG, "Cast Path:" + castPath);

        MediaInfo.Builder builder = new MediaInfo.Builder(castPath);
        builder.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED);
        builder.setContentType(mimeType);
        MediaMetadata metadata = new MediaMetadata(mediaType);
        metadata.putString(MediaMetadata.KEY_TITLE, title);
        //metadata.putString(MediaMetadata.KEY_SUBTITLE, "Sub title Text");
        builder.setMetadata(metadata);
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject();
            jsonObj.put(VideoProvider.KEY_DESCRIPTION, "");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to add description to the json object", e);
        }
        MediaTrack.Builder trackBuilder = new MediaTrack.Builder(1, MediaTrack.TYPE_VIDEO);


        //trackBuilder.setContentId(castPath);
        Log.e(TAG, "Setting path String: " + castPath);
        trackBuilder.setName(title);
        builder.setMediaTracks(Collections.singletonList(trackBuilder.build()));
        builder.setCustomData(jsonObj);
        mSelectedMedia = builder.build();
        setupActionBar();
        Bundle b = getIntent().getExtras();
        boolean shouldStartPlayback = false;
        int startPosition = 0;
        if (b != null) {
            shouldStartPlayback = b.getBoolean("shouldStart", false);
            startPosition = b.getInt("startPosition", 0);
        }
        mVideoView.setVideoURI(Uri.parse(mSelectedMedia.getContentId()));
        Log.e(TAG, "Setting url of the VideoView to: " + Uri.parse(mSelectedMedia.getContentId()));
        if (shouldStartPlayback) {
            // this will be the case only if we are coming from the
            // CastControllerActivity by disconnecting from a device
            mPlaybackState = PlaybackState.PLAYING;
            updatePlaybackLocation(PlaybackLocation.LOCAL);
            updatePlayButton(mPlaybackState);
            if (startPosition > 0) {
                mVideoView.seekTo(startPosition);
            }
            mVideoView.start();
            startControllersTimer();
        } else {
            // we should load the video but pause it
            // and show the album art.
            if (mCastManager.isConnected()) {
                updatePlaybackLocation(PlaybackLocation.REMOTE);
            } else {
                updatePlaybackLocation(PlaybackLocation.LOCAL);
            }
            mPlaybackState = PlaybackState.IDLE;
            updatePlayButton(mPlaybackState);
        }

        if (null != mTitleView) {
            updateMetadata(true);
        }
    }

    private void setupCastListener() {
        mCastConsumer = new VideoCastConsumerImpl() {
            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata,
                                               String sessionId, boolean wasLaunched) {
                Log.d(TAG, "onApplicationLaunched() is reached");
                if (null != mSelectedMedia) {

                    if (mPlaybackState == PlaybackState.PLAYING) {
                        mVideoView.pause();
                        try {
                            loadRemoteMedia(mSeekbar.getProgress(), true);
                            finish();
                        } catch (Exception e) {
                            Utils.handleException(LocalPlayerActivity.this, e);
                        }
                        return;
                    } else {
                        mPlaybackState = PlaybackState.IDLE;
                        updatePlaybackLocation(PlaybackLocation.REMOTE);
                    }
                }
                updatePlayButton(mPlaybackState);
                invalidateOptionsMenu();
            }

            @Override
            public void onApplicationDisconnected(int errorCode) {
                Log.d(TAG, "onApplicationDisconnected() is reached with errorCode: " + errorCode);
                updatePlaybackLocation(PlaybackLocation.LOCAL);
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "onDisconnected() is reached");
                mPlaybackState = PlaybackState.IDLE;
                mLocation = PlaybackLocation.LOCAL;
                updatePlayButton(mPlaybackState);
                invalidateOptionsMenu();
            }

            @Override
            public void onRemoteMediaPlayerMetadataUpdated() {
                try {
                    mRemoteMediaInformation = mCastManager.getRemoteMediaInformation();
                } catch (Exception e) {
                    // silent
                }
            }

            @Override
            public void onFailed(int resourceId, int statusCode) {

            }

            @Override
            public void onConnectionSuspended(int cause) {
                Utils.showToast(LocalPlayerActivity.this,
                        R.string.connection_temp_lost);
            }

            @Override
            public void onConnectivityRecovered() {
                Utils.showToast(LocalPlayerActivity.this,
                        R.string.connection_recovered);
            }

        };
    }

    private void updatePlaybackLocation(PlaybackLocation location) {
        mLocation = location;
        if (location == PlaybackLocation.LOCAL) {
            if (mPlaybackState == PlaybackState.PLAYING ||
                    mPlaybackState == PlaybackState.BUFFERING) {
                setCoverArtStatus(null);
                startControllersTimer();
            } else {
                stopControllersTimer();
                //TODO MediaInfo
                //setCoverArtStatus(com.google.android.libraries.cast.companionlibrary.utils.Utils.getImageUrl(mSelectedMedia, 0));
            }

        } else {
            stopControllersTimer();
            //TODO MediaInfo
            //setCoverArtStatus(com.google.android.libraries.cast.companionlibrary.utils.Utils.getImageUrl(mSelectedMedia, 0));
            updateControllersVisibility(false);
        }
    }

    private void play(int position) {
        startControllersTimer();
        switch (mLocation) {
            case LOCAL:
                mVideoView.seekTo(position);
                mVideoView.start();
                break;
            case REMOTE:
                mPlaybackState = PlaybackState.BUFFERING;
                updatePlayButton(mPlaybackState);
                try {
                    mCastManager.play(position);
                } catch (Exception e) {
                    Utils.handleException(this, e);
                }
                break;
            default:
                break;
        }
        restartTrickplayTimer();
    }

    private void togglePlayback() {
        stopControllersTimer();
        switch (mPlaybackState) {
            case PAUSED:
                switch (mLocation) {
                    case LOCAL:
                        mVideoView.start();
                        if (!mCastManager.isConnecting()) {
                            Log.d(TAG, "Playing locally...");
                            mCastManager.clearPersistedConnectionInfo(
                                    VideoCastManager.CLEAR_SESSION);
                        }
                        mPlaybackState = PlaybackState.PLAYING;
                        startControllersTimer();
                        restartTrickplayTimer();
                        updatePlaybackLocation(PlaybackLocation.LOCAL);
                        break;
                    case REMOTE:
                        try {
                            mCastManager.checkConnectivity();
                            loadRemoteMedia(0, true);
                            finish();
                        } catch (Exception e) {
                            Utils.handleException(LocalPlayerActivity.this, e);
                            return;
                        }
                        break;
                    default:
                        break;
                }
                break;

            case PLAYING:
                mPlaybackState = PlaybackState.PAUSED;
                mVideoView.pause();
                break;

            case IDLE:
                switch (mLocation) {
                    case LOCAL:
                        mVideoView.setVideoURI(Uri.parse(mSelectedMedia.getContentId()));
                        mVideoView.seekTo(0);
                        mVideoView.start();
                        mPlaybackState = PlaybackState.PLAYING;
                        restartTrickplayTimer();
                        updatePlaybackLocation(PlaybackLocation.LOCAL);
                        break;
                    case REMOTE:
                        try {
                            mCastManager.checkConnectivity();
                            Utils.showQueuePopup(this, mPlayCircle, mSelectedMedia);
                        } catch (Exception e) {
                            Utils.handleException(LocalPlayerActivity.this, e);
                            return;
                        }
                        break;
                }
            default:
                break;
        }
        updatePlayButton(mPlaybackState);
    }

    private void loadRemoteMedia(int position, boolean autoPlay) {
        mCastManager.startVideoCastControllerActivity(this, mSelectedMedia, position, autoPlay);
    }

    private void setCoverArtStatus(String url) {
        if (null != url) {
            mAquery.id(mCoverArt).image(url);
            mCoverArt.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.INVISIBLE);
        } else {
            mCoverArt.setVisibility(View.GONE);
            mVideoView.setVisibility(View.VISIBLE);
        }
    }

    private void stopTrickplayTimer() {
        Log.d(TAG, "Stopped TrickPlay Timer");
        if (null != mSeekbarTimer) {
            mSeekbarTimer.cancel();
        }
    }

    private void restartTrickplayTimer() {
        stopTrickplayTimer();
        mSeekbarTimer = new Timer();
        mSeekbarTimer.scheduleAtFixedRate(new UpdateSeekbarTask(), 100, 1500);
        Log.d(TAG, "Restarted TrickPlay Timer");
    }

    private void stopControllersTimer() {
        if (null != mControllersTimer) {
            mControllersTimer.cancel();
        }
    }

    private void startControllersTimer() {
        if (null != mControllersTimer) {
            mControllersTimer.cancel();
        }
        if (mLocation == PlaybackLocation.REMOTE) {
            return;
        }
        mControllersTimer = new Timer();
        mControllersTimer.schedule(new HideControllersTask().setDoHide(false), 1500);
    }

    // should be called from the main thread
    private void updateControllersVisibility(boolean show) {
        if (show) {
            getSupportActionBar().show();
            mControllers.setVisibility(View.VISIBLE);
            mPlayPause.setVisibility(View.VISIBLE);

        } else {
            if (!Utils.isOrientationPortrait(this)) {
                getSupportActionBar().hide();
                hideSystemUI();
            }
            mControllers.setVisibility(View.INVISIBLE);
            mPlayPause.setVisibility(View.INVISIBLE);

            ;
        }
        //onConfigurationChanged(getResources().getConfiguration());
    }

    // This hides the system bars using immersive mode
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE //dont resize when system bars visible
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION// dont resize when system bars visible
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //hideSystemUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() was called");
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() was called");
        super.onStop();
        if (mLocation == PlaybackLocation.LOCAL) {

            if (null != mSeekbarTimer) {
                mSeekbarTimer.cancel();
                mSeekbarTimer = null;
            }
            if (null != mControllersTimer) {
                mControllersTimer.cancel();
            }
            // since we are playing locally, we need to stop the playback of
            // video (if user is not watching, pause it!)
            mVideoView.pause();
            mPlaybackState = PlaybackState.PAUSED;
            updatePlayButton(PlaybackState.PAUSED);
        }
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        mCastManager.decrementUiCounter();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() is called");
        if (null != mCastManager) {
            mCastConsumer = null;
        }
        stopControllersTimer();
        stopTrickplayTimer();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart was called");
        super.onStart();
        mCastManager = VideoCastManager.getInstance();
        mCastManager.addVideoCastConsumer(mCastConsumer);
        mCastManager.incrementUiCounter();
        if (mCastManager.isConnected()) {
            updatePlaybackLocation(PlaybackLocation.REMOTE);
        } else {
            updatePlaybackLocation(PlaybackLocation.LOCAL);
        }
        onConfigurationChanged(getResources().getConfiguration());
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
        //hideSystemUI();
        super.onResume();
    }

    private class HideControllersTask extends TimerTask {
        private boolean mHide = false;

        public HideControllersTask setDoHide(boolean hide){
            mHide = hide;
            return this;
        }
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateControllersVisibility(mHide);
                    mControllersVisible = mHide;
                }
            });

        }
    }

    private class UpdateSeekbarTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (mLocation == PlaybackLocation.LOCAL) {
                        int currentPos = mVideoView.getCurrentPosition();
                        updateSeekbar(currentPos, mDuration);
                    }
                }
            });
        }
    }

    private void setupControlsCallbacks() {
        mVideoView.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e(TAG, "OnErrorListener.onError(): VideoView encountered an " +
                        "error, what: " + what + ", extra: " + extra);
                String msg;
                if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    msg = getString(R.string.video_error_media_load_timeout);
                } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    msg = getString(R.string.video_error_server_unaccessible);
                } else {
                    msg = getString(R.string.video_error_unknown_error);
                }
                Utils.showErrorDialog(LocalPlayerActivity.this, msg);
                mVideoView.stopPlayback();
                mPlaybackState = PlaybackState.IDLE;
                updatePlayButton(mPlaybackState);
                return true;
            }
        });

        mVideoView.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d(TAG, "onPrepared is reached");
                mDuration = mp.getDuration();
                mEndText.setText(com.google.android.libraries.cast.companionlibrary.utils.Utils
                        .formatMillis(mDuration));
                mSeekbar.setMax(mDuration);
                restartTrickplayTimer();

                mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
//                        Log.d(TAG, "Paul Buffered to " + percent);
//                        if (percent<mSeekbar.getMax()) {
                            mSeekbar.setSecondaryProgress(percent*mSeekbar.getMax() / 100);
//                        }
                    }
                });
            }
        });

        mVideoView.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                stopTrickplayTimer();
                Log.d(TAG, "setOnCompletionListener()");
                mPlaybackState = PlaybackState.IDLE;
                updatePlayButton(mPlaybackState);
            }
        });

        mVideoView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mControllersVisible) {
                    updateControllersVisibility(!mControllersVisible);
                }
                startControllersTimer();
                return false;
            }
        });

        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {

                mediaPlayer.getVideoHeight();
                mAspectRatio = ((float)mediaPlayer.getVideoWidth() / ((float)mediaPlayer.getVideoHeight()));
                updateMetadata(mVisable);
                return false;
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mPlaybackState == PlaybackState.PLAYING) {
                    play(seekBar.getProgress());
                } else if (mPlaybackState != PlaybackState.IDLE) {
                    mVideoView.seekTo(seekBar.getProgress());
                }
                startControllersTimer();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopTrickplayTimer();
                mVideoView.pause();
                stopControllersTimer();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                mStartText.setText(com.google.android.libraries.cast.companionlibrary.utils.Utils
                        .formatMillis(progress));
            }
        });

        mPlayPause.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mLocation == PlaybackLocation.LOCAL) {
                    togglePlayback();
                }
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return mCastManager.onDispatchVolumeKeyEvent(event, CastApplication.VOLUME_INCREMENT)
                || super.dispatchKeyEvent(event);
    }

    private void updateSeekbar(int position, int duration) {
        mSeekbar.setProgress(position);
        mSeekbar.setMax(duration);
        mStartText.setText(com.google.android.libraries.cast.companionlibrary.utils.Utils
                .formatMillis(position));
        mEndText.setText(com.google.android.libraries.cast.companionlibrary.utils.Utils
                .formatMillis(duration));
    }

    private void updatePlayButton(PlaybackState state) {
        Log.d(TAG, "Controls: PlayBackState: " + state);
        boolean isConnected = mCastManager.isConnected() || mCastManager.isConnecting();
        mControllers.setVisibility(isConnected ? View.GONE : View.VISIBLE);
        mPlayPause.setVisibility(isConnected ? View.GONE : View.VISIBLE);
        switch (state) {
            case PLAYING:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_av_pause_dark));
                mPlayCircle.setVisibility(isConnected ? View.VISIBLE : View.GONE);
                break;
            case IDLE:
                mPlayCircle.setVisibility(View.VISIBLE);
                mControllers.setVisibility(View.GONE);
                mCoverArt.setVisibility(View.VISIBLE);
                mVideoView.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.INVISIBLE);
                break;
            case PAUSED:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_av_play_dark));
                mPlayCircle.setVisibility(isConnected ? View.VISIBLE : View.GONE);

                mPlayPause.setVisibility(View.VISIBLE);
                break;
            case BUFFERING:
                mPlayPause.setVisibility(View.INVISIBLE);
                mLoading.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onUserLeaveHint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (mLocation == PlaybackLocation.LOCAL) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                    enterPictureInPictureMode();
                }
            }
        }
    }


    @SuppressLint("NewApi")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mOrientation = newConfig.orientation;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
            updateMetadata(false);
            mContainer.setBackgroundColor(getResources().getColor(R.color.black));

        } else {
            getSupportActionBar().show();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            updateMetadata(true);
            mContainer.setBackgroundColor(getResources().getColor(R.color.primary_background));
        }
    }

    private void updateMetadata(boolean visible) {
        mVisable = visible;
        Point displaySize = Utils.getDisplaySize(this);
        int outX;
        int outY;
        if (mAspectRatio == 0){
            outX = 0;
            outY = 0;
        } else {
            int scaleX = (int) (displaySize.y / mAspectRatio);
            int scaleY = (int) (displaySize.x * mAspectRatio);
            if (scaleX > displaySize.x) {
                outX = displaySize.x;
                outY = (int) (outX / mAspectRatio);
            } else {
                if (scaleY > displaySize.y) {
                    outY = displaySize.y;
                    outX = (int) (outY * mAspectRatio);
                } else {
                    outY = displaySize.y;
                    outX = displaySize.x;
                }
            }
        }

//        Log.e(TAG,"Paul using "+ outX+":"+outY);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(outX, outY);
        if (!visible) {//landscape
            mDescriptionView.setVisibility(View.GONE);
            mTitleView.setVisibility(View.GONE);
            mAuthorView.setVisibility(View.GONE);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            mVideoView.setLayoutParams(lp);
            mVideoView.invalidate();
        } else {//portrate
            MediaMetadata mm = mSelectedMedia.getMetadata();
            mDescriptionView.setText(mSelectedMedia.getCustomData().optString(
                    VideoProvider.KEY_DESCRIPTION));
            mTitleView.setText(mm.getString(MediaMetadata.KEY_TITLE));
            mAuthorView.setText(mm.getString(MediaMetadata.KEY_SUBTITLE));
            mDescriptionView.setVisibility(View.VISIBLE);
            mTitleView.setVisibility(View.VISIBLE);
            mAuthorView.setVisibility(View.VISIBLE);

            lp.addRule(RelativeLayout.BELOW, R.id.toolbar);
//            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            mVideoView.setLayoutParams(lp);
            mVideoView.invalidate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.player, menu);
        mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_show_queue).setVisible(mCastManager.isConnected());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        int i1 = item.getItemId();
        if (i1 == R.id.action_show_queue) {
            i = new Intent(LocalPlayerActivity.this, QueueListViewActivity.class);
            startActivity(i);

        } else if (i1 == android.R.id.home) {
            ActivityCompat.finishAfterTransition(this);

        }
        return true;
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mSelectedMedia.getMetadata().getString(MediaMetadata.KEY_TITLE));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void toggleRotation() {
        int newOrientation = mOrientation == Configuration.ORIENTATION_LANDSCAPE ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        this.setRequestedOrientation(newOrientation);
    }

    private void loadViews() {
        mVideoView = (VideoView) findViewById(R.id.videoView1);
        mTitleView = (TextView) findViewById(R.id.textView1);
        mDescriptionView = (TextView) findViewById(R.id.textView2);
        mDescriptionView.setMovementMethod(new ScrollingMovementMethod());
        mAuthorView = (TextView) findViewById(R.id.textView3);
        mStartText = (TextView) findViewById(R.id.startText);
        mEndText = (TextView) findViewById(R.id.endText);
        mSeekbar = (SeekBar) findViewById(R.id.seekBar1);
        mPlayPause = (ImageView) findViewById(R.id.imageView2);
        mLoading = (ProgressBar) findViewById(R.id.progressBar1);
        mControllers = findViewById(R.id.controllers);
        mContainer = findViewById(R.id.container);
        mCoverArt = (ImageView) findViewById(R.id.coverArtView);
        ViewCompat.setTransitionName(mCoverArt, getString(R.string.transition_image));
        mPlayCircle = (ImageButton) findViewById(R.id.play_circle);
        mPlayCircle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayback();
            }
        });
        mRotatePlayer = (ImageButton) findViewById(R.id.rotate_player);
        mRotatePlayer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleRotation();
            }
        });
    }
}

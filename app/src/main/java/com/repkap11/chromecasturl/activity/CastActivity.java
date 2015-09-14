package com.repkap11.chromecasturl.activity;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.repkap11.chromecasturl.R;

import java.io.IOException;

public abstract class CastActivity extends AppCompatActivity {
    private static final String TAG = CastActivity.class.getSimpleName();
    private static final String APPLICATION_ID = "CB2D44C5";
    private MediaRouteSelector mMediaRouteSelector;
    private CastDevice mSelectedDevice;
    private MediaRouter mMediaRouter;
    private MyMediaRouterCallback mMediaRouterCallback;
    private GoogleApiClient mApiClient;
    private boolean mWaitingForReconnect = false;
    private ConnectionCallbacks mConnectionCallbacks;
    private ConnectionFailedListener mConnectionFailedListener;
    private Cast.Listener mCastClientListener;
    private RemoteMediaPlayer mRemoteMediaPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouterCallback = new MyMediaRouterCallback();
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(APPLICATION_ID))
                .build();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    @Override
    protected void onStop() {
        mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onStop();
    }

    private void connectWithGoogleAPI() {
        mCastClientListener = new Cast.Listener() {
            @Override
            public void onApplicationStatusChanged() {
                if (mApiClient != null) {
                    Log.d(TAG, "onApplicationStatusChanged: "
                            + Cast.CastApi.getApplicationStatus(mApiClient));
                }
            }

            @Override
            public void onVolumeChanged() {
                if (mApiClient != null) {
                    Log.d(TAG, "onVolumeChanged: " + Cast.CastApi.getVolume(mApiClient));
                }
            }

            @Override
            public void onApplicationDisconnected(int errorCode) {
                teardown();
            }
        };
        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                .builder(mSelectedDevice, mCastClientListener);


        mConnectionCallbacks = new ConnectionCallbacks();
        mConnectionFailedListener = new ConnectionFailedListener();
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Cast.API, apiOptionsBuilder.build())
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mConnectionFailedListener)
                .build();
        mApiClient.connect();
    }

    private void teardown() {
        Log.e(TAG, "Teardown");
    }

    private class MyMediaRouterCallback extends MediaRouter.Callback {
        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
            String routeId = info.getId();
            Log.e(TAG, "Connected to:" + routeId);
            connectWithGoogleAPI();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            teardown();
            mSelectedDevice = null;
        }
    }

    private class ConnectionFailedListener implements
            GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            teardown();
        }
    }

    private class ConnectionCallbacks implements
            GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle connectionHint) {
            if (mWaitingForReconnect) {
                mWaitingForReconnect = false;
                Log.e(TAG, "Reconnect channels is not implemented");
                reconnectChannels();
            } else {
                try {
                    Cast.CastApi.launchApplication(mApiClient, APPLICATION_ID, false).setResultCallback(
                            new ResultCallback<Cast.ApplicationConnectionResult>() {
                                @Override
                                public void onResult(Cast.ApplicationConnectionResult result) {
                                    Status status = result.getStatus();
                                    if (status.isSuccess()) {
                                        ApplicationMetadata applicationMetadata = result.getApplicationMetadata();
                                        String sessionId = result.getSessionId();
                                        String applicationStatus = result.getApplicationStatus();
                                        boolean wasLaunched = result.getWasLaunched();
                                        Log.e(TAG, "SessionID:" + sessionId + " applicationStatus:" + applicationStatus + " wasLauncher:" + wasLaunched);
                                        startCasting();
                                    } else {
                                        teardown();
                                    }
                                }
                            });
                } catch (Exception e) {
                    Log.e(TAG, "Failed to launch application", e);
                }
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            mWaitingForReconnect = true;
        }
    }

    private void startCasting() {
        mRemoteMediaPlayer = new RemoteMediaPlayer();
        mRemoteMediaPlayer.setOnStatusUpdatedListener(
                new RemoteMediaPlayer.OnStatusUpdatedListener() {
                    @Override
                    public void onStatusUpdated() {
                        MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
                        if (mediaStatus != null) {
                            boolean isPlaying = mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING;
                            Log.e(TAG,"Is playing");
                        }
                    }
                }

        );

        mRemoteMediaPlayer.setOnMetadataUpdatedListener(
                new RemoteMediaPlayer.OnMetadataUpdatedListener()

                {
                    @Override
                    public void onMetadataUpdated() {
                        MediaInfo mediaInfo = mRemoteMediaPlayer.getMediaInfo();
                        if (mediaInfo != null) {
                            MediaMetadata metadata = mediaInfo.getMetadata();
                            //TODO
                        }
                    }
                }

        );
        try

        {
            Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
        } catch (
                IOException e
                )

        {
            Log.e(TAG, "Exception while creating media channel", e);
        }

        mRemoteMediaPlayer.requestStatus(mApiClient).

                setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                                      @Override
                                      public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                                          if (!result.getStatus().isSuccess()) {
                                              Log.e(TAG, "Failed to request status.");
                                          }
                                      }
                                  }

                );
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE,

                getVideoTitle()

        );
        MediaInfo mediaInfo = new MediaInfo.Builder(
                getCastURL())
                .setContentType("video/mp4")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
        try

        {
            mRemoteMediaPlayer.load(mApiClient, mediaInfo, true)
                    .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                            if (result.getStatus().isSuccess()) {
                                Log.d(TAG, "Media loaded successfully");
                            }
                        }
                    });
        } catch (
                IllegalStateException e
                )

        {
            Log.e(TAG, "Problem occurred with media during loading", e);
        } catch (
                Exception e
                )

        {
            Log.e(TAG, "Problem opening media during loading", e);
        }

    }

    protected abstract String getCastURL();

    protected abstract String getVideoTitle();

    private void reconnectChannels() {
        Log.e(TAG, "Reconnect Channels called");
    }

}

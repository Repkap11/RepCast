package com.repkap11.repcast.model.rest;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.RepcastActivity;
import com.repkap11.repcast.fragments.RepcastFragment;
import com.repkap11.repcast.model.adapters.FileListAdapter;
import com.repkap11.repcast.model.parcelables.JsonDirectory;
import com.repkap11.repcast.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by paul on 9/10/15.
 */
public class RepcastSyncChecker extends AsyncTask<Void, Void, Pair<Boolean, String>> {
    private static final String TAG = RepcastSyncChecker.class.getSimpleName();
    private final WeakReference<RepcastActivity> mActivityReference;
    private final JsonDirectory.JsonFileDir mDir;
    private final String mLanPrefix;
    private final boolean mForceShare;
    private boolean mIsVideo = false;
    private float mAspectRatio = 0;

    public RepcastSyncChecker(RepcastActivity activity, JsonDirectory.JsonFileDir dir, boolean forceShare) {
        mForceShare = forceShare;
        mActivityReference = new WeakReference<>(activity);
        if (Utils.backendSupportsLocalCast(activity.getApplicationContext())) {
            mLanPrefix = activity.getString(R.string.endporint_lan);
        } else {
            mLanPrefix = null;
        }
        mDir = dir;
    }

    @Override
    protected Pair<Boolean, String> doInBackground(Void... params) {
        String wanurl = mDir.path;
        if (mLanPrefix != null) {
            String lanurl = mLanPrefix + Uri.encode(mDir.original, "//");
            Log.e(TAG, "LAN:" + lanurl);
            try {
                HttpURLConnection c = (HttpURLConnection) (new URL(lanurl).openConnection());
                c.setUseCaches(false);
                c.setRequestMethod("HEAD");
                c.setConnectTimeout(200);
                c.connect();

                int code = c.getResponseCode();
                Log.e(TAG, "Cache Code:" + code);
                if (code == 200) {
                    doAspectRatio(lanurl);
                    return new Pair<>(true, lanurl);
                } else {
                    doAspectRatio(wanurl);
                    return new Pair<>(false, wanurl);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        doAspectRatio(wanurl);
        return new Pair<>(false, wanurl);
    }

    void doAspectRatio(String url){
        mIsVideo = mDir.mimetype.equals("video/mp4") || mDir.mimetype.equals("audio/mpeg") || mDir.mimetype.equals("video/x-matroska");
        if (mIsVideo){
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();

            mmr.setDataSource(url,new HashMap<String, String>( ));
            float width = Float.parseFloat(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            float height = Float.parseFloat(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            mAspectRatio = width/height;
            Log.e(TAG, "Paul Got aspect ratio:"+mAspectRatio);
        } else {
            mAspectRatio = 1;
        }

    }

    @Override
    protected void onPostExecute(Pair<Boolean, String> pair) {
        RepcastActivity activity = mActivityReference.get();
        if (activity != null) {
            if (pair.first) {
                Toast.makeText(activity, "Playing from LAN", Toast.LENGTH_SHORT).show();
            }
            activity.showFileWithURL(mDir, pair.second, mForceShare, mIsVideo, mAspectRatio);
        }
        super.onPostExecute(pair);
    }
}


package com.repkap11.repcast.model.rest;

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

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by paul on 9/10/15.
 */
public class RepcastSyncChecker extends AsyncTask<Void, Void, Pair<Boolean,String>> {
    private static final String TAG = RepcastSyncChecker.class.getSimpleName();
    private final WeakReference<RepcastActivity> mActivityReference;
    private final JsonDirectory.JsonFileDir mDir;
    private final String mWanPrefix;
    private final String mLanPrefix;

    public RepcastSyncChecker(RepcastActivity activity, JsonDirectory.JsonFileDir dir) {
        mActivityReference = new WeakReference<>(activity);
        mWanPrefix = activity.getString(R.string.endporint_wan);
        mLanPrefix = activity.getString(R.string.endporint_lan);
        mDir = dir;
    }

    @Override
    protected Pair<Boolean,String> doInBackground(Void... params) {
        String wanurl = mDir.path;
        String lanurl = mLanPrefix + Uri.encode(mDir.original, "//");
        Log.e(TAG,"LAN:"+lanurl);
        try {
            HttpURLConnection c = (HttpURLConnection)(new URL(lanurl).openConnection());
            c.setUseCaches(false);
            c.setRequestMethod("HEAD");
            c.setConnectTimeout(200);
            c.connect();

            int code = c.getResponseCode();
            Log.e(TAG,"Cache Code:"+code);
            if (code == 200){
                return new Pair<>(true, lanurl);
            } else {
                return new Pair<>(false, wanurl);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return new Pair<>(false, wanurl);
    }

    @Override
    protected void onPostExecute(Pair<Boolean,String> pair) {
        RepcastActivity activity = mActivityReference.get();
        if (activity != null) {
            if (pair.first){
                Toast.makeText(activity, "Playing from LAN", Toast.LENGTH_SHORT).show();
            }
            activity.showFileWithURL(mDir, pair.second);
        }
        super.onPostExecute(pair);
    }
}


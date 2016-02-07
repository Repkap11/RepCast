package com.repkap11.repcast.model;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by paul on 9/10/15.
 */
public class JsonTorrentListDownloader extends AsyncTask<String, Void, JsonTorrent> {
    private static final String TAG = JsonTorrentListDownloader.class.getSimpleName();
    private final WeakReference<TorrentListAdapter> mAdapterReference;

    public JsonTorrentListDownloader(TorrentListAdapter adapter) {
        mAdapterReference = new WeakReference<>(adapter);
    }

    @Override
    protected JsonTorrent doInBackground(String... params) {
        String url = params[0];
        Log.e(TAG,"URL:"+url);
        try {
            URLConnection c = new URL(url).openConnection();
            String username = "guest";
            String password = "guest";
            String userPassword = username + ":" + password;
            String encoding = Base64.encodeToString(userPassword.getBytes(),Base64.NO_WRAP);
            c.setRequestProperty("Authorization", "Basic " + encoding);
            c.setUseCaches(false);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonTorrent torrentList = objectMapper.readValue(c.getInputStream(), JsonTorrent.class);
            return torrentList;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(JsonTorrent fileList) {
        TorrentListAdapter adapter = mAdapterReference.get();
        if (adapter != null) {
            adapter.updateTorrentList(fileList);
        }
        super.onPostExecute(fileList);
    }
}


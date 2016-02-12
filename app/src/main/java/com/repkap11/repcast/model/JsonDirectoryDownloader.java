package com.repkap11.repcast.model;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by paul on 9/10/15.
 */
public class JsonDirectoryDownloader extends AsyncTask<String, Void, JsonDirectory> {
    private static final String TAG = JsonDirectoryDownloader.class.getSimpleName();
    private final WeakReference<FileListAdapter> mAdapterReference;

    public JsonDirectoryDownloader(FileListAdapter adapter) {
        mAdapterReference = new WeakReference<>(adapter);
    }

    @Override
    protected JsonDirectory doInBackground(String... params) {
        String url = params[0];
        try {
            URLConnection c = new URL(url).openConnection();
            String username = "guest";
            String password = "guest";
            String userPassword = username + ":" + password;
            String encoding = Base64.encodeToString(userPassword.getBytes(),Base64.NO_WRAP);
            c.setRequestProperty("Authorization", "Basic " + encoding);
            c.setUseCaches(false);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonDirectory fileList = objectMapper.readValue(c.getInputStream(), JsonDirectory.class);
            for (JsonDirectory.JsonFileDir dir : fileList.result) {
                if (!dir.type.equals(JsonDirectory.JsonFileDir.TYPE_DIR)) {
                    dir.memeType = getMimeType(dir.path);
                    //Log.e(TAG,"Name:"+dir.name+" Type:"+dir.memeType);
                }
            }
            //Thread.sleep(1000);
            return fileList;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(JsonDirectory fileList) {
        FileListAdapter adapter = mAdapterReference.get();
        if (adapter != null) {
            adapter.updateFileList(fileList);
        }
        super.onPostExecute(fileList);
    }

    public static String getMimeType(String url) {
        try {
            String type = null;
            String encoded = Uri.encode(url);
            encoded = URLEncoder.encode(encoded, "UTF-8");
            String extension = MimeTypeMap.getFileExtensionFromUrl(encoded);
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            return type;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}


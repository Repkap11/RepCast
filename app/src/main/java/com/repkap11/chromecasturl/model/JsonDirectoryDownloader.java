package com.repkap11.chromecasturl.model;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.repkap11.chromecasturl.FileListAdapter;

import java.lang.ref.WeakReference;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by paul on 9/10/15.
 */
public class JsonDirectoryDownloader extends AsyncTask<String, Void, JsonDirectory> {
    private final WeakReference<FileListAdapter> mAdapterReference;

    public JsonDirectoryDownloader(FileListAdapter adapter) {
        mAdapterReference = new WeakReference<FileListAdapter>(adapter);
    }

    @Override
    protected JsonDirectory doInBackground(String... params) {
        String url = params[0];
        try {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("myuser", "mypass".toCharArray());
                }
            });
            URLConnection c = new URL(url).openConnection();
            String username = "guest";
            String password = "guest";
            String userPassword = username + ":" + password;
            String encoding = Base64.encodeToString(userPassword.getBytes(),Base64.DEFAULT);
            c.setRequestProperty("Authorization", "Basic " + encoding);
            c.setUseCaches(false);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonDirectory fileList = objectMapper.readValue(c.getInputStream(), JsonDirectory.class);

            return fileList;
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(JsonDirectory fileList) {
        FileListAdapter adapter = mAdapterReference.get();
        if (adapter != null) {
            adapter.updataFileList(fileList);
        }
        super.onPostExecute(fileList);
    }
}


package com.repkap11.repcast.model.rest;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.util.IOUtils;
import com.repkap11.repcast.activities.TorrentConfirmationActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by paul on 9/10/15.
 */
public class JsonTorrentUploader extends AsyncTask<String, Void, Integer> {
    private static final String TAG = JsonTorrentUploader.class.getSimpleName();
    private final WeakReference<TorrentConfirmationActivity> mActivityReference;

    public JsonTorrentUploader(TorrentConfirmationActivity activity) {
        mActivityReference = new WeakReference<>(activity);
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    @Override
    protected Integer doInBackground(String... params) {
        String url = params[0];
        String torrentContent = params[1];
        Log.e(TAG, "Url:" + url);
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            String username = "guest";
            String password = "guest";
            String userPassword = username + ":" + password;
            String encoding = Base64.encodeToString(userPassword.getBytes(), Base64.NO_WRAP);
            c.setRequestProperty("Authorization", "Basic " + encoding);
            c.setRequestProperty("repka-repcast-token", Base64.encodeToString("repka-repcast-token".getBytes(), Base64.NO_WRAP));
            c.setUseCaches(false);
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type","application/x-bittorrent");
            c.setDoOutput(true);
            c.getOutputStream().write(torrentContent.getBytes());

            int resultCode = c.getResponseCode();

            String error;
            if (resultCode != 200){
                error = convertStreamToString(c.getErrorStream());
            } else {
                error = "";
            }
            Log.e(TAG, "Returning Result:" + resultCode + " Error:" + error);
            return resultCode;
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 400;
    }

        @Override
    protected void onPostExecute(Integer resultCode) {
        TorrentConfirmationActivity activity = mActivityReference.get();
        if (activity != null) {
            activity.torrentUploadComplete(resultCode);
        }
        super.onPostExecute(resultCode);
    }
}


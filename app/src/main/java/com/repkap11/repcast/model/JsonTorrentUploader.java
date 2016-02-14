package com.repkap11.repcast.model;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.repkap11.repcast.activities.TorrentConfirmationActivity;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
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

    @Override
    protected Integer doInBackground(String... params) {
        String url = params[0];
        //url = "https://repkam09.com/dl/toradd/bWFnbmV0Oj94dD11cm46YnRpaDpiOTQwY2IzZTc1MWYyYTc4NTQ2NGEyNTcwYzA3OTdlNzI3NmNiMzk3JmRuPVNhdHVyZGF5K05pZ2h0K0xpdmUrUzM5RTEwK0ppbW15K0ZhbGxvbi1KdXN0aW4rVGltYmVybGFrZStIRFRWK3gmdHI9dWRwJTNBJTJGJTJGdHJhY2tlci5vcGVuYml0dG9ycmVudC5jb20lM0E4MCZ0cj11ZHAlM0ElMkYlMkZvcGVuLmRlbW9uaWkuY29tJTNBMTMzNyZ0cj11ZHAlM0ElMkYlMkZ0cmFja2VyLmNvcHBlcnN1cmZlci50ayUzQTY5NjkmdHI9dWRwJTNBJTJGJTJGZXhvZHVzLmRlc3luYy5jb20lM0E2OTY5";
        Log.e(TAG, "URL:" + url);
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            String username = "guest";
            String password = "guest";
            String userPassword = username + ":" + password;
            String encoding = Base64.encodeToString(userPassword.getBytes(), Base64.NO_WRAP);
            c.setRequestProperty("Authorization", "Basic " + encoding);
            c.setUseCaches(false);

            //ObjectMapper objectMapper = new ObjectMapper();
            //JsonDirectory fileList = objectMapper.readValue(c.getInputStream(), JsonDirectory.class);
            int resultCode = c.getResponseCode();

            Log.e(TAG,"Returning Result:"+resultCode);
            return resultCode;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
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


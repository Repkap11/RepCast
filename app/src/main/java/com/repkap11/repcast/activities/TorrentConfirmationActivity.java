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

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.repkap11.repcast.R;
import com.repkap11.repcast.model.JsonTorrent;
import com.repkap11.repcast.model.JsonTorrentUploader;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

public class TorrentConfirmationActivity extends BaseActivity {

    private static final String TAG = TorrentConfirmationActivity.class.getSimpleName();
    private TextView mTextViewTitle;
    public static final String EXTRA_TORRENT_RESULT = "com.repkap11.repcast.extra_torrent_result";
    private TextView mTextViewSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent startingIntent = getIntent();
        if (startingIntent == null) {
            Log.e(TAG, "No intent on activity");
            finish();
            return;
        }
        String displayName = null;
        JsonTorrent.JsonTorrentResult torrent = startingIntent.getParcelableExtra(EXTRA_TORRENT_RESULT);
        if (torrent == null) {
            String dataString = startingIntent.getDataString();
            try {
                dataString = URLDecoder.decode(dataString, Charset.defaultCharset().name());
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Default charset not supported");
                finish();
                return;
            }
            String[] split = dataString.split("&");
            for (String string : split) {
                String[] split2 = string.split("=");
                if (split2[0].equals("dn")) {
                    displayName = split2[1];
                }
            }
        } else {
            displayName = torrent.name;
        }


        setContentView(R.layout.activity_torrent_confirmation);
        mTextViewTitle = (TextView) findViewById(R.id.activity_torrent_confirmation_title);
        mTextViewSize = (TextView) findViewById(R.id.activity_torrent_confirmation_size);

        mTextViewTitle.setText(displayName);
        if (torrent != null) {
            mTextViewSize.setText(getResources().getString(R.string.torrent_size_prefix)+torrent.size);
        }
        completeOnCreate(savedInstanceState, false);

    }

    @Override
    protected void doShowContent(Parcelable data) {

    }

    @Override
    protected boolean handleOnBackPressed() {
        return false;
    }

    @Override
    public void setTitleBasedOnFragment() {

    }

    @Override
    protected boolean onQuerySubmit(String query) {
        return false;
    }

    @Override
    protected void onQueryChanged(String newText) {

    }

    private void doStartUpload(String magnetLink) {
        String magnetLink64 = Base64.encodeToString(magnetLink.getBytes(), Base64.NO_WRAP);
        String url = "https://repkam09.com/dl/toradd/" + magnetLink64;
        JsonTorrentUploader uploader = new JsonTorrentUploader(this);
        uploader.execute(url);
    }

    public void torrentUploadComplete(Integer resultCode) {
        Toast.makeText(this, "Result:" + resultCode, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Got Result:" + resultCode);
    }
}

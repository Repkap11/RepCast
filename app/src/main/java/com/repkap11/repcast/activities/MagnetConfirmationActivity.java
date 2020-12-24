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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.repkap11.repcast.R;
import com.repkap11.repcast.model.parcelables.JsonTorrent;
import com.repkap11.repcast.model.rest.JsonMagnetUploader;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

public class MagnetConfirmationActivity extends BaseActivity {

    private static final String TAG = MagnetConfirmationActivity.class.getSimpleName();
    private TextView mTextViewTitle;
    public static final String EXTRA_MAGNET_RESULT = "com.repkap11.repcast.extra_torrent_result";
    private TextView mTextViewSize;
    private String mMagnetLink;

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
        JsonTorrent.JsonTorrentResult torrent = startingIntent.getParcelableExtra(EXTRA_MAGNET_RESULT);
        if (torrent == null) {
            mMagnetLink = startingIntent.getDataString();
            String dataString;
            try {
                dataString = URLDecoder.decode(mMagnetLink, Charset.defaultCharset().name());
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
            mMagnetLink = torrent.magnetLink;
        }


        setContentView(R.layout.activity_torrent_confirmation);
        ((Button) findViewById(R.id.activity_torrent_confirmation_yes)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStartUpload(mMagnetLink);
            }
        });
        ((Button) findViewById(R.id.activity_torrent_confirmation_no)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        mTextViewTitle = (TextView) findViewById(R.id.activity_torrent_confirmation_title);
        mTextViewSize = (TextView) findViewById(R.id.activity_torrent_confirmation_size);

        mTextViewTitle.setText(displayName);
        if (torrent != null) {
            mTextViewSize.setText(getResources().getString(R.string.torrent_size_prefix)+" "+torrent.size);
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
        String url = getString(R.string.endpoint_tor_add) +"/"+ magnetLink64;
        JsonMagnetUploader uploader = new JsonMagnetUploader(this);
        uploader.execute(url);
    }

    public void magnetUploadComplete(Integer resultCode) {
        String message;
        if (resultCode == 200) {
            message = getResources().getString(R.string.torrent_confirmation_upload_succeed);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            message = getResources().getString(R.string.torrent_confirmation_upload_failed);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        Log.i(TAG, "Got Result:" + resultCode);


    }
}

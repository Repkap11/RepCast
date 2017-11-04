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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.repkap11.repcast.R;
import com.repkap11.repcast.model.rest.JsonYouTubeUploader;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

public class YouTubeConfirmationActivity extends BaseActivity {

    private static final String TAG = YouTubeConfirmationActivity.class.getSimpleName();
    private TextView mTextViewTitle;
    private TextView mTextViewSize;
    private String mYouTubeKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent startingIntent = getIntent();
        if (startingIntent == null) {
            Log.e(TAG, "No intent on activity");
            finish();
            return;
        }

        //mYouTubeKey = startingIntent.getDataString();
        String youtubeLink = startingIntent.getExtras().getString(Intent.EXTRA_TEXT);
        String displayName = startingIntent.getExtras().getString(Intent.EXTRA_SUBJECT);
        if (youtubeLink == null){
            finish();
            return;
        }
        String prefix = "Watch \"";
        String posfix = "\" on YouTube";
        if (displayName != null){
            if ( displayName.startsWith(prefix) && displayName.endsWith(posfix)){
                displayName = displayName.substring(prefix.length(), displayName.length() - posfix.length());
            }
        }

//        Bundle bundle = startingIntent.getExtras();
//        if (bundle != null) {
//            for (String key : bundle.keySet()) {
//                Object value = bundle.get(key);
//                Log.e(TAG,"Data:" + String.format("%s %s (%s)", key,
//                        value.toString(), value.getClass().getName()));
//            }
//        }

        try {
            mYouTubeKey = URLDecoder.decode(youtubeLink, Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Default charset not supported");
            finish();
            return;
        }
        if (mYouTubeKey == null) {
            finish();
            return;
        }

        prefix = "https://youtu.be/";
        posfix = "";
        if (mYouTubeKey != null){
            if ( mYouTubeKey.startsWith(prefix) && mYouTubeKey.endsWith(posfix)){
                mYouTubeKey = mYouTubeKey.substring(prefix.length(), mYouTubeKey.length() - posfix.length());
            }
        }

        Log.e(TAG,"Link:"+ mYouTubeKey);


        setContentView(R.layout.activity_torrent_confirmation);
        ((Button) findViewById(R.id.activity_torrent_confirmation_yes)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStartUpload(mYouTubeKey);
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

    private void doStartUpload(String youtubeLink) {
        String url = getString(R.string.endpoint_youtube_add) + youtubeLink;
        JsonYouTubeUploader uploader = new JsonYouTubeUploader(this);
        uploader.execute(url);
    }

    public void youtubeUploadComplete(Integer resultCode) {
        String message;
        if (resultCode == 200) {
            message = getResources().getString(R.string.youtube_confirmation_upload_succeed);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            message = getResources().getString(R.string.youtube_confirmation_upload_failed);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        Log.i(TAG, "Got Result:" + resultCode);


    }
}

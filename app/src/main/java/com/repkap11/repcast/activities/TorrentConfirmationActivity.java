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
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.IOUtils;
import com.repkap11.repcast.R;
import com.repkap11.repcast.model.parcelables.JsonTorrent;
import com.repkap11.repcast.model.rest.JsonMagnetUploader;
import com.repkap11.repcast.model.rest.JsonTorrentUploader;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class TorrentConfirmationActivity extends BaseActivity {

    private static final String TAG = TorrentConfirmationActivity.class.getSimpleName();
    private TextView mTextViewTitle;
    private TextView mTextViewSize;
    private byte[] mTorrentContent;


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

    public String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f%cB", bytes / 1000.0, ci.current());
    }

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
        String size = null;

        Uri uri = startingIntent.getData();
        Log.e(TAG, "onCreate: Uri:"+ uri );

        try {
            InputStream torrentFileContent = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copyStream(torrentFileContent, baos);
            mTorrentContent = baos.toByteArray();
            Log.i(TAG, "onCreate: "+ mTorrentContent.length);
            String sillyTorrentContent = new String(mTorrentContent);
            String[] split = sillyTorrentContent.split(":name")[1].split(":");
            int name_length = Integer.parseInt(split[0]);
            displayName = split[1].substring(0,name_length);

            String[] split2 = sillyTorrentContent.split(":infod6")[1].split(":");
            long sizeL = Long.parseLong(split2[1].substring("lengthi".length(), split2[1].length()-"e4".length()));
            size = humanReadableByteCountSI(sizeL);

        } catch (FileNotFoundException e) {
            Log.e(TAG, "onCreate: failed");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mTorrentContent == null){
            Log.e(TAG, "onCreate: Failed to read torrent file");
            finish();
        }

        setContentView(R.layout.activity_torrent_confirmation);
        ((Button) findViewById(R.id.activity_torrent_confirmation_yes)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStartUpload(mTorrentContent);
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
        if (size != null) {
            mTextViewSize.setText(getResources().getString(R.string.torrent_size_prefix)+" "+size);
        }
        completeOnCreate(savedInstanceState, false);

    }

    @Override
    protected void doShowContent(Parcelable data, int scrollPosition) {

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

    private void doStartUpload(byte[] torrentContent) {
        String strings[] = new String[2];
        Pair<String, byte[]> p = new Pair<>( getString(R.string.endpoint_tor_add), torrentContent);
        JsonTorrentUploader uploader = new JsonTorrentUploader(this);
        uploader.execute(p);
    }

    public void torrentUploadComplete(Integer resultCode) {
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

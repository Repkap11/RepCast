package com.repkap11.chromecasturl.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.repkap11.chromecasturl.R;

/**
 * Created by paul on 9/10/15.
 */
public class SelectFileActivity extends AppCompatActivity {
    private static final String TAG = SelectFileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "Activity Created");
        setContentView(R.layout.activity_selectfile);
    }
}

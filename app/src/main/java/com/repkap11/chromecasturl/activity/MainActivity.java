package com.repkap11.chromecasturl.activity;

import android.os.Bundle;
import android.util.Log;

import com.repkap11.chromecasturl.R;

/**
 * Created by paul on 9/10/15.
 */
public class MainActivity extends CastActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "Main Activity Created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}

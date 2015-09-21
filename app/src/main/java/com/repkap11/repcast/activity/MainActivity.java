package com.repkap11.repcast.activity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.repkap11.repcast.R;
import com.repkap11.repcast.model.JsonDirectory;

/**
 * Created by paul on 9/10/15.
 */
public class MainActivity extends CastActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String CAST_DATA = MainActivity.class.getName() + ".CAST_DATA";
    private JsonDirectory.JsonFileDir mCastData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "Main Activity Created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCastData = getIntent().getParcelableExtra(CAST_DATA);
        Log.e(TAG, "Got cast data:" + mCastData);
    }

    @Override
    protected String getCastMeme() {
        return mCastData.memeType;
    }

    @Override
    protected String getCastURL() {
        String path = Uri.encode(mCastData.path,"//");
        String castPath = "http://repkam09.agrius.feralhosting.com/files/" + path;
        Log.e(TAG, "Casting:" + castPath);
        return castPath;
    }

    @Override
    protected String getVideoTitle() {
        return mCastData.name;
    }
}

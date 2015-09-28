package com.repkap11.repcast.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;

import com.repkap11.repcast.R;

/**
 * Created by paul on 9/10/15.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private MainFragment mContentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "Main Activity Created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContentFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.activity_main_fragment_holder);
        if (mContentFragment == null) {
            Log.e(TAG, "Adapter null");
            mContentFragment = new MainFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.activity_main_fragment_holder, mContentFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Log.e(TAG, "Fragmemt reused");
        }
        mContentFragment.initilize(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mContentFragment.notifyOnStart();
    }

    @Override
    protected void onStop() {
        mContentFragment.notifyOnStop();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mContentFragment.notifyOnCreateOptionsMenu(menu);
    }
}

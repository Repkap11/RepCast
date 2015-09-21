package com.repkap11.repcast.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.repkap11.repcast.R;
import com.repkap11.repcast.model.JsonDirectory;

/**
 * Created by paul on 9/10/15.
 */
public class SelectFileActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {
    private static final String TAG = SelectFileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "Activity Created");
        setContentView(R.layout.activity_selectfile);
        SelectFileFragment frag = (SelectFileFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_selectfile_list_holder);
        if (frag == null) {
            Log.e(TAG, "Adapter null");
            JsonDirectory.JsonFileDir dir = new JsonDirectory.JsonFileDir();
            dir.type = JsonDirectory.JsonFileDir.TYPE_DIR;
            dir.name = "Seedbox";
            dir.path = "IDGAF";
            dir.path64 = "";
            dir.isRoot = true;
            showListUsingDirectory(dir);
        }
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    public void showListUsingDirectory(JsonDirectory.JsonFileDir dir) {
        SelectFileFragment newFragment = new SelectFileFragment();
        newFragment.showListUsingDirectory(dir);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!dir.isRoot) {
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        }
        transaction.replace(R.id.fragment_selectfile_list_holder, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {

        Log.e(TAG, "Back stack count:" + getSupportFragmentManager().getBackStackEntryCount());
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            //This manages the back stack by itself.
            super.onBackPressed();
        }
    }

    public void showFile(JsonDirectory.JsonFileDir dir) {
        Log.e(TAG, "Starting file:" + dir.name);
        Intent intent = new Intent();
        intent.setClass(this,MainActivity.class);
        intent.putExtra(MainActivity.CAST_DATA, dir);
        Log.e(TAG,"About to cast:"+dir.path);
        startActivity(intent);
    }

    @Override
    public void onBackStackChanged() {
        SelectFileFragment fragment = (SelectFileFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_selectfile_list_holder);
        if (fragment != null) {
            String name = fragment.getDirectoryName();
            getSupportActionBar().setTitle(name);
        }
    }
}

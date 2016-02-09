package com.repkap11.repcast.activities;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.fragments.SelectFileFragment;
import com.repkap11.repcast.activities.fragments.SelectTorrentFragment;
import com.repkap11.repcast.model.JsonDirectory;
import com.repkap11.repcast.model.JsonTorrent;

/**
 * Created by paul on 2/7/16.
 */
public class RepcastPageAdapter extends FragmentPagerAdapter {
    private static final String TAG = RepcastPageAdapter.class.getSimpleName();
    private final FragmentManager mFragmentManager;
    private final SelectFileFragment mFileFragment;
    private final SelectTorrentFragment mTorrentFragment;
    private final Context mApplicationContest;
    private JsonDirectory.JsonFileDir mCurrentDir;
    private JsonTorrent.JsonTorrentResult mCurrentTorrent;

    public RepcastPageAdapter(FragmentManager fm, Context applicationContext) {
        super(fm);
        mApplicationContest = applicationContext;

        mFileFragment = new SelectFileFragment();
        mCurrentDir = new JsonDirectory.JsonFileDir();
        mCurrentDir.type = JsonDirectory.JsonFileDir.TYPE_DIR;
        mCurrentDir.name = "Seedbox";
        mCurrentDir.path = "IDGAF";
        mCurrentDir.path64 = "";
        mCurrentDir.isRoot = true;
        mFileFragment.showListUsingDirectory(mCurrentDir);
        //doShowFileContent(mCurrentDir);

        mTorrentFragment = new SelectTorrentFragment();
        mCurrentTorrent = new JsonTorrent.JsonTorrentResult();
        mCurrentTorrent.name = "Add Torrents";
        //mTorrentFragment.searchForTorrentsWithName(mCurrentTorrent.name);

        //doShowTorrentContent(result);

        mFragmentManager = fm;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return mTorrentFragment;
        } else if (position == 1) {
            return mFileFragment;
        }
        Log.e(TAG,"Error invalid number of fragments");
        return null;
    }

    protected void doShowFileContent(JsonDirectory.JsonFileDir dir) {
        mCurrentDir = dir;
        notifyDataSetChanged();

        /*
        mTorrentFragment.showListUsingDirectory(dir);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (!dir.isRoot) {
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        }
        transaction.replace(R.id.activity_fragment_holder, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        */
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mApplicationContest.getResources().getString(R.string.tab_torrent_title);
            case 1:
                return mApplicationContest.getResources().getString(R.string.tab_file_title);
            default:
                return "Un Expected Fragment";
        }
    }

    protected void doShowTorrentContent(JsonTorrent.JsonTorrentResult torrent) {
        mCurrentTorrent = torrent;
        notifyDataSetChanged();
        /*
        newFragment.showListUsingDirectory(dir);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (!dir.isRoot) {
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        }
        transaction.replace(R.id.activity_fragment_holder, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        */
    }

    @Override
    public int getCount() {
        return 2;
    }
}

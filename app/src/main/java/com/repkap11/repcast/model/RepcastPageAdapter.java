package com.repkap11.repcast.model;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.repkap11.repcast.R;
import com.repkap11.repcast.activities.RepcastActivity;
import com.repkap11.repcast.activities.fragments.RepcastFragment;
import com.repkap11.repcast.activities.fragments.SelectFileFragment;
import com.repkap11.repcast.activities.fragments.SelectTorrentFragment;

/**
 * Created by paul on 2/7/16.
 */
public class RepcastPageAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = RepcastPageAdapter.class.getSimpleName();
    private final RepcastFragment[] mFragments = new RepcastFragment[2];
    private final Context mApplicationContest;
    public static final int TORRENT_INDEX = 1;
    public static final int FILE_INDEX = 0;

    public RepcastPageAdapter(FragmentManager fm, RepcastActivity activity, Context applicationContext) {
        super(fm);
        mApplicationContest = applicationContext;

        JsonDirectory.JsonFileDir dir = new JsonDirectory.JsonFileDir();
        dir.type = JsonDirectory.JsonFileDir.TYPE_DIR;
        dir.name = "Seedbox";
        dir.path = "IDGAF";
        dir.path64 = "";
        dir.isRoot = true;
        mFragments[FILE_INDEX] = SelectFileFragment.newInstance(dir);
        activity.addFragmentToABackStack(mFragments[FILE_INDEX]);

        JsonTorrent.JsonTorrentResult torrent = new JsonTorrent.JsonTorrentResult();
        torrent.name = "Colbert";
        mFragments[TORRENT_INDEX] = SelectTorrentFragment.newInstance(torrent);
        activity.addFragmentToABackStack(mFragments[TORRENT_INDEX]);
    }

    public void updateFragment(RepcastFragment mostRecentFrag, int index) {
        mFragments[index] = mostRecentFrag;
        /*
        if (mostRecentFrag instanceof SelectFileFragment) {
            mFragments[FILE_INDEX] = mostRecentFrag;
        } else if (mostRecentFrag instanceof SelectTorrentFragment) {
            mFragments[TORRENT_INDEX] = mostRecentFrag;
        } else {
            Log.e(TAG, "Unexpected fragment type class:" + mostRecentFrag.getClass().getName());
        }
        */
        //mostRecentFrag.doFragmentTransition(mFragmentManager);
        notifyDataSetChanged();
    }

    public RepcastFragment updatePageAtIndex(int fragmentIndex, Parcelable data) {
        RepcastFragment newFragment = null;
        if (fragmentIndex == TORRENT_INDEX) {
            newFragment = SelectTorrentFragment.newInstance((JsonTorrent.JsonTorrentResult) data);
        } else if (fragmentIndex == FILE_INDEX) {
            newFragment = SelectFileFragment.newInstance((JsonDirectory.JsonFileDir) data);
        } else {
            Log.e(TAG, "Unexpected fragment type Index:" + fragmentIndex);
        }
        updateFragment(newFragment, fragmentIndex);
        return newFragment;
    }

    @Override
    public int getItemPosition(Object object) {
        RepcastFragment fragObject = (RepcastFragment) object;
        int result;
        if (fragObject == mFragments[TORRENT_INDEX]) {
            result = TORRENT_INDEX;
        } else if (fragObject == mFragments[FILE_INDEX]) {
            result = FILE_INDEX;
        } else {
            result = POSITION_NONE;
        }
        return result;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case TORRENT_INDEX:
                return mApplicationContest.getResources().getString(R.string.tab_torrent_title);
            case FILE_INDEX:
                return mApplicationContest.getResources().getString(R.string.tab_file_title);
            default:
                return "Unexpected Fragment";
        }
    }

    @Override
    public int getCount() {
        return mFragments.length;
    }
}

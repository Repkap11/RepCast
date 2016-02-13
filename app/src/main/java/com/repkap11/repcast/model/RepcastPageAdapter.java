package com.repkap11.repcast.model;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

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
    private final Parcelable[] mFragmentContent = new Parcelable[2];
    private final Context mApplicationContest;
    public static final int TORRENT_INDEX = 1;
    public static final int FILE_INDEX = 0;

    public RepcastPageAdapter(FragmentManager fm, RepcastActivity activity, Context applicationContext) {
        super(fm);
        mApplicationContest = applicationContext;

        JsonDirectory.JsonFileDir dir = new JsonDirectory.JsonFileDir();
        dir.type = JsonDirectory.JsonFileDir.TYPE_DIR;
        dir.name = activity.getString(R.string.app_name);
        dir.path = "IDGAF";
        dir.path64 = "";
        dir.isRoot = true;
        mFragmentContent[FILE_INDEX] = dir;
        activity.addFragmentToABackStack(mFragmentContent[FILE_INDEX]);

        JsonTorrent.JsonTorrentResult torrent = new JsonTorrent.JsonTorrentResult();
        torrent.name = activity.getString(R.string.add_torrent_initial_title);
        mFragmentContent[TORRENT_INDEX] = torrent;
        activity.addFragmentToABackStack(mFragmentContent[TORRENT_INDEX]);
    }

    public RepcastPageAdapter(FragmentManager fm, Context applicationContext, JsonDirectory.JsonFileDir dir, JsonTorrent.JsonTorrentResult torrent) {
        super(fm);
        mApplicationContest = applicationContext;
        mFragmentContent[FILE_INDEX] = dir;
        mFragmentContent[TORRENT_INDEX] = torrent;
    }

    public void updateFragment(Parcelable mostRecentFragContent, int index) {
        mFragmentContent[index] = mostRecentFragContent;
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

    public void updatePageAtIndex(int fragmentIndex, Parcelable data) {
        /*
        RepcastFragment newFragment = null;
        if (fragmentIndex == TORRENT_INDEX) {
            newFragment = SelectTorrentFragment.newInstance((JsonTorrent.JsonTorrentResult) data);
        } else if (fragmentIndex == FILE_INDEX) {
            newFragment = SelectFileFragment.newInstance((JsonDirectory.JsonFileDir) data);
        } else {
            Log.e(TAG, "Unexpected fragment type Index:" + fragmentIndex);
        }
        */
        updateFragment(data, fragmentIndex);
    }

    @Override
    public int getItemPosition(Object object) {
        RepcastFragment fragObject = (RepcastFragment) object;
        int result;
        if (fragObject.getParceable() == mFragmentContent[TORRENT_INDEX]) {
            result = TORRENT_INDEX;
        } else if (fragObject.getParceable() == mFragmentContent[FILE_INDEX]) {
            result = FILE_INDEX;
        } else {
            result = POSITION_NONE;
        }
        Log.d(TAG, "getItemPosition() called with: " + object.getClass().getSimpleName() + " result:" + result);
        return result;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem() called with: " + "position = [" + position + "]");
        if (position == FILE_INDEX) {
            return SelectFileFragment.newInstance((JsonDirectory.JsonFileDir) mFragmentContent[FILE_INDEX]);
        } else if (position == TORRENT_INDEX) {
            return SelectTorrentFragment.newInstance((JsonTorrent.JsonTorrentResult) mFragmentContent[TORRENT_INDEX]);
        }
        return null;
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
        return mFragmentContent.length;
    }

    SparseArray<RepcastFragment> registeredFragments = new SparseArray<>();

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        RepcastFragment fragment = (RepcastFragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public RepcastFragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}

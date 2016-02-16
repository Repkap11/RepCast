package com.repkap11.repcast.fragments;

import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.repkap11.repcast.R;


public abstract class RepcastFragment extends ListFragment {

    private static final String TAG = RepcastFragment.class.getSimpleName();
    protected static final boolean DO_SAVE_STATE = true;
    protected static final boolean DO_RETAIN_INSTANCE = true;
    private TextView mEmptyMessage;
    private ProgressBar mProgress;
    private ViewSwitcher mSwitcherProgressEmpty;
    private boolean mShowProgress;
    private String mResultEmptyString;

    public RepcastFragment() {

    }
    public abstract String getName();

    public abstract boolean onQuerySubmit(String query);

    public abstract boolean onQueryChange(String newText);

    protected static void assertThat(boolean b) {
        if (!b){
            throw new RuntimeException("Assertion Failed");
        }
    }

    public abstract Parcelable getParceable();


    protected void initProgressAndEmptyMessage(View rootView) {
        mSwitcherProgressEmpty = (ViewSwitcher) rootView.findViewById(R.id.fragment_repcast_list_switcher);
        mEmptyMessage = (TextView) rootView.findViewById(R.id.fragment_repcast_list_empty_message);
        mProgress = (ProgressBar) rootView.findViewById(R.id.fragment_repcast_list_progress);
        setShouldProgressBeShown(mShowProgress);
    }

    public String getResultEmptyString() {
        return mResultEmptyString;
    }

    protected void setResultsEmptyString(String string) {
        mResultEmptyString = string;
        if (mEmptyMessage != null) {
            if (mResultEmptyString == null) {
                if (this instanceof SelectTorrentFragment) {
                    mEmptyMessage.setText(R.string.empty_message_files);
                } else {
                    mEmptyMessage.setText(R.string.empty_message_torents);
                }
            } else {
                mEmptyMessage.setText(mEmptyMessage.getResources().getString(R.string.no_results_for_prefix) + mResultEmptyString);
            }
        }

    }

    public void setShouldProgressBeShown(boolean showProgress) {
        mShowProgress = showProgress;
        if (mSwitcherProgressEmpty != null) {
            mSwitcherProgressEmpty.setDisplayedChild(mShowProgress ? 1 : 0);
        }
        setResultsEmptyString(mResultEmptyString);
    }

    @Override
    public void setListShown(boolean shown) {
        if (!shown) {
            setShouldProgressBeShown(mShowProgress);
        }
        super.setListShown(shown);
    }

    @Override
    public void onDestroyView() {
        mProgress = null;
        mSwitcherProgressEmpty = null;
        mEmptyMessage = null;
        super.onDestroyView();
    }
}

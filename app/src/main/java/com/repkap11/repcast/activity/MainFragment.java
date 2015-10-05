package com.repkap11.repcast.activity;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.cast.MediaStatus;
import com.repkap11.repcast.R;
import com.repkap11.repcast.model.JsonDirectory;

/**
 * Created by paul on 9/27/15.
 */
public class MainFragment extends CastFragment {
    private static final String TAG = MainFragment.class.getSimpleName();
    private JsonDirectory.JsonFileDir mCastData;
    protected static final String APPLICATION_ID = "CB2D44C5";
    public static final String CAST_DATA = MainFragment.class.getName() + ".CAST_DATA";
    private ImageView mPauseResumeOverlay;
    private boolean mIsPlaying = false;

    public MainFragment() {
    }

    public void initilize(Intent intent) {
        mCastData = intent.getParcelableExtra(CAST_DATA);
        Log.e(TAG, "Got cast data:" + mCastData);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    protected void onSeekStateChanged(long position) {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        mPauseResumeOverlay = (ImageView) root.findViewById(R.id.activity_main_pause_resume_overlay);
        if (mIsPlaying) {
            mPauseResumeOverlay.setImageResource(R.drawable.ic_av_pause_light);
        } else {
            mPauseResumeOverlay.setImageResource(R.drawable.ic_av_play_light);
        }
        mPauseResumeOverlay.
                setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           MainActivity activity = (MainActivity) getActivity();
                                           Log.e(TAG, "Pause Reusme Clicked");
                                           if (mIsPlaying) {
                                               pauseMediaPlayer();
                                           } else {
                                               resumeMediaPlayer();
                                           }
                                       }
                                   }

                );
        return root;
    }

    public void onPlayerStatusChanged(int status) {
        if (status == MediaStatus.PLAYER_STATE_PAUSED) {
            mIsPlaying = false;
            mPauseResumeOverlay.setImageResource(R.drawable.ic_av_play_light);
        } else {
            mIsPlaying = true;
            mPauseResumeOverlay.setImageResource(R.drawable.ic_av_pause_light);
        }
    }

    @Override
    protected String getCastMeme() {
        return mCastData.memeType;
    }

    @Override
    protected String getCastURL() {
        String path = Uri.encode(mCastData.path, "//");
        String castPath = "http://repkam09.agrius.feralhosting.com/files/" + path;
        Log.e(TAG, "Casting:" + castPath);
        return castPath;
    }

    @Override
    protected String getVideoTitle() {
        return mCastData.name;
    }
}

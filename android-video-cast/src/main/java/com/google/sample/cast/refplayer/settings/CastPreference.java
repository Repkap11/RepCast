/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.google.sample.cast.refplayer.settings;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.sample.cast.refplayer.R;
import com.google.sample.cast.refplayer.utils.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class CastPreference extends PreferenceActivity
        implements OnSharedPreferenceChangeListener {

    public static final String FTU_SHOWN_KEY = "ftu_shown";
    public static final String VOLUME_SELECTION_KEY = "volume_target";
    private ListPreference mVolumeListPreference;
    private VideoCastManager mCastManager;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.application_preference);
        getPreferenceScreen().getSharedPreferences().
                registerOnSharedPreferenceChangeListener(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mCastManager = VideoCastManager.getInstance();

        /* Volume settings */
        mVolumeListPreference = (ListPreference) getPreferenceScreen()
                .findPreference(VOLUME_SELECTION_KEY);
        String volValue = prefs.getString(
                VOLUME_SELECTION_KEY, getString(R.string.prefs_volume_default));
        String volSummary = getResources().getString(R.string.prefs_volume_title_summary, volValue);
        mVolumeListPreference.setSummary(volSummary);

        EditTextPreference versionPref = (EditTextPreference) findPreference("app_version");
        versionPref.setTitle(getString(R.string.version, Utils.getAppVersionName(this),
                getString(R.string.ccl_version)));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (VOLUME_SELECTION_KEY.equals(key)) {
            String value = sharedPreferences.getString(VOLUME_SELECTION_KEY, "");
            String summary = getResources().getString(R.string.prefs_volume_title_summary, value);
            mVolumeListPreference.setSummary(summary);
        }
    }

    public static boolean isFtuShown(Context ctx) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getBoolean(FTU_SHOWN_KEY, false);
    }

    public static void setFtuShown(Context ctx) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        sharedPref.edit().putBoolean(FTU_SHOWN_KEY, true).apply();
    }

    @Override
    protected void onResume() {
        if (null != mCastManager) {
            mCastManager.incrementUiCounter();
            mCastManager.updateCaptionSummary("caption", getPreferenceScreen());
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (null != mCastManager) {
            mCastManager.decrementUiCounter();
        }
        super.onPause();
    }

}

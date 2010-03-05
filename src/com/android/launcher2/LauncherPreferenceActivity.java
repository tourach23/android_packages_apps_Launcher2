/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.launcher2;

import com.android.launcher2.R;

import android.appwidget.AppWidgetHost;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;

/**
 * With this activity, users can set preferences for Launcher2 Mod
 */
public class LauncherPreferenceActivity extends PreferenceActivity {
    // Symbolic names for the keys used for preference lookup
    public static final String LAUNCHER2_LONGPRESS_ADD = "pref_key_launcher2_longpress_add";
	public static final String LAUNCHER2_SCREEN_SIZE = "pref_key_launcher2_screen_size";

    // Menu entries
    private static final int MENU_RESTORE_DEFAULTS    = 1;

	private Preference mScreenSize;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);

		mScreenSize = findPreference("pref_key_launcher2_screen_size");
		setScreenSizeDisplay();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        menu.add(0, MENU_RESTORE_DEFAULTS, 0, R.string.restore_default);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESTORE_DEFAULTS:
                restoreDefaultPreferences();
                return true;
        }
        return false;
    }

    private void restoreDefaultPreferences() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().clear().commit();
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.preferences);
    }

	private int getScreenSize() {
		SharedPreferences mPrefs = mScreenSize.getSharedPreferences();
		return mPrefs.getInt(LauncherPreferenceActivity.LAUNCHER2_SCREEN_SIZE, 7);
	}

	private void setScreenSizeDisplay() {
        mScreenSize.setSummary(
                getString(R.string.pref_summary_launcher2_screen_size,
                        getScreenSize()));
    }

	@Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mScreenSize) {
			new NumberPickerDialog(this,
                    mScreenSizeListener,
                    getScreenSize(),
                    1,
                    7,
                    R.string.pref_title_launcher2_screen_size,
					R.string.pref_summary_launcher2_set_screen_size).show();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

	NumberPickerDialog.OnNumberSetListener mScreenSizeListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int size) {
				if (size != getScreenSize()) {
					SharedPreferences.Editor editor = mScreenSize.getEditor();
					editor.putInt(LauncherPreferenceActivity.LAUNCHER2_SCREEN_SIZE, size);
					editor.commit();
				
					Launcher.resetWidgets = true;
				
	                setScreenSizeDisplay();
				}
            }
    };
}
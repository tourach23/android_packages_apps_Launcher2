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

package com.helixproject.launcher2;

import com.helixproject.launcher2.R;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.appwidget.AppWidgetHost;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
    public static final String LAUNCHER_FULLSCREEN = "pref_key_launcher_fullscreen";
	public static final String LAUNCHER_LONGPRESS_ADD = "pref_key_launcher_longpress_add";
	public static final String LAUNCHER_HIDE_LABELS = "pref_key_launcher_hide_labels";
	public static final String LAUNCHER_SHOW_SHORTCUTS_LABEL = "pref_key_launcher_show_shortcuts_label";
	public static final String LAUNCHER_DOUBLE_TAP = "pref_key_launcher_double_tap";
	public static final String LAUNCHER2_SCREEN_SIZE = "pref_key_launcher2_screen_size";
	public static final String LAUNCHER_DEFAULT_SCREEN = "pref_key_launcher_default_screen";
	public static final String LAUNCHER2_AUTO_ORIENTATION = "pref_key_launcher2_auto_orientation";
	public static final String LAUNCHER2_QUICK_SHORTCUTS = "pref_key_launcher2_quick_shortcuts";
	public static final String LAUNCHER2_APP1_PACKAGE = "pref_key_launcher2_app1_package";
	public static final String LAUNCHER2_APP1_CLASS = "pref_key_launcher2_app1_class";
	public static final String LAUNCHER2_APP1_URI = "pref_key_launcher2_app1_uri";
	public static final String LAUNCHER2_APP2_PACKAGE = "pref_key_launcher2_app2_package";
	public static final String LAUNCHER2_APP2_CLASS = "pref_key_launcher2_app2_class";
	public static final String LAUNCHER2_APP2_URI = "pref_key_launcher2_app2_uri";
	public static final String LAUNCHER2_APP3_PACKAGE = "pref_key_launcher2_app3_package";
	public static final String LAUNCHER2_APP3_CLASS = "pref_key_launcher2_app3_class";
	public static final String LAUNCHER2_APP3_URI = "pref_key_launcher2_app3_uri";
	public static final String LAUNCHER2_APP4_PACKAGE = "pref_key_launcher2_app4_package";
	public static final String LAUNCHER2_APP4_CLASS = "pref_key_launcher2_app4_class";
	public static final String LAUNCHER2_APP4_URI = "pref_key_launcher2_app4_uri";
	public static final String LAUNCHER2_RESTART = "pref_key_launcher2_restart";

    // Menu entries
    private static final int MENU_RESTORE_DEFAULTS    = 1;

	private Preference mScreenSize;
	private Preference mDefaultScreen;
	private Preference mRestart;
	private Preference mQuickShortcuts;
	private Preference mHideLabels;
	private Preference mShowShortcuts;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);

		mScreenSize = findPreference(LAUNCHER2_SCREEN_SIZE);
		mRestart = findPreference(LAUNCHER2_RESTART);
		mDefaultScreen = findPreference(LAUNCHER_DEFAULT_SCREEN);
		mQuickShortcuts = findPreference(LAUNCHER2_QUICK_SHORTCUTS);
		mHideLabels = findPreference(LAUNCHER_HIDE_LABELS);
		mShowShortcuts = findPreference(LAUNCHER_SHOW_SHORTCUTS_LABEL);
		setScreenSizeDisplay();
		setDefaultScreenDisplay();
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

	private int getDefaultScreen() {
		SharedPreferences mPrefs = mScreenSize.getSharedPreferences();
		return mPrefs.getInt(LauncherPreferenceActivity.LAUNCHER_DEFAULT_SCREEN, 2);
	}

	private void setDefaultScreenDisplay() {
        mDefaultScreen.setSummary(
                getString(R.string.pref_summary_launcher_default_screen,
                        getDefaultScreen()));
    }

	private void askRestart() {
		new AlertDialog.Builder(this)
			  .setTitle("Confirm")
		      .setMessage("Restart Launcher2?")
		      .setPositiveButton("Yes", restartLauncher2)
			  .setNegativeButton("No", cancelRestart)
		      .show();
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
			new AlertDialog.Builder(this)
				  .setTitle("WARNING")
			      .setMessage("Your default screen will be centered. If you're shrinking the size, your workspace would automatically be adjusted. Press the back button to continue.")
			      .show();
		} else if (preference == mDefaultScreen) {
			new NumberPickerDialog(this,
                    mDefaultScreenListener,
                    getDefaultScreen(),
                    1,
                    getScreenSize(),
                    R.string.pref_title_launcher_default_screen,
					R.string.pref_summary_launcher_set_default_screen).show();
		} else if (preference == mHideLabels) {
			askRestart();
		} else if (preference == mShowShortcuts) {
			askRestart();
		} else if (preference == mQuickShortcuts) {
			askRestart();
        } else if (preference == mRestart) {
			askRestart();
		}

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

	DialogInterface.OnClickListener restartLauncher2 =
		new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				ActivityManager actmgr = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE); 
				actmgr.restartPackage("com.helixproject.launcher2");
			}
	};
	
	DialogInterface.OnClickListener cancelRestart =
		new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
	};

	NumberPickerDialog.OnNumberSetListener mScreenSizeListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int size) {
				if (size != getScreenSize()) {
					updateDb(size);
		
					SharedPreferences.Editor editor = mScreenSize.getEditor();
					editor.putInt(LauncherPreferenceActivity.LAUNCHER2_SCREEN_SIZE, size);
					editor.putInt(LauncherPreferenceActivity.LAUNCHER_DEFAULT_SCREEN, ((size+1)/2));
					editor.commit();
				
	                setScreenSizeDisplay();
	
					askRestart();
				}
            }
    };

	NumberPickerDialog.OnNumberSetListener mDefaultScreenListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int screen) {
				if (screen != getDefaultScreen()) {
					SharedPreferences.Editor editor = mDefaultScreen.getEditor();
					editor.putInt(LauncherPreferenceActivity.LAUNCHER_DEFAULT_SCREEN, screen);
					editor.commit();
				
	                setDefaultScreenDisplay();
	
					askRestart();
				}
            }
    };

	private void updateDb(int size) {
		HelixDBHelper helper = new HelixDBHelper(getApplicationContext());
		SQLiteDatabase db = helper.getWritableDatabase();
		if (size > getScreenSize()) {
			db.execSQL("UPDATE favorites SET screen = screen+"+((size-getScreenSize())/2));
		} else if (size < getScreenSize()) {
			db.execSQL("UPDATE favorites SET screen = screen-"+((getScreenSize()-size)/2));
			db.execSQL("DELETE FROM favorites WHERE screen < 1");
			db.execSQL("DELETE FROM favorites WHERE screen >= "+(size+1));
		}
       	db.close();
	}
}
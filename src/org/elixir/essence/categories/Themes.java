/*
 * Copyright (C) 2014-2016 The Dirty Unicorns Project
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

package org.elixir.essence.categories;

import android.content.Context;
import android.content.ContentResolver;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import static android.os.UserHandle.USER_SYSTEM;
import static android.os.UserHandle.USER_CURRENT;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import android.graphics.Color;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import com.android.internal.util.custom.customUtils;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import org.json.JSONException;
import org.json.JSONObject;

import static android.provider.Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK;
import static android.provider.Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_STYLES;
import static android.provider.Settings.Secure.ELIXIR_EXCLUSIVE_BUILD;

public class Themes extends SettingsPreferenceFragment 
	implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "Themes";
    private static final String KEY_CUSTOM_CLOCK = "lock_screen_custom_clock";
    private static final String KEY_CUSTOM_CLOCK_STYLE = "ls_clock_styles";
    private static final String KEY_EXCLUSIVE_CAT = "exclusive_clock";
    private static final String HUD_CLOCK = "com.android.systemui.lsclock.hud";
    private Context mContext;
    private SwitchPreference mCustomClock;
    private ListPreference mClockStyle;
    private ContentResolver resolver;
    private IOverlayManager mOverlayService;
    private PreferenceCategory mExclusiveBuild;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.themes);

        mContext = getActivity();

        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        resolver = getActivity().getContentResolver();
        final PreferenceScreen screen = getPreferenceScreen();

        final Boolean isExclusive = Settings.Secure.getInt(resolver, ELIXIR_EXCLUSIVE_BUILD, 0) != 0;
        Log.w("Essence", "Status of exclusive :- " + isExclusive.toString());
        if (isExclusive) {
            mCustomClock = (SwitchPreference) findPreference(KEY_CUSTOM_CLOCK);
            if (mCustomClock != null) {
                mCustomClock.setChecked(Settings.Secure.getIntForUser(resolver, LOCK_SCREEN_CUSTOM_CLOCK, 0, UserHandle.USER_CURRENT) != 0);
                mCustomClock.setOnPreferenceChangeListener(this);
            }
    
            mClockStyle = (ListPreference) findPreference(KEY_CUSTOM_CLOCK_STYLE);
            if (mClockStyle != null) {
                mClockStyle.setValue(String.valueOf(Settings.Secure.getInt(resolver, LOCK_SCREEN_CUSTOM_CLOCK_STYLES, 0)));
                mClockStyle.setSummary(mClockStyle.getEntry());
                mClockStyle.setOnPreferenceChangeListener(this);
            }
        } else {
            mExclusiveBuild = (PreferenceCategory) findPreference(KEY_EXCLUSIVE_CAT);
            screen.removePreference(mExclusiveBuild);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCustomClock) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(resolver, LOCK_SCREEN_CUSTOM_CLOCK, value ? 1 : 0);
            customUtils.showSystemUiRestartDialog(getActivity());
            return true;
        } else if (preference == mClockStyle) {
            mClockStyle.setValue((String) newValue);
            mClockStyle.setSummary(mClockStyle.getEntry());
            Settings.Secure.putInt(resolver, LOCK_SCREEN_CUSTOM_CLOCK_STYLES, Integer.parseInt((String) newValue));
            int current = Settings.Secure.getInt(resolver, LOCK_SCREEN_CUSTOM_CLOCK_STYLES, 0);
            if (current == 0) {
                if (isOverlayEnabled(HUD_CLOCK)) {
                    RROManager(HUD_CLOCK, false);
                }
            } else if (current == 1) {
                if (isOverlayEnabled(HUD_CLOCK)) {
                    // Already enabled ???
                }
                else {
                    RROManager(HUD_CLOCK, true);
                }
            }
            return true;
        }
        return false;
    }

    public void RROManager(String name, boolean status) {
        if (status) {
            Log.d(TAG, "Enabling Overlay Package :- " + name);
        }
        else {
            Log.d(TAG, "Disabling Overlay Package :- " + name);
        }
        try {
            mOverlayService.setEnabled(name, status, UserHandle.USER_CURRENT);
          } catch (RemoteException re) {
                Log.e(TAG, String.valueOf(re));
        }
    }

    public boolean isOverlayEnabled(String name) {
        OverlayInfo info = null;
        try {
            info = mOverlayService.getOverlayInfo(name, UserHandle.USER_CURRENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info != null && info.isEnabled();
    }
}

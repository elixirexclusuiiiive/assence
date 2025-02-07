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
import android.os.Bundle;
import android.os.ServiceManager;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import android.os.UserHandle;
import android.os.Handler;
import android.content.res.Resources;

import com.android.internal.util.custom.customUtils;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class StatusBar extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "StatusBar";

    private ListPreference mQuickPulldown;

    private static final String PREF_STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String PREF_STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String PREF_COMBINED_SIGNAL_ICON = "combined_signal_icon";
    private static final String COMBINED_OVERLAY_PACKAGE = "com.android.systemui.combined.signal";
    private static final int BATTERY_STYLE_PORTRAIT = 0;
    private static final int BATTERY_STYLE_TEXT = 4;
    private static final int BATTERY_STYLE_HIDDEN = 5;
    private static final int BATTERY_PERCENT_HIDDEN = 0;
    private ListPreference mBatteryPercent;
    private ListPreference mBatteryStyle;
    private SwitchPreference mCombinedSignal;
    private int mBatteryPercentValue;
    private int mBatteryPercentValuePrev;
    private IOverlayManager mOverlayService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar);

        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        ContentResolver resolver = getActivity().getContentResolver();
        int qpmode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 0, UserHandle.USER_CURRENT);
        mQuickPulldown = (ListPreference) findPreference("qs_quick_pulldown");
        mQuickPulldown.setValue(String.valueOf(qpmode));
        mQuickPulldown.setSummary(mQuickPulldown.getEntry());
        mQuickPulldown.setOnPreferenceChangeListener(this);

        int batterystyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_PORTRAIT, UserHandle.USER_CURRENT);

        mBatteryStyle = (ListPreference) findPreference(PREF_STATUS_BAR_BATTERY_STYLE);
        mBatteryStyle.setValue(String.valueOf(batterystyle));
        mBatteryStyle.setSummary(mBatteryStyle.getEntry());
        mBatteryStyle.setOnPreferenceChangeListener(this);

        mBatteryPercentValue = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, BATTERY_PERCENT_HIDDEN, UserHandle.USER_CURRENT);

        mBatteryPercent = (ListPreference) findPreference(PREF_STATUS_BAR_SHOW_BATTERY_PERCENT);
        mBatteryPercent.setValue(String.valueOf(mBatteryPercentValue));
        mBatteryPercent.setSummary(mBatteryPercent.getEntry());
        mBatteryPercent.setOnPreferenceChangeListener(this);
        mBatteryPercent.setEnabled(
            batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN);

        mCombinedSignal = (SwitchPreference) findPreference(PREF_COMBINED_SIGNAL_ICON);
        if (mCombinedSignal != null) {
            mCombinedSignal.setChecked(isOverlayEnabled(COMBINED_OVERLAY_PACKAGE));
            mCombinedSignal.setOnPreferenceChangeListener(this);
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

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        final String key = preference.getKey();
        if (preference == mQuickPulldown) {
            int value = Integer.parseInt((String) objValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, value,
                    UserHandle.USER_CURRENT);
            int index = mQuickPulldown.findIndexOfValue((String) objValue);
            mQuickPulldown.setSummary(
                    mQuickPulldown.getEntries()[index]);
            return true;
        } else if (preference == mBatteryStyle) {
            int batterystyle = Integer.parseInt((String) objValue);
            Settings.System.putIntForUser(resolver,
            Settings.System.STATUS_BAR_BATTERY_STYLE, batterystyle,
            UserHandle.USER_CURRENT);            int index = mBatteryStyle.findIndexOfValue((String) objValue);
            mBatteryStyle.setSummary(mBatteryStyle.getEntries()[index]);
            mBatteryPercent.setEnabled(
            batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN);
            return true;
        } else if (preference == mBatteryPercent) {
            mBatteryPercentValue = Integer.parseInt((String) objValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, mBatteryPercentValue,
                    UserHandle.USER_CURRENT);
            int index = mBatteryPercent.findIndexOfValue((String) objValue);
            mBatteryPercent.setSummary(mBatteryPercent.getEntries()[index]);
            return true;
        } else if (preference == mCombinedSignal) {
            boolean value = (Boolean) objValue;
            RROManager(COMBINED_OVERLAY_PACKAGE, value);
            mCombinedSignal.setChecked(value);
            customUtils.showSystemUiRestartDialog(getActivity());
        }
        return false;
    }

    private void handleTextPercentage(int batterypercent) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (mBatteryPercentValuePrev == -1) {
            mBatteryPercentValuePrev = mBatteryPercentValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT + "_prev",
                    mBatteryPercentValue, UserHandle.USER_CURRENT);
        }
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT,
                batterypercent, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_TEXT,
                UserHandle.USER_CURRENT);
        int index = mBatteryPercent.findIndexOfValue(String.valueOf(batterypercent));
        mBatteryPercent.setSummary(mBatteryPercent.getEntries()[index]);
        mBatteryPercent.setEnabled(false);
    }

    private void RROManager(String name, boolean status) {
        if (status) {
            Log.d(TAG, "Enabling Overlay Package :- " + name);
        }
        else {
            Log.d(TAG, "Disabling Overlay Package :- " + name);
        }
        try {
            mOverlayService.setEnabled(name, status, UserHandle.USER_CURRENT);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Overlay " + name + " doesn't exists");
        } catch (Exception re) {
            Log.e(TAG, String.valueOf(re));
        }
    }

    private boolean isOverlayEnabled(String name) {
        OverlayInfo info = null;
        Log.i(TAG, "Getting information of overlay :- " + name);
        try {
            info = mOverlayService.getOverlayInfo(name, UserHandle.USER_CURRENT);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Overlay " + name + " doesn't exists");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info != null && info.isEnabled();
    }
}

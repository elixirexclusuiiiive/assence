/*
 * Copyright (C) 2016 AospExtended ROM Project
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

import android.content.ContentResolver;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.app.ActivityManagerNative;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import android.content.Context;
import android.provider.Settings;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.RemoteException;
import android.util.Log;
import com.android.settings.Utils;

import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;

import androidx.preference.SwitchPreference;
import org.elixir.essence.Essence;
import static android.provider.Settings.Secure.HIDE_ESSENCE_ICONS;

public class Misc extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "Misc";
    private static final String KEY_HIDE_ICONS = "hide_essence_icons";
    private SwitchPreference mHideIcons;
    private boolean enabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.misc);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();
        final PackageManager mPm = getActivity().getPackageManager();

        mHideIcons = (SwitchPreference) findPreference(KEY_HIDE_ICONS);
        enabled = Settings.Secure.getInt(resolver, HIDE_ESSENCE_ICONS, 0) == 1;
        if (enabled) {
            mHideIcons.setChecked(true);
        }
        mHideIcons.setOnPreferenceChangeListener(this);
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
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (preference == mHideIcons) {
            boolean state = Boolean.valueOf(objValue.toString());
            int put = (state) ? 1 : 0;
            Settings.Secure.putInt(getActivity().getContentResolver(), HIDE_ESSENCE_ICONS, put);
            mHideIcons.setChecked(state);
        }
        return false;
    }
}

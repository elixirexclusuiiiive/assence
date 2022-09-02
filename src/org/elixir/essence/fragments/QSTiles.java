/*
 * Copyright (C) 2022 Project Elixir
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

package org.elixir.essence.fragments;

import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.view.View;
import android.view.WindowManagerGlobal;

import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import static android.provider.Settings.Secure.QS_TILE_STYLE;
import static android.provider.Settings.Secure.BRIGHTNESS_SLIDER_STYLE;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class QSTiles extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "QSTiles";
    private static final String KEY_QSTILES_STYLES = "qstiles_styles";
    private static final String CLASSIC_OVERLAY = "com.android.systemui.qstiles.classic";
    private static final String OUTLINE_OVERLAY = "com.android.theme.icon.outlineshapes";
    private static final String MAYBEREC_OVERLAY = "com.android.elixir.maybe.rectangle";
    private static final String KEY_BRIGHTNESS_STYLE = "brightness_styles";
    private static final String KEY_BRIGHTNESS_OUTLINE = "com.android.systemui.brightness.outline";

    private ListPreference mQSTilesStyles;
    private IOverlayManager mOverlayService;
    private ListPreference mBrightnessStyles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.qstiles);
        
        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

        mQSTilesStyles = (ListPreference) findPreference(KEY_QSTILES_STYLES);
        if (mQSTilesStyles != null) {
            mQSTilesStyles.setValue(String.valueOf(Settings.Secure.getInt(resolver, QS_TILE_STYLE, 0)));
            mQSTilesStyles.setSummary(mQSTilesStyles.getEntry());
            mQSTilesStyles.setOnPreferenceChangeListener(this);
        }

        mBrightnessStyles = (ListPreference) findPreference(KEY_BRIGHTNESS_STYLE);
        if (mBrightnessStyles != null) {
            mBrightnessStyles.setValue(String.valueOf(Settings.Secure.getInt(resolver, BRIGHTNESS_SLIDER_STYLE, 0)));
            mBrightnessStyles.setSummary(mBrightnessStyles.getEntry());
            mBrightnessStyles.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQSTilesStyles) {
            mQSTilesStyles.setValue((String) newValue);
            mQSTilesStyles.setSummary(mQSTilesStyles.getEntry());
            Settings.Secure.putInt(getActivity().getContentResolver(), QS_TILE_STYLE, Integer.parseInt((String) newValue));
            int current = Settings.Secure.getInt(getActivity().getContentResolver(), QS_TILE_STYLE, 0);
            if (current == 0) {
                if (isOverlayEnabled(CLASSIC_OVERLAY)) {
                    RROManager(CLASSIC_OVERLAY, false);
                }
                if (isOverlayEnabled(OUTLINE_OVERLAY)) {
                    RROManager(OUTLINE_OVERLAY, false);
                }
                if (isOverlayEnabled(MAYBEREC_OVERLAY)) {
                    RROManager(MAYBEREC_OVERLAY, false);
                }
            } else if (current == 1) {
                if (isOverlayEnabled(OUTLINE_OVERLAY)) {
                    RROManager(OUTLINE_OVERLAY, false);
                }
                if (isOverlayEnabled(MAYBEREC_OVERLAY)) {
                    RROManager(MAYBEREC_OVERLAY, false);
                }
                if (isOverlayEnabled(CLASSIC_OVERLAY)) {
                    Log.e(TAG, "Classic QSTiles is already enabled ?");
                }
                else {
                    RROManager(CLASSIC_OVERLAY, true);
                }
            } else if (current == 2) {
                if (isOverlayEnabled(CLASSIC_OVERLAY)) {
                    RROManager(CLASSIC_OVERLAY, false);
                }
                if (isOverlayEnabled(MAYBEREC_OVERLAY)) {
                    RROManager(MAYBEREC_OVERLAY, false);
                }
                if (isOverlayEnabled(OUTLINE_OVERLAY)) {
                    Log.e(TAG, "Outline QSTiles is already enabled ?");
                }
                else {
                    RROManager(OUTLINE_OVERLAY, true);
                }
            } else if (current == 3) {
                if (isOverlayEnabled(CLASSIC_OVERLAY)) {
                    RROManager(CLASSIC_OVERLAY, false);
                }
                if (isOverlayEnabled(OUTLINE_OVERLAY)) {
                    RROManager(OUTLINE_OVERLAY, false);
                }
                if (isOverlayEnabled(MAYBEREC_OVERLAY)) {
                    Log.e(TAG, "MaybeRectangle QSTiles is already enabled ?");
                }
                else {
                    RROManager(MAYBEREC_OVERLAY, true);
                }
            }
            return true;
        } else if (preference == mBrightnessStyles) {
            mBrightnessStyles.setValue((String) newValue);
            mBrightnessStyles.setSummary(mBrightnessStyles.getEntry());
            Settings.Secure.putInt(getActivity().getContentResolver(), BRIGHTNESS_SLIDER_STYLE, Integer.parseInt((String) newValue));
            int current = Settings.Secure.getInt(getActivity().getContentResolver(), BRIGHTNESS_SLIDER_STYLE, 0);
            if (current == 0) {
                RROManager(KEY_BRIGHTNESS_OUTLINE, false);
            } else if (current == 1) {
                RROManager(KEY_BRIGHTNESS_OUTLINE, true);
            }
            return true;
        }
        return true;
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

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.qstiles);
}

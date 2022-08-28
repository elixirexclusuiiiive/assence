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

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class QSTiles extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "QSTiles";
    private static final String KEY_QSTILES_STYLES = "qstiles_styles";
    private static final String CLASSIC_OVERLAY = "com.android.systemui.qstiles.classic";
    private static final String OUTLINE_OVERLAY = "com.android.theme.icon.outlineshapes";

    private ListPreference mQSTilesStyles;
    private IOverlayManager mOverlayService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.qstiles);
        
        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

        mQSTilesStyles = (ListPreference) findPreference(KEY_QSTILES_STYLES);
        mQSTilesStyles.setValue(String.valueOf(Settings.Secure.getInt(resolver, QS_TILE_STYLE, 0)));
        mQSTilesStyles.setSummary(mQSTilesStyles.getEntry());
        mQSTilesStyles.setOnPreferenceChangeListener(this);
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
                RROManager(CLASSIC_OVERLAY, false);
                RROManager(OUTLINE_OVERLAY, false);
            } else if (current == 1) {
                RROManager(OUTLINE_OVERLAY, false);
                RROManager(CLASSIC_OVERLAY, true);
            } else if (current == 2) {
                RROManager(CLASSIC_OVERLAY, false);
                RROManager(OUTLINE_OVERLAY, true);
            }
        }
        return true;
    }

    public void RROManager(String name, boolean  status) {
        Log.w(TAG, name);
        Log.w(TAG, String.valueOf(status));
        try {
            mOverlayService.setEnabled(name, status, UserHandle.USER_CURRENT);
          } catch (RemoteException re) {
                Log.e(TAG, String.valueOf(re));
        }
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.qstiles);
}

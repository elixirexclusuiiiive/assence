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
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import org.elixir.essence.preferences.SystemSettingListPreference;

import static android.provider.Settings.Secure.BRIGHTNESS_SLIDER_STYLE;
import static android.os.UserHandle.USER_SYSTEM;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class QSTiles extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "QSTiles";
    private static final String KEY_QSTILES_STYLES = "qs_panel_style";
    private static final String KEY_BRIGHTNESS_STYLE = "brightness_styles";

    public static final String[] QS_STYLES = {
        "com.android.system.qs.outline",
        "com.android.system.qs.twotoneaccent",
        "com.android.system.qs.shaded",
        "com.android.system.qs.cyberpunk",
        "com.android.systemui.qstiles.classic",
        "com.android.elixir.maybe.rectangle"
    };

    private Handler mHandler;
    private SystemSettingListPreference mQSTilesStyles;
    private IOverlayManager mOverlayService;
    private ListPreference mBrightnessStyles;
    private String OLD_SLIDER = "DEFAULT";
    private ContentResolver resolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.qstiles);
        
        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

        mQSTilesStyles = (SystemSettingListPreference) findPreference(KEY_QSTILES_STYLES);
        mCustomSettingsObserver.observe();
        if (mQSTilesStyles != null) {
            mQSTilesStyles.setOnPreferenceChangeListener(this);
        }

        mBrightnessStyles = (ListPreference) findPreference(KEY_BRIGHTNESS_STYLE);
        if (mBrightnessStyles != null) {
            mBrightnessStyles.setValue(Settings.Secure.getString(resolver, BRIGHTNESS_SLIDER_STYLE));
            OLD_SLIDER = Settings.Secure.getString(resolver, BRIGHTNESS_SLIDER_STYLE);
            mBrightnessStyles.setSummary(mBrightnessStyles.getEntry());
            mBrightnessStyles.setOnPreferenceChangeListener(this);
        }
    }

    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_PANEL_STYLE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.QS_PANEL_STYLE))) {
                updateQsStyle();
            }
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
            mCustomSettingsObserver.observe();
            return true;
        } else if (preference == mBrightnessStyles) {
            mBrightnessStyles.setValue((String) newValue);
            mBrightnessStyles.setSummary(mBrightnessStyles.getEntry());
            Settings.Secure.putString(resolver, BRIGHTNESS_SLIDER_STYLE, (String) newValue);
            String current = Settings.Secure.getString(resolver, BRIGHTNESS_SLIDER_STYLE);
            if (isOverlayEnabled(OLD_SLIDER)) {
                RROManager(OLD_SLIDER, false);
            }
            OLD_SLIDER = Settings.Secure.getString(resolver, BRIGHTNESS_SLIDER_STYLE);
            RROManager(current, true);
            return true;
        }
        return true;
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

    private void updateQsStyle() {

        int qsPanelStyle = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_PANEL_STYLE , 0, USER_SYSTEM);

        if (qsPanelStyle == 0) {
            setDefaultStyle(mOverlayService);
        } else if (qsPanelStyle == 1) {
            setQsStyle(mOverlayService, "com.android.system.qs.outline");
        } else if (qsPanelStyle == 2 || qsPanelStyle == 3) {
            setQsStyle(mOverlayService, "com.android.system.qs.twotoneaccent");
        } else if (qsPanelStyle == 4) {
            setQsStyle(mOverlayService, "com.android.system.qs.shaded");
        } else if (qsPanelStyle == 5) {
            setQsStyle(mOverlayService, "com.android.system.qs.cyberpunk");
        } else if (qsPanelStyle == 6) {
            setQsStyle(mOverlayService, "com.android.systemui.qstiles.classic");
        } else if (qsPanelStyle == 7) {
            setQsStyle(mOverlayService, "com.android.elixir.maybe.rectangle");
        }
    }

    public static void setDefaultStyle(IOverlayManager overlayManager) {
        for (int i = 0; i < QS_STYLES.length; i++) {
            String qsStyles = QS_STYLES[i];
            try {
                overlayManager.setEnabled(qsStyles, false, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setQsStyle(IOverlayManager overlayManager, String overlayName) {
        try {
            for (int i = 0; i < QS_STYLES.length; i++) {
                String qsStyles = QS_STYLES[i];
                try {
                    overlayManager.setEnabled(qsStyles, false, USER_SYSTEM);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            overlayManager.setEnabled(overlayName, true, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.qstiles);
}

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
import android.hardware.display.AmbientDisplayConfiguration;
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
import androidx.preference.SwitchPreference;

import static android.provider.Settings.System.SWIPE_TO_SCREENSHOT;
import static android.provider.Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED;
import static android.provider.Settings.Secure.ONE_HANDED_MODE_ENABLED;
import static android.provider.Settings.Secure.DOZE_PICK_UP_GESTURE;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_2BUTTON;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL;
import static android.provider.Settings.System.DOZE_TRIGGER_DOUBLETAP;
import static android.provider.Settings.System.VOLBTN_MUSIC_CONTROLS;
import static android.provider.Settings.System.ADAPTIVE_PLAYBACK_ENABLED;


@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class EssenceGestures extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "EssenceGestures";
    private static final String KEY_SWIPE_SCREENSHOT = "swipe_to_screenshot";
    private static final String KEY_DOUBLE_TAP_POWER = "gesture_double_tap_power_input";
    private static final String KEY_GESTURE_NAV = "gesture_system_navigation_input";
    private static final String KEY_GESTURE_ONE_HAND = "gesture_one_hand";
    private static final String KEY_GESTURE_PICKUP = "gesture_pick_up_input";
    private static final String KEY_GESTURE_POWER_MENU = "gesture_power_menu";
    private static final String KEY_GESTURE_DOUBLE_TAP = "doze_double_tap_summary";
    private static final String KEY_GESTURE_MUSIC_CONTROL = "volume_button_music_control_sumary";
    private static final String KEY_GESTURE_ADAPTIVE_PLAYBACK = "gesture_adaptive_playback_summary";

    private AmbientDisplayConfiguration mAmbientConfig;
    private Preference mSwipeSS;
    private Preference mDoubleTapPower;
    private Preference mGestureNav;
    private Preference mGestureOneHand;
    private Preference mGesturePickup;
    private Preference mGesturePowerMenu;
    private Preference mGestureDoubleTap;
    private Preference mGestureMusicControl;
    private Preference mGestureAdaptiveMusic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();
        addPreferencesFromResource(R.xml.essence_gestures);
        updateSummary();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSummary();
    }

    public void updateSummary() {
        final ContentResolver resolver = getActivity().getContentResolver();
        final Resources res = getResources();
        final PreferenceScreen prefSet = getPreferenceScreen();
        mSwipeSS = findPreference(KEY_SWIPE_SCREENSHOT);
        mDoubleTapPower = findPreference(KEY_DOUBLE_TAP_POWER);
        mGestureNav = findPreference(KEY_GESTURE_NAV);
        mGestureOneHand = findPreference(KEY_GESTURE_ONE_HAND);
        mGesturePickup = findPreference(KEY_GESTURE_PICKUP);
        mGesturePowerMenu = findPreference(KEY_GESTURE_POWER_MENU);
        mGestureDoubleTap = findPreference(KEY_GESTURE_DOUBLE_TAP);
        mGestureMusicControl = findPreference(KEY_GESTURE_MUSIC_CONTROL);
        mGestureAdaptiveMusic = findPreference(KEY_GESTURE_ADAPTIVE_PLAYBACK);

        if (mSwipeSS != null) {
            Boolean SwipeSS = Settings.System.getInt(resolver, SWIPE_TO_SCREENSHOT, 0) != 0;
            if (SwipeSS) {
                mSwipeSS.setSummary("ON");
            } else {
                mSwipeSS.setSummary("OFF");
            }
        }
        if (mDoubleTapPower != null) {
            Boolean DoubleTapPower = Settings.Secure.getInt(resolver, CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 0) == 0;
            if (DoubleTapPower) {
                mDoubleTapPower.setSummary("ON");
            } else {
                mDoubleTapPower.setSummary("OFF");
            }
        }
        if (mGestureNav != null) {
            Boolean ButtonNav = NAV_BAR_MODE_2BUTTON == res.getInteger(com.android.internal.R.integer.config_navBarInteractionMode);
            Boolean GestureNav = NAV_BAR_MODE_GESTURAL == res.getInteger(com.android.internal.R.integer.config_navBarInteractionMode);
            if (ButtonNav == true) {
                mGestureNav.setSummary("2-button navigation");
            } else if (GestureNav == true) {
                mGestureNav.setSummary("Gesture navigation");
            } else {
                mGestureNav.setSummary("3-button navigation");
            }
        }
        if (mGestureOneHand != null) {
            Boolean GestureOneHand = Settings.Secure.getIntForUser(resolver, Settings.Secure.ONE_HANDED_MODE_ENABLED, 0, UserHandle.USER_CURRENT) == 1;
            if (GestureOneHand) {
                mGestureOneHand.setSummary("ON");
            } else {
                mGestureOneHand.setSummary("OFF");
            }
        }
        if (mGesturePickup != null) {
            if (!getAmbientConfig().dozePickupSensorAvailable()) {
                prefSet.removePreference(mGesturePickup);
            } else {
                Boolean GesturePickup = Settings.Secure.getInt(resolver, Settings.Secure.DOZE_PICK_UP_GESTURE, 0) == 1;
                if (GesturePickup) {
                    mGesturePickup.setSummary("ON");
                } else {
                    mGesturePickup.setSummary("OFF");
                }
            }
        }
        if (mGesturePowerMenu != null) {
            mGesturePowerMenu.setSummary("Customize power button behavior");
        }
        if (mGestureDoubleTap!= null) {
            final Boolean enabled = Settings.System.getInt(resolver, DOZE_TRIGGER_DOUBLETAP, 0) != 0;
            if (enabled) {
                mGestureDoubleTap.setSummary("ON");
            } else {
                mGestureDoubleTap.setSummary("OFF");
            }
        }
        if (mGestureMusicControl != null) {
            final Boolean enabled = Settings.System.getInt(resolver, VOLBTN_MUSIC_CONTROLS, 0) != 0;
            if (enabled) {
                mGestureMusicControl.setSummary("ON");
            } else {
                mGestureMusicControl.setSummary("OFF");
            }
        }
        if (mGestureAdaptiveMusic != null) {
            final Boolean enabled = Settings.System.getIntForUser(resolver, ADAPTIVE_PLAYBACK_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
            if (enabled) {
                mGestureAdaptiveMusic.setSummary("ON");
            } else {
                mGestureAdaptiveMusic.setSummary("OFF");
            }
        }
    }

    private AmbientDisplayConfiguration getAmbientConfig() {
        if (mAmbientConfig == null) {
            mAmbientConfig = new AmbientDisplayConfiguration(getActivity());
        }

        return mAmbientConfig;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        return true;
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.essence_gestures);
}

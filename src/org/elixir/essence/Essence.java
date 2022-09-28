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

package org.elixir.essence;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.viewpager.widget.ViewPager;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import org.elixir.essence.categories.Lockscreen;
import org.elixir.essence.categories.StatusBar;
import org.elixir.essence.categories.Themes;
import org.elixir.essence.categories.Qs;
import org.elixir.essence.categories.About;
import org.elixir.essence.categories.Misc;
import org.elixir.essence.categories.Donate;
import java.util.Random;

import static android.provider.Settings.Secure.HIDE_ESSENCE_ICONS;


public class Essence extends SettingsPreferenceFragment implements   
       Preference.OnPreferenceChangeListener {

    private static final int MENU_HELP  = 0;
	public static final String TAG = "Essence";
    private boolean enabled;
    private PreferenceCategory prefCat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getActivity().getContentResolver();
        addPreferencesFromResource(R.xml.essence_settings);
        enabled = Settings.Secure.getInt(resolver, HIDE_ESSENCE_ICONS, 0) == 1;
        if (enabled) {
            removeIconTop(2);
            removeIconBot(3);
            removeIconTop(4);
            removeIconMid(5);
            removeIconMid(6);
            removeIconMid(7);
            removeIconMid(8);
            removeIconBot(9);
        }
        updateGreetings();
    }

    public void updateGreetings() {
        prefCat=(PreferenceCategory)findPreference("essenceSubtitle");
        final String[] greetings = 
        {
            "Need some help? Join our TG group.", 
            "Isn't it a great day?",
            "Welcome to my heart! Let me open the door.",
            "It always seems impossible until it's done.",
            "If there is no struggle, there is no progress."
        };
        if (prefCat != null) {
            Random random = new Random();
            int index = random.nextInt(greetings.length);
            prefCat.setTitle(greetings[index]);
        }
    }

    public void removeIconTop(int prefNum) {
        PreferenceScreen prefScreen = getPreferenceScreen();
        Preference pref = prefScreen.getPreference(prefNum);
        if (prefScreen == null) {
            return;
        }
        else if (pref != null) {
            pref.setLayoutResource(R.layout.essence_pref_card_top_no_icn);
        }
    }

    public void removeIconMid(int prefNum) {
        PreferenceScreen prefScreen = getPreferenceScreen();
        Preference pref = prefScreen.getPreference(prefNum);
        if (prefScreen == null) {
            return;
        }
        else if (pref != null) {
            pref.setLayoutResource(R.layout.essence_pref_card_mid_no_icn);
        }
    }

    public void removeIconBot(int prefNum) {
        PreferenceScreen prefScreen = getPreferenceScreen();
        Preference pref = prefScreen.getPreference(prefNum);
        if (prefScreen == null) {
            return;
        }
        else if (pref != null) {
            pref.setLayoutResource(R.layout.essence_pref_card_bot_no_icn);
        }
    }

    public void removeIconMid2(int prefNum) {
        PreferenceScreen prefScreen = getPreferenceScreen();
        Preference pref = prefScreen.getPreference(prefNum);
        if (prefScreen == null) {
            return;
        }
        else if (pref != null) {
            pref.setLayoutResource(R.layout.essence_pref_card_mid2_no_icn);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateGreetings();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        return true;
    }
}


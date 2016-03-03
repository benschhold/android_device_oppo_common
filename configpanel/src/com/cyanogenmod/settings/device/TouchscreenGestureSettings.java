/*
 * Copyright (C) 2015-2016 The CyanogenMod Project
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

package com.cyanogenmod.settings.device;

import java.io.File;

import com.cyanogenmod.settings.device.utils.Constants;
import com.cyanogenmod.settings.device.utils.FileUtils;
import com.cyanogenmod.settings.device.utils.NodePreferenceActivity;

import cyanogenmod.providers.CMSettings;
import org.cyanogenmod.internal.util.ScreenType;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.provider.Settings;

public class TouchscreenGestureSettings extends NodePreferenceActivity {
    private static final String KEY_HAPTIC_FEEDBACK = "touchscreen_gesture_haptic_feedback";

    private SwitchPreference mHapticFeedback;
    private SwitchPreference mKeySwap;
    private ListPreference mSliderTop;
    private ListPreference mSliderMiddle;
    private ListPreference mSliderBottom;
    private PreferenceCategory mNotificationCat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.touchscreen_panel);

        mHapticFeedback = (SwitchPreference) findPreference(KEY_HAPTIC_FEEDBACK);
        mHapticFeedback.setOnPreferenceChangeListener(this);

        mNotificationCat = (PreferenceCategory) findPreference("notification_cat");
        mKeySwap = (SwitchPreference) findPreference(Constants.KEY_SWAP_KEY);
        mSliderTop = (ListPreference) findPreference("keycode_slider_top");
        mSliderMiddle = (ListPreference) findPreference("keycode_slider_middle");
        mSliderBottom = (ListPreference) findPreference("keycode_slider_bottom");

        if (Startup.hasTristateSwitch()) {
            setSummary(mSliderTop, Constants.KEYCODE_SLIDER_TOP);
            setSummary(mSliderMiddle, Constants.KEYCODE_SLIDER_MIDDLE);
            setSummary(mSliderBottom, Constants.KEYCODE_SLIDER_BOTTOM);
        } else {
            getPreferenceScreen().removePreference(mKeySwap);
            getPreferenceScreen().removePreference(mNotificationCat);
        }

    }

    private void setSummary(ListPreference preference, String file) {
        String[] notiactions = getResources().getStringArray(R.array.notification_slider_action_entries);

        String keyCode = FileUtils.readOneLine(file);
        if (keyCode == null) return;
        int value = Integer.parseInt(keyCode);
        preference.setSummary(notiactions[value - 600]);
        preference.setValueIndex(value - 600);
        preference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHapticFeedback) {
            final boolean value = (Boolean) newValue;
            CMSettings.System.putInt(getContentResolver(),
                    CMSettings.System.TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK, value ? 1 : 0);
            return true;
        }

        if (preference instanceof SwitchPreference) {
            return super.onPreferenceChange(preference, newValue);
        }

        String file = null;
        int value = ((ListPreference) preference).findIndexOfValue((String) newValue);
        if (preference == mSliderTop) file = Constants.KEYCODE_SLIDER_TOP;
        else if (preference == mSliderMiddle) file = Constants.KEYCODE_SLIDER_MIDDLE;
        else if (preference == mSliderBottom) file = Constants.KEYCODE_SLIDER_BOTTOM;
        if (file == null) return false;

        FileUtils.writeLine(file, String.valueOf(value + 600));
        String[] notiactions = getResources().getStringArray(R.array.notification_slider_action_entries);
        preference.setSummary(notiactions[value]);

        Constants.savePreferenceInt(this, preference.getKey(), value);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If running on a phone, remove padding around the listview
        if (!ScreenType.isTablet(this)) {
            getListView().setPadding(0, 0, 0, 0);
        }

        mHapticFeedback.setChecked(CMSettings.System.getInt(getContentResolver(),
                CMSettings.System.TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK, 1) != 0);
    }
}

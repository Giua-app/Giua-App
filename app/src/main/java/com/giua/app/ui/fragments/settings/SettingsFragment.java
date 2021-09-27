/*
 * Giua App
 * Android app to view data from the giua@school workbook
 * Copyright (C) 2021 - 2021 Hiem, Franck1421 and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package com.giua.app.ui.fragments.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.giua.app.ActivityManager;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.app.ui.activities.AboutActivity;
import com.giua.app.ui.activities.AppIntroActivity;
import com.giua.app.ui.activities.LogdogViewerActivity;
import com.giua.webscraper.GiuaScraper;

import java.util.Objects;
import java.util.regex.Pattern;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);

        setupAllObjects(requireContext());
    }

    private void setupAllObjects(Context context) {

        //region Generale

        setupNotificationObject(context);
        setupAboutScreenObject();
        setupIntroScreenObject();
        setupDebugModeObject(context);

        //endreigon

        //region Personalizzazione

        setupThemeObject(context);

        //endregion

        //region Debug

        setupDemoModeObject(context);
        setupSiteUrlObject();
        setupCrashScreenObject();
        setupLogcatViewerObject();

        if (SettingsData.getSettingBoolean(context, SettingKey.DEBUG_MODE)) {
            findPreference("debugCategory").setVisible(true);
        }

        //endregion
    }

    private void setupDemoModeObject(Context context) {
        SwitchPreference swDemoMode = findPreference("demoMode");
        swDemoMode.setChecked(SettingsData.getSettingBoolean(context, SettingKey.DEMO_MODE));
        swDemoMode.setOnPreferenceChangeListener(this::swDemoModeChangeListener);
    }

    private boolean swDemoModeChangeListener(Preference preference, Object o) {
        SettingsData.saveSettingBoolean(requireContext(), SettingKey.DEMO_MODE, (boolean) o);
        startActivity(new Intent(requireActivity(), ActivityManager.class));
        return true;
    }

    private void setupDebugModeObject(Context context) {
        SwitchPreference swDebugMode = Objects.requireNonNull(findPreference("debugMode"));
        swDebugMode.setChecked(SettingsData.getSettingBoolean(context, SettingKey.DEBUG_MODE));
        swDebugMode.setOnPreferenceChangeListener(this::swDebugModeChangeListener);
    }

    private boolean swDebugModeChangeListener(Preference preference, Object o) {
        SettingsData.saveSettingBoolean(requireContext(), SettingKey.DEBUG_MODE, (boolean) o);
        findPreference("debugCategory").setVisible((boolean) o);
        return true;
    }

    private void setupNotificationObject(Context context) {
        SwitchPreference swNotification = Objects.requireNonNull(findPreference("notification"));
        swNotification.setChecked(SettingsData.getSettingBoolean(context, SettingKey.NOTIFICATION));
        swNotification.setOnPreferenceChangeListener(this::swNotificationChangeListener);
    }

    private boolean swNotificationChangeListener(Preference preference, Object o) {
        SettingsData.saveSettingBoolean(requireContext(), SettingKey.NOTIFICATION, (boolean) o);
        return true;
    }

    private void setupThemeObject(Context context) {
        ListPreference lTheme = Objects.requireNonNull(findPreference("theme"));
        lTheme.setEntries(new CharSequence[]{"Chiaro", "Scuro", "Segui il sistema"});
        lTheme.setEntryValues(new CharSequence[]{"0", "1", "2"});
        String defualtTheme = SettingsData.getSettingString(context, SettingKey.THEME);
        if (!defualtTheme.equals(""))
            lTheme.setValueIndex(Integer.parseInt(defualtTheme));
        else
            lTheme.setValueIndex(2);

        lTheme.setOnPreferenceChangeListener(this::lThemeChangeListener);
    }

    private void setupCrashScreenObject() {
        Preference btnCrashScreen = Objects.requireNonNull(findPreference("crashScreen"));
        btnCrashScreen.setOnPreferenceClickListener(this::btnCrashScreenOnClick);
    }

    private void setupLogcatViewerObject() {
        Preference btnCrashScreen = Objects.requireNonNull(findPreference("viewLog"));
        btnCrashScreen.setOnPreferenceClickListener(this::btnViewLogScreenOnClick);
    }

    private void setupAboutScreenObject() {
        Preference btnAboutScreen = Objects.requireNonNull(findPreference("aboutScreen"));
        btnAboutScreen.setOnPreferenceClickListener(this::btnAboutScreenOnClick);
    }

    private void setupIntroScreenObject() {
        Preference btnIntroScreen = Objects.requireNonNull(findPreference("introScreen"));
        btnIntroScreen.setOnPreferenceClickListener(this::btnIntroScreenOnClick);
    }

    private void setupSiteUrlObject() {
        EditTextPreference etSiteUrl = Objects.requireNonNull(findPreference("siteUrl"));
        String defaultUrl = SettingsData.getSettingString(requireContext(), SettingKey.DEFAULT_URL);
        if (!defaultUrl.equals(""))
            etSiteUrl.setText(defaultUrl);
        else
            etSiteUrl.setText("https://registro.giua.edu.it");
        etSiteUrl.setOnPreferenceChangeListener(this::siteUrlChanged);
        etSiteUrl.setOnPreferenceClickListener(this::siteUrlOnClick);
    }

    private boolean siteUrlOnClick(Preference preference) {
        if (!Pattern.matches("https?://([a-zA-Z0-9]+[.])+([a-zA-Z0-9]+)(:[0-9]+)?", ((EditTextPreference) preference).getText())) {
            String defaultUrl = SettingsData.getSettingString(requireContext(), SettingKey.DEFAULT_URL);
            if (!defaultUrl.equals(""))
                ((EditTextPreference) preference).setText(defaultUrl);
            else
                ((EditTextPreference) preference).setText("https://registro.giua.edu.it");
        }

        return true;
    }

    private boolean lThemeChangeListener(Preference preference, Object o) {
        SettingsData.saveSettingString(requireContext(), SettingKey.THEME, (String) o);
        switch ((String) o) {
            case "0":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "1":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "2":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        return true;
    }

    private boolean btnCrashScreenOnClick(Preference preference) {
        throw new RuntimeException("FATAL ERROR: World not found. What have you done?!?!");
    }

    private boolean btnAboutScreenOnClick(Preference preference) {
        startActivity(new Intent(requireContext(), AboutActivity.class));
        return true;
    }

    private boolean btnViewLogScreenOnClick(Preference preference) {
        startActivity(new Intent(requireContext(), LogdogViewerActivity.class));
        return true;
    }

    private boolean btnIntroScreenOnClick(Preference preference) {
        startActivity(new Intent(requireContext(), AppIntroActivity.class));
        return true;
    }

    private boolean siteUrlChanged(Preference preference, Object o) {
        if (Pattern.matches("https?://([a-zA-Z0-9]+[.])+([a-zA-Z0-9]+)(:[0-9]+)?", (String) o)) {
            GiuaScraper.setSiteURL((String) o);
            SettingsData.saveSettingString(requireContext(), SettingKey.DEFAULT_URL, (String) o);
        } else {
            ((EditTextPreference) preference).setText("https://registro.giua.edu.it");
        }
        //TODO: Fai capire che ce stato un errore
        return true;
    }
}

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

package com.giua.app.ui.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.giua.app.AboutActivity;
import com.giua.app.R;
import com.giua.webscraper.GiuaScraper;

import java.util.regex.Pattern;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);

        setupAllObjects();
    }

    private void setupAllObjects() {
        EditTextPreference etSiteUrl = findPreference("siteUrl");
        etSiteUrl.setPersistent(true);
        etSiteUrl.setText(GiuaScraper.getSiteURL());
        etSiteUrl.setOnPreferenceChangeListener(this::siteUrlChanged);

        Preference btnAboutScreen = findPreference("aboutScreen");
        btnAboutScreen.setOnPreferenceClickListener(this::btnAboutScreenOnClick);

        Preference btnCrashScreen = findPreference("crashScreen");
        btnCrashScreen.setOnPreferenceClickListener(this::btnCrashScreenOnClick);
    }

    private boolean btnCrashScreenOnClick(Preference preference) {
        throw new RuntimeException("FATAL ERROR: World not found. What have you done?!?!");
    }

    private boolean btnAboutScreenOnClick(Preference preference) {
        startActivity(new Intent(requireContext(), AboutActivity.class));
        return true;
    }

    private boolean siteUrlChanged(Preference preference, Object o) {
        if (Pattern.matches("https?://([a-zA-Z0-9]+[.])+([a-zA-Z0-9]+)(:[0-9]+)?", String.valueOf(((EditTextPreference) preference).getText()))) {
            GiuaScraper.setSiteURL(String.valueOf(((EditTextPreference) preference).getText()));
        } //else
        //TODO: Fai capire che ce stato un errore
        return true;
    }
}

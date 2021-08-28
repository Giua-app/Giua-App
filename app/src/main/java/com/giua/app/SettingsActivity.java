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

package com.giua.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.giua.webscraper.GiuaScraper;
import com.google.android.material.snackbar.Snackbar;

import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {

    EditText etSiteURL;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        etSiteURL = findViewById(R.id.settings_site_url_edit_text);

        setSupportActionBar(toolbar);

        etSiteURL.setText(GiuaScraper.getSiteURL());
        etSiteURL.setSelection(etSiteURL.getText().length());

        findViewById(R.id.settings_save_button).setOnClickListener(this::btnSaveClick);

        findViewById(R.id.settings_crash_button).setOnClickListener(this::btnCrashClick);

        findViewById(R.id.settings_about_button).setOnClickListener(this::btnAboutClick);
    }

    private void setErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void btnSaveClick(View view) {
        if (Pattern.matches("https?://([a-zA-Z0-9]+[.])+([a-zA-Z0-9]+)(:[0-9]+)?", String.valueOf(etSiteURL.getText()))) {
            GiuaScraper.setSiteURL(String.valueOf(etSiteURL.getText()));
            SettingsData.saveSettingString(this, "defaultUrl", GiuaScraper.getSiteURL());
            finish();
        } else
            setErrorMessage("L'url inserito non e' valido.");
    }

    private void btnCrashClick(View view) {
        setErrorMessage("Goodbye");
        throw new RuntimeException("FATAL ERROR: World not found. What have you done?!?!");
    }

    private void btnAboutClick(View view){
        //AboutActivity.launch(SettingsActivity.this);
        startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
    }
}

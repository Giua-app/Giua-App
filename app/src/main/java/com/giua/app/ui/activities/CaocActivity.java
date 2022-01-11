/*
 * Giua App
 * Android app to view data from the giua@school workbook
 * Copyright (C) 2021 - 2022 Hiem, Franck1421 and contributors
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

package com.giua.app.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.giua.app.AppData;
import com.giua.app.BuildConfig;
import com.giua.app.LoggerManager;
import com.giua.app.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

public class CaocActivity extends AppCompatActivity {

    LoggerManager loggerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caoc);
        loggerManager = new LoggerManager("ErrorHandler", CaocActivity.this);
        loggerManager.d("onCreate chiamato");
        loggerManager.e("-.@CRASH");
        loggerManager.d("Costruzione activity error handler");

        Button restartButton = findViewById(R.id.caoc_restart_btn);
        Button reportCrash = findViewById(R.id.caoc_report_btn);

        final CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(getIntent());

        if (config == null) {
            //This should never happen - Just finish the activity to avoid a recursive crash.
            loggerManager.e("Errore Critico: Config di CAOC è null. Impossibile continuare");
            finish();
            return;
        }


        restartButton.setText(R.string.customactivityoncrash_error_activity_restart_app);
        restartButton.setOnClickListener(v -> {
            loggerManager.w("Riavvio app");
            CustomActivityOnCrash.restartApplication(CaocActivity.this, config);
        });

        /*if (config.isShowRestartButton() && config.getRestartActivityClass() != null) {
            restartButton.setText(R.string.customactivityoncrash_error_activity_restart_app);
            restartButton.setOnClickListener(v -> {
                loggerManager.w("Riavvio app");
                CustomActivityOnCrash.restartApplication(CaocActivity.this, config);
            });
        } else {
            restartButton.setOnClickListener(v -> {
                loggerManager.w("Chiusura app");
                CustomActivityOnCrash.closeApplication(CaocActivity.this, config);
            });
        }*/

        Button moreInfoButton = findViewById(R.id.caoc_error_info_btn);

        moreInfoButton.setOnClickListener(v -> {

            AlertDialog dialog = new AlertDialog.Builder(CaocActivity.this)
                    .setTitle(R.string.customactivityoncrash_error_activity_error_details_title)
                    .setMessage(getAllErrorDetailsFromIntent(getIntent()))
                    .setPositiveButton(R.string.customactivityoncrash_error_activity_error_details_close, null)
                    .setNeutralButton(R.string.customactivityoncrash_error_activity_error_details_copy,
                            (dialog1, which) -> copyErrorToClipboard())
                    .show();
            TextView textView = dialog.findViewById(android.R.id.message);
            if (textView != null) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.customactivityoncrash_error_activity_error_details_text_size));
            }
        });

        Integer defaultErrorActivityDrawableId = config.getErrorDrawable();
        ImageView errorImageView = findViewById(R.id.caoc_image);

        if (defaultErrorActivityDrawableId != null) {
            errorImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), defaultErrorActivityDrawableId, getTheme()));
        }

        reportCrash.setOnClickListener(v -> {
            loggerManager.d("Avvio BugReportActivity in un nuovo processo");
            Intent intent = new Intent(this, BugReportActivity.class);
            intent.putExtra("fromCAOC", true);
            intent.putExtra("stacktrace", getAllErrorDetailsFromIntent(getIntent()));

            CustomActivityOnCrash.restartApplicationWithIntent(this, intent, config);
        });

        findViewById(R.id.caoc_settings_btn).setOnClickListener(this::btnSettingOnClick);

        AppData.saveCrashStatus(this, true);
    }


    private void copyErrorToClipboard() {
        String errorInformation = getAllErrorDetailsFromIntent(getIntent());

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        //Are there any devices without clipboard...?
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(getString(R.string.customactivityoncrash_error_activity_error_details_clipboard_label), errorInformation);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(CaocActivity.this, "Errore copiato negli appunti", Toast.LENGTH_SHORT).show();
        }
    }

    private void btnSettingOnClick(View view) {
        loggerManager.d("Avvio SettingsActivity");
        Intent settings = new Intent(this, SettingsActivity.class);
        settings.putExtra("fromCaoc", true);
        startActivity(settings);
    }

    /**
     * Given an Intent, returns several error details including the stack trace extra from the intent.
     *
     * @param intent  The Intent. Must not be null.
     * @author CAOC Developers
     * @return The full error details.
     */
    @NonNull
    private String getAllErrorDetailsFromIntent(@NonNull Intent intent) {
        loggerManager.d("Visualizzo dettagli errore");
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY);


        String errorDetails = "";

        errorDetails += "Build version: " + BuildConfig.VERSION_NAME + " \n";
        errorDetails += "Build date: " + dateFormat.format(new Date(BuildConfig.BUILD_TIME)) + " \n";
        errorDetails += "Current date: " + dateFormat.format(currentDate) + " \n";
        errorDetails += "Device: " + getDeviceModelName() + " \n";
        errorDetails += "OS version: Android " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ") \n \n";

        errorDetails += "Stack trace:  \n";
        errorDetails += CustomActivityOnCrash.getStackTraceFromIntent(intent);

        String activityLog = CustomActivityOnCrash.getActivityLogFromIntent(intent);

        if (activityLog != null) {
            errorDetails += "\nUser actions: \n";
            errorDetails += activityLog;
        }
        return errorDetails;
    }

    /**
     * INTERNAL method that returns the device model name with correct capitalization.
     * Taken from: http://stackoverflow.com/a/12707479/1254846
     *
     * @author CAOC Developers
     * @return The device model name (i.e., "LGE Nexus 5")
     */
    @NonNull
    private String getDeviceModelName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * INTERNAL method that capitalizes the first character of a string
     *
     * @author CAOC Developers
     * @param s The string to capitalize
     * @return The capitalized string
     */
    @NonNull
    private String capitalize(@Nullable String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

}
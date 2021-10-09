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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.giua.app.ui.activities.AppIntroActivity;
import com.giua.app.ui.activities.AutomaticLoginActivity;
import com.giua.app.ui.activities.MainLoginActivity;
import com.giua.webscraper.GiuaScraper;

import cat.ereza.customactivityoncrash.config.CaocConfig;

/**
 * Questa è la prima activity ad essere avviata e serve a gestire quale activity
 * dovrà essere startata dopo.
 */
public class ActivityManager extends AppCompatActivity {

    LoggerManager loggerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loggerManager = new LoggerManager("ActivityManager", this);
        loggerManager.d("---");
        loggerManager.d("onCreate chiamato");
        if (SettingsData.getSettingBoolean(this, SettingKey.DEMO_MODE))
            loggerManager.w("DEMO ATTIVATA");

        switch (SettingsData.getSettingString(this, SettingKey.THEME)) {
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
        super.onCreate(savedInstanceState);

        setupCaoc(); //Crash handler

        GiuaScraper.setDebugMode(true);

        if (!SettingsData.getSettingBoolean(this, SettingKey.NOT_FIRST_START))
            firstStart();

        final String defaultUrl = SettingsData.getSettingString(this, SettingKey.DEFAULT_URL);

        setupNotificationManager();

        if (!defaultUrl.equals(""))
            GiuaScraper.setSiteURL(defaultUrl);

        loggerManager.d("defaultUrl è " + defaultUrl);

        //GiuaScraper.setSiteURL("http://hiemvault.ddns.net:9090");       //Usami solo per DEBUG per non andare continuamente nelle impostazioni

        final int introStatus = SettingsData.getSettingInt(this, SettingKey.INTRO_STATUS);
        //introStatus = 0;         //DEBUG

        // 1 = Intro già vista , 0 = (Non usato) , -1 = Intro mai vista
        loggerManager.d("introStatus è " + introStatus);
        if (introStatus != 1) {
            new Thread(() -> AppData.increaseVisitCount("Primo avvio (nuove installazioni)")).start();
            loggerManager.d("Avvio App Intro Activity");
            startActivity(new Intent(ActivityManager.this, AppIntroActivity.class));
            return;
        }

        checkForUpdates();

        if (LoginData.getUser(this).equals(""))
            startMainLoginActivity();
        else
            startAutomaticLoginActivity();
    }

    //Questa funzione viene chiamata solo al primo avvio di sempre dell'app
    private void firstStart() {
        SettingsData.saveSettingBoolean(this, SettingKey.NOT_FIRST_START, true);
        SettingsData.saveSettingBoolean(this, SettingKey.NEWSLETTER_NOTIFICATION, true);
        SettingsData.saveSettingBoolean(this, SettingKey.ALERTS_NOTIFICATION, true);
        SettingsData.saveSettingBoolean(this, SettingKey.UPDATES_NOTIFICATION, true);
    }

    private void startMainLoginActivity() {
        loggerManager.d("Avvio Main Login Activity");
        startActivity(new Intent(ActivityManager.this, MainLoginActivity.class));
        finish();
    }

    private void startAutomaticLoginActivity() {
        loggerManager.d("Avvio Automatic Login Activity");
        String goTo = getIntent().getStringExtra("goTo");
        if (goTo == null)
            goTo = "";
        startActivity(new Intent(ActivityManager.this, AutomaticLoginActivity.class).putExtra("goTo", goTo));
        finish();
    }

    private void checkForUpdates(){
        new Thread(() -> {
            AppUpdateManager manager = new AppUpdateManager(ActivityManager.this);
            if (SettingsData.getSettingBoolean(this, SettingKey.UPDATES_NOTIFICATION) && manager.checkForUpdates() && manager.checkUpdateReminderDate()) {
                manager.createNotification();
            }
        }).start();
    }

    private void setupCaoc() {
        //CAOC: CustomActivityOnCrash
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //crash silently when the app is in background
                .enabled(true)
                .showErrorDetails(true)
                .showRestartButton(true)
                .trackActivities(true)
                //This shows a different image on the error activity, instead of the default upside-down bug.
                //You may use a drawable or a mipmap.
                .errorDrawable(R.drawable.ic_giuaschool_logo1)
                //.errorActivity(ErrorActivity.class)
                .apply();
        loggerManager.d("CustomActivityOnCrash setup completato");
    }

    private void setupNotificationManager(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Giua App Novità";
            String description = "";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("0", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.cancel(0);  //Cancella la notifica se cè

            CharSequence name2 = "Giua App Aggiornamenti";
            String description2 = "Aggiornamenti dell'app da github";
            int importance2 = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel2 = new NotificationChannel("1", name2, importance2);
            channel.setDescription(description2);
            NotificationManager notificationManager2 = getSystemService(NotificationManager.class);
            notificationManager2.createNotificationChannel(channel2);
        }
        loggerManager.d("Notification Manager setup completato");
    }


    /**
     * Esci dall'applicazione simulando la pressione del tasto home
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }



}

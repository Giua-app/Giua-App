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

import com.giua.webscraper.GiuaScraper;

import cat.ereza.customactivityoncrash.config.CaocConfig;

public class ActivityManager extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupCaoc(); //Crash handler
        //setupAboutBox(); //About screen


        //GiuaScraper.setSiteURL("http://hiemvault.ddns.net:9090");       //Usami solo per DEBUG per non andare continuamente nelle impostazioni
        GiuaScraper.setDebugMode(true);

        final String defaultUrl = SettingsData.getSettingString(this, "defaultUrl");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Giua App Aggiornamenti";
            String description = "";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("0", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        if (defaultUrl != null)
            GiuaScraper.setSiteURL(defaultUrl);

        final int introStatus = SettingsData.getSettingInt(this, "introStatus");
        //final int introStatus = 0;         //DEBUG

        // 1 = Intro già vista , 0 = Intro non vista , -1 = Intro mai vista
        if(introStatus != 1){
            startActivity(new Intent(ActivityManager.this, AppIntroActivity.class));
            return;
        }


        if (LoginData.getUser(this).equals(""))
            startMainLoginActivity();
        else
            startAutomaticLoginActivity();
    }

    private void startMainLoginActivity() {
        startActivity(new Intent(ActivityManager.this, MainLogin.class));
    }

    private void startAutomaticLoginActivity() {
        startActivity(new Intent(ActivityManager.this, AutomaticLogin.class));
    }

    public void setupCaoc() {
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
    }

    /*public void setupAboutBox() {
        AboutConfig aboutConfig = AboutConfig.getInstance();
        aboutConfig.appName = getString(R.string.app_name);
        aboutConfig.appIcon = R.mipmap.ic_launcher;
        aboutConfig.version = BuildConfig.VERSION_NAME;
        aboutConfig.author = "Hiem & Franck1421";
        aboutConfig.aboutLabelTitle = "About App";
        aboutConfig.packageName = getApplicationContext().getPackageName();
        //aboutConfig.buildType = AboutConfig.BuildType.GOOGLE; usato per "Leave review"

        aboutConfig.shareMessage = "ehi ciao ma sai che ho trovato questa app molto bll ceh la devi " +
                "scaricare assolutamente tipo subito ora ceh proprio immediato non ci devi manco " +
                "pensare tipo proprio apri l'app e farai 'wow meno male che ho dato retta al mio " +
                "amico altrimenti ero uno sfigato' - Condiviso da Giua App plus pro edizione premium";

        //aboutConfig.facebookUserName = "Test";
        //aboutConfig.twitterUserName = "Test";
        aboutConfig.webHomePage = "https://github.com/Giua-app/Giua-App";

        // app publisher for "Try Other Apps" item
        //aboutConfig.appPublisher = "Test";

        // if pages are stored locally, then you need to override aboutConfig.dialog to be able use custom WebView
        //aboutConfig.companyHtmlPath = "Test";
        //aboutConfig.privacyHtmlPath = "Test";
        //aboutConfig.acknowledgmentHtmlPath = "Test";

        /*aboutConfig.dialog = new IDialog() {
            @Override
            public void open(AppCompatActivity appCompatActivity, String url, String tag) {
                // handle custom implementations of WebView. It will be called when user click to web items. (Example: "Privacy", "Acknowledgments" and "About")
            }
        };*/

        /*aboutConfig.analytics = new IAnalytic() {
            @Override
            public void logUiEvent(String s, String s1) {
                // handle log events.
            }

            @Override
            public void logException(Exception e, boolean b) {
                // handle exception events.
            }
        };
        // set it only if aboutConfig.analytics is defined.
        aboutConfig.logUiEventName = "Log";*

        // Contact Support email details
        //aboutConfig.emailAddress = "Test";
        //aboutConfig.emailSubject = "Test";
        //aboutConfig.emailBody = "Test";
    }*/


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

    @Override
    protected void onRestart() {
        onRestoreInstanceState(new Bundle());
        releaseInstance();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        onRestoreInstanceState(new Bundle());
        releaseInstance();
        super.onResume();
    }

    @Override
    protected void onPause() {
        onSaveInstanceState(new Bundle());
        super.onPause();
    }

    @Override
    protected void onStop() {
        onSaveInstanceState(new Bundle());
        super.onStop();
    }
}
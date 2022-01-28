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


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.giua.app.AppData;
import com.giua.app.GlobalVariables;
import com.giua.app.InternetThread;
import com.giua.app.LoggerManager;
import com.giua.app.LoginData;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.objects.Maintenance;
import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;

public class AutomaticLoginActivity extends AppCompatActivity {
    int waitToReLogin = 5;
    Button btnLogout;
    Button btnOffline;
    ProgressBar pbLoadingScreen;
    TextView tvAutoLogin;
    LoggerManager loggerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic_login);
        loggerManager = new LoggerManager("AutomaticLoginActivity", this);
        loggerManager.d("onCreate chiamato");

        btnLogout = findViewById(R.id.loading_screen_logout_btn);

        tvAutoLogin = findViewById(R.id.loading_screen_minor_text_view);
        pbLoadingScreen = findViewById(R.id.loading_screen_progressbar);
        btnOffline = findViewById(R.id.loading_screen_offline_btn);

        btnOffline.setOnClickListener(this::btnOfflineOnClick);
        btnLogout.setOnClickListener(this::btnLogoutOnClick);
        findViewById(R.id.loading_screen_btn_settings).setOnClickListener(this::btnSettingOnClick);

        GiuaScraper.setDebugMode(true);
        loginWithPreviousCredentials();
    }

    private void loginWithPreviousCredentials() {
        if (GlobalVariables.internetThread == null || GlobalVariables.internetThread.isInterrupted() || GlobalVariables.internetThread.isInterrupting())
            GlobalVariables.internetThread = new InternetThread();

        loggerManager.d("Login automatico con credenziali salvate in corso");
        GlobalVariables.internetThread.addTask(() -> {
            runOnUiThread(() -> pbLoadingScreen.setVisibility(View.VISIBLE));
            try {
                String username = AppData.getActiveUsername(this);
                String password = LoginData.getPassword(this, username);
                String cookie = LoginData.getCookie(this, username);
                GlobalVariables.gS = new GiuaScraper(username, password, cookie, true, SettingsData.getSettingBoolean(this, SettingKey.DEMO_MODE), new LoggerManager("GiuaScraper", this));
                GlobalVariables.gS.login();
                LoginData.setCredentials(this, username, password, GlobalVariables.gS.getCookie());
                if (!AppData.getAllAccountUsernames(this).contains(GlobalVariables.gS.getUser()))
                    AppData.addAccountUsername(this, GlobalVariables.gS.getUser());
                startDrawerActivity();
            } catch (GiuaScraperExceptions.YourConnectionProblems | GiuaScraperExceptions.SiteConnectionProblems e) {
                loggerManager.e("Errore di connessione - " + e.getMessage());
                runOnUiThread(() -> btnLogout.setVisibility(View.VISIBLE));
                //runOnUiThread(() -> btnOffline.setVisibility(View.VISIBLE));  //TODO: togliere il commento quando sarà realmente disponibile e funzionante

                if (e.getClass() == GiuaScraperExceptions.YourConnectionProblems.class)
                    runOnUiThread(() -> setErrorMessage(getString(R.string.your_connection_error)));
                else if (e.getClass() == GiuaScraperExceptions.SiteConnectionProblems.class)
                    runOnUiThread(() -> setErrorMessage(getString(R.string.site_connection_error)));
                else
                    runOnUiThread(() -> setErrorMessage("E' stato riscontrato qualche problema sconosciuto riguardo la rete"));


                runOnUiThread(() -> pbLoadingScreen.setVisibility(View.INVISIBLE));
                threadSleepWithTextUpdates();

                if (waitToReLogin < 30)
                    waitToReLogin += 5;

                loginWithPreviousCredentials();
            } catch (GiuaScraperExceptions.SessionCookieEmpty sce) {     //Se il login non dovesse funzionare lancia l acitvity di login ed elimina le credenziali salvate
                if (AppData.getActiveUsername(this).equals("gsuite")) {   //Questa condizione si verifica quando è presente un acccount studente con il cookie scaduto
                    loggerManager.w("Cookie gS dell'account studente scaduto, avvio StudentLoginActivity");
                    startStudentLoginActivity();
                } else {
                    loggerManager.w("Cookie gS scaduto, avvio MainLoginActivity");
                    AppData.saveActiveUsername(this, "");
                    setErrorMessage("Le credenziali di accesso non sono più valide");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startActivity(new Intent(AutomaticLoginActivity.this, MainLoginActivity.class));
                }
            } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                loggerManager.e("Errore: sito in manutenzione");
                try {
                    Maintenance maintenance = GlobalVariables.gS.getMaintenanceInfo();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                    runOnUiThread(() -> setErrorMessage("Il sito è in manutenzione sino alle " + simpleDateFormat.format(maintenance.end)));
                } catch (Exception e2) {
                    runOnUiThread(() -> setErrorMessage(getString(R.string.maintenance_is_active_error)));
                }
                runOnUiThread(() -> btnLogout.setVisibility(View.VISIBLE));
                //runOnUiThread(() -> btnOffline.setVisibility(View.VISIBLE));
                runOnUiThread(() -> pbLoadingScreen.setVisibility(View.GONE));
                runOnUiThread(() -> tvAutoLogin.setText("Accesso fallito."));

            }
        });
    }

    private void btnLogoutOnClick(View view) {
        loggerManager.d("Logout richiesto dall'utente, avvio MainLoginActivity");
        AppData.saveActiveUsername(this, "");
        startActivity(new Intent(AutomaticLoginActivity.this, MainLoginActivity.class));
        finish();
    }

    private void btnOfflineOnClick(View view) {
        loggerManager.d("Modalità offline richiesta dall'utente");
        startActivity(new Intent(AutomaticLoginActivity.this, DrawerActivity.class).putExtra("offline", true));
        finish();
    }

    private void btnSettingOnClick(View view) {
        loggerManager.d("Avvio SettingsActivity");
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void threadSleepWithTextUpdates() {
        for (int i = 0; i < waitToReLogin; i++) {
            int finalI = i;
            runOnUiThread(() -> tvAutoLogin.setText("Login fallito\nRiprovo tra " + (waitToReLogin - finalI) + " secondi"));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        runOnUiThread(() -> tvAutoLogin.setText("Riprovo..."));
    }

    private void startStudentLoginActivity() {
        runOnUiThread(() -> findViewById(R.id.loading_screen_btn_settings).setVisibility(View.GONE));
        loggerManager.d("Avvio StudentLoginActivity");
        startActivity(new Intent(AutomaticLoginActivity.this, StudentLoginActivity.class).putExtra("sender", "AutomaticLogin"));
        finish();
    }

    private void startDrawerActivity() {
        runOnUiThread(() -> findViewById(R.id.loading_screen_btn_settings).setVisibility(View.GONE));
        loggerManager.d("Avvio DrawerActivity");
        String goTo = getIntent().getStringExtra("goTo");
        if (goTo == null)
            goTo = "";
        startActivity(new Intent(AutomaticLoginActivity.this, DrawerActivity.class).putExtra("goTo", goTo));
        finish();
    }

    private void setErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
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

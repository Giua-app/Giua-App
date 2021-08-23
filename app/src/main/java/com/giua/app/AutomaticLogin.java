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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

public class AutomaticLogin extends AppCompatActivity {
    int waitToReLogin = 5;
    Button logoutButton;
    ProgressBar progressBar;
    TextView textAutoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        logoutButton = findViewById(R.id.loading_screen_logout_btn);
        textAutoLogin = findViewById(R.id.loading_screen_minor_text_view);
        progressBar = findViewById(R.id.loading_screen_progressbar);
        logoutButton.setOnClickListener((view) -> {
            LoginData.clearAll(this);
            startActivity(new Intent(AutomaticLogin.this, MainLogin.class));
        });

        GiuaScraper.setDebugMode(true);
        loginWithPreviousCredentials();
    }

    private void loginWithPreviousCredentials() {
        new Thread(() -> {
            runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
            try {
                GlobalVariables.gS = new GiuaScraper(LoginData.getUser(this), LoginData.getPassword(this), LoginData.getCookie(this), true);
                GlobalVariables.gS.login();
                LoginData.setCredentials(this, LoginData.getUser(this), LoginData.getPassword(this), GlobalVariables.gS.getCookie());
                startDrawerActivity();
            } catch (GiuaScraperExceptions.YourConnectionProblems | GiuaScraperExceptions.SiteConnectionProblems e) {
                runOnUiThread(() -> logoutButton.setVisibility(View.VISIBLE));
                if (!GiuaScraper.isMyInternetWorking()) {
                    runOnUiThread(() -> setErrorMessage(getString(R.string.your_connection_error)));
                } else if (!GiuaScraper.isSiteWorking()) {
                    runOnUiThread(() -> setErrorMessage(getString(R.string.site_connection_error)));
                } else {
                    runOnUiThread(() -> setErrorMessage("E' stato riscontrato qualche problema sconosciuto riguardo la rete"));
                }
                try {
                    //Thread.sleep(2000);
                    runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));
                    //runOnUiThread(() -> setErrorMessage("Riprovo automaticamente tra " + waitToReLogin + " secondi"));
                    threadSleepWithTextUpdates();

                    if (waitToReLogin < 30)
                        waitToReLogin += 5;

                    loginWithPreviousCredentials();
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            } catch (GiuaScraperExceptions.SessionCookieEmpty sce) {     //Se il login non dovesse funzionare lancia l acitvity di login ed elimina le credenziali salvate
                if (LoginData.getUser(this).equals("gsuite"))    //Questa condizione si verifica quando è presente un acccount studente con il cookie scaduto
                    startStudentLoginActivity();
                else {
                    LoginData.clearAll(this);
                    setErrorMessage("Le credenziali di accesso non sono più valide");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startActivity(new Intent(AutomaticLogin.this, MainLogin.class));
                }
            }
        }).start();
    }

    private void threadSleepWithTextUpdates() throws InterruptedException {
        for (int i = 0; i < waitToReLogin; i++) {
            int finalI = i;
            runOnUiThread(() -> textAutoLogin.setText("Login fallito\nRiprovo tra " + (waitToReLogin - finalI) + " secondi"));
            Thread.sleep(1000);
        }
        runOnUiThread(() -> textAutoLogin.setText("Riprovo..."));
    }

    private void startStudentLoginActivity() {
        startActivity(new Intent(AutomaticLogin.this, StudentLoginActivity.class));
    }

    private void startDrawerActivity() {
        startActivity(new Intent(AutomaticLogin.this, DrawerActivity.class));
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

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
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

public class MainLogin extends AppCompatActivity {
    EditText etUsername;
    EditText etPassword;
    ProgressBar pgProgressBar;
    ImageButton btnShowPassword;
    Button btnLogin;
    boolean btnShowActivated = false;
    CheckBox chRememberCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.textUser);
        etPassword = findViewById(R.id.textPassword);
        pgProgressBar = findViewById(R.id.progressBar);
        btnShowPassword = findViewById(R.id.show_password_button);
        btnLogin = findViewById(R.id.login_button);
        chRememberCredentials = findViewById(R.id.checkbox_remember_credentials);

        etUsername.setText(LoginData.getUser(getApplicationContext()));     //Imposta lo username memorizzato
        etPassword.setText(LoginData.getPassword(getApplicationContext()));     //Imposta la password memorizzata

        findViewById(R.id.floating_settings_button).setOnClickListener(this::btnSettingClickListener);
        btnLogin.setOnClickListener(this::btnLoginClickListener);
        btnShowPassword.setOnClickListener(this::showPasswordClickListener);
    }

    private void login() {
        new Thread(() -> {
            try {
                GlobalVariables.gS = new GiuaScraper(etUsername.getText().toString(), etPassword.getText().toString(), LoginData.getCookie(this), true);
                GlobalVariables.gS.login();
            } catch (GiuaScraperExceptions.SessionCookieEmpty sce) {
                setErrorMessage("Informazioni di login errate!");
                etPassword.setText("");
                pgProgressBar.setVisibility(View.INVISIBLE);
                return;
            } catch (GiuaScraperExceptions.UnableToLogin utl) {
                if (!GiuaScraper.isMyInternetWorking()) {
                    setErrorMessage("Sono stati riscontrati problemi con la tua rete");
                } else if (!GiuaScraper.isSiteWorking()) {
                    setErrorMessage("Il sito non sta funzionando, riprova tra poco!");
                } else {
                    setErrorMessage("E' stato riscontrato qualche problema sconosciuto riguardo la rete");
                }
                etPassword.setText("");
                pgProgressBar.setVisibility(View.INVISIBLE);
                return;
            }

            if (GlobalVariables.gS.checkLogin()) {
                System.out.println("login ok");
                if (chRememberCredentials.isChecked()) {
                    String c = GlobalVariables.gS.getCookie();
                    LoginData.setCredentials(this, etUsername.getText().toString(), etPassword.getText().toString(), c);
                }

                startDrawerActivity();
            } else {
                setErrorMessage("Qualcosa e' andato storto!");
                etPassword.setText("");
                pgProgressBar.setVisibility(View.INVISIBLE);
            }
        }).start();
    }

    private void startDrawerActivity() {
        Intent intent = new Intent(MainLogin.this, DrawerActivity.class);
        startActivity(intent);
    }

    private void setErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Settings button click listener
     */
    private void btnSettingClickListener(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Login click listener
     */
    private void btnLoginClickListener(View view) {
        if (etPassword.getText().length() < 1) {
            setErrorMessage("Il campo della password non può essere vuoto!");
            return;
        } else if (etUsername.getText().length() < 1) {
            setErrorMessage("Il campo dello username non può essere vuoto!");
            return;
        }

        pgProgressBar.setVisibility(View.VISIBLE);

        login();
    }

    /**
     * Show password click listener
     */
    private void showPasswordClickListener(View view) {
        if (!btnShowActivated) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NULL);      //Mostra la password
            btnShowPassword.setImageResource(R.drawable.ic_baseline_visibility_24);
            etPassword.setSelection(etPassword.getText().length());
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  //Nasconde la password
            btnShowPassword.setImageResource(R.drawable.ic_baseline_visibility_off_24);
            etPassword.setSelection(etPassword.getText().length());
        }
        btnShowActivated = !btnShowActivated;
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

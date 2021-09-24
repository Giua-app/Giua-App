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

package com.giua.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.giua.app.AppData;
import com.giua.app.GlobalVariables;
import com.giua.app.LoggerManager;
import com.giua.app.LoginData;
import com.giua.app.R;
import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

public class MainLoginActivity extends AppCompatActivity {
    EditText etUsername;
    EditText etPassword;
    ProgressBar pgProgressBar;
    ImageButton btnShowPassword;
    Button btnLogin;
    TextView btnLoginAsStudent;
    boolean btnShowActivated = false;
    CheckBox chRememberCredentials;
    LoggerManager loggerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_login);
        loggerManager = new LoggerManager("MainLoginActivity", this);
        loggerManager.d("onCreate chiamato");

        etUsername = findViewById(R.id.textUser);
        etPassword = findViewById(R.id.textPassword);
        pgProgressBar = findViewById(R.id.progressBar);
        btnShowPassword = findViewById(R.id.show_password_button);
        btnLogin = findViewById(R.id.login_button);
        chRememberCredentials = findViewById(R.id.checkbox_remember_credentials);
        btnLoginAsStudent = findViewById(R.id.btn_student_login);
        btnLoginAsStudent.setText(Html.fromHtml("<p>Sei uno studente?\n<u><i>Clicca qui!</i></u></p>",0));

        etUsername.setText(LoginData.getUser(getApplicationContext()));     //Imposta lo username memorizzato
        etPassword.setText(LoginData.getPassword(getApplicationContext()));     //Imposta la password memorizzata

        findViewById(R.id.floating_settings_button).setOnClickListener(this::btnSettingOnClick);
        btnLogin.setOnClickListener(this::btnLoginOnClick);
        btnShowPassword.setOnClickListener(this::showPasswordOnClick);
        btnLoginAsStudent.setOnClickListener(this::btnLoginAsStudentOnClick);
    }

    private void login() {
        btnLogin.setEnabled(false);
        new Thread(() -> {
            try {
                loggerManager.d("Eseguo login...");
                GlobalVariables.gS = new GiuaScraper(etUsername.getText().toString(), etPassword.getText().toString(), LoginData.getCookie(this), true);
                GlobalVariables.gS.login();
                loggerManager.d("Devo ricordare le credenziali? " + chRememberCredentials.isChecked());

                if (GlobalVariables.gS.checkLogin()) {
                    if (chRememberCredentials.isChecked())
                        LoginData.setCredentials(this, etUsername.getText().toString(), etPassword.getText().toString(), GlobalVariables.gS.getCookie());
                    startDrawerActivity();
                } else {
                    loggerManager.e("Errore sconosciuto, login eseguito ma checkLogin ritorna false");
                    setErrorMessage("Qualcosa e' andato storto!");
                    etPassword.setText("");
                    this.runOnUiThread(() -> pgProgressBar.setVisibility(View.INVISIBLE));
                }

            } catch (GiuaScraperExceptions.SessionCookieEmpty e) {
                if (!e.siteSays.equals("Tipo di utente non ammesso: usare l'autenticazione tramite GSuite.")) {
                    loggerManager.e("Errore: credenziali errate");
                    setErrorMessage("Informazioni di login errate!");
                    this.runOnUiThread(() -> {
                        etPassword.setText("");
                        pgProgressBar.setVisibility(View.INVISIBLE);
                    });
                } else
                    loggerManager.d("Rilevato credenziali account google");
                    startStudentLoginActivity();
            } catch (GiuaScraperExceptions.UnableToLogin e) {
                loggerManager.e("Errore sconosciuto - " + e.getMessage());
                setErrorMessage("E' stato riscontrato qualche problema sconosciuto");
                this.runOnUiThread(() -> {
                    etPassword.setText("");
                    pgProgressBar.setVisibility(View.INVISIBLE);
                });
            } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                loggerManager.e("Errore: Manutenzione attiva");
                setErrorMessage(getString(R.string.site_in_maintenace_error));
                this.runOnUiThread(() -> {
                    etPassword.setText("");
                    pgProgressBar.setVisibility(View.INVISIBLE);
                });
            } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                loggerManager.e("Errore di connessione dell'utente");
                setErrorMessage(getString(R.string.your_connection_error));
                this.runOnUiThread(() -> {
                    etPassword.setText("");
                    pgProgressBar.setVisibility(View.INVISIBLE);
                });
            } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                loggerManager.e("Errore di connessione da parte del server");
                setErrorMessage(getString(R.string.site_connection_error));
                this.runOnUiThread(() -> {
                    etPassword.setText("");
                    pgProgressBar.setVisibility(View.INVISIBLE);
                });
            }
        }).start();
    }

    private void startStudentLoginActivity() {
        loggerManager.d("Avvio StudenteLoginActivity");
        startActivity(new Intent(MainLoginActivity.this, StudentLoginActivity.class));
    }

    private void startDrawerActivity() {
        loggerManager.d("Avvio DrawerActivity");
        new Thread(() -> AppData.increaseVisitCount("Login OK (Genitore)")).start();
        Intent intent = new Intent(MainLoginActivity.this, DrawerActivity.class);
        startActivity(intent);
    }

    /**
     * Settings button click listener
     */
    private void btnSettingOnClick(View view) {
        loggerManager.d("Avvio SettingsActivity");
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Login click listener
     */
    private void btnLoginOnClick(View view) {
        loggerManager.d("Login richiesto dall'utente");
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

    private void btnLoginAsStudentOnClick(View view) {
        loggerManager.d("Login Studente richiesto dall'utente");
        startStudentLoginActivity();
    }

    /**
     * Show password click listener
     */
    private void showPasswordOnClick(View view) {
        loggerManager.d("Mostra la password? " + !btnShowActivated);
        if (!btnShowActivated) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NULL);      //Mostra la password
            btnShowPassword.setImageResource(R.drawable.ic_baseline_visibility_24);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);  //Nasconde la password
            btnShowPassword.setImageResource(R.drawable.ic_baseline_visibility_off_24);
        }
        etPassword.setSelection(etPassword.getText().length());
        btnShowActivated = !btnShowActivated;
    }

    private void setErrorMessage(String message) {
        this.runOnUiThread(() -> btnLogin.setEnabled(true));
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
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

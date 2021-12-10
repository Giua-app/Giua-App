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
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.giua.app.AppData;
import com.giua.app.AppUpdateManager;
import com.giua.app.BuildConfig;
import com.giua.app.GlobalVariables;
import com.giua.app.LoggerManager;
import com.giua.app.LoginData;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

public class MainLoginActivity extends AppCompatActivity {
    EditText etUsername;
    EditText etPassword;
    TextInputLayout txtLayoutUsername;
    TextInputLayout txtLayoutPassword;
    ProgressBar pgProgressBar;
    Button btnLogin;
    TextView btnLoginAsStudent;
    CheckBox chRememberCredentials;
    LoggerManager loggerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_login);
        loggerManager = new LoggerManager("MainLoginActivity", this);
        loggerManager.d("onCreate chiamato");

        txtLayoutUsername = findViewById(R.id.login_txtlayout_user);
        txtLayoutPassword = findViewById(R.id.login_txtlayout_password);
        etUsername = txtLayoutUsername.getEditText();
        etPassword = txtLayoutPassword.getEditText();

        pgProgressBar = findViewById(R.id.progressBar);
        btnLogin = findViewById(R.id.login_button);
        chRememberCredentials = findViewById(R.id.checkbox_remember_credentials);
        btnLoginAsStudent = findViewById(R.id.btn_student_login);
        btnLoginAsStudent.setText(Html.fromHtml("<p>Sei uno studente?\n<u><i>Clicca qui!</i></u></p>", 0));

        etUsername.setText(LoginData.getUser(getApplicationContext()));     //Imposta lo username memorizzato
        etPassword.setText(LoginData.getPassword(getApplicationContext()));     //Imposta la password memorizzata

        txtLayoutUsername.getEditText().addTextChangedListener(onTextChange(txtLayoutUsername));
        txtLayoutPassword.getEditText().addTextChangedListener(onTextChange(txtLayoutPassword));
        findViewById(R.id.login_btn_settings).setOnClickListener(this::btnSettingOnClick);
        btnLogin.setOnClickListener(this::btnLoginOnClick);
        btnLoginAsStudent.setOnClickListener(this::btnLoginAsStudentOnClick);

        checkDisplayMetrics();
        checkForUpdateChangelog();
    }

    private TextWatcher onTextChange(final TextInputLayout view) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (view.getId() == txtLayoutUsername.getId()) {
                    txtLayoutUsername.setError(null);
                    txtLayoutUsername.setErrorEnabled(false);
                } else if (view.getId() == txtLayoutPassword.getId()) {
                    txtLayoutPassword.setError(null);
                    txtLayoutPassword.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    private void checkDisplayMetrics() {
        DisplayMetrics realMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(realMetrics);

        loggerManager.d("Dimensioni schermo: " + realMetrics.widthPixels + "x" + realMetrics.heightPixels);
        loggerManager.d("Login visibile? " + (realMetrics.widthPixels <= 1080 && realMetrics.heightPixels <= 1920 ? "Probabilmente no" : "Si"));
        if (realMetrics.widthPixels <= 1080 && realMetrics.heightPixels <= 1920) {
            loggerManager.d("Aggiusto margini di card view per essere visibili");
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) findViewById(R.id.main_card_view).getLayoutParams();
            params.setMargins(0, 20, 0, 0);
            findViewById(R.id.main_card_view).setLayoutParams(params);
        }
    }

    private void checkForUpdateChangelog(){
        if(!SettingsData.getSettingString(this, SettingKey.APP_VER).equals("")
                && !SettingsData.getSettingString(this, SettingKey.APP_VER).equals(BuildConfig.VERSION_NAME)){

            loggerManager.w("Aggiornamento installato rilevato");
            loggerManager.d("Cancello apk dell'aggiornamento e mostro changelog");
            AppUpdateManager upd = new AppUpdateManager(MainLoginActivity.this);
            upd.deleteOldApk();
            new Thread(upd::showDialogReleaseChangelog).start();
        }
        SettingsData.saveSettingString(this, SettingKey.APP_VER, BuildConfig.VERSION_NAME);
    }

    private void login() {
        new Thread(() -> {
            try {
                loggerManager.d("Eseguo login...");
                GlobalVariables.gS = new GiuaScraper(etUsername.getText().toString(), etPassword.getText().toString(), LoginData.getCookie(this), true, SettingsData.getSettingBoolean(this, SettingKey.DEMO_MODE), new LoggerManager("GiuaScraper", this));
                GlobalVariables.gS.login();
                loggerManager.d("Devo ricordare le credenziali? " + chRememberCredentials.isChecked());

                if (GlobalVariables.gS.checkLogin()) {
                    if (chRememberCredentials.isChecked())
                        LoginData.setCredentials(this, etUsername.getText().toString(), etPassword.getText().toString(), GlobalVariables.gS.getCookie());
                    startDrawerActivity();
                } else {
                    loggerManager.e("Errore sconosciuto, login eseguito ma checkLogin ritorna false");
                    setErrorMessage("Qualcosa e' andato storto!");
                    this.runOnUiThread(() -> {
                        txtLayoutPassword.setError("Riprova");
                        etPassword.setText("");
                        btnLogin.setVisibility(View.VISIBLE);
                        pgProgressBar.setVisibility(View.INVISIBLE);
                    });
                }

            } catch (GiuaScraperExceptions.SessionCookieEmpty e) {
                loggerManager.e("Errore: il sito dice: " + e.siteSays);
                if (e.siteSays.equals("Tipo di utente non ammesso: usare l'autenticazione tramite GSuite.")) {
                    loggerManager.d("Rilevato credenziali account google");
                    startStudentLoginActivity();
                } else {
                    loggerManager.e("Errore: credenziali errate");
                    setErrorMessage("Credenziali di login errate!");
                    this.runOnUiThread(() -> {
                        etPassword.setText("");
                        txtLayoutPassword.setError("Password o Username errato");
                        pgProgressBar.setVisibility(View.INVISIBLE);
                        btnLogin.setVisibility(View.VISIBLE);
                    });
                }
            } catch (GiuaScraperExceptions.UnableToLogin e) {
                loggerManager.e("Errore sconosciuto - " + e.getMessage());
                setErrorMessage("E' stato riscontrato qualche problema sconosciuto");
                this.runOnUiThread(() -> {
                    etPassword.setText("");
                    pgProgressBar.setVisibility(View.INVISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                });
            } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                loggerManager.e("Errore: Manutenzione attiva");
                setErrorMessage(getString(R.string.site_in_maintenace_error));
                this.runOnUiThread(() -> {
                    etPassword.setText("");
                    txtLayoutPassword.setError("In manutenzione, riprova più tardi!");
                    pgProgressBar.setVisibility(View.INVISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                });
            } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                loggerManager.e("Errore di connessione dell'utente");
                setErrorMessage(getString(R.string.your_connection_error));
                this.runOnUiThread(() -> {
                    etPassword.setText("");
                    txtLayoutPassword.setError("C'è un problema con la tua rete");
                    pgProgressBar.setVisibility(View.INVISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                });
            } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                loggerManager.e("Errore di connessione da parte del server");
                setErrorMessage(getString(R.string.site_connection_error));
                this.runOnUiThread(() -> {
                    etPassword.setText("");
                    txtLayoutPassword.setError("Errore di connesione al registro");
                    pgProgressBar.setVisibility(View.INVISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private void startStudentLoginActivity() {
        loggerManager.d("Avvio StudenteLoginActivity");
        startActivity(new Intent(MainLoginActivity.this, StudentLoginActivity.class).putExtra("sender", "MainLogin"));
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
        txtLayoutPassword.setError("");
        txtLayoutUsername.setError("");
        loggerManager.d("Login richiesto dall'utente");
        if (etPassword.getText().length() < 1) {
            txtLayoutPassword.setError("Inserisci la password");
        }
        if (etUsername.getText().length() < 1) {
            txtLayoutUsername.setError("Inserisci l'username");
        }


        if(etPassword.getText().length() > 0 && etUsername.getText().length() > 0){
            pgProgressBar.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.INVISIBLE);
            hideKeyboard();
            login();
        }
    }

    private void btnLoginAsStudentOnClick(View view) {
        loggerManager.d("Login Studente richiesto dall'utente");
        startStudentLoginActivity();
    }

    private void hideKeyboard() {
        InputMethodManager imm = this.getSystemService(InputMethodManager.class);
        imm.hideSoftInputFromWindow(this.btnLogin.getWindowToken(), 0);
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

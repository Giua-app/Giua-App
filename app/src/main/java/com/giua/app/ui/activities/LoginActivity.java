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
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.giua.app.AccountData;
import com.giua.app.Analytics;
import com.giua.app.AppData;
import com.giua.app.AppUpdateManager;
import com.giua.app.AppUtils;
import com.giua.app.BuildConfig;
import com.giua.app.GiuaScraperThread;
import com.giua.app.GlobalVariables;
import com.giua.app.LoggerManager;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.objects.Maintenance;
import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    EditText etUsername;
    EditText etPassword;
    TextInputLayout txtLayoutUsername;
    TextInputLayout txtLayoutPassword;
    ProgressBar pgProgressBar;
    Button btnLogin;
    TextView btnLoginAsStudent;
    TextView txtCardTitle;
    CheckBox chRememberCredentials;
    LoggerManager loggerManager;
    boolean isAddingAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (GlobalVariables.gsThread == null || GlobalVariables.gsThread.isInterrupted())
            GlobalVariables.gsThread = new GiuaScraperThread();

        isAddingAccount = getIntent().getBooleanExtra("addAccount", false);
        loggerManager = new LoggerManager("LoginActivity", this);
        loggerManager.d("onCreate chiamato");

        txtLayoutUsername = findViewById(R.id.login_txtlayout_user);
        txtLayoutPassword = findViewById(R.id.login_txtlayout_password);
        txtCardTitle = findViewById(R.id.login_card_title);
        etUsername = txtLayoutUsername.getEditText();
        etPassword = txtLayoutPassword.getEditText();

        pgProgressBar = findViewById(R.id.progressBar);
        btnLogin = findViewById(R.id.login_button);
        chRememberCredentials = findViewById(R.id.checkbox_remember_credentials);
        btnLoginAsStudent = findViewById(R.id.btn_student_login);
        btnLoginAsStudent.setText(Html.fromHtml("<p>Sei uno studente?\n<u><i>Clicca qui!</i></u></p>", 0));

        Objects.requireNonNull(txtLayoutUsername.getEditText()).addTextChangedListener(onTextChange(txtLayoutUsername));
        Objects.requireNonNull(txtLayoutPassword.getEditText()).addTextChangedListener(onTextChange(txtLayoutPassword));
        findViewById(R.id.login_btn_settings).setOnClickListener(this::btnSettingOnClick);
        btnLogin.setOnClickListener(this::btnLoginOnClick);
        btnLoginAsStudent.setOnClickListener(this::btnLoginAsStudentOnClick);

        checkDisplayMetrics();
        checkForUpdateChangelog();

        GlobalVariables.gsThread.addTask(() -> {
            try {
                if (!GiuaScraper.isGoogleLoginAvailable())
                    runOnUiThread(() -> btnLoginAsStudent.setVisibility(View.INVISIBLE));
            } catch (Exception ignored) {
            }
        });

        new Thread(() -> {
            try {
                String str = GiuaScraper.getSchoolName();
                runOnUiThread(() -> txtCardTitle.setText("Accesso a " + str));
            } catch (Exception e) {
                runOnUiThread(() -> txtCardTitle.setText("Accesso al registro"));
            }
        }).start();

        if (isAddingAccount) {
            btnLogin.setText("Aggiungi account");
            chRememberCredentials.setChecked(true);
            chRememberCredentials.setVisibility(View.INVISIBLE);
        }

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

    private void checkForUpdateChangelog() {
        if (!AppData.getAppVersion(this).equals("")
                && !AppData.getAppVersion(this).equals(BuildConfig.VERSION_NAME)) {

            loggerManager.w("Aggiornamento installato rilevato");
            loggerManager.d("Cancello apk dell'aggiornamento e mostro changelog");
            new Analytics.Builder(Analytics.APP_UPDATED)
                    .addCustomValue("new_ver", BuildConfig.VERSION_NAME)
                    .addCustomValue("old_ver", AppData.getAppVersion(this)).send();
            AppUpdateManager upd = new AppUpdateManager(LoginActivity.this);
            upd.deleteOldApk();
            new Thread(upd::showDialogReleaseChangelog).start();
        }
        AppData.saveAppVersion(this, BuildConfig.VERSION_NAME);
    }

    private void login() {
        if (GlobalVariables.gsThread == null || GlobalVariables.gsThread.isInterrupted())
            GlobalVariables.gsThread = new GiuaScraperThread();
        GlobalVariables.gsThread.addTask(() -> {
            try {
                if (isAddingAccount && AppData.getAllAccountUsernames(this).contains(etUsername.getText().toString().toLowerCase())) {
                    runOnUiThread(() -> {
                        txtLayoutUsername.setError("Username già salvato");
                        etPassword.setText("");
                        pgProgressBar.setVisibility(View.INVISIBLE);
                        btnLogin.setVisibility(View.VISIBLE);
                    });
                    return;
                }
                loggerManager.d("Eseguo login...");
                GlobalVariables.gS = new GiuaScraper(etUsername.getText().toString(), etPassword.getText().toString(), true, SettingsData.getSettingBoolean(this, SettingKey.DEMO_MODE), new LoggerManager("GiuaScraper", this));
                GlobalVariables.gS.login();
                loggerManager.d("Devo ricordare le credenziali? " + chRememberCredentials.isChecked());

                if (GlobalVariables.gS.isSessionValid(GlobalVariables.gS.getCookie())) {
                    if (chRememberCredentials.isChecked()) {
                        AccountData.setCredentials(this, etUsername.getText().toString(), etPassword.getText().toString(), GlobalVariables.gS.getCookie(), GlobalVariables.gS.getUserTypeString(), GlobalVariables.gS.getProfilePage(false).getProfileInformation()[2]);
                        AccountData.setSiteUrl(this, etUsername.getText().toString(), GlobalVariables.gS.getSiteUrl());
                        AppData.addAccountUsername(this, etUsername.getText().toString().toLowerCase());
                        AppData.saveActiveUsername(this, etUsername.getText().toString().toLowerCase());
                    }
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
                try {
                    Maintenance maintenance = GlobalVariables.gS.getMaintenanceInfo();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.ITALY);
                    runOnUiThread(() -> setErrorMessage("Il sito è in manutenzione sino alle " + simpleDateFormat.format(maintenance.end)));
                } catch (Exception e2) {
                    runOnUiThread(() -> setErrorMessage(getString(R.string.maintenance_is_active_error)));
                }
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
        });
    }

    private void startStudentLoginActivity() {
        loggerManager.d("Avvio StudenteLoginActivity");
        startActivity(new Intent(LoginActivity.this, StudentLoginActivity.class).putExtra("sender", "LoginActivity"));
        finish();
    }

    private void startDrawerActivity() {
        loggerManager.d("Avvio DrawerActivity");
        new Analytics.Builder(Analytics.LOG_IN)
                .addCustomValue("account_type", GlobalVariables.gS.getUserTypeString()).send();
        Intent intent = new Intent(LoginActivity.this, DrawerActivity.class);
        startActivity(intent);
        finish();
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

        if (etPassword.getText().length() > 0 && etUsername.getText().length() > 0) {
            pgProgressBar.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.INVISIBLE);
            AppUtils.hideKeyboard(this, view);
            login();
        }
    }

    private void btnLoginAsStudentOnClick(View view) {
        loggerManager.d("Login Studente richiesto dall'utente");
        startStudentLoginActivity();
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
        if (!isAddingAccount) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            GlobalVariables.gsThread.addTask(() -> {
                String username = AppData.getActiveUsername(this);
                GlobalVariables.gS = new GiuaScraper(username, AccountData.getPassword(this, username), AccountData.getCookie(this, username), true, SettingsData.getSettingBoolean(this, SettingKey.DEMO_MODE), new LoggerManager("GiuaScraper", this));
                GlobalVariables.gS.login();
                runOnUiThread(this::startDrawerActivity);
            });
        }
    }
}

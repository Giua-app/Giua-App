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

package com.giua.app.ui.activities.AccountsActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.github.dhaval2404.colorpicker.model.ColorShape;
import com.giua.app.AccountData;
import com.giua.app.AppData;
import com.giua.app.AppUtils;
import com.giua.app.GlobalVariables;
import com.giua.app.LoggerManager;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.app.ui.activities.AutomaticLoginActivity;
import com.giua.app.ui.activities.StudentLoginActivity;
import com.giua.app.ui.views.SwipeView;
import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Set;
import java.util.regex.Pattern;

public class AccountsActivity extends AppCompatActivity {

    LinearLayout layoutAllAccounts;
    AccountCard lastClickedAccountCard; //Serve per avere un riferimento della carta quando si cambia il tema dell'account
    FloatingActionMenu btnAddAccountMenu;
    FloatingActionButton btnAddNormalAccount;
    FloatingActionButton btnAddStudentAccount;

    SwipeView swipeView;
    LinearLayout layoutManageAccount;
    LinearLayout layoutAddAccount;

    ImageView ivManageAccountDelete;
    TextView tvManageAccountUsername;
    TextView tvManageAccountEmail;
    EditText etManageAccountUrl;
    View dotColorPreview;

    TextView tvAddAccountSave;
    TextInputLayout tilAddAccountUsername;
    TextInputLayout tilAddAccountPassword;
    TextInputLayout tilAddAccountUrl;
    EditText etAddAccountUsername;
    EditText etAddAccountPassword;
    EditText etAddAccountUrl;

    View mainLayout;

    LinearLayout.LayoutParams accountCardParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    LoggerManager loggerManager;
    String goTo = "";
    String lastUserShowed = ""; //L'ultimo utente cliccato per cui si è mostrata la swipeView

    boolean isChooserMode = false; //La chooser mode indica che l'utente sta scegeliendo un account con cui fare il login
    boolean isChangedSomething = false; //Indica se è stato modificato qualcosa

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        isChooserMode = getIntent().getBooleanExtra("account_chooser_mode", false);
        goTo = getIntent().getStringExtra("goTo");
        if (goTo == null)
            goTo = "";

        mainLayout = findViewById(R.id.activity_accounts_main);
        layoutAllAccounts = findViewById(R.id.activity_accounts_layout);
        swipeView = findViewById(R.id.accounts_swipe_view);

        layoutManageAccount = findViewById(R.id.accounts_manage_account_layout);
        ivManageAccountDelete = findViewById(R.id.accounts_manage_account_delete);
        tvManageAccountUsername = findViewById(R.id.accounts_manage_account_username);
        tvManageAccountEmail = findViewById(R.id.accounts_manage_account_email);
        etManageAccountUrl = findViewById(R.id.accounts_manage_account_url);
        dotColorPreview = findViewById(R.id.accounts_manage_account_dot_colored);

        layoutAddAccount = findViewById(R.id.accounts_add_account_layout);
        btnAddAccountMenu = findViewById(R.id.accounts_add_account_button_menu);
        btnAddNormalAccount = findViewById(R.id.accounts_add_account_button);
        btnAddStudentAccount = findViewById(R.id.accounts_add_account_student_button);
        tvAddAccountSave = findViewById(R.id.accounts_add_account_save_text);
        tilAddAccountUsername = findViewById(R.id.accounts_add_account_username);
        tilAddAccountPassword = findViewById(R.id.accounts_add_account_password);
        tilAddAccountUrl = findViewById(R.id.accounts_add_account_url);
        etAddAccountUsername = tilAddAccountUsername.getEditText();
        etAddAccountPassword = tilAddAccountPassword.getEditText();
        etAddAccountUrl = tilAddAccountUrl.getEditText();

        if (isChooserMode) btnAddAccountMenu.setVisibility(View.GONE);

        if (AppData.getAllAccountUsernames(this).contains("gsuite"))
            btnAddStudentAccount.setVisibility(View.GONE);
        else
            checkGoogleLoginAvailability();

        accountCardParams.leftMargin = AppUtils.convertDpToPx(10, this);
        accountCardParams.rightMargin = AppUtils.convertDpToPx(10, this);
        accountCardParams.topMargin = AppUtils.convertDpToPx(10, this);

        loggerManager = new LoggerManager("AccountsActivity", this);

        setupToolBar();
        setupListeners();
    }

    private void setupListeners() {
        View swipeViewColorLayout = findViewById(R.id.accounts_swipe_view_color_layout);

        etManageAccountUrl.setOnFocusChangeListener(this::adaptSwipeViewSizeOnFocusChange);
        etAddAccountUsername.setOnFocusChangeListener(this::adaptSwipeViewSizeOnFocusChange);
        etAddAccountPassword.setOnFocusChangeListener(this::adaptSwipeViewSizeOnFocusChange);
        etAddAccountUrl.setOnFocusChangeListener(this::adaptSwipeViewSizeOnFocusChange);
        swipeView.setOnTouchRelease(this::onSwipeViewTouchRelease);
        swipeView.setOnMove(this::onSwipeViewMove);
        ivManageAccountDelete.setOnClickListener(this::ivSwipeViewDeleteOnClick);
        btnAddNormalAccount.setOnClickListener(this::btnAddNormalAccountOnClick);
        btnAddStudentAccount.setOnClickListener(this::btnAddStudentAccountOnClick);
        swipeViewColorLayout.setOnClickListener(this::swipeViewColorLayoutOnClick);
        tvAddAccountSave.setOnClickListener(this::btnAddAccountSaveOnClick);
        etAddAccountUsername.addTextChangedListener(onEditTextChanged(tilAddAccountUsername));
        etAddAccountPassword.addTextChangedListener(onEditTextChanged(tilAddAccountPassword));
    }

    private void checkGoogleLoginAvailability() {
        GlobalVariables.gsThread.addTask(() -> {
            try {
                if (!GiuaScraper.isGoogleLoginAvailable())
                    runOnUiThread(() -> btnAddStudentAccount.setVisibility(View.INVISIBLE));
            } catch (Exception ignored) {
            }
        });
    }

    private void addAccountCardsToLayout() {
        Set<String> allUsernames = AppData.getAllAccountUsernames(this);

        layoutAllAccounts.removeAllViews();

        for (String username : allUsernames) {
            String type = AccountData.getUserType(this, username);
            int color = AccountData.getTheme(this, username);
            String email = AccountData.getEmail(this, username);
            AccountCard accountCard = new AccountCard(this, username, email, type, color);

            if (isChooserMode)
                accountCard.setOnClickListener(this::onAccountCardClickWhileChoosing);
            else
                accountCard.setOnClickListener(this::onAccountCardClick);

            accountCard.setLayoutParams(accountCardParams);

            layoutAllAccounts.addView(accountCard);
        }

    }

    private void addNewAccountToLayout(String username, String password, String email, @ColorInt int color, String siteUrl) {
        AccountCard accountCard = new AccountCard(this, username, email, "", color);
        View pbAccountCard = accountCard.findViewById(R.id.view_account_card_pb);

        pbAccountCard.setVisibility(View.VISIBLE);
        accountCard.setLayoutParams(accountCardParams);
        accountCard.setOnClickListener(this::onAccountCardClick);

        layoutAllAccounts.addView(accountCard);

        new Thread(() -> {
            GiuaScraper gS = new GiuaScraper(username, password, loggerManager);
            gS.setPrivateSiteUrl(siteUrl);
            try {
                gS.login();

                runOnUiThread(() -> {
                    pbAccountCard.setVisibility(View.INVISIBLE);
                    AccountData.setCredentials(this, username, password, gS.getCookie(), gS.getUserTypeString(), email);
                    AccountData.setSiteUrl(this, username, siteUrl);
                    AppData.addAccountUsername(this, username);
                });
            } catch (GiuaScraperExceptions.YourConnectionProblems | GiuaScraperExceptions.SiteConnectionProblems e) {
                runOnUiThread(() -> {
                    layoutAllAccounts.removeView(accountCard);
                    setErrorMessage("Problema di connessione", layoutAllAccounts);
                });
            } catch (GiuaScraperExceptions.SessionCookieEmpty | GiuaScraperExceptions.UnableToLogin e) {
                runOnUiThread(() -> {
                    layoutAllAccounts.removeView(accountCard);
                    setErrorMessage("Credenziali non valide", layoutAllAccounts);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    layoutAllAccounts.removeView(accountCard);
                    setErrorMessage("Impossibile aggiungere l'account. Riprova più tardi.", layoutAllAccounts);
                });
            }
        }).start();
    }

    private void onSwipeViewMove(SwipeView swipeView, SwipeView.Operation operation) {
        if (operation == SwipeView.Operation.SHOW_START_FROM_BOTTOM)
            btnAddAccountMenu.setVisibility(View.INVISIBLE);
        if (operation == SwipeView.Operation.HIDE_ALL_FROM_Y) {
            btnAddAccountMenu.setVisibility(View.VISIBLE);
            lastUserShowed = "";
            AppUtils.hideKeyboard(this, swipeView);
        }
    }

    private TextWatcher onEditTextChanged(final TextInputLayout view) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                //Elimina gli errori mentre si sta scrivendo nella view
                if (view.getId() == tilAddAccountUsername.getId()) {
                    tilAddAccountUsername.setError(null);
                    tilAddAccountUsername.setErrorEnabled(false);
                } else if (view.getId() == tilAddAccountPassword.getId()) {
                    tilAddAccountPassword.setError(null);
                    tilAddAccountPassword.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    private void btnAddAccountSaveOnClick(View view) {
        String username = etAddAccountUsername.getText().toString();
        String password = etAddAccountPassword.getText().toString();
        String siteUrl = etAddAccountUrl.getText().toString();

        if (username.equals(""))
            tilAddAccountUsername.setError("Il nome utente non può essere vuoto");

        if (password.equals(""))
            tilAddAccountPassword.setError("La password non può essere vuota");

        if (!Pattern.matches("https?://([a-zA-Z0-9]+[.])+([a-zA-Z0-9]+)(:[0-9]+)?((/[a-zA-Z0-9-_]+)+)?", siteUrl))
            tilAddAccountUrl.setError("Sito non valido");

        if (username.equals("") || password.equals("") || !Pattern.matches("https?://([a-zA-Z0-9]+[.])+([a-zA-Z0-9]+)(:[0-9]+)?((/[a-zA-Z0-9-_]+)+)?", siteUrl))
            return;

        addNewAccountToLayout(username, password, AccountData.getEmail(this, username), AccountData.getTheme(this, username), siteUrl);
        swipeView.hideAllFromY();
        AppUtils.hideKeyboard(this, view);
        isChangedSomething = true;
    }

    private void btnAddStudentAccountOnClick(View view) {
        startActivity(new Intent(this, StudentLoginActivity.class)
                .putExtra("requested_from_accounts_activity", true)
                .putExtra("sender", "AccountsActivity"));
        isChangedSomething = true;
    }

    private void btnAddNormalAccountOnClick(View view) {
        String defaultUrl = SettingsData.getSettingString(this, SettingKey.DEFAULT_URL);

        layoutManageAccount.setVisibility(View.GONE);
        layoutAddAccount.setVisibility(View.VISIBLE);
        etAddAccountUsername.setText("");
        etAddAccountPassword.setText("");
        etAddAccountUrl.setText(defaultUrl.equals("") ? GiuaScraper.getGlobalSiteUrl() : defaultUrl);
        swipeView.setMaxHeight(AppUtils.convertDpToPx(300f, this));
        swipeView.setStartHeight(AppUtils.convertDpToPx(300f, this));
        swipeView.show();
    }

    private void ivSwipeViewDeleteOnClick(View view) {
        new AlertDialog.Builder(AccountsActivity.this)
                .setTitle("Attenzione")
                .setMessage("Sei sicuro di voler eliminare l'account " + lastClickedAccountCard.username + " ?")
                .setPositiveButton("Si", (dialogInterface, i) -> {
                    if (lastClickedAccountCard.username.equals("gsuite")) {
                        AppUtils.clearWebViewCookies();
                        btnAddStudentAccount.setVisibility(View.VISIBLE);
                    }

                    AppData.removeAccountUsername(this, lastClickedAccountCard.username);
                    AccountData.removeAccount(this, lastClickedAccountCard.username);

                    Set<String> allUsernames = AppData.getAllAccountUsernames(this);

                    if (AppData.getActiveUsername(this).equals(lastClickedAccountCard.username) && allUsernames.size() > 0)
                        AppData.saveActiveUsername(this, allUsernames.toArray()[0].toString());
                    else
                        AppData.saveActiveUsername(this, "");

                    addAccountCardsToLayout();
                    swipeView.hideAllFromY();
                    isChangedSomething = true;
                })
                .setNeutralButton("Annulla", null)
                .show();
    }

    private void adaptSwipeViewSizeOnFocusChange(View view, boolean focused) {
        if (focused) {
            swipeView.setMaxHeight(swipeView.getMaxScreenHeight() - AppUtils.convertDpToPx(70f, this));
            swipeView.setStartHeight(AppUtils.convertDpToPx(250f, this));
            swipeView.showAllFromY();
        }
    }

    private void onSwipeViewTouchRelease(SwipeView swipeView) {
        boolean isAddingAccount = layoutManageAccount.getVisibility() == View.GONE;
        float hideOffset;

        if (isAddingAccount)
            hideOffset = swipeView.getMaxScreenHeight() / 2;
        else
            hideOffset = AppUtils.convertDpToPx(50, this);

        if (swipeView.getY() < swipeView.getStartHeightAsY() - hideOffset)
            swipeView.showAllFromY();
        else {
            swipeView.hideAllFromY();
            etManageAccountUrl.clearFocus();
            etAddAccountUsername.clearFocus();
            etAddAccountPassword.clearFocus();
            etAddAccountUrl.clearFocus();

            if (isAddingAccount) return;

            //Controllo se l'URL è valido
            if (Pattern.matches("https?://([a-zA-Z0-9]+[.])+([a-zA-Z0-9]+)(:[0-9]+)?((/[a-zA-Z0-9-_]+)+)?", etManageAccountUrl.getText())) {
                AccountData.setSiteUrl(this, lastClickedAccountCard.username, etManageAccountUrl.getText().toString());
                isChangedSomething = true;
            } else {
                etManageAccountUrl.setText(AccountData.getSiteUrl(this, lastClickedAccountCard.username));
                setErrorMessage("Sito non valido", swipeView);
            }

        }
    }

    private void onAccountCardClick(View view) {
        AccountCard accountCard = (AccountCard) view;

        if (lastUserShowed.equals(accountCard.username)) return;

        if (accountCard.email.equals(""))
            tvManageAccountEmail.setVisibility(View.GONE);
        else
            tvManageAccountEmail.setVisibility(View.VISIBLE);

        tvManageAccountUsername.setText(accountCard.username);
        tvManageAccountEmail.setText(accountCard.email);
        etManageAccountUrl.setText(AccountData.getSiteUrl(this, accountCard.username));
        dotColorPreview.setBackgroundTintList(ColorStateList.valueOf(AccountData.getTheme(this, accountCard.username)));
        layoutAddAccount.setVisibility(View.GONE);
        layoutManageAccount.setVisibility(View.VISIBLE);
        swipeView.setMaxHeight(AppUtils.convertDpToPx(300f, this));
        swipeView.setStartHeight(AppUtils.convertDpToPx(140f, this));
        swipeView.show();
        lastClickedAccountCard = accountCard;
        lastUserShowed = accountCard.username;
    }

    private void onAccountCardClickWhileChoosing(View view) {
        AccountCard accountCard = (AccountCard) view;
        AppData.saveActiveUsername(this, accountCard.username);
        startAutomaticLogin();
    }

    private void swipeViewColorLayoutOnClick(View view) {
        new ColorPickerDialog.Builder(this).setDefaultColor(AccountData.getTheme(this, lastClickedAccountCard.username))
                .setTitle("Scegli un colore")
                .setColorShape(ColorShape.CIRCLE)
                .setNegativeButton("Annulla")
                .setColorListener((color, colorHex) -> {
                    dotColorPreview.setBackgroundTintList(ColorStateList.valueOf(color));
                    AccountData.setTheme(view.getContext(), lastClickedAccountCard.username, color);
                    isChangedSomething = true;
                    addAccountCardsToLayout();
                }).show();
    }

    private void setupToolBar() {
        Toolbar toolbar = findViewById(R.id.activity_accounts_toolbar);
        toolbar.setTitle(isChooserMode ? "Selezione account" : "Gestione account");
        setSupportActionBar(toolbar);
        if (!isChooserMode) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    private void startAutomaticLogin() {
        startActivity(new Intent(AccountsActivity.this, AutomaticLoginActivity.class));
        finish();
    }

    private void setErrorMessage(String message, View root) {
        Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        addAccountCardsToLayout();
        if (AppData.getAllAccountUsernames(this).contains("gsuite"))
            btnAddStudentAccount.setVisibility(View.GONE);
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != android.R.id.home) return super.onOptionsItemSelected(item);

        if (isChangedSomething)
            startAutomaticLogin();
        else {
            swipeView.setVisibility(View.INVISIBLE);
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (swipeView.isHidden()) {

            if (!isChangedSomething)
                super.onBackPressed();
            else
                startAutomaticLogin();
        } else
            swipeView.hideAllFromY();
    }
}

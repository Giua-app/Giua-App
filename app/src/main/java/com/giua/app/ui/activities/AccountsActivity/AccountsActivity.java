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

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.github.dhaval2404.colorpicker.model.ColorShape;
import com.giua.app.AccountData;
import com.giua.app.AppData;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.app.ui.activities.AutomaticLoginActivity;
import com.giua.app.ui.activities.DrawerActivity;
import com.giua.app.ui.views.SwipeView;
import com.giua.webscraper.GiuaScraper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Set;
import java.util.regex.Pattern;

public class AccountsActivity extends AppCompatActivity {

    GridLayout gridLayout;
    AccountCard lastClickedAccountCard; //Serve per avere un riferimento della carta quando si cambia il tema dell'account
    FloatingActionButton btnAddAccount;

    SwipeView swipeView;
    LinearLayout layoutManageAccount;
    LinearLayout layoutAddAccount;

    TextView tvManageAccountUsername;
    ImageView ivManageAccountDelete;
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

    String goTo = "";
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

        gridLayout = findViewById(R.id.activity_accounts_table);
        swipeView = findViewById(R.id.accounts_swipe_view);

        layoutManageAccount = findViewById(R.id.accounts_manage_account_layout);
        ivManageAccountDelete = findViewById(R.id.accounts_manage_account_delete);
        tvManageAccountUsername = findViewById(R.id.accounts_manage_account_username);
        etManageAccountUrl = findViewById(R.id.accounts_manage_account_url);
        dotColorPreview = findViewById(R.id.accounts_manage_account_dot_colored);
        View swipeViewColorLayout = findViewById(R.id.accounts_swipe_view_color_layout);

        layoutAddAccount = findViewById(R.id.accounts_add_account_layout);
        btnAddAccount = findViewById(R.id.accounts_add_account_button);
        tvAddAccountSave = findViewById(R.id.accounts_add_account_save_text);
        tilAddAccountUsername = findViewById(R.id.accounts_add_account_username);
        tilAddAccountPassword = findViewById(R.id.accounts_add_account_password);
        tilAddAccountUrl = findViewById(R.id.accounts_add_account_url);
        etAddAccountUsername = tilAddAccountUsername.getEditText();
        etAddAccountPassword = tilAddAccountPassword.getEditText();
        etAddAccountUrl = tilAddAccountUrl.getEditText();

        mainLayout = findViewById(R.id.activity_accounts_main);

        Set<String> allUsernames = AppData.getAllAccountUsernames(this);

        setupToolBar();

        etManageAccountUrl.setOnFocusChangeListener(this::adaptSwipeViewSizeOnFocusChange);
        etAddAccountUsername.setOnFocusChangeListener(this::adaptSwipeViewSizeOnFocusChange);
        etAddAccountPassword.setOnFocusChangeListener(this::adaptSwipeViewSizeOnFocusChange);
        etAddAccountUrl.setOnFocusChangeListener(this::adaptSwipeViewSizeOnFocusChange);
        swipeView.setOnTouchRelease(this::onSwipeViewTouchRelease);
        swipeView.setOnMove(this::onSwipeViewMove);
        ivManageAccountDelete.setOnClickListener(this::ivSwipeViewDeleteOnClick);
        btnAddAccount.setOnClickListener(this::btnAddAccountOnClick);
        swipeViewColorLayout.setOnClickListener(this::swipeViewColorLayoutOnClick);
        tvAddAccountSave.setOnClickListener(this::btnAddAccountSaveOnClick);
        etAddAccountUsername.addTextChangedListener(onEditTextChanged(tilAddAccountUsername));
        etAddAccountPassword.addTextChangedListener(onEditTextChanged(tilAddAccountPassword));

        addAccountCardsToLayout(allUsernames);
    }

    private void addAccountCardsToLayout(Set<String> allUsernames) {
        gridLayout.removeAllViews();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.leftMargin = convertDpToPx(10);

        boolean isFirstElementInRow = true;
        TableRow tableRow = new TableRow(this);
        for (String username : allUsernames) {
            String type = AccountData.getUserType(this, username);
            int color = AccountData.getTheme(this, username);

            if (isFirstElementInRow)
                tableRow = new TableRow(this);

            AccountCard accountCard = new AccountCard(this, username, type, color);

            if (isChooserMode)
                accountCard.setOnClickListener(this::onAccountCardClickWhileChoosing);
            else
                accountCard.setOnClickListener(this::onAccountCardClick);

            tableRow.addView(accountCard);

            if (!isFirstElementInRow)
                gridLayout.addView(tableRow);

            isFirstElementInRow = !isFirstElementInRow;
        }

        if (!isFirstElementInRow)
            gridLayout.addView(tableRow);
    }

    private void onSwipeViewMove(SwipeView swipeView, SwipeView.Operation operation) {
        if (operation == SwipeView.Operation.SHOW_START_FROM_BOTTOM)
            btnAddAccount.setVisibility(View.INVISIBLE);
        if (operation == SwipeView.Operation.HIDE_ALL_FROM_Y)
            btnAddAccount.setVisibility(View.VISIBLE);
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


        if (username.equals(""))
            tilAddAccountUsername.setError("Il nome utente non può essere vuoto");
        if (password.equals("")) tilAddAccountPassword.setError("La password non può essere vuota");
        if (username.equals("") || password.equals("")) return;

        AccountData.setCredentials(this, username, password);
        AccountData.setSiteUrl(this, username, etAddAccountUrl.getText().toString());
        AppData.addAccountUsername(this, username);
        swipeView.hideAllFromY();
        addAccountCardsToLayout(AppData.getAllAccountUsernames(this));
        isChangedSomething = true;
    }

    private void setupToolBar() {
        Toolbar toolbar = findViewById(R.id.activity_accounts_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(isChooserMode ? "Selezione account" : "Gestione account");
        if (!isChooserMode) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    private void btnAddAccountOnClick(View view) {
        String defaultUrl = SettingsData.getSettingString(this, SettingKey.DEFAULT_URL);

        layoutManageAccount.setVisibility(View.GONE);
        layoutAddAccount.setVisibility(View.VISIBLE);
        etAddAccountUsername.setText("");
        etAddAccountPassword.setText("");
        etAddAccountUrl.setText(defaultUrl.equals("") ? GiuaScraper.getSiteURL() : defaultUrl);
        swipeView.show();
    }

    private void ivSwipeViewDeleteOnClick(View view) {
        new AlertDialog.Builder(AccountsActivity.this)
                .setTitle("Attenzione")
                .setMessage("Sei sicuro di voler eliminare l'account " + lastClickedAccountCard.username + " ?")
                .setPositiveButton("Si", (dialogInterface, i) -> {
                    AppData.removeAccountUsername(this, lastClickedAccountCard.username);
                    AccountData.removeAccount(this, lastClickedAccountCard.username);
                    addAccountCardsToLayout(AppData.getAllAccountUsernames(this));
                    swipeView.hideAllFromY();
                    isChangedSomething = true;
                })
                .setNeutralButton("Annulla", null)
                .show();
    }

    private void adaptSwipeViewSizeOnFocusChange(View view, boolean focused) {
        if (focused)
            swipeView.showAllFromY();
        else if (swipeView.isHidden())
            swipeView.showStart();
    }

    private void onSwipeViewTouchRelease(SwipeView swipeView) {
        if (swipeView.getY() < swipeView.startHeight - convertDpToPx(50))
            swipeView.showAllFromY();
        else {
            swipeView.hideAllFromY();
            etManageAccountUrl.clearFocus();
            etAddAccountUsername.clearFocus();
            etAddAccountPassword.clearFocus();
            etAddAccountUrl.clearFocus();

            //Non continuare se si stava aggiungendo un account
            if (layoutManageAccount.getVisibility() == View.GONE) return;

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
        tvManageAccountUsername.setText(accountCard.username);
        etManageAccountUrl.setText(AccountData.getSiteUrl(this, accountCard.username));
        dotColorPreview.setBackgroundTintList(ColorStateList.valueOf(AccountData.getTheme(this, accountCard.username)));
        layoutAddAccount.setVisibility(View.GONE);
        layoutManageAccount.setVisibility(View.VISIBLE);
        swipeView.show();
        lastClickedAccountCard = accountCard;
    }

    private void onAccountCardClickWhileChoosing(View view) {
        AccountCard accountCard = (AccountCard) view;
        AppData.saveActiveUsername(this, accountCard.username);
        startAutomaticLogin();
    }

    private void swipeViewColorLayoutOnClick(View view) {
        Context context = this;

        new ColorPickerDialog.Builder(this).setDefaultColor(AccountData.getTheme(this, lastClickedAccountCard.username))
                .setTitle("Scegli un colore")
                .setColorShape(ColorShape.CIRCLE)
                .setNegativeButton("Annulla")
                .setColorListener((color, colorHex) -> {
                    dotColorPreview.setBackgroundTintList(ColorStateList.valueOf(color));
                    AccountData.setTheme(view.getContext(), lastClickedAccountCard.username, color);
                    isChangedSomething = true;
                    addAccountCardsToLayout(AppData.getAllAccountUsernames(context));
                }).show();
    }

    private void startAutomaticLogin() {
        startActivity(new Intent(AccountsActivity.this, AutomaticLoginActivity.class).putExtra("goTo", goTo));
        finish();
    }

    private void setErrorMessage(String message, View root) {
        Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    private int convertDpToPx(float dp) {
        //https://stackoverflow.com/questions/4605527/converting-pixels-to-dp
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != android.R.id.home) return super.onOptionsItemSelected(item);

        if (isChangedSomething) {
            startActivity(new Intent(this, DrawerActivity.class));
            finish();
        } else {
            swipeView.setVisibility(View.INVISIBLE);
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (swipeView.isHidden())
            super.onBackPressed();
        else
            swipeView.hideAllFromY();
    }
}

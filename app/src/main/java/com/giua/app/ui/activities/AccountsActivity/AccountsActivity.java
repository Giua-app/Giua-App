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
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.giua.app.AccountData;
import com.giua.app.AppData;
import com.giua.app.R;
import com.giua.app.ui.activities.AutomaticLoginActivity;
import com.giua.app.ui.views.SwipeView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Set;
import java.util.regex.Pattern;

import top.defaults.colorpicker.ColorPickerPopup;

public class AccountsActivity extends AppCompatActivity {

    SwipeView swipeView;
    TextView tvSwipeViewUsername;
    ImageView ivSwipeViewDelete;
    EditText etSwipeViewUrl;
    View colorPreview;
    TableLayout tableLayout;
    boolean accountChooserMode = false;
    String goTo = "";
    AccountCard lastClickedAccountCard; //Serve per avere un riferimento della carta quando si cambia il tema dell'account

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        accountChooserMode = getIntent().getBooleanExtra("account_chooser_mode", false);
        goTo = getIntent().getStringExtra("goTo");
        if (goTo == null)
            goTo = "";

        swipeView = findViewById(R.id.accounts_swipe_view);
        ivSwipeViewDelete = findViewById(R.id.accounts_swipe_view_delete);
        tvSwipeViewUsername = findViewById(R.id.accounts_swipe_view_username);
        etSwipeViewUrl = findViewById(R.id.accounts_swipe_view_url);
        colorPreview = findViewById(R.id.accounts_swipe_view_color);
        tableLayout = findViewById(R.id.activity_accounts_table);

        Set<String> allUsernames = AppData.getAllAccountUsernames(this);

        Toolbar toolbar = findViewById(R.id.activity_accounts_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(accountChooserMode ? "Selezione account" : "Gestione account");
        if (!accountChooserMode) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        swipeView.setOnTouchRelease(this::onSwipeViewTouchReleased);
        etSwipeViewUrl.setOnFocusChangeListener(this::etSwipeViewUrlOnFocusChange);
        ivSwipeViewDelete.setOnClickListener(this::ivSwipeViewDelete);

        findViewById(R.id.accounts_swipe_view_color_layout).setOnClickListener(
                (view) -> new ColorPickerPopup.Builder(this).initialColor(AccountData.getTheme(this, lastClickedAccountCard.username))
                        .enableAlpha(false)
                        .enableBrightness(false)
                        .okTitle("Conferma")
                        .cancelTitle("Annulla")
                        .showIndicator(true)
                        .showValue(false)
                        .build()
                        .show(view, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void
                            onColorPicked(int color) {
                                colorPreview.setBackgroundTintList(ColorStateList.valueOf(color));
                                AccountData.setTheme(view.getContext(), lastClickedAccountCard.username, color);
                                addAccountCardsToLayout(allUsernames);
                            }
                        })
        );

        addAccountCardsToLayout(allUsernames);
    }

    private void ivSwipeViewDelete(View view) {
        AlertDialog dialog = new AlertDialog.Builder(AccountsActivity.this)
                .setTitle(R.string.customactivityoncrash_error_activity_error_details_title)
                .setMessage("Sei sicuro di voler eliminare l'account " + lastClickedAccountCard.username + " ?")
                .setPositiveButton(R.string.customactivityoncrash_error_activity_error_details_close, null)
                .setNeutralButton(R.string.customactivityoncrash_error_activity_error_details_copy, null)
                .show();
    }

    private void etSwipeViewUrlOnFocusChange(View view, boolean focused) {
        if (focused)
            swipeView.showAllFromY();
    }

    private void addAccountCardsToLayout(Set<String> allUsernames) {
        tableLayout.removeAllViews();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.leftMargin = convertDpToPx(10);

        boolean isFirstElementInRow = true;
        TableRow tableRow = new TableRow(this);
        for (String username : allUsernames) {
            String type = AccountData.getUserType(this, username);
            int color = AccountData.getTheme(this, username);

            if (isFirstElementInRow) {
                tableRow = new TableRow(this);
            }

            AccountCard accountCard = new AccountCard(this, username, type, color);


            if (accountChooserMode)
                accountCard.setOnClickListener(this::onAccountCardClickWhileChoosing);
            else
                accountCard.setOnClickListener(this::onAccountCardClick);

            tableRow.addView(accountCard);

            if (!isFirstElementInRow)
                tableLayout.addView(tableRow);

            isFirstElementInRow = !isFirstElementInRow;
        }

        if (!isFirstElementInRow)
            tableLayout.addView(tableRow);
    }

    private void onSwipeViewTouchReleased(SwipeView swipeView) {
        if (swipeView.getY() < swipeView.startHeight - convertDpToPx(50))
            swipeView.showAllFromY();
        else {
            swipeView.hideAllFromY();
            etSwipeViewUrl.clearFocus();
            //Controllo se l'URL è valido
            if (Pattern.matches("https?://([a-zA-Z0-9]+[.])+([a-zA-Z0-9]+)(:[0-9]+)?((/[a-zA-Z0-9-_]+)+)?", etSwipeViewUrl.getText())) {
                AccountData.setSiteUrl(this, lastClickedAccountCard.username, etSwipeViewUrl.getText().toString());

            } else {
                etSwipeViewUrl.setText(AccountData.getSiteUrl(this, lastClickedAccountCard.username));
                setErrorMessage("Sito non valido", swipeView);
            }

        }
    }

    private void onAccountCardClick(View view) {
        AccountCard accountCard = (AccountCard) view;
        tvSwipeViewUsername.setText(accountCard.username);
        etSwipeViewUrl.setText(AccountData.getSiteUrl(this, accountCard.username));
        colorPreview.setBackgroundTintList(ColorStateList.valueOf(AccountData.getTheme(this, accountCard.username)));
        swipeView.show();
        lastClickedAccountCard = accountCard;
    }

    private void onAccountCardClickWhileChoosing(View view) {
        AccountCard accountCard = (AccountCard) view;
        AppData.saveActiveUsername(this, accountCard.username);
        startAutomaticLogin();
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
}

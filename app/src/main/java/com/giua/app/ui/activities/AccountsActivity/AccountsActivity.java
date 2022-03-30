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

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.giua.app.AccountData;
import com.giua.app.AppData;
import com.giua.app.GlobalVariables;
import com.giua.app.R;
import com.giua.app.ui.views.SwipeView;

import java.util.Set;

import top.defaults.colorpicker.ColorPickerPopup;

public class AccountsActivity extends AppCompatActivity {

    SwipeView swipeView;
    TextView tvswipeViewUsername;
    EditText etswipeViewUrl;
    View colorPreview;
    GridLayout gridLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        swipeView = findViewById(R.id.accounts_swipe_view);
        tvswipeViewUsername = findViewById(R.id.accounts_swipe_view_username);
        etswipeViewUrl = findViewById(R.id.accounts_swipe_view_url);
        colorPreview = findViewById(R.id.accounts_swipe_view_color);
        gridLayout = findViewById(R.id.activity_accounts_grid);

        Set<String> allUsernames = AppData.getAllAccountUsernames(this);

        Toolbar toolbar = findViewById(R.id.activity_accounts_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        swipeView.setOnTouchRelease(this::onSwipeViewTouchReleased);

        findViewById(R.id.accounts_swipe_view_color_layout).setOnClickListener(
                (view) -> new ColorPickerPopup.Builder(this).initialColor(AccountData.getTheme(this, GlobalVariables.gS.getUser()))
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
                            }
                        }));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(convertDpToPx(180), ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = convertDpToPx(10);
        boolean isFirst = true;
        for (String username : allUsernames) {
            String type = AccountData.getUserType(this, username);
            int color = AccountData.getTheme(this, username);

            AccountCard accountCard = new AccountCard(this, username, type, color);

            if (!isFirst)
                accountCard.setLayoutParams(params);

            isFirst = false;
            accountCard.setOnClickListener(this::onAccountCardClick);
            gridLayout.addView(accountCard);
        }
    }

    private void onSwipeViewTouchReleased(SwipeView swipeView) {
        if (swipeView.getY() < swipeView.startHeight)
            swipeView.showAllFromY();
        else
            swipeView.hideAllFromY();
    }

    private void onAccountCardClick(View view) {
        AccountCard accountCard = (AccountCard) view;
        tvswipeViewUsername.setText(accountCard.username);
        etswipeViewUrl.setText(AccountData.getSiteUrl(this, accountCard.username));
        colorPreview.setBackgroundTintList(ColorStateList.valueOf(AccountData.getTheme(this, accountCard.username)));
        swipeView.show();
    }

    private int convertDpToPx(float dp) {
        //https://stackoverflow.com/questions/4605527/converting-pixels-to-dp
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}

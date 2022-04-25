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
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.content.res.ResourcesCompat;

import com.giua.app.R;

public class AccountCard extends LinearLayout {

    public final String username;
    public final String email;
    public final String type;
    public final int color;

    public AccountCard(Context context) {
        super(context);

        username = "";
        email = "";
        type = "";
        color = -1;
        setVisibility(GONE);
    }

    public AccountCard(Context context, String username, String email, String type, @ColorInt int color) {
        super(context);

        this.username = username;
        this.email = email;
        this.type = type;
        this.color = color;

        initializeComponent(context);
    }

    private void initializeComponent(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_account_card, this);

        Drawable icAccountProfile = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_account_profile, context.getTheme());

        TextView tvUsername = findViewById(R.id.view_account_card_username);
        TextView tvType = findViewById(R.id.view_account_card_type);
        ImageView ivProfile = findViewById(R.id.view_account_card_image);

        tvUsername.setText(username);
        tvType.setText(type);
        ivProfile.setBackground(getImageWithBackgroundColor(context, icAccountProfile, color));
        findViewById(R.id.view_account_card_layout).setBackgroundTintList(ColorStateList.valueOf(color).withAlpha(50));
    }

    /**
     * Serve ad impostare un colore di sfondo a {@code drawable} con il colore {@code color}
     *
     * @param drawable il Drawable a cui impostare il colore di sfondo
     * @param color    il colore da mettere come sfondo
     * @return il {@code drawable} passato come parametro con uno sfondo circolare di colore {@code color}
     */
    private Drawable getImageWithBackgroundColor(Context context, Drawable drawable, int color) {
        Drawable icAccountBackground = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_account_background, context.getTheme());
        icAccountBackground.setTint(color);

        return new LayerDrawable(new Drawable[]{icAccountBackground, drawable});
    }


}

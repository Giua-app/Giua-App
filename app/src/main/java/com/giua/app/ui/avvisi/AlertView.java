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

package com.giua.app.ui.avvisi;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.giua.app.R;
import com.giua.objects.Alert;

import org.jetbrains.annotations.NotNull;

public class AlertView extends ConstraintLayout {
    Alert alert;
    TextView tvStatus;
    TextView tvDate;
    TextView tvObject;
    TextView tvReceivers;
    ImageView imageView;

    public AlertView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, Alert alert) {
        super(context, attrs);

        this.alert = alert;

        initializeComponent(context);
    }

    private void initializeComponent(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.alert_view, this);

        tvStatus = findViewById(R.id.alert_status_text_view);
        tvDate = findViewById(R.id.alert_date_text_view);
        tvObject = findViewById(R.id.alert_object_text_view);
        tvReceivers = findViewById(R.id.alert_receivers_text_view);
        imageView = findViewById(R.id.alert_view_left_image);

        if (!alert.isRead()) {
            tvStatus.setText("Da leggere");
            tvStatus.setTypeface(tvStatus.getTypeface(), Typeface.BOLD);
            tvDate.setTypeface(tvDate.getTypeface(), Typeface.BOLD);
            tvObject.setTypeface(tvObject.getTypeface(), Typeface.BOLD);
            tvReceivers.setTypeface(tvObject.getTypeface(), Typeface.BOLD);
            imageView.setVisibility(VISIBLE);
        } else {
            tvStatus.setText("Letta");
        }

        tvReceivers.setText(alert.receivers);
        tvDate.setText(alert.date);
        tvObject.setText(alert.object);
    }
}

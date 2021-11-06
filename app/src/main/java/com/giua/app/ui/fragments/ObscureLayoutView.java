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

package com.giua.app.ui.fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.giua.app.R;

import org.jetbrains.annotations.NotNull;

public class ObscureLayoutView extends ConstraintLayout {

    private boolean isShown;
    private final Animation hideAnimation;
    private final Animation showAnimation;
    private boolean isClickable = true;

    public ObscureLayoutView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_obscure_layout, this);

        isShown = false;
        TextView tvCancel = findViewById(R.id.obscure_layout_cancel_texview);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ObscureLayoutView);
        final int N = a.getIndexCount();

        if (N > 0) {
            for (int i = 0; i < N; ++i) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.ObscureLayoutView_hasCancelText && a.getBoolean(attr, true))
                    tvCancel.setVisibility(VISIBLE);
                else
                    tvCancel.setVisibility(GONE);
                if (attr == R.styleable.ObscureLayoutView_cancelText)
                    tvCancel.setText(a.getString(attr));
                if (tvCancel.getText().equals(""))
                    tvCancel.setText("Annulla");
            }
        }
        a.recycle();

        showAnimation = AnimationUtils.loadAnimation(context, R.anim.visualizer_show_effect);
        hideAnimation = AnimationUtils.loadAnimation(context, R.anim.visualizer_hide_effect);
    }

    public void hide(Context context) {
        setClickable(false);
        startAnimation(hideAnimation);
        setVisibility(GONE);
        isShown = false;

    }

    public void show(Context context) {
        setClickable(false);
        startAnimation(showAnimation);
        setVisibility(VISIBLE);
        setClickable(true);
        isShown = true;
    }

    public boolean isShown() {
        return isShown;
    }
}

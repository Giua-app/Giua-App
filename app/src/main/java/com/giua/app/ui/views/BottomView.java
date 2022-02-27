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

package com.giua.app.ui.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.giua.app.R;

public class BottomView extends LinearLayout {
    private float yOffset = -1f;
    private final ImageView imageView;
    private final DisplayMetrics realMetrics;

    public BottomView(Context context) {
        super(context);

        realMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getRealMetrics(realMetrics);

        imageView = new ImageView(context);
        LinearLayout.LayoutParams layoutParams = new LayoutParams(80, 15);
        layoutParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(R.drawable.bottom_view_shape);
        addView(imageView);
        setOnTouchListener(this::onTouchEvent);
    }

    public BottomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        realMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getRealMetrics(realMetrics);

        imageView = new ImageView(context);
        LinearLayout.LayoutParams layoutParams = new LayoutParams(80, 15);
        layoutParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(R.drawable.bottom_view_shape);
        addView(imageView);
        setOnTouchListener(this::onTouchEvent);
    }

    private boolean onTouchEvent(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            if (getTranslationY() >= 0) {
                if (yOffset == -1f)
                    yOffset = Math.abs(getTranslationY() - motionEvent.getRawY());
                float newY = (float) Math.sqrt(getTranslationY()) + motionEvent.getRawY() - yOffset;
                if (newY >= 0)
                    setTranslationY(newY);
                else
                    setTranslationY(0);
                return true;
            }
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP) {
            yOffset = -1f;
        }
        return false;
    }

    /**
     * Fa vedere il layout poco sopra lo schermo.
     * Più precisamente verrà visualizzato ad 1/3 dello schermo partendo dal basso.
     */
    public void showStart() {
        Animation translateAnimation = new TranslateAnimation(getX(), getX(), realMetrics.heightPixels, realMetrics.heightPixels - (float) realMetrics.heightPixels / 3);
        translateAnimation.setDuration(300);
        translateAnimation.setFillAfter(true);
        startAnimation(translateAnimation);
        setVisibility(VISIBLE);
    }

    /**
     * Fa vedere il layout all'altezza specificata
     *
     * @param height l'altezza a cui deve arrivare il layout partendo dal basso
     */
    public void showStart(float height) {
        Animation translateAnimation = new TranslateAnimation(getX(), getX(), realMetrics.heightPixels, realMetrics.heightPixels - height);
        translateAnimation.setDuration(300);
        translateAnimation.setFillAfter(true);
        startAnimation(translateAnimation);
        setVisibility(VISIBLE);
    }

    /**
     * Nasconde il layout completamente dalla sua posizione
     */
    public void hideAllFromY() {
        TranslateAnimation translateAnimation = new TranslateAnimation(getX(), getX(), getY(), realMetrics.heightPixels);
        translateAnimation.setDuration(300);
        translateAnimation.setFillAfter(true);
        startAnimation(translateAnimation);
        setVisibility(INVISIBLE);
    }

    /**
     * Nasconde il layout completamente partendo dall'alto
     */
    public void hideAllFromTop() {
        TranslateAnimation translateAnimation = new TranslateAnimation(getX(), getX(), getY(), realMetrics.heightPixels);
        translateAnimation.setDuration(300);
        translateAnimation.setFillAfter(true);
        startAnimation(translateAnimation);
        setVisibility(INVISIBLE);
    }

    /**
     * Rende completamente visibile il layout partendo dalla sua posizione.
     */
    public void showAllFromY() {
        TranslateAnimation translateAnimation = new TranslateAnimation(getX(), getX(), getY(), 0);
        translateAnimation.setDuration(300);
        translateAnimation.setFillAfter(true);
        startAnimation(translateAnimation);
        setVisibility(VISIBLE);
    }

    /**
     * Rende completamente visibile il layout partendo dal basso dello schermo
     */
    public void showAllFromStart() {
        TranslateAnimation translateAnimation = new TranslateAnimation(getX(), getX(), realMetrics.heightPixels, 0);
        translateAnimation.setDuration(300);
        translateAnimation.setFillAfter(true);
        startAnimation(translateAnimation);
        setVisibility(VISIBLE);
    }
}

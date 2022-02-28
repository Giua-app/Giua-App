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

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.giua.app.R;

public class BottomView extends LinearLayout {
    private float yOffset = -1f;
    private final ImageView imageView;
    private final DisplayMetrics realMetrics;
    private float startHeight;   //L'altezza del layout alla posizione iniziale

    public BottomView(Context context) {
        super(context);

        realMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getRealMetrics(realMetrics);

        startHeight = realMetrics.heightPixels - (float) realMetrics.heightPixels / 3;

        imageView = new ImageView(context);
        LinearLayout.LayoutParams layoutParams = new LayoutParams(convertDpToPx(50), convertDpToPx(8));
        layoutParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(R.drawable.bottom_view_shape);
        addView(imageView);
    }

    public BottomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        realMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getRealMetrics(realMetrics);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BottomView,
                0, 0);

        startHeight = a.getDimension(R.styleable.BottomView_startHeight, -1f);
        if (startHeight == -1f)
            startHeight = realMetrics.heightPixels - (float) realMetrics.heightPixels / 3;
        else
            //Converto startHeight nella y corrispondente all' altezza
            startHeight = realMetrics.heightPixels - startHeight;

        imageView = new ImageView(context);
        LinearLayout.LayoutParams layoutParams = new LayoutParams(convertDpToPx(50), convertDpToPx(8));
        layoutParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(R.drawable.bottom_view_shape);
        addView(imageView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            if (getY() < 0) return false;

            if (yOffset == -1f)
                yOffset = Math.abs(getY() - motionEvent.getRawY());
            float newY = motionEvent.getRawY() - yOffset;
            if (newY >= 0)
                setY(newY);
            else
                setY(0);
            return true;
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == MotionEvent.ACTION_UP) {
            yOffset = -1f;
            if (getY() < (float) realMetrics.heightPixels / 2 - 100)
                showAllFromY();
            else
                moveToStart();
        }

        return false;
    }

    /**
     * Fa vedere il layout portandolo a {@code startHeight} partendo dl basso
     */
    public void showStart() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "translationY", realMetrics.heightPixels, startHeight);
        objectAnimator.setDuration(300);
        objectAnimator.start();
    }

    /**
     * Fa vedere il layout all'altezza specificata partendo dal basso
     *
     * @param height l'altezza a cui deve arrivare il layout partendo dal basso
     */
    public void showStart(float height) {
        if (height > realMetrics.heightPixels || height < 0)
            height = 0;
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "translationY", realMetrics.heightPixels, realMetrics.heightPixels - height);
        objectAnimator.setDuration(300);
        objectAnimator.start();
    }

    /**
     * Nasconde il layout completamente partendo dalla sua posizione
     */
    public void hideAllFromY() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "translationY", getY(), realMetrics.heightPixels);
        objectAnimator.setDuration(300);
        objectAnimator.start();
    }

    /**
     * Porta il layout nella posizione iniziale partendo dalla sua posizione
     */
    public void moveToStart() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "translationY", getY(), startHeight);
        objectAnimator.setDuration(300);
        objectAnimator.start();
    }

    /**
     * Rende completamente visibile il layout partendo dalla sua posizione.
     */
    public void showAllFromY() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "translationY", getY(), 0);
        objectAnimator.setDuration(300);
        objectAnimator.start();
    }

    /**
     * Rende completamente visibile il layout partendo dal basso dello schermo
     */
    public void showAllFromZero() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "translationY", realMetrics.heightPixels, 0);
        objectAnimator.setDuration(300);
        objectAnimator.start();
    }

    /**
     * Rende completamente visibile il layout partendo da {@code startHeight}
     */
    public void showAllFromStart() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "translationY", startHeight, 0);
        objectAnimator.setDuration(300);
        objectAnimator.start();
    }

    /**
     * Nasconde il layout con una animazione
     */
    public void hide() {
        hideAllFromY();
        setVisibility(GONE);
    }

    private int convertDpToPx(float dp) {
        //https://stackoverflow.com/questions/4605527/converting-pixels-to-dp
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}

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

package com.giua.app.ui.fragments.votes;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.giua.app.R;
import com.giua.objects.Vote;

import org.jetbrains.annotations.NotNull;

public class SingleVoteView extends androidx.appcompat.widget.AppCompatTextView {
    Vote vote;

    public SingleVoteView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, Vote vote, boolean isFirst) {
        super(context, attrs);

        this.vote = vote;

        initializeComponent(isFirst);
    }

    private void initializeComponent(boolean isFirst) {
        LinearLayout.LayoutParams singleVoteParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (!isFirst)
            singleVoteParams.setMargins(20, 0, 0, 0);

        setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        setTypeface(ResourcesCompat.getFont(getContext(), R.font.varelaroundregular));
        setId(View.generateViewId());
        setMinWidth(convertDpToPx(35f));
        setBackground(ContextCompat.getDrawable(getContext(), R.drawable.corner_radius_10dp));
        Drawable foreground = ContextCompat.getDrawable(getContext(), R.drawable.vote_background);
        foreground.setAlpha(50);
        setForeground(foreground);
        setTextSize(17f);
        setLayoutParams(singleVoteParams);
        setPadding(5, 10, 5, 10);
    }

    private int convertDpToPx(float dp) {
        //https://stackoverflow.com/questions/4605527/converting-pixels-to-dp
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}

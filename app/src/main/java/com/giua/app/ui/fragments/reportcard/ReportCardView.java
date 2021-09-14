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

package com.giua.app.ui.fragments.reportcard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.giua.app.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReportCardView extends ConstraintLayout {

    String subject;
    List<String> vote;

    public ReportCardView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, String subject, List<String> vote) {
        super(context, attrs);

        this.subject = subject;
        this.vote = vote;

        initializeComponent(context);
    }

    private void initializeComponent(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.report_card_view, this);

        ((TextView) findViewById(R.id.report_card_view_subject)).setText(subject);
        ((TextView) findViewById(R.id.report_card_view_vote)).setText(vote.get(0));
        try {
            findViewById(R.id.report_card_view_vote).setBackgroundTintList(getColorFromVote(Float.parseFloat(vote.get(0))));
        } catch (NumberFormatException e) {  //Il voto non è un numero. Es: Ottimo
            //TODO: impostare il colore corretto per i giudizi
            findViewById(R.id.report_card_view_vote).setBackgroundTintList(getResources().getColorStateList(R.color.non_vote, context.getTheme()));
        }
        if (!vote.get(1).isEmpty())
            ((TextView) findViewById(R.id.report_card_view_absent_time)).setText(vote.get(1));
        else
            ((TextView) findViewById(R.id.report_card_view_absent_time)).setText("/");
    }

    private ColorStateList getColorFromVote(float vote) {
        if (vote == -1f) {
            return getResources().getColorStateList(R.color.non_vote, getContext().getTheme());
        } else if (vote >= 6f) {
            return getResources().getColorStateList(R.color.good_vote, getContext().getTheme());
        } else if (vote < 6f && vote >= 5) {
            return getResources().getColorStateList(R.color.middle_vote, getContext().getTheme());
        } else if (vote < 5) {
            return getResources().getColorStateList(R.color.bad_vote, getContext().getTheme());
        }
        return getResources().getColorStateList(R.color.non_vote, getContext().getTheme()); //Non si dovrebbe mai verificare
    }
}

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

package com.giua.app.ui.fragments.votes;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.giua.app.R;
import com.giua.objects.Vote;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VoteView extends ConstraintLayout {
    String subjectName;
    String voteFirstQuarter;
    float rawVoteFirstQuarter;
    String voteSecondQuarter;
    float rawVoteSecondQuarter;
    private LinearLayout listVoteLayout1;
    private LinearLayout listVoteLayout2;
    final List<Vote> allVotes;
    private final OnClickListener onClick;

    public VoteView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, String subject, String voteFirstQuarter, float rawVoteFirstQuarter, String voteSecondQuarter, float rawVoteSecondQuarter, List<Vote> allVotes, OnClickListener onClick) {
        super(context, attrs);

        this.subjectName = subject;
        this.voteFirstQuarter = voteFirstQuarter;
        this.rawVoteFirstQuarter = rawVoteFirstQuarter;
        this.voteSecondQuarter = voteSecondQuarter;
        this.rawVoteSecondQuarter = rawVoteSecondQuarter;
        this.allVotes = allVotes;
        this.onClick = onClick;
        initializeComponent(context);
    }

    private void initializeComponent(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_vote, this);

        TextView tvSubject = findViewById(R.id.text_view_subject);
        TextView tvVoteFisrtQuarter = findViewById(R.id.text_view_vote_primo_quadrimestre);
        TextView tvVoteSecondQuarter = findViewById(R.id.text_view_vote_secondo_quadrimestre);

        tvSubject.setText(this.subjectName);
        tvVoteFisrtQuarter.setText(this.voteFirstQuarter);
        tvVoteSecondQuarter.setText(this.voteSecondQuarter);

        tvVoteFisrtQuarter.setBackgroundTintList(getColorFromVote(rawVoteFirstQuarter));
        tvVoteSecondQuarter.setBackgroundTintList(getColorFromVote(rawVoteSecondQuarter));

        listVoteLayout1 = findViewById(R.id.list_vote_linear_layout_1);
        listVoteLayout2 = findViewById(R.id.list_vote_linear_layout_2);

        createSingleVotes();
    }

    private void createSingleVotes(){
        LinearLayout.LayoutParams singleVoteParams = new LinearLayout.LayoutParams(convertDpToPx(35f), ViewGroup.LayoutParams.WRAP_CONTENT);
        singleVoteParams.setMargins(20,0,0,0);

        for(Vote vote : allVotes){
            SingleVoteView tvVote = new SingleVoteView(getContext(), null, vote);
            tvVote.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvVote.setTypeface(ResourcesCompat.getFont(getContext(), R.font.varelaroundregular));
            tvVote.setId(View.generateViewId());
            tvVote.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.corner_radius_10dp));
            tvVote.setMaxLines(1);

            tvVote.setTextSize(17f);
            tvVote.setLayoutParams(singleVoteParams);
            tvVote.setPadding(5, 10, 5, 10);
            tvVote.setOnClickListener(onClick);

            if(!vote.isAsterisk)
                tvVote.setText(vote.value);
            else
                tvVote.setText("*");
            tvVote.setBackgroundTintList(getColorFromVote(getNumberFromVote(vote)));
            if(vote.quarterlyToInt() == 1) //FIXME: prima era vote.isQuartely
                listVoteLayout1.addView(tvVote);
            else
                listVoteLayout2.addView(tvVote);
        }
    }

    private float getNumberFromVote(Vote vote) {
        if(vote.isAsterisk)
            return -1f;

        char lastChar = vote.value.charAt(vote.value.length() - 1);
        if (lastChar == '+')
            return (vote.value.length() == 2) ? Character.getNumericValue(vote.value.charAt(0)) + 0.15f : Integer.parseInt(vote.value.substring(0, 2)) + 0.15f;

        else if (lastChar == '-')
            return (vote.value.length() == 2) ? Character.getNumericValue(vote.value.charAt(0)) - 1 + 0.85f : Integer.parseInt(vote.value.substring(0, 2)) - 1 + 0.85f;

        else if (lastChar == '½')
            return (vote.value.length() == 2) ? Character.getNumericValue(vote.value.charAt(0)) + 0.5f : Integer.parseInt(vote.value.substring(0, 2)) + 0.5f;

        else
            return Integer.parseInt(vote.value);
    }

    private ColorStateList getColorFromVote(float vote){
        if(vote == -1f){
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

    private int convertDpToPx(float dp) {
        //https://stackoverflow.com/questions/4605527/converting-pixels-to-dp
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}

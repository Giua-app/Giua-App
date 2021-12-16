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
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.giua.app.GlobalVariables;
import com.giua.app.R;
import com.giua.objects.Vote;
import com.giua.pages.VotesPage;
import com.giua.utils.GiuaScraperUtils;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Vector;

public class VoteView extends ConstraintLayout {
    private final String subjectName;
    private List<LinearLayout> listVoteLayouts;
    private final List<Vote> allVotes;
    private final VotesPage votesPage;
    private final OnClickListener onClick;
    private List<Integer> quarterlyCounter;

    public VoteView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, String subject, List<Vote> allVotes, OnClickListener onClick) {
        super(context, attrs);

        this.subjectName = subject;
        this.allVotes = allVotes;
        this.onClick = onClick;
        votesPage = GlobalVariables.gS.getVotesPage(false);
        initializeComponent(context);
    }

    private void initializeComponent(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_vote, this);

        TextView tvSubject = findViewById(R.id.text_view_subject);
        listVoteLayouts = new Vector<>();

        tvSubject.setText(this.subjectName);

        //Conto quanti e quali quadrimestri si dovranno visualizzare
        quarterlyCounter = new Vector<>();
        int length = allVotes.size();
        for (int i = 0; i < length; i++) {
            if (!quarterlyCounter.contains(allVotes.get(i).quarterlyToInt()))
                quarterlyCounter.add(allVotes.get(i).quarterlyToInt());
        }

        //Creo i quadrimestri visivamente
        length = quarterlyCounter.size();
        for (int i = 0; i < length; i++) {
            TextView quarterlyTextView = createTextViewForQuarterly(context, GiuaScraperUtils.getQuarterNameWithNumbers(quarterlyCounter.get(i)), i == 0);
            HorizontalScrollView horizontalScrollView = createHorizontalScrollView(context);
            TextView meanTextView = createTextViewForMeans(context, votesPage.getMeanOf(allVotes, i + 1), i == 0);

            ((LinearLayout) findViewById(R.id.list_vote_layout)).addView(quarterlyTextView);
            ((LinearLayout) findViewById(R.id.list_vote_layout)).addView(horizontalScrollView);
            ((LinearLayout) findViewById(R.id.mean_vote_layout)).addView(meanTextView);

        }

        createSingleVotes();
    }

    private TextView createTextViewForQuarterly(Context context, String text, boolean isFirst) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (!isFirst)
            layoutParams.setMargins(0, convertDpToPx(16), 0, 0);
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.varelaroundregular));
        textView.setTextSize(15);
        textView.setLayoutParams(layoutParams);

        return textView;
    }

    private TextView createTextViewForMeans(Context context, float mean, boolean isFirst) {
        DecimalFormat df = new DecimalFormat("0.0");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(convertDpToPx(50f), ViewGroup.LayoutParams.WRAP_CONTENT);
        if (isFirst)
            layoutParams.setMargins(0, convertDpToPx(14), 0, 0);
        else
            layoutParams.setMargins(0, convertDpToPx(42), 0, 0);
        TextView textView = new TextView(context);
        if (mean != -1f)
            textView.setText(df.format((mean)));
        else
            textView.setText("/");
        textView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.varelaroundregular));
        textView.setTextSize(17);
        textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        int dp = convertDpToPx(5);
        textView.setPadding(dp, dp, dp, dp);
        textView.setBackgroundTintList(getColorFromVote(mean));
        textView.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.corner_radius_10dp, context.getTheme()));
        textView.setLayoutParams(layoutParams);

        return textView;
    }

    private HorizontalScrollView createHorizontalScrollView(Context context) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, convertDpToPx(12), 0, 0);
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(context);
        horizontalScrollView.setOverScrollMode(HorizontalScrollView.OVER_SCROLL_NEVER);
        horizontalScrollView.setScrollBarSize(0);
        horizontalScrollView.setLayoutParams(layoutParams);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        listVoteLayouts.add(linearLayout);

        horizontalScrollView.addView(linearLayout);

        return horizontalScrollView;
    }

    private void createSingleVotes() {
        for (Vote vote : allVotes) {
            int listVoteLayoutsIndex = quarterlyCounter.indexOf(vote.quarterlyToInt());
            SingleVoteView tvVote = new SingleVoteView(getContext(), null, vote, listVoteLayouts.get(listVoteLayoutsIndex).getChildCount() == 0);
            tvVote.setOnClickListener(onClick);

            if (!vote.isAsterisk)
                tvVote.setText(vote.value);
            else
                tvVote.setText("*");

            tvVote.setBackgroundTintList(getColorFromVote(getNumberFromVote(vote)));
            listVoteLayouts.get(listVoteLayoutsIndex).addView(tvVote);
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

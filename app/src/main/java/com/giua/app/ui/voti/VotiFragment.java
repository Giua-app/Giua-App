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

package com.giua.app.ui.voti;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.giua.app.DrawerActivity;
import com.giua.app.GlobalVariables;
import com.giua.app.R;
import com.giua.app.ui.ObscureLayoutView;
import com.giua.objects.Vote;
import com.giua.webscraper.GiuaScraperExceptions;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VotiFragment extends Fragment {

    ProgressBar progressBar;
    VoteView voteView;
    TextView tvNoElements;
    LinearLayout mainLayout;
    LinearLayout detailVoteLayout;
    LinearLayout.LayoutParams params;
    ObscureLayoutView obscureLayoutButton;    //Questo bottone viene visualizzato dietro al detail layout e se viene cliccato si esce dai dettaglii
    DecimalFormat df = new DecimalFormat("0.0");
    Map<String, List<Vote>> allVotes;
    Activity activity;
    View root;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_voti, container, false);

        mainLayout = root.findViewById(R.id.vote_fragment_linear_layout);
        obscureLayoutButton = root.findViewById(R.id.vote_obscure_view);
        detailVoteLayout = root.findViewById(R.id.attachment_layout);
        progressBar = root.findViewById(R.id.vote_loading_page_bar);
        tvNoElements = root.findViewById(R.id.vote_fragment_no_elements_view);

        activity = requireActivity();

        obscureLayoutButton.setOnClickListener(this::obscureButtonClick);

        generateAllViewsAsync();

        return root;
    }

    private void generateAllViewsAsync() {
        new Thread(() -> {
            try {
                allVotes = GlobalVariables.gS.getAllVotes(false);
                activity.runOnUiThread(this::generateAllViews);
            } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                activity.runOnUiThread(() -> {
                    DrawerActivity.setErrorMessage(getString(R.string.your_connection_error), root);
                    progressBar.setVisibility(View.GONE);
                    tvNoElements.setVisibility(View.VISIBLE);
                });
            } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                activity.runOnUiThread(() -> {
                    DrawerActivity.setErrorMessage(getString(R.string.site_connection_error), root);
                    progressBar.setVisibility(View.GONE);
                    tvNoElements.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private void generateAllViews() {
        float meanSecondQuarter;
        float meanFirstQuarter;     //media aritmetica dei voti
        int voteCounterFirstQuarter;     //Conta solamente i voti che ci sono e non gli asterischi
        int voteCounterSecondQuarter;

        params = new LinearLayout.LayoutParams(mainLayout.getLayoutParams().width, mainLayout.getLayoutParams().height);
        params.setMargins(10, 20, 10, 30);

        if (allVotes.isEmpty()) {
            tvNoElements.setVisibility(View.VISIBLE);
        }

        for (String subject : allVotes.keySet()) {     //Cicla ogni materia
            meanFirstQuarter = 0f;
            meanSecondQuarter = 0f;
            voteCounterFirstQuarter = 0;     //Conta solamente i voti che ci sono e non gli asterischi
            voteCounterSecondQuarter = 0;

            for (Vote vote : Objects.requireNonNull(allVotes.get(subject))) {      //Cicla ogni voto della materia
                if (vote.value.length() > 0 && vote.isFirstQuarterly) {
                    meanFirstQuarter += getNumberFromVote(vote);
                    voteCounterFirstQuarter++;
                } else if (vote.value.length() > 0) {
                    meanSecondQuarter += getNumberFromVote(vote);
                    voteCounterSecondQuarter++;
                }
            }

            if(voteCounterFirstQuarter != 0 && voteCounterSecondQuarter != 0) {
                meanFirstQuarter /= voteCounterFirstQuarter;
                meanSecondQuarter /= voteCounterSecondQuarter;
                addVoteView(subject, df.format(meanFirstQuarter), meanFirstQuarter, df.format(meanSecondQuarter), meanSecondQuarter);
            } else{
                if(voteCounterFirstQuarter == 0 && voteCounterSecondQuarter != 0) {
                    meanSecondQuarter /= voteCounterSecondQuarter;
                    addVoteView(subject, "/", -1f, df.format(meanSecondQuarter), meanSecondQuarter);
                } else if(voteCounterFirstQuarter != 0){
                    meanFirstQuarter /= voteCounterFirstQuarter;
                    addVoteView(subject, df.format(meanFirstQuarter), meanFirstQuarter, "/", -1f);
                } else {
                    addVoteView(subject, "/", -1f, "/", -1f);
                }
            }
        }
        progressBar.setVisibility(View.GONE);
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

    public void obscureButtonClick(View view){
        detailVoteLayout.setVisibility(View.GONE);
        obscureLayoutButton.setVisibility(View.GONE);
    }

    private void onClickSingleVote(View view){
        Resources res = getResources();
        SingleVoteView _view = (SingleVoteView) view;
        TextView detailVoteDate = detailVoteLayout.findViewById(R.id.detail_vote_date);
        TextView detailVoteType = detailVoteLayout.findViewById(R.id.detail_vote_type);
        TextView detailVoteArguments = detailVoteLayout.findViewById(R.id.detail_vote_arguments);
        TextView detailVoteJudge = detailVoteLayout.findViewById(R.id.detail_vote_judge);
        detailVoteDate.setVisibility(View.GONE);
        detailVoteType.setVisibility(View.GONE);
        detailVoteArguments.setVisibility(View.GONE);
        detailVoteJudge.setVisibility(View.GONE);
        detailVoteLayout.setVisibility(View.VISIBLE);
        obscureLayoutButton.setVisibility(View.VISIBLE);

        if(!_view.vote.date.equals("")) {
            detailVoteDate.setVisibility(View.VISIBLE);
            detailVoteDate.setText(Html.fromHtml("<b>" + res.getString(R.string.detail_vote_date) + "</b> " + _view.vote.date, Html.FROM_HTML_MODE_COMPACT));
        }
        if(!_view.vote.testType.equals("")) {
            detailVoteType.setVisibility(View.VISIBLE);
            detailVoteType.setText(Html.fromHtml("<b>" + res.getString(R.string.detail_vote_type) + "</b> " + _view.vote.testType, Html.FROM_HTML_MODE_COMPACT));
        }
        if(!_view.vote.arguments.equals("")) {
            detailVoteArguments.setVisibility(View.VISIBLE);
            detailVoteArguments.setText(Html.fromHtml("<b>" + res.getString(R.string.detail_vote_arguments) + "</b> " + _view.vote.arguments, Html.FROM_HTML_MODE_COMPACT));
        }
        if(!_view.vote.judgement.equals("")) {
            detailVoteJudge.setVisibility(View.VISIBLE);
            detailVoteJudge.setText(Html.fromHtml("<b>" + res.getString(R.string.detail_vote_judge) + "</b> " + _view.vote.judgement, Html.FROM_HTML_MODE_COMPACT));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addVoteView(String subject, String voteFirstQuart, float rawVoteFirstQuart, String voteSecondQuart, float rawVoteSecondQuart){
        voteView = new VoteView(requireContext(), null, subject, voteFirstQuart, rawVoteFirstQuart, voteSecondQuart, rawVoteSecondQuart, GlobalVariables.gS.getAllVotes(false).get(subject), this::onClickSingleVote);
        voteView.setId(View.generateViewId());

        voteView.setLayoutParams(params);

        mainLayout.addView(voteView);
    }
}
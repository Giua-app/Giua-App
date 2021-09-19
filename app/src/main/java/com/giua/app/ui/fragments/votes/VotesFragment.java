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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.giua.app.AppData;
import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.R;
import com.giua.app.ThreadManager;
import com.giua.app.ui.fragments.ObscureLayoutView;
import com.giua.objects.Vote;
import com.giua.utils.JsonHelper;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VotesFragment extends Fragment implements IGiuaAppFragment {

    ProgressBar pbLoadingPage;
    VoteView voteView;
    TextView tvNoElements;
    LinearLayout viewsLayout;
    LinearLayout voteVisualizer;
    LinearLayout.LayoutParams params;
    ObscureLayoutView obscureLayoutView;    //Questo bottone viene visualizzato dietro al detail layout e se viene cliccato si esce dai dettagli
    SwipeRefreshLayout swipeRefreshLayout;
    DecimalFormat df = new DecimalFormat("0.0");
    Map<String, List<Vote>> allVotes;
    Activity activity;
    View root;
    ThreadManager threadManager;
    boolean refreshVotes = false;
    boolean offlineMode = false;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null)
            offlineMode = getArguments().getBoolean("offline");
        root = inflater.inflate(R.layout.fragment_votes, container, false);

        viewsLayout = root.findViewById(R.id.vote_fragment_linear_layout);
        obscureLayoutView = root.findViewById(R.id.vote_obscure_view);
        voteVisualizer = root.findViewById(R.id.vote_attachment_layout);
        pbLoadingPage = root.findViewById(R.id.vote_loading_page_bar);
        tvNoElements = root.findViewById(R.id.vote_fragment_no_elements_view);
        swipeRefreshLayout = root.findViewById(R.id.vote_swipe_refresh_layout);

        activity = requireActivity();
        threadManager = new ThreadManager();

        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        obscureLayoutView.setOnClickListener(this::obscureButtonOnClick);

        loadDataAndViews();

        return root;
    }

    @Override
    public void loadDataAndViews() {
        threadManager.addAndRun(() -> {
            try {
                allVotes = GlobalVariables.gS.getAllVotes(refreshVotes);
                /*else
                    allVotes = new JsonHelper().parseJsonForVotes(AppData.getVotesString(requireContext()));*/
                refreshVotes = false;
                activity.runOnUiThread(this::addViews);
            } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(getString(R.string.your_connection_error), root);
                    pbLoadingPage.setVisibility(View.GONE);
                    tvNoElements.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(getString(R.string.site_connection_error), root);
                    pbLoadingPage.setVisibility(View.GONE);
                    tvNoElements.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });
    }

    @Override
    public void addViews() {
        float meanSecondQuarter;
        float meanFirstQuarter;     //media aritmetica dei voti
        int voteCounterFirstQuarter;     //Conta solamente i voti che ci sono e non gli asterischi
        int voteCounterSecondQuarter;

        viewsLayout.removeAllViews();
        params = new LinearLayout.LayoutParams(viewsLayout.getLayoutParams().width, viewsLayout.getLayoutParams().height);
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
                if (vote.value.length() > 0 && !vote.isAsterisk && vote.isFirstQuarterly) {
                    meanFirstQuarter += vote.toFloat();
                    voteCounterFirstQuarter++;
                } else if (vote.value.length() > 0 && !vote.isAsterisk) {
                    meanSecondQuarter += vote.toFloat();
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
        pbLoadingPage.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void onRefresh() {
        refreshVotes = true;
        loadDataAndViews();
    }

    public void obscureButtonOnClick(View view) {
        voteVisualizer.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_hide_effect));
        obscureLayoutView.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_hide_effect));
        voteVisualizer.setVisibility(View.GONE);
        obscureLayoutView.setVisibility(View.GONE);
    }

    private void singleVoteOnClick(View view) {
        Resources res = getResources();
        SingleVoteView _view = (SingleVoteView) view;
        TextView detailVoteDate = root.findViewById(R.id.detail_vote_date);
        TextView detailVoteType = root.findViewById(R.id.detail_vote_type);
        TextView detailVoteArguments = root.findViewById(R.id.detail_vote_arguments);
        TextView detailVoteJudge = root.findViewById(R.id.detail_vote_judge);
        detailVoteArguments.setMovementMethod(new ScrollingMovementMethod());
        detailVoteJudge.setMovementMethod(new ScrollingMovementMethod());
        detailVoteDate.setVisibility(View.GONE);
        detailVoteType.setVisibility(View.GONE);
        detailVoteArguments.setVisibility(View.GONE);
        detailVoteJudge.setVisibility(View.GONE);
        voteVisualizer.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_show_effect));
        obscureLayoutView.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_show_effect));
        voteVisualizer.setVisibility(View.VISIBLE);
        obscureLayoutView.setVisibility(View.VISIBLE);

        if (!_view.vote.date.equals("")) {
            detailVoteDate.setVisibility(View.VISIBLE);
            detailVoteDate.setText(Html.fromHtml("<b>" + res.getString(R.string.detail_vote_date) + "</b> " + _view.vote.date, Html.FROM_HTML_MODE_COMPACT));
        }
        if (!_view.vote.testType.equals("")) {
            detailVoteType.setVisibility(View.VISIBLE);
            detailVoteType.setText(Html.fromHtml("<b>" + res.getString(R.string.detail_vote_type) + "</b> " + _view.vote.testType, Html.FROM_HTML_MODE_COMPACT));
        }
        if (!_view.vote.arguments.equals("")) {
            detailVoteArguments.setVisibility(View.VISIBLE);
            detailVoteArguments.setText(Html.fromHtml("<b>" + res.getString(R.string.detail_vote_arguments) + "</b> " + _view.vote.arguments, Html.FROM_HTML_MODE_COMPACT));
        }
        if (!_view.vote.judgement.equals("")) {
            detailVoteJudge.setVisibility(View.VISIBLE);
            detailVoteJudge.setText(Html.fromHtml("<b>" + res.getString(R.string.detail_vote_judge) + "</b> " + _view.vote.judgement, Html.FROM_HTML_MODE_COMPACT));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addVoteView(String subject, String voteFirstQuart, float rawVoteFirstQuart, String voteSecondQuart, float rawVoteSecondQuart) {
        voteView = new VoteView(requireContext(), null, subject, voteFirstQuart, rawVoteFirstQuart, voteSecondQuart, rawVoteSecondQuart, allVotes.get(subject), this::singleVoteOnClick);
        voteView.setId(View.generateViewId());

        voteView.setLayoutParams(params);

        viewsLayout.addView(voteView);


    }

    private void setErrorMessage(String message, View root) {
        Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        if (allVotes != null && !allVotes.isEmpty())
            AppData.saveVotesString(activity, new JsonHelper().saveVotesToString(allVotes));
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        threadManager.destroyAllAndNullMe();
        super.onDestroyView();
    }
}
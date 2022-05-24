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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.OfflineDBController;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.app.ui.activities.DrawerActivity;
import com.giua.app.ui.views.ObscureLayoutView;
import com.giua.objects.Vote;
import com.giua.pages.VotesPage;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Map;

public class VotesFragment extends Fragment implements IGiuaAppFragment {

    VoteView voteView;
    TextView tvNoElements;
    LinearLayout viewsLayout;
    LinearLayout voteVisualizer;
    LinearLayout.LayoutParams params;
    ObscureLayoutView obscureLayoutView;    //Questo bottone viene visualizzato dietro al detail layout e se viene cliccato si esce dai dettagli
    SwipeRefreshLayout swipeRefreshLayout;
    Map<String, List<Vote>> allVotes;
    VotesPage votesPage;
    Activity activity;
    View root;

    boolean refreshVotes = false;
    boolean offlineMode = false;
    boolean isFragmentDestroyed = false;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_votes, container, false);

        viewsLayout = root.findViewById(R.id.vote_fragment_linear_layout);
        obscureLayoutView = root.findViewById(R.id.vote_obscure_view);
        voteVisualizer = root.findViewById(R.id.vote_attachment_layout);
        tvNoElements = root.findViewById(R.id.vote_fragment_no_elements_view);
        swipeRefreshLayout = root.findViewById(R.id.vote_swipe_refresh_layout);

        activity = requireActivity();

        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        obscureLayoutView.setOnClickListener(this::obscureButtonOnClick);
        offlineMode = activity.getIntent().getBooleanExtra("offline", false);

        swipeRefreshLayout.setRefreshing(true);

        activity.getSystemService(NotificationManager.class).cancel(12);

        return root;
    }

    @Override
    public void loadOfflineDataAndViews() {
        new Thread(() -> {
            try {
                allVotes = new OfflineDBController(activity).readVotes();
                refreshVotes = false;
                if (isFragmentDestroyed)
                    return;
                activity.runOnUiThread(this::addViews);
            } catch (IllegalStateException ignored) {
                //Si verifica quando questa schermata è stata distrutta ma il thread cerca comunque di fare qualcosa
            }
        }).start();
    }

    @Override
    public void loadDataAndViews() {
        GlobalVariables.gsThread.addTask(() -> {
            try {
                votesPage = GlobalVariables.gS.getVotesPage(refreshVotes);
                allVotes = votesPage.getAllVotes();
                new OfflineDBController(activity).addVotes(allVotes);
                ((DrawerActivity) activity).notificationsDBController.replaceVotes(allVotes);
                refreshVotes = false;
                if (isFragmentDestroyed)
                    return;
                activity.runOnUiThread(this::addViews);
            } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.your_connection_error), root);
                    tvNoElements.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.site_connection_error), root);
                    tvNoElements.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                    tvNoElements.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (GiuaScraperExceptions.NotLoggedIn e) {
                activity.runOnUiThread(() -> {
                    ((DrawerActivity) activity).startActivityManager();
                });
            } catch (IllegalStateException ignored) {
                //Si verifica quando questa schermata è stata distrutta ma il thread cerca comunque di fare qualcosa
            }
        });
    }

    @Override
    public void addViews() {
        viewsLayout.removeAllViews();
        params = new LinearLayout.LayoutParams(viewsLayout.getLayoutParams().width, viewsLayout.getLayoutParams().height);
        params.setMargins(10, 20, 10, 30);

        if (allVotes.isEmpty()) {
            tvNoElements.setVisibility(View.VISIBLE);
        }

        for (String subject : allVotes.keySet()) {     //Cicla ogni materia
            addVoteView(subject);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onBackPressed() {
        if (obscureLayoutView.isShown()) {
            obscureLayoutView.performClick();
            return true;
        }
        return false;
    }

    private void onRefresh() {
        refreshVotes = true;
        if (!offlineMode)
            loadDataAndViews();
        else
            loadOfflineDataAndViews();
    }

    public void obscureButtonOnClick(View view) {
        obscureLayoutView.hide();
    }

    private void singleVoteOnClick(View view) {
        Resources res = activity.getResources();
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
        detailVoteArguments.scrollTo(0, 0);
        detailVoteJudge.scrollTo(0, 0);

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

        obscureLayoutView.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addVoteView(String subject) {
        voteView = new VoteView(requireActivity(), null, subject, votesPage.getAllQuarterlyNames(), allVotes.get(subject), this::singleVoteOnClick, SettingsData.getSettingBoolean(activity, SettingKey.SHOW_CENTS));
        voteView.setId(View.generateViewId());

        voteView.setLayoutParams(params);

        viewsLayout.addView(voteView);


    }

    private void setErrorMessage(String message, View root) {
        if (!isFragmentDestroyed)
            Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        if (!offlineMode)
            loadDataAndViews();
        else
            loadOfflineDataAndViews();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        isFragmentDestroyed = true;
        super.onDestroyView();
    }
}
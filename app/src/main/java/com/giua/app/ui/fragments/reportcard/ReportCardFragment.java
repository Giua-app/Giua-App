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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.R;
import com.giua.app.ThreadManager;
import com.giua.objects.ReportCard;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

public class ReportCardFragment extends Fragment implements IGiuaAppFragment {

    ReportCard reportCard;
    LinearLayout viewsLayout;
    TextView tvCurrentQuarter;
    ImageButton btnChangeQuarter;
    ProgressBar pbLoadingPage;
    TextView tvNoElements;
    FragmentActivity activity;
    ThreadManager threadManager;
    View root;
    boolean isFirstQuarter = true;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_reportcard, container, false);

        viewsLayout = root.findViewById(R.id.report_card_views_layout);
        tvCurrentQuarter = root.findViewById(R.id.report_card_current_quarter);
        pbLoadingPage = root.findViewById(R.id.report_card_progress_bar);
        tvNoElements = root.findViewById(R.id.report_card_no_elements);
        btnChangeQuarter = root.findViewById(R.id.report_card_btn_change_quarter);

        activity = requireActivity();
        threadManager = new ThreadManager();

        btnChangeQuarter.setOnClickListener(this::changeQuarterOnClick);
        tvCurrentQuarter.setOnClickListener(this::changeQuarterOnClick);

        loadDataAndViews();

        return root;
    }

    @Override
    public void loadDataAndViews() {
        viewsLayout.removeAllViews();
        pbLoadingPage.setVisibility(View.VISIBLE);
        tvNoElements.setVisibility(View.GONE);
        threadManager.addAndRun(() -> {
            try {
                reportCard = GlobalVariables.gS.getReportCard(isFirstQuarter, true);
                if (reportCard.exists) {
                    activity.runOnUiThread(this::addViews);
                    if (reportCard.finalResult.equals("AMMESSO")) {
                        ((TextView) root.findViewById(R.id.report_card_txt_final_result)).setTextColor(getResources().getColor(R.color.good_vote, requireContext().getTheme()));
                    } else {
                        ((TextView) root.findViewById(R.id.report_card_txt_final_result)).setTextColor(getResources().getColor(R.color.bad_vote, requireContext().getTheme()));
                    }
                    activity.runOnUiThread(() -> ((TextView) root.findViewById(R.id.report_card_txt_final_result)).setText("Esito finale: " + reportCard.finalResult));
                    activity.runOnUiThread(() -> ((TextView) root.findViewById(R.id.report_card_txt_credits)).setText("Crediti: " + reportCard.credits));
                } else
                    activity.runOnUiThread(() -> tvNoElements.setVisibility(View.VISIBLE));

                activity.runOnUiThread(() -> pbLoadingPage.setVisibility(View.GONE));
            } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                //Errore di connessione
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.your_connection_error), root);
                    pbLoadingPage.setVisibility(View.GONE);
                });
            } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                //Errore di connessione al sito
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.site_connection_error), root);
                    pbLoadingPage.setVisibility(View.GONE);
                });
            } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                    pbLoadingPage.setVisibility(View.GONE);
                });
            }

        });
    }

    @Override
    public void addViews() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 40, 0, 0);

        int viewsCounter = 0;
        for (String key : reportCard.allVotes.keySet()) {
            ReportCardView view = new ReportCardView(activity, null, key, reportCard.allVotes.get(key));
            if (viewsCounter > 0)
                view.setLayoutParams(params);

            viewsLayout.addView(view);
            viewsCounter++;
        }

    }

    //region Listeners

    private void changeQuarterOnClick(View view) {
        Animation animation = AnimationUtils.loadAnimation(requireContext(), R.anim.change_quarter);
        btnChangeQuarter.startAnimation(animation);
        isFirstQuarter = !isFirstQuarter;
        if (isFirstQuarter)
            tvCurrentQuarter.setText(R.string.report_card_btn_first_quarter);
        else
            tvCurrentQuarter.setText(R.string.report_card_btn_second_quarter);
        loadDataAndViews();

    }

    //endregion

    private void setErrorMessage(String message, View root) {
        Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        threadManager.destroyAllAndNullMe();
        super.onDestroyView();
    }
}
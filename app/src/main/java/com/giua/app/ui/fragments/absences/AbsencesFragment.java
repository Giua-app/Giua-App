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

package com.giua.app.ui.fragments.absences;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.LoggerManager;
import com.giua.app.R;
import com.giua.app.ThreadManager;
import com.giua.app.ui.fragments.ObscureLayoutView;
import com.giua.objects.Absence;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class AbsencesFragment extends Fragment implements IGiuaAppFragment {

    View root;
    LoggerManager loggerManager;
    Activity activity;
    ThreadManager threadManager;
    List<Absence> absences;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView tvConfirmText;
    Button btnConfirm;
    ObscureLayoutView obscureLayoutView;
    AbsenceView latestAbsenceViewClicked;
    boolean confirmActionIsDelete;  //Indica cosa deve fare il bottone di conferma una volta cliccato. true: elimina la giustificazione ; false: la modifica/pubblica
    boolean refresh = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //TODO: aggiungere le altre info come le ore totali di assenza
        root = inflater.inflate(R.layout.fragment_absences, container, false);
        loggerManager = new LoggerManager("AbsencesFragment", getContext());

        swipeRefreshLayout = root.findViewById(R.id.absences_refresh_layout);
        tvConfirmText = root.findViewById(R.id.absences_confirm_text);
        obscureLayoutView = root.findViewById(R.id.absences_obscure_view);
        btnConfirm = root.findViewById(R.id.absences_confirm_button);

        activity = requireActivity();
        threadManager = new ThreadManager();

        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        obscureLayoutView.setOnClickListener(this::obscureViewOnClick);
        btnConfirm.setOnClickListener(this::btnConfirmOnClick);

        loadDataAndViews();

        return root;
    }

    @Override
    public void loadDataAndViews() {
        if (threadManager.isDestroyed())
            return;

        threadManager.addAndRun(() -> {
            try {
                absences = GlobalVariables.gS.getAbsencesPage(refresh).getAllAbsences();
                if (absences.isEmpty())
                    activity.runOnUiThread(() -> root.findViewById(R.id.absences_no_elements_text).setVisibility(View.VISIBLE));
                else
                    activity.runOnUiThread(this::addViews);
            } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.your_connection_error), root);
                    root.findViewById(R.id.absences_no_elements_text).setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.site_connection_error), root);
                    root.findViewById(R.id.absences_no_elements_text).setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                    root.findViewById(R.id.absences_no_elements_text).setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });

    }

    @Override
    public void addViews() {
        LinearLayout linearLayout = root.findViewById(R.id.absences_views_layout);
        linearLayout.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 40, 0, 0);

        int counter = 0;
        for (Absence absence : absences) {
            AbsenceView absenceView = new AbsenceView(activity, null, absence, this::viewJustifyOnClick, this::viewDeleteOnClick);

            if (counter != 0)
                absenceView.setLayoutParams(params);

            linearLayout.addView(absenceView);
            counter++;
        }

        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public boolean onBackPressed() {
        if (obscureLayoutView.isShown()) {
            btnConfirm.setClickable(false);
            obscureLayoutView.hide(requireContext());
            return true;
        }
        return false;
    }

    private void onRefresh() {
        refresh = true;
        root.findViewById(R.id.absences_no_elements_text).setVisibility(View.GONE);
        loadDataAndViews();
    }

    private void viewDeleteOnClick(View view) {
        latestAbsenceViewClicked = ((AbsenceView) view);
        tvConfirmText.setText(Html.fromHtml("Sei sicuro di voler cancellare la giustificazione del <b>" + latestAbsenceViewClicked.absence.date + "</b> ?", 0));
        confirmActionIsDelete = true;
        btnConfirm.setClickable(true);
        obscureLayoutView.show(activity);
    }

    public void viewJustifyOnClick(View view) {
        if (((AbsenceView) view).justifyText.length() > 0) { //Se non c'è scritto nulla come testo della giustificazione potrebbe essere un click involontario quindi non fare nulla
            latestAbsenceViewClicked = ((AbsenceView) view);
            tvConfirmText.setText(Html.fromHtml("Sei sicuro di voler giustificare con: <b>" + ((AbsenceView) view).justifyText + "</b> ?", 0));
            confirmActionIsDelete = false;
            btnConfirm.setClickable(true);
            obscureLayoutView.show(activity);
        }
    }

    private void obscureViewOnClick(View view) {
        latestAbsenceViewClicked = null;
        obscureLayoutView.hide(activity);
    }

    private void btnConfirmOnClick(View view) {
        if (latestAbsenceViewClicked != null) {
            AbsenceView absenceView = latestAbsenceViewClicked;
            latestAbsenceViewClicked = null;

            threadManager.addAndRun(() -> {
                activity.runOnUiThread(() -> swipeRefreshLayout.setRefreshing(true));
                try {
                    if (!confirmActionIsDelete)
                        GlobalVariables.gS.getAbsencesPage(false).justifyAbsence(absenceView.absence, "", absenceView.justifyText);
                    else
                        GlobalVariables.gS.getAbsencesPage(false).deleteJustificationAbsence(absenceView.absence);
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.your_connection_error), root));
                    return;
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.site_connection_error), root));
                    return;
                } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                    activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root));
                    return;
                }
                activity.runOnUiThread(this::onRefresh);
            });
        }
        btnConfirm.setClickable(false);
        obscureLayoutView.hide(activity);
    }

    private void setErrorMessage(String message, View root) {
        if (!threadManager.isDestroyed())
            Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        threadManager.destroyAllAndNullMe();
        super.onDestroyView();
    }
}

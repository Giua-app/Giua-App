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
        threadManager.addAndRun(() -> {
            absences = GlobalVariables.gS.getAllAbsences(refresh);
            activity.runOnUiThread(this::addViews);
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
            AbsenceView absenceView = new AbsenceView(activity, null, absence, this::viewJustifyOnClick);

            if (counter != 0)
                absenceView.setLayoutParams(params);

            linearLayout.addView(absenceView);
            counter++;
        }

        swipeRefreshLayout.setRefreshing(false);

    }

    private void onRefresh() {
        refresh = true;
        loadDataAndViews();
    }

    public void viewJustifyOnClick(View view) {
        if (((AbsenceView) view).justifyText.length() > 0) { //Se non c'è scritto nulla come testo della giustificazione potrebbe essere un click involontario quindi non fare nulla
            latestAbsenceViewClicked = ((AbsenceView) view);
            tvConfirmText.setText(Html.fromHtml("Sei sicuro di voler giustificare con: <b>" + ((AbsenceView) view).justifyText + "</b> ?", 0));
            obscureLayoutView.show(activity);
        }
    }

    private void obscureViewOnClick(View view) {
        latestAbsenceViewClicked = null;
        obscureLayoutView.hide(activity);
    }

    private void btnConfirmOnClick(View view) {
        threadManager.addAndRun(() -> {
            if (latestAbsenceViewClicked != null) {
                GlobalVariables.gS.justifyAbsence(latestAbsenceViewClicked.absence, "", latestAbsenceViewClicked.justifyText);
                latestAbsenceViewClicked = null;
            }
            activity.runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(true);
                onRefresh();
            });
        });
        obscureLayoutView.hide(activity);
    }
}

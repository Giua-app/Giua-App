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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.LoggerManager;
import com.giua.app.R;
import com.giua.app.ThreadManager;
import com.giua.objects.Absence;

import java.util.List;

public class AbsencesFragment extends Fragment implements IGiuaAppFragment {

    View root;
    LoggerManager loggerManager;
    Activity activity;
    ThreadManager threadManager;
    List<Absence> absences;
    boolean refresh = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_absences, container, false);
        loggerManager = new LoggerManager("AbsencesFragment", getContext());

        activity = requireActivity();
        threadManager = new ThreadManager();

        ((SwipeRefreshLayout) root.findViewById(R.id.absences_refresh_layout)).setRefreshing(true);
        ((SwipeRefreshLayout) root.findViewById(R.id.absences_refresh_layout)).setOnRefreshListener(this::onRefresh);

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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int counter = 0;
        for (Absence absence : absences) {
            AbsenceView absenceView = new AbsenceView(activity, null, absence);

            if (counter != 0)
                absenceView.setLayoutParams(params);

            counter++;
        }

    }

    private void onRefresh() {
        refresh = true;
        loadDataAndViews();
    }
}

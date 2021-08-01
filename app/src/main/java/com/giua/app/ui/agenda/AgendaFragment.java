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

package com.giua.app.ui.agenda;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.giua.app.GlobalVariables;
import com.giua.app.R;
import com.giua.objects.Homework;
import com.giua.objects.Test;

import java.util.List;

public class AgendaFragment extends Fragment {

    LinearLayout mainLayout;
    List<Test> allTests;
    List<Homework> allHomeworks;
    Activity activity;
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_agenda, container, false);

        mainLayout = root.findViewById(R.id.agenda_linear_layout);
        activity = requireActivity();

        addDateViewsAsync();

        return root;
    }

    private void addDateViewsAsync() {

        new Thread(() -> {
            allTests = GlobalVariables.gS.getAllTestsWithoutDetails(null, true);
            allHomeworks = GlobalVariables.gS.getAllHomeworksWithoutDetails(null, true);
            activity.runOnUiThread(this::addDateViews);
        }).start();

    }

    private void addDateViews() {
        for (int i = 0; i < 10; i++) {
            DateView view = new DateView(requireContext(), null);
            view.setId(View.generateViewId());
            mainLayout.addView(view);
        }
    }
}
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

package com.giua.app.ui.fragments.home;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.R;
import com.giua.app.ThreadManager;
import com.giua.objects.Vote;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

public class HomeFragment extends Fragment implements IGiuaAppFragment {

    ThreadManager threadManager;
    LineChart chart;
    Activity activity;
    View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);

        chart = root.findViewById(R.id.home_mean_chart);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.setNoDataText("Nessun voto");
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.getLegend().setTextSize(13);
        chart.getLegend().setWordWrapEnabled(true);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.invalidate();
        threadManager = new ThreadManager();
        activity = requireActivity();

        loadDataAndViews();
        return root;
    }

    @Override
    public void loadDataAndViews() {
        threadManager.addAndRun(() -> {
            Map<String, List<Vote>> allVotes = GlobalVariables.gS.getAllVotes(false);

            requireActivity().runOnUiThread(() -> {
                setupMeanVotesText(allVotes);
                chart.setData(generateLineData(allVotes));
                chart.invalidate();
            });
        });
    }

    @Override
    public void addViews() {
    }

    private LineData generateLineData(Map<String, List<Vote>> allVotes) {
        List<Entry> entriesFirstQuarter = new ArrayList<>();
        List<Entry> entriesSecondQuarter = new ArrayList<>();
        int i = 0;

        for (String subject : allVotes.keySet()) {
            for (Vote vote : Objects.requireNonNull(allVotes.get(subject))) {
                if (!vote.isAsterisk) {
                    if (vote.isFirstQuarterly)
                        entriesFirstQuarter.add(new Entry(i, vote.toFloat()));
                    else
                        entriesSecondQuarter.add(new Entry(i, vote.toFloat()));
                    i++;
                }
            }
        }

        LineDataSet lineDataSetFirstQuarter = new LineDataSet(entriesFirstQuarter, "Primo quadrimestre");
        lineDataSetFirstQuarter.setDrawCircles(false);
        lineDataSetFirstQuarter.setDrawCircleHole(false);
        lineDataSetFirstQuarter.setDrawValues(false);
        lineDataSetFirstQuarter.setLineWidth(3);
        lineDataSetFirstQuarter.setDrawFilled(true);
        lineDataSetFirstQuarter.setColor(Color.argb(255, 5, 157, 192));
        lineDataSetFirstQuarter.setFillColor(Color.argb(255, 5, 157, 192));
        lineDataSetFirstQuarter.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet lineDataSetSecondQuarter = new LineDataSet(entriesSecondQuarter, "Secondo quadrimestre");
        lineDataSetSecondQuarter.setDrawCircles(false);
        lineDataSetSecondQuarter.setDrawCircleHole(false);
        lineDataSetSecondQuarter.setDrawValues(false);
        lineDataSetSecondQuarter.setLineWidth(3);
        lineDataSetSecondQuarter.setDrawFilled(true);
        lineDataSetSecondQuarter.setColor(Color.argb(255, 0, 88, 189));
        lineDataSetSecondQuarter.setFillColor(Color.argb(255, 0, 88, 189));
        lineDataSetSecondQuarter.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        return new LineData(lineDataSetFirstQuarter, lineDataSetSecondQuarter);
    }

    private void setupMeanVotesText(Map<String, List<Vote>> allVotes) {
        DecimalFormat df = new DecimalFormat("0.00");
        float meanFirstQuarter = getMeanOfAllVotes(allVotes);

        if (meanFirstQuarter == 0f)
            ((TextView) root.findViewById(R.id.home_txt_mean)).setTextColor(getResources().getColorStateList(R.color.non_vote, activity.getTheme()));
        else if (meanFirstQuarter >= 6)
            ((TextView) root.findViewById(R.id.home_txt_mean)).setTextColor(getResources().getColorStateList(R.color.good_vote_darker, activity.getTheme()));
        else if (meanFirstQuarter < 6 && meanFirstQuarter >= 5)
            ((TextView) root.findViewById(R.id.home_txt_mean)).setTextColor(getResources().getColorStateList(R.color.middle_vote, activity.getTheme()));
        else
            ((TextView) root.findViewById(R.id.home_txt_mean)).setTextColor(getResources().getColorStateList(R.color.bad_vote, activity.getTheme()));
        if (meanFirstQuarter == 0f)
            ((TextView) root.findViewById(R.id.home_txt_mean)).setText("/");
        else
            ((TextView) root.findViewById(R.id.home_txt_mean)).setText(df.format(meanFirstQuarter));

    }

    private float getMeanOfAllVotes(Map<String, List<Vote>> votes) {
        List<Float> allMeans = new Vector<>();
        for (String subject : votes.keySet()) {
            float mean = 0f;
            float voteCounter = 0;     //Conta solamente i voti che ci sono e non gli asterischi

            for (Vote vote : Objects.requireNonNull(votes.get(subject))) {      //Cicla ogni voto della materia
                if (vote.value.length() > 0 && !vote.isAsterisk) {
                    mean += vote.toFloat();
                    voteCounter++;
                }
            }

            if (voteCounter > 0) {
                mean /= voteCounter;
                allMeans.add(mean);
            }
        }

        float meanOfMeans = 0f; //La media delle singole medie delle materie
        for (Float f : allMeans) {
            meanOfMeans += f;
        }

        if (meanOfMeans == 0f)
            return 0f;

        return meanOfMeans / allMeans.size();
    }

    @Override
    public void onDestroyView() {
        threadManager.destroyAllAndNullMe();
        super.onDestroyView();
    }
}

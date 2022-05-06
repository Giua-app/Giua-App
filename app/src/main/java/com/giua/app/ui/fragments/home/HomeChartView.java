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

package com.giua.app.ui.fragments.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.giua.app.R;
import com.giua.objects.Vote;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class HomeChartView extends LinearLayout {
    private List<List<Entry>> allEntries;
    private List<String> allTexts;
    private List<Integer> allColors;
    private final LineChart chart;

    public HomeChartView(Context context){
        super(context);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_home_mean_chart, this);

        chart = findViewById(R.id.view_home_mean_chart);

        setupChart(context);
    }

    public void refreshData(Context context, String newTitle, double newMean, List<List<Entry>> allNewEntries, List<String> allNewTexts, @ColorInt List<Integer> allNewColors){
        TextView tvTitle = findViewById(R.id.view_home_title);

        allEntries = allNewEntries;
        allTexts = allNewTexts;
        allColors = allNewColors;

        tvTitle.setText(newTitle);
        setTvMeanText(context, newMean);

        chart.setData(generateLineData());
        chart.invalidate();
    }

    public void refreshData(Context context, String newTitle, double newMean, List<Entry> newEntries, String newText, @ColorInt int newColor){
        TextView tvTitle = findViewById(R.id.view_home_title);

        allEntries = new Vector<>(3);
        allTexts = new Vector<>(3);
        allColors = new Vector<>(3);

        allEntries.add(newEntries);
        allColors.add(newColor);
        allTexts.add(newText);

        tvTitle.setText(newTitle);
        setTvMeanText(context, newMean);

        chart.setData(generateLineData());
        chart.invalidate();
    }

    private void setupChart(Context context) {
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(true);
        chart.getAxisRight().setTextSize(14);
        chart.getAxisRight().setTextColor(context.getResources().getColor(R.color.night_white_light_black, context.getTheme()));
        chart.getAxisRight().setAxisMinimum(0f);
        chart.getAxisRight().setAxisMaximum(10f);
        chart.getAxisRight().setLabelCount(5, false);
        chart.getXAxis().setEnabled(false);
        chart.setNoDataText("Nessun voto");
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.getLegend().setTextSize(13);
        chart.getLegend().setTextColor(context.getResources().getColor(R.color.night_white_light_black, context.getTheme()));
        chart.getLegend().setWordWrapEnabled(true);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.invalidate();
    }

    private LineData generateLineData() {

        LineDataSet[] allLineDataSets = new LineDataSet[allTexts.size()];

        for(int i = 0; i < allTexts.size(); i++){
            allLineDataSets[i] = getLineForChart(allEntries.get(i), allTexts.get(i),  allColors.get(i));
        }

        return new LineData(allLineDataSets);
    }

    private LineDataSet getLineForChart(List<Entry> entries, String text, @ColorInt int color) {
        LineDataSet lineDataSet = new LineDataSet(entries, text);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setLineWidth(3);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setColor(color);
        lineDataSet.setFillColor(color);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        return lineDataSet;
    }

    private void setTvMeanText(Context context, double mean) {
        TextView tvMean = findViewById(R.id.view_home_txt_mean);
        DecimalFormat df = new DecimalFormat("0.00");

        if(mean == 0f)
            tvMean.setTextColor(context.getResources().getColorStateList(R.color.non_vote, context.getTheme()));
        else if(mean >= 6f)
            tvMean.setTextColor(context.getResources().getColorStateList(R.color.good_vote_darker, context.getTheme()));
        else if(mean < 6f && mean >= 5f)
            tvMean.setTextColor(context.getResources().getColorStateList(R.color.middle_vote, context.getTheme()));
        else
            tvMean.setTextColor(context.getResources().getColorStateList(R.color.bad_vote, context.getTheme()));

        tvMean.setBackground(null);
        tvMean.setText(df.format(mean));
    }

}

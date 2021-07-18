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

package com.giua.app.ui.lezioni;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.giua.app.R;
import com.giua.objects.Lesson;
import com.giua.webscraper.GiuaScraper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LezioniFragment extends Fragment {

    GiuaScraper gS;
    TextView tvPrevDate;
    TextView tvCurrentDate;
    TextView tvNextDate;
    ImageView calendarBtn;
    ImageView obscureLayout;
    TextView tvNoElements;
    TextView tvDetailArgs;
    TextView tvDetailActs;
    ProgressBar pbLoadingContent;
    FrameLayout frameLayout;
    CalendarView calendarView;
    FragmentActivity activity;
    LinearLayout lessonsLayout;
    LinearLayout lessonDetailLayout;
    ScrollView scrollView;
    List<Lesson> allLessons;
    Date currentDate;
    Calendar calendar;
    long lastCallTime = 0;
    SimpleDateFormat formatterForScraping = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
    SimpleDateFormat formatterForVisualize = new SimpleDateFormat("dd-MM-yyyy", Locale.ITALIAN);

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lezioni, container, false);

        Intent intent = requireActivity().getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");

        tvPrevDate = root.findViewById(R.id.lezioni_fragment_prev_date);
        tvCurrentDate = root.findViewById(R.id.lezioni_fragment_current_date);
        tvNextDate = root.findViewById(R.id.lezioni_fragment_next_date);
        lessonsLayout = root.findViewById(R.id.lezioni_fragment_lessons_layout);
        calendarBtn = root.findViewById(R.id.lezioni_fragment_image_view);
        calendarView = root.findViewById(R.id.lezioni_fragment_calendar_view);
        obscureLayout = root.findViewById(R.id.obscure_layout_image_button3);
        frameLayout = root.findViewById(R.id.lezioni_fragment_frame_layout);
        tvNoElements = root.findViewById(R.id.lezioni_fragment_no_elements_view);
        pbLoadingContent = root.findViewById(R.id.lezioni_fragment_loading_content);
        scrollView = root.findViewById(R.id.lezioni_fragment_scroll_lessons_view);
        lessonDetailLayout = root.findViewById(R.id.lezioni_fragment_lesson_detail);
        tvDetailArgs = root.findViewById(R.id.lezioni_fragment_lesson_detail_args);
        tvDetailActs = root.findViewById(R.id.lezioni_fragment_lesson_detail_acts);

        activity = requireActivity();
        calendar = Calendar.getInstance();
        currentDate = new Date();

        tvPrevDate.setText(formatterForVisualize.format(getPrevDate(currentDate)));
        tvCurrentDate.setText(formatterForVisualize.format(currentDate));
        tvNextDate.setText(formatterForVisualize.format(getNextDate(currentDate)));

        tvPrevDate.setOnClickListener(this::prevDateOnClick);
        tvNextDate.setOnClickListener(this::nextDateOnClick);
        calendarBtn.setOnClickListener(this::calendarBtnOnClick);
        obscureLayout.setOnClickListener(this::obscureLayoutOnClick);
        calendarView.setOnDateChangeListener(this::calendarOnChangeDateListener);

        addLessonViewsAsync();

        return root;
    }

    private void addLessonViewsAsync() {
        pbLoadingContent.setVisibility(View.VISIBLE);

        if (System.nanoTime() - lastCallTime > 700000000) {     //Anti click spam
            new Thread(() -> {
                lastCallTime = System.nanoTime();
                allLessons = gS.getAllLessons(formatterForScraping.format(currentDate), true);
                activity.runOnUiThread(() -> {
                    addLessonViews();
                    pbLoadingContent.setVisibility(View.GONE);
                });
            }).start();
        } else
            lastCallTime = System.nanoTime();
    }


    /**
     * Aggiunge le lezioni nella UI
     */
    private void addLessonViews() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lessonsLayout.removeAllViews();

        params.setMargins(20, 40, 20, 0);
        tvNoElements.setVisibility(View.GONE);

        if (allLessons.size() == 1 && !allLessons.get(0).exists) {
            tvNoElements.setVisibility(View.VISIBLE);
        } else {
            for (Lesson lesson : allLessons) {
                LessonView lessonView = new LessonView(requireContext(), null, lesson);
                lessonView.setId(View.generateViewId());
                lessonView.setLayoutParams(params);
                lessonView.setOnClickListener(this::lessonViewOnClick);

                lessonsLayout.addView(lessonView);
            }
        }
    }

    private void lessonViewOnClick(View view) {
        lessonDetailLayout.setVisibility(View.VISIBLE);
        obscureLayout.setVisibility(View.VISIBLE);

        if (!((LessonView) view).lesson.arguments.equals(""))
            tvDetailArgs.setText(Html.fromHtml("<b>Argomenti:</b> " + ((LessonView) view).lesson.arguments, Html.FROM_HTML_MODE_COMPACT));
        else
            tvDetailArgs.setText(Html.fromHtml("<b>Argomenti:</b> Non specificati", Html.FROM_HTML_MODE_COMPACT));

        if (!((LessonView) view).lesson.activities.equals(""))
            tvDetailActs.setText(Html.fromHtml("<b>Attività:</b> " + ((LessonView) view).lesson.activities, Html.FROM_HTML_MODE_COMPACT));
        else
            tvDetailActs.setText(Html.fromHtml("<b>Attività:</b> Non specificata", Html.FROM_HTML_MODE_COMPACT));
    }

    private void calendarOnChangeDateListener(CalendarView view, int year, int month, int dayOfMonth) {
        try {
            currentDate = getCurrentDate(formatterForScraping.parse(year + "-" + (month + 1) + "-" + dayOfMonth));
            tvCurrentDate.setText(formatterForVisualize.format(currentDate));
            tvPrevDate.setText(formatterForVisualize.format(getPrevDate(currentDate)));
            tvNextDate.setText(formatterForVisualize.format(getNextDate(currentDate)));
            addLessonViewsAsync();
            obscureLayout.callOnClick();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void obscureLayoutOnClick(View view) {
        frameLayout.setVisibility(View.GONE);
        obscureLayout.setVisibility(View.GONE);
        lessonDetailLayout.setVisibility(View.GONE);
    }

    private void calendarBtnOnClick(View view) {
        frameLayout.setVisibility(View.VISIBLE);
        obscureLayout.setVisibility(View.VISIBLE);
    }

    private void prevDateOnClick(View view) {
        currentDate = getPrevDate(currentDate);
        calendarView.setDate(currentDate.getTime());
        tvNextDate.setText(tvCurrentDate.getText());
        tvCurrentDate.setText(tvPrevDate.getText());
        tvPrevDate.setText(formatterForVisualize.format(getPrevDate(currentDate)));
        addLessonViewsAsync();
    }

    private void nextDateOnClick(View view) {
        currentDate = getNextDate(currentDate);
        calendarView.setDate(currentDate.getTime());
        tvPrevDate.setText(tvCurrentDate.getText());
        tvCurrentDate.setText(tvNextDate.getText());
        tvNextDate.setText(formatterForVisualize.format(getNextDate(currentDate)));
        addLessonViewsAsync();
    }

    private Date getCurrentDate(Date date) {
        calendar.setTime(date);
        return calendar.getTime();
    }

    private Date getNextDate(Date date) {
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }

    private Date getPrevDate(Date date) {
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime();
    }
}
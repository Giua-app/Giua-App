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

import com.giua.app.DrawerActivity;
import com.giua.app.GlobalVariables;
import com.giua.app.R;
import com.giua.objects.Lesson;
import com.giua.webscraper.GiuaScraperExceptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LezioniFragment extends Fragment {

    ImageView imgNextDate;
    ImageView imgPrevDate;
    TextView tvCurrentDate;
    ImageView imgCalendar;
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
    Date todayDate;
    Date yesterdayDate;
    Date tomorrowDate;
    Thread threadWaiter;
    SimpleDateFormat formatterForScraping = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
    SimpleDateFormat formatterForVisualize = new SimpleDateFormat("dd-MM-yyyy", Locale.ITALIAN);

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lezioni, container, false);

        tvCurrentDate = root.findViewById(R.id.lezioni_fragment_current_date);
        lessonsLayout = root.findViewById(R.id.lezioni_fragment_lessons_layout);
        imgCalendar = root.findViewById(R.id.lezioni_fragment_calendar_img);
        calendarView = root.findViewById(R.id.lezioni_fragment_calendar_view);
        obscureLayout = root.findViewById(R.id.obscure_layout_image_button3);
        frameLayout = root.findViewById(R.id.lezioni_fragment_frame_layout);
        tvNoElements = root.findViewById(R.id.lezioni_fragment_no_elements_view);
        pbLoadingContent = root.findViewById(R.id.lezioni_fragment_loading_content);
        scrollView = root.findViewById(R.id.lezioni_fragment_scroll_lessons_view);
        lessonDetailLayout = root.findViewById(R.id.lezioni_fragment_lesson_detail);
        tvDetailArgs = root.findViewById(R.id.lezioni_fragment_lesson_detail_args);
        tvDetailActs = root.findViewById(R.id.lezioni_fragment_lesson_detail_acts);
        imgNextDate = root.findViewById(R.id.lezioni_fragment_img_next_date);
        imgPrevDate = root.findViewById(R.id.lezioni_fragment_img_prev_date);

        activity = requireActivity();
        calendar = Calendar.getInstance();
        currentDate = new Date();

        todayDate = currentDate;
        yesterdayDate = getPrevDate(currentDate);
        tomorrowDate = getNextDate(currentDate);

        tvCurrentDate.setText("Oggi");

        imgPrevDate.setOnClickListener(this::prevDateOnClick);
        imgNextDate.setOnClickListener(this::nextDateOnClick);
        imgCalendar.setOnClickListener(this::calendarBtnOnClick);
        tvCurrentDate.setOnClickListener(this::calendarBtnOnClick);
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
                try {
                    allLessons = GlobalVariables.gS.getAllLessons(formatterForScraping.format(currentDate), true);
                    activity.runOnUiThread(() -> {
                        addLessonViews();
                        pbLoadingContent.setVisibility(View.GONE);
                    });
                } catch (GiuaScraperExceptions.InternetProblems e) {
                    DrawerActivity.setErrorMessage(getString(R.string.your_connection_error), lessonDetailLayout);
                    activity.runOnUiThread(() -> pbLoadingContent.setVisibility(View.GONE));
                    activity.runOnUiThread(() -> tvNoElements.setVisibility(View.VISIBLE));
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    DrawerActivity.setErrorMessage(getString(R.string.site_connection_error), lessonDetailLayout);
                    activity.runOnUiThread(() -> pbLoadingContent.setVisibility(View.GONE));
                    activity.runOnUiThread(() -> tvNoElements.setVisibility(View.VISIBLE));
                }
            }).start();
        } else {
            lastCallTime = System.nanoTime();
        }
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
            setTextWithNames();
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

    private void setTextWithNames() {
        if (currentDate.compareTo(todayDate) == 0)
            tvCurrentDate.setText("Oggi");
        else if (currentDate.compareTo(yesterdayDate) == 0)
            tvCurrentDate.setText("Ieri");
        else if (currentDate.compareTo(tomorrowDate) == 0)
            tvCurrentDate.setText("Domani");
        else
            tvCurrentDate.setText(formatterForVisualize.format(currentDate));
    }

    private void prevDateOnClick(View view) {
        currentDate = getPrevDate(currentDate);
        calendarView.setDate(currentDate.getTime());
        setTextWithNames();
        addLessonViewsAsync();
    }

    private void nextDateOnClick(View view) {
        currentDate = getNextDate(currentDate);
        calendarView.setDate(currentDate.getTime());
        setTextWithNames();
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
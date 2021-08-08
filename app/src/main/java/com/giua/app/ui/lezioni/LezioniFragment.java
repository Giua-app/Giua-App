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
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.giua.app.DrawerActivity;
import com.giua.app.GlobalVariables;
import com.giua.app.R;
import com.giua.app.ui.ObscureLayoutView;
import com.giua.objects.Lesson;
import com.giua.webscraper.GiuaScraperExceptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LezioniFragment extends Fragment {

    TextView tvCurrentDate;
    ObscureLayoutView obscureLayout;
    TextView tvNoElements;
    TextView tvDetailArgs;
    TextView tvDetailActs;
    ImageView ivCalendarImage;
    ProgressBar pbLoadingContent;
    FrameLayout frameLayout;
    Button btnConfirmDate;
    CalendarView calendarView;
    FragmentActivity activity;
    LinearLayout lessonsLayout;
    LinearLayout lessonDetailLayout;
    CardView bottomCardView;
    List<Lesson> allLessons;
    Date currentDate;
    Calendar calendar;
    Date todayDate;
    Date yesterdayDate;
    Date tomorrowDate;
    View root;
    SimpleDateFormat formatterForScraping = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
    SimpleDateFormat formatterForVisualize = new SimpleDateFormat("dd-MM-yyyy", Locale.ITALIAN);
    long lastCallTime = 0;
    boolean hasCompletedLoading = false;
    boolean isSpammingClick = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_lezioni, container, false);

        tvCurrentDate = root.findViewById(R.id.lezioni_current_date);
        lessonsLayout = root.findViewById(R.id.lezioni_lessons_layout);
        calendarView = root.findViewById(R.id.lezioni_calendar_view);
        obscureLayout = root.findViewById(R.id.lezioni_obscure_view);
        frameLayout = root.findViewById(R.id.lezioni_frame_layout);
        tvNoElements = root.findViewById(R.id.lezioni_no_elements_view);
        pbLoadingContent = root.findViewById(R.id.lezioni_loading_content);
        lessonDetailLayout = root.findViewById(R.id.lezioni_lesson_detail);
        tvDetailArgs = root.findViewById(R.id.lezioni_lesson_detail_args);
        tvDetailActs = root.findViewById(R.id.lezioni_lesson_detail_acts);
        bottomCardView = root.findViewById(R.id.lezioni_bottom_card_view);
        btnConfirmDate = root.findViewById(R.id.lezioni_btn_confirm_date);
        ivCalendarImage = root.findViewById(R.id.lezioni_calendar_image_view);

        activity = requireActivity();
        calendar = Calendar.getInstance();
        currentDate = new Date();

        todayDate = currentDate;
        yesterdayDate = getPrevDate(currentDate);
        tomorrowDate = getNextDate(currentDate);

        tvCurrentDate.setText("Oggi");

        root.findViewById(R.id.lezioni_img_next_date).setOnClickListener(this::nextDateOnClick);
        root.findViewById(R.id.lezioni_img_prev_date).setOnClickListener(this::prevDateOnClick);
        ivCalendarImage.setOnClickListener(this::tvCurrentDateOnClick);
        tvCurrentDate.setOnClickListener(this::tvCurrentDateOnClick);
        obscureLayout.setOnClickListener(this::obscureLayoutOnClick);
        calendarView.setOnDateChangeListener(this::calendarOnChangeDateListener);
        btnConfirmDate.setOnClickListener(this::btnConfirmDateOnClick);

        addLessonViewsAsync();

        return root;
    }

    private void addLessonViewsAsync() {
        pbLoadingContent.setVisibility(View.VISIBLE);
        hasCompletedLoading = false;

        if (!isSpammingClick && System.nanoTime() - lastCallTime > 500000000) {     //Anti click spam
            new Thread(() -> {
                lastCallTime = System.nanoTime();
                try {
                    allLessons = GlobalVariables.gS.getAllLessons(formatterForScraping.format(currentDate), true);
                    if (allLessons == null)
                        return;
                    hasCompletedLoading = true;
                    activity.runOnUiThread(() -> {
                        lessonsLayout.removeAllViews();
                        addLessonViews();
                        pbLoadingContent.setVisibility(View.GONE);
                    });
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        DrawerActivity.setErrorMessage(getString(R.string.your_connection_error), root);
                        pbLoadingContent.setVisibility(View.GONE);
                        tvNoElements.setVisibility(View.VISIBLE);
                    });
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        DrawerActivity.setErrorMessage(getString(R.string.site_connection_error), root);
                        pbLoadingContent.setVisibility(View.GONE);
                        tvNoElements.setVisibility(View.VISIBLE);
                    });
                }
            }).start();
        } else {
            isSpammingClick = true;
            btnConfirmDate.setVisibility(View.VISIBLE);
            pbLoadingContent.setVisibility(View.GONE);
            tvNoElements.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Aggiunge le lezioni nella UI
     */
    private void addLessonViews() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

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

    private void btnConfirmDateOnClick(View view) {
        btnConfirmDate.setVisibility(View.GONE);
        isSpammingClick = false;
        addLessonViewsAsync();
    }

    private void lessonViewOnClick(View view) {
        lessonDetailLayout.setVisibility(View.VISIBLE);
        obscureLayout.setVisibility(View.VISIBLE);
        bottomCardView.setZ(-10f);
        btnConfirmDate.setZ(-10f);
        ivCalendarImage.setZ(-10f);

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
        ivCalendarImage.setZ(13.75f);
        bottomCardView.setZ(13.75f);
        btnConfirmDate.setZ(13.75f);
    }

    private void tvCurrentDateOnClick(View view) {
        frameLayout.setVisibility(View.VISIBLE);
        obscureLayout.setVisibility(View.VISIBLE);
        ivCalendarImage.setZ(-10f);
        bottomCardView.setZ(-10f);
        btnConfirmDate.setZ(-10f);
    }

    private void setTextWithNames() {
        String s = formatterForVisualize.format(currentDate);
        if (s.equals(formatterForVisualize.format(todayDate)))
            tvCurrentDate.setText("Oggi");
        else if (s.equals(formatterForVisualize.format(yesterdayDate)))
            tvCurrentDate.setText("Ieri");
        else if (s.equals(formatterForVisualize.format(tomorrowDate)))
            tvCurrentDate.setText("Domani");
        else
            tvCurrentDate.setText(formatterForVisualize.format(currentDate));
    }

    private void prevDateOnClick(View view) {
        currentDate = getPrevDate(currentDate);
        calendarView.setDate(currentDate.getTime());
        lessonsLayout.removeAllViews();
        setTextWithNames();
        addLessonViewsAsync();
    }

    private void nextDateOnClick(View view) {
        currentDate = getNextDate(currentDate);
        calendarView.setDate(currentDate.getTime());
        lessonsLayout.removeAllViews();
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
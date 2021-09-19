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

package com.giua.app.ui.fragments.lessons;

import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.R;
import com.giua.app.ThreadManager;
import com.giua.app.ui.fragments.ObscureLayoutView;
import com.giua.objects.Lesson;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LessonsFragment extends Fragment implements IGiuaAppFragment {

    TextView tvCurrentDate;
    ObscureLayoutView obscureLayoutView;
    TextView tvNoElements;
    TextView tvVisualizerArguments;
    TextView tvVisualizerActivities;
    ImageView ivCalendarImage;
    ProgressBar pbLoadingContent;
    FrameLayout frameLayout;
    Button btnConfirmDate;
    CalendarView calendarView;
    FragmentActivity activity;
    LinearLayout viewsLayout;
    LinearLayout visualizerLayout;
    CardView bottomCardView;
    List<Lesson> allLessons;
    Date currentDate;
    Calendar calendar;
    Date todayDate;
    Date yesterdayDate;
    Date tomorrowDate;
    View root;
    ThreadManager threadManager;
    SwipeRefreshLayout swipeRefreshLayout;
    SimpleDateFormat formatterForScraping = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
    SimpleDateFormat formatterForVisualize = new SimpleDateFormat("dd-MM-yyyy", Locale.ITALIAN);
    long lastCallTime = 0;
    boolean hasCompletedLoading = false;
    boolean isSpammingClick = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_lessons, container, false);

        tvCurrentDate = root.findViewById(R.id.lezioni_current_date);
        viewsLayout = root.findViewById(R.id.lezioni_lessons_layout);
        calendarView = root.findViewById(R.id.lezioni_calendar_view);
        obscureLayoutView = root.findViewById(R.id.lezioni_obscure_view);
        frameLayout = root.findViewById(R.id.lezioni_frame_layout);
        tvNoElements = root.findViewById(R.id.lezioni_no_elements_view);
        pbLoadingContent = root.findViewById(R.id.lezioni_loading_content);
        visualizerLayout = root.findViewById(R.id.lezioni_visualizer_layout);
        tvVisualizerArguments = root.findViewById(R.id.lezioni_visualizer_arguments);
        tvVisualizerActivities = root.findViewById(R.id.lezioni_visualizer_activities);
        bottomCardView = root.findViewById(R.id.lezioni_bottom_card_view);
        btnConfirmDate = root.findViewById(R.id.lezioni_btn_confirm_date);
        ivCalendarImage = root.findViewById(R.id.lezioni_calendar_image_view);
        swipeRefreshLayout = root.findViewById(R.id.lezioni_swipe_refresh_layout);

        activity = requireActivity();
        calendar = Calendar.getInstance();
        currentDate = new Date();
        threadManager = new ThreadManager();

        todayDate = currentDate;
        yesterdayDate = getPrevDate(currentDate);
        tomorrowDate = getNextDate(currentDate);

        tvCurrentDate.setText("Oggi");
        tvVisualizerArguments.setMovementMethod(new ScrollingMovementMethod());
        tvVisualizerActivities.setMovementMethod(new ScrollingMovementMethod());

        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        root.findViewById(R.id.lezioni_img_next_date).setOnClickListener(this::nextDateOnClick);
        root.findViewById(R.id.lezioni_img_prev_date).setOnClickListener(this::prevDateOnClick);
        ivCalendarImage.setOnClickListener(this::tvCurrentDateOnClick);
        tvCurrentDate.setOnClickListener(this::tvCurrentDateOnClick);
        obscureLayoutView.setOnClickListener(this::obscureLayoutOnClick);
        calendarView.setOnDateChangeListener(this::calendarOnChangeDateListener);
        btnConfirmDate.setOnClickListener(this::btnConfirmDateOnClick);

        loadDataAndViews();

        return root;
    }

    @Override
    public void loadDataAndViews() {
        pbLoadingContent.setVisibility(View.VISIBLE);
        viewsLayout.removeAllViews();
        hasCompletedLoading = false;

        if (!isSpammingClick && System.nanoTime() - lastCallTime > 500000000) {     //Anti click spam
            threadManager.addAndRun(() -> {
                lastCallTime = System.nanoTime();
                try {
                    allLessons = GlobalVariables.gS.getAllLessons(formatterForScraping.format(currentDate), true);
                    if (allLessons == null)
                        return;
                    hasCompletedLoading = true;
                    activity.runOnUiThread(this::addViews);
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    //Errore di connessione
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.your_connection_error), root);
                        pbLoadingContent.setVisibility(View.GONE);
                        tvNoElements.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    //Errore di connessione al registro
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.site_connection_error), root);
                        pbLoadingContent.setVisibility(View.GONE);
                        tvNoElements.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                    //Errore di connessione al registro
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                        pbLoadingContent.setVisibility(View.GONE);
                        tvNoElements.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }
            });
        } else {
            //l'utente sta spammando
            isSpammingClick = true;
            btnConfirmDate.setVisibility(View.VISIBLE);
            pbLoadingContent.setVisibility(View.GONE);
            tvNoElements.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Aggiunge le lezioni nella UI
     */
    @Override
    public void addViews() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(20, 40, 20, 0);
        tvNoElements.setVisibility(View.GONE);

        if (allLessons.isEmpty() || (allLessons.size() == 1 && !allLessons.get(0).exists)) {
            tvNoElements.setVisibility(View.VISIBLE);
        } else {
            for (Lesson lesson : allLessons) {
                LessonView lessonView = new LessonView(requireContext(), null, lesson);
                lessonView.setId(View.generateViewId());
                lessonView.setLayoutParams(params);
                lessonView.setOnClickListener(this::lessonViewOnClick);

                viewsLayout.addView(lessonView);
            }
        }

        pbLoadingContent.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    //region Listeners

    private void onRefresh() {
        lastCallTime = 0;
        hasCompletedLoading = false;
        isSpammingClick = false;
        loadDataAndViews();
    }

    private void btnConfirmDateOnClick(View view) {
        btnConfirmDate.setVisibility(View.GONE);
        isSpammingClick = false;
        loadDataAndViews();
    }

    private void lessonViewOnClick(View view) {
        //Dettagli delle lezioni
        visualizerLayout.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_show_effect));
        obscureLayoutView.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_show_effect));
        visualizerLayout.setVisibility(View.VISIBLE);
        obscureLayoutView.setVisibility(View.VISIBLE);
        bottomCardView.setZ(-10f);
        btnConfirmDate.setZ(-10f);
        ivCalendarImage.setZ(-10f);

        if (!((LessonView) view).lesson.arguments.equals(""))
            tvVisualizerArguments.setText(Html.fromHtml("<b>Argomenti:</b> " + ((LessonView) view).lesson.arguments, Html.FROM_HTML_MODE_COMPACT));
        else
            tvVisualizerArguments.setText(Html.fromHtml("<b>Argomenti:</b> Non specificati", Html.FROM_HTML_MODE_COMPACT));

        if (!((LessonView) view).lesson.activities.equals(""))
            tvVisualizerActivities.setText(Html.fromHtml("<b>Attività:</b> " + ((LessonView) view).lesson.activities, Html.FROM_HTML_MODE_COMPACT));
        else
            tvVisualizerActivities.setText(Html.fromHtml("<b>Attività:</b> Non specificata", Html.FROM_HTML_MODE_COMPACT));
    }

    private void calendarOnChangeDateListener(CalendarView view, int year, int month, int dayOfMonth) {
        try {
            currentDate = getCurrentDate(formatterForScraping.parse(year + "-" + (month + 1) + "-" + dayOfMonth));
            setTextWithNames();
            loadDataAndViews();
            obscureLayoutView.callOnClick();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void obscureLayoutOnClick(View view) {
        frameLayout.setVisibility(View.GONE);
        visualizerLayout.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_hide_effect));
        obscureLayoutView.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_hide_effect));
        obscureLayoutView.setVisibility(View.GONE);
        visualizerLayout.setVisibility(View.GONE);
        ivCalendarImage.setZ(13.75f);
        bottomCardView.setZ(13.75f);
        btnConfirmDate.setZ(13.75f);
    }

    private void tvCurrentDateOnClick(View view) {
        frameLayout.setVisibility(View.VISIBLE);
        obscureLayoutView.setVisibility(View.VISIBLE);
        ivCalendarImage.setZ(-10f);
        bottomCardView.setZ(-10f);
        btnConfirmDate.setZ(-10f);
    }

    private void prevDateOnClick(View view) {
        //data precedente
        currentDate = getPrevDate(currentDate);
        calendarView.setDate(currentDate.getTime());
        viewsLayout.removeAllViews();
        setTextWithNames();
        loadDataAndViews();
    }

    private void nextDateOnClick(View view) {
        //prossimo giorno
        currentDate = getNextDate(currentDate);
        calendarView.setDate(currentDate.getTime());
        viewsLayout.removeAllViews();
        setTextWithNames();
        loadDataAndViews();
    }

    //endregion

    //region Metodi

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

    private void setErrorMessage(String message, View root) {
        Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    //endregion


    @Override
    public void onDestroyView() {
        threadManager.destroyAllAndNullMe();
        super.onDestroyView();
    }
}
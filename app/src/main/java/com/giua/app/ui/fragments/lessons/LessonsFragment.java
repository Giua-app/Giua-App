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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.R;
import com.giua.app.ui.activities.DrawerActivity;
import com.giua.app.ui.views.ObscureLayoutView;
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
    TextView tvVisualizerSupport;
    ImageView ivCalendarImage;
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
    SwipeRefreshLayout swipeRefreshLayout;
    SimpleDateFormat formatterForScraping = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
    SimpleDateFormat formatterForVisualize = new SimpleDateFormat("dd-MM-yyyy", Locale.ITALIAN);
    long lastCallTime = 0;
    boolean hasCompletedLoading = false;
    boolean isSpammingClick = false;
    boolean isFragmentDestroyed = false;
    boolean offlineMode = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_lessons, container, false);

        tvCurrentDate = root.findViewById(R.id.lezioni_current_date);
        viewsLayout = root.findViewById(R.id.lezioni_lessons_layout);
        calendarView = root.findViewById(R.id.lezioni_calendar_view);
        obscureLayoutView = root.findViewById(R.id.lezioni_obscure_view);
        frameLayout = root.findViewById(R.id.lezioni_frame_layout);
        tvNoElements = root.findViewById(R.id.lezioni_no_elements_view);
        visualizerLayout = root.findViewById(R.id.lezioni_visualizer_layout);
        tvVisualizerArguments = root.findViewById(R.id.lezioni_visualizer_arguments);
        tvVisualizerActivities = root.findViewById(R.id.lezioni_visualizer_activities);
        tvVisualizerSupport = root.findViewById(R.id.lezioni_visualizer_support);
        bottomCardView = root.findViewById(R.id.lezioni_bottom_card_view);
        btnConfirmDate = root.findViewById(R.id.lezioni_btn_confirm_date);
        ivCalendarImage = root.findViewById(R.id.lezioni_calendar_image_view);
        swipeRefreshLayout = root.findViewById(R.id.lezioni_swipe_refresh_layout);

        activity = requireActivity();
        calendar = Calendar.getInstance();
        currentDate = new Date();

        todayDate = currentDate;
        yesterdayDate = getPrevDate(currentDate);
        tomorrowDate = getNextDate(currentDate);

        tvCurrentDate.setText("Oggi");
        tvVisualizerArguments.setMovementMethod(new ScrollingMovementMethod());
        tvVisualizerActivities.setMovementMethod(new ScrollingMovementMethod());
        tvVisualizerSupport.setMovementMethod(new ScrollingMovementMethod());

        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        root.findViewById(R.id.lezioni_img_next_date).setOnClickListener(this::nextDateOnClick);
        root.findViewById(R.id.lezioni_img_prev_date).setOnClickListener(this::prevDateOnClick);
        ivCalendarImage.setOnClickListener(this::tvCurrentDateOnClick);
        tvCurrentDate.setOnClickListener(this::tvCurrentDateOnClick);
        obscureLayoutView.setOnClickListener(this::obscureLayoutOnClick);
        calendarView.setOnDateChangeListener(this::calendarOnChangeDateListener);
        btnConfirmDate.setOnClickListener(this::btnConfirmDateOnClick);

        offlineMode = activity.getIntent().getBooleanExtra("offline", false);

        return root;
    }

    @Override
    public void loadOfflineDataAndViews() {
        tvNoElements.setText("Non disponibile offline");
        tvNoElements.setVisibility(View.VISIBLE);
    }

    @Override
    public void loadDataAndViews() {
        tvNoElements.setVisibility(View.GONE);
        viewsLayout.removeAllViews();
        hasCompletedLoading = false;
        swipeRefreshLayout.setRefreshing(true);

        if (!isSpammingClick && System.nanoTime() - lastCallTime > 500_000_000) {     //Anti click spam
            GlobalVariables.gsThread.addTask(() -> {
                lastCallTime = System.nanoTime();
                try {
                    allLessons = GlobalVariables.gS.getLessonsPage(true).getAllLessonsFromDate(currentDate);

                    if (allLessons == null || isFragmentDestroyed) return;

                    hasCompletedLoading = true;
                    activity.runOnUiThread(this::addViews);
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    //Errore di connessione
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.your_connection_error), root);
                        tvNoElements.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    //Errore di connessione al registro
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.site_connection_error), root);
                        tvNoElements.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                    //Errore di connessione al registro
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                        tvNoElements.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                } catch (GiuaScraperExceptions.NotLoggedIn e) {
                    activity.runOnUiThread(() -> {
                        ((DrawerActivity) activity).startActivityManager();
                    });
                }
            });
        } else {
            //l'utente sta spammando
            isSpammingClick = true;
            btnConfirmDate.setVisibility(View.VISIBLE);
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
        tvNoElements.setText(R.string.no_elements);
        tvNoElements.setVisibility(View.GONE);

        if (allLessons.isEmpty())
            tvNoElements.setVisibility(View.VISIBLE);
        else if (allLessons.size() == 1 && !allLessons.get(0)._exists) {
            if (allLessons.get(0).isError)
                tvNoElements.setText(allLessons.get(0).arguments);
            tvNoElements.setVisibility(View.VISIBLE);
        } else {
            for (Lesson lesson : allLessons) {
                LessonView lessonView = new LessonView(requireActivity(), null, lesson);
                lessonView.setId(View.generateViewId());
                lessonView.setLayoutParams(params);
                lessonView.setOnClickListener(this::lessonViewOnClick);

                viewsLayout.addView(lessonView);
            }
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onBackPressed() {
        if (obscureLayoutView.isShown()) {
            obscureLayoutView.performClick();
            return true;
        }
        return false;
    }

    //region Listeners

    private void onRefresh() {
        lastCallTime = 0;
        hasCompletedLoading = false;
        isSpammingClick = false;
        if (!offlineMode)
            loadDataAndViews();
        else
            loadOfflineDataAndViews();
    }

    private void btnConfirmDateOnClick(View view) {
        btnConfirmDate.setVisibility(View.GONE);
        isSpammingClick = false;
        if (!offlineMode)
            loadDataAndViews();
        else
            loadOfflineDataAndViews();
    }

    private void lessonViewOnClick(View view) {
        //Dettagli delle lezioni
        visualizerLayout.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.visualizer_show_effect));
        visualizerLayout.setVisibility(View.VISIBLE);
        obscureLayoutView.show();
        bottomCardView.setZ(-10f);
        btnConfirmDate.setZ(-10f);
        ivCalendarImage.setZ(-10f);
        tvVisualizerArguments.scrollTo(0, 0);
        tvVisualizerActivities.scrollTo(0, 0);
        tvVisualizerSupport.scrollTo(0, 0);

        if (!((LessonView) view).lesson.arguments.equals(""))
            tvVisualizerArguments.setText(Html.fromHtml("<b>Argomenti:</b> " + ((LessonView) view).lesson.arguments, Html.FROM_HTML_MODE_COMPACT));
        else
            tvVisualizerArguments.setText(Html.fromHtml("<b>Argomenti:</b> (Non specificati)", Html.FROM_HTML_MODE_COMPACT));

        if (!((LessonView) view).lesson.activities.equals(""))
            tvVisualizerActivities.setText(Html.fromHtml("<b>Attività:</b> " + ((LessonView) view).lesson.activities, Html.FROM_HTML_MODE_COMPACT));
        else
            tvVisualizerActivities.setText(Html.fromHtml("<b>Attività:</b> (Non specificata)", Html.FROM_HTML_MODE_COMPACT));

        if (!((LessonView) view).lesson.support.equals("")) {
            tvVisualizerSupport.setText(Html.fromHtml("<b>Sostegno:</b> " + ((LessonView) view).lesson.support, Html.FROM_HTML_MODE_COMPACT));
            tvVisualizerSupport.setVisibility(View.VISIBLE);
        }
    }

    private void calendarOnChangeDateListener(CalendarView view, int year, int month, int dayOfMonth) {
        try {
            currentDate = getCurrentDate(formatterForScraping.parse(year + "-" + (month + 1) + "-" + dayOfMonth));
            setTextWithNames();
            if (!offlineMode)
                loadDataAndViews();
            else
                loadOfflineDataAndViews();
            obscureLayoutView.callOnClick();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void obscureLayoutOnClick(View view) {
        if (visualizerLayout.getVisibility() == View.VISIBLE)
            visualizerLayout.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.visualizer_hide_effect));
        if (frameLayout.getVisibility() == View.VISIBLE)
            frameLayout.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.visualizer_hide_effect));
        tvVisualizerSupport.setVisibility(View.GONE);
        frameLayout.setVisibility(View.GONE);
        visualizerLayout.setVisibility(View.GONE);
        obscureLayoutView.hide();
        ivCalendarImage.setZ(13.75f);
        bottomCardView.setZ(13.75f);
        btnConfirmDate.setZ(13.75f);
    }

    private void tvCurrentDateOnClick(View view) {
        frameLayout.setVisibility(View.VISIBLE);
        obscureLayoutView.show();
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
        if (!offlineMode)
            loadDataAndViews();
        else
            loadOfflineDataAndViews();
    }

    private void nextDateOnClick(View view) {
        //prossimo giorno
        currentDate = getNextDate(currentDate);
        calendarView.setDate(currentDate.getTime());
        viewsLayout.removeAllViews();
        setTextWithNames();
        if (!offlineMode)
            loadDataAndViews();
        else
            loadOfflineDataAndViews();
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
        if (!isFragmentDestroyed)
            Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    //endregion

    @Override
    public void onStart() {
        if (!offlineMode)
            loadDataAndViews();
        else
            loadOfflineDataAndViews();
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        isFragmentDestroyed = true;
        super.onDestroyView();
    }
}
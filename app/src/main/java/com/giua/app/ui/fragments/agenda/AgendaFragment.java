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

package com.giua.app.ui.fragments.agenda;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.LoggerManager;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.app.ThreadManager;
import com.giua.app.ui.fragments.ObscureLayoutView;
import com.giua.objects.AgendaObject;
import com.giua.objects.Homework;
import com.giua.objects.Test;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class AgendaFragment extends Fragment implements IGiuaAppFragment {

    LinearLayout viewsLayout;
    List<AgendaObject> allAgendaObjects;
    TextView tvNoElements;
    Activity activity;
    View root;
    Date currentDisplayedDate;
    Calendar currentDate;
    ProgressBar pbForDetails;
    Calendar calendar;
    ScrollView scrollView;
    MaterialCalendarView calendarView;
    SwipeRefreshLayout swipeRefreshLayout;
    ObscureLayoutView obscureLayoutView;
    ThreadManager threadManager;
    long lastRequestTime = 0;
    boolean isLoadingData = false;
    LoggerManager loggerManager;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_agenda, container, false);
        loggerManager = new LoggerManager("AgendaFragment", getContext());
        loggerManager.d("onCreateView chiamato");

        scrollView = root.findViewById(R.id.agenda_scroll_view);
        calendarView = root.findViewById(R.id.agenda_calendar);
        viewsLayout = root.findViewById(R.id.agenda_views_layout);
        tvNoElements = root.findViewById(R.id.agenda_no_elements_text);
        obscureLayoutView = root.findViewById(R.id.agenda_obscure_layout);
        pbForDetails = root.findViewById(R.id.agenda_progress_bar_details);
        swipeRefreshLayout = root.findViewById(R.id.agenda_swipe_refresh_layout);

        activity = requireActivity();

        if (SettingsData.getSettingBoolean(requireContext(), SettingKey.DEMO_MODE)) {
            calendar = Calendar.getInstance();
            calendar.set(2021, 11, 1);
            currentDisplayedDate = calendar.getTime();
        } else {    //No demo
            calendar = Calendar.getInstance();
            currentDisplayedDate = calendar.getTime();
            currentDate = calendar;
        }

        calendarView.setTitleMonths(new CharSequence[]{
                "Gennaio", "Febbraio", "Marzo",
                "Aprile", "Maggio", "Giugno",
                "Luglio", "Agosto", "Settembre",
                "Ottobre", "Novembre", "Dicembre"
        });

        calendarView.setWeekDayLabels(new CharSequence[]{
                "Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"
        });

        threadManager = new ThreadManager();

        calendarView.setOnDateChangedListener(this::onDateChangeListener);
        calendarView.setOnMonthChangedListener(this::onMonthChangeListener);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        obscureLayoutView.setOnClickListener(this::obscureLayoutOnClick);

        loadDataAndViews();

        return root;
    }

    @Override
    public void loadDataAndViews() {
        loggerManager.d("Carico views...");
        if (!isLoadingData && System.nanoTime() - lastRequestTime > 500_000_000) {  //Anti click spam
            lastRequestTime = System.nanoTime();
            swipeRefreshLayout.setRefreshing(true);
            threadManager.addAndRun(() -> {
                isLoadingData = true;
                try {
                    allAgendaObjects = GlobalVariables.gS.getAgendaPage(false)
                            .getAllAgendaObjectsWithoutDetails(getCurrentYear() + "-" + getNumberForScraping(Integer.parseInt(getCurrentMonth())));

                    if (allAgendaObjects.isEmpty()) {
                        activity.runOnUiThread(() -> {
                            viewsLayout.removeViews(1, viewsLayout.getChildCount() - 1);
                            tvNoElements.setVisibility(View.VISIBLE);
                        });
                    } else {
                        activity.runOnUiThread(() -> {
                            viewsLayout.removeViews(1, viewsLayout.getChildCount() - 1);
                            decorateCalendar();
                        });
                    }
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.your_connection_error), root);
                        tvNoElements.setVisibility(View.VISIBLE);
                    });
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.site_connection_error), root);
                        tvNoElements.setVisibility(View.VISIBLE);
                    });
                } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                        tvNoElements.setVisibility(View.VISIBLE);
                    });
                } finally {
                    activity.runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        isLoadingData = false;
                    });
                }
            });
        }
    }

    @Override
    public void addViews() {
    }

    @Override
    public boolean onBackPressed() {
        if (obscureLayoutView.isShown()) {
            obscureLayoutView.performClick();
            return true;
        }
        return false;
    }

    private void addViews(List<AgendaObject> objectsToShow) {
        loggerManager.d("Aggiungo views...");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 50, 0, 0);

        for (AgendaObject agendaObject : objectsToShow) {
            AgendaView agendaView = new AgendaView(activity, null, agendaObject);
            agendaView.setLayoutParams(params);
            agendaView.setId(View.generateViewId());
            viewsLayout.addView(agendaView);
        }

        swipeRefreshLayout.setRefreshing(false);
        isLoadingData = false;
    }

    private void decorateCalendar() {
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                String d = String.valueOf(day.getDay());
                String m = String.valueOf(day.getMonth());
                String y = String.valueOf(day.getYear());
                if (d.length() == 1)
                    d = "0" + d;
                for (AgendaObject agendaObject : allAgendaObjects) {
                    if (agendaObject.date.equals(y + "-" + m + "-" + d)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void decorate(DayViewFacade view) {
                GradientDrawable shape;
                shape = ((GradientDrawable) ResourcesCompat.getDrawable(activity.getResources(), R.drawable.agenda_calendar_homeworks, activity.getTheme()));
                shape.setStroke(0, ResourcesCompat.getColorStateList(activity.getResources(), R.color.adaptive_color_text, activity.getTheme()));
                shape.setColor(ResourcesCompat.getColorStateList(activity.getResources(), R.color.main_color, activity.getTheme()));
                view.setBackgroundDrawable(shape);
            }
        });

        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                Calendar cal = Calendar.getInstance();
                return cal.get(Calendar.DAY_OF_MONTH) == day.getDay() && cal.get(Calendar.MONTH) + 1 == day.getMonth() && cal.get(Calendar.YEAR) == day.getYear();
            }

            @Override
            public void decorate(DayViewFacade view) {
                GradientDrawable shape = ((GradientDrawable) ResourcesCompat.getDrawable(activity.getResources(), R.drawable.agenda_calendar_today, activity.getTheme()));
                view.setBackgroundDrawable(shape);
            }
        });

        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                String d = String.valueOf(day.getDay());
                String m = String.valueOf(day.getMonth());
                String y = String.valueOf(day.getYear());
                Calendar cal = Calendar.getInstance();
                if (d.length() == 1)
                    d = "0" + d;
                for (AgendaObject agendaObject : allAgendaObjects) {
                    if (agendaObject.date.equals(y + "-" + m + "-" + d) && cal.get(Calendar.DAY_OF_MONTH) == day.getDay() && cal.get(Calendar.MONTH) + 1 == day.getMonth() && cal.get(Calendar.YEAR) == day.getYear())
                        return true;
                }
                return false;
            }

            @Override
            public void decorate(DayViewFacade view) {
                GradientDrawable shape = ((GradientDrawable) ResourcesCompat.getDrawable(activity.getResources(), R.drawable.agenda_calendar_test, activity.getTheme()));
                shape.setColor(ResourcesCompat.getColorStateList(getResources(), R.color.main_color, activity.getTheme()));
                shape.setStroke(4, ResourcesCompat.getColorStateList(activity.getResources(), R.color.adaptive_color_text, activity.getTheme()));
                view.setBackgroundDrawable(shape);
            }
        });

    }

    //region Listeners

    private void onDateChangeListener(MaterialCalendarView materialCalendarView, CalendarDay calendarDay, boolean b) {
        if (isLoadingData) return;
        threadManager.addAndRun(() -> {
            //Conto quanti oggetti ci sono nel giorno cliccato
            List<AgendaObject> agendaObjectsOfTheDay = new Vector<>();
            String day = String.valueOf(calendarDay.getDay());
            String month = String.valueOf(calendarDay.getMonth());
            String year = String.valueOf(calendarDay.getYear());
            if (day.length() == 1)
                day = "0" + day;
            AgendaObject agendaObjectComparator = new AgendaObject(
                    day,
                    month,
                    year,
                    year + "-" + month + "-" + day
            );

            for (AgendaObject agendaObject : allAgendaObjects)
                if (agendaObject.date.equals(agendaObjectComparator.date))
                    agendaObjectsOfTheDay.add(agendaObject);

            if (!agendaObjectsOfTheDay.isEmpty()) {
                activity.runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(true);
                    isLoadingData = true;
                    viewsLayout.removeViews(1, viewsLayout.getChildCount() - 1);
                    tvNoElements.setVisibility(View.GONE);
                    viewsLayout.scrollTo(0, 0);
                });
                for (AgendaObject agendaObject : agendaObjectsOfTheDay) {
                    List<AgendaObject> objectsToShow = new Vector<>();
                    if (agendaObject.getRepresentingClass() == Test.class)
                        objectsToShow.addAll(GlobalVariables.gS.getAgendaPage(false).getTests(agendaObject.date));
                    else if (agendaObject.getRepresentingClass() == Homework.class)
                        objectsToShow.addAll(GlobalVariables.gS.getAgendaPage(false).getHomeworks(agendaObject.date));
                    else if (agendaObject.getRepresentingClass() == com.giua.objects.Activity.class)
                        objectsToShow.addAll(GlobalVariables.gS.getAgendaPage(false).getActivities(agendaObject.date));
                    activity.runOnUiThread(() -> addViews(objectsToShow));
                }
            } else {
                activity.runOnUiThread(() -> {
                    tvNoElements.setVisibility(View.VISIBLE);
                    isLoadingData = false;
                    swipeRefreshLayout.setRefreshing(false);
                    viewsLayout.removeViews(1, viewsLayout.getChildCount() - 1);
                });
            }
        });


        materialCalendarView.clearSelection();
    }

    private void onMonthChangeListener(MaterialCalendarView materialCalendarView, CalendarDay calendarDay) {
        Calendar selectedDay = Calendar.getInstance();
        selectedDay.set(calendarDay.getYear(), calendarDay.getMonth() - 1, calendarDay.getDay());
        if (calendarDay.getYear() == currentDate.get(Calendar.YEAR) && calendarDay.getMonth() - 1 == currentDate.get(Calendar.MONTH))
            currentDisplayedDate = currentDate.getTime();
        else
            currentDisplayedDate = selectedDay.getTime();
        loadDataAndViews();
    }

    private void obscureLayoutOnClick(View view) {
        obscureLayoutView.hide();
    }

    private void onRefresh() {
        loadDataAndViews();
    }
    //endregion

    //region Metodi

    private Date getNextMonth(Date date) {
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    private Date getPrevMonth(Date date) {
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        return calendar.getTime();
    }

    private String getCurrentMonth() {
        SimpleDateFormat formatterForMonth = new SimpleDateFormat("MM", Locale.ITALIAN);
        return formatterForMonth.format(currentDisplayedDate);
    }

    private String getCurrentYear() {
        SimpleDateFormat formatterForYear = new SimpleDateFormat("yyyy", Locale.ITALIAN);
        return formatterForYear.format(currentDisplayedDate);
    }

    private String getMonthFromNumber(int number) {
        switch (number) {
            case 1:
                return "Gennaio";
            case 2:
                return "Febbraio";
            case 3:
                return "Marzo";
            case 4:
                return "Aprile";
            case 5:
                return "Maggio";
            case 6:
                return "Giugno";
            case 7:
                return "Luglio";
            case 8:
                return "Agosto";
            case 9:
                return "Settembre";
            case 10:
                return "Ottobre";
            case 11:
                return "Novembre";
            case 12:
                return "Dicembre";
        }
        return "Data inesistente";
    }

    private int getNumberFromMonth(String month) {
        switch (month) {
            case "Gennaio":
                return 1;
            case "Febbraio":
                return 2;
            case "Marzo":
                return 3;
            case "Aprile":
                return 4;
            case "Maggio":
                return 5;
            case "Giugno":
                return 6;
            case "Luglio":
                return 7;
            case "Agosto":
                return 8;
            case "Settembre":
                return 9;
            case "Ottobre":
                return 10;
            case "Novembre":
                return 11;
            case "Dicembre":
                return 12;
        }
        return -1;
    }

    private String getNumberForScraping(int n) {
        if (n >= 10)
            return String.valueOf(n);
        else
            return "0" + n;
    }

    private void setErrorMessage(String message, View root) {
        if (!threadManager.isDestroyed())
            Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }
    //endregion

    @Override
    public void onDestroyView() {
        loggerManager.d("onDestroyView chiamato");
        threadManager.destroyAllAndNullMe();
        super.onDestroyView();
    }
}
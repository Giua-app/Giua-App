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

package com.giua.app.ui.fragments.agenda;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
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

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.LoggerManager;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.app.ui.activities.DrawerActivity;
import com.giua.objects.AgendaObject;
import com.giua.objects.Homework;
import com.giua.objects.Test;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class AgendaFragment extends Fragment implements IGiuaAppFragment {

    LinearLayout viewsLayout;
    List<AgendaObject> allAgendaObjects = new Vector<>();
    TextView tvNoElements;
    Activity activity;
    View root;
    Date currentDisplayedDate;
    Calendar currentDate;
    ProgressBar pbForDetails;
    Calendar calendar;
    ScrollView scrollView;
    CompactCalendarView calendarView;
    SwipeRefreshLayout swipeRefreshLayout;
    SimpleDateFormat dateFormatForMonth;
    SimpleDateFormat dateFormatForYear;
    long lastRequestTime = 0;
    long lastOnDateCall = 0;
    boolean isLoadingData = false;
    boolean firstStart = true;
    boolean isFragmentDestroyed = false;
    boolean offlineMode = false;
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
        pbForDetails = root.findViewById(R.id.agenda_progress_bar_details);
        swipeRefreshLayout = root.findViewById(R.id.agenda_swipe_refresh_layout);

        activity = requireActivity();
        allAgendaObjects = new Vector<>();
        dateFormatForMonth = new SimpleDateFormat("MM", Locale.ITALIAN);
        dateFormatForYear = new SimpleDateFormat("yyyy", Locale.ITALIAN);

        offlineMode = activity.getIntent().getBooleanExtra("offline", false);

        if (SettingsData.getSettingBoolean(requireActivity(), SettingKey.DEMO_MODE)) {
            calendar = Calendar.getInstance();
            calendar.set(2021, 11, 1);
            currentDisplayedDate = calendar.getTime();
        } else {    //No demo
            calendar = Calendar.getInstance();
            currentDisplayedDate = calendar.getTime();
            currentDate = calendar;
        }

        calendarView = root.findViewById(R.id.agenda_calendar);
        calendarView.setUseThreeLetterAbbreviation(true);

        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                onDayChanged(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                scrollView.requestDisallowInterceptTouchEvent(false);
                swipeRefreshLayout.requestDisallowInterceptTouchEvent(false);
                onMonthChanged(firstDayOfNewMonth);
            }
        });

        ((TextView) root.findViewById(R.id.agenda_calendar_month)).setText(getMonthFromNumber(Integer.parseInt(dateFormatForMonth.format(new Date()))));
        ((TextView) root.findViewById(R.id.agenda_calendar_year)).setText(dateFormatForYear.format(new Date()));

        root.findViewById(R.id.agenda_prev_month).setOnClickListener((view -> calendarView.scrollLeft()));
        root.findViewById(R.id.agenda_next_month).setOnClickListener((view -> calendarView.scrollRight()));
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);

        activity.getSystemService(NotificationManager.class).cancel(13);
        activity.getSystemService(NotificationManager.class).cancel(14);

        return root;
    }

    @Override
    public void loadOfflineDataAndViews() {
        if (!isLoadingData && System.nanoTime() - lastRequestTime > 500_000_000) {  //Anti click spam
            lastRequestTime = System.nanoTime();
            swipeRefreshLayout.setRefreshing(true);
            new Thread(() -> {
                isLoadingData = true;
                try {
                    allAgendaObjects = GlobalVariables.gS.getAgendaPage(false)
                            .getAllAgendaObjectsWithoutDetails(getCurrentYear() + "-" + getNumberForScraping(Integer.parseInt(getCurrentMonth())));

                    if (allAgendaObjects.isEmpty()) {
                        activity.runOnUiThread(() -> {
                            viewsLayout.removeViews(1, viewsLayout.getChildCount() - 1);
                            tvNoElements.setText("Non ci sono compiti per questo mese");
                            tvNoElements.setVisibility(View.VISIBLE);
                        });
                    } else {
                        activity.runOnUiThread(() -> {
                            viewsLayout.removeViews(1, viewsLayout.getChildCount() - 1);
                            try {
                                refreshCalendarEvents();
                            } catch (ParseException ignored) {
                            }
                        });
                    }
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.your_connection_error), root));
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.site_connection_error), root));
                } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                    activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root));
                } catch (GiuaScraperExceptions.NotLoggedIn e) {
                    activity.runOnUiThread(() -> {
                        ((DrawerActivity) activity).startActivityManager();
                    });
                } finally {
                    activity.runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        isLoadingData = false;
                    });
                    if (firstStart) {
                        showDateActivities(Calendar.getInstance());
                        firstStart = false;
                    }
                }

            }).start();
        }
    }

    @Override
    public void loadDataAndViews() {
        loggerManager.d("Carico views...");
        if (!isLoadingData && System.nanoTime() - lastRequestTime > 500_000_000) {  //Anti click spam
            lastRequestTime = System.nanoTime();
            swipeRefreshLayout.setRefreshing(true);
            GlobalVariables.gsThread.addTask(() -> {
                isLoadingData = true;
                try {
                    allAgendaObjects = GlobalVariables.gS.getAgendaPage(false)
                            .getAllAgendaObjectsWithoutDetails(getCurrentYear() + "-" + getNumberForScraping(Integer.parseInt(getCurrentMonth())));

                    if (allAgendaObjects.isEmpty()) {
                        activity.runOnUiThread(() -> {
                            viewsLayout.removeViews(1, viewsLayout.getChildCount() - 1);
                            tvNoElements.setText("Non ci sono compiti per questo mese");
                            tvNoElements.setVisibility(View.VISIBLE);
                        });
                    } else {
                        activity.runOnUiThread(() -> {
                            viewsLayout.removeViews(1, viewsLayout.getChildCount() - 1);
                            try {
                                refreshCalendarEvents();
                            } catch (ParseException ignored) {
                            }
                        });
                    }
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.your_connection_error), root));
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.site_connection_error), root));
                } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                    activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root));
                } catch (GiuaScraperExceptions.NotLoggedIn e) {
                    activity.runOnUiThread(() -> {
                        ((DrawerActivity) activity).startActivityManager();
                    });
                } finally {
                    activity.runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        isLoadingData = false;
                    });
                    if (firstStart) {
                        showDateActivities(Calendar.getInstance());
                        firstStart = false;
                    }
                }

            });
        }
    }

    @Override
    public void addViews() {
    }

    @Override
    public boolean onBackPressed() {
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
    }

    //region Listeners

    private void showDateActivities(Calendar selectedDate) {
        //Conto quanti oggetti ci sono nel giorno cliccato
        List<AgendaObject> agendaObjectsOfTheDay = new Vector<>();
        String day = String.valueOf(selectedDate.get(Calendar.DAY_OF_MONTH));
        String month = String.valueOf(selectedDate.get(Calendar.MONTH) + 1);
        String year = String.valueOf(selectedDate.get(Calendar.YEAR));
        if (day.length() == 1)
            day = "0" + day;
        if (month.length() == 1)
            month = "0" + month;
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
                viewsLayout.removeViews(1, viewsLayout.getChildCount() - 1);
                tvNoElements.setVisibility(View.GONE);
                viewsLayout.scrollTo(0, 0);
            });
            for (AgendaObject agendaObject : agendaObjectsOfTheDay) {
                List<AgendaObject> objectsToShow = new Vector<>();
                try {
                    if (agendaObject.getRepresentingClass() == Test.class)
                        objectsToShow.addAll(GlobalVariables.gS.getAgendaPage(false).getTests(agendaObject.date));
                    else if (agendaObject.getRepresentingClass() == Homework.class)
                        objectsToShow.addAll(GlobalVariables.gS.getAgendaPage(false).getHomeworks(agendaObject.date));
                    else if (agendaObject.getRepresentingClass() == com.giua.objects.Activity.class)
                        objectsToShow.addAll(GlobalVariables.gS.getAgendaPage(false).getActivities(agendaObject.date));
                    else if (agendaObject.getRepresentingClass() == com.giua.objects.InterviewAgenda.class)
                        objectsToShow.addAll(GlobalVariables.gS.getAgendaPage(false).getInterviews(agendaObject.date));
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.your_connection_error), root);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                    return;
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.site_connection_error), root);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                    return;
                } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                    return;
                }
                activity.runOnUiThread(() -> addViews(objectsToShow));
            }
            activity.runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
            isLoadingData = false;
        } else {
            activity.runOnUiThread(() -> {
                tvNoElements.setText("Non ci sono compiti per questo giorno");
                tvNoElements.setVisibility(View.VISIBLE);
                isLoadingData = false;
                swipeRefreshLayout.setRefreshing(false);
                viewsLayout.removeViews(1, viewsLayout.getChildCount() - 1);
            });
        }
    }

    private void onDayChanged(Date dateClicked) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.setTime(dateClicked);
        currentDisplayedDate = selectedDate.getTime();
        if (System.nanoTime() - lastOnDateCall < 400_000_000) {
            setErrorMessage("Clicca più lentamente!", root);
            return;
        }
        if (isLoadingData) return;
        lastOnDateCall = System.nanoTime();
        isLoadingData = true;
        swipeRefreshLayout.setRefreshing(true);
        GlobalVariables.gsThread.addTask(() -> showDateActivities(selectedDate));
    }

    private void onMonthChanged(Date firstDayOfNewMonth) {
        tvNoElements.setVisibility(View.GONE);
        Calendar selectedDay = Calendar.getInstance();
        selectedDay.setTime(firstDayOfNewMonth);
        if (selectedDay.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) && selectedDay.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH))
            currentDisplayedDate = currentDate.getTime();
        else
            currentDisplayedDate = selectedDay.getTime();
        ((TextView) root.findViewById(R.id.agenda_calendar_month)).setText(getMonthFromNumber(Integer.parseInt(dateFormatForMonth.format(firstDayOfNewMonth))));
        ((TextView) root.findViewById(R.id.agenda_calendar_year)).setText(dateFormatForYear.format(firstDayOfNewMonth));
        loadDataAndViews();
    }

    private void refreshCalendarEvents() throws ParseException {
        calendarView.removeAllEvents();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);

        for (AgendaObject agendaObject : allAgendaObjects) {
            Event event;
            if (agendaObject.getRepresentingClass() == Homework.class) {
                event = new Event(
                        ResourcesCompat.getColor(activity.getResources(), R.color.agenda_views_cyan, activity.getTheme()),
                        simpleDateFormat.parse(agendaObject.date).getTime()
                );
            } else if (agendaObject.getRepresentingClass() == Test.class) {
                event = new Event(
                        ResourcesCompat.getColor(activity.getResources(), R.color.agenda_views_orange, activity.getTheme()),
                        simpleDateFormat.parse(agendaObject.date).getTime()
                );
            } else if (agendaObject.getRepresentingClass() == com.giua.objects.Activity.class) {
                event = new Event(
                        ResourcesCompat.getColor(activity.getResources(), R.color.agenda_views_green, activity.getTheme()),
                        simpleDateFormat.parse(agendaObject.date).getTime()
                );
            } else {
                event = new Event(
                        ResourcesCompat.getColor(activity.getResources(), R.color.agenda_views_purple, activity.getTheme()),
                        simpleDateFormat.parse(agendaObject.date).getTime()
                );
            }

            calendarView.addEvent(event);
        }


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

    private void onRefresh() {
        if (!offlineMode)
            loadDataAndViews();
        else
            loadOfflineDataAndViews();
    }

    @Override
    public void onDestroyView() {
        loggerManager.d("onDestroyView chiamato");
        isFragmentDestroyed = true;
        super.onDestroyView();
    }
}
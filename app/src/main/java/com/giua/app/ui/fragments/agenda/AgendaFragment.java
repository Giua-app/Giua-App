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

import com.applikeysolutions.cosmocalendar.model.Month;
import com.applikeysolutions.cosmocalendar.selection.SingleSelectionManager;
import com.applikeysolutions.cosmocalendar.settings.appearance.ConnectedDayIconPosition;
import com.applikeysolutions.cosmocalendar.settings.lists.connected_days.ConnectedDays;
import com.applikeysolutions.cosmocalendar.view.CalendarView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
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
    CalendarView calendarView;
    SwipeRefreshLayout swipeRefreshLayout;
    ObscureLayoutView obscureLayoutView;
    ThreadManager threadManager;
    long lastRequestTime = 0;
    long lastOnDateCall = 0;
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
        allAgendaObjects = new Vector<>();

        if (SettingsData.getSettingBoolean(requireContext(), SettingKey.DEMO_MODE)) {
            calendar = Calendar.getInstance();
            calendar.set(2021, 11, 1);
            currentDisplayedDate = calendar.getTime();
        } else {    //No demo
            calendar = Calendar.getInstance();
            currentDisplayedDate = calendar.getTime();
            currentDate = calendar;
        }

        threadManager = new ThreadManager();

        calendarView.setSelectionManager(new SingleSelectionManager(this::onDateChangedListener));
        calendarView.setOnMonthChangeListener(this::onMonthChangeListener);
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
                } finally {
                    activity.runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        isLoadingData = false;
                    });
                }
            });
        }
    }

    private void refreshCalendarEvents() throws ParseException {
        Set<Long> testDaysSet = new TreeSet<>();
        Set<Long> homeworkDaysSet = new TreeSet<>();
        Set<Long> activitiesDaysSet = new TreeSet<>();
        /*
        String date1 = "12-18-2021";
        String date2 = "12-09-2021";

        SimpleDateFormat sdf1 = new SimpleDateFormat("MM-dd-yyyy");
        try {
            Date mDate1 = sdf1.parse(date1);
            long timeInMilliseconds1 = mDate1.getTime();


            Date mDate2 = sdf1.parse(date2);
            long timeInMilliseconds2 = mDate2.getTime();

            days.add(timeInMilliseconds1);
            days.add(timeInMilliseconds2);
            // can add more days to set

        } catch (Exception e) {
        }
        ConnectedDays connectedDays = new ConnectedDays(days, Color.parseColor("#000000"));
        calendarView.setConnectedDayIconPosition(ConnectedDayIconPosition.BOTTOM);
        calendarView.setConnectedDayIconRes(R.drawable.agenda_calendar_test);
        calendarView.addConnectedDays(connectedDays);
        calendarView.update();*/

        /*calendarView.setTitleMonths(new CharSequence[]{
                "Gennaio", "Febbraio", "Marzo",
                "Aprile", "Maggio", "Giugno",
                "Luglio", "Agosto", "Settembre",
                "Ottobre", "Novembre", "Dicembre"
        });

        calendarView.setWeekDayLabels(new CharSequence[]{
                "Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"
        });

        decorateCalendar();*/
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (AgendaObject agendaObject : allAgendaObjects) {
            if (agendaObject.getRepresentingClass() == Test.class)
                testDaysSet.add(simpleDateFormat.parse(agendaObject.date).getTime());
            else if (agendaObject.getRepresentingClass() == Homework.class)
                homeworkDaysSet.add(simpleDateFormat.parse(agendaObject.date).getTime());
            else if (agendaObject.getRepresentingClass() == com.giua.objects.Activity.class)
                activitiesDaysSet.add(simpleDateFormat.parse(agendaObject.date).getTime());
        }

        ConnectedDays testDays = new ConnectedDays(testDaysSet, ResourcesCompat.getColor(activity.getResources(), R.color.adaptive_color_text, activity.getTheme()));
        calendarView.setConnectedDayIconPosition(ConnectedDayIconPosition.BOTTOM);
        calendarView.setConnectedDayIconRes(R.drawable.agenda_calendar_test);
        calendarView.addConnectedDays(testDays);
        ConnectedDays homeworkDays = new ConnectedDays(homeworkDaysSet, ResourcesCompat.getColor(activity.getResources(), R.color.adaptive_color_text, activity.getTheme()));
        calendarView.setConnectedDayIconPosition(ConnectedDayIconPosition.BOTTOM);
        calendarView.setConnectedDayIconRes(R.drawable.agenda_calendar_homeworks);
        calendarView.addConnectedDays(homeworkDays);
        ConnectedDays activitiesDays = new ConnectedDays(activitiesDaysSet, ResourcesCompat.getColor(activity.getResources(), R.color.adaptive_color_text, activity.getTheme()));
        calendarView.setConnectedDayIconPosition(ConnectedDayIconPosition.BOTTOM);
        calendarView.setConnectedDayIconRes(R.drawable.agenda_calendar_activities);
        calendarView.addConnectedDays(activitiesDays);
        calendarView.update();

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

    //region Listeners

    private void onMonthChangeListener(Month month) {
        tvNoElements.setVisibility(View.GONE);
        Calendar selectedDay = month.getFirstDay().getCalendar();
        if (selectedDay.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) && selectedDay.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH))
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

    private void onDateChangedListener() {
        if (calendarView.getSelectedDates().size() <= 0) return;
        Calendar selectedDate = calendarView.getSelectedDates().get(0);
        if (System.currentTimeMillis() - lastOnDateCall < 300) {
            setErrorMessage("Clicca più lentamente!", root);
            return;
        }
        if (isLoadingData) return;
        lastOnDateCall = System.currentTimeMillis();
        isLoadingData = true;
        swipeRefreshLayout.setRefreshing(true);
        threadManager.addAndRun(() -> {
            //Conto quanti oggetti ci sono nel giorno cliccato
            List<AgendaObject> agendaObjectsOfTheDay = new Vector<>();
            String day = String.valueOf(selectedDate.get(Calendar.DAY_OF_MONTH));
            String month = String.valueOf(selectedDate.get(Calendar.MONTH) + 1);
            String year = String.valueOf(selectedDate.get(Calendar.YEAR));
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
            } else {
                activity.runOnUiThread(() -> {
                    tvNoElements.setText("Non ci sono compiti per questo giorno");
                    tvNoElements.setVisibility(View.VISIBLE);
                    isLoadingData = false;
                    swipeRefreshLayout.setRefreshing(false);
                    viewsLayout.removeViews(1, viewsLayout.getChildCount() - 1);
                });
            }
        });
    }
}
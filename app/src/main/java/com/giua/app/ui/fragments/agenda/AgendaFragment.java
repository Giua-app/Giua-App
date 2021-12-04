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
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
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
import com.giua.objects.Homework;
import com.giua.objects.PinBoardObject;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class AgendaFragment extends Fragment implements IGiuaAppFragment {

    LinearLayout viewsLayout;
    List<com.giua.objects.Activity> allActivities;
    List<Test> allTests;
    List<Homework> allHomeworks;
    List<PinBoardObject> visualizerPinBoardObjects;
    TextView tvNoElements;
    Activity activity;
    View root;
    Date currentDisplayedDate;
    Calendar currentDate;
    ProgressBar pbForDetails;
    Calendar calendar;
    ScrollView scrollView;
    MaterialCalendarView calendarView;
    HashSet<CalendarDay> objectDays;
    SwipeRefreshLayout swipeRefreshLayout;
    ObscureLayoutView obscureLayoutView;
    LinearLayout visualizerLayout;
    TextView tvVisualizerType;
    TextView tvVisualizerSubject;
    TextView tvVisualizerCreator;
    TextView tvVisualizerText;
    TextView tvVisualizerDate;
    ImageView ivVisualizerPrevBtn;
    ImageView ivVisualizerNextBtn;
    ThreadManager threadManager;
    long lastRequestTime = 0;
    int visualizerPointer = 0;
    boolean isLoadingData = false;
    boolean isLoadingDetails = false;
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
        visualizerLayout = root.findViewById(R.id.agenda_object_visualizer_layout);
        tvVisualizerType = root.findViewById(R.id.agenda_visualizer_type);
        tvVisualizerSubject = root.findViewById(R.id.agenda_visualizer_subject);
        tvVisualizerCreator = root.findViewById(R.id.agenda_visualizer_creator);
        tvVisualizerText = root.findViewById(R.id.agenda_visualizer_text);
        tvVisualizerDate = root.findViewById(R.id.agenda_visualizer_date);
        obscureLayoutView = root.findViewById(R.id.agenda_obscure_layout);
        ivVisualizerPrevBtn = root.findViewById(R.id.agenda_visualizer_prev_btn);
        ivVisualizerNextBtn = root.findViewById(R.id.agenda_visualizer_next_btn);
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

        tvVisualizerText.setMovementMethod(new ScrollingMovementMethod());
        //tvTodayText.setText(getMonthFromNumber(Integer.parseInt(getCurrentMonth())) + " " + getCurrentYear());

        calendarView.setOnDateChangedListener(this::onDateChangeListener);
        calendarView.setOnMonthChangedListener(this::onMonthChangeListener);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        ivVisualizerPrevBtn.setOnClickListener(this::ivVisualizerPrevBtnOnClick);
        ivVisualizerNextBtn.setOnClickListener(this::ivVisualizerNextBtnOnClick);
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
                try {
                    isLoadingData = true;

                    allTests = GlobalVariables.gS.getPinBoardPage(false).getAllTestsWithoutDetails(getCurrentYear() + "-" + getNumberForScraping(Integer.parseInt(getCurrentMonth())));
                    allHomeworks = GlobalVariables.gS.getPinBoardPage(false).getAllHomeworksWithoutDetails(getCurrentYear() + "-" + getNumberForScraping(Integer.parseInt(getCurrentMonth())));
                    allActivities = GlobalVariables.gS.getPinBoardPage(false).getAllActivitiesWithoutDetails(getCurrentYear() + "-" + getNumberForScraping(Integer.parseInt(getCurrentMonth())));

                    if (allTests.isEmpty() && allHomeworks.isEmpty()) {
                        activity.runOnUiThread(() -> {
                            viewsLayout.removeAllViews();
                            tvNoElements.setVisibility(View.VISIBLE);
                            swipeRefreshLayout.setRefreshing(false);
                            isLoadingData = false;
                        });
                    } else
                        activity.runOnUiThread(this::addViews);
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.your_connection_error), root);
                        tvNoElements.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);
                        isLoadingData = false;
                    });
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.site_connection_error), root);
                        tvNoElements.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);
                        isLoadingData = false;
                    });
                } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                        tvNoElements.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);
                        isLoadingData = false;
                    });
                }
                activity.runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
            });
        }
    }

    @Override
    public void addViews() {
        loggerManager.d("Aggiungo views...");
        viewsLayout.removeViews(1, viewsLayout.getChildCount() - 1);
        tvNoElements.setVisibility(View.GONE);
        viewsLayout.scrollTo(0, 0);
        objectDays = new HashSet<>();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 50, 0, 0);

        List<PinBoardObject> allObjects = new Vector<>();
        allObjects.addAll(allTests);
        allObjects.addAll(allHomeworks);
        allObjects.addAll(allActivities);
        allObjects.sort((pinBoardObject1, pinBoardObject2) -> {
            int year1 = Integer.parseInt(pinBoardObject1.year);
            int month1 = Integer.parseInt(pinBoardObject1.month);
            ;
            int day1 = Integer.parseInt(pinBoardObject1.day);
            ;
            int year2 = Integer.parseInt(pinBoardObject2.year);
            ;
            int month2 = Integer.parseInt(pinBoardObject2.month);
            ;
            int day2 = Integer.parseInt(pinBoardObject2.day);
            ;

            if (year1 == year2) {
                if (month1 == month2)
                    return Integer.compare(day1, day2);
                else
                    return Integer.compare(month1, month2);
            } else
                return Integer.compare(year1, year2);
        });

        for (PinBoardObject pinBoardObject : allObjects) {
            AgendaView agendaView = new AgendaView(activity, null, pinBoardObject);
            agendaView.setOnClickListener(this::agendaViewOnClick);
            agendaView.setId(View.generateViewId());
            agendaView.setLayoutParams(params);
            viewsLayout.addView(agendaView);
            objectDays.add(CalendarDay.from(Integer.parseInt(pinBoardObject.year), Integer.parseInt(pinBoardObject.month), Integer.parseInt(pinBoardObject.day)));
        }

        //Decorator per i giorni dei compiti
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return objectDays.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.agenda_calendar_homeworks, activity.getTheme()));
            }
        });

        //Decorator per i giorni delle verifiche che server per il calendario
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return objectDays.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.agenda_calendar_test, activity.getTheme()));
            }
        });

        //Decorator per i giorni in cui ci sono compiti e verifiche
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return objectDays.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.agenda_calendar_test_homework, activity.getTheme()));
            }
        });

        swipeRefreshLayout.setRefreshing(false);
        isLoadingData = false;
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

    private void onDateChangeListener(MaterialCalendarView materialCalendarView, CalendarDay calendarDay, boolean b) {
        //Se nel giorno cliccato ci sonon compiti o verifiche o entrambi
        if (objectDays.contains(calendarDay)) {
            int childCount = viewsLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AgendaView agendaView = (AgendaView) viewsLayout.getChildAt(i);
                boolean isDayEqual = Integer.parseInt(agendaView.pinBoardObject.day) == calendarDay.getDay();
                boolean isMonthEqual = Integer.parseInt(agendaView.pinBoardObject.month) == calendarDay.getMonth();
                boolean isYearEqual = Integer.parseInt(agendaView.pinBoardObject.year) == calendarDay.getYear();
                if (isDayEqual && isMonthEqual && isYearEqual) {
                    agendaView.callOnClick();
                    return;
                }
            }
        }
        materialCalendarView.clearSelection();
    }

    private void onMonthChangeListener(MaterialCalendarView materialCalendarView, CalendarDay calendarDay) {
        Calendar selectedDay = Calendar.getInstance();
        selectedDay.set(calendarDay.getYear(), calendarDay.getMonth() - 1, calendarDay.getDay());
        if (calendarDay.getYear() == currentDate.get(Calendar.YEAR) && calendarDay.getMonth() - 1 == currentDate.get(Calendar.MONTH))
            currentDisplayedDate = currentDate.getTime();
        else
            currentDisplayedDate = selectedDay.getTime();
        viewsLayout.removeAllViews();
        loadDataAndViews();
    }

    private void ivVisualizerNextBtnOnClick(View view) {
        int size = visualizerPinBoardObjects.size();
        tvVisualizerText.scrollTo(0, 0);
        if (visualizerPointer + 1 < size)  //Se si può ancora andare avanti punto il visualizerPointer al prossimo oggetto (quello che sta per essere visualizzato)
            visualizerPointer++;
        if (visualizerPinBoardObjects.get(visualizerPointer).getClass() == Test.class) {
            Test test = (Test) visualizerPinBoardObjects.get(visualizerPointer);
            tvVisualizerType.setText("Verifica");
            tvVisualizerSubject.setText(test.subject);
            tvVisualizerCreator.setText(test.creator);
            tvVisualizerText.setText(test.details);
            tvVisualizerDate.setText(test.date);
        } else if (visualizerPinBoardObjects.get(visualizerPointer).getClass() == Homework.class) {
            Homework homework = (Homework) visualizerPinBoardObjects.get(visualizerPointer);
            tvVisualizerType.setText("Compito");
            tvVisualizerSubject.setText(homework.subject);
            tvVisualizerCreator.setText(homework.creator);
            tvVisualizerText.setText(homework.details);
            tvVisualizerDate.setText(homework.date);
        } else if (visualizerPinBoardObjects.get(visualizerPointer).getClass() == com.giua.objects.Activity.class) {
            com.giua.objects.Activity activity = (com.giua.objects.Activity) visualizerPinBoardObjects.get(visualizerPointer);
            tvVisualizerType.setText("Attività");
            tvVisualizerSubject.setText("");
            tvVisualizerCreator.setText(activity.creator);
            tvVisualizerText.setText(activity.details);
            tvVisualizerDate.setText(activity.date);
        }
        ivVisualizerPrevBtn.setVisibility(View.VISIBLE);
        if (visualizerPointer >= size)
            ivVisualizerNextBtn.setVisibility(View.INVISIBLE);
    }

    private void ivVisualizerPrevBtnOnClick(View view) {
        int size = visualizerPinBoardObjects.size();
        tvVisualizerText.scrollTo(0, 0);
        if (visualizerPointer > 0)
            visualizerPointer--;
        if (visualizerPointer >= 0 && visualizerPointer < size) {
            if (visualizerPinBoardObjects.get(visualizerPointer).getClass() == Test.class) {
                Test test = (Test) visualizerPinBoardObjects.get(visualizerPointer);
                tvVisualizerType.setText("Verifica");
                tvVisualizerSubject.setText(test.subject);
                tvVisualizerCreator.setText(test.creator);
                tvVisualizerText.setText(test.details);
                tvVisualizerDate.setText(test.date);
            } else if (visualizerPinBoardObjects.get(visualizerPointer).getClass() == Homework.class) {
                Homework homework = (Homework) visualizerPinBoardObjects.get(visualizerPointer);
                tvVisualizerType.setText("Compito");
                tvVisualizerSubject.setText(homework.subject);
                tvVisualizerCreator.setText(homework.creator);
                tvVisualizerText.setText(homework.details);
                tvVisualizerDate.setText(homework.date);
            } else if (visualizerPinBoardObjects.get(visualizerPointer).getClass() == com.giua.objects.Activity.class) {
                com.giua.objects.Activity activity = (com.giua.objects.Activity) visualizerPinBoardObjects.get(visualizerPointer);
                tvVisualizerType.setText("Attività");
                tvVisualizerSubject.setText("");
                tvVisualizerCreator.setText(activity.creator);
                tvVisualizerText.setText(activity.details);
                tvVisualizerDate.setText(activity.date);
            }
            ivVisualizerNextBtn.setVisibility(View.VISIBLE);
        }
        if (visualizerPointer == 0)
            ivVisualizerPrevBtn.setVisibility(View.INVISIBLE);
    }

    private void agendaViewOnClick(View view) {
        if (!isLoadingDetails) {
            isLoadingDetails = true;
            pbForDetails.setVisibility(View.VISIBLE);
            threadManager.addAndRun(() -> {
                AgendaView agendaView = (AgendaView) view;
                visualizerPinBoardObjects = new Vector<>();
                visualizerPointer = 0;

                try {
                    if (agendaView.getRepresentedObject() == Test.class) {
                        List<Test> visualizerTests = GlobalVariables.gS.getPinBoardPage(false).getTest(agendaView.pinBoardObject.date);
                        visualizerPinBoardObjects.addAll(visualizerTests);
                    } else if (agendaView.getRepresentedObject() == Homework.class) {
                        List<Homework> visualizerHomeworks = GlobalVariables.gS.getPinBoardPage(false).getHomework(agendaView.pinBoardObject.date);
                        visualizerPinBoardObjects.addAll(visualizerHomeworks);
                    } else if (agendaView.getRepresentedObject() == Activity.class) {
                        List<com.giua.objects.Activity> visualizerActivities = GlobalVariables.gS.getPinBoardPage(false).getActivity(agendaView.pinBoardObject.date);
                        visualizerPinBoardObjects.addAll(visualizerActivities);
                    }
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.your_connection_error), root);
                        pbForDetails.setVisibility(View.GONE);
                    });
                    return;
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.site_connection_error), root);
                        pbForDetails.setVisibility(View.GONE);
                    });
                    return;
                } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                        pbForDetails.setVisibility(View.GONE);
                    });
                    return;
                }

                activity.runOnUiThread(() -> {
                    tvVisualizerText.scrollTo(0, 0);
                    if (agendaView.getRepresentedObject() == Test.class) {
                        tvVisualizerType.setText("Verifica");
                        Test test = (Test) visualizerPinBoardObjects.get(0);
                        tvVisualizerSubject.setText(test.subject);
                        tvVisualizerCreator.setText(test.creator);
                        tvVisualizerText.setText(test.details);
                        tvVisualizerDate.setText(test.date);
                    } else if (agendaView.getRepresentedObject() == Homework.class) {
                        tvVisualizerType.setText("Compito");
                        Homework homework = (Homework) visualizerPinBoardObjects.get(0);
                        tvVisualizerSubject.setText(homework.subject);
                        tvVisualizerCreator.setText(homework.creator);
                        tvVisualizerText.setText(homework.details);
                        tvVisualizerDate.setText(homework.date);
                    } else if (agendaView.getRepresentedObject() == Activity.class) {
                        tvVisualizerType.setText("Attività");
                        com.giua.objects.Activity activity = (com.giua.objects.Activity) visualizerPinBoardObjects.get(0);
                        tvVisualizerCreator.setText(activity.creator);
                        tvVisualizerText.setText(activity.details);
                        tvVisualizerDate.setText(activity.date);
                    }

                    ivVisualizerPrevBtn.setVisibility(View.INVISIBLE);
                    if (visualizerPinBoardObjects.size() + visualizerPinBoardObjects.size() <= 1) {  //E' presente solo un elemento
                        ivVisualizerPrevBtn.setVisibility(View.GONE);
                        ivVisualizerNextBtn.setVisibility(View.GONE);
                    } else
                        ivVisualizerNextBtn.setVisibility(View.VISIBLE);
                    visualizerLayout.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_show_effect));
                    visualizerLayout.setVisibility(View.VISIBLE);
                    obscureLayoutView.show();

                    isLoadingDetails = false;
                    pbForDetails.setVisibility(View.GONE);
                });
            });
        }
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
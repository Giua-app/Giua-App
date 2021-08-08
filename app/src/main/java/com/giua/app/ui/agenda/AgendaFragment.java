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

package com.giua.app.ui.agenda;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.giua.app.DrawerActivity;
import com.giua.app.GlobalVariables;
import com.giua.app.R;
import com.giua.app.ui.ObscureLayoutView;
import com.giua.objects.Homework;
import com.giua.objects.Test;
import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class AgendaFragment extends Fragment {

    LinearLayout viewsLayout;
    List<Test> allTests;
    List<Homework> allHomeworks;
    List<Test> visualizerTests;
    List<Homework> visualizerHomeworks;
    TextView tvTodayText;
    TextView tvNoElements;
    ImageView btnPrevMonth;
    ImageView btnNextMonth;
    Activity activity;
    View root;
    Date currentDate;
    ProgressBar progressBar;
    ProgressBar progressBarForDetails;
    Calendar calendar;
    SimpleDateFormat formatterForMonth = new SimpleDateFormat("MM", Locale.ITALIAN);
    SimpleDateFormat formatterForYear = new SimpleDateFormat("yyyy", Locale.ITALIAN);
    ObscureLayoutView obscureLayoutView;
    LinearLayout visualizerLayout;
    TextView tvVisualizerType;
    TextView tvVisualizerSubject;
    TextView tvVisualizerCreator;
    TextView tvVisualizerText;
    TextView tvVisualizerDate;
    ImageView ivVisualizerPrevBtn;
    ImageView ivVisualizerNextBtn;
    int visualizerCounter = 0;
    boolean isLoadingData = false;
    boolean isLoadingDetails = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_agenda, container, false);

        viewsLayout = root.findViewById(R.id.agenda_views_layout);
        tvTodayText = root.findViewById(R.id.agenda_month_text);
        tvNoElements = root.findViewById(R.id.agenda_no_elements_text);
        btnNextMonth = root.findViewById(R.id.agenda_month_next_btn);
        btnPrevMonth = root.findViewById(R.id.agenda_month_prev_btn);
        visualizerLayout = root.findViewById(R.id.agenda_object_visualizer_layout);
        tvVisualizerType = root.findViewById(R.id.agenda_visualizer_type);
        tvVisualizerSubject = root.findViewById(R.id.agenda_visualizer_subject);
        tvVisualizerCreator = root.findViewById(R.id.agenda_visualizer_creator);
        tvVisualizerText = root.findViewById(R.id.agenda_visualizer_text);
        tvVisualizerDate = root.findViewById(R.id.agenda_visualizer_date);
        obscureLayoutView = root.findViewById(R.id.agenda_obscure_layout);
        ivVisualizerPrevBtn = root.findViewById(R.id.agenda_visualizer_prev_btn);
        ivVisualizerNextBtn = root.findViewById(R.id.agenda_visualizer_next_btn);
        progressBarForDetails = root.findViewById(R.id.agenda_progress_bar_details);

        activity = requireActivity();

        calendar = Calendar.getInstance();
        currentDate = calendar.getTime();

        progressBar = new ProgressBar(requireContext(), null);

        tvTodayText.setText(getMonthFromNumber(Integer.parseInt(getCurrentMonth())) + " " + getCurrentYear());

        btnNextMonth.setOnClickListener(this::btnNextMonthOnClick);
        btnPrevMonth.setOnClickListener(this::btnPrevMonthOnClick);
        ivVisualizerPrevBtn.setOnClickListener(this::ivVisualizerPrevBtnOnClick);
        ivVisualizerNextBtn.setOnClickListener(this::ivVisualizerNextBtnOnClick);
        visualizerLayout.setOnClickListener((view) -> {
        });
        obscureLayoutView.setOnClickListener((view) -> {
            visualizerLayout.setVisibility(View.GONE);
            obscureLayoutView.setVisibility(View.GONE);
        });

        if (Integer.parseInt(getCurrentMonth()) >= 6 && Integer.parseInt(getCurrentMonth()) <= 8)
            btnNextMonth.setVisibility(View.GONE);
        if (Integer.parseInt(getCurrentMonth()) == 7)
            btnPrevMonth.setVisibility(View.GONE);

        addDateViewsAsync();

        return root;
    }

    private void btnPrevMonthOnClick(View view) {
        if (!isLoadingData) {
            currentDate = getPrevMonth(currentDate);
            if (Integer.parseInt(getCurrentMonth()) < 6 || Integer.parseInt(getCurrentMonth()) > 8)
                btnNextMonth.setVisibility(View.VISIBLE);
            if (Integer.parseInt(getCurrentMonth()) == 9)
                btnPrevMonth.setVisibility(View.GONE);
            tvTodayText.setText(getMonthFromNumber(Integer.parseInt(getCurrentMonth())) + " " + getCurrentYear());
            viewsLayout.removeAllViews();
            addDateViewsAsync();
        }
    }

    private void btnNextMonthOnClick(View view) {
        if (!isLoadingData) {
            currentDate = getNextMonth(currentDate);
            btnPrevMonth.setVisibility(View.VISIBLE);
            if (Integer.parseInt(getCurrentMonth()) >= 6 && Integer.parseInt(getCurrentMonth()) <= 8)
                btnNextMonth.setVisibility(View.GONE);
            tvTodayText.setText(getMonthFromNumber(Integer.parseInt(getCurrentMonth())) + " " + getCurrentYear());
            viewsLayout.removeAllViews();
            addDateViewsAsync();
        }
    }

    private void ivVisualizerNextBtnOnClick(View view) {
        if (visualizerCounter < visualizerTests.size() + visualizerHomeworks.size() - 1)
            visualizerCounter++;
        if (visualizerCounter < visualizerTests.size()) {
            tvVisualizerType.setText("Verifica");
            tvVisualizerSubject.setText(visualizerTests.get(visualizerCounter).subject);
            tvVisualizerCreator.setText(visualizerTests.get(visualizerCounter).creator);
            tvVisualizerText.setText(visualizerTests.get(visualizerCounter).details);
            tvVisualizerDate.setText(visualizerTests.get(visualizerCounter).date);
            ivVisualizerPrevBtn.setVisibility(View.VISIBLE);
        } else if (visualizerCounter - visualizerTests.size() < visualizerHomeworks.size()) {
            int index = visualizerCounter - visualizerTests.size();

            tvVisualizerType.setText("Compiti");
            tvVisualizerSubject.setText(visualizerHomeworks.get(index).subject);
            tvVisualizerCreator.setText(visualizerHomeworks.get(index).creator);
            tvVisualizerText.setText(visualizerHomeworks.get(index).details);
            tvVisualizerDate.setText(visualizerHomeworks.get(index).date);
            ivVisualizerPrevBtn.setVisibility(View.VISIBLE);
        }
        if (visualizerCounter >= visualizerTests.size() + visualizerHomeworks.size() - 1)
            ivVisualizerNextBtn.setVisibility(View.INVISIBLE);
    }

    private void ivVisualizerPrevBtnOnClick(View view) {
        if (visualizerCounter > 0)
            visualizerCounter--;
        if (visualizerCounter >= 0 && !visualizerTests.isEmpty()) {
            tvVisualizerType.setText("Verifica");
            tvVisualizerSubject.setText(visualizerTests.get(visualizerCounter).subject);
            tvVisualizerCreator.setText(visualizerTests.get(visualizerCounter).creator);
            tvVisualizerText.setText(visualizerTests.get(visualizerCounter).details);
            tvVisualizerDate.setText(visualizerTests.get(visualizerCounter).date);
            ivVisualizerNextBtn.setVisibility(View.VISIBLE);
        } else if (visualizerCounter - visualizerTests.size() >= 0 && !visualizerHomeworks.isEmpty()) {
            int index = visualizerCounter - visualizerTests.size();

            tvVisualizerType.setText("Compito");
            tvVisualizerSubject.setText(visualizerHomeworks.get(index).subject);
            tvVisualizerCreator.setText(visualizerHomeworks.get(index).creator);
            tvVisualizerText.setText(visualizerHomeworks.get(index).details);
            tvVisualizerDate.setText(visualizerHomeworks.get(index).date);
            ivVisualizerNextBtn.setVisibility(View.VISIBLE);
        }
        if (visualizerCounter == 0)
            ivVisualizerPrevBtn.setVisibility(View.INVISIBLE);
    }

    private void agendaViewOnClick(View view) {
        if (!isLoadingDetails) {
            isLoadingDetails = true;
            progressBarForDetails.setVisibility(View.VISIBLE);
            new Thread(() -> {
                AgendaView agendaView = (AgendaView) view;
                visualizerHomeworks = new Vector<>();
                visualizerTests = new Vector<>();
                visualizerCounter = 0;

                try {
                    if (agendaView.test != null)
                        visualizerTests = GlobalVariables.gS.getTest(agendaView.test.date);
                    if (agendaView.homework != null)
                        visualizerHomeworks = GlobalVariables.gS.getHomework(agendaView.homework.date);
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        DrawerActivity.setErrorMessage(getString(R.string.your_connection_error), root);
                        progressBarForDetails.setVisibility(View.GONE);
                    });
                    return;
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        DrawerActivity.setErrorMessage(getString(R.string.site_connection_error), root);
                        progressBarForDetails.setVisibility(View.GONE);
                    });
                    return;
                } catch (NullPointerException e) {
                    activity.runOnUiThread(() -> {
                        if (!GiuaScraper.isMyInternetWorking())
                            DrawerActivity.setErrorMessage(getString(R.string.your_connection_error), root);
                        progressBarForDetails.setVisibility(View.GONE);
                    });
                    return;
                }

                activity.runOnUiThread(() -> {
                    if (agendaView.test != null) {
                        tvVisualizerType.setText("Verifica");
                        tvVisualizerSubject.setText(visualizerTests.get(0).subject);
                        tvVisualizerCreator.setText(visualizerTests.get(0).creator);
                        tvVisualizerText.setText(visualizerTests.get(0).details);
                        tvVisualizerDate.setText(visualizerTests.get(0).date);
                    } else if (agendaView.homework != null) {
                        tvVisualizerType.setText("Compito");
                        tvVisualizerSubject.setText(visualizerHomeworks.get(0).subject);
                        tvVisualizerCreator.setText(visualizerHomeworks.get(0).creator);
                        tvVisualizerText.setText(visualizerHomeworks.get(0).details);
                        tvVisualizerDate.setText(visualizerHomeworks.get(0).date);
                    }

                    ivVisualizerPrevBtn.setVisibility(View.INVISIBLE);
                    if (visualizerHomeworks.size() + visualizerTests.size() == 1)
                        ivVisualizerNextBtn.setVisibility(View.INVISIBLE);
                    else
                        ivVisualizerNextBtn.setVisibility(View.VISIBLE);
                    visualizerLayout.setVisibility(View.VISIBLE);
                    obscureLayoutView.setVisibility(View.VISIBLE);

                    isLoadingDetails = false;
                    progressBarForDetails.setVisibility(View.GONE);
                });
            }).start();
        }
    }

    private void addDateViewsAsync() {
        if (!isLoadingData) {
            if (viewsLayout.indexOfChild(progressBar) == -1)
                viewsLayout.addView(progressBar, 0);
            new Thread(() -> {
                try {
                    isLoadingData = true;
                    allTests = GlobalVariables.gS.getAllTestsWithoutDetails(getCurrentYear() + "-" + getNumberForScraping(Integer.parseInt(getCurrentMonth())), true);
                    allHomeworks = GlobalVariables.gS.getAllHomeworksWithoutDetails(getCurrentYear() + "-" + getNumberForScraping(Integer.parseInt(getCurrentMonth())), true);

                    if (allTests.isEmpty() && allHomeworks.isEmpty()) {
                        activity.runOnUiThread(() -> viewsLayout.removeAllViews());
                        activity.runOnUiThread(() -> tvNoElements.setVisibility(View.VISIBLE));
                        isLoadingData = false;
                    } else
                        activity.runOnUiThread(this::addDateViews);
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        DrawerActivity.setErrorMessage(getString(R.string.your_connection_error), root);
                        progressBar.setVisibility(View.GONE);
                        tvNoElements.setVisibility(View.VISIBLE);
                    });
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        DrawerActivity.setErrorMessage(getString(R.string.site_connection_error), root);
                        progressBar.setVisibility(View.GONE);
                        tvNoElements.setVisibility(View.VISIBLE);
                    });
                } catch (NullPointerException e) {
                    activity.runOnUiThread(() -> {
                        if (!GiuaScraper.isMyInternetWorking())
                            DrawerActivity.setErrorMessage(getString(R.string.your_connection_error), root);
                        progressBar.setVisibility(View.GONE);
                        tvNoElements.setVisibility(View.VISIBLE);
                    });
                }

            }).start();
        }
    }

    private void addDateViews() {
        viewsLayout.removeAllViews();
        tvNoElements.setVisibility(View.GONE);
        viewsLayout.scrollTo(0, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 50, 0, 0);

        int testCounter = 0;
        int allTestsLength = allTests.size();
        //Rircordarsi che i compiti e le verifiche sono già messe in ordine di data
        for (Homework homework : allHomeworks) {
            int homeworkDay = Integer.parseInt(homework.day);
            AgendaView view;

            while (testCounter < allTestsLength && homeworkDay > Integer.parseInt(allTests.get(testCounter).day)) {
                //Sino a quando non arrivi alla data del primo compito trovato metti tutte le verifche che trovi
                AgendaView view2 = new AgendaView(requireContext(), null, allTests.get(testCounter));
                view2.setOnClickListener(this::agendaViewOnClick);
                view2.setId(View.generateViewId());
                view2.setLayoutParams(params);
                viewsLayout.addView(view2);

                testCounter++;
            }

            if (testCounter < allTestsLength && homeworkDay == Integer.parseInt(allTests.get(testCounter).day)) {     //In questo giorno ci sono sia verifiche che compiti
                view = new AgendaView(requireContext(), null, homework, allTests.get(testCounter));
                testCounter++;
            } else {    //Solo compiti
                view = new AgendaView(requireContext(), null, homework);
            }

            view.setOnClickListener(this::agendaViewOnClick);
            view.setId(View.generateViewId());
            view.setLayoutParams(params);
            viewsLayout.addView(view);
        }
        while (testCounter < allTestsLength) {
            //Aggiungi tutte le restanti verifiche che si trovano dopo l'ultimo compito
            AgendaView view = new AgendaView(requireContext(), null, allTests.get(testCounter));
            view.setOnClickListener(this::agendaViewOnClick);
            view.setId(View.generateViewId());
            view.setLayoutParams(params);
            viewsLayout.addView(view);

            testCounter++;
        }
        isLoadingData = false;
    }

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
        return formatterForMonth.format(currentDate);
    }

    private String getCurrentYear() {
        return formatterForYear.format(currentDate);
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
}
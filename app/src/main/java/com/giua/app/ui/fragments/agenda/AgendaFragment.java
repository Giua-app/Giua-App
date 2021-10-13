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
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.giua.objects.Test;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class AgendaFragment extends Fragment implements IGiuaAppFragment {

    LinearLayout viewsLayout;
    List<Test> allTests;
    List<Homework> allHomeworks;
    List<Test> visualizerTests;
    List<Homework> visualizerHomeworks;
    TextView tvTodayText;
    TextView tvNoElements;
    ImageView ivPrevMonth;
    ImageView ivNextMonth;
    Activity activity;
    View root;
    Date currentDate;
    ProgressBar pbLoadingPage;
    ProgressBar pbForDetails;
    Calendar calendar;
    SimpleDateFormat formatterForMonth = new SimpleDateFormat("MM", Locale.ITALIAN);
    SimpleDateFormat formatterForYear = new SimpleDateFormat("yyyy", Locale.ITALIAN);
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
    int visualizerPointer = 0;
    boolean isLoadingData = false;
    boolean isLoadingDetails = false;
    LoggerManager loggerManager;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_agenda, container, false);
        loggerManager = new LoggerManager("AgendaFragment", getContext());
        loggerManager.d("onCreateView chiamato");

        viewsLayout = root.findViewById(R.id.agenda_views_layout);
        tvTodayText = root.findViewById(R.id.agenda_month_text);
        tvNoElements = root.findViewById(R.id.agenda_no_elements_text);
        ivNextMonth = root.findViewById(R.id.agenda_month_next_btn);
        ivPrevMonth = root.findViewById(R.id.agenda_month_prev_btn);
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
            currentDate = calendar.getTime();
        } else {    //No demo
            calendar = Calendar.getInstance();
            currentDate = calendar.getTime();
        }

        pbLoadingPage = new ProgressBar(requireContext(), null);
        threadManager = new ThreadManager();

        tvVisualizerText.setMovementMethod(new ScrollingMovementMethod());
        tvTodayText.setText(getMonthFromNumber(Integer.parseInt(getCurrentMonth())) + " " + getCurrentYear());

        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        ivNextMonth.setOnClickListener(this::btnNextMonthOnClick);
        ivPrevMonth.setOnClickListener(this::btnPrevMonthOnClick);
        ivVisualizerPrevBtn.setOnClickListener(this::ivVisualizerPrevBtnOnClick);
        ivVisualizerNextBtn.setOnClickListener(this::ivVisualizerNextBtnOnClick);
        visualizerLayout.setOnClickListener((view) -> {/*Serve ad evitare che quando si clicca il layout questo non sparisca*/});
        obscureLayoutView.setOnClickListener(this::obscureLayoutOnClick);

        if (Integer.parseInt(getCurrentMonth()) >= 6 && Integer.parseInt(getCurrentMonth()) <= 8)
            ivNextMonth.setVisibility(View.GONE);
        if (Integer.parseInt(getCurrentMonth()) == 7 || Integer.parseInt(getCurrentMonth()) == 1 || Integer.parseInt(getCurrentMonth()) == 9)
            ivPrevMonth.setVisibility(View.GONE);

        loadDataAndViews();

        return root;
    }

    @Override
    public void loadDataAndViews() {
        loggerManager.d("Carico views...");
        if (!isLoadingData) {
            if (viewsLayout.indexOfChild(pbLoadingPage) == -1)
                viewsLayout.addView(pbLoadingPage, 0);
            threadManager.addAndRun(() -> {
                try {
                    isLoadingData = true;
                    allTests = GlobalVariables.gS.getAllTestsWithoutDetails(getCurrentYear() + "-" + getNumberForScraping(Integer.parseInt(getCurrentMonth())), true);
                    allHomeworks = GlobalVariables.gS.getAllHomeworksWithoutDetails(getCurrentYear() + "-" + getNumberForScraping(Integer.parseInt(getCurrentMonth())), true);

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
                        pbLoadingPage.setVisibility(View.GONE);
                        tvNoElements.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);
                        isLoadingData = false;
                    });
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.site_connection_error), root);
                        pbLoadingPage.setVisibility(View.GONE);
                        tvNoElements.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setRefreshing(false);
                        isLoadingData = false;
                    });
                } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                        pbLoadingPage.setVisibility(View.GONE);
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
                //Sino a quando non arrivi alla data del primo compito trovato metti nel layout tutte le verifche che trovi
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

    //region Listeners
    @SuppressLint("SetTextI18n")
    private void btnPrevMonthOnClick(View view) {
        if (!isLoadingData) {
            currentDate = getPrevMonth(currentDate);
            loggerManager.d("Carico compiti del mese " + currentDate.toString());
            if (Integer.parseInt(getCurrentMonth()) < 6 || Integer.parseInt(getCurrentMonth()) > 8)
                ivNextMonth.setVisibility(View.VISIBLE);
            if (Integer.parseInt(getCurrentMonth()) == 9)
                ivPrevMonth.setVisibility(View.GONE);
            tvTodayText.setText(getMonthFromNumber(Integer.parseInt(getCurrentMonth())) + " " + getCurrentYear());
            viewsLayout.removeAllViews();
            loadDataAndViews();
        }
    }

    @SuppressLint("SetTextI18n")
    private void btnNextMonthOnClick(View view) {
        loggerManager.d("Car");
        if (!isLoadingData) {
            currentDate = getNextMonth(currentDate);
            loggerManager.d("Carico compiti del mese " + currentDate.toString());
            ivPrevMonth.setVisibility(View.VISIBLE);
            if (Integer.parseInt(getCurrentMonth()) >= 6 && Integer.parseInt(getCurrentMonth()) <= 8)
                ivNextMonth.setVisibility(View.GONE);
            tvTodayText.setText(getMonthFromNumber(Integer.parseInt(getCurrentMonth())) + " " + getCurrentYear());
            viewsLayout.removeAllViews();
            loadDataAndViews();
        }
    }

    private void ivVisualizerNextBtnOnClick(View view) {
        if (visualizerPointer + 1 < visualizerTests.size() + visualizerHomeworks.size())  //Se si può ancora andare avanti punto il visualizerPointer al prossimo oggetto (quello che sta per essere visualizzato)
            visualizerPointer++;
        if (visualizerPointer < visualizerTests.size()) {
            tvVisualizerType.setText("Verifica");
            tvVisualizerSubject.setText(visualizerTests.get(visualizerPointer).subject);
            tvVisualizerCreator.setText(visualizerTests.get(visualizerPointer).creator);
            tvVisualizerText.setText(visualizerTests.get(visualizerPointer).details);
            tvVisualizerDate.setText(visualizerTests.get(visualizerPointer).date);
            ivVisualizerPrevBtn.setVisibility(View.VISIBLE);
        } else if (visualizerPointer - visualizerTests.size() < visualizerHomeworks.size()) {
            int index = visualizerPointer - visualizerTests.size();

            tvVisualizerType.setText("Compiti");
            tvVisualizerSubject.setText(visualizerHomeworks.get(index).subject);
            tvVisualizerCreator.setText(visualizerHomeworks.get(index).creator);
            tvVisualizerText.setText(visualizerHomeworks.get(index).details);
            tvVisualizerDate.setText(visualizerHomeworks.get(index).date);
            ivVisualizerPrevBtn.setVisibility(View.VISIBLE);
        }
        if (visualizerPointer >= visualizerTests.size() + visualizerHomeworks.size() - 1)
            ivVisualizerNextBtn.setVisibility(View.INVISIBLE);
    }

    private void ivVisualizerPrevBtnOnClick(View view) {
        int testsSize = visualizerTests.size();
        int homeworkSize = visualizerHomeworks.size();
        if (visualizerPointer > 0)
            visualizerPointer--;
        if (visualizerPointer >= 0 && visualizerPointer < testsSize) {
            tvVisualizerType.setText("Verifica");
            tvVisualizerSubject.setText(visualizerTests.get(visualizerPointer).subject);
            tvVisualizerCreator.setText(visualizerTests.get(visualizerPointer).creator);
            tvVisualizerText.setText(visualizerTests.get(visualizerPointer).details);
            tvVisualizerDate.setText(visualizerTests.get(visualizerPointer).date);
            ivVisualizerNextBtn.setVisibility(View.VISIBLE);
        } else if (visualizerPointer - testsSize >= 0 && visualizerPointer - testsSize < homeworkSize && !visualizerHomeworks.isEmpty()) {
            int index = visualizerPointer - testsSize;

            tvVisualizerType.setText("Compito");
            tvVisualizerSubject.setText(visualizerHomeworks.get(index).subject);
            tvVisualizerCreator.setText(visualizerHomeworks.get(index).creator);
            tvVisualizerText.setText(visualizerHomeworks.get(index).details);
            tvVisualizerDate.setText(visualizerHomeworks.get(index).date);
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
                visualizerHomeworks = new Vector<>();
                visualizerTests = new Vector<>();
                visualizerPointer = 0;

                try {
                    if (agendaView.test != null)
                        visualizerTests = GlobalVariables.gS.getTest(agendaView.test.date);
                    if (agendaView.homework != null)
                        visualizerHomeworks = GlobalVariables.gS.getHomework(agendaView.homework.date);
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
                    if (visualizerHomeworks.size() + visualizerTests.size() == 1) {  //E' presente solo un elemento
                        ivVisualizerPrevBtn.setVisibility(View.GONE);
                        ivVisualizerNextBtn.setVisibility(View.GONE);
                    } else
                        ivVisualizerNextBtn.setVisibility(View.VISIBLE);
                    visualizerLayout.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_show_effect));
                    visualizerLayout.setVisibility(View.VISIBLE);
                    obscureLayoutView.show(activity);

                    isLoadingDetails = false;
                    pbForDetails.setVisibility(View.GONE);
                });
            });
        }
    }

    private void obscureLayoutOnClick(View view) {
        visualizerLayout.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_hide_effect));
        visualizerLayout.setVisibility(View.GONE);
        obscureLayoutView.hide(activity);
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
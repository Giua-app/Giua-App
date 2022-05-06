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

package com.giua.app.ui.fragments.home;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.giua.app.AppUpdateManager;
import com.giua.app.AppUtils;
import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.LoggerManager;
import com.giua.app.OfflineDBController;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.app.ui.activities.DrawerActivity;
import com.giua.objects.Vote;
import com.giua.pages.VotesPage;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

public class HomeFragment extends Fragment implements IGiuaAppFragment {

    LinearLayout contentLayout;
    List<HomeChartView> allCharts;
    Activity activity;
    TextView tvHomeworks;
    TextView tvTests;
    TextView txUserInfo;
    SwipeRefreshLayout swipeRefreshLayout;
    View root;
    LoggerManager loggerManager;

    boolean forceRefresh = false;
    boolean isFragmentDestroyed = false;
    boolean offlineMode = false;
    boolean addVoteNotRelevantForMean = false;

    final @ColorInt int[] QUARTERLY_COLORS = new int[]{
            Color.argb(255, 5, 157, 192),
            Color.argb(255, 0, 88, 189),
            Color.argb(255, 0, 189, 75)
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);

        activity = requireActivity();
        loggerManager = new LoggerManager("HomeFragment", activity);

        contentLayout = root.findViewById(R.id.home_content_layout);
        tvHomeworks = root.findViewById(R.id.home_txt_homeworks);
        tvTests = root.findViewById(R.id.home_txt_tests);
        swipeRefreshLayout = root.findViewById(R.id.home_swipe_refresh_layout);
        txUserInfo = root.findViewById(R.id.home_user_info);

        allCharts = new Vector<>(4);
        allCharts.add(new HomeChartView(activity));
        contentLayout.addView(allCharts.get(0));

        allCharts.get(0).setOnClickListener(this::mainChartOnClick);
        root.findViewById(R.id.home_agenda_alerts).setOnClickListener(this::agendaAlertsOnClick);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        offlineMode = activity.getIntent().getBooleanExtra("offline", false);
        addVoteNotRelevantForMean = SettingsData.getSettingBoolean(activity, SettingKey.VOTE_NRFM_ON_CHART);

        new Thread(() -> {

            if (offlineMode) {
                activity.runOnUiThread(() -> txUserInfo.setText("Accesso eseguito in modalità Offline"));
            } else {
                String userType = GlobalVariables.gS.getUserTypeString();
                activity.runOnUiThread(() -> txUserInfo.setText("Accesso eseguito nell'account " + GlobalVariables.gS.getUser() + " (" + userType + ")"));
            }

            AppUpdateManager manager = new AppUpdateManager(activity);
            if (manager.checkForUpdates()) {
                activity.runOnUiThread(() -> {
                    loggerManager.d("Rendo visibile avviso su home dell'aggiornamento");
                    root.findViewById(R.id.home_app_update_reminder).setVisibility(View.VISIBLE);
                    root.findViewById(R.id.home_app_update_reminder).setOnClickListener(this::updateReminderOnClick);
                });
            }
        }).start();

        return root;
    }

    private void mainChartOnClick(View view) {
        boolean isFirst = true;
        for(HomeChartView homeChartView : allCharts) {
            if (isFirst) {
                isFirst = false;
                continue;
            }

            if(homeChartView.getVisibility() == View.INVISIBLE)
                homeChartView.setVisibility(View.VISIBLE);
            else
                homeChartView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void loadOfflineDataAndViews() {
        new Thread(() -> {
            try {
                Map<String, List<Vote>> allVotes = new OfflineDBController(activity).readVotes();
                int homeworks = 0;
                int tests = 0;

                if (isFragmentDestroyed)
                    return;

                activity.runOnUiThread(() -> {
                    setupHomeworksTestsText(homeworks, tests);
                    if (!allVotes.isEmpty()) {
                        allCharts.get(0).refreshData(
                                activity,
                                "Andamento generale",
                                getMeanOfAllVotes(allVotes),
                                generateEntries(allVotes),
                                Arrays.asList(
                                        "Primo Quadrimestre",
                                        "Secondo Quadrimestre",
                                        "Terzo Quadrimestre"
                                ),
                                Arrays.asList(
                                        QUARTERLY_COLORS[0],
                                        QUARTERLY_COLORS[1],
                                        QUARTERLY_COLORS[2]
                                ));
                    }
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (IllegalStateException ignored) {
                //Si verifica quando questa schermata è stata distrutta ma il thread cerca comunque di fare qualcosa
            }
        }).start();
    }

    @Override
    public void loadDataAndViews() {
        GlobalVariables.gsThread.addTask(() -> {
            try {
                VotesPage votesPage = GlobalVariables.gS.getVotesPage(forceRefresh);
                Map<String, List<Vote>> allVotes = votesPage.getAllVotes();
                int homeworks = GlobalVariables.gS.getHomePage(forceRefresh).getNearHomeworks();
                int tests = GlobalVariables.gS.getHomePage(false).getNearTests();

                new OfflineDBController(activity).addVotes(allVotes);

                if (forceRefresh)
                    forceRefresh = false;

                if (isFragmentDestroyed) return;

                activity.runOnUiThread(() -> {
                    setupHomeworksTestsText(homeworks, tests);
                    if (!allVotes.isEmpty())
                        refreshCharts(allVotes, votesPage);
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.your_connection_error), root);
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.site_connection_error), root);
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (GiuaScraperExceptions.NotLoggedIn e) {
                activity.runOnUiThread(() -> {
                    ((DrawerActivity) activity).startActivityManager();
                });
            } catch (IllegalStateException ignored) {
            }   //Si verifica quando questa schermata è stata distrutta ma il thread cerca comunque di fare qualcosa
        });
    }

    private void refreshCharts(Map<String, List<Vote>> allVotes, VotesPage votesPage) {
        allCharts.get(0).refreshData(
                activity,
                "Andamento generale",
                getMeanOfAllVotes(allVotes),
                generateEntries(allVotes),
                votesPage.getAllQuarterlyNames(),
                Arrays.asList(
                        QUARTERLY_COLORS[0],
                        QUARTERLY_COLORS[1],
                        QUARTERLY_COLORS[2]
                ));

        List<String> allQuarterlyNames = votesPage.getAllQuarterlyNames();
        List<Vote> allVotesSorted = sortVotes(allVotes);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int nQuarterly = allQuarterlyNames.size();
        float mean = 0;

        params.topMargin = AppUtils.convertDpToPx(20, activity);

        for(int i = 1; i < allCharts.size(); i++)
            contentLayout.removeView(allCharts.get(i));

        allCharts = allCharts.subList(0, 1);

        for(int quarterly = 0; quarterly < nQuarterly; quarterly++){
            HomeChartView homeChartView = new HomeChartView(activity);
            List<Entry> entries = new Vector<>();

            int j = 0;
            for(Vote vote : allVotesSorted){
                if(vote.quarterly != quarterly+1) continue;

                float voteValue = vote.toFloat();

                mean += voteValue;
                entries.add(new Entry(j, voteValue));
                j++;
            }

            mean = mean / j;

            homeChartView.refreshData(activity, allQuarterlyNames.get(quarterly), mean,entries, allQuarterlyNames.get(quarterly), QUARTERLY_COLORS[quarterly]);
            homeChartView.setVisibility(View.INVISIBLE);
            homeChartView.setLayoutParams(params);

            allCharts.add(homeChartView);
            contentLayout.addView(homeChartView);
        }
    }

    @Override
    public void addViews() {
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    private void agendaAlertsOnClick(View view) {
        ((DrawerActivity) activity).myFragmentManager.changeFragment(R.id.nav_agenda);
        ((DrawerActivity) activity).selectItemInDrawer(16);
    }

    private void updateReminderOnClick(View view) {
        loggerManager.d("Aggiornamento app richiesto dall'utente tramite Home");
        new Thread(() -> new AppUpdateManager(activity).startUpdateDialog()).start();
    }

    private void onRefresh() {
        forceRefresh = true;
        if (!offlineMode)
            loadDataAndViews();
        else
            loadOfflineDataAndViews();
    }

    private void setupHomeworksTestsText(int homeworks, int tests) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 0);

        root.findViewById(R.id.home_txt_homeworks).setVisibility(View.GONE);
        root.findViewById(R.id.home_txt_tests).setVisibility(View.GONE);
        root.findViewById(R.id.home_agenda_alerts).setBackgroundTintList(activity.getResources().getColorStateList(R.color.middle_vote, activity.getTheme()));

        if (homeworks == 0 && tests == 0) {
            root.findViewById(R.id.home_agenda_alerts).setVisibility(View.VISIBLE);
            root.findViewById(R.id.home_agenda_alerts).setBackgroundTintList(activity.getResources().getColorStateList(R.color.general_view_color, activity.getTheme()));
            tvHomeworks.setVisibility(View.VISIBLE);
            tvHomeworks.setText("Non sono presenti attività nei prossimi giorni");
        }

        if (homeworks == 1) {
            root.findViewById(R.id.home_agenda_alerts).setVisibility(View.VISIBLE);
            tvHomeworks.setVisibility(View.VISIBLE);
            tvHomeworks.setText("E' presente un compito per domani");
        } else if (homeworks > 1) {
            root.findViewById(R.id.home_agenda_alerts).setVisibility(View.VISIBLE);
            tvHomeworks.setVisibility(View.VISIBLE);
            tvHomeworks.setText("Sono presenti " + homeworks + " compiti per domani");
        }

        if (tests == 1) {
            root.findViewById(R.id.home_agenda_alerts).setVisibility(View.VISIBLE);
            tvTests.setVisibility(View.VISIBLE);
            tvTests.setText("E' presente una verifica nei prossimi giorni");
        } else if (tests > 1) {
            root.findViewById(R.id.home_agenda_alerts).setVisibility(View.VISIBLE);
            tvTests.setVisibility(View.VISIBLE);
            tvTests.setText("Sono presenti " + tests + " verifiche nei prossimi giorni");
        }

        if (homeworks <= 0)
            tvTests.setLayoutParams(params);

        tvHomeworks.setBackground(null);
        tvTests.setBackground(null);
        tvHomeworks.setMinWidth(0);
        tvTests.setMinWidth(0);
    }

    private List<List<Entry>> generateEntries(Map<String, List<Vote>> allVotes) {

        List<Entry> entriesFirstQuarter = new Vector<>();
        List<Entry> entriesSecondQuarter = new Vector<>();
        List<Entry> entriesThirdQuarter = new Vector<>();

        int voteCounter = 0;

        List<Vote> allVotesSorted = sortVotes(allVotes);
        for (Vote vote : allVotesSorted) {
            if (vote.isAsterisk || !(vote.isRelevantForMean || addVoteNotRelevantForMean)) break;

            switch (vote.quarterly) {
                case 1:
                    entriesFirstQuarter.add(new Entry(voteCounter, vote.toFloat()));
                    break;
                case 2:
                    entriesSecondQuarter.add(new Entry(voteCounter, vote.toFloat()));
                    break;
                case 3:
                    entriesThirdQuarter.add(new Entry(voteCounter, vote.toFloat()));
                    break;
            }

            voteCounter++;
        }

        if (voteCounter == 1)    //Se si ha solamente un voto allora duplicalo nel grafico per far visualizzare almeno una riga
            entriesFirstQuarter.add(new Entry(voteCounter, allVotesSorted.get(0).toFloat()));

        List<List<Entry>> entries = new Vector<>(3);
        entries.add(entriesFirstQuarter);
        entries.add(entriesSecondQuarter);
        entries.add(entriesThirdQuarter);

        return entries;
    }


    /**
     * Riordina in una lista tutti i voti di tutte le materie secondo le date
     *
     * @param allVotes I voti
     * @return La lista dei voti ordinata per data
     */
    private List<Vote> sortVotes(Map<String, List<Vote>> allVotes) {
        boolean showVoteNotRelevantForMean = SettingsData.getSettingBoolean(activity, SettingKey.VOTE_NRFM_ON_CHART);
        List<Vote> listToSort = new Vector<>();
        Set<String> allSubjects = allVotes.keySet();
        for (String subject : allSubjects) {
            List<Vote> subjectVotes = allVotes.get(subject);
            for (Vote vote : subjectVotes) {
                if (!vote.isAsterisk && (vote.isRelevantForMean || showVoteNotRelevantForMean)) {
                    listToSort.add(vote);
                }
            }
        }

        listToSort.sort((firstVote, secondVote) -> {
            int firstDay = Integer.parseInt(firstVote.date.split(" ")[0]);
            int firstMonth = getNumberFromMonth(firstVote.date.split(" ")[1]);
            int firstYear = 0;   //Non interessa l'anno vero serve solo per il sorting

            if (firstMonth < 9)
                firstYear = 1;  //1 se è il secondo anno dell'anno scolastico

            int secondDay = Integer.parseInt(secondVote.date.split(" ")[0]);
            int secondMonth = getNumberFromMonth(secondVote.date.split(" ")[1]);
            int secondYear = 0;

            if (secondMonth < 9)
                secondYear = 1;

            if (firstYear == secondYear) {
                if (firstMonth == secondMonth) {
                    if (firstDay == secondDay)
                        return 0;   //I giorni sono uguali
                    if (firstDay > secondDay)
                        return 1;   //Il primo giorno è maggiore rispetto al primo
                    return -1; //Il primo giorno è minore rispetto al primo
                }
                if (firstMonth > secondMonth)
                    return 1;
                return -1;
            }
            if (firstYear > secondYear)
                return 1;
            return -1;
        });

        return listToSort;
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
        loggerManager.e("E' stato rilevato un mese che non ho capito: " + month);
        return -1;
    }

    private float getMeanOfAllVotes(Map<String, List<Vote>> votes) {
        List<Float> allMeans = new Vector<>();
        for (String subject : votes.keySet()) {
            float mean = 0f;
            float voteCounter = 0;     //Conta solamente i voti che ci sono e non gli asterischi

            for (Vote vote : Objects.requireNonNull(votes.get(subject))) {      //Cicla ogni voto della materia
                if (vote.value.length() > 0 && !vote.isAsterisk && vote.isRelevantForMean) {
                    mean += vote.toFloat();
                    voteCounter++;
                }
            }

            if (voteCounter > 0) {
                mean /= voteCounter;
                allMeans.add(mean);
            }
        }

        float meanOfMeans = 0f; //La media delle singole medie delle materie
        for (Float f : allMeans) {
            meanOfMeans += f;
        }

        if (meanOfMeans == 0f)
            return 0f;

        return meanOfMeans / allMeans.size();
    }

    private void setErrorMessage(String message, View root) {
        if (!isFragmentDestroyed)
            Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

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

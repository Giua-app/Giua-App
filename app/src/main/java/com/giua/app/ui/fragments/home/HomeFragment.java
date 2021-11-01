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

package com.giua.app.ui.fragments.home;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.LoggerManager;
import com.giua.app.R;
import com.giua.app.ThreadManager;
import com.giua.objects.Vote;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

public class HomeFragment extends Fragment implements IGiuaAppFragment {

    ThreadManager threadManager;
    LineChart chart;
    Activity activity;
    TextView tvHomeworks;
    TextView tvTests;
    SwipeRefreshLayout swipeRefreshLayout;
    View root;
    LoggerManager loggerManager;
    boolean forceRefresh = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);

        threadManager = new ThreadManager();
        activity = requireActivity();
        loggerManager = new LoggerManager("HomeFragment", activity);

        chart = root.findViewById(R.id.home_mean_chart);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(true);
        chart.getAxisRight().setTextSize(14);
        chart.getAxisRight().setTextColor(getResources().getColor(R.color.night_white_light_black, activity.getTheme()));
        chart.getAxisRight().setAxisMinimum(0f);
        chart.getAxisRight().setAxisMaximum(10f);
        chart.getAxisRight().setLabelCount(5, false);
        chart.getXAxis().setEnabled(false);
        chart.setNoDataText("Nessun voto");
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.getLegend().setTextSize(13);
        chart.getLegend().setTextColor(getResources().getColor(R.color.night_white_light_black, activity.getTheme()));
        chart.getLegend().setWordWrapEnabled(true);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.invalidate();

        tvHomeworks = root.findViewById(R.id.home_txt_homeworks);
        tvTests = root.findViewById(R.id.home_txt_tests);
        swipeRefreshLayout = root.findViewById(R.id.home_swipe_refresh_layout);

        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);

        new Thread(() -> {
            //TODO: Mettere la cache for check updates (almeno tra activity manager e home fragemnt visto che in ogni caso viene chiamata due volte)
            AppUpdateManager manager = new AppUpdateManager(activity);
            if (manager.checkForUpdates()) {
                activity.runOnUiThread(() -> {
                    loggerManager.d("Rendo visibile avviso su home dell'aggiornamento");
                    root.findViewById(R.id.home_app_update_reminder).setVisibility(View.VISIBLE);
                    root.findViewById(R.id.home_app_update_reminder).setOnClickListener(this::updateReminderOnClick);
                });
            }
        }).start();


        loadDataAndViews();
        return root;
    }

    @Override
    public void loadDataAndViews() {
        threadManager.addAndRun(() -> {
            try {
                Map<String, List<Vote>> allVotes = GlobalVariables.gS.getVotesPage(forceRefresh).getAllVotes();
                int homeworks = GlobalVariables.gS.getHomePage(forceRefresh).getNearHomeworks();
                int tests = GlobalVariables.gS.getHomePage(false).getNearTests();

                if (forceRefresh)
                    forceRefresh = false;

                if (threadManager.isDestroyed())
                    return;

                activity.runOnUiThread(() -> {
                    setupHomeworksTestsText(homeworks, tests);
                    setupMeanVotesText(allVotes);
                    if (!allVotes.isEmpty()) {
                        chart.setData(generateLineData(allVotes));
                        chart.invalidate();
                    }
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
            } catch (IllegalStateException ignored) {
            }   //Si verifica quando questa schermata è stata distrutta ma il thread cerca comunque di fare qualcosa
        });
    }

    @Override
    public void addViews() {
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    private void updateReminderOnClick(View view) {
        loggerManager.d("Aggiornamento app richiesto dall'utente tramite Home");
        new Thread(() -> new AppUpdateManager(activity).startUpdateDialog()).start();
    }

    private void onRefresh() {
        forceRefresh = true;
        loadDataAndViews();
    }

    private void setupHomeworksTestsText(int homeworks, int tests) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 0);

        root.findViewById(R.id.home_txt_homeworks).setVisibility(View.GONE);
        root.findViewById(R.id.home_txt_tests).setVisibility(View.GONE);
        root.findViewById(R.id.home_agenda_alerts).setBackgroundTintList(getResources().getColorStateList(R.color.middle_vote, activity.getTheme()));

        if (homeworks == 0 && tests == 0) {
            root.findViewById(R.id.home_agenda_alerts).setVisibility(View.VISIBLE);
            root.findViewById(R.id.home_agenda_alerts).setBackgroundTintList(getResources().getColorStateList(R.color.general_view_color, activity.getTheme()));
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

    private LineData generateLineData(Map<String, List<Vote>> allVotes) {
        List<Entry> entriesFirstQuarter = new ArrayList<>();
        List<Entry> entriesSecondQuarter = new ArrayList<>();
        int voteCounter = 0;

        List<Vote> allVotesSorted = sortVotes(allVotes);
        for (Vote vote : allVotesSorted) {
            if (vote.quarterlyToInt() == 1) //FIXME: Da rifare per supportare più quadrimestri/trimestri. Prima era "if(vote.isQuarterly)"
                entriesFirstQuarter.add(new Entry(voteCounter, vote.toFloat()));
            else
                entriesSecondQuarter.add(new Entry(voteCounter, vote.toFloat()));
            voteCounter++;
        }

        if (voteCounter == 1)    //Se si ha solamente un voto allora duplicalo nel grafico per far visualizzare almeno una riga
            entriesFirstQuarter.add(new Entry(voteCounter, allVotesSorted.get(0).toFloat()));

        LineDataSet lineDataSetFirstQuarter = new LineDataSet(entriesFirstQuarter, "Primo quadrimestre");
        lineDataSetFirstQuarter.setDrawCircles(false);
        lineDataSetFirstQuarter.setDrawCircleHole(false);
        lineDataSetFirstQuarter.setDrawValues(false);
        lineDataSetFirstQuarter.setLineWidth(3);
        lineDataSetFirstQuarter.setDrawFilled(true);
        lineDataSetFirstQuarter.setColor(Color.argb(255, 5, 157, 192));
        lineDataSetFirstQuarter.setFillColor(Color.argb(255, 5, 157, 192));
        lineDataSetFirstQuarter.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSetFirstQuarter.setAxisDependency(YAxis.AxisDependency.RIGHT);

        LineDataSet lineDataSetSecondQuarter = new LineDataSet(entriesSecondQuarter, "Secondo quadrimestre");
        lineDataSetSecondQuarter.setDrawCircles(false);
        lineDataSetSecondQuarter.setDrawCircleHole(false);
        lineDataSetSecondQuarter.setDrawValues(false);
        lineDataSetSecondQuarter.setLineWidth(3);
        lineDataSetSecondQuarter.setDrawFilled(true);
        lineDataSetSecondQuarter.setColor(Color.argb(255, 0, 88, 189));
        lineDataSetSecondQuarter.setFillColor(Color.argb(255, 0, 88, 189));
        lineDataSetSecondQuarter.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSetSecondQuarter.setAxisDependency(YAxis.AxisDependency.RIGHT);

        return new LineData(lineDataSetFirstQuarter, lineDataSetSecondQuarter);
    }

    /**
     * Riordina in una lista tutti i voti di tutte le materie secondo le date
     *
     * @param allVotes I voti
     * @return La lista dei voti ordinata per data
     */
    private List<Vote> sortVotes(Map<String, List<Vote>> allVotes) {

        List<Vote> listToSort = new Vector<>();
        Set<String> allSubjects = allVotes.keySet();
        for (String subject : allSubjects) {
            List<Vote> subjectVotes = allVotes.get(subject);
            for (Vote vote : subjectVotes) {
                if (!vote.isAsterisk) {
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

    private void setupMeanVotesText(Map<String, List<Vote>> allVotes) {
        DecimalFormat df = new DecimalFormat("0.00");
        float meanFirstQuarter = getMeanOfAllVotes(allVotes);

        if (meanFirstQuarter == 0f)
            ((TextView) root.findViewById(R.id.home_txt_mean)).setTextColor(getResources().getColorStateList(R.color.non_vote, activity.getTheme()));
        else if (meanFirstQuarter >= 6)
            ((TextView) root.findViewById(R.id.home_txt_mean)).setTextColor(getResources().getColorStateList(R.color.good_vote_darker, activity.getTheme()));
        else if (meanFirstQuarter < 6 && meanFirstQuarter >= 5)
            ((TextView) root.findViewById(R.id.home_txt_mean)).setTextColor(getResources().getColorStateList(R.color.middle_vote, activity.getTheme()));
        else
            ((TextView) root.findViewById(R.id.home_txt_mean)).setTextColor(getResources().getColorStateList(R.color.bad_vote, activity.getTheme()));
        if (meanFirstQuarter == 0f)
            ((TextView) root.findViewById(R.id.home_txt_mean)).setText("/");
        else
            ((TextView) root.findViewById(R.id.home_txt_mean)).setText(df.format(meanFirstQuarter));

        root.findViewById(R.id.home_txt_mean).setBackground(null);
        ((TextView) root.findViewById(R.id.home_txt_mean)).setMinWidth(0);

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
                if (vote.value.length() > 0 && !vote.isAsterisk) {
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
        if (!threadManager.isDestroyed())
            Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        threadManager.destroyAllAndNullMe();
        super.onDestroyView();
    }
}

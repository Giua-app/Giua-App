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

package com.giua.app.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.giua.app.AppData;
import com.giua.app.LoggerManager;
import com.giua.app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.List;

public class LogdogViewerActivity extends AppCompatActivity {

    LinearLayout linearLayout;
    ScrollView scrollView;
    FloatingActionButton deleteLogs;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logdog_viewer);

        linearLayout = findViewById(R.id.log_linearlayout);
        scrollView = findViewById(R.id.log_scroll_view);
        deleteLogs = findViewById(R.id.log_floating_deletelogs);
        ProgressBar progressBar = findViewById(R.id.logdog_progressbar);
        TextView pbText = findViewById(R.id.logdog_progressbartxt);

        deleteLogs.setOnClickListener(this::onClickDeleteLogs);

        Toolbar toolbar = findViewById(R.id.logDog_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        LoggerManager loggerManager = new LoggerManager("Logdog", this);

        try{
            loggerManager.parseLogsFrom(AppData.getLogsString(this));
        } catch(ArrayIndexOutOfBoundsException e){
            onClickDeleteLogs(null);
        }


        List<LoggerManager.Log> logs = loggerManager.getLogs();
        new Thread(() -> {
            int i=0;
            TextView textView = new TextView(this);
            StringBuilder text = new StringBuilder();
            linearLayout.removeAllViews();

            for (LoggerManager.Log log : logs) {
                if (i == 1000) break;
                int finalI = i; //Non so perchè ma dobbiamo fare questa cosa per passare variabili al thread
                runOnUiThread(() -> pbText.setText(finalI + "/" + logs.size()));
                // --@ = Avvio dell'app
                // -.@ = Crash dell'app
                String[] splitted = log.text.split("@");
                if (log.text.startsWith("--@")) {
                    text.append("<b>    \u23af\u23af\u23af     ")
                            .append(splitted[1])
                            .append("     \u23af\u23af\u23af</b>");
                } else if (log.text.startsWith("-.@")) {
                    text.append("<font color='red'>    \u23af\u23af\u23af     ")
                            .append(splitted[1])
                            .append("     \u23af\u23af\u23af </font>");
                } else {
                    switch (log.type) {
                        case "ERROR":
                            text.append("<font color='red'>");
                            break;
                        case "WARNING":
                            text.append("<font color='#FFA500'>");
                            break;
                        case "DEBUG":
                            text.append("<font color='gray'>");
                            break;
                        default:
                            text.append("<font color='black'>");
                            break;
                    }
                    // + "|" + log.type + "| " + log.tag + ": <b>" + log.text + "</b></font>"
                    text.append(dateFormat.format(log.date))
                            .append("|")
                            .append(log.type)
                            .append("| ")
                            .append(log.tag)
                            .append(": <b>")
                            .append(log.text)
                            .append("</b></font>");

                }

                text.append("<br>");
                i++;
            }

            if (logs.isEmpty()) {
                text.append(noLogPresentText());
            }


            if(logs.size() >= 2000){
                //Troppi log da mostrare in html, ne tagliamo un quarto
                int last = text.length() - (text.length() / 4);
                runOnUiThread(() -> pbText.setText("Rendering\nonly " + last + " of " + text.length() + " chars"));
                textView.setText(Html.fromHtml(text.substring(0,last), 0));
            } else {
                runOnUiThread(() -> pbText.setText("Rendering " + text.length() + " chars"));
                textView.setText(Html.fromHtml(text.toString(), 0));
            }
            runOnUiThread(() -> {
                linearLayout.addView(textView);
                progressBar.setVisibility(View.INVISIBLE);
                pbText.setVisibility(View.INVISIBLE);
            });
        }).start();

    }

    private void onClickDeleteLogs(View v) {
        AppData.saveLogsString(LogdogViewerActivity.this, "");
        linearLayout.removeAllViews();
        TextView textView = new TextView(LogdogViewerActivity.this);
        textView.setText(Html.fromHtml(noLogPresentText(), 0));
        linearLayout.addView(textView);
    }

    private String noLogPresentText(){
        return "<b>\u23af\u23af\u23af  Nessun log trovato!  \u23af\u23af\u23af</b>";
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

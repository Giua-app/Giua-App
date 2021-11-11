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

package com.giua.app.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
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

        deleteLogs.setOnClickListener(this::onClickDeleteLogs);

        Toolbar toolbar = findViewById(R.id.logDog_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        LoggerManager loggerManager = new LoggerManager("Logdog", this);

        loggerManager.parseLogsFrom(AppData.getLogsString(this));


        List<LoggerManager.Log> logs = loggerManager.getLogs();
        TextView textView = new TextView(this);
        CharSequence text = "";
        linearLayout.removeAllViews();
        for (LoggerManager.Log log : logs) {
            // --@ = Avvio dell'app
            // -.@ = Crash dell'app
            if (log.text.startsWith("--@")) {
                text += "<b>    \u23af\u23af\u23af     " + log.text.split("@")[1] + "     \u23af\u23af\u23af</b>";
            } else if (log.text.startsWith("-.@")) {
                text += "<font color='red'>    \u23af\u23af\u23af     " + log.text.split("@")[1] + "     \u23af\u23af\u23af </font>";
            } else {
                switch (log.type) {
                    case "ERROR":
                        text += "<font color='red'>";
                        break;
                    case "WARNING":
                        text += "<font color='#FFA500'>";
                        break;
                    case "DEBUG":
                        text += "<font color='gray'>";
                        break;
                    default:
                        text += "<font color='black'>";
                        break;
                }
                text += dateFormat.format(log.date) + "|" + log.type + "| " + log.tag + ": <b>" + log.text + "</b></font>";

            }

            text += "<br>";
        }

        if (logs.isEmpty()) {
            text = "<b>\u23af\u23af\u23af  Nessun log trovato!  \u23af\u23af\u23af</b>";
        }

        textView.setText(Html.fromHtml(text.toString(), 0));
        linearLayout.addView(textView);
    }

    private void onClickDeleteLogs(View v){
        AppData.saveLogsString(LogdogViewerActivity.this, "");
        linearLayout.removeAllViews();
        TextView textView = new TextView(LogdogViewerActivity.this);
        textView.setText(Html.fromHtml("<b>\u23af\u23af\u23af  Nessun log trovato!  \u23af\u23af\u23af</b>",0));
        linearLayout.addView(textView);
    }
}
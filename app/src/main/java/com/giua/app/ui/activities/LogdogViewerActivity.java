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
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.giua.app.AppData;
import com.giua.app.LoggerManager;
import com.giua.app.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class LogdogViewerActivity extends AppCompatActivity {

    LinearLayout linearLayout;
    ScrollView scrollView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logdog_viewer);

        linearLayout = findViewById(R.id.log_linearlayout);
        scrollView = findViewById(R.id.log_scroll_view);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        LoggerManager loggerManager = new LoggerManager("Logdog", this);

        loggerManager.parseLogsFrom(AppData.getLogsString(this));


        List<LoggerManager.Log> logs = loggerManager.getLogs();

        for(LoggerManager.Log log : logs){
            TextView textView = new TextView(this);
            textView.setText(dateFormat.format(log.date) + "|" + log.type + "| " + log.tag + ": ");
            textView.append(Html.fromHtml("<b>" + log.text + "</b>", 0));
            switch (log.type){
                case "ERROR":
                    textView.setTextColor(Color.RED);
                    break;
                case "WARNING":
                    textView.setTextColor(Color.parseColor("#ffa500"));
                    break;
                case "DEBUG":
                    textView.setTextColor(Color.GRAY);
                    break;
                default:
                    textView.setTextColor(Color.BLACK);
                    break;
            }
            linearLayout.addView(textView);
        }
    }
}
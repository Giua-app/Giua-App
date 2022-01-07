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

package com.giua.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.giua.utils.JsonBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class BugReportActivity extends AppCompatActivity {

    Button btnSend;
    TextInputLayout bugTitle;
    TextInputLayout bugDesc;
    ProgressBar progressBar;
    String dontStealMePlease = "dontStealMePlease";
    String stacktrace;
    boolean fromCAOC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bugreport);

        btnSend = findViewById(R.id.bug_send_button);
        bugTitle = findViewById(R.id.txtBugTitle);
        bugDesc = findViewById(R.id.txtBugDesc);
        progressBar = findViewById(R.id.bug_progressbar);

        btnSend.setOnClickListener(v -> onSendBug());
        dontStealMePlease = BuildConfig.SECRET_KEY;

        fromCAOC = getIntent().getBooleanExtra("fromCAOC", false);

        if(fromCAOC){
            stacktrace = getIntent().getStringExtra("stacktrace");
        }
    }

    private void onSendBug() {
        btnSend.setVisibility(View.INVISIBLE);
        bugTitle.setEnabled(false);
        bugDesc.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            String body = "**Descrizione del bug**\n" +
                    bugDesc.getEditText().getText() + "\n\n" +
                    "**Indica i passaggi per riprodurre il bug**\n" +
                    "1. Andare su...\n" +
                    "2. Premere...\n" +
                    "3. Cliccare su...\n\n";

            if(fromCAOC){
                body += "\n\n**Stacktrace**\n" + stacktrace;
            }

            try {
                Connection session = Jsoup.newSession().ignoreContentType(true);
                session.url("https://api.github.com/repos/HiemSword/regnodellasintassi-site/issues")
                        .header("Authorization", "token " + new Secrets().getgEPeTNbQ(getPackageName()) + dontStealMePlease)
                        .header("Accept", "application/vnd.github.v3+json")
                        .requestBody("{\"title\": \"" + bugTitle.getEditText().getText() + "\"," +
                                "\"body\": \"" + JsonBuilder.escape(body) + "\"}")
                        .post();
            } catch(Exception e){
                Snackbar.make(findViewById(android.R.id.content), "Errore durante l'invio del Bug Report", Snackbar.LENGTH_SHORT).show();
            }
            runOnUiThread(() -> {
                btnSend.setVisibility(View.VISIBLE);
                bugTitle.setEnabled(true);
                bugDesc.setEnabled(true);
                progressBar.setVisibility(View.INVISIBLE);
            });
        }).start();
    }
}
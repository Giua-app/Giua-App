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

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giua.app.ActivityManager;
import com.giua.app.AppData;
import com.giua.app.BuildConfig;
import com.giua.app.LoggerManager;
import com.giua.app.R;
import com.giua.app.Secrets;
import com.giua.utils.JsonBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BugReportActivity extends AppCompatActivity {

    Button btnSend;
    Button btnCancel;
    TextInputLayout txBugTitle;
    TextInputLayout txBugDesc;
    TextInputLayout txBugDesc2;
    ProgressBar progressBar;
    String dontStealMePlease = "dontStealMePlease";
    String stacktrace;
    LoggerManager lm;
    boolean fromCAOC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bugreport);

        lm = new LoggerManager("BugReportActivity", this);

        btnSend = findViewById(R.id.btn_bug_send);
        btnCancel = findViewById(R.id.btn_bug_cancel);
        txBugTitle = findViewById(R.id.txtBugTitle);
        txBugDesc = findViewById(R.id.txtBugDesc);
        txBugDesc2 = findViewById(R.id.txtBugDesc2);
        progressBar = findViewById(R.id.bug_progressbar);

        btnSend.setOnClickListener(v -> onSendBug());
        btnCancel.setOnClickListener(v -> exitActivity());
        dontStealMePlease = BuildConfig.SECRET_KEY;

        fromCAOC = getIntent().getBooleanExtra("fromCAOC", false);

        if (fromCAOC) {
            stacktrace = getIntent().getStringExtra("stacktrace");
        }
    }

    private void exitActivity(){
        if(fromCAOC){
            lm.w("Tento di avviare ActivityManager");
            startActivity(new Intent(this, ActivityManager.class));
        }
        finish();
    }

    private void onSendBug() {
        if (System.currentTimeMillis() - AppData.getLastSentReportTime(this) <= 60_000) {
            setErrorMessage("Devi aspettare almeno un minuto dall'ultima segnalazione prima di inviarne un' altra");
            return;
        }

        if (txBugTitle.getEditText().getText().toString().equals("")) {
            setErrorMessage("Il titolo non può essere vuoto");
            return;
        }
        if (txBugDesc.getEditText().getText().toString().equals("")) {
            setErrorMessage("La descrizione non può essere vuota");
            return;
        }
        if (txBugDesc2.getEditText().getText().toString().equals("")) {
            setErrorMessage("I passaggi non possono essere vuoti");
            return;
        }

        btnSend.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
        txBugTitle.setEnabled(false);
        txBugDesc.setEnabled(false);
        txBugDesc2.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        String body = "**Descrizione del bug**\n" +
                txBugDesc.getEditText().getText() + "\n\n" +
                "**Indica i passaggi per riprodurre il bug**\n" +
                txBugDesc2.getEditText().getText();

        if(fromCAOC){
            body += "\n\n**Stacktrace**\n" + stacktrace;
        } else {
            body += "\n\n**Informazioni dispositivo**\n" + deviceInfo();
        }

        viewPreview(body);
    }

    private void viewPreview(String body){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Anteprima");
        builder.setMessage("Anteprima del Bug Report: \n" + body)
                .setPositiveButton("Invia", (dialog, id) -> sendBug(body))

                .setNegativeButton("Annulla", (dialog, id) -> resetComponents())

                .setCancelable(false);

        builder.show();
    }

    private void viewIssue(Document doc){
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(doc.text());
        } catch (IOException e) {
            //Non dovrebbe MAI succedere
            lm.e("Impossibile leggere json di risposta di github");
            exitActivity();
            return;
        }

        String url = rootNode.findPath("html_url").asText();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bug report");
        builder.setMessage("Vuoi vedere il bug report pubblicato? (" + url + ")")
                .setPositiveButton("Si", (dialog, id) -> {
                    Intent i = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
                    startActivity(i);
                    exitActivity();
                })

                .setNegativeButton("No", (dialog, id) -> exitActivity())

                .setCancelable(false);

        builder.show();
    }

    private void sendBug(String body){
        new Thread(() -> {
            try {
                Connection session = Jsoup.newSession().ignoreContentType(true);
                Document doc = session.url("https://api.github.com/repos/HiemSword/regnodellasintassi-site/issues")
                        .header("Authorization", "token " + new Secrets().getgEPeTNbQ(getPackageName()) + dontStealMePlease)
                        .header("Accept", "application/vnd.github.v3+json")
                        .requestBody("{\"title\": \"" + txBugTitle.getEditText().getText() + "\"," +
                                "\"body\": \"" + JsonBuilder.escape(body) + "\"}")
                        .post();
                AppData.saveLastSentReportTime(this, System.currentTimeMillis());
                runOnUiThread(() -> viewIssue(doc));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setErrorMessage("Errore durante l'invio del Bug Report");
                    resetComponents();
                });
            }
        }).start();
    }

    private String deviceInfo(){
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY);


        String info = "";

        info += "Build version: " + BuildConfig.VERSION_NAME + " \n";
        info += "Build date: " + dateFormat.format(new Date(BuildConfig.BUILD_TIME)) + " \n";
        info += "Current date: " + dateFormat.format(currentDate) + " \n";
        info += "Device: " + getDeviceModelName() + " \n";
        info += "OS version: Android " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")\n";

        return info;
    }



    private void resetComponents() {
        btnSend.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
        txBugTitle.setEnabled(true);
        txBugDesc.setEnabled(true);
        txBugDesc2.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void setErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Code from CAOC
     * @return
     */
    private String getDeviceModelName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * Code from CAOC
     * @param s
     * @return
     */
    private String capitalize(@Nullable String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
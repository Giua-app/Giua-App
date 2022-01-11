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
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giua.app.AppData;
import com.giua.app.BuildConfig;
import com.giua.app.LoggerManager;
import com.giua.app.R;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

public class TransparentUpdateDialogActivity extends AppCompatActivity {

    String url;
    String tagName;
    Calendar date;
    LoggerManager loggerManager;
    JsonNode rootNode;

    //TODO: Rendere questa activity universale per qualsiasi dialogo a cui serva una activity trasparente

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent);
        loggerManager = new LoggerManager("TrasparentUpdateDialogActivity", this);
        loggerManager.d("onCreate chiamato");

        String json = getIntent().getStringExtra("json");
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            rootNode = objectMapper.readTree(json);
        } catch (IOException e) {
            loggerManager.e("Impossibile leggere json! - " + e.getMessage());
            e.printStackTrace();
            finish();
        }

        url = Objects.requireNonNull(rootNode).findPath("browser_download_url").asText();
        tagName = rootNode.findPath("tag_name").asText();
        date = Calendar.getInstance();

        if(getIntent().getBooleanExtra("doUpdate", false)){
            showDialogNewUpdate();
            return;
        }
        showDialogUpdateChangelog();
    }


    private void showDialogUpdateChangelog(){
        loggerManager.d("Mostro dialogo changelog");
        String body = rootNode.findPath("body").asText();

        final SpannableString txt = new SpannableString(Html.fromHtml(body, 0));
        Linkify.addLinks(txt, Linkify.ALL);

        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Novità della versione " + tagName)
                .setMessage(txt)
                .setPositiveButton("Chiudi", (dialog, id) -> finish())
                .setOnDismissListener(dialog -> finish())
                .create();

        d.show();
        ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }


    private void showDialogNewUpdate() {
        loggerManager.d("Mostro dialogo per aggiornamento app");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Aggiornamento");
        builder.setMessage("La tua versione attuale è la " + BuildConfig.VERSION_NAME + "\nLa nuova versione è la " + tagName + "\n\nVuoi aggiornare l'app?\n" +
                "Nota: I tuoi dati NON VERRANNO cancellati, verranno scaricati circa 10MB")
                .setPositiveButton("Si", (dialog, id) -> new Thread(this::downloadInstallApk).start());

        builder.setNeutralButton("Ricorda domani", (dialog, id) -> {
            loggerManager.d("Aggiornamento posticipato dall'utente a domani");
            AppData.saveLastUpdateReminderDate(TransparentUpdateDialogActivity.this, date);
        });

        builder.setNegativeButton("No", (dialog, id) -> {
            loggerManager.d("Aggiornamento negato senza postipazione dall'utente");
        })
                .setOnCancelListener(dialog -> {
                    loggerManager.d("Dialogo aggiornamento cancellato");
                    finish();
                })
                .setOnDismissListener(dialog -> {
                    loggerManager.d("Dialogo aggiornamento dismesso");
                    finish();
                });

        builder.show();
    }


    private void downloadInstallApk(){
        loggerManager.d("Aggiornamento richiesto dall'utente");
        loggerManager.d("Scarico aggiornamento...");
        runOnUiThread(() -> Toast.makeText(this, "Download in corso...", Toast.LENGTH_SHORT).show());
        String downloadLocation = getExternalFilesDir(null) + "/giua_update.apk";

        File file = new File(downloadLocation);
        Uri uri = Uri.parse("file://" + file.getAbsolutePath());

        loggerManager.d("Directory: " + downloadLocation);


        if(!file.delete()){
            loggerManager.w("Errore nel cancellare file apk precedente! E' la prima volta che si aggiorna?");
        }


        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(tagName);
        request.setTitle("Download Giua App");


        request.setDestinationUri(uri);


        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);
        loggerManager.d("Download messo in coda");

        //Eseguito quando finisce di scaricare
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @SuppressLint("ObsoleteSdkInt")
            public void onReceive(Context ctxt, Intent intent) {
                //Controlla se il broadcast ha lo stesso id del download (cioe se è nostro o no)
                long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == referenceId) {
                    loggerManager.d("Download completato");
                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    Uri downloadUri = Uri.parse(downloadLocation);

                    loggerManager.d("Lettura file...");

                    if (android.os.Build.VERSION.SDK_INT >= 24) {

                        File file = new File(downloadUri.getPath());
                        if (file.exists()) {
                            Uri uri = FileProvider.getUriForFile(TransparentUpdateDialogActivity.this, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                            installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //Required for Android 8+
                            //installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
                            loggerManager.d("Avvio dialogo per installare aggiornamento");
                            startActivity(installIntent);

                        } else {
                            loggerManager.e("Errore: file apk scaricato non trovato");
                            Toast.makeText(TransparentUpdateDialogActivity.this, "Errore, file scaricato non trovato", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        installIntent.setDataAndType(downloadUri, "application/vnd.android.package-archive");
                        loggerManager.d("Avvio dialogo per installare aggiornamento");
                        startActivity(installIntent);
                    }
                }
                loggerManager.d("Chiusura transparent activity");
                finish();
            }
        };


        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}

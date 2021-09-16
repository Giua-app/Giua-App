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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.giua.app.AppData;
import com.giua.app.BuildConfig;
import com.giua.app.R;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class TransparentUpdateDialogActivity extends AppCompatActivity {

    String url;
    String newVer;
    Date time;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent);
        url = getIntent().getStringExtra("url");
        newVer = getIntent().getStringExtra("newVersion");
        time = Calendar.getInstance().getTime();
        showDialog();
    }


    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Aggiornamento");
        builder.setMessage("La tua versione attuale è la " + BuildConfig.VERSION_NAME + "\nLa nuova versione è la " + newVer + "\n\nVuoi aggiornare l'app?\n" +
                "Nota: I tuoi dati NON VERRANNO cancellati, l'app ti chiedera l'accesso alla memoria e poi il file verrà scaricato in background")

                .setPositiveButton("Si", (dialog, id) -> {
                    new Thread(() -> {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                0);
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(TransparentUpdateDialogActivity.this, "Errore, permesso negato", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        downloadInstallApk();

                    }).start();
                })
                .setNegativeButton("Ricorda tra un giorno", (dialog, id) -> {AppData.setLastUpdateReminder(TransparentUpdateDialogActivity.this, time);});

        builder.show();
    }


    private void downloadInstallApk(){
        String downloadLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/giua_update.apk";
        File file = new File(downloadLocation);
        Uri uri = Uri.parse("file://" + downloadLocation);

        if(file.exists()){
            file.delete();
        }


        //set downloadmanager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Scarico aggiornamento Giua App");
        request.setTitle("Download Giua App " + newVer);

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        //set BroadcastReceiver to install app when .apk is downloaded
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @SuppressLint("ObsoleteSdkInt")
            public void onReceive(Context ctxt, Intent intent) {
                //check if the broadcast message is for our Enqueued download
                long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == referenceId) {

                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    Uri downloadUri = Uri.parse(downloadLocation);


                    if (android.os.Build.VERSION.SDK_INT >= 24) {

                        File file = new File(downloadUri.getPath());
                        if (file.exists()) {
                            Uri uri = FileProvider.getUriForFile(TransparentUpdateDialogActivity.this, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                            installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //Required for Android 8+
                            //installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
                            startActivity(installIntent);

                        } else {
                            Toast.makeText(TransparentUpdateDialogActivity.this, "Errore, file scaricato non trovato", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        installIntent.setDataAndType(downloadUri, "application/vnd.android.package-archive");
                        startActivity(installIntent);
                    }
                }
                AppData.setLastUpdateReminder(TransparentUpdateDialogActivity.this, time);
            }
        };

        //register receiver for when .apk download is compete
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}

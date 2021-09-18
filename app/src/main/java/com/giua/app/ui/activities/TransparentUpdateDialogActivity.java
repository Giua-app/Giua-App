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
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
                "Nota: I tuoi dati NON VERRANNO cancellati, il file occupa circa 9MB")

                .setPositiveButton("Si", (dialog, id) -> new Thread(this::downloadInstallApk).start())
                .setNegativeButton("Ricorda tra un giorno", (dialog, id) -> AppData.setLastUpdateReminder(TransparentUpdateDialogActivity.this, time))
                .setOnCancelListener(dialog -> finish())
                .setOnDismissListener(dialog -> finish());

        builder.show();
    }


    private void downloadInstallApk(){
        String downloadLocation = getExternalFilesDir(null) + "/giua_update.apk";

        File file = new File(downloadLocation);
        Uri uri = Uri.parse("file://" + file.getAbsolutePath());


        if(!file.delete()){
            Log.w("TEST","Errore nel cancellare apk!");
        }


        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(newVer);
        request.setTitle("Download Giua App");


        request.setDestinationUri(uri);


        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        //Eseguito quando finisce di scaricare
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @SuppressLint("ObsoleteSdkInt")
            public void onReceive(Context ctxt, Intent intent) {
                //Controlla se il broadcast ha lo stesso id del download (cioe se è nostro o no)
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
                //L'installazione è gia iniziata quando si arriva qui, praticamente sono gli ultimi instanti dell'app
                AppData.setLastUpdateReminder(TransparentUpdateDialogActivity.this, time);
                finish();
            }
        };


        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}

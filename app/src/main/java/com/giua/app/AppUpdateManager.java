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

package com.giua.app;


import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class AppUpdateManager {

    Connection session = Jsoup.newSession().ignoreContentType(true);
    byte[] apkFile;
    private NotificationManagerCompat notificationManager;
    String[] updateVer;
    String tagName;
    String[] currentVer;

    public void checkForAppUpdates(Context context){

        //TODO: in futuro quando si scaricheranno gli apk
        /*if(BuildConfig.BUILD_TYPE.equals("debug")){
            //Non si possono aggiornare le build di debug, annulla silenziosamente
            //return;
        }*/

        String response = "";
        notificationManager = NotificationManagerCompat.from(context);

        try {
            response = session
                    .url("https://api.github.com/repos/giua-app/giua-app/releases/latest")
                    .get().text();
        } catch (IOException e) { e.printStackTrace(); }

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(response);
        } catch (IOException e) { e.printStackTrace(); }

        JsonNode assetsNode = Objects.requireNonNull(rootNode).findPath("assets_url");

        if(assetsNode.isMissingNode()){
            //Assets non trovati, nessun apk
            return;
        }

        String assetsUrl = assetsNode.asText();
        tagName = rootNode.findPath("tag_name").asText();

        //DEBUG
        /*try {
            response = session
                    .url(assetsUrl)
                    .get().text();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        String downloadUrl = rootNode.findPath("browser_download_url").asText();
        String contentType = rootNode.findPath("content_type").asText();

        String semverRegex = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";

        currentVer = BuildConfig.VERSION_NAME.split("-")[0].split("\\.");
        if(tagName.matches(semverRegex)){
            updateVer = tagName.split("-")[0].split("\\.");
        } else {
            //Non è una versione, esci silenziosamente
            return;
        }

        Log.d("LOOK", "-> " + Arrays.toString(updateVer) + " " + Arrays.toString(currentVer));

        if (currentVer[0].equals(updateVer[0]) && currentVer[1].equals(updateVer[1]) && currentVer[2].equals(updateVer[2])) {
            //Nessun aggiornamento, esci silenziosamente
            return;
        }

        if (AppData.getLastUpdateVersionKey(context).equals(tagName)) {
            //Questo aggiornamento è già stato notificato all'utente, esci silenziosamente
            return;
        }

        //Se siamo arrivati fino a qui vuol dire che c'è un aggiornamento
        sendUpdateNotific(context);
        AppData.saveLastUpdateVersionString(context, tagName);

        //TODO: una volta scaricato l'apk chiedere all'utente di installare
        /*Uri uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/giua_update.apk");

        //set downloadmanager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setDescription("Scarico aggiornamento Giua App");
        request.setTitle("Download Giua App " + tagName);

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        //set BroadcastReceiver to install app when .apk is downloaded
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                sendUpdateNotific(context, tagName);
                context.unregisterReceiver(this);
            }
        };

        //register receiver for when .apk download is compete
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));*/

    }

    private void sendUpdateNotific(Context context){
        createNotification(context,"Nuova versione rilevata", "Hai la versione " + BuildConfig.VERSION_NAME + "\nLa nuova versione è " + tagName);
    }


    private void createNotification(Context context, String title, String description){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.ic_giuaschool_logo1)
                .setContentTitle(title)
                //.setContentIntent()
                .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(15, builder.build());
    }

}

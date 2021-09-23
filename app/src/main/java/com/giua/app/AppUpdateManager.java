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


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giua.app.ui.activities.TransparentUpdateDialogActivity;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class AppUpdateManager {

    Connection session = Jsoup.newSession().ignoreContentType(true);
    byte[] apkFile;
    private NotificationManagerCompat notificationManager;
    Integer[] updateVer = {0,0,0};
    String tagName;
    Integer[] currentVer = {0,0,0};
    String downloadUrl;
    LoggerManager loggerManager;

    public void checkForAppUpdates(Context context, boolean sendNotification) {
        loggerManager = new LoggerManager("AppUpdateManager", context);
        loggerManager.d("checking for updates...");

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
            loggerManager.e("No assets found on release");
            return;
        }

        String assetsUrl = assetsNode.asText();
        tagName = rootNode.findPath("tag_name").asText();

        downloadUrl = rootNode.findPath("browser_download_url").asText();
        String contentType = rootNode.findPath("content_type").asText();

        String semverRegex = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";

        String[] temp = BuildConfig.VERSION_NAME.split("-")[0].split("\\.");
        currentVer[0] = Integer.parseInt(temp[0]);
        currentVer[1] = Integer.parseInt(temp[1]);
        currentVer[2] = Integer.parseInt(temp[2]);
        if(tagName.matches(semverRegex)){
            temp = tagName.split("-")[0].split("\\.");
            updateVer[0] = Integer.parseInt(temp[0]);
            updateVer[1] = Integer.parseInt(temp[1]);
            updateVer[2] = Integer.parseInt(temp[2]);
        } else {
            //Non è una versione, esci silenziosamente
            loggerManager.e("Tag version found on github is not semver complaint, ignoring");
            return;
        }

        if(!contentType.equals("application/vnd.android.package-archive")){
            //Il file non è un apk, ignora
            loggerManager.e("Asset on release is not an APK file, ignoring");
            return;
        }


        if (currentVer[0].equals(updateVer[0]) && currentVer[1].equals(updateVer[1]) && currentVer[2].equals(updateVer[2])) {
            //Nessun aggiornamento, esci silenziosamente
            loggerManager.e("No new updates found, current version is " + BuildConfig.VERSION_NAME  + ", latest on github is " + tagName);
            return;
        }

        if(currentVer[0] > updateVer[0] || currentVer[1] > updateVer[1] || currentVer[2] > updateVer[2]){
            //Versione vecchia, esci silenziosamente
            loggerManager.e("Current application is newer than github release, ignoring");
            return;
        }

        Date date = Calendar.getInstance().getTime();
        Date lastUpdateDate = date;
        try {
            lastUpdateDate = AppData.getLastUpdateReminderDate(context);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        if (date.after(lastUpdateDate)) {
            //Aggiornamento gia notificato, nextUpdateDate è il prossimo giorno in cui notificare
            loggerManager.e("Update already notified, ignoring");
            return;
        }

        //Se siamo arrivati fino a qui vuol dire che c'è un aggiornamento
        if (sendNotification)
            createNotification(context);
        AppData.saveLastUpdateVersionString(context, tagName);
        AppData.saveUpdatePresence(context, true);
    }


    private void createNotification(Context context){

        loggerManager.d("Creating update notification...");

        String title = "Nuova versione rilevata";
        String description = "Clicca per informazioni";

        Intent intent = new Intent(context, TransparentUpdateDialogActivity.class);
        intent.putExtra("url", downloadUrl);
        intent.putExtra("newVersion", tagName);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.ic_giuaschool_logo1)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(15, builder.build());
    }

}

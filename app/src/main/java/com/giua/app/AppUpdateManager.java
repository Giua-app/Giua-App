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


import android.annotation.SuppressLint;
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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class AppUpdateManager {

    final Connection session = Jsoup.newSession().ignoreContentType(true);
    String tagName;
    LoggerManager loggerManager;
    Context context;
    //Semantic Version Regex
    final String semVerRegex = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";
    private int upgradable = -1;

    public AppUpdateManager(Context context){
        this.context = context;
        loggerManager = new LoggerManager("AppUpdateManager", this.context);
    }



    public void deleteOldApk(){
        String downloadLocation = context.getExternalFilesDir(null) + "/giua_update.apk";
        File file = new File(downloadLocation);

        if(!file.delete()){
            loggerManager.w("Errore nel cancellare file apk dell'aggiornamento! Forse si è aggiornato manualmente?");
        }
    }



    public void showDialogReleaseChangelog(){
        String json;
        try {
            json = getReleasesJson().toString();
        } catch(Exception e){
            loggerManager.e("Errore critico: " + e.getMessage());
            loggerManager.e(Arrays.toString(e.getStackTrace()));
            return;
        }
        Intent intent = new Intent(context, TransparentUpdateDialogActivity.class);
        intent.putExtra("json", json);

        context.startActivity(intent);
    }

    public static String getPrettyAppVersion() {
        String[] temp = BuildConfig.VERSION_NAME.split("-")[0].split("\\.");
        return temp[0] + "." + temp[1] + "." + temp[2];
    }

    /**
     * Confronta {@code version1} con {@code version2}
     *
     * @param version1 un array formato dai numeri della versione
     * @param version2 un array formato dai numeri della versione
     * @return 1 se {@code version1} è maggiore di {@code version2} <br>
     * 0 se sono uguali <br>
     * -1 se {@code version1} è minore di {@code version2}
     */
    public static int compareVersions(int[] version1, int[] version2) {
        int result = version1[0] - version2[0];
        if (result == 0) {
            result = version1[1] - version2[1];
            if (result == 0) {
                result = version1[2] - version2[2];
            }
        }

        return Integer.compare(result, 0);
    }


    public JsonNode getReleasesJson() {
        String response;
        try {
            response = session.newRequest()
                    .url("https://api.github.com/repos/giua-app/giua-app/releases")
                    .execute().body();
        } catch (IOException e) {
            loggerManager.e("Impossibile contattare API di github! - " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(response);
        } catch (IOException e) {
            loggerManager.e("Impossibile leggere json! - " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return rootNode;
    }

    /**
     * Controlla se c'è un aggiornamento
     *
     * @return true se c'è, false se non c'è
     */
    public boolean checkForUpdates() {
        if (upgradable != -1) return upgradable == 1;

        int[] currentVer = new int[3];
        int[] updateVer = new int[3];

        loggerManager.d("Controllo aggiornamenti...");
        JsonNode rootNode;
        try {
            rootNode = getReleasesJson().get(0);
        } catch (Exception e) {
            loggerManager.e("Errore critico: " + e.getMessage());
            loggerManager.e(Arrays.toString(e.getStackTrace()));
            upgradable = 0;
            return false;
        }


        if (rootNode == null) {   //Si è verificato un errore di qualche tipo
            loggerManager.e("Errore critico sconosciuto");
            upgradable = 0;
            return false;
        }


        tagName = rootNode.findPath("tag_name").asText();

        loggerManager.d("Versione tag github: " + tagName);
        loggerManager.d("Versione app: " + BuildConfig.VERSION_NAME);
        String[] temp = BuildConfig.VERSION_NAME.split("-")[0].split("\\.");
        currentVer[0] = Integer.parseInt(temp[0]);
        currentVer[1] = Integer.parseInt(temp[1]);
        currentVer[2] = Integer.parseInt(temp[2]);
        if (tagName.matches(semVerRegex)) {
            temp = tagName.split("-")[0].split("\\.");
            updateVer[0] = Integer.parseInt(temp[0]);
            updateVer[1] = Integer.parseInt(temp[1]);
            updateVer[2] = Integer.parseInt(temp[2]);
        } else {
            //Non è una versione, esci silenziosamente
            loggerManager.w("Versione tag trovata su github non rispetta SemVer, annullo");
            upgradable = 0;
            return false;
        }

        if (compareVersions(updateVer, currentVer) == 1) {
            loggerManager.w("Rilevata nuova versione");
            upgradable = 1;
            return true;
        }

        loggerManager.w("Nessuna nuova versione rilevata");
        upgradable = 0;
        return false;
    }

    /**
     * Controlla LastUpdateReminderDate, serve a mandare la notifica di aggiornamento una sola volta al giorno
     * @return true se si può inviare l'update, false se bisogna aspettare ad un altro giorno
     */
    //TODO: trasferire in AppUtils
    public boolean checkUpdateReminderDate(){
        int dayOfYear;
        int year;
        Calendar yesterdayCal = Calendar.getInstance();
        yesterdayCal.set(Calendar.DAY_OF_YEAR, Calendar.getInstance().get(Calendar.DAY_OF_YEAR)-1);
        try {
            dayOfYear = Integer.parseInt(AppData.getLastUpdateReminderDate(context).split("#")[0]);
            year = Integer.parseInt(AppData.getLastUpdateReminderDate(context).split("#")[1]);
        } catch(Exception e){
            loggerManager.e("Errore critico nel parsing di LastUpdateReminder, è possibile che non sia mai stato notificato?");
            loggerManager.e("Sovrascrivo LastReminder con la data di ieri e notifico l'update");
            AppData.saveLastUpdateReminderDate(context, yesterdayCal);
            return true;
        }
        loggerManager.d("L'ultima volta che ho ricordato l'aggiornamento è stato il " + dayOfYear + "° giorno dell'anno " + year);
        loggerManager.d("Oggi è " + Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + ", ieri invece era il " + yesterdayCal.get(Calendar.DAY_OF_YEAR));


        if(Calendar.getInstance().get(Calendar.YEAR) != year){
            loggerManager.e("Errore, anno diverso da quello corrente. " +
                    "Non è possibile confrontare ReminderDate, cambio reminder a ieri e avviso l'utente dell'update");
            AppData.saveLastUpdateReminderDate(context, yesterdayCal);
            return true;
        }

        if(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) > dayOfYear){
            loggerManager.d("Reminder passato, bisogna ricordare l'utente dell'update");
            return true;
        }

        loggerManager.w("Il LastReminder è di oggi (o avanti nel tempo). Update gia notificato non c'è bisogno di rifarlo di nuovo");
        return false;
    }


    public void createNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        loggerManager.d("Creo notifica aggiornamento...");

        String title = "Nuova versione rilevata (" + tagName + ")";
        String description = "Clicca per informazioni";

        Intent intent = new Intent(context, TransparentUpdateDialogActivity.class);
        intent.putExtra("json", getReleasesJson().toString());
        intent.putExtra("doUpdate", true);
        PendingIntent pendingIntent;

        pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.ic_giuaschool_black)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(15, builder.build());
    }

    public void startUpdateDialog(){
        Intent intent = new Intent(context, TransparentUpdateDialogActivity.class);
        intent.putExtra("json", getReleasesJson().toString());
        intent.putExtra("doUpdate", true);
        context.startActivity(intent);
    }

    @SuppressLint("SimpleDateFormat")
    public static String buildChangelogForHTML(JsonNode rootNode){

        StringBuilder changelog = new StringBuilder();
        Iterator<JsonNode> releases = rootNode.elements();

        while(releases.hasNext()){
            JsonNode release = releases.next();

            String tag = release.findPath("tag_name").asText();
            String body = release.findPath("body").asText();

            String date = release.findPath("published_at").asText();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
            Date date1 = new Date(0);
            try {
                date1 = format.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return "Errore durante la lettura dei changelog";
            }

            if(!body.equals("")){
                changelog.append("<hr><h1>").append(tag).append(" <span style=\"font-size: 60%;\">(")
                        .append(format1.format(date1)).append(")</span></h1>").append(body);
            }
        }

        changelog.append("<br><br>");
        return changelog.toString();

    }

}

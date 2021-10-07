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

public class AppUpdateManager {

    final Connection session = Jsoup.newSession().ignoreContentType(true);
    Integer[] updateVer = {0,0,0};
    String tagName;
    Integer[] currentVer = {0,0,0};
    LoggerManager loggerManager;
    Context context;
    final String semverRegex = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";


    public AppUpdateManager(Context context){
        this.context = context;
        loggerManager = new LoggerManager("AppUpdateManager", this.context);
    }

    public boolean checkForUpdates(){
        loggerManager.d("Controllo aggiornamenti...");

        JsonNode rootNode = getReleasesJson();

        if (rootNode == null)    //Si è verificato un errore di qualche tipo
            return false;

        tagName = rootNode.findPath("tag_name").asText();

        loggerManager.d("Versione tag github: " + tagName);
        loggerManager.d("Versione app: " + BuildConfig.VERSION_NAME);
        String[] temp = BuildConfig.VERSION_NAME.split("-")[0].split("\\.");
        currentVer[0] = Integer.parseInt(temp[0]);
        currentVer[1] = Integer.parseInt(temp[1]);
        currentVer[2] = Integer.parseInt(temp[2]);
        if (tagName.matches(semverRegex)) {
            temp = tagName.split("-")[0].split("\\.");
            updateVer[0] = Integer.parseInt(temp[0]);
            updateVer[1] = Integer.parseInt(temp[1]);
            updateVer[2] = Integer.parseInt(temp[2]);
        } else {
            //Non è una versione, esci silenziosamente
            loggerManager.w("Versione tag trovata su github non rispetta SemVer, annullo");
            return false;
        }

        if(isUpdateNewerThanApp()){
            loggerManager.w("Rilevata nuova versione");
            return true;
        }
        loggerManager.w("Nessuna nuova versione rilevata");
        return false;
    }

    public JsonNode getReleasesJson(){
        String response = "";
        try {
            response = session
                    .url("https://api.github.com/repos/giua-app/giua-app/releases/latest")
                    .get().text();
        } catch (IOException e) {
            loggerManager.e("Impossibile contattare API di github! - " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(response);
        } catch (IOException e) {
            loggerManager.e("Impossibile leggere json! - " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return rootNode;
    }

    private boolean isUpdateNewerThanApp(){
        if (currentVer[0].equals(updateVer[0]) && currentVer[1].equals(updateVer[1]) && currentVer[2].equals(updateVer[2])) {
            //Nessun aggiornamento
            return false;
        }

        //false = versione vecchia, true = versione nuova
        return currentVer[0] <= updateVer[0] && currentVer[1] <= updateVer[1] && currentVer[2] <= updateVer[2];
    }

    /**
     * Controlla LastUpdateReminderDate
     * @return true se si può inviare l'update, false se bisogna aspettare ad un altro giorno
     */
    public boolean checkUpdateReminderDate(){
        /*boolean thing = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) > AppData.getLastUpdateReminderDate(context);
        loggerManager.d("Devo ricordare l'update? " + thing + ", ultima volta che lo ricordato: " + AppData.getLastUpdateReminderDate(context));

        if(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) == 0){
            loggerManager.e("Errore, è possibile che oggi sia un anno nuovo. Non è possibile confrontare ReminderDate, " +
                    "ignoro, cambio reminder a oggi e avviso l'utente dell'update");
            AppData.saveLastUpdateReminderDate(context, 0);
            return true;
        }
        return thing;*/
        return true;
    }


    /*public void checkForAppUpdates(Context context, boolean sendNotification) {
        loggerManager.d("Controllo aggiornamenti...");

        //TODO: in futuro quando si scaricheranno gli apk
        /*if(BuildConfig.BUILD_TYPE.equals("debug")){
            //Non si possono aggiornare le build di debug, annulla silenziosamente
            //return;
        }

        String response = "";
        notificationManager = NotificationManagerCompat.from(context);

        try {
            response = session
                    .url("https://api.github.com/repos/giua-app/giua-app/releases/latest")
                    .get().text();
        } catch (IOException e) {
            loggerManager.e("Impossibile contattare API di github! - " + e.getMessage());
            e.printStackTrace();
            AppData.saveUpdatePresence(context, false);
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(response);
        } catch (IOException e) {
            loggerManager.e("Impossibile leggere json! - " + e.getMessage());
            e.printStackTrace();
            AppData.saveUpdatePresence(context, false);
            return;
        }

        JsonNode assetsNode = Objects.requireNonNull(rootNode).findPath("assets_url");

        if(assetsNode.isMissingNode()){
            //Assets non trovati, nessun apk
            loggerManager.w("Assets non presenti sulla release");
            AppData.saveUpdatePresence(context, false);
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
            loggerManager.w("Versione su Tag trovata su github non rispetta SemVer, annullo");
            AppData.saveUpdatePresence(context, false);
            return;
        }

        if(!contentType.equals("application/vnd.android.package-archive")){
            //Il file non è un apk, ignora
            loggerManager.w("Asset sulla release non è un file APK, annullo");
            AppData.saveUpdatePresence(context, false);
            return;
        }


        if (currentVer[0].equals(updateVer[0]) && currentVer[1].equals(updateVer[1]) && currentVer[2].equals(updateVer[2])) {
            //Nessun aggiornamento, esci silenziosamente
            loggerManager.w("Nessun aggiornamento trovato, versione corrente è " + BuildConfig.VERSION_NAME + ", ultima su github è " + tagName);
            AppData.saveUpdatePresence(context, false);
            return;
        }

        if(currentVer[0] > updateVer[0] || currentVer[1] > updateVer[1] || currentVer[2] > updateVer[2]){
            //Versione vecchia, esci silenziosamente
            loggerManager.w("Versione dell'app maggiore di quella su github, annullo");
            AppData.saveUpdatePresence(context, false);
            return;
        }

        Date date = Calendar.getInstance().getTime();
        Date lastUpdateDate = date;
        try {
            lastUpdateDate = AppData.getLastUpdateReminderDate(context);
        } catch (ParseException e) {
            loggerManager.w("Impossibile leggere lastUpdateReminder! - " + e.getMessage());
            e.printStackTrace();
        }

        //Se siamo arrivati fino a qui vuol dire che c'è un aggiornamento
        AppData.saveLastUpdateVersionString(context, tagName);
        AppData.saveUpdatePresence(context, true);

        if (date.after(lastUpdateDate) && sendNotification) {
            //Aggiornamento gia notificato, nextUpdateDate è il prossimo giorno in cui notificare
            loggerManager.w("Aggiornamento già notificato, annullo");
            return;
        }

        createNotification(context, sendNotification);
    }*/


    public void createNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        loggerManager.d("Creo notifica aggiornamento...");

        String title = "Nuova versione rilevata (" + tagName + ")";
        String description = "Clicca per informazioni";

        Intent intent = new Intent(context, TransparentUpdateDialogActivity.class);
        //intent.putExtra("url", downloadUrl);
        intent.putExtra("json", getReleasesJson().toString());
        PendingIntent pendingIntent;

        pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.ic_giuaschool_black)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(15, builder.build());
    }

}

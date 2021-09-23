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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppData {

    /**
     * Controlla la memorizzazione dei dati dell'app
     */
    private static final String appDataPreferenceKey = "appData";
    private static final String voteKey = "votes";
    private static final String newsletterKey = "newsletters";
    private static final String alertKey = "alerts";
    private static final String numberNewslettersKey = "number_newsletters";
    private static final String numberAlertsKey = "number_alerts";
    private static final String lastUpdateVersionKey = "last_update_version";
    private static final String nextUpdateReminderKey = "next_update_reminder";
    private static final String updatePresenceKey = "update_presence";

    private static SharedPreferences getSharedPreferences(final Context context) {
        return context.getSharedPreferences(appDataPreferenceKey, Context.MODE_PRIVATE);
    }

    //region Dati per il json

    public static void saveVotesString(final Context context, final String jsonString) {
        getSharedPreferences(context).edit()
                .putString(voteKey, jsonString)
                .apply();
    }

    public static void saveNewslettersString(final Context context, final String jsonString) {
        getSharedPreferences(context).edit()
                .putString(newsletterKey, jsonString)
                .apply();
    }

    public static void saveAlertsString(final Context context, final String jsonString) {
        getSharedPreferences(context).edit()
                .putString(alertKey, jsonString)
                .apply();
    }

    public static String getVotesString(final Context context) {
        return getSharedPreferences(context).getString(voteKey, null);
    }

    public static String getNewslettersString(final Context context) {
        return getSharedPreferences(context).getString(newsletterKey, null);
    }

    public static String getAlertsString(final Context context) {
        return getSharedPreferences(context).getString(alertKey, null);
    }

    //endregion

    //region Dati del broadcast di background

    public static void saveNumberNewslettersInt(final Context context, final int value) {
        getSharedPreferences(context).edit()
                .putInt(numberNewslettersKey, value)
                .apply();
    }

    public static int getNumberNewslettersInt(final Context context) {
        return getSharedPreferences(context).getInt(numberNewslettersKey, -1);
    }

    public static void saveNumberAlertsInt(final Context context, final int value) {
        getSharedPreferences(context).edit()
                .putInt(numberAlertsKey, value)
                .apply();
    }

    public static int getNumberAlertsInt(final Context context) {
        return getSharedPreferences(context).getInt(numberAlertsKey, -1);
    }

    //endregion

    //region Dati dell'utlimo aggiornamento trovato

    public static void saveLastUpdateVersionString(final Context context, final String value) {
        getSharedPreferences(context).edit()
                .putString(lastUpdateVersionKey, value)
                .apply();
    }

    public static String getLastUpdateVersionString(final Context context) {
        return getSharedPreferences(context).getString(lastUpdateVersionKey, "");
    }

    public static void saveUpdatePresence(final Context context, final boolean value) {
        getSharedPreferences(context).edit()
                .putBoolean(updatePresenceKey, value)
                .apply();
    }

    public static boolean getUpdatePresence(final Context context) {
        return getSharedPreferences(context).getBoolean(updatePresenceKey, false);
    }

    @SuppressLint("SimpleDateFormat")
    public static void setLastUpdateReminderDate(final Context context, final Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String value = format.format(date);
        getSharedPreferences(context).edit()
                .putString(nextUpdateReminderKey, value)
                .apply();
        Log.d("TEST", "setdate: " + value);
    }

    @SuppressLint("SimpleDateFormat")
    public static Date getLastUpdateReminderDate(final Context context) throws ParseException {
        String value = getSharedPreferences(context).getString(nextUpdateReminderKey, "err");
        if (value.equals("err")) {
            return new Date();
        }
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Log.d("TEST", "getdate: " + value);
        return format.parse(value);
    }

    //endregion

    /**
     * Questa funzione, facendo una richiesta HTTP, aumenta il numero di visite
     * Piratepx tiene traccia delle visite per 30 giorni
     * In questo momento stiamo contando le visite per:
     * - Nuove installazioni
     * - Login studente/genitore
     * - Errori di webview
     * - Easter egg
     *
     * @param name Nome dell'oggetto su cui aumentare le visite
     */
    public static void increaseVisitCount(String name){
        if(BuildConfig.BUILD_TYPE.equals("debug")){
            //Ignora le build di debug
            return;
        }
        try {
            Jsoup.newSession().url("https://app.piratepx.com/ship?p=6af8462c-efe0-4f9a-96d0-ec9a8bbb9060&i=" + name).get();
        } catch (IOException ignored) { } //Non ci interessa se va in errore
    }
}

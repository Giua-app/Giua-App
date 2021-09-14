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
import android.content.SharedPreferences;

import org.jsoup.Jsoup;

import java.io.IOException;

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

    /**
     * Questa funzione facendo una richiesta web aumenta il numero di visite
     * Piratepx tiene traccia delle visite per 30 giorni ed è open source
     *
     * @param name Nome dell'oggetto su cui aumentare le visite
     */
    public static void increaseVisitCount(String name){
        try {
            Jsoup.newSession().url("https://app.piratepx.com/ship?p=a58207a1-9863-4bc9-a377-39ef3c802a24&i=" + name).get();
        } catch (IOException ignored) { } //Non ci interessa se va in errore
    }

    //endregion
}

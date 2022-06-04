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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArraySet;

import java.util.Calendar;
import java.util.Set;

public class AppData {

    /**
     * Controlla la memorizzazione dei dati dell'app
     */
    private static final String appDataPreferenceKey = "appData";

    //region Chiavi rimaste per compatibilità ma non più utilizzate

    //Chiavi per il salvataggio delle cose offline
    private static final String voteKey = "votes";
    private static final String newsletterKey = "newsletters";
    private static final String alertsKey = "alerts";

    //Chiavi per il salvataggio dei dati per le notifiche
    private static final String numberNewslettersKey = "number_newsletters";
    private static final String numberAlertsKey = "number_alerts";
    private static final String numberVotesKey = "number_votes";
    private static final String notificationErrorsCounter = "notification_errors";

    //endregion

    //Chiavi per il salvataggio delle versioni
    private static final String lastUpdateVersionKey = "last_update_version";
    private static final String nextUpdateReminderKey = "next_update_reminder";
    private static final String updatePresenceKey = "update_presence";
    private static final String logsStorageKey = "logs_storage";
    private static final String appVersionKey = "app_version";

    //Chiavi per il salvataggio delle configurazioni per più account
    private static final String allAccountUsernamesKey = "all_account_usernames";
    private static final String activeUsername = "active_username";

    private static final String introStatusKey = "intro_status";
    private static final String crashStatusKey = "crash_status";
    private static final String lastSentReportTimeKey = "last_sent_report_time";
    private static final String lastStartupDateKey = "last_startup_date";


    private static SharedPreferences getSharedPreferences(final Context context) {
        return context.getSharedPreferences(appDataPreferenceKey, Context.MODE_PRIVATE);
    }

    //region Funzioni per rimuove i dati inutili
    public static void clearOfflineData(final Context context) {
        getSharedPreferences(context).edit()
                .remove(voteKey)
                .remove(newsletterKey)
                .remove(alertsKey)
                .apply();
    }

    public static void clearNotificationsData(final Context context) {
        getSharedPreferences(context).edit()
                .remove(numberNewslettersKey)
                .remove(numberVotesKey)
                .remove(numberAlertsKey)
                .remove(notificationErrorsCounter)
                .apply();
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

    public static void saveAppVersion(final Context context, final String value) {
        getSharedPreferences(context).edit()
                .putString(appVersionKey, value)
                .apply();
    }

    public static String getAppVersion(final Context context) {
        return getSharedPreferences(context).getString(appVersionKey, "");
    }

    public static void saveLastUpdateReminderDate(final Context context, final Calendar date) {
        getSharedPreferences(context).edit()
                .putString(nextUpdateReminderKey, date.get(Calendar.DAY_OF_YEAR) + "#" + date.get(Calendar.YEAR))
                .apply();
    }

    public static String getLastUpdateReminderDate(final Context context) {
        return getSharedPreferences(context).getString(nextUpdateReminderKey, "");
    }

    //endregion

    //region Dati per più account

    public static void addAccountUsername(final Context context, final String username) {
        Set<String> allNames = getAllAccountUsernames(context);

        allNames.add(username);

        getSharedPreferences(context).edit()
                .putStringSet(allAccountUsernamesKey, allNames)
                .apply();
    }

    public static void removeAccountUsername(final Context context, final String username) {
        Set<String> allNames = getAllAccountUsernames(context);

        allNames.remove(username);

        getSharedPreferences(context).edit()
                .putStringSet(allAccountUsernamesKey, allNames)
                .apply();

        //usernames
    }

    public static void saveAllAccountUsernames(final Context context, final Set<String> value) {
        getSharedPreferences(context).edit()
                .putStringSet(allAccountUsernamesKey, value)
                .apply();
    }

    public static Set<String> getAllAccountUsernames(final Context context) {
        return getSharedPreferences(context).getStringSet(allAccountUsernamesKey, new ArraySet<>());
    }

    public static void saveActiveUsername(final Context context, final String username) {
        getSharedPreferences(context).edit()
                .putString(activeUsername, username)
                .apply();
    }

    public static String getActiveUsername(final Context context) {
        return getSharedPreferences(context).getString(activeUsername, "");
    }

    //endregion

    public static void saveIntroStatus(final Context context, final int value) {
        getSharedPreferences(context).edit()
                .putInt(introStatusKey, value)
                .apply();
    }

    public static int getIntroStatus(final Context context) {
        return getSharedPreferences(context).getInt(introStatusKey, -1);
    }

    public static void saveLogsString(final Context context, final String value) {
        getSharedPreferences(context).edit()
                .putString(logsStorageKey, value)
                .apply();
    }

    public static String getLogsString(final Context context) {
        return getSharedPreferences(context).getString(logsStorageKey, "");
    }

    public static void saveCrashStatus(final Context context, final boolean value) {
        getSharedPreferences(context).edit()
                .putBoolean(crashStatusKey, value)
                .apply();
    }

    public static boolean getCrashStatus(final Context context) {
        return getSharedPreferences(context).getBoolean(crashStatusKey, false);
    }

    public static void saveLastSentReportTime(final Context context, final long value) {
        getSharedPreferences(context).edit()
                .putLong(lastSentReportTimeKey, value)
                .apply();
    }

    public static long getLastSentReportTime(final Context context) {
        return getSharedPreferences(context).getLong(lastSentReportTimeKey, -1L);
    }

    public static void saveLastStartupDate(final Context context, final Calendar date) {
        getSharedPreferences(context).edit()
                .putString(lastStartupDateKey, date.get(Calendar.DAY_OF_YEAR) + "#" + date.get(Calendar.YEAR))
                .apply();
    }

    public static String getLastStartupDate(final Context context) {
        return getSharedPreferences(context).getString(lastStartupDateKey, "");
    }

    /*
      Questa funzione, facendo una richiesta HTTP, aumenta il numero di visite
      Piratepx tiene traccia delle visite per 30 giorni
      In questo momento stiamo contando le visite per:
      - Nuove installazioni
      - Login studente/genitore
      - Errori di webview
      - Crash
      - Aggiornamenti
      - Easter egg

      @param name Nome dell'oggetto su cui aumentare le visite
     */
    /*public static void increaseVisitCount(String name){
        if(BuildConfig.BUILD_TYPE.equals("debug")){
            //Ignora le build di debug
            return;
        }
        try {
            Jsoup.newSession().url("https://app.piratepx.com/ship?p=6af8462c-efe0-4f9a-96d0-ec9a8bbb9060&i=" + name)
                    .get();
        } catch (IOException ignored) { } //Non ci interessa se va in errore

    }*/
}

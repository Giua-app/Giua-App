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

/**
 * Controlla la compatibilità tra un aggiornamento e l'altro
 */
public class CompatibilityManager {
    //TODO: fare sta classe bene e magari cambiare nome
    /**
     * Controlla se la versione vecchia era 0.6.1 e nel caso cancella i log e imposta l'intro
     */
    public static void applyCompatibilityForUpdateFrom061(Context context) {
        LoggerManager lm = new LoggerManager("CompatManager", context);

        lm.w("Rilevato aggiornamento da 0.6.1");
        lm.d("Cancello log...");
        AppData.saveLogsString(context, "");
        lm.w("Ciao! Abbiamo notato che hai aggiornato versione dalla 0.6.1." +
                " I log non sono compatibili con questa versione quindi sono stati cancellati");
        lm.d("Imposto notifiche ai valori di default");
        SettingsData.saveSettingBoolean(context, SettingKey.NOTIFICATION, true);

        SettingsData.saveSettingBoolean(context, SettingKey.NEWSLETTERS_NOTIFICATION, true);
        SettingsData.saveSettingBoolean(context, SettingKey.ALERTS_NOTIFICATION, true);
        SettingsData.saveSettingBoolean(context, SettingKey.UPDATES_NOTIFICATION, true);
        SettingsData.saveSettingBoolean(context, SettingKey.VOTES_NOTIFICATION, true);
        SettingsData.saveSettingBoolean(context, SettingKey.HOMEWORKS_NOTIFICATION, true);
        SettingsData.saveSettingBoolean(context, SettingKey.TESTS_NOTIFICATION, true);

        AppData.saveIntroStatus(context, -2);
    }

    public static void applyCompatibilityForUpdateFrom062(Context context) {
        LoggerManager lm = new LoggerManager("CompatManager", context);
        final String oldVer = SettingsData.getSettingString(context, "appVersion");

        lm.w("Rilevato aggiornamento da 0.6.2");
        lm.d("Cancello impostazioni vecchie (versione & intro)");
        SettingsData.saveSettingString(context, "appVersion", null);
        SettingsData.saveSettingString(context, "introStatus", null);

        AppData.saveIntroStatus(context, -2);
        AppData.saveAppVersion(context, oldVer);
    }

    public static void applyCompatibilityForUpdateFrom063(Context context) {
        LoggerManager lm = new LoggerManager("CompatManager", context);

        lm.w("Rilevato aggiornamento da 0.6.3");
        lm.d("Sposto le credenziali da AccountData non sicuro a AccountData sicuro");

        SharedPreferences sharedPreferences = AccountData.getSharedPreferencesForOldLogin(context);
        String username = sharedPreferences.getString("user", "");
        String password = sharedPreferences.getString("password", "");

        AppData.saveActiveUsername(context, username);
        AccountData.setCredentials(context, username, password);

        lm.d("Elimino AccountData vecchio");
        sharedPreferences.edit().clear().apply();
    }

    public static void applyCompatibilityForUpdateFrom066(Context context) {
        LoggerManager lm = new LoggerManager("CompatManager", context);

        lm.w("Rilevato aggiornamento da 0.6.6");
        lm.d("Elimino i dati in AppData non più utilizzati");

        AppData.clearOfflineData(context);
        AppData.clearNotificationsData(context);
    }

    public static void applyCompatibilityForUpdateFrom070(Context context) {
        LoggerManager lm = new LoggerManager("CompatManager", context);

        lm.w("Rilevato aggiornamento da 0.7.0");
        lm.d("Imposto valore di default per funzionalità instabili");

        SettingsData.saveSettingInt(context, SettingKey.OPEN_UNSTABLE_FEAT_WITH_WEBVIEW, 1);
    }
}

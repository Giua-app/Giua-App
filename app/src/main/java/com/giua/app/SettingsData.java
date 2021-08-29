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

public class SettingsData {

    /**
     * Controlla la memorizzazione delle impostazioni
     */
    private static final String settingsPreferenceKey = "settings";

    private static SharedPreferences getSharedPreferences(final Context context) {
        return context.getSharedPreferences(settingsPreferenceKey, Context.MODE_PRIVATE);
    }

    /**
     * Salva un valore di tipo {@code string} associato ad una chiave
     */
    public static void saveSettingString(final Context context, final String key, final String value) {
        getSharedPreferences(context).edit()
                .putString(key, value)
                .apply();
    }

    /**
     * Salva un valore di tipo {@code int} associato ad una chiave
     */
    public static void saveSettingInt(final Context context, final String key, final int value) {
        getSharedPreferences(context).edit()
                .putInt(key, value)
                .apply();
    }

    /**
     * Salva un valore di tipo {@code float} associato ad una chiave
     */
    public static void saveSettingFloat(final Context context, final String key, final float value) {
        getSharedPreferences(context).edit()
                .putFloat(key, value)
                .apply();
    }

    public static String getSettingString(final Context context, final String key) {
        return getSharedPreferences(context).getString(key, null);
    }

    public static int getSettingInt(final Context context, final String key) {
        return getSharedPreferences(context).getInt(key, -1);
    }

    public static String getSettingFloat(final Context context, final String key) {
        return getSharedPreferences(context).getString(key, null);
    }

    public static void clearAll(final Context context) {
        getSharedPreferences(context).edit().clear().apply();
    }

}

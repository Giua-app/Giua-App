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

    public static String getSettingInt(final Context context, final String key) {
        return getSharedPreferences(context).getString(key, null);
    }

    public static String getSettingFloat(final Context context, final String key) {
        return getSharedPreferences(context).getString(key, null);
    }

    public static void clearAll(final Context context) {
        getSharedPreferences(context).edit().clear().apply();
    }

}

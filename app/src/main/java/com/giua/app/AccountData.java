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
import android.graphics.Color;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class AccountData {

    /**
     * Controlla la memorizzazione delle credenziali
     * Qui si trovano i dati riguardanti l'account attivo
     */

    private static final String loginPreferenceKeyOld = "login";    //DA NON UTILIZZARE
    //private static final String APIUrlKey = "APIUrl";
    private static final String passwordKey = "password";
    private static final String cookieKey = "cookie";
    private static final String siteUrlKey = "siteUrl";
    private static final String themeKey = "theme";
    private static final String emailKey = "email";
    private static final String userTypeKey = "userType";
    private static MasterKey masterKey = null;
    private static final String masterKeyAlias = "masterKeyAlias";

    private static void createMasterKeyValueForEncryption(Context context) throws GeneralSecurityException, IOException {
        if (masterKey == null) {
            new KeyGenParameterSpec.Builder(
                    masterKeyAlias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build();
            masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
        }
    }

    public static SharedPreferences getSharedPreferencesForOldLogin(final Context context) {
        return context.getSharedPreferences(loginPreferenceKeyOld, Context.MODE_PRIVATE);
    }

    private static SharedPreferences getSharedPreferences(final Context context, final String fileName) {
        try {
            createMasterKeyValueForEncryption(context);

            return EncryptedSharedPreferences.create(
                    context,
                    fileName,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            LoggerManager loggerManager = new LoggerManager("AccountData", context);
            loggerManager.e("Errore critico recuperando le EncryptedSharedPreferences. " + e + "\n" + e.getMessage());
            return null;
        }

    }

    public static void setCredentials(final Context context, final String username, final String password) {

        getSharedPreferences(context, username).edit()
                .putString(passwordKey, password)
                .apply();
    }

    public static void setCredentials(final Context context, final String username, final String password, final String cookie, final String userType, final String email) {

        getSharedPreferences(context, username).edit()
                .putString(passwordKey, password)
                .putString(cookieKey, cookie)
                .putString(userTypeKey, userType)
                .putString(emailKey, email)
                .apply();
    }

    public static String getPassword(final Context context, final String username) {
        return getSharedPreferences(context, username).getString(passwordKey, "");
    }

    public static String getCookie(final Context context, final String username) {
        return getSharedPreferences(context, username).getString(cookieKey, "");
    }

    public static String getUserType(final Context context, final String username) {
        return getSharedPreferences(context, username).getString(userTypeKey, "");
    }

    public static void setSiteUrl(final Context context, final String username, final String siteUrl) {
        getSharedPreferences(context, username).edit()
                .putString(siteUrlKey, siteUrl)
                .apply();
    }

    public static String getSiteUrl(final Context context, final String username) {
        return getSharedPreferences(context, username).getString(siteUrlKey, "");
    }

    public static void setTheme(final Context context, final String username, final int color) {
        getSharedPreferences(context, username).edit()
                .putInt(themeKey, color)
                .apply();
    }

    public static int getTheme(final Context context, final String username) {
        return getSharedPreferences(context, username).getInt(themeKey, Color.rgb(0, 123, 255));
    }

    public static void setEmail(final Context context, final String username, final String email) {
        getSharedPreferences(context, username).edit()
                .putString(themeKey, email)
                .apply();
    }

    public static String getEmail(final Context context, final String username) {
        return getSharedPreferences(context, username).getString(emailKey, "");
    }

    public static void removeAccount(final Context context, final String username) {
        getSharedPreferences(context, username).edit()
                .clear()
                .commit();
    }

}
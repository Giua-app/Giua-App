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
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class LoginData {

    /**
     * Controlla la memorizzazione delle credenziali
     * Qui si trovano i dati riguardanti l'account attivo
     */

    private static final String loginPreferenceKeyOld = "login";    //DA NON UTILIZZARE
    //private static final String APIUrlKey = "APIUrl";
    private static final String passwordKey = "password";
    private static final String cookieKey = "cookie";
    private static final String themeKey = "theme";
    private static String masterKeyAlias = null;

    private static void createMasterKeyValueForEncryption() throws GeneralSecurityException, IOException {
        if (masterKeyAlias == null) {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        }
    }

    public static SharedPreferences getSharedPreferencesForOldLogin(final Context context) {
        return context.getSharedPreferences(loginPreferenceKeyOld, Context.MODE_PRIVATE);
    }

    private static SharedPreferences getSharedPreferences(final Context context, final String fileName) {

        try {
            createMasterKeyValueForEncryption();

            return EncryptedSharedPreferences.create(
                    fileName,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    //add final String APIUrl to parameters
    public static void setCredentials(final Context context, final String username, final String password) {

        getSharedPreferences(context, username).edit()
                //.putString(APIUrlKey, APIUrl)
                .putString(passwordKey, password)
                .apply();
    }

    //add final String APIUrl to parameters
    public static void setCredentials(final Context context, final String username, final String password, String cookie) {

        getSharedPreferences(context, username).edit()
                //.putString(APIUrlKey, APIUrl)
                .putString(passwordKey, password)
                .putString(cookieKey, cookie)
                .apply();
    }

    //add final String APIUrl to parameters
    @SuppressLint("ApplySharedPref")
    public static void setCredentialsSynchronously(final Context context, final String username, final String password, String cookie) {

        getSharedPreferences(context, username).edit()
                //.putString(APIUrlKey, APIUrl)
                .putString(passwordKey, password)
                .putString(cookieKey, cookie)
                .commit();
    }

    public static String getPassword(final Context context, final String username) {
        return getSharedPreferences(context, username).getString(passwordKey, "");
    }

    public static String getCookie(final Context context, final String username) {
        return getSharedPreferences(context, username).getString(cookieKey, "");
    }

    public static void clearAllForAccount(final Context context, final String username) {
        setCredentials(context, username, "", "");
    }

    public static void setTheme(final Context context, final String username, final int color) {
        getSharedPreferences(context, username).edit()
                .putInt(themeKey, color)
                .commit();
    }

    public static int getTheme(final Context context, final String username) {
        return getSharedPreferences(context, username).getInt(themeKey, Color.rgb(0, 123, 255));
    }

}
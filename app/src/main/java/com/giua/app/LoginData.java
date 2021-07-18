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

public class LoginData {

    /**
     * Controlla la memorizzazione delle credenziali
     */

    private static final String loginPreferenceKey = "login";
    //private static final String APIUrlKey = "APIUrl";
    private static final String passwordKey = "password";
    private static final String userKey = "user";
    private static final String cookieKey = "cookie";

    private static SharedPreferences getSharedPreferences(final Context context) {
        return context.getSharedPreferences(loginPreferenceKey, Context.MODE_PRIVATE);
    }

    //add final String APIUrl to parameters
    public static void setCredentials(final Context context, final String user, final String password) {

        getSharedPreferences(context).edit()
                //.putString(APIUrlKey, APIUrl)
                .putString(userKey, user)
                .putString(passwordKey, password)
                .apply();
    }

    //add final String APIUrl to parameters
    public static void setCredentials(final Context context, final String user, final String password, String cookie) {

        getSharedPreferences(context).edit()
                //.putString(APIUrlKey, APIUrl)
                .putString(userKey, user)
                .putString(passwordKey, password)
                .putString(cookieKey, cookie)
                .apply();
    }

    public static String getUser(final Context context) {
        return getSharedPreferences(context).getString(userKey, "");
    }

    public static String getPassword(final Context context) {
        return getSharedPreferences(context).getString(passwordKey, "");
    }

    public static String getCookie(final Context context) {
        return getSharedPreferences(context).getString(cookieKey, "");
    }

    public static void clearAll(final Context context) {
        setCredentials(context, "", "", "");
    }

}

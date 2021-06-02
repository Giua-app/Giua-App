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

    private static SharedPreferences getSharedPreferences(final Context context) {
        return context.getSharedPreferences(loginPreferenceKey, Context.MODE_PRIVATE);
    }

    public static void setCredentials(final Context context,
                                      //final String APIUrl,
                                      final String user,
                                      final String password) {

        getSharedPreferences(context).edit()
                //.putString(APIUrlKey, APIUrl)
                .putString(userKey, user)
                .putString(passwordKey, password)
                .apply();
    }

    public static String getUser(final Context context) {
        return getSharedPreferences(context).getString(userKey, "");
    }

    public static String getPassword(final Context context) {
        return getSharedPreferences(context).getString(passwordKey, "");
    }

}

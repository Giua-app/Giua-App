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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.CookieManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class CheckNewsReceiver extends BroadcastReceiver {
    private Context context;
    private NotificationManagerCompat notificationManager;
    private GiuaScraper gS;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SettingsData.getSettingBoolean(context, SettingKey.NOTIFICATION)) {
            Log.d("", "Broadcast di background STARTATO");
            this.context = context;
            notificationManager = NotificationManagerCompat.from(context);
            checkNews();
        }
    }

    private void checkNews() {
        new Thread(() -> {
            Log.d("", "Servizio di background: controllo nuove cose");

            try {

                checkAndMakeLogin();

                checkNewsAndSendNotifications();

            } catch (Exception e) {
                //DEBUG
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                        .setSmallIcon(R.drawable.ic_giuaschool_logo1)
                        .setContentTitle("Si è verificato un errore")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(e.toString() + "; " + e.getMessage()))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                notificationManager.notify(10, builder.build());
            }
        }).start();
    }

    private void checkAndMakeLogin() {
        if (GlobalVariables.gS != null)     //Se un istanza di Giuascraper esiste già non ricrearla ed usa quella
            gS = GlobalVariables.gS;
        else if (!LoginData.getUser(context).equals("gsuite")) {  //Se l'account non è di gsuite fai il login normale
            gS = new GiuaScraper(LoginData.getUser(context), LoginData.getPassword(context), LoginData.getCookie(context), true);
            gS.login();
            LoginData.setCredentials(context, LoginData.getUser(context), LoginData.getPassword(context), gS.getCookie());
        } else {    //Se l'account è di gsuite fai il login con gsuite
            try {
                gS = new GiuaScraper(LoginData.getUser(context), LoginData.getPassword(context), LoginData.getCookie(context), true);
                gS.login();
            } catch (GiuaScraperExceptions.SessionCookieEmpty e) {
                gS = new GiuaScraper(LoginData.getUser(context), LoginData.getPassword(context), makeGsuiteLogin(), true);
                gS.login();
                LoginData.setCredentials(context, LoginData.getUser(context), LoginData.getPassword(context), gS.getCookie());
            }
        }
    }

    private void checkNewsAndSendNotifications() {
        int numberNewslettersOld = AppData.getNumberNewslettersInt(context);
        int numberAlertsOld = AppData.getNumberAlertsInt(context);
        int numberNewsletters = gS.checkForNewsletterUpdate();
        int numberAlerts = gS.checkForAlertsUpdate();
        AppData.saveNumberNewslettersInt(context, numberNewsletters);
        AppData.saveNumberAlertsInt(context, numberAlerts);

        if (numberNewslettersOld != -1 && numberNewsletters - numberNewslettersOld > 0) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                    .setSmallIcon(R.drawable.ic_giuaschool_logo1)
                    .setContentTitle("Nuova circolare")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("Numero circolari: " + numberNewsletters + "\nNumero circolari vecchie: " + numberNewslettersOld))  //DEBUG
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            notificationManager.notify(10, builder.build());
        }
        if (numberAlertsOld != -1 && numberAlerts - numberAlertsOld > 0) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                    .setSmallIcon(R.drawable.ic_giuaschool_logo1)
                    .setContentTitle("Nuovo avviso")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("Numero avvisi: " + numberAlerts + "\nNumero avvisi vecchie: " + numberAlertsOld))  //DEBUG
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            notificationManager.notify(11, builder.build());
        }
        if (gS.checkForAbsenceUpdate()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                    .setSmallIcon(R.drawable.ic_giuaschool_logo1)
                    .setContentTitle("Nuova assenza")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            notificationManager.notify(12, builder.build());
        }
    }

    private String makeGsuiteLogin() {
        Connection session = Jsoup.newSession()
                .followRedirects(true)
                .userAgent("Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36");

        String[] allCookiesRaw = CookieManager.getInstance().getCookie("https://accounts.google.com").split("; ");
        for (String cookie : allCookiesRaw) {
            session.cookie(cookie.split("=")[0], cookie.split("=")[1]);
        }

        try {
            session.newRequest().url("https://registro.giua.edu.it/login/gsuite").get();
            return session.cookieStore().get(new URI("https://registro.giua.edu.it")).toString().split("=")[1].replace("]", "");
        } catch (IOException | URISyntaxException ignored) {
            return "";
        }
    }

}

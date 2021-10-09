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

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
    LoggerManager loggerManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        loggerManager = new LoggerManager("CheckNewsReceiver", context);
        loggerManager.d("onReceive chiamato");
        if (SettingsData.getSettingBoolean(context, SettingKey.NOTIFICATION)) {
            loggerManager.d("Broadcast di background STARTATO");
            this.context = context;
            notificationManager = NotificationManagerCompat.from(context);
            checkNews();
        }
    }

    private void checkNews() {
        new Thread(() -> {
            loggerManager.d("Servizio di background: controllo nuove cose");

            try {

                checkAndMakeLogin();

                checkNewsAndSendNotifications();

            } catch (GiuaScraperExceptions.YourConnectionProblems | GiuaScraperExceptions.SiteConnectionProblems e) {
                loggerManager.e("Errore di connessione - " + e.getMessage());
                if (SettingsData.getSettingBoolean(context, SettingKey.DEBUG_MODE)) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                            .setSmallIcon(R.drawable.ic_giuaschool_black)
                            .setContentTitle("Si è verificato un errore di connessione")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    notificationManager.notify(12, builder.build());
                }
            } catch (Exception e) {
                loggerManager.e("Errore sconosciuto - " + e.getMessage());
                if (SettingsData.getSettingBoolean(context, SettingKey.DEBUG_MODE)) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                            .setSmallIcon(R.drawable.ic_giuaschool_black)
                            .setContentTitle("Si è verificato un errore")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(e.toString()))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    notificationManager.notify(12, builder.build());
                }
            }
        }).start();
    }

    private void checkAndMakeLogin() {
        //GiuaScraper.setSiteURL("http://hiemvault.ddns.net:9090"); //DEBUG
        if (GlobalVariables.gS != null) {    //Se un istanza di Giuascraper esiste già non ricrearla ed usa quella
            gS = GlobalVariables.gS;
            loggerManager.d("Riutilizzo istanza gS");
        }
        else if (!LoginData.getUser(context).equals("gsuite")) {  //Se l'account non è di gsuite fai il login normale
            loggerManager.d("Account non google rilevato, eseguo login");
            gS = new GiuaScraper(LoginData.getUser(context), LoginData.getPassword(context), LoginData.getCookie(context), true);
            gS.login();
            LoginData.setCredentials(context, LoginData.getUser(context), LoginData.getPassword(context), gS.getCookie());
        } else {    //Se l'account è di gsuite fai il login con gsuite
            try {
                loggerManager.d("Account google rilevato, provo ad entrare con cookie precedente");
                gS = new GiuaScraper(LoginData.getUser(context), LoginData.getPassword(context), LoginData.getCookie(context), true);
                gS.login();
            } catch (GiuaScraperExceptions.SessionCookieEmpty e) {
                loggerManager.d("Cookie precedente non valido");
                gS = new GiuaScraper(LoginData.getUser(context), LoginData.getPassword(context), makeGsuiteLogin(), true);
                gS.login();
                LoginData.setCredentials(context, LoginData.getUser(context), LoginData.getPassword(context), gS.getCookie());
            }
        }
    }

    private void checkNewsAndSendNotifications() {
        loggerManager.d("Controllo pagina home per le news");

        int numberNewslettersOld = -1;
        int numberNewsletters = -1;
        int numberAlertsOld = -1;
        int numberAlerts = -1;

        //Se la notifica non può essere mandata non faccio nemmeno controllare i check
        if (SettingsData.getSettingBoolean(context, SettingKey.NEWSLETTER_NOTIFICATION)) {
            numberNewslettersOld = AppData.getNumberNewslettersInt(context);
            numberNewsletters = gS.checkForNewsletterUpdate(false);
            AppData.saveNumberNewslettersInt(context, numberNewsletters);
        }
        if (SettingsData.getSettingBoolean(context, SettingKey.NEWSLETTER_NOTIFICATION)) {
            numberAlertsOld = AppData.getNumberAlertsInt(context);
            numberAlerts = gS.checkForAlertsUpdate(false);
            AppData.saveNumberAlertsInt(context, numberAlerts);
        }

        if (numberNewslettersOld != -1 && numberNewsletters - numberNewslettersOld > 0) {
            loggerManager.d("Trovata nuova circolare");
            Intent intent = new Intent(context, ActivityManager.class).putExtra("goTo", "Newsletters");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                    .setContentIntent(PendingIntent.getActivity(context, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                    .setSmallIcon(R.drawable.ic_giuaschool_black)
                    .setContentTitle(numberNewsletters - numberNewslettersOld == 1 ? "Nuova circolare" : numberNewsletters - numberNewslettersOld + " nuove circolari")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            notificationManager.notify(10, builder.build());
        }
        if (numberAlertsOld != -1 && numberAlerts - numberAlertsOld > 0) {
            loggerManager.d("Trovati nuovi avvisi");
            Intent intent = new Intent(context, ActivityManager.class).putExtra("goTo", "Alerts");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                    .setContentIntent(PendingIntent.getActivity(context, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                    .setSmallIcon(R.drawable.ic_giuaschool_black)
                    .setContentTitle(numberAlerts - numberAlertsOld == 1 ? "Nuovo avviso" : numberAlerts - numberAlertsOld + " nuovi avvisi")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            notificationManager.notify(11, builder.build());
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
            loggerManager.d("Eseguo login con cookie di google");
            session.newRequest().url("https://registro.giua.edu.it/login/gsuite").get();
            return session.cookieStore().get(new URI("https://registro.giua.edu.it")).toString().split("=")[1].replace("]", "");
        } catch (IOException | URISyntaxException e) {
            loggerManager.e("Errore, cookie google e cookie giua non più validi, impossibile continuare");
            return "";
        }
    }

}

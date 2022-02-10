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

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.webkit.CookieManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.giua.objects.Alert;
import com.giua.objects.Vote;
import com.giua.pages.AlertsPage;
import com.giua.utils.JsonBuilder;
import com.giua.utils.JsonParser;
import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

public class CheckNewsReceiver extends BroadcastReceiver {
    private Context context;
    private NotificationManagerCompat notificationManager;
    private GiuaScraper gS;
    LoggerManager loggerManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        loggerManager = new LoggerManager("CheckNewsReceiver", context);
        loggerManager.d("onReceive chiamato");
        if (!AppData.getActiveUsername(context).equals("") && SettingsData.getSettingBoolean(context, SettingKey.NOTIFICATION)) {
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
                AppData.saveNumberNotificationErrors(context, 0);

            } catch (Exception e) {
                loggerManager.e("Errore sconosciuto - " + e.getMessage());
                for (int i = 0; i < e.getStackTrace().length; i++)
                    loggerManager.e(e.getStackTrace()[i].toString());
                if (SettingsData.getSettingBoolean(context, SettingKey.DEBUG_MODE)) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                            .setSmallIcon(R.drawable.ic_giuaschool_black)
                            .setContentTitle("Si è verificato un errore")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(e.toString()))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    notificationManager.notify(12, builder.build());
                }
                if (e.getClass() == GiuaScraperExceptions.SessionCookieEmpty.class)
                    loggerManager.e("Username utilizzato: " + gS.getUser());
                int nErrors = AppData.getNumberNotificationErrors(context);
                if (nErrors < 3 && e.getClass() != GiuaScraperExceptions.SiteConnectionProblems.class) {
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Intent iCheckNewsReceiver = new Intent(context, CheckNewsReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, iCheckNewsReceiver, PendingIntent.FLAG_IMMUTABLE);
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME,
                            SystemClock.elapsedRealtime() + 900_000,   //Intervallo di 15 minuti
                            pendingIntent);
                    AppData.saveNumberNotificationErrors(context, ++nErrors);
                    return;
                }
            }

            //Risetta l'allarme con un nuovo intervallo random
            Random r = new Random(SystemClock.elapsedRealtime());
            long interval = AlarmManager.INTERVAL_HOUR + r.nextInt(3_600_000);
            Intent iCheckNewsReceiver = new Intent(context, CheckNewsReceiver.class);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, iCheckNewsReceiver, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + interval,   //Intervallo di 1 ora più numero random tra 0 e 60 minuti
                    pendingIntent);
            loggerManager.d("Risetto l'allarme con un nuovo intervallo random (" + (interval / 60_000) + " minuti)");
            //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+60000, 60000, pendingIntent);    //DEBUG
        }).start();
    }

    private void checkAndMakeLogin() {
        String username = AppData.getActiveUsername(context);
        String accountUrl = LoginData.getSiteUrl(context, username);
        String defaultUrl = SettingsData.getSettingString(context, SettingKey.DEFAULT_URL);
        if (accountUrl.equals("") && !defaultUrl.equals(""))
            GiuaScraper.setSiteURL(defaultUrl);
        else if(!accountUrl.equals(""))
            GiuaScraper.setSiteURL(accountUrl);
        loggerManager.d("Username letto: " + username);
        if (GlobalVariables.gS != null) {    //Se un istanza di Giuascraper esiste già non ricrearla ed usa quella
            gS = GlobalVariables.gS;
            loggerManager.d("Riutilizzo istanza gS");
        } else if (!username.equals("gsuite")) {  //Se l'account non è di gsuite fai il login normale
            loggerManager.d("Account non google rilevato, eseguo login");
            gS = new GiuaScraper(username, LoginData.getPassword(context, username), LoginData.getCookie(context, username), true, new LoggerManager("GiuaScraper", context));
            gS.login();
            LoginData.setCredentials(context, username, LoginData.getPassword(context, username), gS.getCookie());
        } else {    //Se l'account è di gsuite fai il login con gsuite
            try {
                loggerManager.d("Account google rilevato, provo ad entrare con cookie precedente");
                gS = new GiuaScraper(username, LoginData.getPassword(context, username), LoginData.getCookie(context, username), true, new LoggerManager("GiuaScraper", context));
                gS.login();
                LoginData.setCredentials(context, username, LoginData.getPassword(context, username), gS.getCookie());
            } catch (GiuaScraperExceptions.SessionCookieEmpty e) {
                loggerManager.d("Cookie precedente non valido");
                gS = new GiuaScraper(username, LoginData.getPassword(context, username), makeGsuiteLogin(), true, new LoggerManager("GiuaScraper", context));
                gS.login();
                LoginData.setCredentials(context, username, LoginData.getPassword(context, username), gS.getCookie());
            }
        }
    }

    private void checkNewsAndSendNotifications() throws IOException {
        loggerManager.d("Controllo pagina home per le news");

        int numberNewslettersOld = -1;
        int numberNewsletters = -1;
        int numberAlertsOld = -1;
        int numberAlerts = -1;
        int numberVotesOld = -1;
        int numberVotes = -1;
        int sumHomeworkTestsNotificated = 0;

        //Se la notifica non può essere mandata non faccio nemmeno controllare i check
        if (SettingsData.getSettingBoolean(context, SettingKey.NEWSLETTER_NOTIFICATION)) {
            numberNewslettersOld = AppData.getNumberNewslettersInt(context);
            numberNewsletters = gS.getHomePage(false).getNumberNewsletters();
            AppData.saveNumberNewslettersInt(context, numberNewsletters);
        }
        if (SettingsData.getSettingBoolean(context, SettingKey.ALERTS_NOTIFICATION)) {
            numberAlertsOld = AppData.getNumberAlertsInt(context);
            numberAlerts = gS.getHomePage(false).getNumberAlerts();
            AppData.saveNumberAlertsInt(context, numberAlerts);
        }
        if (SettingsData.getSettingBoolean(context, SettingKey.VOTES_NOTIFICATION)) {
            numberVotesOld = AppData.getNumberVotesInt(context);
            Map<String, List<Vote>> votes = gS.getVotesPage(false).getAllVotes();
            //Conto i voti
            numberVotes = 0;
            for (String subject : votes.keySet()) {
                numberVotes += votes.get(subject).size();
            }
            AppData.saveNumberVotesInt(context, numberVotes);
        }
        if (SettingsData.getSettingBoolean(context, SettingKey.HOMEWORKS_NOTIFICATION) || SettingsData.getSettingBoolean(context, SettingKey.TESTS_NOTIFICATION)) {
            sumHomeworkTestsNotificated = checkAndSendNotificationForAgenda();
        }

        if (numberNewslettersOld != -1 && numberNewsletters - numberNewslettersOld > 0) {
            loggerManager.d("Trovate nuove circolari: " + (numberNewsletters - numberNewslettersOld));
            Notification notification;
            if (numberNewsletters - numberNewslettersOld == 1)
                notification = createNotification("Nuova circolare", "Newsletters", 2);
            else
                notification = createNotification(numberNewsletters - numberNewslettersOld + " nuove circolari", "Newsletters", 2);

            notificationManager.notify(10, notification);
        }
        if (numberAlertsOld != -1 && numberAlerts - numberAlertsOld - sumHomeworkTestsNotificated > 0) {
            loggerManager.d("Trovati nuovi avvisi: " + (numberAlerts - numberAlertsOld));
            Notification notification;
            if (numberAlerts - numberAlertsOld - sumHomeworkTestsNotificated == 1)
                notification = createNotification("Nuovo avviso", "Alerts", 3);
            else
                notification = createNotification(numberAlerts - numberAlertsOld + " nuovi avvisi", "Alerts", 3);

            notificationManager.notify(11, notification);
        }
        if (numberVotesOld != -1 && numberVotes - numberVotesOld > 0) {
            loggerManager.d("Trovati nuovi voti: " + (numberVotes - numberVotesOld));
            Notification notification;
            if (numberVotes - numberVotesOld == 1)
                notification = createNotification("\u00c8 stato pubblicato un nuovo voto", "Votes", 4);
            else
                notification = createNotification("Sono stati pubblicati " + (numberVotes - numberVotesOld) + " nuovi voti", "Votes", 4);

            notificationManager.notify(12, notification);
        }
    }

    /**
     * Controlla e invia le notifiche riguardanti compiti e verifiche.
     *
     * @return La somma dei compiti e delle verifiche notificate. Serve a non far notificare anche gli avvisi.
     */
    private int checkAndSendNotificationForAgenda() throws IOException {
        loggerManager.d("Inizio a controllare gli avvisi per le notifiche di compiti e verifiche");
        com.giua.utils.JsonParser jsonParser = new JsonParser();
        boolean canSendHomeworkNotification = SettingsData.getSettingBoolean(context, SettingKey.HOMEWORKS_NOTIFICATION);
        boolean canSendTestNotification = SettingsData.getSettingBoolean(context, SettingKey.TESTS_NOTIFICATION);
        if (!canSendTestNotification && !canSendHomeworkNotification)
            return 0;    //Se non puo inviare nessuna notifica lo blocco
        loggerManager.d("Leggo il json per vedere gli avvisi dei compiti e delle verifiche già notificati");
        AlertsPage alertsPage = gS.getAlertsPage(false);
        File f = new File(context.getCacheDir() + "/alertsToNotify.json");
        if (!f.exists()) {
            createJsonNotificationFile(f, alertsPage);
            return 0; //Return perchè andrebbe a notificare tutti i vecchi compiti
        }
        BufferedReader file = new BufferedReader(new FileReader(context.getCacheDir() + "/alertsToNotify.json"));
        StringBuilder oldAlertsString = new StringBuilder();
        String read = file.readLine();
        if (read == null) {
            createJsonNotificationFile(f, alertsPage);
            return 0;
        }
        while (read != null) {
            oldAlertsString.append(read);
            read = file.readLine();
        }
        file.close();

        loggerManager.d("Faccio il parsing del json");
        //Lista con gli avvisi già notificati
        List<Alert> oldAlerts;
        if (oldAlertsString.toString().equals("")) {
            loggerManager.w("oldAlertsString è vuoto");
            oldAlerts = new Vector<>();
        } else
            oldAlerts = jsonParser.parseJsonForAlerts(oldAlertsString.toString());
        //Lista degli avvisi da notificare
        List<Alert> alertsToNotify = alertsPage.getAlertsToNotify(oldAlerts);
        //Salva gli avvisi (compresi i nuovi) nel json
        JsonBuilder jsonBuilder = new JsonBuilder(context.getCacheDir() + "/alertsToNotify.json", gS);
        jsonBuilder.writeAlerts(alertsPage.getAllAlertsWithFilters(false, "per la materia"));
        jsonBuilder.saveJson();

        loggerManager.d("Conto i compiti e le verifiche da notificare");
        int homeworkCounter = 0;    //Conta i compiti da notificare
        int testCounter = 0;    //Conta le verifiche da notificare
        List<String> homeworkDates = new Vector<>(40);  //Lista in cui ci sono tutte le date dei compiti da notificare
        List<String> testDates = new Vector<>(40);   //Lista in cui ci sono tutte le date delle verifiche da notificare
        List<String> homeworkSubjects = new Vector<>(40);    //Lista in cui ci sono tutte le materie dei compiti da notificare
        List<String> testSubjects = new Vector<>(40);    //Lista in cui ci sono tutte le materie delle verifiche da notificare
        for (Alert alert : alertsToNotify) {
            if (alert.object.startsWith("C")) {
                homeworkDates.add(alert.date);
                homeworkSubjects.add(alert.object.split(" per la materia ")[1]);
                homeworkCounter++;
            } else if (alert.object.startsWith("V")) {
                testDates.add(alert.date);
                testSubjects.add(alert.object.split(" per la materia ")[1]);
                testCounter++;
            }
        }
        loggerManager.d("Preparo le notifiche");
        StringBuilder homeworkNotificationText;
        StringBuilder testNotificationText;
        Notification homeworkNotification = null;
        Notification testNotification = null;

        if (canSendHomeworkNotification && homeworkCounter > 0) {
            String contentText;
            if (homeworkCounter == 1) {
                contentText = "Clicca per andare all' agenda";
                homeworkNotificationText = new StringBuilder("È stato programmato un nuovo compito di " + homeworkSubjects.get(0) + " per il giorno " + homeworkDates.get(0));
            } else {
                contentText = "Clicca per andare all' agenda";
                homeworkNotificationText = new StringBuilder("Sono stati programmati nuovi compiti:\n");
                for (int i = 0; i < homeworkCounter; i++) {
                    homeworkNotificationText.append(homeworkSubjects.get(i));
                    homeworkNotificationText.append(" - ");
                    homeworkNotificationText.append(homeworkDates.get(i));
                    if (i != homeworkCounter - 1)
                        homeworkNotificationText.append("\n");
                }
            }
            homeworkNotification = createNotificationForAgenda("Nuovi compiti", contentText, homeworkNotificationText.toString());
        }
        if (canSendTestNotification && testCounter > 0) {
            String contentText;
            if (testCounter == 1) {
                contentText = "Clicca per andare all' agenda";
                testNotificationText = new StringBuilder("È stata programmata una nuova verifica di " + testSubjects.get(0) + " per il giorno " + testDates.get(0));
            } else {
                contentText = "Clicca per andare all' agenda";
                testNotificationText = new StringBuilder("Sono state programmate nuove verifiche:\n");
                for (int i = 0; i < testCounter; i++) {
                    testNotificationText.append(testSubjects.get(i));
                    testNotificationText.append(" - ");
                    testNotificationText.append(testDates.get(i));
                    if (i != testCounter - 1)
                        testNotificationText.append("\n");
                }
            }
            testNotification = createNotificationForAgenda("Nuove verifiche", contentText, testNotificationText.toString());
        }

        loggerManager.d("Invio le notifiche");
        if (canSendHomeworkNotification && homeworkNotification != null)
            notificationManager.notify(13, homeworkNotification);
        if (canSendTestNotification && testNotification != null)
            notificationManager.notify(14, testNotification);

        return testCounter + homeworkCounter;
    }

    private void createJsonNotificationFile(File f, AlertsPage alertsPage) throws IOException {
        FileWriter fileWriter = new FileWriter(f);
        fileWriter.write("");
        fileWriter.close();
        JsonBuilder jsonBuilder = new JsonBuilder(context.getCacheDir() + "/alertsToNotify.json", gS);
        jsonBuilder.writeAlerts(alertsPage.getAllAlertsWithFilters(false, "per la materia"));
        jsonBuilder.saveJson();
    }

    private Notification createNotificationForAgenda(String title, String contentText, String bigText) {
        Intent intent = new Intent(context, ActivityManager.class).putExtra("goTo", "Agenda").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return new NotificationCompat.Builder(context, "0")
                .setContentIntent(PendingIntent.getActivity(context, 5, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE))
                .setSmallIcon(R.drawable.ic_giuaschool_black)
                .setContentTitle(title)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(bigText))
                .build();
    }

    private Notification createNotification(String title, String goTo, int requestCode) {
        Intent intent = new Intent(context, ActivityManager.class).putExtra("goTo", goTo).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return new NotificationCompat.Builder(context, "0")
                .setContentIntent(PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE))
                .setSmallIcon(R.drawable.ic_giuaschool_black)
                .setContentTitle(title)
                .setContentText("Clicca per avere più informazioni")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
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

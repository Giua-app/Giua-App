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

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.giua.webscraper.GiuaScraper;

public class CheckNewsReceiver extends BroadcastReceiver {
    private Context context;
    private NotificationManagerCompat notificationManager;
    private GiuaScraper gS;

    private void checkNews() {
        new Thread(() -> {
            Log.d("", "Servizio di background: controllo nuove cose");

            try {
                if (GlobalVariables.gS != null)
                    gS = GlobalVariables.gS;
                else {
                    //GiuaScraper.setSiteURL("http://hiemvault.ddns.net:9090");  //DEBUG
                    gS = new GiuaScraper(LoginData.getUser(context), LoginData.getPassword(context), LoginData.getCookie(context), true);
                    gS.login();
                    LoginData.setCredentials(context, LoginData.getUser(context), LoginData.getPassword(context), gS.getCookie());
                }
                int numberNewslettersOld = AppData.getNumberNewslettersInt(context);
                int numberAlertsOld = AppData.getNumberAlertsInt(context);
                int numberNewsletters = gS.checkForNewsletterUpdate();
                int numberAlerts = gS.checkForAlertsUpdate();
                AppData.saveNumberNewslettersInt(context, numberNewsletters);
                AppData.saveNumberAlertsInt(context, numberAlerts);

                if (numberNewslettersOld != -1 && numberNewsletters - numberNewslettersOld > 0) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                            .setSmallIcon(R.drawable.ic_baseline_calendar_today_24)
                            .setContentTitle("Nuova circolare")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    notificationManager.notify(10, builder.build());
                }
                if (numberAlertsOld != -1 && numberAlerts - numberAlertsOld > 0) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                            .setSmallIcon(R.drawable.ic_baseline_calendar_today_24)
                            .setContentTitle("Nuovo avviso")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    notificationManager.notify(11, builder.build());
                }
                if (gS.checkForAbsenceUpdate()) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                            .setSmallIcon(R.drawable.ic_baseline_calendar_today_24)
                            .setContentTitle("Nuova assenza")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    notificationManager.notify(12, builder.build());
                }
            } catch (Exception e) {
                //DEBUG
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                        .setSmallIcon(R.drawable.ic_baseline_calendar_today_24)
                        .setContentTitle("Cè stato un errore")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                notificationManager.notify(10, builder.build());
            }
        }).start();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("", "Broadcast di background STARTATO");
        this.context = context;
        notificationManager = NotificationManagerCompat.from(context);
        checkNews();
    }


}

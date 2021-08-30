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

import static com.giua.app.GlobalVariables.numberAlerts;
import static com.giua.app.GlobalVariables.numberNewsletters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class BackgroundReceiver extends BroadcastReceiver {
    private Context context;
    private NotificationManagerCompat notificationManager;

    private void checkNews() {
        new Thread(() -> {
            Log.d("", "Servizio di background: controllo nuove cose");
            int numberNewslettersOld = numberNewsletters;
            int numberAlertsOld = numberAlerts;
            numberNewsletters = GlobalVariables.gS.checkForNewsletterUpdate();
            numberAlerts = GlobalVariables.gS.checkForAlertsUpdate();

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
            if (GlobalVariables.gS.checkForAbsenceUpdate()) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                        .setSmallIcon(R.drawable.ic_baseline_calendar_today_24)
                        .setContentTitle("Nuova assenza")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                notificationManager.notify(12, builder.build());
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

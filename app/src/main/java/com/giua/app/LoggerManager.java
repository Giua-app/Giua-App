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

public class LoggerManager extends com.giua.utils.LoggerManager {
    Context context;

    @SuppressLint("SimpleDateFormat")

    public LoggerManager(String tag, Context context) {
        super(tag);
        this.context = context;
    }

    @Override
    protected void saveToData(Log log) {
        String old = AppData.getLogsString(context);
        AppData.saveLogsString(context, log.toString() + old);

        if (log.type.equals("DEBUG")) android.util.Log.d("GIUA-APP:" + log.tag, log.text);
        if (log.type.equals("WARNING")) android.util.Log.w("GIUA-APP:" + log.tag, log.text);
        if (log.type.equals("ERROR")) android.util.Log.e("GIUA-APP:" + log.tag, log.text);
    }

    public void cleanupLogs(){
        String old = AppData.getLogsString(context);
        parseLogsFrom(old);
        d("Pulizia di " + logs.size() + " log in corso... ");
        int total = logs.size();

        if(logs.size() > 6_000)
            logs = logs.subList(0, 6_000);

        d("Pulizia completata. Ho cancellato " + (total-logs.size()) + " logs! " +
                "Totale corrente: " + logs.size());

        String logsString = "";
        for (Log log : logs) { //conversione log per il salvataggio
            logsString += log.toString();
        }

        AppData.saveLogsString(context, logsString);
    }
}

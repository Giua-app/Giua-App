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

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class LoggerManager {
    String tag;
    List<Log> logs;
    Context c;
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat logDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public LoggerManager(String tag, Context c) {
        this.tag = tag;
        logs = new Vector<>();
        this.c = c;
    }

    public void d(String text){
        Date now = Calendar.getInstance().getTime();
        Log log = new Log(tag, "DEBUG", now, text);
        logs.add(log);
        saveToAppData(log);
    }

    public void w(String text){
        Date now = Calendar.getInstance().getTime();
        Log log = new Log(tag, "WARNING", now, text);
        logs.add(log);
        saveToAppData(log);
    }

    public void e(String text){
        Date now = Calendar.getInstance().getTime();
        Log log = new Log(tag, "ERROR", now, text);
        logs.add(log);
        saveToAppData(log);
    }

    public void saveToAppData(Log log){
        String old = AppData.getLogsString(c);
        AppData.saveLogsString(c, log.toString() + old);
    }

    public List<Log> getLogs(){
        return logs;
    }

    //$ - categoria
    //# - fine log
    // tag$tipo$date$text#
    public void parseLogsFrom(String logs){
        String[] logsOb = logs.split("#");


        for (String s : logsOb) {
            String[] logsSub = s.split("\\$");

            try {
                this.logs.add(new Log(logsSub[0], logsSub[1], logDateFormat.parse(logsSub[2]), logsSub[3]));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static class Log{
        public String tag;
        public String type;
        public Date date;
        public String text;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat logDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        public Log(String tag, String type, Date date, String text){
            this.tag = tag;
            this.type = type;
            this.date = date;
            this.text = text;
        }

        @NonNull
        @Override
        public String toString() {
            return this.tag + "$" + this.type + "$" + logDateFormat.format(this.date) + "$" + this.text + "#";
        }
    }
}

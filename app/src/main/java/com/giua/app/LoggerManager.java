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
    Context c;
    @SuppressLint("SimpleDateFormat")

    public LoggerManager(String tag, Context c) {
        super(tag);
        this.c = c;
    }

    @Override
    protected void saveToData(Log log) {
        String old = AppData.getLogsString(c);
        AppData.saveLogsString(c, log.toString() + old);
    }
}

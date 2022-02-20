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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.giua.objects.Alert;

import java.util.List;
import java.util.Vector;

/**
 * Controller per interagire con il database per la modalità offline
 */
public class DBController extends SQLiteOpenHelper {
    private static final String DB_NAME = "giuapp_offline_data";
    private static final int DB_VERSION = 1;

    private static final String ALERTS_TABLE = "alerts";
    private static final String NEWSLETTERS_TABLE = "newsletters";

    /**
     * Crea un istanza DbController. Se il database non esiste, ne crea uno nuovo
     * @param context
     */
    public DBController(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Crea tabella con nome alert con le colonne specificate
        String query = "CREATE TABLE " + ALERTS_TABLE + " ("
                + DBAlert.STATUS_COL + " TEXT, "
                + DBAlert.DATE_COL + " TEXT,"
                + DBAlert.RECEIVERS_COL + " TEXT,"
                + DBAlert.OBJECT_COL + " TEXT,"
                + DBAlert.PAGE_COL + " INTEGER,"
                + DBAlert.DETAILS_URL_COL + " TEXT,"
                + DBAlert.DETAILS_COL + " TEXT,"
                + DBAlert.CREATOR_COL + " TEXT,"
                + DBAlert.TYPE_COL + " TEXT,"
                + DBAlert.ATTACHMENT_URLS_COL + " TEXT,"
                + DBAlert.IS_DETAILED_COL + " BOOLEAN" + ")";

        db.execSQL(query);
    }

    private long addAlert(Alert alert, SQLiteDatabase db){
        ContentValues values = new ContentValues();

        values.put(DBAlert.STATUS_COL, alert.status);
        values.put(DBAlert.DATE_COL, alert.date);
        values.put(DBAlert.RECEIVERS_COL, alert.receivers);
        values.put(DBAlert.OBJECT_COL, alert.object);
        values.put(DBAlert.PAGE_COL, alert.page);
        values.put(DBAlert.DETAILS_URL_COL, alert.detailsUrl);
        values.put(DBAlert.DETAILS_COL, alert.details);
        values.put(DBAlert.CREATOR_COL, alert.creator);
        values.put(DBAlert.TYPE_COL, alert.type);

        //Non si può memorizzare una lista su sql
        String attachmentUrls = "";
        if(alert.attachmentUrls != null){
            for(String url : alert.attachmentUrls){
                attachmentUrls += url + ";";
            }
        }
        values.put(DBAlert.ATTACHMENT_URLS_COL, attachmentUrls);
        values.put(DBAlert.IS_DETAILED_COL, alert.isDetailed ? 1 : 0);

        return db.insert(ALERTS_TABLE, null, values);
    }

    public void addAlerts(List<Alert> alerts){
        SQLiteDatabase db = getWritableDatabase();

        for (Alert alert : alerts) {
            addAlert(alert, db);
        }

        db.close();
    }

    //ATTENZIONE NON TESTATO
    public List<Alert> readAlerts() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + ALERTS_TABLE, null);

        List<Alert> alerts = new Vector<>();
        List<String> tmp = new Vector<>();

        if (cursor.moveToFirst()) {
            do {
                alerts.add(new Alert(cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5),
                        tmp, //TODO: fare parsing per attachmentUrls
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getString(9)));
            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return alerts;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Se c'è stato un aggiornamento del database, crea uno nuovo
        db.execSQL("DROP TABLE IF EXISTS " + ALERTS_TABLE);
        onCreate(db);
    }

    //Identificativi delle colonne di Alert
    private static class DBAlert {
        private static final String STATUS_COL = "status";
        private static final String DATE_COL = "date";
        private static final String RECEIVERS_COL = "receivers";
        private static final String OBJECT_COL = "object";
        private static final String PAGE_COL = "page";
        private static final String DETAILS_URL_COL = "detailsUrl";
        private static final String DETAILS_COL = "details";
        private static final String CREATOR_COL = "creator";
        private static final String TYPE_COL = "type";
        private static final String ATTACHMENT_URLS_COL = "attachmentUrls";
        private static final String IS_DETAILED_COL = "isDetailed";
    }
}

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

import com.giua.objects.Absence;
import com.giua.objects.Activity;
import com.giua.objects.Alert;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Controller per interagire con il database per la modalità offline
 */
public class DBController extends SQLiteOpenHelper {

    //!!!
    //FIXME: USARE SQLiteDatabase.releaseMemory() DOVE L'APP VA IN BACKGROUND O ALTRO

    private static final String DB_NAME = "giuapp_offline_data";
    private static final int DB_VERSION = 2;

    private static final String ALERTS_TABLE = "alerts";
    private static final String ABSENCE_TABLE="absence";
    private static final String ACTIVITY_TABLE="activity";
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
                + DBAlert.IS_DETAILED_COL + " BOOLEAN,"
                + DBAlert.ALERT_ID + " INTEGER"+")";

        db.execSQL(query);

        //Crea tabella con nome absence con le colonne specificate
        String query2 = "CREATE TABLE " + ABSENCE_TABLE + " ("
                + DBAbsece.DATE_COL + " TEXT,"
                + DBAbsece.TYPE_COL + " TEXT,"
                + DBAbsece.NOTES_COL+" TEXT,"
                +DBAbsece.IS_JUSTIFIED_COL+" BOOLEAN,"
                +DBAbsece.IS_MODIFICABLE_COL+" BOOLEAN,"
                +DBAbsece.JUSTIFY_URL_COL+" TEXT"+")";
        db.execSQL(query2);

        //Crea tabella con nome activity con le colonne specificate
        String query3 = "CREATE TABLE " + ACTIVITY_TABLE + " ("
                + DBActivity.DATE_COL + " TEXT,"
                + DBActivity.CREATOR_COL + " TEXT,"
                + DBActivity.DETAILS_COL + " TEXT,"
                +DBActivity.EXISTS_COL+" BOOLEAN"+")";
        db.execSQL(query3);


    }



    //region DB Alert
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
        String attachmentUrls = null;
        if(alert.attachmentUrls != null){
            for(String url : alert.attachmentUrls){
                attachmentUrls += url + ";";
            }
        }
        values.put(DBAlert.ATTACHMENT_URLS_COL, attachmentUrls);
        values.put(DBAlert.IS_DETAILED_COL, alert.isDetailed ? 1 : 0); //false = 0, true = 1

        String[] a = alert.detailsUrl.split("/");
        int id = Integer.parseInt(a[a.length -1]);

        values.put(DBAlert.ALERT_ID, id);

        long b = db.insert(ALERTS_TABLE, null, values);

        return b;
    }

    public void addAlerts(List<Alert> alerts){
        SQLiteDatabase db = getWritableDatabase();

        for (Alert alert : alerts) {
            addAlert(alert, db);
        }

        db.close();
    }

    public List<Alert> readAlerts() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ALERTS_TABLE + " ORDER BY " + DBAlert.ALERT_ID + " DESC", null);

        List<Alert> alerts = new Vector<>();

        if (cursor.moveToFirst()) {
            do {
                boolean isDetailed = cursor.getInt(10) != 0; //0 = false, 1 = true

                if(isDetailed){
                    List<String> attachmentUrls = null;
                    try{
                        attachmentUrls = Arrays.asList(cursor.getString(6).split(";"));
                    } catch(NullPointerException ignored){ }

                    alerts.add(new Alert(cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getInt(5),
                            attachmentUrls,
                            cursor.getString(7),
                            cursor.getString(8),
                            cursor.getString(9)));

                } else {
                    alerts.add(new Alert(cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(5),
                            cursor.getInt(4)));
                }


            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return alerts;
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

        private static final String ALERT_ID = "id";
    }
    //endregion

    //region DB Absence
    private long addAbsence(Absence absence, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(DBAbsece.DATE_COL, absence.date);
        values.put(DBAbsece.TYPE_COL, absence.type);
        values.put(DBAbsece.NOTES_COL, absence.notes);
        values.put(DBAbsece.IS_JUSTIFIED_COL, absence.isJustified);
        values.put(DBAbsece.IS_JUSTIFIED_COL, absence.isModificable);
        values.put(DBAbsece.JUSTIFY_URL_COL, absence.justifyUrl);

        return db.insert(ABSENCE_TABLE, null, values);
    }

    public void addAbsences(List<Absence> absences){
        SQLiteDatabase db = getWritableDatabase();

        for (Absence absence : absences) {
            addAbsence(absence, db);
        }

        db.close();
    }

    public List<Absence> readAbsences() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + ABSENCE_TABLE, null);

        List<Absence> absences = new Vector<>();

        if (cursor.moveToFirst()) {
            boolean isJustify=true;
            if(cursor.getInt(3)==0) isJustify=false;
            boolean isModificable=true;
            if(cursor.getInt(4)==0) isModificable=false;
            do {
                absences.add(new Absence(cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        isJustify,
                        isModificable,
                        cursor.getString(5)));
            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return absences;
    }

    //Identificativi delle colonne di Absence
    private static class DBAbsece {
        private static final String DATE_COL = "date";
        private static final String TYPE_COL = "type";
        private static final String NOTES_COL = "notes";
        private static final String IS_JUSTIFIED_COL = "isJustified";
        private static final String IS_MODIFICABLE_COL = "isModificable";
        private static final String JUSTIFY_URL_COL = "justifyUrl";
    }
    //endregion

    //region DB Activity
    private long addActivity(Activity activity, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(DBActivity.DATE_COL, activity.date);
        values.put(DBActivity.CREATOR_COL, activity.creator);
        values.put(DBActivity.DETAILS_COL, activity.details);
        values.put(DBActivity.EXISTS_COL, activity.exists);

        return db.insert(ACTIVITY_TABLE, null, values);
    }

    public void addActivities(List<Activity> activities){
        SQLiteDatabase db = getWritableDatabase();

        for (Activity activity : activities) {
            addActivity(activity, db);
        }

        db.close();
    }

    public List<Activity> readActivity() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + ABSENCE_TABLE, null);

        List<Activity> activities = new Vector<>();

        if (cursor.moveToFirst()) {
            boolean exists=true;
            if(cursor.getInt(3)==0) exists=false;
            do {
                activities.add(new Activity(cursor.getString(0).split("-")[2],
                        cursor.getString(0).split("-")[1],
                        cursor.getString(0).split("-")[0],
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        exists));
            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return activities;
    }

    //identificativi delle colonne di Activity
    private static class DBActivity{
        private static final String DATE_COL="date";
        private static final String CREATOR_COL="creator";
        private static final String DETAILS_COL="details";
        private static final String EXISTS_COL="_exists";
    }
    //endregion

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Se c'è stato un aggiornamento del database, crea uno nuovo
        db.execSQL("DROP TABLE IF EXISTS " + ALERTS_TABLE);
        onCreate(db);
    }
}

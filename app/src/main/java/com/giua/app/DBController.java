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
import com.giua.objects.Authorization;
import com.giua.objects.DisciplinaryNotices;
import com.giua.objects.Homework;
import com.giua.objects.Lesson;
import com.giua.objects.Test;
import com.giua.objects.Vote;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Controller per interagire con il database per la modalità offline
 */
public class DBController extends SQLiteOpenHelper {

    private Context context;
    private LoggerManager lm;

    //!!!
    //FIXME: USARE SQLiteDatabase.releaseMemory() DOVE L'APP VA IN BACKGROUND O ALTRO

    private static final String DB_NAME = "giuapp_offline_data";
    private static final int DB_VERSION = 3;

    private static final String ALERTS_TABLE = "alerts";
    private static final String ABSENCES_TABLE ="absences";
    private static final String ACTIVITIES_TABLE ="activities";
    private static final String AUTHORIZATIONS_TABLE ="authorizations";
    private static final String DISCIPLINARY_NOTICES_TABLE="disciplinaryNotices";
    private static final String HOMEWORKS_TABLE="homeworks";
    private static final String TESTS_TABLE="tests";
    private static final String VOTES_TABLE ="votes";
    private static final String DOCUMENT_TABLE="document";
    private static final String LESSON_TABLE="document";
    private static final String NEWSLETTERS_TABLE = "newsletters";

    /**
     * Crea un istanza DbController. Se il database non esiste, ne crea uno nuovo
     * @param context
     */
    public DBController(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
        lm = new LoggerManager("DBController", this.context);
        lm.d("istanza");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        lm.d("onCreate");
        //region Crea tabella con nome alert con le colonne specificate
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
        //endregion

        //region Crea tabella con nome absence con le colonne specificate
        String query2 = "CREATE TABLE " + ABSENCES_TABLE + " ("
                + DBAbsence.DATE_COL.name + " TEXT,"
                + DBAbsence.TYPE_COL.name + " TEXT,"
                + DBAbsence.NOTES_COL.name+" TEXT,"
                + DBAbsence.IS_JUSTIFIED_COL.name+" BOOLEAN,"
                + DBAbsence.IS_MODIFICABLE_COL.name+" BOOLEAN,"
                + DBAbsence.JUSTIFY_URL_COL.name+" TEXT"+")";
        db.execSQL(query2);
        //endregion

        //region Crea tabella con nome activity con le colonne specificate
        String query3 = "CREATE TABLE " + ACTIVITIES_TABLE + " ("
                + DBActivity.DATE_COL.name + " TEXT,"
                + DBActivity.CREATOR_COL.name + " TEXT,"
                + DBActivity.DETAILS_COL.name + " TEXT,"
                +DBActivity.EXISTS_COL.name+" BOOLEAN"+")";
        db.execSQL(query3);
        //endregion

        //regionCrea tabella con nome authorization con le colonne specificate
        String query4="CREATE TABLE "+ AUTHORIZATIONS_TABLE +" ("
                + DBAuthorization.ENTRY_COL.name+" TEXT,"
                +DBAuthorization.EXIT_COL.name+" TEXT"+")";
        db.execSQL(query4);
        //endregion

        //region Crea tabella con nome disciplinaryNote con le colonne specificate
        String query5="CREATE TABLE "+DISCIPLINARY_NOTICES_TABLE+" ("
                +DBDisciplinaryNote.DATE_COL.name+" TEXT,"
                +DBDisciplinaryNote.TYPE_COL.name+" TEXT,"
                +DBDisciplinaryNote.DETAILS_COL.name+" TEXT,"
                +DBDisciplinaryNote.COUNTERMEASURES_COL.name+" TEXT,"
                +DBDisciplinaryNote.AUTHOR_OF_DETAILS_COL.name+" TEXT,"
                +DBDisciplinaryNote.AUTHOR_OF_COUNTERMEASURES_COL.name+" TEXT,"
                +DBDisciplinaryNote.QUARTERLY_COL.name+" TEXT"+")";
        db.execSQL(query5);
        //endregion

        //region Crea tabella con nome homework con le colonne specificate
        String query6 = "CREATE TABLE " + HOMEWORKS_TABLE + " ("
                + DBHomework.DATE_COL.name + " TEXT,"
                + DBHomework.SUBJECT_COL.name+" TEXT,"
                + DBHomework.CREATOR_COL.name + " TEXT,"
                + DBHomework.DETAILS_COL.name + " TEXT,"
                + DBHomework.EXISTS_COL.name+" BOOLEAN"+")";
        db.execSQL(query6);
        //endregion

        //region Crea tabella con nome test con le colonne specificate
        String query7 = "CREATE TABLE " + TESTS_TABLE + " ("
                + DBTest.DATE_COL.name + " TEXT,"
                + DBTest.SUBJECT_COL.name+" TEXT,"
                + DBTest.CREATOR_COL.name + " TEXT,"
                + DBTest.DETAILS_COL.name + " TEXT,"
                +DBTest.EXISTS_COL.name+" BOOLEAN"+")";
        db.execSQL(query7);
        //endregion

        //region Crea tabella con nome vote con le colonne specificate
        String query8 = "CREATE TABLE " + VOTES_TABLE + " ("
                + DBVote.VALUE_COL.name + " TEXT,"
                + DBVote.DATE_COL.name + " TEXT,"
                + DBVote.TEST_TYPE_COL.name + " TEXT,"
                + DBVote.ARGUMENTS_COL.name + " TEXT,"
                + DBVote.JUDGEMENT_COL.name + " TEXT,"
                + DBVote.QUARTERLY_COL.name + " TEXT,"
                + DBVote.IS_ASTERISK_COL.name + " BOOLEAN,"
                + DBVote.IS_RELEVANT_FOR_MEAN_COL.name + " BOOLEAN,"
                + DBVote.SUBJECT.name + " TEXT" + ")";
        db.execSQL(query8);
        //endregion

        /*region Crea tabella con nome document con le colonne specificate
        String query9 = "CREATE TABLE " + DOCUMENT_TABLE + " ("
                + DBDocument.STATUS_COL + " TEXT,"
                + DBDocument.CLASSROOM_COL + " TEXT,"
                + DBDocument.SUBJECT_COL + " TEXT,"
                + DBDocument.INSTITUTE_COL + " TEXT,"
                + DBDocument.DOWNLOAD_URL_COL + " TEXT"+")";
        db.execSQL(query9);
        //endregion */

        //region Crea tabella con nome lesson con le colonne specificate
        String query10 = "CREATE TABLE " + LESSON_TABLE + " ("
                + DBLesson.DATE_COL + " DATE,"
                + DBLesson.TIME_COL + " TEXT,"
                + DBLesson.SUBJECT_COL + " TEXT,"
                + DBLesson.ARGUMENTS_COL + " TEXT,"
                + DBLesson.ACTIVITIES_COL + " TEXT,"
                + DBLesson.EXISTS_COL + " BOOLEAN,"
                + DBLesson.IS_ERROR_COL + " BOOLEAN,"
                + DBLesson.SUPPORT_COL + " TEXT"+")";
        db.execSQL(query10);
        //endregion
    }



    //region DB Alert
    private long addAlert(Alert alert, SQLiteDatabase db){
        ContentValues values = new ContentValues();

        values.put(DBAlert.STATUS_COL.name, alert.status);
        values.put(DBAlert.DATE_COL.name, alert.date);
        values.put(DBAlert.RECEIVERS_COL.name, alert.receivers);
        values.put(DBAlert.OBJECT_COL.name, alert.object);
        values.put(DBAlert.PAGE_COL.name, alert.page);
        values.put(DBAlert.DETAILS_URL_COL.name, alert.detailsUrl);
        values.put(DBAlert.DETAILS_COL.name, alert.details);
        values.put(DBAlert.CREATOR_COL.name, alert.creator);
        values.put(DBAlert.TYPE_COL.name, alert.type);

        //Non si può memorizzare una lista su sql
        String attachmentUrls = null;
        if(alert.attachmentUrls != null){
            for(String url : alert.attachmentUrls){
                attachmentUrls += url + ";";
            }
        }
        values.put(DBAlert.ATTACHMENT_URLS_COL.name, attachmentUrls);
        values.put(DBAlert.IS_DETAILED_COL.name, alert.isDetailed ? 1 : 0); //false = 0, true = 1

        String[] a = alert.detailsUrl.split("/");
        int id = Integer.parseInt(a[a.length -1]);

        values.put(DBAlert.ALERT_ID.name, id);

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
                boolean isDetailed = cursor.getInt(DBAlert.IS_DETAILED_COL.ordinal()) != 0; //0 = false, 1 = true

                if(isDetailed){
                    List<String> attachmentUrls = null;
                    try{
                        attachmentUrls = Arrays.asList(cursor.getString(DBAlert.ATTACHMENT_URLS_COL.ordinal()).split(";"));
                    } catch(NullPointerException ignored){ }

                    alerts.add(new Alert(cursor.getString(DBAlert.STATUS_COL.ordinal()),
                            cursor.getString(DBAlert.DATE_COL.ordinal()),
                            cursor.getString(DBAlert.RECEIVERS_COL.ordinal()),
                            cursor.getString(DBAlert.OBJECT_COL.ordinal()),
                            cursor.getString(DBAlert.DETAILS_URL_COL.ordinal()),
                            cursor.getInt(DBAlert.PAGE_COL.ordinal()),
                            attachmentUrls,
                            cursor.getString(DBAlert.DETAILS_COL.ordinal()),
                            cursor.getString(DBAlert.CREATOR_COL.ordinal()),
                            cursor.getString(DBAlert.TYPE_COL.ordinal())));

                } else {
                    alerts.add(new Alert(cursor.getString(DBAlert.DATE_COL.ordinal()),
                            cursor.getString(DBAlert.DATE_COL.ordinal()),
                            cursor.getString(DBAlert.RECEIVERS_COL.ordinal()),
                            cursor.getString(DBAlert.OBJECT_COL.ordinal()),
                            cursor.getString(DBAlert.DETAILS_URL_COL.ordinal()),
                            cursor.getInt(DBAlert.PAGE_COL.ordinal())));
                }


            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return alerts;
    }

    private enum DBAlert{
        STATUS_COL("status"),
        DATE_COL("date"),
        RECEIVERS_COL("receivers"),
        OBJECT_COL("object"),
        PAGE_COL("page"),
        DETAILS_URL_COL("detailsUrl"),
        DETAILS_COL("details"),
        CREATOR_COL("creator"),
        TYPE_COL("type"),
        ATTACHMENT_URLS_COL("attachmentUrls"),
        IS_DETAILED_COL("isDetailed"),
        ALERT_ID("id");

        private final String name;

        DBAlert(String name) {
            this.name = name;
        }
    }


    //endregion

    //region DB Absence
    private long addAbsence(Absence absence, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(DBAbsence.DATE_COL.name, absence.date);
        values.put(DBAbsence.TYPE_COL.name, absence.type);
        values.put(DBAbsence.NOTES_COL.name, absence.notes);
        values.put(DBAbsence.IS_JUSTIFIED_COL.name, absence.isJustified);
        values.put(DBAbsence.IS_JUSTIFIED_COL.name, absence.isModificable);
        values.put(DBAbsence.JUSTIFY_URL_COL.name, absence.justifyUrl);

        return db.insert(ABSENCES_TABLE, null, values);
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

        Cursor cursor = db.rawQuery("SELECT * FROM " + ABSENCES_TABLE, null);

        List<Absence> absences = new Vector<>();

        if (cursor.moveToFirst()) {
            boolean isJustify= cursor.getInt(DBAbsence.IS_JUSTIFIED_COL.ordinal()) != 0;
            boolean isModificable= cursor.getInt(DBAbsence.IS_MODIFICABLE_COL.ordinal()) != 0;

            do {
                absences.add(new Absence(cursor.getString(DBAbsence.DATE_COL.ordinal()),
                        cursor.getString(DBAbsence.TYPE_COL.ordinal()),
                        cursor.getString(DBAbsence.NOTES_COL.ordinal()),
                        isJustify,
                        isModificable,
                        cursor.getString(DBAbsence.JUSTIFY_URL_COL.ordinal())));
            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return absences;
    }

    private enum DBAbsence{
        DATE_COL("date"),
        TYPE_COL("type"),
        NOTES_COL("notes"),
        IS_JUSTIFIED_COL("isJustified"),
        IS_MODIFICABLE_COL("isModificable"),
        JUSTIFY_URL_COL("justifyUrl");

        private final String name;

        DBAbsence(String name) {
            this.name = name;
        }
    }
    //endregion

    //region Agenda Objects

    //region DB Activity
    private long addActivity(Activity activity, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(DBActivity.DATE_COL.name, activity.date);
        values.put(DBActivity.CREATOR_COL.name, activity.creator);
        values.put(DBActivity.DETAILS_COL.name, activity.details);
        values.put(DBActivity.EXISTS_COL.name, activity._exists);

        return db.insert(ACTIVITIES_TABLE, null, values);
    }

    public void addActivities(List<Activity> activities){
        SQLiteDatabase db = getWritableDatabase();

        for (Activity activity : activities) {
            addActivity(activity, db);
        }

        db.close();
    }

    public List<Activity> readActivities() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + ACTIVITIES_TABLE, null);

        List<Activity> activities = new Vector<>();

        if (cursor.moveToFirst()) {
            boolean exists= cursor.getInt(DBActivity.EXISTS_COL.ordinal()) != 0;
            do {
                activities.add(new Activity(cursor.getString(DBActivity.DATE_COL.ordinal()).split("-")[2],
                        cursor.getString(DBActivity.DATE_COL.ordinal()).split("-")[1],
                        cursor.getString(DBActivity.DATE_COL.ordinal()).split("-")[0],
                        cursor.getString(DBActivity.DATE_COL.ordinal()),
                        cursor.getString(DBActivity.CREATOR_COL.ordinal()),
                        cursor.getString(DBActivity.DETAILS_COL.ordinal()),
                        exists));
            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return activities;
    }

    private enum DBActivity{
        DATE_COL("date"),
        CREATOR_COL("creator"),
        DETAILS_COL("details"),
        EXISTS_COL("_exists");

        private final String name;

        DBActivity(String name) {
            this.name = name;
        }
    }
    //endregion

    //region DBHomework

    private long addHomework(Homework homework, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(DBHomework.DATE_COL.name, homework.date);
        values.put(DBHomework.SUBJECT_COL.name, homework.subject);
        values.put(DBHomework.CREATOR_COL.name, homework.creator);
        values.put(DBHomework.DETAILS_COL.name, homework.details);
        values.put(DBHomework.EXISTS_COL.name, homework._exists);

        return db.insert(HOMEWORKS_TABLE, null, values);
    }

    public void addHomeworks(List<Homework> homeworks){
        SQLiteDatabase db = getWritableDatabase();

        for (Homework homework : homeworks) {
            addHomework(homework, db);
        }

        db.close();
    }

    public List<Homework> readHomeworks() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + HOMEWORKS_TABLE, null);

        List<Homework> homeworks = new Vector<>();

        if (cursor.moveToFirst()) {
            boolean exists= cursor.getInt(DBHomework.EXISTS_COL.ordinal()) != 0;
            do {
                homeworks.add(new Homework(cursor.getString(DBHomework.DATE_COL.ordinal()).split("-")[2],
                        cursor.getString(DBHomework.DATE_COL.ordinal()).split("-")[1],
                        cursor.getString(DBHomework.DATE_COL.ordinal()).split("-")[0],
                        cursor.getString(DBHomework.DATE_COL.ordinal()),
                        cursor.getString(DBHomework.SUBJECT_COL.ordinal()),
                        cursor.getString(DBHomework.CREATOR_COL.ordinal()),
                        cursor.getString(DBHomework.DETAILS_COL.ordinal()),
                        exists));
            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return homeworks;
    }

    private enum DBHomework{
        DATE_COL("date"),
        SUBJECT_COL("subject"),
        CREATOR_COL("creator"),
        DETAILS_COL("details"),
        EXISTS_COL("_exists");


        private final String name;

        DBHomework(String name) {
            this.name = name;
        }
    }

    //endregion

    //region DBTest

    private long addTest(Test test, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(DBTest.DATE_COL.name, test.date);
        values.put(DBTest.SUBJECT_COL.name, test.subject);
        values.put(DBTest.CREATOR_COL.name, test.creator);
        values.put(DBTest.DETAILS_COL.name, test.details);
        values.put(DBTest.EXISTS_COL.name, test._exists);

        return db.insert(TESTS_TABLE, null, values);
    }

    public void addTests(List<Test> tests){
        SQLiteDatabase db = getWritableDatabase();

        for (Test test : tests) {
            addTest(test, db);
        }

        db.close();
    }

    public List<Test> readTests() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TESTS_TABLE, null);

        List<Test> tests = new Vector<>();

        if (cursor.moveToFirst()) {
            boolean exists = cursor.getInt(DBTest.EXISTS_COL.ordinal()) != 0;
            do {
                tests.add(new Test(cursor.getString(DBTest.DATE_COL.ordinal()).split("-")[2],
                        cursor.getString(DBTest.DATE_COL.ordinal()).split("-")[1],
                        cursor.getString(DBTest.DATE_COL.ordinal()).split("-")[0],
                        cursor.getString(DBTest.DATE_COL.ordinal()),
                        cursor.getString(DBTest.SUBJECT_COL.ordinal()),
                        cursor.getString(DBTest.CREATOR_COL.ordinal()),
                        cursor.getString(DBTest.DETAILS_COL.ordinal()),
                        exists));
            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return tests;
    }

    private enum DBTest{
        DATE_COL("date"),
        SUBJECT_COL("subject"),
        CREATOR_COL("creator"),
        DETAILS_COL("details"),
        EXISTS_COL("_exists");


        private final String name;

        DBTest(String name) {
            this.name = name;
        }
    }
    //endregion

    //endregion

    //region DB Authorization
    private long addAuthorization(Authorization authorization, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(DBAuthorization.ENTRY_COL.name, authorization.entry);
        values.put(DBAuthorization.EXIT_COL.name, authorization.exit);

        return db.insert(AUTHORIZATIONS_TABLE, null, values);
    }

    public void addAuthorizations(List<Authorization> authorizations){
        SQLiteDatabase db = getWritableDatabase();

        for (Authorization authorization : authorizations) {
            addAuthorization(authorization, db);
        }

        db.close();
    }

    public List<Authorization> readAuthorization() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + AUTHORIZATIONS_TABLE, null);

        List<Authorization> authorizations = new Vector<>();

        if (cursor.moveToFirst()) {
            do {
                authorizations.add(new Authorization(
                        cursor.getString(DBAuthorization.ENTRY_COL.ordinal()),
                        cursor.getString(DBAuthorization.EXIT_COL.ordinal())));
            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return authorizations;
    }

    private enum DBAuthorization{
        ENTRY_COL("entry"),
        EXIT_COL("exit");

        private final String name;

        DBAuthorization(String name) {
            this.name = name;
        }
    }
    //endregion

    //region DBDisciplinaryNote
    private long addDisciplinaryNote(DisciplinaryNotices disciplinaryNote, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(DBDisciplinaryNote.DATE_COL.name, disciplinaryNote.date);
        values.put(DBDisciplinaryNote.TYPE_COL.name, disciplinaryNote.type);
        values.put(DBDisciplinaryNote.DETAILS_COL.name, disciplinaryNote.details);
        values.put(DBDisciplinaryNote.COUNTERMEASURES_COL.name, disciplinaryNote.countermeasures);
        values.put(DBDisciplinaryNote.AUTHOR_OF_DETAILS_COL.name, disciplinaryNote.authorOfDetails);
        values.put(DBDisciplinaryNote.AUTHOR_OF_COUNTERMEASURES_COL.name, disciplinaryNote.authorOfCountermeasures);
        values.put(DBDisciplinaryNote.QUARTERLY_COL.name, disciplinaryNote.quarterly);

        return db.insert(DISCIPLINARY_NOTICES_TABLE, null, values);
    }

    public void addDisciplinaryNotices(List<DisciplinaryNotices> disciplinaryNotices){
        SQLiteDatabase db = getWritableDatabase();

        for (DisciplinaryNotices disciplinaryNote : disciplinaryNotices) {
            addDisciplinaryNote(disciplinaryNote, db);
        }

        db.close();
    }

    public List<DisciplinaryNotices> readDisciplinaryNotices() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + DISCIPLINARY_NOTICES_TABLE, null);

        List<DisciplinaryNotices> disciplinaryNotices = new Vector<>();

        if (cursor.moveToFirst()) {
            do {
                disciplinaryNotices.add(new DisciplinaryNotices(
                        cursor.getString(DBDisciplinaryNote.DATE_COL.ordinal()),
                        cursor.getString(DBDisciplinaryNote.TYPE_COL.ordinal()),
                        cursor.getString(DBDisciplinaryNote.DETAILS_COL.ordinal()),
                        cursor.getString(DBDisciplinaryNote.COUNTERMEASURES_COL.ordinal()),
                        cursor.getString(DBDisciplinaryNote.AUTHOR_OF_DETAILS_COL.ordinal()),
                        cursor.getString(DBDisciplinaryNote.AUTHOR_OF_COUNTERMEASURES_COL.ordinal()),
                        cursor.getString(DBDisciplinaryNote.QUARTERLY_COL.ordinal())));
            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return disciplinaryNotices;
    }

    private enum DBDisciplinaryNote{
        DATE_COL("date"),
        TYPE_COL("type"),
        DETAILS_COL("details"),
        COUNTERMEASURES_COL("countermeasures"),
        AUTHOR_OF_DETAILS_COL("authorOfDetails"),
        AUTHOR_OF_COUNTERMEASURES_COL("authorOfCountermeasures"),
        QUARTERLY_COL("quarterly");

        private final String name;

        DBDisciplinaryNote(String name) {
            this.name = name;
        }
    }

    //endregion

    //region DBVote
    private void addSubject(String subject, List<Vote> votes, SQLiteDatabase db){
        for(Vote vote : votes) {
            ContentValues values = new ContentValues();

            values.put(DBVote.VALUE_COL.name, vote.value);
            values.put(DBVote.QUARTERLY_COL.name, vote.quarterly);
            values.put(DBVote.IS_ASTERISK_COL.name, vote.isAsterisk);
            values.put(DBVote.DATE_COL.name, vote.date);
            values.put(DBVote.JUDGEMENT_COL.name, vote.judgement);
            values.put(DBVote.TEST_TYPE_COL.name, vote.testType);
            values.put(DBVote.ARGUMENTS_COL.name, vote.arguments);
            values.put(DBVote.IS_RELEVANT_FOR_MEAN_COL.name, vote.isRelevantForMean);
            values.put(DBVote.SUBJECT.name, subject);

            db.insert(VOTES_TABLE, null, values);
        }
    }

    public void addVotes(Map<String, List<Vote>> votes){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + VOTES_TABLE + ";");

        for (String m : votes.keySet()) {
            addSubject(m, votes.get(m), db);
        }

        db.close();
    }

    public Map<String, List<Vote>> readVotes() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + VOTES_TABLE, null);

        Map<String, List<Vote>> votes = new HashMap<>();

        if (cursor.moveToFirst()) {
            do {
                boolean isAsterisk = cursor.getInt(DBVote.IS_ASTERISK_COL.ordinal()) != 0;
                boolean isRelevantForMean = cursor.getInt(DBVote.IS_RELEVANT_FOR_MEAN_COL.ordinal()) != 0;
                String subject = cursor.getString(DBVote.SUBJECT.ordinal());

                List<Vote> voteList = votes.get(subject);

                if(voteList == null){
                    voteList = new Vector<>();
                }

                voteList.add(new Vote(
                        cursor.getString(DBVote.VALUE_COL.ordinal()),
                        cursor.getString(DBVote.DATE_COL.ordinal()),
                        cursor.getString(DBVote.TEST_TYPE_COL.ordinal()),
                        cursor.getString(DBVote.ARGUMENTS_COL.ordinal()),
                        cursor.getString(DBVote.JUDGEMENT_COL.ordinal()),
                        cursor.getString(DBVote.QUARTERLY_COL.ordinal()),
                        isAsterisk,
                        isRelevantForMean));


                votes.put(subject, voteList);

            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return votes;
    }

    private enum DBVote{
        VALUE_COL("value"),
        DATE_COL("date"),
        TEST_TYPE_COL("testType"),
        ARGUMENTS_COL("arguments"),
        JUDGEMENT_COL("judgement"),
        QUARTERLY_COL("quarterly"),
        IS_ASTERISK_COL("isAsterisk"),
        IS_RELEVANT_FOR_MEAN_COL("isRelevantForMean"),
        SUBJECT("subject");

        private final String name;

        DBVote(String name) {
            this.name = name;
        }
    }
    //endregion

/* Inutile visto che non abbiamo una schermata per i Documents
    //region DBDocuments
    private long addDocument(Document document, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(DBDocument.STATUS_COL.name, document.status);
        values.put(DBDocument.CLASSROOM_COL.name, document.classroom);
        values.put(DBDocument.SUBJECT_COL.name, document.subject);
        values.put(DBDocument.INSTITUTE_COL.name, document.institute);
        values.put(DBDocument.DOWNLOAD_URL_COL.name, document.downloadUrl);

        return db.insert(DOCUMENT_TABLE, null, values);
    }

    public void addDocuments(List<Document> documents){
        SQLiteDatabase db = getWritableDatabase();

        for (Document document : documents) {
            addDocument(document, db);
        }

        db.close();
    }

    public List<Document> readDocuments() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + DOCUMENT_TABLE, null);

        List<Document> documents = new Vector<>();

        if (cursor.moveToFirst()) {
            do {
                documents.add(new Document(
                        cursor.getString(DBDocument.STATUS_COL.ordinal()),
                        cursor.getString(DBDocument.CLASSROOM_COL.ordinal()),
                        cursor.getString(DBDocument.SUBJECT_COL.ordinal()),
                        cursor.getString(DBDocument.INSTITUTE_COL.ordinal()),
                        cursor.getString(DBDocument.DOWNLOAD_URL_COL.ordinal())));
            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return documents;
    }

    private enum DBDocument{
        STATUS_COL("status"),
        CLASSROOM_COL("classroom"),
        SUBJECT_COL("subject"),
        INSTITUTE_COL("institute"),
        DOWNLOAD_URL_COL("downloadUrl");

        private final String name;

        DBDocument(String name) {
            this.name = name;
        }
    }
    //endregion */

    //region DBLesson
    private long addLesson(Lesson lesson, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(DBLesson.DATE_COL.name, lesson.date.toString());
        values.put(DBLesson.TIME_COL.name, lesson.time);
        values.put(DBLesson.SUBJECT_COL.name, lesson.subject);
        values.put(DBLesson.ARGUMENTS_COL.name, lesson.arguments);
        values.put(DBLesson.ACTIVITIES_COL.name, lesson.activities);
        values.put(DBLesson.EXISTS_COL.name, lesson._exists);
        values.put(DBLesson.IS_ERROR_COL.name, lesson.isError);
        values.put(DBLesson.SUPPORT_COL.name, lesson.support);

        return db.insert(LESSON_TABLE, null, values);
    }

    public void addLessons(List<Lesson> lessons){
        SQLiteDatabase db = getWritableDatabase();

        for (Lesson lesson : lessons) {
            addLesson(lesson, db);
        }

        db.close();
    }

    public List<Lesson> readLessons() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + LESSON_TABLE, null);

        List<Lesson> lessons = new Vector<>();

        if (cursor.moveToFirst()) {
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
            Date date=null;
            try{
                date = simpleDateFormat.parse(cursor.getString(DBLesson.DATE_COL.ordinal()));
            }catch(ParseException e){
                date = new Date(0);
            }

            boolean exists=cursor.getInt(DBLesson.EXISTS_COL.ordinal())==0;
            boolean isError=cursor.getInt(DBLesson.IS_ERROR_COL.ordinal())==0;
            do {
                lessons.add(new Lesson(
                        date,
                        cursor.getString(DBLesson.TIME_COL.ordinal()),
                        cursor.getString(DBLesson.SUBJECT_COL.ordinal()),
                        cursor.getString(DBLesson.ARGUMENTS_COL.ordinal()),
                        cursor.getString(DBLesson.ACTIVITIES_COL.ordinal()),
                        cursor.getString(DBLesson.SUPPORT_COL.ordinal()),
                        exists,
                        isError));
            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return lessons;
    }

    private enum DBLesson{
        DATE_COL("date"),
        TIME_COL("time"),
        SUBJECT_COL("subject"),
        ARGUMENTS_COL("arguments"),
        ACTIVITIES_COL("activities"),
        EXISTS_COL("_exists"),
        IS_ERROR_COL("isError"),
        SUPPORT_COL("support");

        private final String name;

        DBLesson(String name) {
            this.name = name;
        }
    }
    //endregion

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Se c'è stato un aggiornamento del database, crea uno nuovo
        db.execSQL("DROP TABLE IF EXISTS " + ALERTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ABSENCES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ACTIVITIES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + AUTHORIZATIONS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DISCIPLINARY_NOTICES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + HOMEWORKS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TESTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + VOTES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + NEWSLETTERS_TABLE);
        onCreate(db);
    }
}

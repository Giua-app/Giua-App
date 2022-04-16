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
import com.giua.objects.Homework;
import com.giua.objects.Newsletter;
import com.giua.objects.Test;
import com.giua.objects.Vote;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Controller per interagire con il database per la modalità offline
 */
public class NotificationsDBController extends SQLiteOpenHelper {

    private final Context context;
    private final LoggerManager lm;

    //!!!
    //FIXME: USARE SQLiteDatabase.releaseMemory() DOVE L'APP VA IN BACKGROUND O ALTRO

    private static final String DB_NAME = "giuapp_notification_data";
    private static final int DB_VERSION = 1;

    private static final String ALERTS_TABLE = "alerts";
    private static final String HOMEWORKS_TABLE="homeworks";
    private static final String TESTS_TABLE="tests";
    private static final String VOTES_TABLE ="votes";
    private static final String NEWSLETTERS_TABLE = "newsletters";

    /**
     * Crea un istanza DbController. Se il database non esiste, ne crea uno nuovo
     *
     * @param context
     */
    public NotificationsDBController(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
        lm = new LoggerManager("NotificationsDBController", this.context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        lm.d("Creazione database in corso...");
        AlertsTable.createTable(db);

        HomeworksTable.createTable(db);

        TestsTable.createTable(db);

        VotesTable.createTable(db);

        NewslettersTable.createTable(db);
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

    //region Agenda Objects

    //region DBHomework

    public void deleteHomeworks(){
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("DELETE FROM " + HOMEWORKS_TABLE + ";");

        db.close();
    }

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

    public void deleteTests(){
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("DELETE FROM " + TESTS_TABLE + ";");

        db.close();
    }

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

    //region DBVote
    public void deleteVotes(){
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("DELETE FROM " + VOTES_TABLE + ";");

        db.close();
    }

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
        //db.execSQL("DELETE FROM " + VOTES_TABLE + ";");

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

    //region DBNewsletter
    public void deleteNewsletters(){
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("DELETE FROM " + NEWSLETTERS_TABLE + ";");

        db.close();
    }

    private void addNewsletter(Newsletter newsletter, SQLiteDatabase db){
        ContentValues values = new ContentValues();

        values.put(DBNewsletter.STATUS_COL.name, newsletter.getStatus());
        values.put(DBNewsletter.NUMBER_COL.name, newsletter.date);
        values.put(DBNewsletter.DATE_COL.name, newsletter.object);
        values.put(DBNewsletter.OBJECT_COL.name, newsletter.detailsUrl);
        values.put(DBNewsletter.DETAILS_URL_COL.name, newsletter.number);

        //Non si può memorizzare una lista su sql
        String attachmentUrls = null;
        if(newsletter.attachmentsUrl != null){
            for(String url : newsletter.attachmentsUrl){
                attachmentUrls += url + ";";
            }
        }
        values.put(DBNewsletter.ATTACHMENTS_URL_COL.name, attachmentUrls);

        values.put(DBNewsletter.PAGE_COL.name, newsletter.page);

        db.insert(VOTES_TABLE, null, values);
    }

    public void addNewsletters(List<Newsletter> newsletters){
        SQLiteDatabase db = getWritableDatabase();

        for (Newsletter n : newsletters) {
            addNewsletter(n, db);
        }

        db.close();
    }

    public List<Newsletter> readNewsletters() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + NEWSLETTERS_TABLE, null);

        List<Newsletter> newsletters = new Vector<>();

        if (cursor.moveToFirst()) {
            do {
                List<String> attachmentUrls = null;
                try{
                    attachmentUrls = Arrays.asList(cursor.getString(DBNewsletter.ATTACHMENTS_URL_COL.ordinal()).split(";"));
                } catch(NullPointerException ignored){ }

                newsletters.add(new Newsletter(cursor.getString(DBNewsletter.STATUS_COL.ordinal()),
                        cursor.getInt(DBNewsletter.NUMBER_COL.ordinal()),
                        cursor.getString(DBNewsletter.DATE_COL.ordinal()),
                        cursor.getString(DBNewsletter.OBJECT_COL.ordinal()),
                        cursor.getString(DBNewsletter.DETAILS_URL_COL.ordinal()),
                        attachmentUrls,
                        cursor.getInt(DBNewsletter.PAGE_COL.ordinal())));



            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return newsletters;
    }

    private enum DBNewsletter{
        STATUS_COL("status"),
        NUMBER_COL("number"),
        DATE_COL("date"),
        OBJECT_COL("object"),
        DETAILS_URL_COL("detailsUrl"),
        ATTACHMENTS_URL_COL("attachmentsUrl"),
        PAGE_COL("page");

        private final String name;

        DBNewsletter(String name) {
            this.name = name;
        }
    }
    //endregion


    public static class AlertsTable {
        public static void createTable(SQLiteDatabase db) {
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
        }
    }

    public static class HomeworksTable {
        public static void createTable(SQLiteDatabase db) {
            String query6 = "CREATE TABLE " + HOMEWORKS_TABLE + " ("
                    + DBHomework.DATE_COL.name + " TEXT,"
                    + DBHomework.SUBJECT_COL.name+" TEXT,"
                    + DBHomework.CREATOR_COL.name + " TEXT,"
                    + DBHomework.DETAILS_COL.name + " TEXT,"
                    + DBHomework.EXISTS_COL.name+" BOOLEAN"+")";
            db.execSQL(query6);
        }
    }

    public static class TestsTable {
        public static void createTable(SQLiteDatabase db) {
            String query7 = "CREATE TABLE " + TESTS_TABLE + " ("
                    + DBTest.DATE_COL.name + " TEXT,"
                    + DBTest.SUBJECT_COL.name+" TEXT,"
                    + DBTest.CREATOR_COL.name + " TEXT,"
                    + DBTest.DETAILS_COL.name + " TEXT,"
                    +DBTest.EXISTS_COL.name+" BOOLEAN"+")";
            db.execSQL(query7);
        }
    }

    public static class VotesTable {
        public static void createTable(SQLiteDatabase db) {
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
        }
    }

    public static class NewslettersTable {
        public static void createTable(SQLiteDatabase db) {
            String query = "CREATE TABLE " + NEWSLETTERS_TABLE + " ("
                    + DBNewsletter.STATUS_COL.name + " TEXT,"
                    + DBNewsletter.NUMBER_COL.name + " INTEGER,"
                    + DBNewsletter.DATE_COL.name + " DATE,"
                    + DBNewsletter.OBJECT_COL.name + " TEXT,"
                    + DBNewsletter.DETAILS_URL_COL.name + " TEXT,"
                    + DBNewsletter.ATTACHMENTS_URL_COL.name + " BOOLEAN,"
                    + DBNewsletter.PAGE_COL.name + " INTEGER"+")";
            db.execSQL(query);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        lm.w("Aggiornamento database rilevato (" + oldVersion + " -> " + newVersion + "). " +
                "Impossibile convertire database, cancello e ne ricreo uno nuovo");
        db.execSQL("DROP TABLE IF EXISTS " + ALERTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + HOMEWORKS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TESTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + VOTES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + NEWSLETTERS_TABLE);
        onCreate(db);
    }
}

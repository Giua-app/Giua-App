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
import com.giua.objects.Newsletter;
import com.giua.objects.Vote;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Controller per interagire con il database per la modalità offline
 */
public class NotificationsDBController extends SQLiteOpenHelper {

    private final LoggerManager lm;
    private final SQLiteDatabase db;

    //!!!
    //ATTENZIONE: USARE SQLiteDatabase.releaseMemory() DOVE L'APP VA IN BACKGROUND O ALTRO

    private static final String DB_NAME = "giuapp_notification_data";
    private static final int DB_VERSION = 1;


    private static final String ALERTS_HOMEWORKS_TABLE = "alerts_homeworks";
    private static final String ALERTS_TESTS_TABLE = "alerts_tests";
    private static final String ALERTS_TABLE = "alerts";
    private static final String VOTES_TABLE = "votes";
    private static final String NEWSLETTERS_TABLE = "newsletters";

    /**
     * Crea un istanza DbController. Se il database non esiste, ne crea uno nuovo
     *
     * @param context
     */
    public NotificationsDBController(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        lm = new LoggerManager("NotificationsDBController", context);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        lm.d("Creazione database in corso...");

        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        lm.w("Upgrade database rilevato (" + oldVersion + " -> " + newVersion + "). " +
                "Impossibile convertire database, cancello e ne ricreo uno nuovo");

        deleteTables(db);
        createTables(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        lm.w("Downgrade database rilevato (" + oldVersion + " -> " + newVersion + "). " +
                "Impossibile convertire database, cancello e ne ricreo uno nuovo");

        deleteTables(db);
        createTables(db);
    }

    private void deleteTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + ALERTS_TESTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ALERTS_HOMEWORKS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + VOTES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + NEWSLETTERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ALERTS_TABLE);
    }

    private void createTables(SQLiteDatabase db) {
        AlertsTestsTable.createTable(db);
        AlertsHomeworksTable.createTable(db);

        VotesTable.createTable(db);
        HashMap<String, List<Vote>> noVotes = new HashMap<>();
        noVotes.put("N", Collections.emptyList());
        addVotes(noVotes);

        NewslettersTable.createTable(db);
        addNewsletters(Arrays.asList(new Newsletter("",-1,"","","",Collections.emptyList(),-1)));

        AlertsTable.createTable(db);
        addAlerts(Arrays.asList(new Alert("LETTO", "", "","","",-1)));
    }


    //region DBAlerts

    public void addAlerts(List<Alert> alerts) {
        for (Alert alert : alerts) {
            addAlert(alert);
        }
    }

    private long addAlert(Alert alert) {
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
        if (alert.attachmentUrls != null) {
            for (String url : alert.attachmentUrls) {
                attachmentUrls += url + ";";
            }
        }
        values.put(DBAlert.ATTACHMENT_URLS_COL.name, attachmentUrls);
        values.put(DBAlert.IS_DETAILED_COL.name, alert.isDetailed ? 1 : 0); //false = 0, true = 1

        return db.insertOrThrow(ALERTS_TABLE, null, values);
    }

    public void replaceAlerts(List<Alert> alerts) {
        deleteAlerts();
        addAlerts(alerts);
    }

    public void deleteAlerts() {
        db.execSQL("DELETE FROM " + ALERTS_TABLE + ";");
    }

    public List<Alert> readAlerts() {
        Cursor cursor = db.rawQuery("SELECT * FROM " + ALERTS_TABLE, null);

        List<Alert> alerts = new Vector<>();

        if (cursor.moveToFirst()) {
            do {
                boolean isDetailed = cursor.getInt(DBAlert.IS_DETAILED_COL.ordinal()) != 0; //0 = false, 1 = true

                if (isDetailed) {
                    List<String> attachmentUrls = null;
                    try {
                        attachmentUrls = Arrays.asList(cursor.getString(DBAlert.ATTACHMENT_URLS_COL.ordinal()).split(";"));
                    } catch (NullPointerException ignored) {
                    }

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

    //endregion

    //region DBAlertsHomeworks

    public void addAlertsHomeworks(List<Alert> alerts) {
        for (Alert alert : alerts) {
            addAlertHomework(alert);
        }
    }

    private long addAlertHomework(Alert alert) {
        ContentValues values = new ContentValues();

        values.put(DBAlertHomeworks.STATUS_COL.name, alert.status);
        values.put(DBAlertHomeworks.DATE_COL.name, alert.date);
        values.put(DBAlertHomeworks.RECEIVERS_COL.name, alert.receivers);
        values.put(DBAlertHomeworks.OBJECT_COL.name, alert.object);
        values.put(DBAlertHomeworks.PAGE_COL.name, alert.page);
        values.put(DBAlertHomeworks.DETAILS_URL_COL.name, alert.detailsUrl);
        values.put(DBAlertHomeworks.DETAILS_COL.name, alert.details);
        values.put(DBAlertHomeworks.CREATOR_COL.name, alert.creator);
        values.put(DBAlertHomeworks.TYPE_COL.name, alert.type);

        //Non si può memorizzare una lista su sql
        String attachmentUrls = null;
        if (alert.attachmentUrls != null) {
            for (String url : alert.attachmentUrls) {
                attachmentUrls += url + ";";
            }
        }
        values.put(DBAlertHomeworks.ATTACHMENT_URLS_COL.name, attachmentUrls);
        values.put(DBAlertHomeworks.IS_DETAILED_COL.name, alert.isDetailed ? 1 : 0); //false = 0, true = 1

        return db.insertOrThrow(ALERTS_HOMEWORKS_TABLE, null, values);
    }

    public void replaceAlertsHomeworks(List<Alert> alerts) {
        deleteAlertsHomeworks();
        addAlertsHomeworks(alerts);
    }

    public void deleteAlertsHomeworks() {
        db.execSQL("DELETE FROM " + ALERTS_HOMEWORKS_TABLE + ";");
    }

    public List<Alert> readAlertsHomeworks() {
        Cursor cursor = db.rawQuery("SELECT * FROM " + ALERTS_HOMEWORKS_TABLE, null);

        List<Alert> alerts = new Vector<>();

        if (cursor.moveToFirst()) {
            do {
                boolean isDetailed = cursor.getInt(DBAlertHomeworks.IS_DETAILED_COL.ordinal()) != 0; //0 = false, 1 = true

                if (isDetailed) {
                    List<String> attachmentUrls = null;
                    try {
                        attachmentUrls = Arrays.asList(cursor.getString(DBAlertHomeworks.ATTACHMENT_URLS_COL.ordinal()).split(";"));
                    } catch (NullPointerException ignored) {
                    }

                    alerts.add(new Alert(cursor.getString(DBAlertHomeworks.STATUS_COL.ordinal()),
                            cursor.getString(DBAlertHomeworks.DATE_COL.ordinal()),
                            cursor.getString(DBAlertHomeworks.RECEIVERS_COL.ordinal()),
                            cursor.getString(DBAlertHomeworks.OBJECT_COL.ordinal()),
                            cursor.getString(DBAlertHomeworks.DETAILS_URL_COL.ordinal()),
                            cursor.getInt(DBAlertHomeworks.PAGE_COL.ordinal()),
                            attachmentUrls,
                            cursor.getString(DBAlertHomeworks.DETAILS_COL.ordinal()),
                            cursor.getString(DBAlertHomeworks.CREATOR_COL.ordinal()),
                            cursor.getString(DBAlertHomeworks.TYPE_COL.ordinal())));

                } else {
                    alerts.add(new Alert(cursor.getString(DBAlertHomeworks.DATE_COL.ordinal()),
                            cursor.getString(DBAlertHomeworks.DATE_COL.ordinal()),
                            cursor.getString(DBAlertHomeworks.RECEIVERS_COL.ordinal()),
                            cursor.getString(DBAlertHomeworks.OBJECT_COL.ordinal()),
                            cursor.getString(DBAlertHomeworks.DETAILS_URL_COL.ordinal()),
                            cursor.getInt(DBAlertHomeworks.PAGE_COL.ordinal())));
                }


            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return alerts;
    }

    //endregion

    //region DBAlertsTests

    public void addAlertsTests(List<Alert> alerts) {
        for (Alert alert : alerts) {
            addAlertTest(alert);
        }
    }

    private long addAlertTest(Alert alert) {
        ContentValues values = new ContentValues();

        values.put(DBAlertTests.STATUS_COL.name, alert.status);
        values.put(DBAlertTests.DATE_COL.name, alert.date);
        values.put(DBAlertTests.RECEIVERS_COL.name, alert.receivers);
        values.put(DBAlertTests.OBJECT_COL.name, alert.object);
        values.put(DBAlertTests.PAGE_COL.name, alert.page);
        values.put(DBAlertTests.DETAILS_URL_COL.name, alert.detailsUrl);
        values.put(DBAlertTests.DETAILS_COL.name, alert.details);
        values.put(DBAlertTests.CREATOR_COL.name, alert.creator);
        values.put(DBAlertTests.TYPE_COL.name, alert.type);

        //Non si può memorizzare una lista su sql
        String attachmentUrls = null;
        if (alert.attachmentUrls != null) {
            for (String url : alert.attachmentUrls) {
                attachmentUrls += url + ";";
            }
        }
        values.put(DBAlertTests.ATTACHMENT_URLS_COL.name, attachmentUrls);
        values.put(DBAlertTests.IS_DETAILED_COL.name, alert.isDetailed ? 1 : 0); //false = 0, true = 1

        return db.insertOrThrow(ALERTS_TESTS_TABLE, null, values);
    }

    public void replaceAlertsTests(List<Alert> alerts) {
        deleteAlertsTests();
        addAlertsTests(alerts);
    }

    public void deleteAlertsTests() {
        db.execSQL("DELETE FROM " + ALERTS_TESTS_TABLE + ";");
    }

    public List<Alert> readAlertsTests() {
        Cursor cursor = db.rawQuery("SELECT * FROM " + ALERTS_TESTS_TABLE, null);

        List<Alert> alerts = new Vector<>();

        if (cursor.moveToFirst()) {
            do {
                boolean isDetailed = cursor.getInt(DBAlertTests.IS_DETAILED_COL.ordinal()) != 0; //0 = false, 1 = true

                if (isDetailed) {
                    List<String> attachmentUrls = null;
                    try {
                        attachmentUrls = Arrays.asList(cursor.getString(DBAlertTests.ATTACHMENT_URLS_COL.ordinal()).split(";"));
                    } catch (NullPointerException ignored) {
                    }

                    alerts.add(new Alert(cursor.getString(DBAlertTests.STATUS_COL.ordinal()),
                            cursor.getString(DBAlertTests.DATE_COL.ordinal()),
                            cursor.getString(DBAlertTests.RECEIVERS_COL.ordinal()),
                            cursor.getString(DBAlertTests.OBJECT_COL.ordinal()),
                            cursor.getString(DBAlertTests.DETAILS_URL_COL.ordinal()),
                            cursor.getInt(DBAlertTests.PAGE_COL.ordinal()),
                            attachmentUrls,
                            cursor.getString(DBAlertTests.DETAILS_COL.ordinal()),
                            cursor.getString(DBAlertTests.CREATOR_COL.ordinal()),
                            cursor.getString(DBAlertTests.TYPE_COL.ordinal())));

                } else {
                    alerts.add(new Alert(cursor.getString(DBAlertTests.DATE_COL.ordinal()),
                            cursor.getString(DBAlertTests.DATE_COL.ordinal()),
                            cursor.getString(DBAlertTests.RECEIVERS_COL.ordinal()),
                            cursor.getString(DBAlertTests.OBJECT_COL.ordinal()),
                            cursor.getString(DBAlertTests.DETAILS_URL_COL.ordinal()),
                            cursor.getInt(DBAlertTests.PAGE_COL.ordinal())));
                }


            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return alerts;
    }
    //endregion

    //region DBVotes

    public void addVotes(Map<String, List<Vote>> votes) {
        for (String m : votes.keySet()) {
            addSubject(m, votes.get(m));
        }
    }

    public void replaceVotes(Map<String, List<Vote>> votes) {
        deleteVotes();
        addVotes(votes);
    }

    public void deleteVotes() {
        db.execSQL("DELETE FROM " + VOTES_TABLE + ";");
    }

    public Map<String, List<Vote>> readVotes() {

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
                        cursor.getInt(DBVote.QUARTERLY_COL.ordinal()),
                        isAsterisk,
                        isRelevantForMean));


                votes.put(subject, voteList);

            } while (cursor.moveToNext());
            //muovi il cursore nella prossima riga
        }
        cursor.close();
        return votes;
    }

    //endregion

    //region DBNewsletters

    public void addNewsletters(List<Newsletter> newsletters) {
        for (Newsletter n : newsletters) {
            addNewsletter(n);
        }
    }

    private long addNewsletter(Newsletter newsletter) {
        ContentValues values = new ContentValues();

        values.put(DBNewsletter.STATUS_COL.name, newsletter.getStatus());
        values.put(DBNewsletter.NUMBER_COL.name, newsletter.number);
        values.put(DBNewsletter.DATE_COL.name, newsletter.date);
        values.put(DBNewsletter.OBJECT_COL.name, newsletter.object);
        values.put(DBNewsletter.DETAILS_URL_COL.name, newsletter.detailsUrl);

        //Non si può memorizzare una lista su sql
        String attachmentUrls = null;
        if (newsletter.attachmentsUrl != null) {
            for (String url : newsletter.attachmentsUrl) {
                attachmentUrls += url + ";";
            }
        }

        values.put(DBNewsletter.ATTACHMENTS_URL_COL.name, attachmentUrls);
        values.put(DBNewsletter.PAGE_COL.name, newsletter.page);

        return db.insertOrThrow(NEWSLETTERS_TABLE, null, values);
    }

    private void addSubject(String subject, List<Vote> votes) {
        for (Vote vote : votes) {
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

            db.insertOrThrow(VOTES_TABLE, null, values);
        }
    }

    public void replaceNewsletters(List<Newsletter> newsletters) {
        deleteNewsletters();
        addNewsletters(newsletters);
    }

    public void deleteNewsletters() {
        db.execSQL("DELETE FROM " + NEWSLETTERS_TABLE + ";");
    }

    public List<Newsletter> readNewsletters() {
        Cursor cursor = db.rawQuery("SELECT * FROM " + NEWSLETTERS_TABLE, null);

        List<Newsletter> newsletters = new Vector<>();

        if (cursor.moveToFirst()) {
            do {
                List<String> attachmentUrls = null;
                try {
                    attachmentUrls = Arrays.asList(cursor.getString(DBNewsletter.ATTACHMENTS_URL_COL.ordinal()).split(";"));
                } catch (NullPointerException ignored) {
                }

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

    //endregion

    private enum DBVote {
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

    private enum DBAlert {
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
        IS_DETAILED_COL("isDetailed");

        private final String name;

        DBAlert(String name) {
            this.name = name;
        }
    }

    private enum DBAlertHomeworks {
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
        IS_DETAILED_COL("isDetailed");

        private final String name;

        DBAlertHomeworks(String name) {
            this.name = name;
        }
    }

    private enum DBAlertTests {
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
        IS_DETAILED_COL("isDetailed");

        private final String name;

        DBAlertTests(String name) {
            this.name = name;
        }
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

    public static class AlertsTable {
        public static void createTable(SQLiteDatabase db) {
            String query = "CREATE TABLE " + ALERTS_TABLE + " ("
                    + DBAlert.STATUS_COL.name + " TEXT,"
                    + DBAlert.DATE_COL.name + " TEXT,"
                    + DBAlert.RECEIVERS_COL.name + " TEXT,"
                    + DBAlert.OBJECT_COL.name + " TEXT,"
                    + DBAlert.PAGE_COL.name + " INTEGER,"
                    + DBAlert.DETAILS_URL_COL.name + " TEXT,"
                    + DBAlert.DETAILS_COL.name + " TEXT,"
                    + DBAlert.CREATOR_COL.name + " TEXT,"
                    + DBAlert.TYPE_COL.name + " TEXT,"
                    + DBAlert.ATTACHMENT_URLS_COL.name + " TEXT,"
                    + DBAlert.IS_DETAILED_COL.name + " BOOLEAN" + ")";

            db.execSQL(query);
        }
    }

    public static class AlertsHomeworksTable {
        public static void createTable(SQLiteDatabase db) {
            String query = "CREATE TABLE " + ALERTS_HOMEWORKS_TABLE + " ("
                    + DBAlertHomeworks.STATUS_COL.name + " TEXT,"
                    + DBAlertHomeworks.DATE_COL.name + " TEXT,"
                    + DBAlertHomeworks.RECEIVERS_COL.name + " TEXT,"
                    + DBAlertHomeworks.OBJECT_COL.name + " TEXT,"
                    + DBAlertHomeworks.PAGE_COL.name + " INTEGER,"
                    + DBAlertHomeworks.DETAILS_URL_COL.name + " TEXT,"
                    + DBAlertHomeworks.DETAILS_COL.name + " TEXT,"
                    + DBAlertHomeworks.CREATOR_COL.name + " TEXT,"
                    + DBAlertHomeworks.TYPE_COL.name + " TEXT,"
                    + DBAlertHomeworks.ATTACHMENT_URLS_COL.name + " TEXT,"
                    + DBAlertHomeworks.IS_DETAILED_COL.name + " BOOLEAN" + ")";

            db.execSQL(query);
        }
    }

    public static class VotesTable {
        public static void createTable(SQLiteDatabase db) {
            String query = "CREATE TABLE " + VOTES_TABLE + " ("
                    + DBVote.VALUE_COL.name + " TEXT,"
                    + DBVote.DATE_COL.name + " TEXT,"
                    + DBVote.TEST_TYPE_COL.name + " TEXT,"
                    + DBVote.ARGUMENTS_COL.name + " TEXT,"
                    + DBVote.JUDGEMENT_COL.name + " TEXT,"
                    + DBVote.QUARTERLY_COL.name + " TEXT,"
                    + DBVote.IS_ASTERISK_COL.name + " BOOLEAN,"
                    + DBVote.IS_RELEVANT_FOR_MEAN_COL.name + " BOOLEAN,"
                    + DBVote.SUBJECT.name + " TEXT" + ")";
            db.execSQL(query);
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
                    + DBNewsletter.PAGE_COL.name + " INTEGER" + ")";
            db.execSQL(query);
        }
    }

    public static class AlertsTestsTable {
        public static void createTable(SQLiteDatabase db) {
            String query = "CREATE TABLE " + ALERTS_TESTS_TABLE + " ("
                    + DBAlertTests.STATUS_COL.name + " TEXT,"
                    + DBAlertTests.DATE_COL.name + " TEXT,"
                    + DBAlertTests.RECEIVERS_COL.name + " TEXT,"
                    + DBAlertTests.OBJECT_COL.name + " TEXT,"
                    + DBAlertTests.PAGE_COL.name + " INTEGER,"
                    + DBAlertTests.DETAILS_URL_COL.name + " TEXT,"
                    + DBAlertTests.DETAILS_COL.name + " TEXT,"
                    + DBAlertTests.CREATOR_COL.name + " TEXT,"
                    + DBAlertTests.TYPE_COL.name + " TEXT,"
                    + DBAlertTests.ATTACHMENT_URLS_COL.name + " TEXT,"
                    + DBAlertTests.IS_DETAILED_COL.name + " BOOLEAN" + ")";

            db.execSQL(query);
        }
    }
}

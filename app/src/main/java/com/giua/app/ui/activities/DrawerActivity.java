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

package com.giua.app.ui.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;

import com.giua.app.ActivityManager;
import com.giua.app.AppData;
import com.giua.app.CheckNewsReceiver;
import com.giua.app.GlobalVariables;
import com.giua.app.LoggerManager;
import com.giua.app.LoginData;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.app.ui.fragments.absences.AbsencesFragment;
import com.giua.app.ui.fragments.agenda.AgendaFragment;
import com.giua.app.ui.fragments.authorizations.AuthorizationFragment;
import com.giua.app.ui.fragments.home.HomeFragment;
import com.giua.app.ui.fragments.lessons.LessonsFragment;
import com.giua.app.ui.fragments.not_implmented.NotImplementedFragment;
import com.giua.app.ui.fragments.pinboard.PinboardFragment;
import com.giua.app.ui.fragments.reportcard.ReportCardFragment;
import com.giua.app.ui.fragments.votes.VotesFragment;
import com.giua.webscraper.GiuaScraper;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.concurrent.ThreadLocalRandom;

public class DrawerActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    FragmentTransaction transaction;
    FragmentManager fragmentManager;
    Toolbar toolbar;
    Bundle bundle;
    boolean offlineMode = false;
    boolean demoMode = false;
    LoggerManager loggerManager;
    String userType = "";
    String username = "";
    Drawer mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            savedInstanceState.clear();
        offlineMode = getIntent().getBooleanExtra("offline", false);
        demoMode = SettingsData.getSettingBoolean(this, SettingKey.DEMO_MODE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        loggerManager = new LoggerManager("DrawerActivity", this);
        loggerManager.d("onCreate chiamato");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bundle = new Bundle();
        bundle.putBoolean("offline", offlineMode);

        Intent iCheckNewsReceiver = new Intent(this, CheckNewsReceiver.class);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(this, 0, iCheckNewsReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        changeFragment(R.id.nav_home);

        if (!offlineMode) {
            if (SettingsData.getSettingBoolean(this, SettingKey.DEMO_MODE)) {
                userType = "DEMO";
                username = "DEMO";
            }
            new Thread(() -> {
                GiuaScraper.userTypes _userType = GlobalVariables.gS.getUserTypeEnum();
                String user = GlobalVariables.gS.loadUserFromDocument();
                if (_userType == GiuaScraper.userTypes.PARENT)
                    userType = "Genitore";
                else if (_userType == GiuaScraper.userTypes.STUDENT)
                    userType = "Studente";
                username = user;
                runOnUiThread(this::setupMaterialDrawer);
            }).start();
        } else {
            loggerManager.d("Applicazione in offline mode");
            userType = "Offline";
            username = "Offline";
            runOnUiThread(this::setupMaterialDrawer);
        }
    }

    private void setupMaterialDrawer() {
        // Create the AccountHeader
        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.relative_main_color)
                .withTextColor(getColor(R.color.white))
                .withSelectionListEnabled(false)
                .addProfiles(
                        new ProfileDrawerItem().withName(username).withEmail(userType).withIcon(R.mipmap.ic_launcher)
                ).build();

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .withSliderBackgroundColor(getResources().getColor(R.color.general_view_color, getTheme()))
                .addDrawerItems(
                        createDrawerMainItem(0, "Home", R.id.nav_home, true, false),
                        createDrawerCategory(1, "Lezioni").withSubItems(
                                createDrawerMainItem(2, "Lezioni svolte", R.id.nav_lessons, true, true),
                                createDrawerMainItem(3, "Argomenti e attività", "/genitori/argomenti", true, true)
                        ),
                        createDrawerCategory(4, "Situazione").withSubItems(
                                createDrawerMainItem(5, "Voti", R.id.nav_votes, true, true),
                                createDrawerMainItem(6, "Assenze", R.id.nav_absences, true, true),
                                createDrawerMainItem(7, "Note", "/genitori/note/", true, true),
                                createDrawerMainItem(8, "Osservazioni", "/genitori/osservazioni/", !userType.equals("Studente"), true), //SOLO GENITORE,
                                createDrawerMainItem(9, "Autorizzazioni", R.id.nav_authorization, true, true)
                        ),
                        createDrawerMainItem(10, "Pagella", "/genitori/pagelle", true, false),
                        createDrawerMainItem(11, "Colloqui", "/genitori/colloqui", !userType.equals("Studente"), false),    //SOLO GENITORE,
                        createDrawerCategory(12, "Bacheca").withSubItems(
                                createDrawerMainItem(13, "Circolari e avvisi", R.id.nav_pin_board, true, true),
                                createDrawerMainItem(14, "Documenti", "/documenti/bacheca", true, true)
                        ),
                        createDrawerMainItem(15, "Agenda", R.id.nav_agenda, true, false),

                        new DividerDrawerItem(),

                        createDrawerSecondaryItem(16, "Impostazioni")
                                .withOnDrawerItemClickListener(this::settingsItemOnClick)
                                .withSelectable(false),
                        createDrawerSecondaryItem(17, "Esci")
                                .withOnDrawerItemClickListener(this::logoutItemOnClick)
                                .withSelectable(false)
                )
                .build();
    }

    private ExpandableDrawerItem createDrawerCategory(int identifier, String name) {
        return new ExpandableDrawerItem()
                .withIdentifier(identifier)
                .withIconTintingEnabled(true)
                .withTextColor(getResources().getColor(R.color.adaptive_color_text, getTheme()))
                .withArrowColor(getResources().getColor(R.color.night_white_light_black, getTheme()))
                .withSelectable(false)
                .withName(name);
    }

    private PrimaryDrawerItem createDrawerMainItem(int identifier, String name, @IdRes int id, boolean enabled, boolean withMoreSpace) {
        PrimaryDrawerItem primaryDrawerItem = new PrimaryDrawerItem()
                .withIdentifier(identifier)
                .withIconTintingEnabled(true)
                //.withIcon(icon)
                .withName(name)
                .withTextColor(getResources().getColor(R.color.adaptive_color_text, getTheme()))
                .withEnabled(enabled)
                .withOnDrawerItemClickListener((view, i, item) -> {
                    changeFragment(id);
                    return false;
                });

        if (withMoreSpace)
            primaryDrawerItem.withIcon(R.color.transparent);

        return primaryDrawerItem;
    }

    private PrimaryDrawerItem createDrawerMainItem(int identifier, String name, String url, boolean enabled, boolean withMoreSpace) {
        if (demoMode)    //Nella modalità demo si possono vedere solo le schermate implementate
            enabled = false;

        PrimaryDrawerItem primaryDrawerItem = new PrimaryDrawerItem()
                .withIdentifier(identifier)
                .withIconTintingEnabled(true)
                //.withIcon(icon)
                .withName(name)
                .withTextColor(getResources().getColor(R.color.adaptive_color_text, getTheme()))
                .withEnabled(enabled)
                .withOnDrawerItemClickListener((view, i, item) -> {
                    changeToFragmentNotImplemented(name, url);
                    return false;
                });

        if (withMoreSpace)
            primaryDrawerItem.withIcon(R.color.transparent);

        return primaryDrawerItem;
    }

    private PrimaryDrawerItem createDrawerSecondaryItem(int identifier, String name) {
        return new PrimaryDrawerItem()
                .withIdentifier(identifier)
                .withName(name)
                .withTextColor(getResources().getColor(R.color.adaptive_color_text, getTheme()))
                .withIconTintingEnabled(true);
    }

    private boolean logoutItemOnClick(View view, int i, IDrawerItem item) {
        loggerManager.d("Logout richiesto dall'utente");
        new Thread(() -> {
            AppData.increaseVisitCount("Log out");
        }).start();
        Intent intent = new Intent(this, ActivityManager.class);
        LoginData.clearAll(this);
        startActivity(intent);
        finish();
        return true;
    }

    private boolean settingsItemOnClick(View view, int i, IDrawerItem item) {
        loggerManager.d("Avvio SettingsActivity");
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }

    private void changeToFragmentNotImplemented(String toolbarTitle, String url) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment;
        String tag = "FRAGMENT_NOT_IMPLEMENTED";

        if (!toolbarTitle.equals(toolbar.getTitle())) {  //Se l'elemento cliccato non è già visualizzato allora visualizzalo
            loggerManager.w("Pagina non ancora implementata, la faccio visualizzare dalla webview");
            fragment = new NotImplementedFragment(GiuaScraper.getSiteURL() + url, GlobalVariables.gS.getCookie());
            toolbar.setTitle(toolbarTitle);
            toolbar.setSubtitle("Non ancora implementato!");
            changeFragmentWithManager(fragment, tag);
        }
    }

    private void changeFragment(@IdRes int id) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment;
        String tag = getTagFromId(id);
        toolbar.setSubtitle("");

        if (!manager.getFragments().isEmpty() && manager.getFragments().get(0).getTag().equals(tag)) //Se il fragment visualizzato è quello di id allora non fare nulla
            return;

        if (tag.equals("")) {  //Se tag è vuoto vuol dire che questo id non è stato ancora implementato quindi finisci
            loggerManager.e("Tag vuoto, fragment non ancora implementato");
            return;
        }
        if (id == 0) {
            loggerManager.w("Dovrebbe essere chiamato changeToFragmentNotImplemented non changeFragment");
            return;
        }

        fragment = manager.findFragmentByTag(tag);

        if (id == R.id.nav_home) {
            if (fragment == null)
                fragment = new HomeFragment();
            setTextToolbar("Home");
        } else if (id == R.id.nav_absences) {
            if (fragment == null)
                fragment = new AbsencesFragment();
            setTextToolbar("Assenze");
        } else if (id == R.id.nav_authorization) {
            if (fragment == null)
                fragment = new AuthorizationFragment();
            setTextToolbar("Autorizzazioni");
        } else if (id == R.id.nav_votes) {
            if (fragment == null)
                fragment = new VotesFragment();
            setTextToolbar("Voti");
        } else if (id == R.id.nav_agenda) {
            if (fragment == null)
                fragment = new AgendaFragment();
            setTextToolbar("Agenda");
        } else if (id == R.id.nav_lessons) {
            if (fragment == null)
                fragment = new LessonsFragment();
            setTextToolbar("Lezioni");
        } else if (id == R.id.nav_pin_board) {
            if (fragment == null)
                fragment = new PinboardFragment();
            setTextToolbar("Bacheca");
        } else if (id == R.id.nav_report_card) {
            if (fragment == null)
                fragment = new ReportCardFragment();
            setTextToolbar("Pagella");
        }
        changeFragmentWithManager(fragment, tag);
    }

    private void setTextToolbar(String defaultName) {
        if (offlineMode) {
            toolbar.setTitle(defaultName + " - Offline");
            return;
        }
        if (demoMode) {
            toolbar.setTitle(defaultName + " - DEMO");
            return;
        }
        toolbar.setTitle(defaultName);
    }

    private String getTagFromId(@IdRes int id) {
        if (id == 0)
            return "FRAGMENT_NOT_IMPLEMENTED";
        if (id == R.id.nav_home)
            return "FRAGMENT_HOME";
        if (id == R.id.nav_votes)
            return "FRAGMENT_VOTES";
        if (id == R.id.nav_agenda)
            return "FRAGMENT_AGENDA";
        if (id == R.id.nav_lessons)
            return "FRAGMENT_LESSONS";
        if (id == R.id.nav_pin_board)
            return "FRAGMENT_PIN_BOARD";
        if (id == R.id.nav_report_card)
            return "FRAGMENT_REPORT_CARD";
        if (id == R.id.nav_authorization)
            return "FRAGMENT_AUTHORIZATIONS";
        if (id == R.id.nav_absences)
            return "FRAGMENT_ABSENCES";
        return "";
    }

    private void changeFragmentWithManager(Fragment fragment, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content_main, fragment, tag).commit();
    }

    @Override
    public void onBackPressed() {
        if (!toolbar.getTitle().toString().contains("Home")) {
            mDrawer.setSelection(0, false);
            changeFragment(R.id.nav_home);
        } else {   //Vai alla home del telefono se sei già nella home dell'app
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("offline", offlineMode);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        loggerManager.d("onDestroy chiamato");
        new Thread(() -> {  //Questo serve a prevenire la perdita di news
            AppData.saveNumberNewslettersInt(this, GlobalVariables.gS.checkForNewsletterUpdate(false));
            AppData.saveNumberAlertsInt(this, GlobalVariables.gS.checkForAlertsUpdate(false));
        }).start();
        alarmManager.cancel(pendingIntent);
        if (!LoginData.getUser(this).equals("") && SettingsData.getSettingBoolean(this, SettingKey.NOTIFICATION)) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),
                    AlarmManager.INTERVAL_HOUR + ThreadLocalRandom.current().nextInt(0, 3_600_000),   //Intervallo di 1 ora più numero random tra 0 e 60 minuti
                    pendingIntent);
            //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 60000 , pendingIntent);    //DEBUG
        }
        super.onDestroy();
    }
}
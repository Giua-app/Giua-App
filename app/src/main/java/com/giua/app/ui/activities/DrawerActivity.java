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
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.giua.app.ui.fragments.agenda.AgendaFragment;
import com.giua.app.ui.fragments.home.HomeFragment;
import com.giua.app.ui.fragments.lessons.LessonsFragment;
import com.giua.app.ui.fragments.not_implmented.NotImplementedFragment;
import com.giua.app.ui.fragments.pinboard.PinboardFragment;
import com.giua.app.ui.fragments.reportcard.ReportCardFragment;
import com.giua.app.ui.fragments.votes.VotesFragment;
import com.giua.webscraper.GiuaScraper;
import com.google.android.material.navigation.NavigationView;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;

    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    FragmentTransaction transaction;
    FragmentManager fragmentManager;
    Toolbar toolbar;
    Bundle bundle;
    boolean offlineMode = false;
    LoggerManager loggerManager;
    String userType = "";
    String username = "";
    Drawer mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            savedInstanceState.clear();
        offlineMode = getIntent().getBooleanExtra("offline", false);
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

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        /*DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);

        changeFragment(R.id.nav_home);*/
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        /*if (item.isChecked()) {
            closeNavDrawer();
        } else if (item.getItemId() == R.id.nav_settings) {
            settingsItemOnClick();
        } else if (item.getItemId() == R.id.nav_logout) {
            makeLogout();
        } else
            loggerManager.d("Cambio fragment a " + item.getTitle());
            changeFragment(item.getItemId());
        closeNavDrawer();

        return true;*/
        return true;
    }

    private void setupMaterialDrawer() {
        // Create the AccountHeader
        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.relative_main_color)
                .withTextColor(getColor(R.color.white))
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
                                createDrawerMainItem(3, "Argomenti e attività", 0, "/genitori/argomenti", true, true)
                        ),
                        createDrawerCategory(4, "Situazione").withSubItems(
                                createDrawerMainItem(5, "Voti", R.id.nav_votes, true, true),
                                createDrawerMainItem(6, "Assenze", 0, "/genitori/assenze", true, true),
                                createDrawerMainItem(7, "Note", 0, "/genitori/note/", true, true),
                                createDrawerMainItem(8, "Osservazioni", 0, "/genitori/osservazioni/", !userType.equals("Studente"), true), //SOLO GENITORE,
                                createDrawerMainItem(9, "Autorizzazioni", 0, "/genitori/deroghe/", true, true)
                        ),
                        createDrawerMainItem(10, "Pagella", R.id.nav_report_card, true, false),
                        createDrawerMainItem(11, "Colloqui", 0, "/genitori/colloqui", !userType.equals("Studente"), false),    //SOLO GENITORE,
                        createDrawerCategory(12, "Bacheca").withSubItems(
                                createDrawerMainItem(13, "Circolari e avvisi", R.id.nav_pin_board, true, true),
                                createDrawerMainItem(14, "Documenti", 0, "/documenti/bacheca", true, true)
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

    private PrimaryDrawerItem createDrawerMainItem(int identifier, String name, @IdRes int id, String url, boolean enabled, boolean withMoreSpace) {
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

    private void closeNavDrawer() {
        /*DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }*/
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

        loggerManager.w("Pagina non ancora implementata, la faccio visualizzare dalla webview");
        fragment = new NotImplementedFragment(GiuaScraper.getSiteURL() + url, GlobalVariables.gS.getCookie());
        toolbar.setTitle(toolbarTitle);
        toolbar.setSubtitle("Non ancora implementato!");
        changeFragmentWithManager(fragment, tag);
    }

    private void changeFragment(@IdRes int id) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment;
        String tag = getTagFromId(id);
        toolbar.setSubtitle("");

        if (tag.equals("")) {  //Se tag è vuoto vuol dire che questo id non è stato ancora implementato quindi finisci
            loggerManager.e("Tag vuoto, fragment non ancora implementato");
            return;
        }
        if (id == 0) {
            loggerManager.w("Dovrebbe essere chiamato changeToFragmentNotImplemented non changeFragment");
            return;
        }

        if (id == R.id.nav_home) {
            fragment = manager.findFragmentByTag(tag);
            if (fragment == null)
                fragment = new HomeFragment();
            changeFragmentWithManager(fragment, tag);
            if (!offlineMode)
                toolbar.setTitle("Home");
            else
                toolbar.setTitle("Home - Offline");
        } else if (id == R.id.nav_votes) {
            fragment = manager.findFragmentByTag(tag);
            if (fragment == null)
                fragment = new VotesFragment();
            changeFragmentWithManager(fragment, tag);
            if (!offlineMode)
                toolbar.setTitle("Voti");
            else
                toolbar.setTitle("Voti - Offline");
        } else if (id == R.id.nav_agenda) {
            fragment = manager.findFragmentByTag(tag);
            if (fragment == null)
                fragment = new AgendaFragment();
            changeFragmentWithManager(fragment, tag);
            if (!offlineMode)
                toolbar.setTitle("Agenda");
            else
                toolbar.setTitle("Agenda - Offline");
        } else if (id == R.id.nav_lessons) {
            fragment = manager.findFragmentByTag(tag);
            if (fragment == null)
                fragment = new LessonsFragment();
            changeFragmentWithManager(fragment, tag);
            if (!offlineMode)
                toolbar.setTitle("Lezioni");
            else
                toolbar.setTitle("Lezioni - Offline");
        } else if (id == R.id.nav_pin_board) {
            fragment = manager.findFragmentByTag(tag);
            if (fragment == null)
                fragment = new PinboardFragment();
            changeFragmentWithManager(fragment, tag);
            if (!offlineMode)
                toolbar.setTitle("Bacheca");
            else
                toolbar.setTitle("Bacheca - Offline");
        } else if (id == R.id.nav_report_card) {
            fragment = manager.findFragmentByTag(tag);
            if (fragment == null)
                fragment = new ReportCardFragment();
            changeFragmentWithManager(fragment, tag);
            if (!offlineMode)
                toolbar.setTitle("Pagella");
            else
                toolbar.setTitle("Pagella - Offline");
        }
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
        return "";
    }

    private void changeFragmentWithManager(Fragment fragment, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content_main, fragment, tag).commit();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),
                    AlarmManager.INTERVAL_HOUR + ThreadLocalRandom.current().nextInt(0, 2_700_000),   //Intervallo di 1 ora più numero random tra 0 e 45 minuti più un tempo "random" dato da inexact
                    pendingIntent);
            //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 60000 , pendingIntent);    //DEBUG
        }
        super.onDestroy();
    }
}
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
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;

import com.giua.app.ActivityManager;
import com.giua.app.AppData;
import com.giua.app.CheckNewsReceiver;
import com.giua.app.GlobalVariables;
import com.giua.app.LoginData;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.app.ui.fragments.agenda.AgendaFragment;
import com.giua.app.ui.fragments.home.HomeFragment;
import com.giua.app.ui.fragments.lessons.LessonsFragment;
import com.giua.app.ui.fragments.pinboard.PinboardFragment;
import com.giua.app.ui.fragments.reportcard.ReportCardFragment;
import com.giua.app.ui.fragments.votes.VotesFragment;
import com.giua.webscraper.GiuaScraper;
import com.google.android.material.navigation.NavigationView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            savedInstanceState.clear();
        offlineMode = getIntent().getBooleanExtra("offline", false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);    //Il navigation drawer vero e proprio

        bundle = new Bundle();
        bundle.putBoolean("offline", offlineMode);

        Intent iCheckNewsReceiver = new Intent(this, CheckNewsReceiver.class);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(this, 0, iCheckNewsReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        navigationView.setNavigationItemSelectedListener(this);

        TextView tvUserType = navigationView.getHeaderView(0).findViewById(R.id.txtSubtitle);
        TextView tvUsername = navigationView.getHeaderView(0).findViewById(R.id.txtTitle);

        navigationView.setCheckedItem(R.id.nav_home);

//        btnLogout.setOnClickListener(this::logoutButtonClick);
//        btnSettings.setOnClickListener(this::settingsButtonClick);

        if (!offlineMode) {
            new Thread(() -> {
                GiuaScraper.userTypes userType = GlobalVariables.gS.getUserTypeEnum();
                String user = GlobalVariables.gS.loadUserFromDocument();
                this.runOnUiThread(() -> {
                    if (userType == GiuaScraper.userTypes.PARENT)
                        runOnUiThread(() -> tvUserType.setText("Genitore"));
                    else if (userType == GiuaScraper.userTypes.STUDENT)
                        runOnUiThread(() -> tvUserType.setText("Studente"));
                    runOnUiThread(() -> tvUsername.setText(user));
                });
            }).start();
        } else {
            runOnUiThread(() -> tvUserType.setText("Offline"));
            runOnUiThread(() -> tvUsername.setText("Offline"));
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);

        if (!offlineMode)
            toolbar.setTitle("Home");
        else
            toolbar.setTitle("Home - Offline");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.isChecked()) {
            closeNavDrawer();
        } else if (item.getItemId() == R.id.nav_settings) {
            startSettingsActivity();
        } else if (item.getItemId() == R.id.nav_logout) {
            makeLogout();
        } else
            changeFragment(item.getItemId());
        closeNavDrawer();

        return true;
    }

    private void closeNavDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void makeLogout() {
        new Thread(() -> {
            AppData.increaseVisitCount("Log out");
        }).start();
        Intent intent = new Intent(this, ActivityManager.class);
        LoginData.clearAll(this);
        startActivity(intent);
        finish();
    }

    private void startSettingsActivity() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void changeFragment(@IdRes int id) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment;
        String tag = getTagFromId(id);

        if (tag.equals(""))  //Se tag è vuoto vuol dire che questo id non è stato ancora implementato quindi finisci
            return;

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
        alarmManager.cancel(pendingIntent);
        if (!LoginData.getUser(this).equals("") && SettingsData.getSettingBoolean(this, SettingKey.NOTIFICATION)) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),
                    AlarmManager.INTERVAL_HOUR + ThreadLocalRandom.current().nextInt(0, 900000),   //Intervallo di 1 ora più numero random tra 0 e 15 minuti
                    pendingIntent);
            //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 60000 , pendingIntent);    //DEBUG
        }
        super.onDestroy();
    }
}
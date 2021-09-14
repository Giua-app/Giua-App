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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.giua.app.ActivityManager;
import com.giua.app.CheckNewsReceiver;
import com.giua.app.GlobalVariables;
import com.giua.app.LoginData;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.app.ui.fragments.agenda.AgendaFragment;
import com.giua.app.ui.fragments.lessons.LessonsFragment;
import com.giua.app.ui.fragments.pinboard.PinboardFragment;
import com.giua.app.ui.fragments.reportcard.ReportCardFragment;
import com.giua.app.ui.fragments.votes.VotesFragment;
import com.giua.webscraper.GiuaScraper;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;

    TextView tvUsername;
    TextView tvUserType;
    DrawerLayout drawerLayout;
    NavigationView navigationView;     //Il navigation drawer vero e proprio
    NavController navController = null;     //Si puo intendere come il manager dei fragments
    Intent iCheckNewsReceiver;
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
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        bundle = new Bundle();
        bundle.putBoolean("offline", offlineMode);

        iCheckNewsReceiver = new Intent(this, CheckNewsReceiver.class);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(this, 0, iCheckNewsReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        navigationView.setNavigationItemSelectedListener(this);

        tvUserType = navigationView.getHeaderView(0).findViewById(R.id.txtSubtitle);
        tvUsername = navigationView.getHeaderView(0).findViewById(R.id.txtTitle);

        navigationView.setCheckedItem(R.id.nav_voti);

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

        if (navController == null) {
            fragmentManager = getSupportFragmentManager();
            transaction = fragmentManager.beginTransaction();
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_voti, R.id.nav_agenda, R.id.nav_lezioni, R.id.nav_bacheca, R.id.nav_pagella)
                    .setOpenableLayout(drawerLayout)
                    .build();

            if (Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)).getClass() != NavHostFragment.class) {
                fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, NavHostFragment.class, null).commitNow();
            }

            navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment))).getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            startVotesFragment();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        if (item.isChecked()) {
            closeNavDrawer();
        } else if (item.getItemId() == R.id.nav_voti) {
            startVotesFragment();
        } else if (item.getItemId() == R.id.nav_agenda) {
            startAgendaFragment();
        } else if (item.getItemId() == R.id.nav_lezioni) {
            startLessonsFragment();
        } else if (item.getItemId() == R.id.nav_bacheca) {
            startPinBoardFragment();
        } else if (item.getItemId() == R.id.nav_pagella) {
            startReportCardFragment();
        } else if (item.getItemId() == R.id.nav_settings) {
            startSettingsActivity();
        } else if (item.getItemId() == R.id.nav_logout) {
            makeLogout();
        }
        transaction.commit();
        closeNavDrawer();

        return true;
    }

    private void closeNavDrawer() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void makeLogout() {
        Intent intent = new Intent(this, ActivityManager.class);
        LoginData.clearAll(this);
        startActivity(intent);
        finish();
    }

    private void startSettingsActivity() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void startReportCardFragment() {
        transaction.replace(R.id.nav_host_fragment, ReportCardFragment.class, bundle);
        if (!offlineMode)
            toolbar.setTitle("Pagella");
        else
            toolbar.setTitle("Pagella - Offline");
    }

    private void startPinBoardFragment() {
        transaction.replace(R.id.nav_host_fragment, PinboardFragment.class, bundle);
        if (!offlineMode)
            toolbar.setTitle("Bacheca");
        else
            toolbar.setTitle("Bacheca - Offline");
    }

    private void startVotesFragment() {
        transaction.replace(R.id.nav_host_fragment, VotesFragment.class, bundle);
        if (!offlineMode)
            toolbar.setTitle("Voti");
        else
            toolbar.setTitle("Voti - Offline");
    }

    private void startLessonsFragment() {
        transaction.replace(R.id.nav_host_fragment, LessonsFragment.class, bundle);
        if (!offlineMode)
            toolbar.setTitle("Lezioni");
        else
            toolbar.setTitle("Lezioni - Offline");
    }

    private void startAgendaFragment() {
        transaction.replace(R.id.nav_host_fragment, AgendaFragment.class, bundle);
        if (!offlineMode)
            toolbar.setTitle("Agenda");
        else
            toolbar.setTitle("Agenda - Offline");
    }

    @Override
    public void onBackPressed() {
        if (Objects.requireNonNull(navController.getCurrentDestination()).getId() != R.id.nav_voti) {
            navigationView.setCheckedItem(R.id.nav_voti);
            startVotesFragment();
        } else {
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
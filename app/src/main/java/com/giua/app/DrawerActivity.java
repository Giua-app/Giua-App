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

package com.giua.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.giua.app.ui.agenda.AgendaFragment;
import com.giua.app.ui.lessons.LessonsFragment;
import com.giua.app.ui.pinboard.PinboardFragment;
import com.giua.app.ui.reportcard.ReportCardFragment;
import com.giua.app.ui.votes.VotesFragment;
import com.giua.webscraper.GiuaScraper;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;

    TextView tvUsername;
    TextView tvUserType;
    DrawerLayout drawerLayout;
    NavigationView navigationView;     //Il navigation drawer vero e proprio
    NavController navController;     //Si puo intendere come il manager dei fragments
    Button btnLogout;
    Button btnSettings;
    Intent iCheckNewsReceiver;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    FragmentTransaction transaction;
    FragmentManager fragmentManager;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnLogout = findViewById(R.id.nav_drawer_logout_button);
        btnSettings = findViewById(R.id.nav_drawer_settings_button);

        iCheckNewsReceiver = new Intent(this, CheckNewsReceiver.class);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(this, 0, iCheckNewsReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        navigationView.setNavigationItemSelectedListener(this);

        tvUserType = navigationView.getHeaderView(0).findViewById(R.id.txtSubtitle);
        tvUsername = navigationView.getHeaderView(0).findViewById(R.id.txtTitle);

        navigationView.setCheckedItem(R.id.nav_voti);

        btnLogout.setOnClickListener(this::logoutButtonClick);
        btnSettings.setOnClickListener(this::settingsButtonClick);

        new Thread(() -> {
            GiuaScraper.userTypes userType = GlobalVariables.gS.getUserType();
            String user = GlobalVariables.gS.loadUserFromDocument();
            this.runOnUiThread(() -> {
                if (userType == GiuaScraper.userTypes.PARENT)
                    runOnUiThread(() -> tvUserType.setText("Genitore"));
                else if (userType == GiuaScraper.userTypes.STUDENT)
                    runOnUiThread(() -> tvUserType.setText("Studente"));
                runOnUiThread(() -> tvUsername.setText(user));
            });
        }).start();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_voti, R.id.nav_agenda, R.id.nav_lezioni, R.id.nav_bacheca, R.id.nav_pagella)
                .setOpenableLayout(drawerLayout)
                .build();

        navController = ((NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)).getNavController(); //Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
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

    private void logoutButtonClick(View view) {
        Intent intent = new Intent(this, ActivityManager.class);
        LoginData.clearAll(this);
        startActivity(intent);
        startVotesFragment();
    }

    private void settingsButtonClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));

        // Ritorna una lista di fragment che sono presenti nel fragmentManager.
        // Dovrebbe sempre restituire 1 solo fragment in quanto viene usato sempre il replace().
        List<Fragment> allFragments = fragmentManager.getFragments();

        for (Fragment fragment : allFragments) {    //Rimuove tutti i fragment nel fragmentManager
            fragmentManager.beginTransaction().remove(fragment).commit();
        }

        //Questo sleep è solo per motivi grafici
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            runOnUiThread(() -> navigationView.setCheckedItem(R.id.nav_voti));
        }).start();
    }

    private void startReportCardFragment() {
        transaction.replace(R.id.nav_host_fragment, ReportCardFragment.class, null);
        toolbar.setTitle("Pagella");
    }

    private void startPinBoardFragment() {
        transaction.replace(R.id.nav_host_fragment, PinboardFragment.class, null);
        toolbar.setTitle("Bacheca");
    }

    private void startVotesFragment() {
        transaction.replace(R.id.nav_host_fragment, VotesFragment.class, null);
        toolbar.setTitle("Voti");
    }

    private void startLessonsFragment() {
        transaction.replace(R.id.nav_host_fragment, LessonsFragment.class, null);
        toolbar.setTitle("Lezioni");
    }

    private void startAgendaFragment() {
        transaction.replace(R.id.nav_host_fragment, AgendaFragment.class, null);
        toolbar.setTitle("Agenda");
    }

    public static void setErrorMessage(String message, View root, int layoutID, NavController _navController) {
        if (Objects.requireNonNull(_navController.getCurrentDestination()).getId() == layoutID)
            Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
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
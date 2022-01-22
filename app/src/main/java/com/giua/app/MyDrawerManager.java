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

import android.app.Activity;

import androidx.annotation.IdRes;
import androidx.appcompat.widget.Toolbar;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;

public class MyDrawerManager {

    private final Activity activity;
    private final AccountHeader.OnAccountHeaderListener onChangeAccountFromDrawer;
    public String realUsername;
    public String userType;
    private final Toolbar toolbar;
    private final Drawer.OnDrawerItemClickListener settingsItemOnClick;
    private final Drawer.OnDrawerItemClickListener logoutItemOnClick;
    private final MyFragmentManager myFragmentManager;
    private final boolean demoMode;

    public MyDrawerManager(Activity activity,
                           AccountHeader.OnAccountHeaderListener onChangeAccountFromDrawer,
                           Drawer.OnDrawerItemClickListener settingsItemOnClick,
                           Drawer.OnDrawerItemClickListener logoutItemOnClick,
                           String realUsername, String userType,
                           Toolbar toolbar, MyFragmentManager myFragmentManager,
                           boolean demoMode) {
        this.activity = activity;
        this.onChangeAccountFromDrawer = onChangeAccountFromDrawer;
        this.realUsername = realUsername;
        this.toolbar = toolbar;
        this.userType = userType;
        this.settingsItemOnClick = settingsItemOnClick;
        this.logoutItemOnClick = logoutItemOnClick;
        this.myFragmentManager = myFragmentManager;
        this.demoMode = demoMode;
    }

    public Drawer setupMaterialDrawer() {
        String actualUsername = LoginData.getUser(activity);

        // Create the AccountHeader
        AccountHeaderBuilder accountHeaderBuilder = new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.color.relative_main_color)
                .withTextColor(activity.getColor(R.color.white))
                .withSelectionListEnabled(true)
                .withOnlyMainProfileImageVisible(true)
                .withOnAccountHeaderListener(onChangeAccountFromDrawer)
                .withCurrentProfileHiddenInList(true)
                .addProfiles(
                        new ProfileDrawerItem().withName(realUsername).withEmail(actualUsername)
                                .withIcon(R.mipmap.ic_launcher)
                                .withTextColor(activity.getResources().getColor(R.color.adaptive_color_text, activity.getTheme()))
                );

        //Aggiungi nel drawer gli account disponibili
        String[] allUsernames = AppData.getAllAccountNames(activity).split(";");
        for (String _username : allUsernames) {
            if (_username.equals(actualUsername)) continue;

            if (_username.equals("gsuite")) {
                accountHeaderBuilder.addProfiles(
                        new ProfileDrawerItem().withName(_username).withEmail("Studente")
                                .withIcon(R.mipmap.ic_launcher)
                                .withTextColor(activity.getResources().getColor(R.color.adaptive_color_text, activity.getTheme()))
                );
            } else {
                accountHeaderBuilder.addProfiles(
                        new ProfileDrawerItem().withName(_username).withEmail(_username)
                                .withIcon(R.mipmap.ic_launcher)
                                .withTextColor(activity.getResources().getColor(R.color.adaptive_color_text, activity.getTheme()))
                );
            }
        }

        accountHeaderBuilder.addProfiles(
                new ProfileSettingDrawerItem().withName("Aggiungi account")
                        .withIcon(android.R.color.transparent)
                        .withTextColor(activity.getResources().getColor(R.color.adaptive_color_text, activity.getTheme()))
        );

        AccountHeader accountHeader = accountHeaderBuilder.build();

        return new DrawerBuilder()
                .withActivity(activity)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .withSliderBackgroundColor(activity.getResources().getColor(R.color.general_view_color, activity.getTheme()))
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
                                createDrawerMainItem(13, "Circolari", R.id.nav_newsletters, true, true),
                                createDrawerMainItem(14, "Avvisi", R.id.nav_alerts, true, true),
                                createDrawerMainItem(15, "Documenti", "/documenti/bacheca", true, true)
                        ),
                        createDrawerMainItem(16, "Agenda", R.id.nav_agenda, true, false),

                        new DividerDrawerItem(),

                        createDrawerSecondaryItem(17, "Impostazioni")
                                .withOnDrawerItemClickListener(settingsItemOnClick)
                                .withSelectable(false),
                        createDrawerSecondaryItem(18, "Esci")
                                .withOnDrawerItemClickListener(logoutItemOnClick)
                                .withSelectable(false)
                ).build();
    }


    private ExpandableDrawerItem createDrawerCategory(int identifier, String name) {
        return new ExpandableDrawerItem()
                .withIdentifier(identifier)
                .withIconTintingEnabled(true)
                .withTextColor(activity.getResources().getColor(R.color.adaptive_color_text, activity.getTheme()))
                .withArrowColor(activity.getResources().getColor(R.color.night_white_light_black, activity.getTheme()))
                .withSelectable(false)
                .withName(name);
    }

    //Usato per fragment implementati
    private PrimaryDrawerItem createDrawerMainItem(int identifier, String name, @IdRes int id, boolean enabled, boolean withMoreSpace) {
        PrimaryDrawerItem primaryDrawerItem = new PrimaryDrawerItem()
                .withIdentifier(identifier)
                .withIconTintingEnabled(true)
                //.withIcon(icon)
                .withName(name)
                .withTextColor(activity.getResources().getColor(R.color.adaptive_color_text, activity.getTheme()))
                .withEnabled(enabled)
                .withOnDrawerItemClickListener((view, i, item) -> {
                    myFragmentManager.changeFragment(id);
                    return false;
                });

        if (withMoreSpace)
            primaryDrawerItem.withIcon(R.color.transparent);

        return primaryDrawerItem;
    }

    //Usato per fragment non implementati
    private PrimaryDrawerItem createDrawerMainItem(int identifier, String name, String url, boolean enabled, boolean withMoreSpace) {
        if (demoMode)    //Nella modalità demo si possono vedere solo le schermate implementate
            enabled = false;

        PrimaryDrawerItem primaryDrawerItem = new PrimaryDrawerItem()
                .withIdentifier(identifier)
                .withIconTintingEnabled(true)
                //.withIcon(icon)
                .withName(name)
                .withTextColor(activity.getResources().getColor(R.color.adaptive_color_text, activity.getTheme()))
                .withEnabled(enabled)
                .withOnDrawerItemClickListener((view, i, item) -> {
                    myFragmentManager.changeToFragmentNotImplemented(name, url);
                    return false;
                });

        if (withMoreSpace)
            primaryDrawerItem.withIcon(R.color.transparent);

        return primaryDrawerItem;
    }

    //Usato per funzionalità sperimentali
    private PrimaryDrawerItem createDrawerMainItem(int identifier, String name, String url, @IdRes int id, boolean enabled, boolean withMoreSpace) {
        PrimaryDrawerItem primaryDrawerItem = new PrimaryDrawerItem()
                .withIdentifier(identifier)
                .withIconTintingEnabled(true)
                //.withIcon(icon)
                .withName(name)
                .withTextColor(activity.getResources().getColor(R.color.adaptive_color_text, activity.getTheme()))
                .withEnabled(enabled)
                .withOnDrawerItemClickListener((view, i, item) -> {
                    if (SettingsData.getSettingBoolean(activity, SettingKey.EXP_MODE)) {
                        myFragmentManager.changeFragment(id, "Funzione Sperimentale!");
                        return false;
                    }
                    myFragmentManager.changeToFragmentNotImplemented(name, url);
                    return false;
                });

        if (withMoreSpace)
            primaryDrawerItem.withIcon(R.color.transparent);

        return primaryDrawerItem;
    }

    //Usato per i pulsanti tipo quello delle impostazioni e del logout
    private PrimaryDrawerItem createDrawerSecondaryItem(int identifier, String name) {
        return new PrimaryDrawerItem()
                .withIdentifier(identifier)
                .withName(name)
                .withTextColor(activity.getResources().getColor(R.color.adaptive_color_text, activity.getTheme()))
                .withIconTintingEnabled(true);
    }
}

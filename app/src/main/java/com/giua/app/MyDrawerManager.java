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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.giua.pages.UrlPaths;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

public class MyDrawerManager {

    private final Activity activity;
    private final AccountHeader.OnAccountHeaderListener onChangeAccountFromDrawer;
    public String realUsername;
    public String userType;
    public IProfile<ProfileDrawerItem> activeProfile;
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
        String actualUsername = AppData.getActiveUsername(activity);
        Drawable d = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_account, activity.getTheme());
        d.setColorFilter(activity.getResources().getColor(R.color.profile_background_color, activity.getTheme()), PorterDuff.Mode.SRC_OUT);
        activeProfile = new ProfileDrawerItem().withName(realUsername).withEmail(actualUsername)
                .withIcon(d)
                .withTextColor(activity.getResources().getColor(R.color.adaptive_color_text, activity.getTheme()));

        // Create the AccountHeader
        AccountHeaderBuilder accountHeaderBuilder = new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.color.relative_main_color)
                .withTextColor(activity.getColor(R.color.white))
                .withSelectionListEnabled(SettingsData.getSettingBoolean(activity, SettingKey.EXP_MODE))
                .withOnAccountHeaderListener(onChangeAccountFromDrawer)
                .withCurrentProfileHiddenInList(true)
                .withOnlyMainProfileImageVisible(true)
                .addProfiles(
                        activeProfile
                );

        //Aggiungi nel drawer gli account disponibili
        Object[] allUsernames = AppData.getAllAccountUsernames(activity).toArray();
        for (Object _username : allUsernames) {
            String u = (String) _username;
            if (u.equals(actualUsername)) continue;

            /*Drawable icon = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_account, activity.getTheme());
            icon.setColorFilter(LoginData.getTheme(activity, u), PorterDuff.Mode.ADD);*/

            if (u.equals("gsuite")) {
                accountHeaderBuilder.addProfiles(
                        new ProfileDrawerItem().withName(u).withEmail("Studente")
                                .withIcon(d)
                                .withTextColor(activity.getResources().getColor(R.color.adaptive_color_text, activity.getTheme()))
                );
            } else {
                accountHeaderBuilder.addProfiles(
                        new ProfileDrawerItem().withName(u).withEmail(u)
                                .withIcon(d)
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
                        createAllDrawerItems()
                ).build();
    }

    @NonNull
    private IDrawerItem[] createAllDrawerItems() {
        boolean isParent = !userType.equals("Studente");

        return new IDrawerItem[]{
                createItem(0, R.id.nav_home, false, "Home"),

                //CATEGORIA LEZIONI
                createCategoryItem(1, "Lezioni").withSubItems(
                        createItem(2, R.id.nav_lessons, true, "Lezioni svolte"),
                        createNotImplementedItem(3, true, true, "Argomenti e attività", UrlPaths.ARGUMENTS_ACTIVITIES_PAGE)
                ),

                //CATEGORIA SITUAZIONE
                createCategoryItem(4, "Situazione").withSubItems(
                        createItem(5, R.id.nav_votes, true, "Voti"),
                        createItem(6, R.id.nav_absences, true, "Assenze"),
                        createNotImplementedItem(7, true, true, "Note", UrlPaths.DISCIPLINARY_NOTICES_PAGE),
                        createNotImplementedItem(8, isParent, true, "Osservazioni", UrlPaths.OBSERVATIONS_PAGE), //SOLO GENITORE,
                        createItem(9, R.id.nav_authorization, true, "Autorizzazioni")
                ),


                createNotImplementedItem(10, true, false, "Pagella", UrlPaths.REPORTCARD_PAGE),
                createNotImplementedItem(11, isParent, false, "Colloqui", UrlPaths.INTERVIEWS_PAGE),    //SOLO GENITORE,

                //CATEGORIA BACHECA
                createCategoryItem(12, "Bacheca").withSubItems(
                        createItem(13, R.id.nav_newsletters, true, "Circolari"),
                        createItem(14, R.id.nav_alerts, true, "Avvisi"),
                        createNotImplementedItem(15, true, true, "Documenti", UrlPaths.DOCUMENTS_PAGE)
                ),


                createItem(16, R.id.nav_agenda, false, "Agenda"),

                new DividerDrawerItem(),

                createDrawerSecondaryItem(17, "Impostazioni")
                        .withOnDrawerItemClickListener(settingsItemOnClick)
                        .withSelectable(false),
                createDrawerSecondaryItem(18, "Esci")
                        .withOnDrawerItemClickListener(logoutItemOnClick)
                        .withSelectable(false)
        };
    }

    private ExpandableDrawerItem createCategoryItem(int identifier, String name) {
        return new ExpandableDrawerItem()
                .withIdentifier(identifier)
                .withIconTintingEnabled(true)
                .withTextColor(activity.getResources().getColor(R.color.adaptive_color_text, activity.getTheme()))
                .withArrowColor(activity.getResources().getColor(R.color.night_white_light_black, activity.getTheme()))
                .withSelectable(false)
                .withName(name);
    }

    //Usato per fragment implementati
    private PrimaryDrawerItem createItem(int identifier, @IdRes int id, boolean withMoreSpace, String name) {
        PrimaryDrawerItem primaryDrawerItem = new PrimaryDrawerItem()
                .withIdentifier(identifier)
                .withIconTintingEnabled(true)
                //.withIcon(icon)
                .withName(name)
                .withTextColor(activity.getResources().getColor(R.color.adaptive_color_text, activity.getTheme()))
                .withEnabled(true)
                .withOnDrawerItemClickListener((view, i, item) -> {
                    myFragmentManager.changeFragment(id);
                    return false;
                });

        if (withMoreSpace)
            primaryDrawerItem.withIcon(R.color.transparent);

        return primaryDrawerItem;
    }

    //Usato per fragment non implementati
    private PrimaryDrawerItem createNotImplementedItem(int identifier, boolean enabled, boolean withMoreSpace, String name, String url) {
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

    //Usato per i pulsanti tipo quello delle impostazioni e del logout
    private PrimaryDrawerItem createDrawerSecondaryItem(int identifier, String name) {
        return new PrimaryDrawerItem()
                .withIdentifier(identifier)
                .withName(name)
                .withTextColor(activity.getResources().getColor(R.color.adaptive_color_text, activity.getTheme()))
                .withIconTintingEnabled(true);
    }
}

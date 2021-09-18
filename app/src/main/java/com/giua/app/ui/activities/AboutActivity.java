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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.danielstone.materialaboutlibrary.util.OpenSourceLicense;
import com.giua.app.AppData;
import com.giua.app.R;
import com.google.android.material.snackbar.Snackbar;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

public class AboutActivity extends MaterialAboutActivity {

    int importantInteger;

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {

        importantInteger = 20000 - 300 - 20000 + 160 + 120 + 20;

        MaterialAboutCard.Builder appCardBuilder = new MaterialAboutCard.Builder();

        // Add items to card

        appCardBuilder.addItem(new MaterialAboutTitleItem.Builder()
                .text("Giua App")
                .desc("L'app dell'IIS Michele Giua")
                .icon(R.mipmap.ic_launcher)
                .build());

        appCardBuilder.addItem(ConvenienceBuilder.createVersionActionItem(this,
                new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon2.cmd_information_outline)
                        .sizeDp(18),
                "Versione",
                false)
                .setOnClickAction(() -> {
                    importantInteger++;
                    if(importantInteger==3){
                        Snackbar.make(findViewById(android.R.id.content), ";)", Snackbar.LENGTH_SHORT).show();
                        new Thread(() -> AppData.increaseVisitCount(";)")).start();
                        importantInteger=0;
                    }
                }));

        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Changelog")
                .icon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon2.cmd_history)
                        .sizeDp(18))
                .setOnClickAction(ConvenienceBuilder.createWebViewDialogOnClickAction(this,"", "Chiudi",
                        "https://github.com/Giua-app/Giua-App/releases", true, false))
                .build());

        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Licenze")
                .icon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .sizeDp(18))
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(getBaseContext(), AboutLicenseActivity.class);
                        intent.putExtra("", getIntent().getIntExtra("", 0));
                        context.startActivity(intent);
                    }
                })
                .build());

        MaterialAboutCard.Builder authorCardBuilder = new MaterialAboutCard.Builder();
        authorCardBuilder.title("Author");
//        authorCardBuilder.titleColor(ContextCompat.getColor(c, R.color.colorAccent));

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Hiem")
                //.subText("United Kingdom")
                .icon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_account)
                        .sizeDp(18))
                .build());

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Franck1421")
                //.subText("United Kingdom")
                .icon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_account)
                        .sizeDp(18))
                .build());

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Source code su GitHub")
                .icon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_github_circle)
                        .sizeDp(18))
                .setOnClickAction(ConvenienceBuilder.createWebViewDialogOnClickAction(this,"", "Chiudi",
                        "https://github.com/Giua-app/Giua-App", true, false))
                .build());


        MaterialAboutCard.Builder otherCardBuilder = new MaterialAboutCard.Builder();
        otherCardBuilder.title("Other");

        otherCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Guarda gli analytics pubblici")
                .subText("più un \"contatore di visite\" rispettoso della privacy che un vero e proprio analytics")
                .icon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_google_analytics)
                        .sizeDp(18))
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(this, Uri.parse("https://app.piratepx.com/shared/x9v-IWrD1ZG_Jvk6vyAT53tdz5wCCijE55V9pn060ZfuUJRXT9rcqTwPnR_PWz7R")))
                .build());



        return new MaterialAboutList(appCardBuilder.build(), authorCardBuilder.build(), otherCardBuilder.build());
    }

    public static MaterialAboutList createMaterialAboutLicenseList(final Context c) {

        MaterialAboutCard appIntroLicenseCard = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .sizeDp(18),
                "AppIntro", "2015-2020", "AppIntro Developers",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard giuaScraperLicenseCard = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .sizeDp(18),
                "Giua Webscraper", "2021", "Hiem, Franck1421 and contributors",
                OpenSourceLicense.GNU_GPL_3);

        MaterialAboutCard caocLicenseCard = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .sizeDp(18),
                "CustomActivityOnCrash", "", "Eduard Ereza Martínez",
                OpenSourceLicense.MIT);

        MaterialAboutCard jsoupLicenseCard = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .sizeDp(18),
                "JSoup", "2009-2021", "Jonathan Hedley",
                OpenSourceLicense.MIT);

        MaterialAboutCard materialAboutLibraryLicenseCard = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .sizeDp(18),
                "material-about-library", "2016", "Daniel Stone",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard androidIconicsLicenseCard = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .sizeDp(18),
                "Android Iconics", "2016", "Mike Penz",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard leakCanaryLicenseCard = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .sizeDp(18),
                "LeakCanary", "2015", "Square, Inc",
                OpenSourceLicense.APACHE_2);


        return new MaterialAboutList(giuaScraperLicenseCard,
                appIntroLicenseCard,
                caocLicenseCard,
                jsoupLicenseCard,
                materialAboutLibraryLicenseCard,
                androidIconicsLicenseCard,
                leakCanaryLicenseCard);
    }

    @Nullable
    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.mal_title_about);
    }
}

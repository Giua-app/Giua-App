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

package com.giua.app.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.danielstone.materialaboutlibrary.util.OpenSourceLicense;
import com.giua.app.Analytics;
import com.giua.app.AppUpdateManager;
import com.giua.app.GlobalVariables;
import com.giua.app.LoggerManager;
import com.giua.app.R;
import com.google.android.material.snackbar.Snackbar;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

public class AboutActivity extends MaterialAboutActivity {

    int importantInteger = 20000 - 300 - 20001;
    LoggerManager loggerManager;

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {
        loggerManager = new LoggerManager("AboutActivity", this);
        loggerManager.d("getMaterialAboutList chiamato");
        loggerManager.d("costruisco pagina about...");

        importantInteger += 160 + 120 + 20 - 1;

        MaterialAboutCard.Builder appCardBuilder = new MaterialAboutCard.Builder();

        // Add items to card

        appCardBuilder.addItem(new MaterialAboutTitleItem.Builder()
                .text("Giua App")
                .desc("L'app non ufficiale per registri giua@school")
                .icon(R.mipmap.ic_launcher)
                .build());

        appCardBuilder.addItem(ConvenienceBuilder.createVersionActionItem(this,
                new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon2.cmd_information_outline)
                        .sizeDp(18),
                "Versione",
                false)
                .setOnClickAction(this::justANormalJavaFunction));

        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Changelog")
                .icon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon2.cmd_history)
                        .sizeDp(18))
                .setOnClickAction(() -> {
                    loggerManager.d("Mostro dialogo changelog");
                    GlobalVariables.internetThread.addTask(() -> {
                        String body = AppUpdateManager.buildChangelogForHTML(new AppUpdateManager(this).getReleasesJson());
                        final SpannableString txt = new SpannableString(Html.fromHtml(body, 0));
                        Linkify.addLinks(txt, Linkify.ALL);

                        runOnUiThread(() -> {
                            final AlertDialog d = new AlertDialog.Builder(this)
                                    .setTitle("Changelog")
                                    .setMessage(txt)
                                    .setPositiveButton("Chiudi", (dialog, id) -> dialog.dismiss())
                                    .setCancelable(true)
                                    .create();

                            d.show();

                            ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                        });
                    });
                })
                .build());

        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Privacy Policy")
                .icon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon2.cmd_lock)
                        .sizeDp(18))
                .setOnClickAction(() -> {
                    loggerManager.d("Mostro dialogo privacy");
                    String body = getString(R.string.privacy);
                    final SpannableString txt = new SpannableString(Html.fromHtml(body, 0));
                    Linkify.addLinks(txt, Linkify.ALL);

                    final AlertDialog d = new AlertDialog.Builder(this)
                            .setTitle("Privacy Policy")
                            .setMessage(txt)
                            .setPositiveButton("Chiudi", (dialog, id) -> dialog.dismiss())
                            .setCancelable(true)
                            .create();

                    d.show();

                    ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                })
                .build());

        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Licenze")
                .icon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .sizeDp(18))
                .setOnClickAction(() -> {
                    Intent intent = new Intent(getBaseContext(), AboutLicenseActivity.class);
                    intent.putExtra("", getIntent().getIntExtra("", 0));
                    context.startActivity(intent);
                })
                .build());

        MaterialAboutCard.Builder authorCardBuilder = new MaterialAboutCard.Builder();
        authorCardBuilder.title("Sviluppatori");
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
                .icon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_account)
                        .sizeDp(18))
                .build());

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Krek3r")
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

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Sito web ufficiale")
                .icon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon2.cmd_web)
                        .sizeDp(18))
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(this,
                        Uri.parse("https://giua-app.github.io")))
                .build());


        MaterialAboutCard.Builder otherCardBuilder = new MaterialAboutCard.Builder();
        otherCardBuilder.title("Altro");

        otherCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Guarda gli analytics pubblici")
                .icon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_google_analytics)
                        .sizeDp(18))
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(this,
                        Uri.parse("https://app.posthog.com/shared_dashboard/11AwfEU2Eks9EXtGR50DtXgSLhnB2w")))
                .build());

        loggerManager.d("Creazione pagina about completata");
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
                "Giua Webscraper", "2021-2022", "Hiem, Franck1421 and contributors",
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

        MaterialAboutCard androidChartLicenseCard = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .sizeDp(18),
                "MPAndroidChart", "2020", "Philipp Jahoda",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard drawerLicenseCard = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .sizeDp(18),
                "Material Drawer", "2021", "Mike Penz",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard calendarLicenseCard = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .sizeDp(18),
                "Compact Calendar View", "2017", "Sundeepk",
                OpenSourceLicense.MIT);

        MaterialAboutCard glideLicenseCard = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .sizeDp(18),
                "Glide", "2014", "bumptech",
                OpenSourceLicense.APACHE_2); //FIXME: NON E' LA VERA LICENZA



        return new MaterialAboutList(giuaScraperLicenseCard,
                appIntroLicenseCard,
                caocLicenseCard,
                jsoupLicenseCard,
                materialAboutLibraryLicenseCard,
                androidIconicsLicenseCard,
                leakCanaryLicenseCard, androidChartLicenseCard,
                drawerLicenseCard, calendarLicenseCard, glideLicenseCard);
    }

    @Nullable
    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.mal_title_about);
    }



    private void justANormalJavaFunction(){
        importantInteger++;
        switch(importantInteger){
            case 3:
                Snackbar.make(findViewById(android.R.id.content), ";)", Snackbar.LENGTH_SHORT).show();
                Analytics.sendDefaultRequest(";)");
                loggerManager.d(";)");
                break;
            case 6:
                Snackbar.make(findViewById(android.R.id.content), "(ﾉ◕ヮ◕)ﾉ", Snackbar.LENGTH_SHORT).show();
                loggerManager.d("(ﾉ◕ヮ◕)ﾉ");
                break;
            case 9:
                Snackbar.make(findViewById(android.R.id.content), "(｡◕‿‿◕｡)", Snackbar.LENGTH_SHORT).show();
                loggerManager.d("(｡◕‿‿◕｡)");
                break;
            case 12:
                Snackbar.make(findViewById(android.R.id.content), "(╯°□°）╯︵ ┻━┻  puoi smetterla?", Snackbar.LENGTH_SHORT).show();
                loggerManager.d("(╯°□°）╯︵ ┻━┻");
                break;
            case 15:
                Snackbar.make(findViewById(android.R.id.content), "TI HO DETTO DI SMETTERLA", Snackbar.LENGTH_SHORT).show();
                break;
            case 18:
                Snackbar.make(findViewById(android.R.id.content), "NON MI TOCCARE", Snackbar.LENGTH_SHORT).show();
                break;
            case 21:
                Snackbar.make(findViewById(android.R.id.content), "STAI FERMO ALTRIMENTI...", Snackbar.LENGTH_SHORT).show();
                break;
            case 24:
                Snackbar.make(findViewById(android.R.id.content), "altrimenti....uhh....", Snackbar.LENGTH_SHORT).show();
                break;
            case 27:
                Snackbar.make(findViewById(android.R.id.content), "TI FACCIO CRASHARE", Snackbar.LENGTH_SHORT).show();
                break;
            case 30:
                Snackbar.make(findViewById(android.R.id.content), "SI HAI CAPITO BENE", Snackbar.LENGTH_SHORT).show();
                break;
            case 33:
                Snackbar.make(findViewById(android.R.id.content), "TOCCAMI ANCORA E TI FARO' ESPLODERE IL TELEFONO", Snackbar.LENGTH_SHORT).show();
                break;
            case 36:
                Snackbar.make(findViewById(android.R.id.content), "va bene si non posso farlo, PERO' POSSO FARE QUESTO!", Snackbar.LENGTH_SHORT).show();
                break;
            case 39:
                finish();
                break;
        }
    }
}

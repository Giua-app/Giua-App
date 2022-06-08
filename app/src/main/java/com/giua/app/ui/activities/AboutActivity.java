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
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;
import android.widget.Toast;

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
import com.giua.app.ActivityManager;
import com.giua.app.Analytics;
import com.giua.app.AppUpdateManager;
import com.giua.app.LoggerManager;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.google.android.material.snackbar.Snackbar;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

public class AboutActivity extends MaterialAboutActivity {

    int importantInteger = -23;
    LoggerManager loggerManager;
    boolean isDialogActive = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //setTheme(R.style.Theme_GiuaApp_PartyMode);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {
        loggerManager = new LoggerManager("AboutActivity", this);
        loggerManager.d("getMaterialAboutList chiamato");
        loggerManager.d("costruisco pagina about...");

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
                .setOnClickAction(() -> soLongAndThanksForAllTheFish(true)));

        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text("Changelog")
                .icon(new IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon2.cmd_history)
                        .sizeDp(18))
                .setOnClickAction(() -> {
                    loggerManager.d("Mostro dialogo changelog");
                    new Thread(() -> {
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

                            ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                        });
                    }).start();
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
                        Uri.parse("https://app.posthog.com/shared_dashboard/m3EBhJ_T3dsd4rl3HV399mwKj8szDg")))
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



    private void soLongAndThanksForAllTheFish(boolean fromButton){
        if(SettingsData.getSettingBoolean(this, SettingKey.FIRST_PARTY)) {
            SettingsData.saveSettingBoolean(this, SettingKey.PARTY_MODE, true);
            Toast.makeText(this, "Dì addio ai tuoi occhi", Toast.LENGTH_SHORT).show();
            this.finish();
            Intent intent = new Intent(this, ActivityManager.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }
        if(!isDialogActive && fromButton){
            importantInteger++;
        } else if (isDialogActive && !fromButton)
            importantInteger++;

        if(importantInteger == -16) {
            Snackbar.make(findViewById(android.R.id.content), "Ehi...", Snackbar.LENGTH_SHORT).show();
            Analytics.sendDefaultRequest(Analytics.HERE_TAKE_SOME_CAKE);
            loggerManager.d("So Long, and Thanks for All the Fish");
        }
        switch (importantInteger) {
            case 1:
                isDialogActive = true;
                new AlertDialog.Builder(this)
                        .setTitle(":/")
                        .setMessage("Quindi sei tornato...")
                        .setPositiveButton("Si", (dialog, id) -> soLongAndThanksForAllTheFish(false))

                        .setCancelable(false)
                        .show();
                break;
            case 2:
                new AlertDialog.Builder(this)
                        .setTitle("-_-")
                        .setMessage("...")
                        .setPositiveButton("(avanti)", (dialog, id) -> soLongAndThanksForAllTheFish(false))

                        .setCancelable(false)
                        .show();
                break;
            case 3:
                new AlertDialog.Builder(this)
                        .setTitle("-_-")
                        .setMessage("Purtroppo quest'app non verrà più aggiornata spesso...")
                        .setPositiveButton("Si...", (dialog, id) -> soLongAndThanksForAllTheFish(false))

                        .setCancelable(false)
                        .show();
                break;
            case 4:
                new AlertDialog.Builder(this)
                        .setTitle(";_;")
                        .setMessage("...")
                        .setPositiveButton("(avanti)", (dialog, id) -> soLongAndThanksForAllTheFish(false))

                        .setCancelable(false)
                        .show();
                break;
            case 5:
                new AlertDialog.Builder(this)
                        .setTitle(";-;")
                        .setMessage("Non sono molto bravo con gli addii...")
                        .setPositiveButton("(avanti)", (dialog, id) -> soLongAndThanksForAllTheFish(false))

                        .setCancelable(false)
                        .show();
                break;
            case 6:
                new AlertDialog.Builder(this)
                        .setTitle(";_;")
                        .setMessage("...quindi ho pensato di fare qualcosa di diverso...")
                        .setPositiveButton("(avanti)", (dialog, id) -> soLongAndThanksForAllTheFish(false))

                        .setCancelable(false)
                        .show();
                break;
            case 7:
                new AlertDialog.Builder(this)
                        .setTitle(";_;")
                        .setMessage("...qualcosa di più di un semplice addio...")
                        .setPositiveButton("(avanti)", (dialog, id) -> soLongAndThanksForAllTheFish(false))

                        .setCancelable(false)
                        .show();
                break;
            case 8:
                new AlertDialog.Builder(this)
                        .setTitle("'_'")
                        .setMessage("se più avanti vuoi rivederlo, puoi sempre ritornare da me... sai dove trovarmi")
                        .setPositiveButton("(avanti)", (dialog, id) -> soLongAndThanksForAllTheFish(false))

                        .setCancelable(false)
                        .show();
                break;
            case 9:
                new AlertDialog.Builder(this)
                        .setTitle("'_'")
                        .setMessage("Sei pronto a scoprire cosa sia?")
                        .setPositiveButton("Ok, sono pronto", (dialog, id) -> soLongAndThanksForAllTheFish(false))

                        .setCancelable(false)
                        .show();

                break;
            case 10:
                new AlertDialog.Builder(this)
                        .setTitle("._.")
                        .setMessage("3")
                        .setPositiveButton("(avanti)", (dialog, id) -> soLongAndThanksForAllTheFish(false))

                        .setCancelable(false)
                        .show();
                break;
            case 11:
                new AlertDialog.Builder(this)
                        .setTitle("*-*")
                        .setMessage("2")
                        .setPositiveButton("(avanti)", (dialog, id) -> soLongAndThanksForAllTheFish(false))

                        .setCancelable(false)
                        .show();
                break;
            case 12:
                new AlertDialog.Builder(this)
                        .setTitle("^-^ <(bye)")
                        .setMessage("1")
                        .setPositiveButton("Addio, e grazie per tutto il pesce", (dialog, id) -> {
                            SettingsData.saveSettingBoolean(this, SettingKey.PARTY_MODE, true);
                            SettingsData.saveSettingBoolean(this, SettingKey.FIRST_PARTY, true);
                            Toast.makeText(this, "Dì addio ai tuoi occhi", Toast.LENGTH_SHORT).show();
                            this.finish();
                            Intent intent = new Intent(this, ActivityManager.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        })

                        .setCancelable(false)
                        .show();
                break;
        }

    }
}

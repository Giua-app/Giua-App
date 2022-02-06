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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.giua.app.ui.fragments.absences.AbsencesFragment;
import com.giua.app.ui.fragments.agenda.AgendaFragment;
import com.giua.app.ui.fragments.alerts.AlertsFragment;
import com.giua.app.ui.fragments.authorizations.AuthorizationFragment;
import com.giua.app.ui.fragments.home.HomeFragment;
import com.giua.app.ui.fragments.lessons.LessonsFragment;
import com.giua.app.ui.fragments.newsletters.NewslettersFragment;
import com.giua.app.ui.fragments.not_implemented.NotImplementedFragment;
import com.giua.app.ui.fragments.reportcard.ReportCardFragment;
import com.giua.app.ui.fragments.votes.VotesFragment;
import com.giua.pages.UrlPaths;
import com.giua.webscraper.GiuaScraper;

import java.util.Objects;

/**
 * Questa classe gestisce la transizione e il cambiamento dei fragment in DrawerActivity
 */
public class MyFragmentManager {

    private final LoggerManager loggerManager;
    private final Toolbar toolbar;
    private final FragmentManager fragmentManager;
    private final Activity activity;
    private final boolean offlineMode;
    private final boolean demoMode;
    public String unstableFeatures;

    public MyFragmentManager(Activity activity, Toolbar toolbar, FragmentManager fragmentManager, boolean offlineMode, boolean demoMode, String unstableFeatures) {
        this.activity = activity;
        this.loggerManager = new LoggerManager("MyFragmentManager", activity);
        this.toolbar = toolbar;
        this.fragmentManager = fragmentManager;
        this.offlineMode = offlineMode;
        this.demoMode = demoMode;
        this.unstableFeatures = unstableFeatures;
    }

    private String getUrlFromId(int id) {
        if (id == R.id.nav_home) return UrlPaths.HOME_PAGE;
        else if (id == R.id.nav_absences) return UrlPaths.ABSENCES_PAGE;
        else if (id == R.id.nav_authorization) return UrlPaths.AUTHORIZATIONS_PAGE;
        else if (id == R.id.nav_votes) return UrlPaths.VOTES_PAGE;
        else if (id == R.id.nav_agenda) return UrlPaths.PINBOARD_PAGE;
        else if (id == R.id.nav_lessons) return UrlPaths.LESSONS_PAGE;
        else if (id == R.id.nav_newsletters) return UrlPaths.NEWSLETTERS_PAGE;
        else if (id == R.id.nav_alerts) return UrlPaths.ALERTS_PAGE;
        else if (id == R.id.nav_report_card) return UrlPaths.REPORTCARD_PAGE;
        return "";
    }

    /**
     * Cambia il fragment attuale in quello di {@code id}
     *
     * @param id l' id del layout del fragment da visualizzare
     */
    public void changeFragment(@IdRes int id) {
        changeFragment(id, "", getUrlFromId(id));
    }

    /**
     * Cambia il fragment attuale in quello di {@code id}
     *
     * @param id       l' id del layout del fragment da visualizzare
     * @param subtitle la scritta che apparirà sotto il nome della schermata
     * @param url      l'URL da far visualizzare nella webview nel caso in cui il fragment sia instabile
     */
    public void changeFragment(@IdRes int id, String subtitle, String url) {
        Fragment fragment;
        String tag = getTagFromId(id);
        String toolbarTxt = "";

        //Se il fragment visualizzato è quello di id allora non fare nulla
        if (!fragmentManager.getFragments().isEmpty() && Objects.requireNonNull(fragmentManager.getFragments().get(0).getTag()).equals(tag))
            return;

        if (tag.equals("")) {  //Se tag è vuoto vuol dire che questo id non è stato ancora implementato quindi finisci
            loggerManager.e("Tag vuoto, fragment non ancora implementato");
            return;
        }
        if (id == 0) {
            loggerManager.w("Dovrebbe essere chiamato changeToFragmentNotImplemented non changeFragment");
            return;
        }

        fragment = fragmentManager.findFragmentByTag(tag);

        //FIXME: Troppi if, lo switch non si può usare perchè R.id.x in futuro non sarà final
        if (id == R.id.nav_home) {
            if (fragment == null)
                fragment = new HomeFragment();
            toolbarTxt = "Home";
        } else if (id == R.id.nav_absences) {
            if (fragment == null)
                fragment = new AbsencesFragment();
            toolbarTxt = "Assenze";
        } else if (id == R.id.nav_authorization) {
            if (fragment == null)
                fragment = new AuthorizationFragment();
            toolbarTxt = "Autorizzazioni";
        } else if (id == R.id.nav_votes) {
            if (fragment == null)
                fragment = new VotesFragment();
            toolbarTxt = "Voti";
        } else if (id == R.id.nav_agenda) {
            if (fragment == null)
                fragment = new AgendaFragment();
            toolbarTxt = "Agenda";
        } else if (id == R.id.nav_lessons) {
            if (fragment == null)
                fragment = new LessonsFragment();
            toolbarTxt = "Lezioni";
        } else if (id == R.id.nav_newsletters) {
            if (fragment == null)
                fragment = new NewslettersFragment();
            toolbarTxt = "Circolari";
        } else if (id == R.id.nav_alerts) {
            if (fragment == null)
                fragment = new AlertsFragment();
            toolbarTxt = "Avvisi";
        } else if (id == R.id.nav_report_card) {
            if (fragment == null)
                fragment = new ReportCardFragment();
            toolbarTxt = "Pagella";
        }

        executeChangeFragment(fragment, tag, toolbarTxt, subtitle, url);
    }

    /**
     * Esegue effetivamente il cambio di fragment
     *
     * @param fragment   il fragment da visualizzare
     * @param tag        il tag del fragment da visualizzare
     * @param toolbarTxt il nome che apparirà come nome della schermata
     * @param subtitle   la scritta che apparirà sotto il nome della schermata
     * @param url        l'URL da far visualizzare nella webview nel caso in cui il fragment sia instabile
     */
    private void executeChangeFragment(Fragment fragment, String tag, String toolbarTxt, String subtitle, String url) {
        loggerManager.d("Cambio fragment a " + tag);

        if (fragmentIsUnstable(tag)) {
            loggerManager.w("Rilevata apertura funzionalità instabile (" + tag + ")");
            fragment = new NotImplementedFragment(GiuaScraper.getSiteURL() + "/" + url, GlobalVariables.gS.getCookie());
        }

        setTextToolbar(toolbarTxt);
        toolbar.setSubtitle(subtitle);
        fragmentManager.beginTransaction().replace(R.id.content_main, fragment, tag).commit();
    }

    /**
     * Cambia il fragment in {@code NotImplementedFragment} per far visualizzare la webview
     *
     * @param toolbarTitle il nome che apparirà come nome della schermata
     * @param url          l' URL della pagina visualizzata
     */
    public void changeToFragmentNotImplemented(String toolbarTitle, String url) {
        Fragment fragment;
        String tag = "FRAGMENT_NOT_IMPLEMENTED";

        //Se l'elemento cliccato è già visualizzato allora non fare niente
        if (toolbarTitle.contentEquals(toolbar.getTitle())) return;

        loggerManager.w("Pagina " + toolbarTitle + " non ancora implementata, la faccio visualizzare dalla webview");
        fragment = new NotImplementedFragment(GiuaScraper.getSiteURL() + "/" + url, GlobalVariables.gS.getCookie());
        executeChangeFragment(fragment, tag, toolbarTitle, "Non ancora implementato!", url);
    }

    /**
     * Imposta il nome della schermata
     *
     * @param defaultName il nome che apparirà nella schermata
     */
    private void setTextToolbar(String defaultName) {
        if (offlineMode)
            toolbar.setTitle(defaultName + " - Offline");
        else if (demoMode)
            toolbar.setTitle(defaultName + " - DEMO");
        else
            toolbar.setTitle(defaultName);
    }

    public static String getTagFromId(@IdRes int id) {
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
        if (id == R.id.nav_alerts)
            return "FRAGMENT_ALERTS";
        if (id == R.id.nav_newsletters)
            return "FRAGMENT_NEWSLETTERS";
        if (id == R.id.nav_report_card)
            return "FRAGMENT_REPORT_CARD";
        if (id == R.id.nav_authorization)
            return "FRAGMENT_AUTHORIZATIONS";
        if (id == R.id.nav_absences)
            return "FRAGMENT_ABSENCES";
        return "";
    }

    private boolean fragmentIsUnstable(String tag) {
        String[] uF = unstableFeatures.split("#");
        try {
            for (String feat : uF) {
                String frag = feat.split("\\|")[0].trim();
                String ver = feat.split("\\|")[1].trim();
                if (frag.equals(tag) && ver.equals(BuildConfig.VERSION_NAME)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        } //Se per qualche motivo c'è errore, vuol dire che unstableFeatures è vuoto
        return false;
    }
}

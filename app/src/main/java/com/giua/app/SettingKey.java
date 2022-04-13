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

public abstract class SettingKey {
    /**
     * Il sito a cui fare riferimento di default
     **/
    public static final String DEFAULT_URL = "defaultUrl";

    /**
     * Mostra i centesimi nella media dei voti
     */
    public static final String SHOW_CENTS = "show_cents";

    /**
     * Il tema da utilizzare di defualt
     **/
    public static final String THEME = "theme";

    /**
     * Indica se le notifiche sono attive
     */
    public static final String NOTIFICATION = "notification";

    /**
     * Indica se la notifica per le circolari è abilitata
     */
    public static final String NEWSLETTERS_NOTIFICATION = "newsletter_notification";

    /**
     * Indica se la notifica per gli avvisi è abilitata
     */
    public static final String ALERTS_NOTIFICATION = "alerts_notification";

    /**
     * Indica se la notifica per i nuovi compiti è abilitata
     */
    public static final String HOMEWORKS_NOTIFICATION = "homeworks_notification";

    /**
     * Indica se la notifica per le nuove verifiche è abilitata
     */
    public static final String TESTS_NOTIFICATION = "tests_notification";

    /**
     * Indica se la notifica per i voti è abilitata
     */
    public static final String VOTES_NOTIFICATION = "votes_notification";

    /**
     * Indica se la notifica per gli aggiornamenti è abilitata
     */
    public static final String UPDATES_NOTIFICATION = "updates_notification";

    /**
     * Indica se è il primo avvio dell'app
     */
    public static final String NOT_FIRST_START = "not_first_start";

    /**
     * Indica se la modalità di debug è attiva
     */
    public static final String DEBUG_MODE = "debugMode";

    /**
     * Indica se la modalità demo è attiva
     */
    public static final String DEMO_MODE = "demoMode";

    /**
     * Indica se le funzionalià sperimentali sono attive
     */
    public static final String EXP_MODE = "experimentalMode";

    /**
     * Indica se i voti che non valgono per la media devono essere visualizzati nel grafico della home
     */
    public static final String VOTE_NRFM_ON_CHART = "voteNotRelevantForMeanOnChart";

    /**
     * Indica il comportamento di default da usare per le funzionalità instabili
     */
    public static final String OPEN_UNSTABLE_FEAT_WITH_WEBVIEW = "openUnstableFeaturesWithWebview";


}

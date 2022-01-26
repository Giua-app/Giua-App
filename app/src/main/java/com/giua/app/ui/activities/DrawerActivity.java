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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.giua.app.ActivityManager;
import com.giua.app.Analytics;
import com.giua.app.AppData;
import com.giua.app.AppUpdateManager;
import com.giua.app.BuildConfig;
import com.giua.app.CheckNewsReceiver;
import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.InternetThread;
import com.giua.app.LoggerManager;
import com.giua.app.LoginData;
import com.giua.app.MyDrawerManager;
import com.giua.app.MyFragmentManager;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.objects.Vote;
import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class DrawerActivity extends AppCompatActivity {

    AlarmManager alarmManager;
    Toolbar toolbar;
    Bundle bundle;
    boolean offlineMode = false;
    boolean demoMode = false;
    String goTo = "";
    LoggerManager loggerManager;
    String userType = "Tipo utente non caricato";
    String realUsername = "Nome utente non caricato";
    Drawer mDrawer;
    String unstableFeatures = "";
    public MyFragmentManager myFragmentManager;
    MyDrawerManager myDrawerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            savedInstanceState.clear();
        super.onCreate(savedInstanceState);
        loggerManager = new LoggerManager("DrawerActivity", this);
        if (GlobalVariables.internetThread == null || GlobalVariables.internetThread.isInterrupted())
            GlobalVariables.internetThread = new InternetThread();
        if (GlobalVariables.gS == null) {
            loggerManager.w("gs è null ma non dovrebbe esserlo quindi avvio AutomaticLogin");
            startActivity(new Intent(this, ActivityManager.class));
            finish();
            return;
        }
        offlineMode = getIntent().getBooleanExtra("offline", false);
        demoMode = SettingsData.getSettingBoolean(this, SettingKey.DEMO_MODE);
        goTo = getIntent().getStringExtra("goTo");
        setContentView(R.layout.activity_drawer);
        loggerManager.d("onCreate chiamato");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myFragmentManager = new MyFragmentManager(this, toolbar, getSupportFragmentManager(), offlineMode, demoMode, unstableFeatures);
        myDrawerManager = new MyDrawerManager(this, this::onChangeAccountFromDrawer,
                this::settingsItemOnClick, this::logoutItemOnClick,
                realUsername, userType, toolbar, myFragmentManager, demoMode);

        bundle = new Bundle();
        bundle.putBoolean("offline", offlineMode);

        if (goTo == null || goTo.equals(""))
            myFragmentManager.changeFragment(R.id.nav_home);
        else if (goTo.equals("Newsletters"))
            myFragmentManager.changeFragment(R.id.nav_newsletters);
        else if (goTo.equals("Alerts"))
            myFragmentManager.changeFragment(R.id.nav_alerts);
        else if (goTo.equals("Votes"))
            myFragmentManager.changeFragment(R.id.nav_votes);
        else if (goTo.equals("Agenda"))
            myFragmentManager.changeFragment(R.id.nav_agenda);
        else
            myFragmentManager.changeFragment(R.id.nav_home);

        //Setup CheckNewsReceiver
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent iCheckNewsReceiver = new Intent(this, CheckNewsReceiver.class);
        boolean alarmUp = (PendingIntent.getBroadcast(this, 0, iCheckNewsReceiver, PendingIntent.FLAG_NO_CREATE) != null);  //Controlla se l'allarme è già settato
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, iCheckNewsReceiver, 0);
        loggerManager.d("L'allarme è già settato?: " + alarmUp);
        //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 60000, pendingIntent);    //DEBUG
        if (!alarmUp && !LoginData.getUser(this).equals("") && SettingsData.getSettingBoolean(this, SettingKey.NOTIFICATION)) {

            Random r = new Random(SystemClock.elapsedRealtime());
            long interval = AlarmManager.INTERVAL_HOUR + r.nextInt(3_600_000);

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + interval,   //Intervallo di 1 ora più numero random tra 0 e 60 minuti
                    pendingIntent);
            loggerManager.d("Alarm per CheckNews settato a " + (interval / 60_000) + " minuti");
            //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 60000, pendingIntent);    //DEBUG
        }

        if (!offlineMode) {
            if (SettingsData.getSettingBoolean(this, SettingKey.DEMO_MODE)) {
                userType = "DEMO";
                realUsername = "DEMO";
                myDrawerManager.userType = userType;
                myDrawerManager.realUsername = realUsername;
                mDrawer = myDrawerManager.setupMaterialDrawer();
                return;
            }
            GlobalVariables.internetThread.addRunnableToRun(() -> {
                try {
                    runOnUiThread(() -> mDrawer = myDrawerManager.setupMaterialDrawer());
                    GiuaScraper.userTypes _userType = GlobalVariables.gS.getUserTypeEnum();
                    String user = GlobalVariables.gS.loadUserFromDocument();
                    if (_userType == GiuaScraper.userTypes.PARENT)
                        userType = "Genitore";
                    else if (_userType == GiuaScraper.userTypes.STUDENT)
                        userType = "Studente";
                    realUsername = user;
                    myDrawerManager.userType = userType;
                    myDrawerManager.realUsername = realUsername;
                    runOnUiThread(() -> mDrawer = myDrawerManager.setupMaterialDrawer());
                } catch (GiuaScraperExceptions.YourConnectionProblems | GiuaScraperExceptions.MaintenanceIsActiveException | GiuaScraperExceptions.SiteConnectionProblems ignored) {
                }
            });
        } else {
            loggerManager.w("Applicazione in offline mode");
            userType = "Offline";
            realUsername = "Offline";
            myDrawerManager.userType = userType;
            myDrawerManager.realUsername = realUsername;
            mDrawer = myDrawerManager.setupMaterialDrawer();
        }

        new Thread(() -> {
            loggerManager.d("Scarico le informazioni sulle funzionalità instabili");
            try {
                unstableFeatures = GlobalVariables.gS.getExtPage("https://giua-app.github.io/unstable_features2.txt").text();
                myFragmentManager.unstableFeatures = unstableFeatures;
            } catch (Exception ignored) {}

        }).start();

        checkForUpdateChangelog();
    }

    private void checkForUpdateChangelog(){
        if (!AppData.getAppVersion(this).equals("")
                && !AppData.getAppVersion(this).equals(BuildConfig.VERSION_NAME)) {

            loggerManager.w("Aggiornamento installato rilevato");
            loggerManager.d("Cancello apk dell'aggiornamento e mostro changelog");
            new Analytics.Builder("App aggiornata")
                    .addCustomValue("new_ver", BuildConfig.VERSION_NAME)
                    .addCustomValue("old_ver", AppData.getAppVersion(this)).send();

            AppUpdateManager upd = new AppUpdateManager(DrawerActivity.this);
            upd.deleteOldApk();
            new Thread(upd::showDialogReleaseChangelog).start();
        }
        AppData.saveAppVersion(this, BuildConfig.VERSION_NAME);
    }



    private boolean logoutItemOnClick(View view, int i, IDrawerItem item) {
        loggerManager.d("Logout richiesto dall'utente");
        Analytics.sendDefaultRequest("Log out");
        String[] allAccountNames = AppData.getAllAccountNames(this).split(";");
        String activeUsername = LoginData.getUser(this);
        if (activeUsername.equals("gsuite"))
            CookieManager.getInstance().removeAllCookies(null); //Cancello il cookie della webview
        int index = getIndexOf(allAccountNames, activeUsername);
        if (index == -1) {
            loggerManager.e("Non ho trovato lo username selezionato da drawer in AppData");
            Snackbar.make(view, "Qualcosa è andato storto, impossibile continuare.", Snackbar.LENGTH_SHORT).show();
            return false;   //Chiudi il drawer
        }
        LoginData.clearAll(this);
        AppData.removeAccountCredentialsOfIndex(this, index);
        allAccountNames = AppData.getAllAccountNames(this).split(";");
        //Se vero vuol dire che ci sono altri account diponibili quindi lo faccio riloggare al primo che trovo
        if (allAccountNames.length > 0) {
            index = 0;
            //Cerca il primo username disponibile
            while (index < allAccountNames.length - 1 && allAccountNames[index].equals(""))
                index++;
            String password = AppData.getAllAccountPasswords(this).split(";")[index];
            LoginData.setCredentials(this, allAccountNames[index], password);
        }
        startActivity(new Intent(this, ActivityManager.class));
        finish();
        return true;
    }

    private boolean onChangeAccountFromDrawer(View view, IProfile iProfile, boolean b) {
        if (iProfile.getName().getText().equals("Aggiungi account")) {
            startActivity(new Intent(this, MainLoginActivity.class).putExtra("addAccount", true));
        } else {
            String selectedProfileUsername = iProfile.getName().toString();
            String[] allUsernames = AppData.getAllAccountNames(this).split(";");
            String[] allPasswords = AppData.getAllAccountPasswords(this).split(";");
            int indexOfSelectedUsername = getIndexOf(allUsernames, selectedProfileUsername);
            if (indexOfSelectedUsername == -1) {
                loggerManager.e("Non ho trovato lo username selezionato da drawer in AppData");
                Snackbar.make(view, "Qualcosa è andato storto, impossibile continuare.", Snackbar.LENGTH_SHORT).show();
                return false;   //Chiudi il drawer
            }
            LoginData.setCredentials(this, allUsernames[indexOfSelectedUsername], allPasswords[indexOfSelectedUsername], "");
            GlobalVariables.gS = null;
            startActivity(new Intent(this, ActivityManager.class));
        }
        finish();
        return true;    //Non chiudere il drawer
    }

    private boolean settingsItemOnClick(View view, int i, IDrawerItem item) {
        loggerManager.d("Avvio SettingsActivity");
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }

    public void selectItemInDrawer(long identifier) {
        mDrawer.setSelection(identifier);
    }

    /**
     * Cerca una stringa in un array di stringhe e ne ritorna l'index
     *
     * @param strings l'array in cui bisogna cercare {@code s}
     * @param s       la stringa da cercare in {@code strings}
     * @return l'indice di {@code s} in {@code strings}
     */
    private int getIndexOf(String[] strings, String s) {
        int index = -1;
        for (int j = 0; j < strings.length; j++) {
            if (strings[j].equals(s))
                index = j;
        }
        return index;
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
            return;
        }

        Fragment fragment = getSupportFragmentManager().getFragments().get(0);
        if (fragment.getTag() != null && !fragment.getTag().equals("FRAGMENT_NOT_IMPLEMENTED")) //Se il fragment corrente ha un tag ed è una schermata implementata
            if (((IGiuaAppFragment) fragment).onBackPressed())   //Chiama il metodo onBackPressed e se la chiamata viene gestita (ritorna true) allora finisci
                return;

        if (!toolbar.getTitle().toString().contains("Home")) {  //Se non sei nella home vacci
            mDrawer.setSelection(0, false);
            myFragmentManager.changeFragment(R.id.nav_home);
        } else {   //Vai alla home del telefono se sei già nella home dell'app
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_drawer_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.help_menu_drawer){
            Fragment fragment = getSupportFragmentManager().getFragments().get(0);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setPositiveButton("Chiudi", (dialog, id) -> {})

                .setOnDismissListener(dialog -> {});
            if (fragment.getTag() != null && !fragment.getTag().equals("FRAGMENT_NOT_IMPLEMENTED")){
                //Se il fragment corrente ha un tag ed è una schermata implementata

                builder.setTitle("Come si usa la pagina " + toolbar.getTitle());
                String message = "";

                switch (fragment.getTag()){
                    case "FRAGMENT_HOME":
                        message = "Nella Home puoi vedere il tuo andamento scolastico e " +
                                "avvisi sulle verifiche o compiti per i prossimi giorni";
                        break;
                    case "FRAGMENT_VOTES":
                        message = "Puoi cliccare sui voti per vederne i dettagli (data, tipo, argomento).<br>" +
                                "L'app calcolerà per te la media delle materie.<br>" +
                                "<i>Legenda dei colori:<br>" +
                                "<font color='#69F169'>Verde</font> = Sufficiente<br><font color='#FFBC58'>Arancione</font> = Quasi Sufficiente<br><font color='#F86461'>Rosso</font> = Insufficiente</i>";
                        break;
                    case "FRAGMENT_AGENDA":
                        message = "Puoi cliccare su una data per visualizzarne le attività.<br>" +
                                "Per cambiare mese puoi utilizzare le frecce posizionate ai lati del nome del mese" +
                                ", oppure trascinare il calendario verso destra o verso sinistra.<br>" +
                                "Nel calendario le attività vengono rappresentate da un puntino colorato:<br>" +
                                "<i><font color='#FFBC58'>Arancione</font> = Verifica<br><font color='#6BD5E4'>Celeste</font> = Compiti<br><font color='#7ADD7D'>Verde</font> = Eventi/Attività</i>";
                        break;
                    case "FRAGMENT_LESSONS":
                        message = "Puoi cliccare su una lezione per vederne i dettagli (argomenti, attività)<br>Clicca in basso, " +
                                "sull'icona del calendario o le freccie, per cambiare giorno";
                        break;
                    case "FRAGMENT_ALERTS":
                        message = "Puoi cliccare su un avviso per vederne il contenuto ed eventuali allegati<br>" +
                                "Gli avvisi ancora da leggere sono segnati da una barra arancione affianco all'avviso";
                        break;
                    case "FRAGMENT_NEWSLETTERS":
                        message = "Il tasto a sinistra (con il documento) scarica e apre la circolare<br>" +
                                "Il tasto a destra (con la graffetta) mostra una lista degli allegati presenti," +
                                " clicca un allegato per scaricarlo ed aprirlo.<br><br>" +
                                "Per segnare come letta una circolare <i>senza aprirla</i>, basta trascinare " +
                                "il dito da sinistra verso destra sopra una circolare";
                        break;
                    case "FRAGMENT_ABSENCES":
                        message = "Scrivi la giustificazione (o clicca la freccia per scegliere tra giustificazioni gia fatte) e " +
                                "clicca il pulsante rosso \"Giustifica\" per giustificare l'assenza<br><br>" +
                                "Se vuoi modificarla, scrivi la nuova giustificazione e clicca il pulsante blu \"Modifica\"<br>" +
                                "Se non riesci a modificarla, vuol dire che il professore ha già convalidato l'assenza e non si può più modificare";
                        break;
                    case "FRAGMENT_AUTHORIZATIONS":
                        message = "In questa schermata puoi vedere le tue Autorizzazioni di uscita o entrata";
                        break;
                }
                builder.setMessage(Html.fromHtml(message, 0));
                builder.show();
                return true;
            }
            //Titolo personalizzato per schermate non implementate
            builder.setTitle("Che cosa è una schermata \"Non Implementata?\"");
            builder.setMessage(Html.fromHtml("Queste schermate non sono ancora implementate nell'app, ma lo saranno in futuro.<br><br>" +
                    "Per facilitare l'uso dell'app, essa ti aprirà il sito del registro all'interno dell'app stessa, in modo da non dover cambiare " +
                    "continuamente tra Giua App e Browser", 0));
            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        GlobalVariables.internetThread.addRunnableToRun(() -> {  //Questo serve a prevenire la perdita di news
            try {
                AppData.saveNumberNewslettersInt(this, GlobalVariables.gS.getHomePage(false).getNumberNewsletters());
                AppData.saveNumberAlertsInt(this, GlobalVariables.gS.getHomePage(false).getNumberAlerts());

                Map<String, List<Vote>> votes = GlobalVariables.gS.getVotesPage(false).getAllVotes();
                int numberVotes = 0;
                for (String subject : votes.keySet()) {
                    numberVotes += Objects.requireNonNull(votes.get(subject)).size();
                }
                AppData.saveNumberVotesInt(this, numberVotes);
            } catch (Exception ignored) {
            }
        });
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        loggerManager.d("onDestroy chiamato");
        GlobalVariables.internetThread.interrupt();
        myDrawerManager = null;
        myFragmentManager = null;
        super.onDestroy();
    }
}

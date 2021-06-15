package com.giua.app;

import android.content.Intent;
import android.os.Bundle;

import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.StrictMode;
import android.text.InputType;
import android.view.View;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    EditText etUsername;
    EditText etPassword;
    ProgressBar pgProgressBar;
    ImageButton btnShowPassword;
    Button btnLogin;
    boolean btnShowActivated = false;
    GiuaScraper gS;
    TextView txvErrorMessage;
    Animation errorMessageAnimationStart;
    Animation errorMessageAnimationEnd;
    Handler handler;
    View vErrorView;
    CheckBox chRememberCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.textUser);
        etPassword = findViewById(R.id.textPassword);
        pgProgressBar = findViewById(R.id.progressBar);
        btnShowPassword = findViewById(R.id.show_password_button);
        btnLogin = findViewById(R.id.login_button);

        //TODO: da togliere in futuro
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        etUsername.setText(LoginData.getUser(getApplicationContext()));     //Imposta lo username memorizzato
        etPassword.setText(LoginData.getPassword(getApplicationContext()));     //Imposta la password memorizzata

        handler = new Handler();

        txvErrorMessage = findViewById(R.id.error_notification);
        errorMessageAnimationStart = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.error_notification_animation_start);    //Animazione per mostrare il messaggio

        errorMessageAnimationEnd = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.error_notification_animation_end);        //Animazione per far andare via il messaggio

        Runnable runnable = () -> txvErrorMessage.startAnimation(errorMessageAnimationEnd);

        vErrorView = findViewById(R.id.mainLayout);

        chRememberCredentials = findViewById(R.id.checkbox_remember_credentials);

        /**
         * Oncick per la TextView dell'errore
         */
        txvErrorMessage.setOnClickListener(view -> {            //Quando si clicca l'errore questo scompare
            handler.removeCallbacks(runnable);
            txvErrorMessage.startAnimation(errorMessageAnimationEnd);
        });

        /**
         * Listener per l'animazione per mostrare il messaggio
         */
        errorMessageAnimationEnd.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {       //Quando hai finito l animazione dell'uscita fai scomparire il messaggio
                txvErrorMessage.setVisibility(View.GONE);
                txvErrorMessage.setText("");
            }
        });

        /**
         * Listener per l'animazione per far andare via il messaggio
         */
        errorMessageAnimationStart.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                handler.postDelayed(runnable, 5000);
            }
        });

        /**
         * Login click listener
         */
        btnLogin.setOnClickListener(view -> {
            handler.removeCallbacks(runnable);
            if(etPassword.getText().length() < 1){
                setErrorMessage("Il campo della password non può essere vuoto!");
                return;
            }
            else if(etUsername.getText().length() < 1){
                setErrorMessage("Il campo dello username non può essere vuoto!");
                return;
            }

            pgProgressBar.setVisibility(View.VISIBLE);

            login();
        });


        /**
         * Show password click listener
         */
        btnShowPassword.setOnClickListener(view -> {
            if(!btnShowActivated) {
                etPassword.setInputType (InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_CLASS_TEXT);      //Mostra la password
                btnShowPassword.setImageResource(R.drawable.btn_show_password_true_image);
            } else {
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);  //Nasconde la password
                btnShowPassword.setImageResource(R.drawable.btn_show_password_false_image);
            }
            btnShowActivated = !btnShowActivated;
        });

        //Login automatico se user e password presenti
        if(!etUsername.getText().toString().isEmpty() && !etPassword.getText().toString().isEmpty()){
            login();
        }
    }

    private void login(){
        try {
            gS = new GiuaScraper(etUsername.getText().toString(), etPassword.getText().toString());
        } catch (GiuaScraperExceptions.SessionCookieEmpty sce){
            setErrorMessage("Informazioni di login errate!");
            etPassword.setText("");
            pgProgressBar.setVisibility(View.INVISIBLE);
            return;
        } catch (GiuaScraperExceptions.UnableToLogin utl){
            if(!GiuaScraper.isMyInternetWorking()){
                setErrorMessage("Sono stati riscontrati problemi con la tua rete");
            } else if(!GiuaScraper.isSiteWorking()){
                setErrorMessage("Il sito non sta funzionando, riprova tra poco!");
            } else {
                setErrorMessage("E' stato riscontrato qualche problema sconosciuto riguardo la rete");
            }
            etPassword.setText("");
            pgProgressBar.setVisibility(View.INVISIBLE);
            return;
        }

        if(gS.checkLogin()){
            System.out.println("login ok");
            if(chRememberCredentials.isChecked()) {
                LoginData.setCredentials(this,
                        etUsername.getText().toString(),
                        etPassword.getText().toString());
            }

            Intent intent = new Intent(MainActivity.this, DrawerActivity.class);
            intent.putExtra("giuascraper", gS);
            startActivity(intent);
        } else {
            setErrorMessage("Qualcosa e' andato storto!");
            etPassword.setText("");
            pgProgressBar.setVisibility(View.INVISIBLE);
        }
    }
/*
    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);
    }*/

    /**
     * Esci dall'applicazione simulando la pressione del tasto home
     */
    @Override
    public void onBackPressed(){
        //TODO: da usare solo per debug, l'utente una volta loggato non dovrebbe tornare indietro al login
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setErrorMessage(String message){
        /*
        //TODO: Fare in modo che questo tipo di funzione possa essere richiamabile anche in altre classi
        if(!txvErrorMessage.getText().equals(message)) {
            txvErrorMessage.setVisibility(View.VISIBLE);
            txvErrorMessage.setText(message);
            txvErrorMessage.startAnimation(errorMessageAnimationStart);
        }*/

        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}
package com.giua.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    EditText etUsername;
    EditText etPassword;
    ProgressBar pgProgressBar;
    ImageButton btnShowPassword;
    Button btnLogin;
    boolean btnShowActivated = false;
    GiuaScraper gS;
    TextView txvErrorMessage;
    CheckBox chRememberCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GiuaScraper.setSiteURL("SITO");       //Usami solo per DEBUG per non andare continuamente nelle impostazioni

        etUsername = findViewById(R.id.textUser);
        etPassword = findViewById(R.id.textPassword);
        pgProgressBar = findViewById(R.id.progressBar);
        btnShowPassword = findViewById(R.id.show_password_button);
        btnLogin = findViewById(R.id.login_button);
        txvErrorMessage = findViewById(R.id.error_notification);
        chRememberCredentials = findViewById(R.id.checkbox_remember_credentials);

        //TODO: da togliere in futuro
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        etUsername.setText(LoginData.getUser(getApplicationContext()));     //Imposta lo username memorizzato
        etPassword.setText(LoginData.getPassword(getApplicationContext()));     //Imposta la password memorizzata

        /**
         * Settings button click listener
         */
        findViewById(R.id.floating_settings_button).setOnClickListener(view -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        /**
         * Login click listener
         */
        btnLogin.setOnClickListener(view -> {
            if (etPassword.getText().length() < 1) {
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
            String cookie = LoginData.getCookie(this);
            gS = new GiuaScraper(etUsername.getText().toString(), etPassword.getText().toString(), cookie, true);
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
                String c = gS.getSessionCookie();
                LoginData.setCredentials(this, etUsername.getText().toString(), etPassword.getText().toString(), c);
            }

            Intent intent = new Intent(MainActivity.this, DrawerActivity.class);
            intent.putExtra("giuascraper", gS);
            startActivity(intent);
            finish();
        } else {
            setErrorMessage("Qualcosa e' andato storto!");
            etPassword.setText("");
            pgProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void setErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Esci dall'applicazione simulando la pressione del tasto home
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onRestart() {
        onRestoreInstanceState(new Bundle());
        releaseInstance();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        onRestoreInstanceState(new Bundle());
        releaseInstance();
        super.onResume();
    }

    @Override
    protected void onPause() {
        onSaveInstanceState(new Bundle());
        super.onPause();
    }

    @Override
    protected void onStop() {
        onSaveInstanceState(new Bundle());
        super.onStop();
    }
}
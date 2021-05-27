package com.giua.app;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.StrictMode;
import android.text.InputType;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText etUsername;
    EditText etPassword;
    ProgressBar pgProgressBar;
    ImageButton btnShowPassword;
    Button btnLogin;
    boolean btnShowActivated = false;
    GiuaScraper gS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.textUser);
        etPassword = findViewById(R.id.textPassword);
        pgProgressBar = findViewById(R.id.progressBar);
        btnShowPassword = findViewById(R.id.show_password_button);
        btnLogin = findViewById(R.id.login_button);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /**
         * Login click listener
         */
        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pgProgressBar.setVisibility(View.VISIBLE);

                try {
                    gS = new GiuaScraper(etUsername.getText().toString(), etPassword.getText().toString());
                } catch (GiuaScraperExceptions.SessionCookieEmpty sce){
                    Toast toast = Toast.makeText(getApplicationContext(), "Informazioni di login errate!", Toast.LENGTH_SHORT);
                    toast.show();
                    etPassword.setText("");
                    pgProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                if(gS.checkLogin()){
                    System.out.println("login ok");
                    Intent intent = new Intent(MainActivity.this, LoggedInActivity.class);
                    intent.putExtra("giuascraper", gS);
                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Qualcosa e andato storto!", Toast.LENGTH_SHORT);
                    toast.show();
                    etPassword.setText("");
                    pgProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });


        /**
         * Show password click listener
         */
        btnShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!btnShowActivated) {
                    etPassword.setInputType (InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_CLASS_TEXT);      //Mostra la password
                    btnShowPassword.setImageResource(R.drawable.btn_show_password_true_image);
                } else {
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);  //Nasconde la password
                    btnShowPassword.setImageResource(R.drawable.btn_show_password_false_image);
                }
                btnShowActivated = !btnShowActivated;
            }
        });
    }

    /**
     * Esci dall'applicazione simulando la pressione del tasto home
     */
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
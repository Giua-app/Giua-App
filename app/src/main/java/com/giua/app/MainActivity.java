package com.giua.app;

import android.content.Intent;
import android.os.Bundle;

import com.giua.webscraper.GiuaScraper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Parcelable;
import android.os.StrictMode;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    EditText user;
    EditText password;
    ProgressBar progressBar;
    Button btnShowPassword;
    boolean btnShowActivated = false;
    GiuaScraper gS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = findViewById(R.id.textUser);
        password = findViewById(R.id.textPassword);
        progressBar = findViewById(R.id.progressBar);
        btnShowPassword = findViewById(R.id.btnShowPassword);

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
        findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                
                gS = new GiuaScraper(user.getText().toString(), password.getText().toString());

                if(gS.checkLogin()){
                    System.out.println("login ok");
                    //TODO: Fai partire la seconda activity
                    Intent intent = new Intent(MainActivity.this, LoggedInActivity.class);
                    intent.putExtra("giuascraper", gS);
                    startActivity(intent);
                }
            }
        });


        /**
         * Show password click listener
         */
        findViewById(R.id.btnShowPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!btnShowActivated) {
                    password.setInputType (InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_CLASS_TEXT);      //Mostra la password
                } else {
                    password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);  //Nasconde la password
                }
                btnShowActivated = !btnShowActivated;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
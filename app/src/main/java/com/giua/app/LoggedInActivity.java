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
import android.widget.TextView;

import org.jsoup.nodes.Document;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class LoggedInActivity extends AppCompatActivity {

    TextView text;
    TextView text2;
    GiuaScraper gS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);

        text = findViewById(R.id.txvWelcome);
        text2 = findViewById(R.id.txvVotes);

        Intent intent = getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");
        Document doc = gS.getPage("");

        text.setText("Benvenuto " + gS.getUserType(doc));

        text2.setText("" + gS.getAllVotes().toString());


        findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "no vai via non mi toccare", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }


    /**
     * Torna alla schermata di login quando si clicca il bottone per tornare indietro
     */
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(LoggedInActivity.this, MainActivity.class);
        startActivity(intent);
    }
}

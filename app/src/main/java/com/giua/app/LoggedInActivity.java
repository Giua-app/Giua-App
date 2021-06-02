package com.giua.app;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.giua.webscraper.GiuaScraper;

import androidx.appcompat.app.AppCompatActivity;

public class LoggedInActivity extends AppCompatActivity {

    GiuaScraper gS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO: Questo file e stato rimpiazzato da drawerActivity quindi sarebbe da eliminare
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.fra);

        Intent intent = getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");


    }

    public void onClickVotesNavDrawer(MenuItem menuItem){
        //TODO
    }

    public void onClickAgendaNavDrawer(MenuItem menuItem){
        //TODO
    }

    public void onClickLessonsNavDrawer(MenuItem menuItem){
        //TODO
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

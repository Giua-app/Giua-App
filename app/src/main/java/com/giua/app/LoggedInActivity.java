package com.giua.app;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.giua.webscraper.GiuaScraper;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

public class LoggedInActivity extends AppCompatActivity {

    GiuaScraper gS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.fra);

        Intent intent = getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");

        Bundle bundle = new Bundle();
        bundle.putSerializable("giuascraper", gS);
        VotesFragment votesFragment = new VotesFragment();      //Preparo VotesFragment per prendere la viariabile gS come parametro
        votesFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()      //Parte il fragment dei voti
            .replace(R.id.votes_fragment_placeholder, votesFragment, "votes_fragment_tag")
            .commit();
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

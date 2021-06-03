package com.giua.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.giua.app.ui.agenda.AgendaFragment;
import com.giua.app.ui.lezioni.LezioniFragment;
import com.giua.app.ui.voti.VotiFragment;
import com.giua.webscraper.GiuaScraper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;

    GiuaScraper gS;
    TextView tvUsername;
    TextView tvUserType;
    Bundle bundle;
    DrawerLayout drawerLayout;
    NavigationView navigationView;     //Il navigation drawer vero e proprio
    NavController navController;     //Si puo intendere come il manager dei fragments

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_voti, R.id.nav_agenda, R.id.nav_lezioni)
                .setOpenableLayout(drawerLayout)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        navigationView.setNavigationItemSelectedListener(this);
        Intent intent = getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");

        tvUsername = navigationView.getHeaderView(0).findViewById(R.id.txtUsername);
        tvUserType = navigationView.getHeaderView(0).findViewById(R.id.txtUserType);

        tvUsername.setText(gS.getUser());
        tvUserType.setText(gS.getUserType());

        bundle = new Bundle();
        bundle.putSerializable("giuascraper", gS);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        if(item.getItemId() == R.id.nav_voti){
            startVotesFragment();
        } else if(item.getItemId() == R.id.nav_agenda){
            startAgendaFragment();
        } else if(item.getItemId() == R.id.nav_lezioni){
            startLessonsFragment();
        }

        return true;
    }

    private void closeNavDrawer(){
        if(drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void startVotesFragment(){
        navController.navigate(R.id.nav_voti, bundle);
        closeNavDrawer();
    }

    private void startLessonsFragment(){
        navController.navigate(R.id.nav_lezioni, bundle);
        closeNavDrawer();
    }

    private void startAgendaFragment(){
        navController.navigate(R.id.nav_agenda, bundle);
        closeNavDrawer();
    }
}
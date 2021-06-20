package com.giua.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.giua.webscraper.GiuaScraper;
import com.google.android.material.navigation.NavigationView;

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
    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnLogout = findViewById(R.id.logout_button);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_voti, R.id.nav_agenda, R.id.nav_lezioni, R.id.nav_circolari)
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
        navigationView.setCheckedItem(R.id.nav_voti);

        btnLogout.setOnClickListener(this::logoutButtonClick);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        if(item.isChecked()){
            closeNavDrawer();
        } else if(item.getItemId() == R.id.nav_voti){
            startVotesFragment();
        } else if(item.getItemId() == R.id.nav_agenda){
            startAgendaFragment();
        } else if(item.getItemId() == R.id.nav_lezioni){
            startLessonsFragment();
        } else if (item.getItemId() == R.id.nav_circolari) {
            startNewsLetterFragment();
        }

        return true;
    }

    private void closeNavDrawer() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void logoutButtonClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        LoginData.setCredentials(this, "", "", "");
        startActivity(intent);
        finish();
    }

    private void startNewsLetterFragment() {
        navController.navigate(R.id.nav_circolari, bundle);
        closeNavDrawer();
    }

    private void startVotesFragment() {
        navController.navigate(R.id.nav_voti, bundle);
        closeNavDrawer();
    }

    private void startLessonsFragment() {
        navController.navigate(R.id.nav_lezioni, bundle);
        closeNavDrawer();
    }

    private void startAgendaFragment() {
        navController.navigate(R.id.nav_agenda, bundle);
        closeNavDrawer();
    }

    @Override
    public void onBackPressed() {
        if (navController.getCurrentDestination().getId() != R.id.nav_voti)
            startVotesFragment();
        else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
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
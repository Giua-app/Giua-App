package com.giua.app;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.giua.webscraper.GiuaScraper;
import com.google.android.material.snackbar.Snackbar;

import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {

    EditText etSiteURL;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        etSiteURL = findViewById(R.id.settings_site_url_edit_text);

        setSupportActionBar(toolbar);

        etSiteURL.setText(GiuaScraper.getSiteURL());
        etSiteURL.setSelection(etSiteURL.getText().length());

        findViewById(R.id.settings_save_button).setOnClickListener(this::btnSaveClick);

        findViewById(R.id.settings_crash_button).setOnClickListener(this::btnCrashClick);
    }

    private void setErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void btnSaveClick(View view) {
        if (Pattern.matches("https?://([a-zA-Z0-9]+[.])+([a-zA-Z0-9]+)(:[0-9]+)?", String.valueOf(etSiteURL.getText()))) {
            GiuaScraper.setSiteURL(String.valueOf(etSiteURL.getText()));
            SettingsData.saveSettingString(this, "defaultUrl", GiuaScraper.getSiteURL());
            finish();
        } else
            setErrorMessage("L'url inserito non e' valido.");
    }

    private void btnCrashClick(View view) {
        setErrorMessage("Goodbye");
        throw new RuntimeException("FATAL ERROR: World not found. What have you done?!?!");
    }
}

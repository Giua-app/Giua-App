/*
 * Giua App
 * Android app to view data from the giua@school workbook 
 * Copyright (C) 2021 - 2021 Hiem, Franck1421 and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package com.giua.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.giua.app.ui.ObscureLayoutView;
import com.giua.webscraper.GiuaScraper;
import com.google.android.material.snackbar.Snackbar;

public class StudentLoginActivity extends AppCompatActivity {

    String TAG = "StudentLoginActivity";
    WebView webView;
    ObscureLayoutView obscureLayoutView;
    String userAgent = "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36";
    String cookie = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        webView = findViewById(R.id.studentWebView);
        obscureLayoutView = findViewById(R.id.studentObscureLayoutView);

        obscureLayoutView.setVisibility(View.GONE);

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                String requestedUrl = request.getUrl().toString();
                if (requestedUrl.equals("https://registro.giua.edu.it/") || requestedUrl.equals("https://registro.giua.edu.it/#")) {
                    String rawCookie = CookieManager.getInstance().getCookie("https://registro.giua.edu.it");
                    if (rawCookie != null) {
                        cookie = rawCookie.split("=")[1];
                        onStoppedWebView();
                        return true;
                    }
                    Snackbar.make(findViewById(android.R.id.content), "Login studente fallito, contatta gli sviluppatori", Snackbar.LENGTH_LONG).show();
                }
                return false;
            }
        });

        webView.getSettings().setUserAgentString(userAgent);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl("https://registro.giua.edu.it/login/gsuite");

    }


    private void onStoppedWebView() {
        webView.setVisibility(View.INVISIBLE);
        obscureLayoutView.setVisibility(View.VISIBLE);

        GlobalVariables.gS = new GiuaScraper("gsuite", "gsuite", cookie, true);
        LoginData.setCredentials(this, "gsuite", "gsuite", cookie);
        obscureLayoutView.setVisibility(View.GONE);
        Intent intent = new Intent(StudentLoginActivity.this, DrawerActivity.class);
        startActivity(intent);
    }
}
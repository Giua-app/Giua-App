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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.giua.app.ui.ObscureLayoutView;
import com.giua.webscraper.GiuaScraper;

public class StudentLoginActivity extends AppCompatActivity {

    String TAG = "StudentLoginActivity";
    WebView webView;
    //ProgressBar webViewProgress;
    ObscureLayoutView obscureLayoutView;
    String googleAuthCookies = "";
    String giuaAuthUrl = "";
    String url2 = "";
    String userAgent = "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36";
    String cookie = "";
    int i = 0;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        webView = findViewById(R.id.studentWebView);
        //webViewProgress = findViewById(R.id.webViewProgressBar);
        obscureLayoutView = findViewById(R.id.studentObscureLayoutView);

        obscureLayoutView.setVisibility(View.INVISIBLE);
        //webViewProgress.setVisibility(View.VISIBLE);



        webView.setWebViewClient(new WebViewClient() {
            public void onPageStarted (WebView view, String url, Bitmap favicon){

            }
            public void onPageFinished (WebView view, String url){

            }
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                if (request.getUrl().toString().startsWith("https://registro.giua.edu.it")) {
                    cookie = CookieManager.getInstance().getCookie("https://registro.giua.edu.it").split("=")[1];
                    onStoppedWebView();
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
        Intent intent = new Intent(StudentLoginActivity.this, DrawerActivity.class);
        startActivity(intent);

    }
}
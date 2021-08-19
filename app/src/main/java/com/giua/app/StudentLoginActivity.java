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

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.giua.app.ui.ObscureLayoutView;
import com.giua.webscraper.GiuaScraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.util.Map;

public class StudentLoginActivity extends AppCompatActivity {

    String TAG = "StudentLoginActivity";
    WebView webView;
    //ProgressBar webViewProgress;
    ObscureLayoutView obscureLayoutView;
    String googleAuthCookies = "";
    String giuaAuthUrl = "";
    String url2 = "";
    String userAgent = "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36";

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
                Log.d(TAG,"Loading page " + url);
            }
            public void onPageFinished (WebView view, String url){
                Log.d(TAG,"LOADED " + url);
            }
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request)
            {
                Log.d(TAG,"Override Url reached with url " + request.getUrl());
                if(request.getUrl().toString().matches("^https://registro\\.giua\\.edu\\.it/login/gsuite/check.*$")){
                    //Contiene gsuite/check, ma non è un paramentro dentro google.com
                    Log.d(TAG,"FOUND CHECK GSUITE LINK, STOPPING WEBVIEW - " + request.getUrl().toString());
                    giuaAuthUrl = request.getUrl().toString();
                    onStoppedWebView();
                    return true;
                }
                if(request.getUrl().toString().contains("https://accounts.google.com")){
                    Log.d(TAG,"Found google login, saving cookies - webview url:" + webView.getUrl());
                    googleAuthCookies = CookieManager.getInstance().getCookie(webView.getUrl());
                    Log.d(TAG,"Found google login, saved: " + googleAuthCookies);
                }
                return false;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                //webViewProgress.setProgress(progress, true);
            }
        });
        webView.getSettings().setUserAgentString(userAgent);
        webView.getSettings().setJavaScriptEnabled(true);
        clearCookies();

        webView.loadUrl("https://registro.giua.edu.it/login/gsuite");

    }


    private void onStoppedWebView() {
        webView.setVisibility(View.INVISIBLE);
        obscureLayoutView.setVisibility(View.VISIBLE);
        Connection session = Jsoup.newSession();

        Log.d(TAG,"Link: " + giuaAuthUrl);

        new Thread(() -> {
            try {
                Document doc = session.newRequest()
                        .url(giuaAuthUrl)
                        .get();

                //Log.d(TAG,"HTML: \n" + session.cookieStore().getCookies().get(0).getValue() + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        //PHPSESSID = session.cookieStore().getCookies().get(0).getValue();

        //LoginData.setCredentials(getApplicationContext(), "GSuite user", "123", PHPSESSID);

        //obscureLayoutView.setVisibility(View.INVISIBLE);
    }


    public static void clearCookies()
    {
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
    }

}
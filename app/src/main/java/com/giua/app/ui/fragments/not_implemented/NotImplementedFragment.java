/*
 * Giua App
 * Android app to view data from the giua@school workbook
 * Copyright (C) 2021 - 2022 Hiem, Franck1421 and contributors
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

package com.giua.app.ui.fragments.not_implemented;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.giua.app.GlobalVariables;
import com.giua.app.R;

/**
 * Questo frammento viene visualizzato quando si cerca di visualizzare una schermata non ancora implementata
 */
public class NotImplementedFragment extends Fragment {

    final String userAgent = "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36";
    String url = "";
    String cookie = "";

    public NotImplementedFragment(String url, String cookie) {
        this.url = url;
        this.cookie = cookie;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_not_implemented, container, false);
        ProgressBar pg = root.findViewById(R.id.webview_progressbar);

        WebView webView = root.findViewById(R.id.not_implemented_webview);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                return request.getUrl().toString().equals(GlobalVariables.gS.getSiteUrl() + "/logout/") || !request.getUrl().toString().contains(GlobalVariables.gS.getSiteUrl());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                pg.setVisibility(View.INVISIBLE);
                view.setVisibility(View.VISIBLE);
                webView.evaluateJavascript("try {var exitNode = document.querySelector('[title=\"Esci dal Registro Elettronico\"]');" +
                        "exitNode.parentNode.removeChild(exitNode);" +
                        "var menuNode = document.querySelector('[class=\"navbar-toggle collapsed gs-navbar-toggle gs-pt-1 gs-pb-1 gs-mt-2 gs-mb-0\"]');" +
                        "menuNode.parentNode.removeChild(menuNode); } catch(e){}", null);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                pg.setVisibility(View.VISIBLE);
                view.setVisibility(View.INVISIBLE);
                super.onPageStarted(view, url, favicon);
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setUserAgentString(userAgent);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        CookieManager.getInstance().setCookie(GlobalVariables.gS.getSiteUrl(), "PHPSESSID=" + cookie + ";path=/; HttpOnly; SameSite=lax");

        webView.loadUrl(url);

        return root;
    }
}

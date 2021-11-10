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

package com.giua.app.ui.fragments.not_implemented;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.giua.app.R;
import com.giua.webscraper.GiuaScraper;

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

        WebView webView = root.findViewById(R.id.not_implemented_webview);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                return request.getUrl().toString().equals(GiuaScraper.getSiteURL() + "/logout/");
            }
        });
        webView.getSettings().setUserAgentString(userAgent);
        webView.getSettings().setJavaScriptEnabled(true);
        CookieManager.getInstance().setCookie(GiuaScraper.getSiteURL(), "PHPSESSID=" + cookie + ";path=/; HttpOnly; SameSite=lax");

        webView.loadUrl(url);

        return root;
    }
}

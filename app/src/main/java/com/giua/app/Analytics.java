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

package com.giua.app;

import android.os.Build;
import android.util.Log;

import com.giua.utils.JsonBuilder;
import com.giua.webscraper.GiuaScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class Analytics {
    private final static String API_ENDPOINT = "https://app.posthog.com/capture/";
    private final static String API_KEY = BuildConfig.SUPER_SECRET_KEY + new Secrets().getjFsBBWqO("com.giua.app");

    public static void sendDefaultRequest(String eventName){
        String body = "{" +
                "\"api_key\": \"" + API_KEY + "\"," +
                "\"event\": \"" + eventName + "\"," +
                "\"properties\": {" +
                "\"distinct_id\": \"0\"," +
                "\"scraper_url\": \"" + JsonBuilder.escape(GiuaScraper.getSiteURL()) + "\"," +
                "\"app_ver\": \"" + JsonBuilder.escape(BuildConfig.VERSION_NAME) + "\"," +
                "\"os\": \"Android " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")\"" +
                "}}";

        safeSend(body);
    }

    /**
     * Invia la richiesta HTTPS POST all'API con un Thread.
     * @param body JSON body da inviare insieme alla richiesta
     * @see #send(String)
     */
    public static void safeSend(String body){
        new Thread(() -> send(body)).start();
    }

    /**
     * Invia la richiesta HTTPS POST all'API
     * <h3>ATTENZIONE QUESTA FUNZIONE NON E' THREAD-SAFE</h3>
     * @param body JSON body da inviare insieme alla richiesta
     * @return risposta dal server in JSON
     * @see #safeSend(String)
     */
    public static String send(String body){
        Document doc;
        Log.d("FINDME", "send: AAAAAAAAAAAAAA");
        try {
            doc = Jsoup.newSession().ignoreContentType(true).ignoreHttpErrors(true)
                    .url(API_ENDPOINT)
                    .header("Content-Type", "application/json")
                    .requestBody(body).post();
        } catch (IOException e){
            e.printStackTrace();
            Log.e("FINDME", "crash ", e);
            return null;
        }
        Log.d("FINDME", "send: " + doc.text());
        System.out.println(doc.text());
        return doc.text();
    }


    public static class Builder {
        private String body;

        public Builder(String eventName) {
            body = "{" +
                    "\"api_key\": \"" + API_KEY + "\"," +
                    "\"event\": \"" + eventName + "\"," +
                    "\"properties\": {" +
                    "\"distinct_id\": \"0\"," +
                    "\"scraper_url\": \"" + JsonBuilder.escape(GiuaScraper.getSiteURL()) + "\"," +
                    "\"app_ver\": \"" + JsonBuilder.escape(BuildConfig.VERSION_NAME) + "\"," +
                    "\"os\": \"Android " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")\"";
        }


        public void send(){
            body += "}}";
            Analytics.safeSend(body);
        }


        public Builder addCustomValue(String key, String value){
            body += ",\"" + key + "\": " + "\"" + JsonBuilder.escape(value) + "\"";
            return this;
        }
    }
}

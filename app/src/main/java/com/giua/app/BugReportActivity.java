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

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class BugReportActivity extends AppCompatActivity {

    Button btnSend;
    TextInputLayout bugTitle;
    TextInputLayout bugDesc;
    String dontStealMePlease = "dontStealMePlease";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bugreport);

        btnSend = findViewById(R.id.bug_send_button);
        bugTitle = findViewById(R.id.txtBugTitle);
        bugDesc = findViewById(R.id.txtBugDesc);

        btnSend.setOnClickListener(v -> onSendBug());
        dontStealMePlease = BuildConfig.SECRET_KEY;
    }

    private void onSendBug(){
        new Thread(() -> {
            try {
                Connection session = Jsoup.newSession().ignoreContentType(true);
                session.url("https://api.github.com/repos/HiemSword/regnodellasintassi-site/issues")
                        .header("Authorization", "token " + new Secrets().getgEPeTNbQ(getPackageName()) + dontStealMePlease)
                        .header("Accept", "application/vnd.github.v3+json")
                        .requestBody("{\"title\": \"" + bugTitle.getEditText().getText() + "\"," +
                                "\"body\": \"" + bugDesc.getEditText().getText() + "\"}")
                        .post();
            } catch(Exception e){
                e.printStackTrace();
            }
        }).start();
    }
}
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

package com.giua.app.ui.intro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.giua.app.ActivityManager
import com.giua.app.R
import dev.doubledot.doki.views.DokiContentView

class CustomDokiActivity : AppCompatActivity() {

    private val dokiContent: DokiContentView? by lazy {
        findViewById(R.id.doki_content)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_intro_doki)

        dokiContent?.setOnCloseListener { startActivity(Intent(this@CustomDokiActivity, ActivityManager::class.java)) }
        dokiContent?.setExplanationVisibility(false)
        dokiContent?.setDeveloperSolutionVisibility(false)
        dokiContent?.loadContent(appName = "Giua App")
        Log.d("ARII", "IDUASUDLSADLASDSAUIPGD")
    }

}
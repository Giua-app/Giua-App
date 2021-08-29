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
package com.giua.app

import android.content.Intent
import android.os.Bundle
import androidx.core.view.LayoutInflaterCompat
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.github.appintro.model.SliderPage
import com.mikepenz.iconics.context.IconicsLayoutInflater2

class AppIntroActivity : AppIntro(){

    override fun onCreate(savedInstanceState: Bundle?) {
        LayoutInflaterCompat.setFactory2(layoutInflater, IconicsLayoutInflater2(delegate))
        super.onCreate(savedInstanceState)

        addSlide(AppIntroFragment.newInstance(
                "Benvenuto!",
                "Segui questa preve introduzione per scorpire tutte le funzionalità di Giua App",
                imageDrawable = R.mipmap.ic_launcher,
                backgroundDrawable = R.drawable.intro_back_slide1,
                titleTypefaceFontRes = R.font.caviar_dreams_bold,
                descriptionTypefaceFontRes = R.font.caviar_dreams_bold
        ))

        addSlide(AppIntroFragment.newInstance(SliderPage(
                "Voti",
                "Puoi cliccare su un voto per vederne i dettagli",
                imageDrawable = R.mipmap.ic_launcher,
                backgroundDrawable = R.drawable.intro_back_slide2,
                titleTypefaceFontRes = R.font.roboto_light,
                descriptionTypefaceFontRes = R.font.roboto_light
        )))

        addSlide(AppIntroFragment.newInstance(
                "Bacheca (Circolari)",
                "Clicca su {gmd_pageview} per visualizzare la circolare, clicca su {gmd_attachment} per vedere i suoi allegati",
                imageDrawable = R.mipmap.ic_launcher,
                backgroundDrawable = R.drawable.intro_back_slide3,
                titleTypefaceFontRes = R.font.roboto_light,
                descriptionTypefaceFontRes = R.font.roboto_light
        ))

        addSlide(AppIntroFragment.newInstance(
                "Bacheca (Avvisi)",
                "Clicca su un avviso per vedere i dettagli",
                imageDrawable = R.mipmap.ic_launcher,
                backgroundDrawable = R.drawable.intro_back_slide4
        ))

        addSlide(AppIntroFragment.newInstance(
                "Lezioni",
                "Clicca su una lezione per vedere i dettagli. Per cambiare giorno, clicca su ICONACALENDARIO per selezionare il giorno",
                imageDrawable = R.mipmap.ic_launcher,
                backgroundDrawable = R.drawable.intro_back_slide5
        ))

        addSlide(AppIntroFragment.newInstance(
            "Agenda",
            "bla",
            imageDrawable = R.mipmap.ic_launcher,
            backgroundDrawable = R.drawable.intro_back_slide1,
            titleTypefaceFontRes = R.font.caviar_dreams_bold,
            descriptionTypefaceFontRes = R.font.caviar_dreams_bold
        ))

        addSlide(AppIntroFragment.newInstance(SliderPage(
            "bla",
            "bla",
            imageDrawable = R.mipmap.ic_launcher,
            backgroundDrawable = R.drawable.intro_back_slide2,
            titleTypefaceFontRes = R.font.roboto_light,
            descriptionTypefaceFontRes = R.font.roboto_light
        )))


        setTransformer(AppIntroPageTransformerType.Parallax())
    }

    public override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        SettingsData.saveSettingInt(this, "introStatus", 1)
        startActivity(Intent(this@AppIntroActivity, ActivityManager::class.java))
    }

    public override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        SettingsData.saveSettingInt(this, "introStatus", 1)
        startActivity(Intent(this@AppIntroActivity, ActivityManager::class.java))
    }


}
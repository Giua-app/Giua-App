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


        setProgressIndicator()

        addSlide(AppIntroFragment.newInstance(
            "Benvenuto!",
            "Segui questa preve introduzione per scoprire tutte le funzionalità di Giua App",
            imageDrawable = R.drawable.ic_giuaschool_logo1,
            backgroundDrawable = R.drawable.intro_back_slide1,
            titleTypefaceFontRes = R.font.caviar_dreams_bold,
            descriptionTypefaceFontRes = R.font.caviar_dreams
        ))

        addSlide(AppIntroFragment.newInstance(SliderPage(
            "Voti",
            "Puoi cliccare su un voto per vederne i dettagli",
            imageDrawable = R.mipmap.voti_tutorial,
            backgroundDrawable = R.drawable.intro_back_slide2,
            titleTypefaceFontRes = R.font.caviar_dreams_bold,
            descriptionTypefaceFontRes = R.font.caviar_dreams
        )))

        /*addSlide(AppIntroFragment.newInstance(SliderPage(
            "Voti",
            "Puoi cliccare su un voto per vederne i dettagli",
            imageDrawable = R.mipmap.voti2_tutorial,
            backgroundDrawable = R.drawable.intro_back_slide5,
            titleTypefaceFontRes = R.font.caviar_dreams_bold,
            descriptionTypefaceFontRes = R.font.roboto_light
        )))*/

        addSlide(AppIntroFragment.newInstance(
            "Bacheca (Circolari)",
            "Clicca su {cmd_file_document_outline} per visualizzare la circolare",
            imageDrawable = R.mipmap.circolari_tutorial,
            backgroundDrawable = R.drawable.intro_back_slide3,
            titleTypefaceFontRes = R.font.caviar_dreams_bold,
            descriptionTypefaceFontRes = R.font.caviar_dreams
        ))

        addSlide(AppIntroFragment.newInstance(
            "Bacheca (Circolari)",
            "Clicca su {gmd_attachment} per vedere i suoi allegati",
            imageDrawable = R.mipmap.circolari2_tutorial,
            backgroundDrawable = R.drawable.intro_back_slide4,
            titleTypefaceFontRes = R.font.caviar_dreams_bold,
            descriptionTypefaceFontRes = R.font.caviar_dreams
        ))

        addSlide(AppIntroFragment.newInstance(
            "Bacheca (Avvisi)",
            "Clicca su un avviso per vederne i dettagli",
            imageDrawable = R.mipmap.avvisi_tutorial,
            backgroundDrawable = R.drawable.intro_back_slide6,
            titleTypefaceFontRes = R.font.caviar_dreams_bold,
            descriptionTypefaceFontRes = R.font.caviar_dreams
        ))

        addSlide(AppIntroFragment.newInstance(
            "Lezioni",
            "Clicca su una lezione per vederne i dettagli",
            imageDrawable = R.mipmap.lezioni_tutorial,
            backgroundDrawable = R.drawable.intro_back_slide7,
            titleTypefaceFontRes = R.font.caviar_dreams_bold,
            descriptionTypefaceFontRes = R.font.caviar_dreams
        ))

        addSlide(AppIntroFragment.newInstance(
            "Lezioni",
            "Per cambiare giorno, clicca su {cmd_calendar_blank} per aprire il calendario",
            imageDrawable = R.mipmap.lezioni2_tutorial,
            backgroundDrawable = R.drawable.intro_back_slide8,
            titleTypefaceFontRes = R.font.caviar_dreams_bold,
            descriptionTypefaceFontRes = R.font.caviar_dreams
        ))

        addSlide(AppIntroFragment.newInstance(
            "Agenda",
            "Clicca su una verifica o compito per vedere i dettagli",
            imageDrawable = R.mipmap.agenda_tutorial,
            backgroundDrawable = R.drawable.intro_back_slide9,
            titleTypefaceFontRes = R.font.caviar_dreams_bold,
            descriptionTypefaceFontRes = R.font.caviar_dreams
        ))

        addSlide(AppIntroFragment.newInstance(SliderPage(
            "Ottimizzazione batteria",
            "Per poter ricevere notifiche dal registro devi togliere sta cosa perchè si (inserire codice per aprire impostazioni e togliere la cosa)",
            imageDrawable = R.mipmap.ic_launcher,
            backgroundDrawable = R.drawable.intro_back_slide1,
            titleTypefaceFontRes = R.font.caviar_dreams_bold,
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
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
package com.giua.app.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.view.LayoutInflaterCompat
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.giua.app.*
import com.giua.app.ui.fragments.intro.CustomSlideFragment
import com.mikepenz.iconics.context.IconicsLayoutInflater2

class AppIntroActivity : AppIntro2(){

    override fun onCreate(savedInstanceState: Bundle?) {
        LayoutInflaterCompat.setFactory2(layoutInflater, IconicsLayoutInflater2(delegate))
        super.onCreate(savedInstanceState)

        setProgressIndicator() //Progress bar come barra anzichè pallini
        setIndicatorColor(
            selectedIndicatorColor = getColor(R.color.design_default_color_secondary),
            unselectedIndicatorColor = getColor(R.color.design_default_color_secondary)
        )
        setImmersiveMode() //Fullscreen
        isSystemBackButtonLocked = true
        isWizardMode = true //Rimuove pulsante salta

        setTransformer(AppIntroPageTransformerType.Parallax(
            titleParallaxFactor = 1.0,
            imageParallaxFactor = 1.0,
            descriptionParallaxFactor = 2.0
        ))

        addSlide(CustomSlideFragment.newInstance(
            "Benvenuto!",
            "Segui questa breve introduzione per configurare Giua App",
            backgroundDrawable = R.drawable.bg_intro_slide1,
            layoutResId = R.layout.fragment_appintro_customslide_1,
            gifRaw = R.raw.introv4
        ))

        addSlide(AppIntroFragment.newInstance(
            "Disattiva il Risparmio Batteria",
            "Disattiva il Risparmio Batteria per consentirci di inviarti notifiche sui nuovi compiti, verifiche, avvisi e tanto altro",
            imageDrawable = R.mipmap.battery_tutorial,
            backgroundDrawable = R.drawable.bg_intro_slide2,
            descriptionTypefaceFontRes = R.font.ubuntu_light,
            titleTypefaceFontRes = R.font.ubuntu_light
        ))

        addSlide(CustomSlideFragment.newInstance(
            "Disattiva il Risparmio Batteria",
            "Disattiva il Risparmio Batteria per consentirci di inviarti notifiche sui nuovi compiti, verifiche, avvisi e tanto altro",
            imageDrawable = R.mipmap.battery_tutorial,
            backgroundDrawable = R.drawable.bg_intro_slide2,
            layoutResId = R.layout.fragment_appintro_customslide_2
        ))

        addSlide(CustomSlideFragment.newInstance(
            "Lezioni",
            "Clicca su una lezione per vederne i dettagli, clicca ovunque per annulare",
            gifRaw = R.raw.lezioni,
            backgroundDrawable = R.drawable.bg_intro_slide2,
            layoutResId = R.layout.fragment_appintro_customslide_2
        ))

        addSlide(CustomSlideFragment.newInstance(
            "Lezioni",
            "Per cambiare giorno, clicca su {cmd_calendar_blank} per aprire il calendario",
            gifRaw = R.raw.lezioni2,
            backgroundDrawable = R.drawable.bg_intro_slide2,
            layoutResId = R.layout.fragment_appintro_customslide_2
        ))

        addSlide(CustomSlideFragment.newInstance(
            "Voti",
            "Puoi cliccare su un voto per vederne i dettagli",
            imageDrawable = R.mipmap.phone1,
            backgroundDrawable = R.drawable.bg_intro_slide3,
            layoutResId = R.layout.fragment_appintro_customslide_2
        ))

        addSlide(CustomSlideFragment.newInstance(
            "Assenze",
            "Puoi cliccare su un voto per vederne i dettagli",
            imageDrawable = R.mipmap.phone1,
            backgroundDrawable = R.drawable.bg_intro_slide4,
            layoutResId = R.layout.fragment_appintro_customslide_2
        ))

        addSlide(CustomSlideFragment.newInstance(
            "Circolari",
            "Clicca su {cmd_file_document_outline} per visualizzare la circolare\nClicca su {cmd_paperclip} per visualizzare gli allegati",
            imageDrawable = R.mipmap.phone1,
            backgroundDrawable = R.drawable.bg_intro_slide3,
            layoutResId = R.layout.fragment_appintro_customslide_2
        ))

        addSlide(CustomSlideFragment.newInstance(
            "Circolari",
            "Muovi il dito da sinistra verso destra per impostare come letta la circolare",
            imageDrawable = R.mipmap.phone1,
            backgroundDrawable = R.drawable.bg_intro_slide3,
            layoutResId = R.layout.fragment_appintro_customslide_2
        ))

        addSlide(CustomSlideFragment.newInstance(
            "Avvisi",
            "Clicca su un avviso per vederne i dettagli",
            imageDrawable = R.mipmap.phone1,
            backgroundDrawable = R.drawable.bg_intro_slide4,
            layoutResId = R.layout.fragment_appintro_customslide_2
        ))

        addSlide(CustomSlideFragment.newInstance(
            "Batteria",
            "Inserire testo riguardo la cosa la batteria bho",
            imageDrawable = R.mipmap.battery_tutorial,
            backgroundDrawable = R.drawable.bg_intro_slide9,
            layoutResId = R.layout.fragment_appintro_customslide_2
        ))

        addSlide(CustomSlideFragment.newInstance(
            "E' tutto pronto!",
            "Clicca \"FINE\" per passare alla pagina di login",
            imageDrawable = R.mipmap.fine_tutorial,
            backgroundDrawable = R.drawable.bg_intro_slide1,
            layoutResId = R.layout.fragment_appintro_customslide_2
        ))
    }

    public override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finishIntro()
    }

    public override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finishIntro()
    }

    private fun finishIntro(){
        SettingsData.saveSettingInt(this, SettingKey.INTRO_STATUS, 1)
        //Ritorna all'activity manager per decidere cosa fare
        startActivity(Intent(this@AppIntroActivity, ActivityManager::class.java))
        finish()
    }


}
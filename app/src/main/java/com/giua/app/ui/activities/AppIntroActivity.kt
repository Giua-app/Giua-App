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
package com.giua.app.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.view.LayoutInflaterCompat
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroPageTransformerType
import com.giua.app.*
import com.giua.app.ui.fragments.intro.CustomSlideFragment
import com.giua.app.ui.fragments.intro.CustomSlidePolicy2Fragment
import com.giua.app.ui.fragments.intro.CustomSlidePolicyFragment
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.mikepenz.iconics.context.IconicsLayoutInflater2

class AppIntroActivity : AppIntro2(){

    var welcomeBack = false

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


        welcomeBack = intent.extras?.getBoolean("welcomeBack") == true //aiuto

        if(welcomeBack){
            addSlide(CustomSlideFragment.newInstance(
                "Bentornato!",
                "Segui questa breve introduzione per scoprire le novità di Giua App " + AppUpdateManager.getPrettyAppVersion(),
                backgroundDrawable = R.drawable.bg_intro_slide1,
                layoutResId = R.layout.fragment_appintro_customslide_1,
                gifRaw = R.raw.introv5
            ))
        } else {
            addSlide(CustomSlideFragment.newInstance(
                "Benvenuto!",
                "Segui questa breve introduzione per configurare Giua App",
                backgroundDrawable = R.drawable.bg_intro_slide1,
                layoutResId = R.layout.fragment_appintro_customslide_1,
                gifRaw = R.raw.introv5
            ))
        }

        addSlide(CustomSlideFragment.newInstance(
            "Dubbi? Clicca sugli Aiuti!",
            "In ogni schermata troverai un pulsante con l'icona {cmd_help_circle_outline}, cliccalo per scoprire come usare quella schermata",
            imageDrawable = R.mipmap.help_tutorial,
            backgroundDrawable = R.drawable.bg_intro_slide3,
            layoutResId = R.layout.fragment_appintro_customslide_2
        ))

        addSlide(CustomSlidePolicyFragment.newInstance(
            "Risparmio Batteria",
            "Disattiva il Risparmio Batteria per consentirci di inviarti notifiche sui nuovi compiti, verifiche, avvisi e tanto altro\n" +
                    "(l'app non può funzionare senza)",
            imageDrawable = R.mipmap.battery_tutorial,
            backgroundDrawable = R.drawable.bg_intro_slide4,
            layoutResId = R.layout.fragment_appintro_customslide_policy
        ))

        if(AutoStartPermissionHelper.getInstance().getAutoStartPermission(this, false)){
            addSlide(CustomSlidePolicy2Fragment.newInstance(
                "Notifiche",
                "Consenti all'app di avviarsi all'avvio per poter controllare le nuove notifiche.",
                imageDrawable = R.mipmap.battery_tutorial,
                backgroundDrawable = R.drawable.bg_intro_slide6,
                layoutResId = R.layout.fragment_appintro_customslide_policy_2,
                description2 = "L'app avvierà un processo separato per controllare le notifiche, non consuma molta batteria ma puoi comunque " +
                        "decidere di saltare questo passaggio, in questo caso l'app inizierà a controllare nuove notifiche automaticamente " +
                        "SOLO quando la aprirai tu per la prima volta dopo aver avviato il telefono."
            ))
        }

        //TODO: Inserire slide per scegliere la scuola

        addSlide(CustomSlideFragment.newInstance(
            "E' tutto pronto!",
            "Clicca \"FINE\" per passare alla pagina di login",
            imageDrawable = R.mipmap.fine_tutorial,
            backgroundDrawable = R.drawable.bg_intro_slide2,
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
        if(welcomeBack){
            AppData.saveIntroStatus(this, 2)
        } else {
            AppData.saveIntroStatus(this, 1)
        }

        //Ritorna all'activity manager per decidere cosa fare
        startActivity(Intent(this@AppIntroActivity, ActivityManager::class.java))
        finish()
    }


}
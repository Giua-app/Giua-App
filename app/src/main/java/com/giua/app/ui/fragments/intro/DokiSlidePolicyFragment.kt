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

package com.giua.app.ui.fragments.intro


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.appintro.SlidePolicy
import com.giua.app.R
import com.giua.app.ui.activities.CustomDokiActivity

class DokiSlidePolicyFragment : Fragment(), SlidePolicy {

    private var dokiViewed = false

    private lateinit var button: Button
    private lateinit var title: TextView
    private lateinit var description: TextView
    private lateinit var image: ImageView
    private lateinit var layout: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? = inflater.inflate(R.layout.fragment_doki_slidepolicy, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button = view.findViewById(R.id.intro_slidepolicy_button)
        title = view.findViewById(R.id.title)
        description = view.findViewById(R.id.description)
        image = view.findViewById(R.id.image)
        layout = view.findViewById(R.id.constraint_layout_slidepolicy)

        //Background
        layout.background = ResourcesCompat.getDrawable(resources, R.drawable.intro_back_slide9, null)
        //Pulsante istruzioni
        button.setOnClickListener {
            dokiViewed = true
            Toast.makeText(
                requireContext(),
                "Segui le istruzioni prima di chiudere!",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(activity, CustomDokiActivity::class.java))
        }

        title.text = "Ottimizzazione batteria"

        description.text = Html.fromHtml("<p> " +
                "Per poter ricevere notifiche dal registro devi disattivare l'ottimizzazione batteria </p>" +
                "<p> <b>Clicca il pulsante qua sotto per vedere le istruzioni, poi seguile fino a disattivare l'ottimizzazione, e infine torna qui per andare avanti </b></p>",0)

        image.setImageDrawable(ResourcesCompat.getDrawable(resources, R.mipmap.battery_tutorial, null))
    }

    //getter per dokiViewed, richiamato da SlidePolicy
    override val isPolicyRespected: Boolean
        get() = dokiViewed

    override fun onUserIllegallyRequestedNextPage() {
        //Eseguito quando isPolicyRespected è false
        Toast.makeText(
            requireContext(),
            "Leggi le istruzioni e disattiva l'ottimz. batteria!",
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        fun newInstance(): DokiSlidePolicyFragment {
            return DokiSlidePolicyFragment()
        }
    }
}
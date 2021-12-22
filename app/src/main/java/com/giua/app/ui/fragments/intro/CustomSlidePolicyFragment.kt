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
import android.provider.Settings
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.appintro.SlidePolicy
import com.giua.app.R

class CustomSlidePolicyFragment(
    var title: String?,
    var description: String?,
    @DrawableRes var imageDrawable: Int,
    @LayoutRes var layoutResId: Int,
    @DrawableRes var backgroundDrawable: Int
) : Fragment(), SlidePolicy {

    private var viewed = false

    private lateinit var yesButton: Button
    private lateinit var noButton: Button
    private lateinit var vTitle: TextView
    private lateinit var vDescription: TextView
    private lateinit var image: ImageView
    private lateinit var layout: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? = inflater.inflate(layoutResId, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        yesButton = view.findViewById(R.id.button_yes)
        noButton = view.findViewById(R.id.button_no)
        vTitle = view.findViewById(R.id.title)
        vDescription = view.findViewById(R.id.description)
        image = view.findViewById(R.id.image)
        layout = view.findViewById(R.id.constraint_layout)

        //Background
        layout.background = ResourcesCompat.getDrawable(resources, backgroundDrawable, null)

        yesButton.text = "Apri Risp. Batt."
        noButton.text = "No grazie"

        //Pulsante istruzioni
        yesButton.setOnClickListener {
            viewed = true

            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }

        noButton.setOnClickListener {
            viewed = true
            Toast.makeText(
                requireContext(),
                "Ora puoi andare avanti!",
                Toast.LENGTH_SHORT
            ).show()
        }

        vTitle.text = title

        vDescription.text = Html.fromHtml(description,0)

        image.setImageDrawable(ResourcesCompat.getDrawable(resources, imageDrawable, null))
    }

    //getter per viewed, richiamato da SlidePolicy
    override val isPolicyRespected: Boolean
        get() = viewed

    override fun onUserIllegallyRequestedNextPage() {
        //Eseguito quando isPolicyRespected è false
        Toast.makeText(
            requireContext(),
            "Leggi le istruzioni e disattiva l'ottimz. batteria!",
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        fun newInstance(
            title: String?,
            description: String?,
            @DrawableRes imageDrawable: Int = -1,
            @LayoutRes layoutResId: Int,
            @DrawableRes backgroundDrawable: Int
        ): CustomSlidePolicyFragment {
            return CustomSlidePolicyFragment(
                title = title,
                description = description,
                imageDrawable = imageDrawable,
                backgroundDrawable = backgroundDrawable,
                layoutResId = layoutResId
            )
        }
    }
}
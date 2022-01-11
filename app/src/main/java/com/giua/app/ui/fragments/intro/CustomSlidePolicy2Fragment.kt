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

package com.giua.app.ui.fragments.intro


import android.annotation.SuppressLint
import android.os.Bundle
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
import com.judemanutd.autostarter.AutoStartPermissionHelper

class CustomSlidePolicy2Fragment(
    var title: String?,
    var description: String?,
    var description2: String?,
    @DrawableRes var imageDrawable: Int,
    @LayoutRes var layoutResId: Int,
    @DrawableRes var backgroundDrawable: Int
) : Fragment(), SlidePolicy {

    private var viewed = false
    private lateinit var yesButton: Button
    private lateinit var noButton: Button
    private lateinit var vTitle: TextView
    private lateinit var vDescription: TextView
    private lateinit var vDescription2: TextView
    private lateinit var image: ImageView
    private lateinit var layout: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? = inflater.inflate(layoutResId, container, false)

    @SuppressLint("SetTextI18n", "BatteryLife")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        yesButton = view.findViewById(R.id.button_yes)
        noButton = view.findViewById(R.id.button_no)
        vTitle = view.findViewById(R.id.title)
        vDescription = view.findViewById(R.id.description)
        vDescription2 = view.findViewById(R.id.description2)
        image = view.findViewById(R.id.image)
        layout = view.findViewById(R.id.constraint_layout)

        //Background
        layout.background = ResourcesCompat.getDrawable(resources, backgroundDrawable, null)

        yesButton.text = "Apri Impostazioni"
        noButton.text = "No grazie"

        vTitle.text = title
        view.findViewById<TextView>(R.id.txt_csp_icon).text = "{cmd_checkbox_marked_circle_outline}"

        vDescription.text = Html.fromHtml(description,0)
        vDescription2.text = Html.fromHtml(description2,0)

        image.setImageDrawable(ResourcesCompat.getDrawable(resources, imageDrawable, null))


        yesButton.setOnClickListener {
            AutoStartPermissionHelper.getInstance().getAutoStartPermission(requireContext(), true)
            Toast.makeText(
                requireContext(),
                "Abilita l'avvio automatico a Giua App!",
                Toast.LENGTH_SHORT
            ).show()
            viewed = true
            yesButton.visibility = View.INVISIBLE
            noButton.visibility = View.INVISIBLE
            view.findViewById<TextView>(R.id.txt_csp_icon).visibility = View.VISIBLE
        }



        noButton.setOnClickListener {
            viewed = true
            yesButton.visibility = View.INVISIBLE
            noButton.visibility = View.INVISIBLE
            view.findViewById<TextView>(R.id.txt_csp_icon).visibility = View.VISIBLE
        }



    }

    //getter per viewed, richiamato da SlidePolicy
    override val isPolicyRespected: Boolean
        get() = viewed

    override fun onUserIllegallyRequestedNextPage() {
        //Eseguito quando isPolicyRespected è false
        Toast.makeText(
            requireContext(),
            "Leggi le istruzioni!",
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        fun newInstance(
            title: String?,
            description: String?,
            description2: String?,
            @DrawableRes imageDrawable: Int = -1,
            @LayoutRes layoutResId: Int,
            @DrawableRes backgroundDrawable: Int
        ): CustomSlidePolicy2Fragment {
            return CustomSlidePolicy2Fragment(
                title = title,
                description = description,
                description2 = description2,
                imageDrawable = imageDrawable,
                backgroundDrawable = backgroundDrawable,
                layoutResId = layoutResId
            )
        }
    }
}
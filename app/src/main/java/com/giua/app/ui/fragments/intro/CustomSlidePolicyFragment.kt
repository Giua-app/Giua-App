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
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
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
    private lateinit var vTitle: TextView
    private lateinit var vDescription: TextView
    private lateinit var image: ImageView
    private lateinit var layout: ConstraintLayout
    private lateinit var powerManager: PowerManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? = inflater.inflate(layoutResId, container, false)

    @SuppressLint("SetTextI18n", "BatteryLife")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        yesButton = view.findViewById(R.id.button_yes)
        vTitle = view.findViewById(R.id.title)
        vDescription = view.findViewById(R.id.description)
        image = view.findViewById(R.id.image)
        layout = view.findViewById(R.id.constraint_layout)
        powerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager

        //Background
        layout.background = ResourcesCompat.getDrawable(resources, backgroundDrawable, null)

        yesButton.text = "Apri Risp. Batt."

        vTitle.text = title

        vDescription.text = Html.fromHtml(description,0)

        image.setImageDrawable(ResourcesCompat.getDrawable(resources, imageDrawable, null))


        if (powerManager.isIgnoringBatteryOptimizations(requireActivity().packageName)) {
            yesButton.text = "{cmd_checkbox_marked_circle_outline}"
            yesButton.textSize = 36F
            yesButton.background = null
        } else {
            yesButton.setOnClickListener {
                //AutoStartPermissionHelper.getInstance().getAutoStartPermission(requireActivity(), true)
                startActivity(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(
                        Uri.parse("package:" + requireActivity().packageName)
                    )
                )
                Toast.makeText(
                    requireActivity(),
                    "Disattiva il risparmio batteria!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }




    }

    //getter per viewed, richiamato da SlidePolicy
    override val isPolicyRespected: Boolean
        get() = powerManager.isIgnoringBatteryOptimizations(requireActivity().packageName)

    override fun onUserIllegallyRequestedNextPage() {
        //Eseguito quando isPolicyRespected è false
        Toast.makeText(
            requireActivity(),
            "Leggi le istruzioni!",
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
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
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.RawRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.giua.app.R


class CustomSlideFragment(
    var title: String?,
    var description: String?,
    @DrawableRes var imageDrawable: Int,
    @RawRes var gifRaw: Int,
    @LayoutRes var layoutResId: Int,
    @DrawableRes var backgroundDrawable: Int
) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? = inflater.inflate(layoutResId, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val titleView: TextView = view.findViewById(R.id.title)
        val descriptionView: TextView = view.findViewById(R.id.description)
        val imageView: ImageView = view.findViewById(R.id.image)
        val layout: ConstraintLayout = view.findViewById(R.id.constraint_layout)

        if(gifRaw != -1){
            Glide.with(this).asGif().load(gifRaw).into(imageView)
        } else {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(resources, imageDrawable, null))
        }

        //Background
        layout.background = ResourcesCompat.getDrawable(resources, backgroundDrawable, null)

        titleView.text = title

        descriptionView.text = Html.fromHtml(description, 0)


    }

    companion object {
        fun newInstance(
            title: String?,
            description: String?,
            @DrawableRes imageDrawable: Int = -1,
            @RawRes gifRaw: Int = -1,
            @LayoutRes layoutResId: Int ,
            @DrawableRes backgroundDrawable: Int
        ): CustomSlideFragment {
            return CustomSlideFragment(
                title = title,
                description = description,
                imageDrawable = imageDrawable,
                backgroundDrawable = backgroundDrawable,
                layoutResId = layoutResId,
                gifRaw = gifRaw
            )
        }
    }
}
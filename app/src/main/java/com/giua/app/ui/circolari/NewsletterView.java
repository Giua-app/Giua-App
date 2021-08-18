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

package com.giua.app.ui.circolari;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.giua.app.R;
import com.giua.objects.Newsletter;

import org.jetbrains.annotations.NotNull;

public class NewsletterView extends ConstraintLayout {
    Newsletter newsletter;
    TextView tvStatus;
    TextView tvNumberID;
    TextView tvDate;
    TextView tvObject;
    ImageView ivNotRead;
    ImageButton btnDocument;
    ImageButton btnAttachments;
    Runnable clickDocument;
    Runnable clickAttachment;

    public NewsletterView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, Newsletter newsletter, Runnable clickDocument, Runnable clickAttachment) {
        super(context, attrs);

        this.newsletter = newsletter;
        this.clickDocument = clickDocument;
        this.clickAttachment = clickAttachment;

        initializeComponent(context);
    }

    private void initializeComponent(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.newsletter_view, this);

        tvStatus = findViewById(R.id.newsletter_status_text_view);
        tvNumberID = findViewById(R.id.newsletter_numberid_text_view);
        tvDate = findViewById(R.id.newsletter_date_text_view);
        tvObject = findViewById(R.id.newsletter_object_text_view);
        ivNotRead = findViewById(R.id.newsletter_view_left_image);
        btnDocument = findViewById(R.id.newsletter_view_btn_document);
        btnAttachments = findViewById(R.id.newsletter_view_btn_attachment);

        btnDocument.setOnClickListener((view) -> clickDocument.run());
        if (newsletter.attachments != null) {   //Se cè almeno un allegato metti l'onlick nell'immagine
            btnAttachments.setOnClickListener((view) -> clickAttachment.run());
        } else {    //Se non ce ne sono metti l'immagine un po trasparente e di colore grigio
            btnAttachments.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.non_vote, context.getTheme())));
            btnAttachments.setAlpha(0.3f);
        }

        if (!newsletter.isRead()) {
            tvStatus.setText("Da leggere");
            tvStatus.setTypeface(tvStatus.getTypeface(), Typeface.BOLD);
            tvDate.setTypeface(tvDate.getTypeface(), Typeface.BOLD);
            tvNumberID.setTypeface(tvNumberID.getTypeface(), Typeface.BOLD);
            tvObject.setTypeface(tvObject.getTypeface(), Typeface.BOLD);
            ivNotRead.setVisibility(VISIBLE);
        } else {
            tvStatus.setText("Letta");
        }

        tvNumberID.setText("n." + newsletter.number);
        tvDate.setText(newsletter.date);
        tvObject.setText(newsletter.newslettersObject);
    }
}

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

package com.giua.app.ui.fragments.newsletters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.giua.app.R;
import com.giua.objects.Newsletter;

import org.jetbrains.annotations.NotNull;

public class NewsletterView extends ConstraintLayout {
    public Newsletter newsletter;
    public float normalTranslationX = 0;
    private boolean isMoved = false;
    public float offset = 0f; //Usato per fare lo scorrimento
    public View upperView;
    private Context context;

    public NewsletterView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, Newsletter newsletter) {
        super(context, attrs);

        this.newsletter = newsletter;
        this.context = context;

        initializeComponent(context);
    }

    private void initializeComponent(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_newsletter, this);

        LinearLayout hiddenLayout = findViewById(R.id.newsletter_left_layout);
        TextView hiddenLayoutText = findViewById(R.id.newsletter_left_text);
        TextView tvStatus = findViewById(R.id.newsletter_status_text_view);
        TextView tvNumberID = findViewById(R.id.newsletter_numberid_text_view);
        TextView tvDate = findViewById(R.id.newsletter_date_text_view);
        TextView tvObject = findViewById(R.id.newsletter_object_text_view);
        ImageView ivNotRead = findViewById(R.id.newsletter_view_left_image);

        if (!newsletter.isRead()) {
            hiddenLayout.setBackground(ResourcesCompat.getDrawable(getResources(), R.color.bad_vote_lighter, context.getTheme()));
            hiddenLayoutText.setText("Segna come già letta");
            tvStatus.setText("Da leggere");
            tvStatus.setTypeface(tvStatus.getTypeface(), Typeface.BOLD);
            tvDate.setTypeface(tvDate.getTypeface(), Typeface.BOLD);
            tvNumberID.setTypeface(tvNumberID.getTypeface(), Typeface.BOLD);
            tvObject.setTypeface(tvObject.getTypeface(), Typeface.BOLD);
            ivNotRead.setVisibility(VISIBLE);
        } else {
            hiddenLayout.setBackground(ResourcesCompat.getDrawable(getResources(), R.color.non_vote_lighter, context.getTheme()));
            hiddenLayoutText.setText("Già letta");
            tvStatus.setText("Letta");
        }

        tvNumberID.setText("n." + newsletter.number);
        tvDate.setText(newsletter.date);
        tvObject.setText(newsletter.newslettersObject);

        upperView = findViewById(R.id.newsletter_view);
    }

    public void refreshView() {
        LinearLayout hiddenLayout = findViewById(R.id.newsletter_left_layout);
        TextView hiddenLayoutText = findViewById(R.id.newsletter_left_text);
        TextView tvStatus = findViewById(R.id.newsletter_status_text_view);
        TextView tvNumberID = findViewById(R.id.newsletter_numberid_text_view);
        TextView tvDate = findViewById(R.id.newsletter_date_text_view);
        TextView tvObject = findViewById(R.id.newsletter_object_text_view);
        ImageView ivNotRead = findViewById(R.id.newsletter_view_left_image);
        Typeface typeface = ResourcesCompat.getFont(context, R.font.varelaroundregular);

        if (!newsletter.isRead()) {
            hiddenLayout.setBackground(ResourcesCompat.getDrawable(getResources(), R.color.bad_vote_lighter, context.getTheme()));
            hiddenLayoutText.setText("Segna come già letta");
            tvStatus.setText("Da leggere");
            tvStatus.setTypeface(typeface, Typeface.BOLD);
            tvDate.setTypeface(typeface, Typeface.BOLD);
            tvNumberID.setTypeface(typeface, Typeface.BOLD);
            tvObject.setTypeface(typeface, Typeface.BOLD);
            ivNotRead.setVisibility(VISIBLE);
        } else {
            hiddenLayout.setBackground(ResourcesCompat.getDrawable(getResources(), R.color.non_vote_lighter, context.getTheme()));
            hiddenLayoutText.setText("Già letta");
            tvStatus.setText("Letta");
            tvStatus.setTypeface(typeface, Typeface.NORMAL);
            tvDate.setTypeface(typeface, Typeface.NORMAL);
            tvNumberID.setTypeface(typeface, Typeface.NORMAL);
            tvObject.setTypeface(typeface, Typeface.NORMAL);
            ivNotRead.setVisibility(GONE);
        }

        tvNumberID.setText("n." + newsletter.number);
        tvDate.setText(newsletter.date);
        tvObject.setText(newsletter.newslettersObject);

        findViewById(R.id.newsletter_view_btn_document).setOnTouchListener(this::docsAttachmentsOnTouch);
        findViewById(R.id.newsletter_view_btn_attachment).setOnTouchListener(this::docsAttachmentsOnTouch);

        upperView = findViewById(R.id.newsletter_view);
    }

    private boolean docsAttachmentsOnTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            setClickable(false);
        } else
            setClickable(true);
        return false;
    }

    public float getNormalTranslationX() {
        return normalTranslationX;
    }

    public void moveTo(float x) {
        if (!isMoved) {
            normalTranslationX = upperView.getTranslationX();
            isMoved = true;
        }
        if (x >= normalTranslationX)
            upperView.setTranslationX(x);
    }

    public void resetPosition() {
        offset = 0;
        upperView.setTranslationX(normalTranslationX);
    }

    public void markAsRead() {
        newsletter.markAsRead();
        refreshView();
    }
}

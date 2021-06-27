package com.giua.app.ui.circolari;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
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
    ImageView imageView;

    public NewsletterView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, Newsletter newsletter) {
        super(context, attrs);

        this.newsletter = newsletter;

        initializeComponent(context);
    }

    private void initializeComponent(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.newsletter_view, this);

        tvStatus = findViewById(R.id.newsletter_status_text_view);
        tvNumberID = findViewById(R.id.newsletter_numberid_text_view);
        tvDate = findViewById(R.id.newsletter_date_text_view);
        tvObject = findViewById(R.id.newsletter_object_text_view);
        imageView = findViewById(R.id.newsletter_view_left_image);

        if (!newsletter.isRead()) {
            tvStatus.setText("Da leggere");
            tvStatus.setTypeface(tvStatus.getTypeface(), Typeface.BOLD);
            tvDate.setTypeface(tvDate.getTypeface(), Typeface.BOLD);
            tvNumberID.setTypeface(tvNumberID.getTypeface(), Typeface.BOLD);
            tvObject.setTypeface(tvObject.getTypeface(), Typeface.BOLD);
            imageView.setVisibility(VISIBLE);
        } else {
            tvStatus.setText("Letta");
        }

        tvNumberID.setText("n." + newsletter.number);
        tvDate.setText(newsletter.date);
        tvObject.setText(newsletter.newslettersObject);
    }


}

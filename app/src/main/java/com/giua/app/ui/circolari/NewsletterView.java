package com.giua.app.ui.circolari;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.giua.app.R;
import com.giua.objects.Newsletter;

import org.jetbrains.annotations.NotNull;

public class NewsletterView extends ConstraintLayout {
    Newsletter newsletter;
    TextView tvStatus;
    TextView tvNumberID;
    TextView tvDate;
    TextView tvObject;

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

        if (!newsletter.isRead()) {
            tvStatus.setText("Da leggere");
            tvStatus.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.middle_vote, context.getTheme()));
        } else {
            tvStatus.setText("Letta");
        }

        tvNumberID.setText("n." + newsletter.number);
        tvDate.setText(newsletter.date);
        tvObject.setText(newsletter.newslettersObject);
    }


}

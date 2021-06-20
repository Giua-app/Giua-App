package com.giua.app.ui.circolari;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.giua.app.R;

import org.jetbrains.annotations.NotNull;

public class NewsletterView extends ConstraintLayout {
    String status;
    String numberID;
    String date;
    String object;
    String url;
    TextView tvStatus;
    TextView tvNumberID;
    TextView tvDate;
    TextView tvObject;

    public NewsletterView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, String status, String numberID, String date, String object, String url) {
        super(context, attrs);

        this.status = status;
        this.numberID = numberID;
        this.date = date;
        this.object = object;
        this.url = url;

        initializeComponent(context);
    }

    private void initializeComponent(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.newsletter_view, this);

        tvStatus = findViewById(R.id.newsletter_status_text_view);
        tvNumberID = findViewById(R.id.newsletter_numberid_text_view);
        tvDate = findViewById(R.id.newsletter_date_text_view);
        tvObject = findViewById(R.id.newsletter_object_text_view);

        if (status.equals("LETTA")) {
            tvStatus.setText("Letta");
            //IMPOSTA ANCHE IL COLORE DI BACKGROUND
        } else {
            tvStatus.setText("Da leggere");
            //IMPOSTA ANCHE IL COLORE DI BACKGROUND
        }

        tvNumberID.setText(numberID);
        tvDate.setText(date);
        tvObject.setText(object);
    }


}

package com.giua.app.ui.voti;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.giua.app.R;

import org.jetbrains.annotations.NotNull;

public class VoteView extends LinearLayout {
    String subjectValue;
    String voteValue;
    TextView tvVote;
    TextView tvSubject;
    float rawVote;

    public VoteView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, String subject, String vote, float rawVote) {
        super(context, attrs);

        this.subjectValue = subject;
        this.voteValue = vote;
        this.rawVote = rawVote;
        initializeComponent(context);
    }

    private void initializeComponent(Context context){
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.vote_component, this);

        tvSubject = findViewById(R.id.text_view_subject);
        tvVote = findViewById(R.id.text_view_vote);

        tvSubject.setText(this.subjectValue);
        tvVote.setText(this.voteValue);

        if(rawVote == -1f){
            tvVote.setBackgroundTintList(getResources().getColorStateList(R.color.non_vote, context.getTheme()));
        } else if(rawVote >= 6f){
            tvVote.setBackgroundTintList(getResources().getColorStateList(R.color.good_vote, context.getTheme()));
        } else if(rawVote < 6f && rawVote >= 5){
            tvVote.setBackgroundTintList(getResources().getColorStateList(R.color.middle_vote, context.getTheme()));
        } else if(rawVote < 5){
            tvVote.setBackgroundTintList(getResources().getColorStateList(R.color.bad_vote, context.getTheme()));
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        addView(tvSubject);
        addView(tvVote);
    }
}

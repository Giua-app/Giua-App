package com.giua.app.ui.voti;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.giua.app.R;
import com.giua.objects.Vote;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VoteView extends LinearLayout {
    String subjectValue;
    String voteFirstQuarter;
    float rawVoteFirstQuarter;
    String voteSecondQuarter;
    float rawVoteSecondQuarter;
    TextView tvVoteFisrtQuarter;
    TextView tvVoteSecondQuarter;
    TextView tvSubject;

    public VoteView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, String subject, String voteFirstQuarter, float rawVoteFirstQuarter, String voteSecondQuarter, float rawVoteSecondQuarter) {
        super(context, attrs);

        this.subjectValue = subject;
        this.voteFirstQuarter = voteFirstQuarter;
        this.rawVoteFirstQuarter = rawVoteFirstQuarter;
        this.voteSecondQuarter = voteSecondQuarter;
        this.rawVoteSecondQuarter = rawVoteSecondQuarter;
        initializeComponent(context);
    }

    private void initializeComponent(Context context){
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.vote_component, this);

        tvSubject = findViewById(R.id.text_view_subject);
        tvVoteFisrtQuarter = findViewById(R.id.text_view_vote_primo_quadrimestre);
        tvVoteSecondQuarter = findViewById(R.id.text_view_vote_secondo_quadrimestre);

        tvSubject.setText(this.subjectValue);
        tvVoteFisrtQuarter.setText(this.voteFirstQuarter);
        tvVoteSecondQuarter.setText(this.voteSecondQuarter);

        tvVoteFisrtQuarter.setBackgroundTintList(getColorFromVote(rawVoteFirstQuarter));
        tvVoteSecondQuarter.setBackgroundTintList(getColorFromVote(rawVoteSecondQuarter));
    }

    private ColorStateList getColorFromVote(float vote){
        if(vote == -1f){
            return getResources().getColorStateList(R.color.non_vote, getContext().getTheme());
        } else if(vote >= 6f){
            return getResources().getColorStateList(R.color.good_vote, getContext().getTheme());
        } else if(vote < 6f && vote >= 5){
            return getResources().getColorStateList(R.color.middle_vote, getContext().getTheme());
        } else if(vote < 5){
            return getResources().getColorStateList(R.color.bad_vote, getContext().getTheme());
        }
        return getResources().getColorStateList(R.color.non_vote, getContext().getTheme()); //Non si dovrebbe mai verificare
    }
}

package com.giua.app.ui.voti;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

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
    LinearLayout listVoteLayout1;
    LinearLayout listVoteLayout2;
    List<Vote> allVotes;

    public VoteView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, String subject, String voteFirstQuarter, float rawVoteFirstQuarter, String voteSecondQuarter, float rawVoteSecondQuarter, List<Vote> allVotes) {
        super(context, attrs);

        this.subjectValue = subject;
        this.voteFirstQuarter = voteFirstQuarter;
        this.rawVoteFirstQuarter = rawVoteFirstQuarter;
        this.voteSecondQuarter = voteSecondQuarter;
        this.rawVoteSecondQuarter = rawVoteSecondQuarter;
        this.allVotes = allVotes;
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

        listVoteLayout1 = findViewById(R.id.list_vote_linear_layout_1);
        listVoteLayout2 = findViewById(R.id.list_vote_linear_layout_2);

        LinearLayout.LayoutParams singleVoteParams = new LinearLayout.LayoutParams(95, ViewGroup.LayoutParams.WRAP_CONTENT);
        singleVoteParams.setMargins(20,0,0,0);

        for(Vote vote : allVotes){
            TextView tvVote = new TextView(getContext(), null);

            if(!vote.isAsterisk)
                tvVote.setText(vote.value);
            else
                tvVote.setText("*");
            tvVote.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvVote.setTypeface(ResourcesCompat.getFont(getContext(), R.font.varela_round_regular));
            tvVote.setId(View.generateViewId());
            tvVote.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.single_vote_style));
            tvVote.setBackgroundTintList(getColorFromVote(getNumberFromVote(vote)));
            tvVote.setTextSize(18f);
            tvVote.setLayoutParams(singleVoteParams);
            tvVote.setPadding(5,5,5,5);

            if(vote.isFirstQuarterly)
                listVoteLayout1.addView(tvVote);
            else
                listVoteLayout2.addView(tvVote);
        }
    }

    private float getNumberFromVote(Vote vote) {
        if(vote.isAsterisk)
            return -1f;

        char lastChar = vote.value.charAt(vote.value.length() - 1);
        if (lastChar == '+')
            return (vote.value.length() == 2) ? Character.getNumericValue(vote.value.charAt(0)) + 0.15f : Integer.parseInt(vote.value.substring(0, 2)) + 0.15f;

        else if (lastChar == '-')
            return (vote.value.length() == 2) ? Character.getNumericValue(vote.value.charAt(0)) - 1 + 0.85f : Integer.parseInt(vote.value.substring(0, 2)) - 1 + 0.85f;

        else if (lastChar == '½')
            return (vote.value.length() == 2) ? Character.getNumericValue(vote.value.charAt(0)) + 0.5f : Integer.parseInt(vote.value.substring(0, 2)) + 0.5f;

        else
            return Integer.parseInt(vote.value);
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

package com.giua.app.ui.voti;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.giua.app.DrawerActivity;
import com.giua.app.R;
import com.giua.objects.Vote;
import com.giua.webscraper.GiuaScraper;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class VotiFragment extends Fragment {

    GiuaScraper gS;
    VoteView voteView;
    LinearLayout mainLayout;
    LinearLayout voteListLayout;
    LinearLayout listVoteScrollView1;       //Scroll view del primo quadrimestre
    LinearLayout listVoteScrollView2;       //Scroll view del secondo quadrimestre
    TextView tvSecondQuarter;
    List<VoteView> allVoteView;
    LinearLayout.LayoutParams params;
    DecimalFormat df = new DecimalFormat("0.0");
    ConstraintLayout constraintLayout;
    ConstraintSet constraintSet;
    boolean isListVoteOpened = false;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_voti, container, false);

        Intent intent = getActivity().getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");
        mainLayout = root.findViewById(R.id.vote_fragment_linear_layout);
        listVoteScrollView1 = root.findViewById(R.id.list_vote_linear_layout_1);
        listVoteScrollView2 = root.findViewById(R.id.list_vote_linear_layout_2);
        constraintLayout = root.findViewById(R.id.vote_fragment_constraint_layout);
        tvSecondQuarter = root.findViewById(R.id.list_votes_text_view_2);
        voteListLayout = root.findViewById(R.id.list_vote_layout);
        constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        Map<String, List<Vote>> allVotes = gS.getAllVotes(false);

        float meanSecondQuarter;
        float meanFirstQuarter;     //media aritmetica dei voti
        int voteCounterFirstQuarter;     //Conta solamente i voti che ci sono e non gli asterischi
        int voteCounterSecondQuarter;

        allVoteView = new Vector<>();
        params = new LinearLayout.LayoutParams(mainLayout.getLayoutParams().width, mainLayout.getLayoutParams().height);
        params.setMargins(10, 20, 0, 30);

        for(String subject: allVotes.keySet()){     //Cicla ogni materia
            meanFirstQuarter = 0f;
            meanSecondQuarter = 0f;
            voteCounterFirstQuarter = 0;     //Conta solamente i voti che ci sono e non gli asterischi
            voteCounterSecondQuarter = 0;

            for(Vote vote: allVotes.get(subject)){      //Cicla ogni voto della materia
                if(vote.value.length() > 0 && vote.isFirstQuarterly){
                    meanFirstQuarter += getNumberFromVote(vote);
                    voteCounterFirstQuarter++;
                } else if(vote.value.length() > 0) {
                    meanSecondQuarter += getNumberFromVote(vote);
                    voteCounterSecondQuarter++;
                }
            }

            if(voteCounterFirstQuarter != 0 && voteCounterSecondQuarter != 0) {
                meanFirstQuarter /= voteCounterFirstQuarter;
                meanSecondQuarter /= voteCounterSecondQuarter;
                addVoteView(subject, df.format(meanFirstQuarter), meanFirstQuarter, df.format(meanSecondQuarter), meanSecondQuarter);
            } else{
                if(voteCounterFirstQuarter == 0 && voteCounterSecondQuarter != 0) {
                    meanSecondQuarter /= voteCounterSecondQuarter;
                    addVoteView(subject, "/", -1f, df.format(meanSecondQuarter), meanSecondQuarter);
                } else if(voteCounterFirstQuarter != 0){
                    meanFirstQuarter /= voteCounterFirstQuarter;
                    addVoteView(subject, df.format(meanFirstQuarter), meanFirstQuarter, "/", -1f);
                } else {
                    addVoteView(subject, "/", -1f, "/", -1f);
                }
            }

        }

        return root;
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

    @SuppressLint("ClickableViewAccessibility")
    private void addVoteView(String subject, String voteFirstQuart, float rawVoteFirstQuart, String voteSecondQuart, float rawVoteSecondQuart){
        voteView = new VoteView(getContext(), null, subject, voteFirstQuart, rawVoteFirstQuart, voteSecondQuart, rawVoteSecondQuart, gS.getAllVotes(false).get(subject));
        voteView.setId(View.generateViewId());

        voteView.setLayoutParams(params);

        //TODO: Add on click effect that work

        allVoteView.add(voteView);
        mainLayout.addView(voteView);
    }
}
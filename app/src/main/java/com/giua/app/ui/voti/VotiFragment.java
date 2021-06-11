package com.giua.app.ui.voti;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.giua.app.R;
import com.giua.objects.Vote;
import com.giua.webscraper.GiuaScraper;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class VotiFragment extends Fragment {

    GiuaScraper gS;
    TextView text;
    VoteView voteView;
    LinearLayout mainLayout;
    ScrollView scrollView;
    List<VoteView> allVoteView;
    LinearLayout.LayoutParams params;
    DecimalFormat df = new DecimalFormat("0.0");

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_voti, container, false);

        Intent intent = getActivity().getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");
        text = root.findViewById(R.id.textView);
        mainLayout = root.findViewById(R.id.vote_fragment_constraint_layout);
        scrollView = root.findViewById(R.id.vote_fragment_scroll_view);

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
        char lastChar = vote.value.charAt(vote.value.length() - 1);
        if (lastChar == '+') {
            return (vote.value.length() == 2) ? Character.getNumericValue(vote.value.charAt(0)) + 0.15f : Integer.parseInt(vote.value.substring(0, 2)) + 0.15f;

        } else if (lastChar == '-') {
            return (vote.value.length() == 2) ? Character.getNumericValue(vote.value.charAt(0)) - 1 + 0.85f : Integer.parseInt(vote.value.substring(0, 2)) - 1 + 0.85f;

        } else if (lastChar == '½') {
            return (vote.value.length() == 2) ? Character.getNumericValue(vote.value.charAt(0)) + 0.5f : Integer.parseInt(vote.value.substring(0, 2)) + 0.5f;

        } else {
            return Integer.parseInt(vote.value);

        }
    }

    private void addVoteView(String subject, String voteFirstQuart, float rawVoteFirstQuart, String voteSecondQuart, float rawVoteSecondQuart){
        voteView = new VoteView(getContext(), null, subject, voteFirstQuart, rawVoteFirstQuart, voteSecondQuart, rawVoteSecondQuart);
        voteView.setId(View.generateViewId());

        voteView.setLayoutParams(params);

        voteView.setOnClickListener(view -> {
            mainLayout.removeView(view);
            mainLayout.addView(view, 0);
        });

        allVoteView.add(voteView);
        mainLayout.addView(voteView);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        /*Map<String, List<Vote>> allVotes = gS.getAllVotes(false);

        for(String m: allVotes.keySet()){
            text.setText(text.getText().toString() + " - " + allVotes.get(m).toString());
        }*/
    }

}
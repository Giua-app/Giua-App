package com.giua.app.ui.voti;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;

import com.giua.app.R;
import com.giua.objects.Vote;
import com.giua.webscraper.GiuaScraper;

import java.text.AttributedCharacterIterator;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

public class VotiFragment extends Fragment {

    GiuaScraper gS;
    TextView text;
    VoteView voteView;
    LinearLayout mainLayout;
    List<VoteView> allVoteView;
    LinearLayout.LayoutParams params;
    private DecimalFormat df = new DecimalFormat("0.0");

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_voti, container, false);

        Intent intent = getActivity().getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");
        text = root.findViewById(R.id.textView);
        mainLayout = root.findViewById(R.id.main_constraint_layout);

        Map<String, List<Vote>> allVotes = gS.getAllVotes(false);

        float mean;     //media aritmetica dei voti

        allVoteView = new Vector<>();
        params = new LinearLayout.LayoutParams(mainLayout.getLayoutParams().width, mainLayout.getLayoutParams().height);
        params.setMargins(10, 20, 0, 30);

        for(String subject: allVotes.keySet()){     //Cicla ogni materia
            mean = 0f;
            int voteCounter = 0;     //Conta solamente i voti che ci sono e non gli asterischi

            for(Vote vote: allVotes.get(subject)){      //Cicla ogni voto della materia
                if(vote.value.length() > 0 && !vote.isFirstQuarterly){      //TODO: se il secondo quadrimestre e partito fa la media solo su quelli altrimenti fa la media solo sui voti del primo
                    mean += getNumberFromVote(vote);
                    voteCounter++;
                }
            }

            if(voteCounter != 0) {
                mean /= voteCounter;
                addVoteView(subject, df.format(mean), mean);
            } else{
                addVoteView(subject, "/", -1f);
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

    private void addVoteView(String subject, String vote, float rawVote){
        voteView = new VoteView(getContext(), null, subject, vote, rawVote);
        voteView.setId(View.generateViewId());
        voteView.setLayoutParams(params);

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
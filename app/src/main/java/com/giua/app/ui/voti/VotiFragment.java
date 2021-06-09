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

import java.util.List;
import java.util.Map;
import java.util.Vector;

public class VotiFragment extends Fragment {

    GiuaScraper gS;
    TextView text;
    VoteView voteView;
    ConstraintLayout mainLayout;
    ConstraintSet constraintSet;
    List<VoteView> allVoteView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_voti, container, false);

        Intent intent = getActivity().getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");
        text = root.findViewById(R.id.textView);
        mainLayout = root.findViewById(R.id.main_constraint_layout);

        Map<String, List<Vote>> allVotes = gS.getAllVotes(false);

        float mean;     //media aritmetica dei voti

        constraintSet = new ConstraintSet();
        constraintSet.clone(mainLayout);
        allVoteView = new Vector<>();

        for(String subject: allVotes.keySet()){
            mean = 0f;

            for(Vote vote: allVotes.get(subject)){
                if(vote.value.length() > 0){
                    if(vote.value.length() > 1){
                        if(vote.value.charAt(1) == '+'){
                            mean += Character.getNumericValue(vote.value.charAt(0));
                            mean += 0.15f;
                        } else if(vote.value.charAt(1) == '-'){
                            mean += Character.getNumericValue(vote.value.charAt(0))-1;
                            mean += 0.75f;
                        } else if(vote.value.charAt(1) == '½'){
                            mean += Character.getNumericValue(vote.value.charAt(0));
                            mean += 0.5f;
                        }
                    } else {
                        mean += Integer.parseInt(vote.value);
                    }
                }
            }

            mean /= allVotes.get(subject).size();

            addVoteView(subject, Float.toString(mean));

        }

        for(int j = 0; j < allVoteView.size(); j++){
            //if(j == 0) {
                constraintSet.connect(allVoteView.get(j).getId(), ConstraintSet.TOP, mainLayout.getId(), ConstraintSet.TOP);
                constraintSet.connect(allVoteView.get(j).getId(), ConstraintSet.START, mainLayout.getId(), ConstraintSet.START);
                constraintSet.connect(allVoteView.get(j).getId(), ConstraintSet.END, mainLayout.getId(), ConstraintSet.END);
                constraintSet.connect(allVoteView.get(j).getId(), ConstraintSet.BOTTOM, mainLayout.getId(), ConstraintSet.BOTTOM);
            /*} else {
                constraintSet.connect(allVoteView.get(j).getId(), ConstraintSet.TOP, allVoteView.get(j-1).getId(), ConstraintSet.BOTTOM);
                constraintSet.connect(allVoteView.get(j).getId(), ConstraintSet.START, allVoteView.get(j-1).getId(), ConstraintSet.START);
                constraintSet.connect(allVoteView.get(j).getId(), ConstraintSet.END, allVoteView.get(j-1).getId(), ConstraintSet.END);
                constraintSet.connect(allVoteView.get(j).getId(), ConstraintSet.BOTTOM, mainLayout.getId(), ConstraintSet.BOTTOM);
            }*/

            mainLayout.addView(allVoteView.get(j));
        }

        constraintSet.applyTo(mainLayout);

        return root;
    }

    private void addVoteView(String subject, String vote){
        voteView = new VoteView(getContext(), null, subject, vote);
        voteView.setId(View.generateViewId());

        allVoteView.add(voteView);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        /*Map<String, List<Vote>> allVotes = gS.getAllVotes(false);

        for(String m: allVotes.keySet()){
            text.setText(text.getText().toString() + " - " + allVotes.get(m).toString());
        }*/
    }

}
package com.giua.app.ui.voti;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.giua.app.R;
import com.giua.objects.Vote;
import com.giua.webscraper.GiuaScraper;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class VotiFragment extends Fragment {

    GiuaScraper gS;
    ProgressBar progressBar;
    VoteView voteView;
    LinearLayout mainLayout;
    LinearLayout detailVoteLayout;
    LinearLayout.LayoutParams params;
    ImageButton obscureLayoutButton;    //Questo bottone viene triggerato viene visualizzato dietro al detail layout e se viene cliccato si esce dai dettaglic
    DecimalFormat df = new DecimalFormat("0.0");
    Map<String, List<Vote>> allVotes;
    Handler handler = new Handler();

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_voti, container, false);

        Intent intent = getActivity().getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");
        mainLayout = root.findViewById(R.id.vote_fragment_linear_layout);
        obscureLayoutButton = root.findViewById(R.id.obscure_layout_image_button);
        detailVoteLayout = root.findViewById(R.id.attachment_layout);
        progressBar = root.findViewById(R.id.vote_loading_page_bar);

        obscureLayoutButton.setOnClickListener(this::obscureButtonClick);

        generateAllViewsAsync();

        return root;
    }

    private void generateAllViewsAsync() {
        new Thread(() -> {
            allVotes = gS.getAllVotes(false);
            handler.post(this::generateAllViews);
        }).start();
    }

    private void generateAllViews() {
        float meanSecondQuarter;
        float meanFirstQuarter;     //media aritmetica dei voti
        int voteCounterFirstQuarter;     //Conta solamente i voti che ci sono e non gli asterischi
        int voteCounterSecondQuarter;

        params = new LinearLayout.LayoutParams(mainLayout.getLayoutParams().width, mainLayout.getLayoutParams().height);
        params.setMargins(10, 20, 0, 30);

        for (String subject : allVotes.keySet()) {     //Cicla ogni materia
            meanFirstQuarter = 0f;
            meanSecondQuarter = 0f;
            voteCounterFirstQuarter = 0;     //Conta solamente i voti che ci sono e non gli asterischi
            voteCounterSecondQuarter = 0;

            for (Vote vote : allVotes.get(subject)) {      //Cicla ogni voto della materia
                if (vote.value.length() > 0 && vote.isFirstQuarterly) {
                    meanFirstQuarter += getNumberFromVote(vote);
                    voteCounterFirstQuarter++;
                } else if (vote.value.length() > 0) {
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
        progressBar.setVisibility(View.GONE);
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

    public void obscureButtonClick(View view){
        detailVoteLayout.setVisibility(View.GONE);
        obscureLayoutButton.setVisibility(View.GONE);
    }

    private Drawable getDrawable(int id){
        return ResourcesCompat.getDrawable(getResources(), id, getContext().getTheme());
    }

    private ColorStateList getColorStateList(int id){
        return ResourcesCompat.getColorStateList(getResources(), id, getContext().getTheme());
    }

    private void onClickSingleVote(View view){
        Resources res = getResources();
        SingleVoteView _view = (SingleVoteView) view;
        TextView detailVoteDate = detailVoteLayout.findViewById(R.id.detail_vote_date);
        TextView detailVoteType = detailVoteLayout.findViewById(R.id.detail_vote_type);
        TextView detailVoteArguments = detailVoteLayout.findViewById(R.id.detail_vote_arguments);
        TextView detailVoteJudge = detailVoteLayout.findViewById(R.id.detail_vote_judge);
        detailVoteDate.setVisibility(View.GONE);
        detailVoteType.setVisibility(View.GONE);
        detailVoteArguments.setVisibility(View.GONE);
        detailVoteJudge.setVisibility(View.GONE);
        detailVoteLayout.setVisibility(View.VISIBLE);
        obscureLayoutButton.setVisibility(View.VISIBLE);

        if(!_view.vote.date.equals("")) {
            detailVoteDate.setVisibility(View.VISIBLE);
            detailVoteDate.setText(Html.fromHtml("<b>" + res.getString(R.string.detail_vote_date) + "</b> " + _view.vote.date, Html.FROM_HTML_MODE_COMPACT));
        }
        if(!_view.vote.testType.equals("")) {
            detailVoteType.setVisibility(View.VISIBLE);
            detailVoteType.setText(Html.fromHtml("<b>" + res.getString(R.string.detail_vote_type) + "</b> " + _view.vote.testType, Html.FROM_HTML_MODE_COMPACT));
        }
        if(!_view.vote.arguments.equals("")) {
            detailVoteArguments.setVisibility(View.VISIBLE);
            detailVoteArguments.setText(Html.fromHtml("<b>" + res.getString(R.string.detail_vote_arguments) + "</b> " + _view.vote.arguments, Html.FROM_HTML_MODE_COMPACT));
        }
        if(!_view.vote.judgement.equals("")) {
            detailVoteJudge.setVisibility(View.VISIBLE);
            detailVoteJudge.setText(Html.fromHtml("<b>" + res.getString(R.string.detail_vote_judge) + "</b> " + _view.vote.judgement, Html.FROM_HTML_MODE_COMPACT));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addVoteView(String subject, String voteFirstQuart, float rawVoteFirstQuart, String voteSecondQuart, float rawVoteSecondQuart){
        voteView = new VoteView(getContext(), null, subject, voteFirstQuart, rawVoteFirstQuart, voteSecondQuart, rawVoteSecondQuart, gS.getAllVotes(false).get(subject), this::onClickSingleVote);
        voteView.setId(View.generateViewId());

        voteView.setLayoutParams(params);

        mainLayout.addView(voteView);
    }
}
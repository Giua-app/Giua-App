package com.giua.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.giua.objects.Vote;
import com.giua.webscraper.GiuaScraper;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.nodes.Document;

public class SecondFragment extends Fragment {

    TextView text;
    TextView text2;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View fragmentSecondLayout = inflater.inflate(R.layout.fragment_second, container, false);

        text = fragmentSecondLayout.findViewById(R.id.textView2);
        text2 = fragmentSecondLayout.findViewById(R.id.textView5);

        return fragmentSecondLayout;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GiuaScraper gS = new GlobalVars().gS;

        Document doc = gS.getPage("https://registro.giua.edu.it");

        text.setText("Benvenuto " + gS.getUserType(doc));

        text2.setText("" + Vote.getAllVotes(gS).toString());


        view.findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "no vai via non mi toccare", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                /*NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);*/
            }
        });
    }
}
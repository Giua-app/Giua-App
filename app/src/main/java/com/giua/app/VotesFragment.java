package com.giua.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;

import com.giua.webscraper.GiuaScraper;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.nodes.Document;

public class VotesFragment extends Fragment {
    GiuaScraper gS;
    TextView text;
    TextView text2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.votes_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        gS = (GiuaScraper) getArguments().get("giuascraper");

        text = view.findViewById(R.id.txvWelcome);
        text2 = view.findViewById(R.id.txvVotes);

        Document doc = gS.getPage("");

        text.setText("Benvenuto " + gS.getUserType(doc));

        text2.setText("" + gS.getAllVotes().toString());



        view.findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "no vai via non mi toccare", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}

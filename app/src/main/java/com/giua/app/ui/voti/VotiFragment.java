package com.giua.app.ui.voti;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.giua.app.R;
import com.giua.webscraper.GiuaScraper;
import com.google.android.material.snackbar.Snackbar;

public class VotiFragment extends Fragment {

    private VotiViewModel votiViewModel;

    GiuaScraper gS;
    TextView text;
    TextView text2;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        votiViewModel =
                new ViewModelProvider(this).get(VotiViewModel.class);
        View root = inflater.inflate(R.layout.fragment_voti, container, false);
        final TextView textView = root.findViewById(R.id.text_home);


        Intent intent = getActivity().getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");

        votiViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //gS = (GiuaScraper) getArguments().get("giuascraper");

        text = view.findViewById(R.id.txvWelcome);
        text2 = view.findViewById(R.id.txvVotes);

        text.setText("Benvenuto " + gS.getUserType());

        text2.setText(gS.getAllVotes(false).toString());



        view.findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "no vai via non mi toccare", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
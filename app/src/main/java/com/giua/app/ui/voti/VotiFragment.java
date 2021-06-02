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

public class VotiFragment extends Fragment {

    private VotiViewModel votiViewModel;

    GiuaScraper gS;

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
}
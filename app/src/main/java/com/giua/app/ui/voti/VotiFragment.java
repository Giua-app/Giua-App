package com.giua.app.ui.voti;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.giua.app.R;
import com.giua.objects.Vote;
import com.giua.webscraper.GiuaScraper;

import java.util.List;
import java.util.Map;

public class VotiFragment extends Fragment {

    GiuaScraper gS;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_voti, container, false);

        Intent intent = getActivity().getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");
        Map<String, List<Vote>> allVotes = gS.getAllVotes(false);

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

}
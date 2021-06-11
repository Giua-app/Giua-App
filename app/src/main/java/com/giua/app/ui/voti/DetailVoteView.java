package com.giua.app.ui.voti;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.giua.app.R;
import com.giua.objects.Vote;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DetailVoteView extends LinearLayout {
    List<Vote> allVotes;
    LinearLayout thisLayout;

    public DetailVoteView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, List<Vote> allVotes) {
        super(context, attrs);

        this.allVotes = allVotes;
        initializeComponent(context);
    }

    private void initializeComponent(Context context){
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.detail_vote_view_component, this);

        thisLayout = findViewById(R.id.detail_vote_view_layout);
    }
}

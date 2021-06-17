package com.giua.app.ui.voti;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.giua.objects.Vote;

import org.jetbrains.annotations.NotNull;

public class SingleVoteView extends androidx.appcompat.widget.AppCompatTextView {
    Vote vote;

    public SingleVoteView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, Vote vote){
        super(context, attrs);

        this.vote = vote;
    }
}

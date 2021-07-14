package com.giua.app.ui.lezioni;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.giua.app.R;
import com.giua.objects.Lesson;

import org.jetbrains.annotations.NotNull;

public class LessonView extends ConstraintLayout {

    Lesson lesson;

    public LessonView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, Lesson lesson) {
        super(context, attrs);

        this.lesson = lesson;

        initializeComponent(context);
    }

    private void initializeComponent(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.lesson_view, this);

        ((TextView) findViewById(R.id.lesson_view_subject)).setText(lesson.subject);
        ((TextView) findViewById(R.id.lesson_view_time)).setText(lesson.time);
    }
}

package com.giua.app.ui.lezioni;

import android.content.Context;
import android.text.Html;
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

        ((TextView) findViewById(R.id.lesson_view_subject)).setText(Html.fromHtml("" + lesson.subject, Html.FROM_HTML_MODE_COMPACT));
        //((TextView) findViewById(R.id.lesson_view_argument)).setText(Html.fromHtml("<b>Argomenti: </b> " + lesson.arguments, Html.FROM_HTML_MODE_COMPACT));
        //((TextView) findViewById(R.id.lesson_view_activity)).setText(Html.fromHtml("<b>Attività: </b> " + lesson.activities, Html.FROM_HTML_MODE_COMPACT));
        ((TextView) findViewById(R.id.lesson_view_time)).setText("   "+lesson.time);
    }
}

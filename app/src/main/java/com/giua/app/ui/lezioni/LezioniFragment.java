package com.giua.app.ui.lezioni;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.giua.app.R;
import com.giua.objects.Lesson;
import com.giua.webscraper.GiuaScraper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LezioniFragment extends Fragment {

    GiuaScraper gS;
    TextView tvPrevDate;
    TextView tvCurrentDate;
    TextView tvNextDate;
    ImageView calendarBtn;
    ImageView obscureLayout;
    TextView tvNoElements;
    FrameLayout frameLayout;
    CalendarView calendarView;
    LinearLayout lessonsLayout;
    List<Lesson> allLessons;
    Date currentDate;
    Calendar calendar;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lezioni, container, false);

        Intent intent = requireActivity().getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");

        tvPrevDate = root.findViewById(R.id.lezioni_fragment_prev_date);
        tvCurrentDate = root.findViewById(R.id.lezioni_fragment_current_date);
        tvNextDate = root.findViewById(R.id.lezioni_fragment_next_date);
        lessonsLayout = root.findViewById(R.id.lezioni_fragment_lessons_layout);
        calendarBtn = root.findViewById(R.id.lezioni_fragment_image_view);
        calendarView = root.findViewById(R.id.lezioni_fragment_calendar_view);
        obscureLayout = root.findViewById(R.id.obscure_layout_image_button3);
        frameLayout = root.findViewById(R.id.lezioni_fragment_frame_layout);
        tvNoElements = root.findViewById(R.id.lezioni_fragment_no_elements_view);

        calendar = Calendar.getInstance();
        currentDate = new Date();

        tvPrevDate.setText(formatter.format(getPrevDate(currentDate)));
        tvCurrentDate.setText(formatter.format(currentDate));
        tvNextDate.setText(formatter.format(getNextDate(currentDate)));

        tvPrevDate.setOnClickListener(this::prevDateOnClick);
        tvNextDate.setOnClickListener(this::nextDateOnClick);
        calendarBtn.setOnClickListener(this::calendarBtnOnClick);
        obscureLayout.setOnClickListener(this::obscureLayoutOnClick);
        calendarView.setOnDateChangeListener(this::calendarOnChangeDateListener);

        refreshAllLessons();

        addLessonViews();

        return root;
    }

    /**
     * Riaggiorna le lezioni di oggi utilizzando {@code currentDate}
     */
    private void refreshAllLessons() {
        allLessons = gS.getAllLessons(formatter.format(currentDate), true);
    }


    /**
     * Aggiunge le lezioni nella UI
     */
    private void addLessonViews() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lessonsLayout.removeAllViews();

        params.setMargins(5, 40, 5, 0);
        tvNoElements.setVisibility(View.GONE);

        if (allLessons.size() == 1 && !allLessons.get(0).exists) {
            tvNoElements.setVisibility(View.VISIBLE);
        } else {
            for (Lesson lesson : allLessons) {
                LessonView lessonView = new LessonView(requireContext(), null, lesson);
                lessonView.setId(View.generateViewId());
                lessonView.setLayoutParams(params);

                lessonsLayout.addView(lessonView);
            }
        }
    }

    private void calendarOnChangeDateListener(CalendarView view, int year, int month, int dayOfMonth) {
        try {
            currentDate = getCurrentDate(formatter.parse(year + "-" + month + "-" + dayOfMonth));
            tvCurrentDate.setText(formatter.format(currentDate));
            tvPrevDate.setText(formatter.format(getPrevDate(currentDate)));
            tvNextDate.setText(formatter.format(getNextDate(currentDate)));
            refreshAllLessons();
            addLessonViews();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void obscureLayoutOnClick(View view) {
        frameLayout.setVisibility(View.GONE);
        obscureLayout.setVisibility(View.GONE);
    }

    private void calendarBtnOnClick(View view) {
        frameLayout.setVisibility(View.VISIBLE);
        obscureLayout.setVisibility(View.VISIBLE);
    }

    private void prevDateOnClick(View view) {
        currentDate = getPrevDate(currentDate);
        tvNextDate.setText(tvCurrentDate.getText());
        tvCurrentDate.setText(tvPrevDate.getText());
        tvPrevDate.setText(formatter.format(getPrevDate(currentDate)));
        refreshAllLessons();
        addLessonViews();
    }

    private void nextDateOnClick(View view) {
        currentDate = getNextDate(currentDate);
        tvPrevDate.setText(tvCurrentDate.getText());
        tvCurrentDate.setText(tvNextDate.getText());
        tvNextDate.setText(formatter.format(getNextDate(currentDate)));
        refreshAllLessons();
        addLessonViews();
    }

    private Date getCurrentDate(Date date) {
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 0);
        return calendar.getTime();
    }

    private Date getNextDate(Date date) {
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }

    private Date getPrevDate(Date date) {
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime();
    }
}
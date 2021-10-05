/*
 * Giua App
 * Android app to view data from the giua@school workbook
 * Copyright (C) 2021 - 2021 Hiem, Franck1421 and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package com.giua.app.ui.fragments.agenda;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.giua.app.LoggerManager;
import com.giua.app.R;
import com.giua.objects.Homework;
import com.giua.objects.Test;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class AgendaView extends RelativeLayout {
    Homework homework = null;
    Test test = null;
    LoggerManager loggerManager;

    public AgendaView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, Test test) {
        super(context, attrs);

        this.test = test;

        initializeComponent(context);
    }

    public AgendaView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, Homework homework) {
        super(context, attrs);

        this.homework = homework;

        initializeComponent(context);
    }

    public AgendaView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, Homework homework, Test test) {
        super(context, attrs);

        this.homework = homework;
        this.test = test;

        initializeComponent(context);
    }

    private void initializeComponent(Context context) {
        Calendar objectDay = Calendar.getInstance();        //Rappresenta il giorno del compito o della verifica
        loggerManager = new LoggerManager("AgendaView", context);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_agenda, this);

        TextView tvTime = findViewById(R.id.agenda_view_time);
        TextView tvDate = findViewById(R.id.agenda_view_date);
        TextView tvType = findViewById(R.id.agenda_view_type);

        if (homework != null && test != null) {
            objectDay.set(Integer.parseInt(homework.year), Integer.parseInt(homework.month) - 1, Integer.parseInt(homework.day), 23, 59, 59);
            tvDate.setText(homework.day + "-" + homework.month + "-" + homework.year);
            tvType.setText(R.string.agenda_view_type_homework_and_tests);
            tvType.setTextColor(getResources().getColor(R.color.agenda_views_text_green, context.getTheme()));
        } else if (homework != null) {
            objectDay.set(Integer.parseInt(homework.year), Integer.parseInt(homework.month) -1, Integer.parseInt(homework.day), 23, 59, 59);
            tvDate.setText(homework.day + "-" + homework.month + "-" + homework.year);
            tvType.setText(R.string.agenda_view_type_homeworks);
            tvType.setTextColor(getResources().getColor(R.color.agenda_views_text_cyan, context.getTheme()));
        } else {
            objectDay.set(Integer.parseInt(test.year), Integer.parseInt(test.month) -1, Integer.parseInt(test.day), 23, 59, 59);
            tvDate.setText(test.day + "-" + test.month + "-" + test.year);
            tvType.setText(R.string.agenda_view_type_tests);
            tvType.setTextColor(getResources().getColor(R.color.agenda_views_text_orange, context.getTheme()));
        }

        //Calendar fake = Calendar.getInstance();
        //fake.set(Calendar.MONTH, 11);
        //fake.set(Calendar.DAY_OF_MONTH, 29);
        int timeDiff = calcTimeDifferenceInDay(Calendar.getInstance(), objectDay);
        loggerManager.d("mancano " + timeDiff + " giorni al compito del " + objectDay.get(Calendar.DAY_OF_MONTH) + "/"
                + objectDay.get(Calendar.MONTH) + "/" + objectDay.get(Calendar.YEAR));

        if (timeDiff < 0)
            if (timeDiff == -1)
                tvTime.setText("Ieri");
            else
                tvTime.setText(Math.abs(timeDiff) + " giorni fa");
        else {
            switch (timeDiff) {
                case 3:
                    tvTime.setTextColor(getResources().getColor(R.color.middle_vote_lighter, context.getTheme()));
                    tvTime.setText("Tra " + timeDiff + " giorni");
                    break;
                case 2:
                    tvTime.setTextColor(getResources().getColor(R.color.middle_vote, context.getTheme()));
                    tvTime.setText("Tra " + timeDiff + " giorni");
                    break;
                case 1:
                    tvTime.setTextColor(getResources().getColor(R.color.bad_vote, context.getTheme()));
                    tvTime.setText("Domani");
                    break;
                case 0:
                    tvTime.setTextColor(getResources().getColor(R.color.bad_vote, context.getTheme()));
                    tvTime.setText("Oggi");
                    break;
                default:
                    tvTime.setText("Tra " + timeDiff + " giorni");
                    break;
            }
        }
    }

    private int calcTimeDifferenceInDay(Calendar now, Calendar homework) {
        if(now.get(Calendar.YEAR) == homework.get(Calendar.YEAR)) {
            return homework.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR);
        }

        long diffInMillis = homework.getTime().getTime() - now.getTime().getTime();
        double diffInDays = diffInMillis / 86_400_000f;

        loggerManager.d("la differenza in ms è " + diffInMillis);
        loggerManager.d("in giorni è: " + diffInDays);
        return Math.round(diffInMillis / 86_400_000f);       //Una giornata sono 86_400_000 ms
    }
}
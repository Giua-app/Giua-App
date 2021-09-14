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

package com.giua.app.ui.fragments.pinboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.giua.app.R;
import com.giua.app.ui.fragments.alerts.AlertsFragment;
import com.giua.app.ui.fragments.newsletters.NewslettersFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class PinboardFragment extends Fragment {

    TabLayout tabLayout;
    FragmentManager fragmentManager;
    FragmentTransaction ft;
    Bundle bundle;
    //ViewPager2 viewPager;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pinboard, container, false);

        tabLayout = root.findViewById(R.id.tabLayout);
        //viewPager = root.findViewById(R.id.pager);

        bundle = getArguments();
        fragmentManager = getChildFragmentManager();
        ft = fragmentManager.beginTransaction();
        ft.addToBackStack(null);
        ft.setReorderingAllowed(true);
        ft.replace(R.id.fragment_tabs_circolari_avvisi_framelayout, NewslettersFragment.class, bundle);
        ft.commit();
        //End


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ft = fragmentManager.beginTransaction();
                ft.addToBackStack(null);
                ft.setReorderingAllowed(true);

                if (Objects.requireNonNull(tab.getText()).toString().equals("Circolari")) {
                    ft.replace(R.id.fragment_tabs_circolari_avvisi_framelayout, NewslettersFragment.class, bundle);
                    ft.commit();
                } else if (tab.getText().toString().equals("Avvisi")) {
                    ft.replace(R.id.fragment_tabs_circolari_avvisi_framelayout, AlertsFragment.class, bundle);
                    ft.commit();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        fragmentManager = null;
        ft = null;
        tabLayout = null;
        super.onDestroyView();
    }
}
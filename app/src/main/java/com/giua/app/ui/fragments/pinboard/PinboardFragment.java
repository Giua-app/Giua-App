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

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.giua.app.R;
import com.giua.app.ui.fragments.alerts.AlertsFragment;
import com.giua.app.ui.fragments.newsletters.NewslettersFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class PinboardFragment extends Fragment {

    String goTo = "";

    public PinboardFragment(String goTo) {
        this.goTo = goTo;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pinboard, container, false);

        TabLayout tabLayout = root.findViewById(R.id.tabLayout);

        Bundle bundle = getArguments();
        FragmentManager fragmentManager = getChildFragmentManager();

        if (goTo == null || goTo.equals("") || goTo.equals("Newsletters")) {
            Fragment fragment = fragmentManager.findFragmentByTag("FRAGMENT_NEWSLETTER");
            if (fragment == null) {
                fragmentManager.beginTransaction()
                        .addToBackStack(null)
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_tabs_circolari_avvisi_framelayout, NewslettersFragment.class, bundle, "FRAGMENT_NEWSLETTER")
                        .commit();
            } else {
                fragmentManager.beginTransaction()
                        .addToBackStack(null)
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_tabs_circolari_avvisi_framelayout, fragment, "FRAGMENT_NEWSLETTER")
                        .commit();
            }
        } else if (goTo.equals("Alerts")) {
            Fragment fragment = fragmentManager.findFragmentByTag("FRAGMENT_ALERTS");
            if (fragment == null) {
                fragmentManager.beginTransaction()
                        .addToBackStack(null)
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_tabs_circolari_avvisi_framelayout, AlertsFragment.class, bundle, "FRAGMENT_ALERTS")
                        .commit();
            } else {
                fragmentManager.beginTransaction()
                        .addToBackStack(null)
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_tabs_circolari_avvisi_framelayout, fragment, "FRAGMENT_ALERTS")
                        .commit();
            }
        }

        //Elimina le notifiche delle circolari e degli avvisi se ce ne sono
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(10);
        notificationManager.cancel(11);

        //End


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (Objects.requireNonNull(tab.getText()).toString().equals("Circolari")) {
                    Fragment fragment = fragmentManager.findFragmentByTag("FRAGMENT_NEWSLETTER");
                    if (fragment == null) {
                        fragmentManager.beginTransaction()
                                .addToBackStack(null)
                                .setReorderingAllowed(true)
                                .replace(R.id.fragment_tabs_circolari_avvisi_framelayout, NewslettersFragment.class, bundle, "FRAGMENT_NEWSLETTER")
                                .commit();
                    } else {
                        fragmentManager.beginTransaction()
                                .addToBackStack(null)
                                .setReorderingAllowed(true)
                                .replace(R.id.fragment_tabs_circolari_avvisi_framelayout, fragment, "FRAGMENT_NEWSLETTER")
                                .commit();
                    }
                } else if (tab.getText().toString().equals("Avvisi")) {
                    Fragment fragment = fragmentManager.findFragmentByTag("FRAGMENT_ALERTS");
                    if (fragment == null) {
                        fragmentManager.beginTransaction()
                                .addToBackStack(null)
                                .setReorderingAllowed(true)
                                .replace(R.id.fragment_tabs_circolari_avvisi_framelayout, AlertsFragment.class, bundle, "FRAGMENT_ALERTS")
                                .commit();
                    } else {
                        fragment.onDestroy();
                        fragmentManager.beginTransaction()
                                .addToBackStack(null)
                                .setReorderingAllowed(true)
                                .replace(R.id.fragment_tabs_circolari_avvisi_framelayout, fragment, "FRAGMENT_ALERTS")
                                .commit();
                    }
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
}
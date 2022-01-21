/*
 * Giua App
 * Android app to view data from the giua@school workbook
 * Copyright (C) 2021 - 2022 Hiem, Franck1421 and contributors
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

package com.giua.app.ui.fragments.authorizations;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.objects.Authorization;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

public class AuthorizationFragment extends Fragment implements IGiuaAppFragment {

    View root;
    Authorization authorization;
    Activity activity;
    boolean demoMode = false;
    boolean offlineMode = false;
    boolean refresh = false;
    boolean isFragmentDestroyed = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        demoMode = SettingsData.getSettingBoolean(requireActivity(), SettingKey.DEMO_MODE);
        if (getArguments() != null)
            offlineMode = getArguments().getBoolean("offline");
        root = inflater.inflate(R.layout.fragment_autorizations, container, false);

        activity = requireActivity();

        ((SwipeRefreshLayout) root.findViewById(R.id.authorizations_swipe_refresh)).setRefreshing(true);
        ((SwipeRefreshLayout) root.findViewById(R.id.authorizations_swipe_refresh)).setOnRefreshListener(this::onRefresh);
        loadDataAndViews();
        return root;
    }

    @Override
    public void loadDataAndViews() {
        GlobalVariables.internetThread.addRunnableToRun(() -> {
            try {
                authorization = GlobalVariables.gS.getAuthorizationsPage(refresh).getAuthorizations();
                activity.runOnUiThread(this::addViews);
            } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.your_connection_error), root));
            } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.site_connection_error), root));
            } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root));
            }
        });
    }

    @Override
    public void addViews() {
        ((TextView) root.findViewById(R.id.authorizations_entry)).setText(getText(R.string.authorizations_entry) + authorization.entry);
        ((TextView) root.findViewById(R.id.authorizations_exit)).setText(getText(R.string.authorizations_exit) + authorization.exit);

        refresh = false;
        ((SwipeRefreshLayout) root.findViewById(R.id.authorizations_swipe_refresh)).setRefreshing(false);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    private void onRefresh() {
        refresh = true;
        loadDataAndViews();
    }

    private void setErrorMessage(String message, View root) {
        if (!isFragmentDestroyed)
            Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        isFragmentDestroyed = true;
        super.onDestroyView();
    }
}

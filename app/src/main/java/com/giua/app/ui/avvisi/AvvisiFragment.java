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

package com.giua.app.ui.avvisi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.giua.app.GlobalVariables;
import com.giua.app.R;
import com.giua.objects.Alert;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class AvvisiFragment extends Fragment {

    List<Alert> allAlerts;
    LinearLayout viewsLayout;
    ProgressBar pbLoadingPage;
    ProgressBar pbLoadingContent;
    ScrollView scrollView;
    FloatingActionButton fabGoUp;
    TextView tvNoElements;
    SwipeRefreshLayout swipeRefreshLayout;
    View root;
    FragmentActivity activity;
    int currentPage = 1;    //Rappresenta la pagina degli avvisi da caricare
    boolean hasLoadedAllPages = false;
    boolean isLoadingContent = false;
    boolean canSendErrorMessage = true;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_avvisi, container, false);

        viewsLayout = root.findViewById(R.id.alert_linear_layout);
        pbLoadingPage = root.findViewById(R.id.alert_loading_page_bar);
        scrollView = root.findViewById(R.id.alert_scroll_view);
        tvNoElements = root.findViewById(R.id.alert_no_elements_view);
        fabGoUp = root.findViewById(R.id.alert_btn_go_up);
        swipeRefreshLayout = root.findViewById(R.id.alert_swipe_refresh_layout);
        pbLoadingContent = new ProgressBar(requireContext());

        activity = requireActivity();

        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        fabGoUp.setOnClickListener(this::btnGoUpOnClick);
        scrollView.setOnScrollChangeListener(this::scrollViewOnScroll);

        loadDataAndViews();
        return root;
    }

    /**
     * Riaggiorna i dati in modo asincrono e fa aggiungere le views con i dati raccolti
     */
    private void loadDataAndViews() {
        if (!hasLoadedAllPages && !isLoadingContent) {
            isLoadingContent = true;
            if (currentPage > 1 && pbLoadingContent.getParent() == null)
                viewsLayout.addView(pbLoadingContent);
            new Thread(() -> {
                try {
                    allAlerts = GlobalVariables.gS.getAllAlerts(currentPage, true);

                    if (allAlerts.isEmpty() && currentPage == 1) {
                        hasLoadedAllPages = true;
                        activity.runOnUiThread(this::finishedLoading);
                    } else if (allAlerts.isEmpty()) {
                        hasLoadedAllPages = true;
                        activity.runOnUiThread(this::finishedLoading);
                    } else {
                        activity.runOnUiThread(this::addViews);
                        currentPage++;
                    }
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(getString(R.string.site_connection_error), root);
                        finishedLoading();
                        tvNoElements.setVisibility(View.VISIBLE);
                    });
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(getString(R.string.your_connection_error), root);
                        finishedLoading();
                        tvNoElements.setVisibility(View.VISIBLE);
                    });
                }
            }).start();
        }
    }

    /**
     * Aggiunge effetivamente le {@code AlertView}
     */
    private void addViews() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 40, 0, 0);

        for (Alert alert : allAlerts) {
            AlertView view = new AlertView(requireContext(), null, alert);
            view.setLayoutParams(params);
            view.setId(View.generateViewId());

            viewsLayout.addView(view);
        }

        finishedLoading();
    }

    /**
     * Listener dello scorrimento della scroll view
     */
    private void scrollViewOnScroll(View view, int x, int y, int oldX, int oldY) {
        if (!isLoadingContent && !hasLoadedAllPages && !scrollView.canScrollVertically(100) && y - oldY > 10) {
            loadDataAndViews();
        }
        if (scrollView.canScrollVertically(-100))
            fabGoUp.setVisibility(View.VISIBLE);
        else
            fabGoUp.setVisibility(View.GONE);
    }

    private void onRefresh() {
        currentPage = 1;
        hasLoadedAllPages = false;
        isLoadingContent = false;
        viewsLayout.removeAllViews();
        loadDataAndViews();
    }

    private void btnGoUpOnClick(View view) {
        scrollView.smoothScrollTo(0, 0);
    }

    private void finishedLoading() {
        isLoadingContent = false;
        pbLoadingPage.setVisibility(View.GONE);
        viewsLayout.removeView(pbLoadingContent);
        swipeRefreshLayout.setRefreshing(false);
    }

    public void setErrorMessage(String message, View root) {
        if (canSendErrorMessage)
            Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        canSendErrorMessage = true;
        super.onResume();
    }

    @Override
    public void onPause() {
        canSendErrorMessage = false;
        super.onPause();
    }
}
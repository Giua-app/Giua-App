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

package com.giua.app.ui.fragments.alerts;

import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.OfflineDBController;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.app.ui.activities.DrawerActivity;
import com.giua.app.ui.views.ObscureLayoutView;
import com.giua.objects.Alert;
import com.giua.webscraper.DownloadedFile;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class AlertsFragment extends Fragment implements IGiuaAppFragment {

    List<Alert> allAlerts = new Vector<>();
    List<Alert> allAlertsToSave = new Vector<>();
    LinearLayout viewsLayout;
    LinearLayout detailsLayout;
    LinearLayout attachmentLayout;
    ProgressBar pbLoadingPage;
    ProgressBar pbLoadingContent;
    ScrollView scrollView;
    FloatingActionButton fabGoUp;
    TextView tvNoElements;
    SwipeRefreshLayout swipeRefreshLayout;
    ObscureLayoutView obscureLayoutView;
    View root;
    FragmentActivity activity;
    int currentPage = 1;    //Rappresenta la pagina degli avvisi da caricare
    boolean hasLoadedAllPages = false;
    boolean isLoadingContent = false;
    boolean canSendErrorMessage = true;
    boolean isDownloading = false;
    boolean offlineMode = false;
    boolean demoMode = false;
    boolean isFragmentDestroyed = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        demoMode = SettingsData.getSettingBoolean(requireActivity(), SettingKey.DEMO_MODE);
        root = inflater.inflate(R.layout.fragment_alerts, container, false);

        allAlerts = new Vector<>();
        allAlertsToSave = new Vector<>();
        currentPage = 1;    //Rappresenta la pagina degli avvisi da caricare
        hasLoadedAllPages = false;
        isLoadingContent = false;
        canSendErrorMessage = true;
        isDownloading = false;
        offlineMode = false;

        viewsLayout = root.findViewById(R.id.alert_linear_layout);
        pbLoadingPage = root.findViewById(R.id.alert_loading_page_bar);
        scrollView = root.findViewById(R.id.alert_scroll_view);
        tvNoElements = root.findViewById(R.id.alert_no_elements_view);
        fabGoUp = root.findViewById(R.id.alert_btn_go_up);
        swipeRefreshLayout = root.findViewById(R.id.alert_swipe_refresh_layout);
        obscureLayoutView = root.findViewById(R.id.alert_obscure_layout);
        detailsLayout = root.findViewById(R.id.alert_details_layout);
        attachmentLayout = root.findViewById(R.id.alert_attachment_layout);
        pbLoadingContent = new ProgressBar(requireActivity());

        activity = requireActivity();

        offlineMode = activity.getIntent().getBooleanExtra("offline", false);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        fabGoUp.setOnClickListener(this::btnGoUpOnClick);
        scrollView.setOnScrollChangeListener(this::scrollViewOnScroll);
        obscureLayoutView.setOnClickListener((view) -> {
            detailsLayout.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.visualizer_hide_effect));
            obscureLayoutView.hide();
            detailsLayout.setVisibility(View.GONE);
        });

        activity.getSystemService(NotificationManager.class).cancel(11);

        return root;
    }

    @Override
    public void loadOfflineDataAndViews() {
        if (!hasLoadedAllPages && !isLoadingContent) {
            isLoadingContent = true;
            if (pbLoadingContent.getParent() == null)
                viewsLayout.addView(pbLoadingContent);
            new Thread(() -> {
                allAlerts = new OfflineDBController(root.getContext()).readAlerts();
                if (allAlerts.isEmpty()) {
                    hasLoadedAllPages = true;
                    activity.runOnUiThread(this::finishedLoading);
                    activity.runOnUiThread(() -> tvNoElements.setVisibility(View.VISIBLE));
                } else {
                    activity.runOnUiThread(this::addViews);
                    allAlertsToSave.addAll(allAlerts);
                }
            }).start();
        }
    }

    /**
     * Riaggiorna i dati in modo asincrono e fa aggiungere le views con i dati raccolti
     */
    @Override
    public void loadDataAndViews() {
        if (!hasLoadedAllPages && !isLoadingContent) {

            isLoadingContent = true;
            if (currentPage == 1)
                swipeRefreshLayout.setRefreshing(true);
            if (currentPage > 1 && pbLoadingContent.getParent() == null)
                viewsLayout.addView(pbLoadingContent);

            GlobalVariables.gsThread.addTask(() -> {
                try {
                    allAlerts = GlobalVariables.gS.getAlertsPage(false).getAllAlerts(currentPage);

                    if (isFragmentDestroyed) return;

                    if (currentPage == 1) {
                        new OfflineDBController(root.getContext()).addAlerts(allAlerts);
                        ((DrawerActivity) activity).notificationsDBController.replaceAlerts(allAlerts);
                        ((DrawerActivity) activity).notificationsDBController.addAlerts(GlobalVariables.gS.getAlertsPage(false).getAllAlerts(2));
                    }

                    if (allAlerts.isEmpty() && currentPage == 1) {
                        hasLoadedAllPages = true;
                        activity.runOnUiThread(this::finishedLoading);
                        activity.runOnUiThread(() -> tvNoElements.setVisibility(View.VISIBLE));
                    } else if (allAlerts.isEmpty()) {
                        hasLoadedAllPages = true;
                        activity.runOnUiThread(this::finishedLoading);
                    } else {
                        activity.runOnUiThread(this::addViews);
                        currentPage++;
                        allAlertsToSave.addAll(allAlerts);
                    }
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.site_connection_error), root);
                        finishedLoading();
                        tvNoElements.setVisibility(View.VISIBLE);
                    });
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.your_connection_error), root);
                        finishedLoading();
                        tvNoElements.setVisibility(View.VISIBLE);
                    });
                } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                        finishedLoading();
                        tvNoElements.setVisibility(View.VISIBLE);
                    });
                } catch (GiuaScraperExceptions.NotLoggedIn e) {
                    activity.runOnUiThread(() -> {
                        ((DrawerActivity) activity).startActivityManager();
                    });
                }
            });
        }
    }

    /**
     * Aggiunge effetivamente le {@code AlertView}
     */
    @Override
    public void addViews() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 40, 0, 0);

        for (Alert alert : allAlerts) {
            AlertView view = new AlertView(requireActivity(), null, alert);
            view.setLayoutParams(params);
            view.setId(View.generateViewId());

            view.setOnClickListener(this::alertViewOnClick);

            viewsLayout.addView(view);
        }

        finishedLoading();
    }

    @Override
    public boolean onBackPressed() {
        if (obscureLayoutView.isShown()) {
            obscureLayoutView.performClick();
            return true;
        }
        return false;
    }

    //region Listeners
    private void alertViewOnClick(View view) {
        if(offlineMode){
            alertViewOfflineOnClick(view);
            return;
        }

        pbLoadingPage.setVisibility(View.VISIBLE);

        GlobalVariables.gsThread.addTask(() -> {
            try {
                ((AlertView) view).alert.getDetails(GlobalVariables.gS);

                if (isFragmentDestroyed) return;

                activity.runOnUiThread(() -> {
                    Alert alert = ((AlertView) view).alert;
                    ((AlertView) view).markAsRead();
                    TextView alertDetailsTextView = root.findViewById(R.id.alert_details_text_view);


                    alertDetailsTextView.setText(Html.fromHtml(alert.details, 0));
                    ((TextView) root.findViewById(R.id.alert_creator_text_view)).setText(alert.creator);
                    ((TextView) root.findViewById(R.id.alert_type_text_view)).setText(alert.type);

                    //Parsing per url nel html
                    Linkify.addLinks(alertDetailsTextView, Linkify.WEB_URLS);

                    attachmentLayout.removeAllViews();
                    int urlsListLength = alert.attachmentUrls.size();
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 20, 0, 0);
                    for (int i = 0; i < urlsListLength; i++) {
                        TextView tvUrl = new TextView(requireActivity());
                        tvUrl.setText("Allegato " + (i + 1));
                        tvUrl.setTypeface(tvNoElements.getTypeface(), Typeface.NORMAL);
                        tvUrl.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.corner_radius_10dp, activity.getTheme()));
                        tvUrl.setBackgroundTintList(activity.getResources().getColorStateList(R.color.main_color, activity.getTheme()));
                        tvUrl.setPadding(20, 20, 20, 20);
                        tvUrl.setLayoutParams(params);
                        tvUrl.setTextColor(activity.getResources().getColorStateList(R.color.light_white_night_black, activity.getTheme()));
                        int finalI = i;
                        tvUrl.setOnClickListener((view2) -> downloadAndOpenFile(alert.attachmentUrls.get(finalI)));
                        attachmentLayout.addView(tvUrl);
                    }

                    detailsLayout.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.visualizer_show_effect));
                    detailsLayout.setVisibility(View.VISIBLE);
                    obscureLayoutView.show();
                    pbLoadingPage.setVisibility(View.GONE);
                });
            } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.site_connection_error), root);
                    finishedLoading();
                    tvNoElements.setVisibility(View.VISIBLE);
                });
            } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.your_connection_error), root);
                    finishedLoading();
                    tvNoElements.setVisibility(View.VISIBLE);
                });
            } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                    finishedLoading();
                    tvNoElements.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void alertViewOfflineOnClick(View view) {
        Alert alert = ((AlertView) view).alert;

        if(!alert.isDetailed){
            setErrorMessage("Dettagli di questo avviso non scaricati!", root);
            return;
        }

        swipeRefreshLayout.setRefreshing(true);

        TextView alertDetailsTextView = root.findViewById(R.id.alert_details_text_view);

        alertDetailsTextView.setText(Html.fromHtml(alert.details, 0));
        ((TextView) root.findViewById(R.id.alert_creator_text_view)).setText(alert.creator);
        ((TextView) root.findViewById(R.id.alert_type_text_view)).setText(alert.type);

        //Parsing per url nel html
        Linkify.addLinks(alertDetailsTextView, Linkify.WEB_URLS);

        attachmentLayout.removeAllViews();
        int urlsListLength = alert.attachmentUrls.size();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 20, 0, 0);
        for (int i = 0; i < urlsListLength; i++) {
            TextView tvUrl = new TextView(requireActivity());
            tvUrl.setText("Allegato " + (i + 1));
            tvUrl.setTypeface(tvNoElements.getTypeface(), Typeface.NORMAL);
            tvUrl.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.corner_radius_10dp, activity.getTheme()));
            tvUrl.setBackgroundTintList(activity.getResources().getColorStateList(R.color.main_color, activity.getTheme()));
            tvUrl.setPadding(20, 20, 20, 20);
            tvUrl.setLayoutParams(params);
            tvUrl.setTextColor(activity.getResources().getColorStateList(R.color.light_white_night_black, activity.getTheme()));
            int finalI = i;
            tvUrl.setOnClickListener((view2) -> downloadAndOpenFile(alert.attachmentUrls.get(finalI)));
            attachmentLayout.addView(tvUrl);
        }

        detailsLayout.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.visualizer_show_effect));
        detailsLayout.setVisibility(View.VISIBLE);
        obscureLayoutView.show();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void btnGoUpOnClick(View view) {
        scrollView.smoothScrollTo(0, 0);
    }


    /**
     * Listener dello scorrimento della scroll view
     */
    private void scrollViewOnScroll(View view, int x, int y, int oldX, int oldY) {
        if (!offlineMode && !isLoadingContent && !hasLoadedAllPages && !scrollView.canScrollVertically(100) && y - oldY > 10) {
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
        if(offlineMode)
            loadOfflineDataAndViews();
        else
            loadDataAndViews();
    }
    //endregion

    //region Metodi

    /**
     * Scarica e salva dall'url un file col nome "avviso" e lo apre chiamando openFile
     */
    private void downloadAndOpenFile(String url) {
        if(offlineMode){
            setErrorMessage("Non si possono scaricare allegati in modalità offline", root);
            return;
        }
        if (!isDownloading) {
            isDownloading = true;
            swipeRefreshLayout.setRefreshing(true);
            GlobalVariables.gsThread.addTask(() -> {
                try {
                    DownloadedFile downloadedFile = GlobalVariables.gS.download(url);

                    if (isFragmentDestroyed) return;

                    FileOutputStream out = new FileOutputStream(requireActivity().getFilesDir() + "/" + "allegato." + downloadedFile.fileExtension);
                    if (downloadedFile.data != null && downloadedFile.data.length > 0) {
                        out.write(downloadedFile.data);
                        out.close();
                        openFile("allegato." + downloadedFile.fileExtension, downloadedFile.fileExtension);
                    } else {
                        activity.runOnUiThread(() -> setErrorMessage("E' stato incontrato un errore di rete durante il download: riprovare", root));
                    }
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> setErrorMessage(getString(R.string.your_connection_error), root));
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> setErrorMessage(getString(R.string.site_connection_error), root));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isDownloading = false;
                activity.runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
            });
        }
    }

    /**
     * Apre il file col nome di "avviso"
     */
    private void openFile(String fileName, String fileExtension) {
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setData(FileProvider.getUriForFile(activity, "com.giua.app.fileprovider", new File(requireActivity().getFilesDir() + "/" + fileName)));
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent intent = Intent.createChooser(target, "Apri con:");
        try {
            startActivity(intent);
        } catch (Exception e) {
            setErrorMessage("Non è stata trovata alcuna app compatibile con il tipo di file " + fileExtension, root);
        }
    }

    private void finishedLoading() {
        isLoadingContent = false;
        viewsLayout.removeView(pbLoadingContent);
        swipeRefreshLayout.setRefreshing(false);
    }

    public void setErrorMessage(String message, View root) {
        if (!isFragmentDestroyed && canSendErrorMessage)
            Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    //endregion

    //region Fragment lifecycle

    @Override
    public void onStart() {
        if(offlineMode){
            loadOfflineDataAndViews();
        } else{
            loadDataAndViews();
        }

        super.onStart();
    }

    @Override
    public void onResume() {
        canSendErrorMessage = true;
        super.onResume();
    }

    @Override
    public void onPause() {
        canSendErrorMessage = false;
        allAlerts = new Vector<>();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
    //endregion

    @Override
    public void onDestroyView() {
        isFragmentDestroyed = true;
        super.onDestroyView();
    }
}
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

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.giua.app.AppData;
import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.R;
import com.giua.app.ui.ObscureLayoutView;
import com.giua.objects.Alert;
import com.giua.utils.JsonHelper;
import com.giua.webscraper.DownloadedFile;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class AvvisiFragment extends Fragment implements IGiuaAppFragment {

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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_avvisi, container, false);

        viewsLayout = root.findViewById(R.id.alert_linear_layout);
        pbLoadingPage = root.findViewById(R.id.alert_loading_page_bar);
        scrollView = root.findViewById(R.id.alert_scroll_view);
        tvNoElements = root.findViewById(R.id.alert_no_elements_view);
        fabGoUp = root.findViewById(R.id.alert_btn_go_up);
        swipeRefreshLayout = root.findViewById(R.id.alert_swipe_refresh_layout);
        obscureLayoutView = root.findViewById(R.id.alert_obscure_layout);
        detailsLayout = root.findViewById(R.id.alert_details_layout);
        attachmentLayout = root.findViewById(R.id.alert_attachment_layout);
        pbLoadingContent = new ProgressBar(requireContext());

        activity = requireActivity();

        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        fabGoUp.setOnClickListener(this::btnGoUpOnClick);
        scrollView.setOnScrollChangeListener(this::scrollViewOnScroll);
        obscureLayoutView.setOnClickListener((view) -> {
            obscureLayoutView.setVisibility(View.GONE);
            detailsLayout.setVisibility(View.GONE);
        });

        return root;
    }

    /**
     * Riaggiorna i dati in modo asincrono e fa aggiungere le views con i dati raccolti
     */
    @Override
    public void loadDataAndViews() {
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
                        allAlertsToSave.addAll(allAlerts);
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
    @Override
    public void addViews() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 40, 0, 0);

        for (Alert alert : allAlerts) {
            AlertView view = new AlertView(requireContext(), null, alert);
            view.setLayoutParams(params);
            view.setId(View.generateViewId());

            view.setOnClickListener(this::alertViewOnClick);

            viewsLayout.addView(view);
        }

        finishedLoading();
    }

    /**
     * Scarica e salva dall'url un file col nome "avviso" e lo apre chiamando openFile
     */
    private void downloadAndOpenFile(String url) {
        if (!isDownloading) {
            isDownloading = true;
            pbLoadingPage.setZ(10f);
            pbLoadingPage.setVisibility(View.VISIBLE);
            new Thread(() -> {
                try {
                    DownloadedFile downloadedFile = GlobalVariables.gS.download(url);
                    FileOutputStream out = new FileOutputStream(requireContext().getFilesDir() + "/" + "allegato." + downloadedFile.fileExtension);
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
                activity.runOnUiThread(() -> pbLoadingPage.setVisibility(View.GONE));
            }).start();
        }
    }

    /**
     * Apre il file col nome di "avviso"
     */
    private void openFile(String fileName, String fileExtension) {
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setData(FileProvider.getUriForFile(activity, "com.giua.app.provider", new File(requireContext().getFilesDir() + "/" + fileName)));
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent intent = Intent.createChooser(target, "Apri con:");
        try {
            startActivity(intent);
        } catch (Exception e) {
            setErrorMessage("Non è stata trovata alcuna app compatibile con il tipo di file " + fileExtension, root);
        }
    }

    private void alertViewOnClick(View view) {
        pbLoadingPage.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                ((AlertView) view).alert.getDetails(GlobalVariables.gS);

                activity.runOnUiThread(() -> {
                    Alert alert = ((AlertView) view).alert;
                    ((TextView) root.findViewById(R.id.alert_details_text_view)).setText(alert.details);
                    ((TextView) root.findViewById(R.id.alert_creator_text_view)).setText(alert.creator);
                    ((TextView) root.findViewById(R.id.alert_type_text_view)).setText(alert.type);

                    attachmentLayout.removeAllViews();
                    int urlsListLength = alert.attachmentUrls.size();
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 20, 0, 0);
                    for (int i = 0; i < urlsListLength; i++) {
                        TextView tvUrl = new TextView(requireContext());
                        tvUrl.setText("Allegato " + (i + 1));
                        tvUrl.setTypeface(tvNoElements.getTypeface(), Typeface.NORMAL);
                        tvUrl.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.corner_radius_10dp, activity.getTheme()));
                        tvUrl.setBackgroundTintList(getResources().getColorStateList(R.color.main_color, activity.getTheme()));
                        tvUrl.setPadding(20, 20, 20, 20);
                        tvUrl.setLayoutParams(params);
                        tvUrl.setTextColor(getResources().getColorStateList(R.color.light_white_night_black, activity.getTheme()));
                        int finalI = i;
                        tvUrl.setOnClickListener((view2) -> downloadAndOpenFile(alert.attachmentUrls.get(finalI)));
                        attachmentLayout.addView(tvUrl);
                    }

                    detailsLayout.setVisibility(View.VISIBLE);
                    obscureLayoutView.setVisibility(View.VISIBLE);
                    pbLoadingPage.setVisibility(View.GONE);
                });
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
    public void onStart() {
        loadDataAndViews();
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
        viewsLayout.removeAllViews();
        allAlerts = new Vector<>();
        super.onPause();
    }

    @Override
    public void onStop() {
        //FIXME: Salva solo gli ultimi avvisi caricati
        AppData.saveAlertsString(activity, new JsonHelper().saveAlertsToString(allAlertsToSave));
        super.onStop();
    }
}
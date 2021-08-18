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

package com.giua.app.ui.circolari;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.giua.app.R;
import com.giua.app.ui.ObscureLayoutView;
import com.giua.objects.Newsletter;
import com.giua.webscraper.DownloadedFile;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class CircolariFragment extends Fragment {

    LinearLayout layout;
    Context context;
    List<Newsletter> allNewsletter = new Vector<>();
    ProgressBar progressBarLoadingPage;
    ScrollView scrollView;
    LinearLayout attachmentLayout;
    ObscureLayoutView obscureButton;
    ImageView btnFilter;
    LinearLayout filterLayout;
    ProgressBar progressBarLoadingNewsletters;
    TextView tvNoElements;
    FragmentActivity activity;
    SwipeRefreshLayout swipeRefreshLayout;
    View root;
    boolean isDownloading = false;
    int currentPage = 1;
    boolean loadedAllPages = false;
    boolean loadingPage = false;
    boolean hasCompletedLoading = false;
    boolean isFilterApplied = false;
    boolean onlyNotRead = false;
    boolean canSendErrorMessage = true;
    String filterDate = "";
    String filterText = "";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_circolari, container, false);

        context = getContext();
        layout = root.findViewById(R.id.newsletter_linear_layout);
        progressBarLoadingPage = root.findViewById(R.id.circolari_loading_page_bar);
        scrollView = root.findViewById(R.id.newsletter_scroll_view);
        attachmentLayout = root.findViewById(R.id.attachment_layout);
        obscureButton = root.findViewById(R.id.newsletter_obscure_layout);
        tvNoElements = root.findViewById(R.id.newsletter_fragment_no_elements_view);
        btnFilter = root.findViewById(R.id.newsletter_filter_button);
        filterLayout = root.findViewById(R.id.newsletter_filter_layout);
        swipeRefreshLayout = root.findViewById(R.id.newsletter_swipe_refresh_layout);

        activity = requireActivity();

        progressBarLoadingNewsletters = new ProgressBar(getContext());
        progressBarLoadingNewsletters.setId(View.generateViewId());

        scrollView.setOnScrollChangeListener(this::onScrollViewScrolled);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        attachmentLayout.setOnClickListener((view) -> {
        });
        btnFilter.setOnClickListener(this::btnFilterOnClick);

        obscureButton.setOnClickListener((view) -> {
            view.setVisibility(View.GONE);
            attachmentLayout.setVisibility(View.GONE);
            filterLayout.setVisibility(View.GONE);
            attachmentLayout.removeAllViews();
            ((CheckBox) root.findViewById(R.id.newsletter_filter_checkbox)).setChecked(onlyNotRead);
            ((EditText) root.findViewById(R.id.newsletter_filter_date)).setText(filterDate);
            ((EditText) root.findViewById(R.id.newsletter_filter_text)).setText(filterText);
        });

        root.findViewById(R.id.newsletter_fragment_btn_go_up).setOnClickListener((view -> scrollView.smoothScrollTo(0, 0)));

        root.findViewById(R.id.newsletter_filter_btn_confirm).setOnClickListener(this::btnFilterConfirmOnClick);

        addNewslettersToViewAsync();

        return root;
    }

    private void addNewslettersToViewAsync() {
        loadingPage = true;
        hasCompletedLoading = false;
        if (currentPage > 1 && progressBarLoadingNewsletters.getParent() == null)
            layout.addView(progressBarLoadingNewsletters);

        if (!loadedAllPages) {
            new Thread(() -> {
                try {
                    if (!isFilterApplied) {
                        allNewsletter = GlobalVariables.gS.getAllNewslettersWithFilter(onlyNotRead, filterDate, filterText, currentPage, true);
                        isFilterApplied = true;
                    } else
                        allNewsletter = GlobalVariables.gS.getAllNewsletters(currentPage, true);

                    if (allNewsletter != null) {
                        hasCompletedLoading = true;
                        if (allNewsletter.isEmpty() && currentPage == 1)
                            activity.runOnUiThread(() -> tvNoElements.setVisibility(View.VISIBLE));
                        else if (allNewsletter.isEmpty())
                            loadedAllPages = true;
                        activity.runOnUiThread(this::addNewslettersToView);
                        currentPage++;
                    }

                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(getString(R.string.your_connection_error), root);
                        if (currentPage == 1)
                            tvNoElements.setVisibility(View.VISIBLE);
                        progressBarLoadingPage.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                    allNewsletter = new Vector<>();

                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(getString(R.string.site_connection_error), root);
                        if (currentPage == 1)
                            tvNoElements.setVisibility(View.VISIBLE);
                        progressBarLoadingPage.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                    allNewsletter = new Vector<>();
                }
                activity.runOnUiThread(() -> layout.removeView(progressBarLoadingNewsletters));
                swipeRefreshLayout.setRefreshing(false);
                loadingPage = false;
            }).start();
        } else {
            layout.removeView(progressBarLoadingNewsletters);
            swipeRefreshLayout.setRefreshing(false);
            loadingPage = false;
        }

    }

    private void addNewslettersToView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 40, 0, 0);

        for (Newsletter newsletter : allNewsletter) {
            NewsletterView newsletterView = new NewsletterView(context, null, newsletter, () -> onClickDocument(newsletter), () -> onClickAttachmentImage(newsletter));
            newsletterView.setLayoutParams(params);

            layout.addView(newsletterView);
        }

        progressBarLoadingPage.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void onClickSingleAttachment(String url) {
        if (!isDownloading) {
            downloadAndOpenFile(url);
        }
    }

    private void onClickAttachmentImage(Newsletter newsletter) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 130);
        List<String> allAttachments = newsletter.attachments;

        int counter = 0;
        for (String attachment : allAttachments) {
            TextView tvAttachment = new TextView(getContext());
            tvAttachment.setText("Allegato " + (counter + 1));
            tvAttachment.setOnClickListener((view) -> onClickSingleAttachment(attachment));
            tvAttachment.setId(View.generateViewId());
            tvAttachment.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.corner_radius_10dp, context.getTheme()));
            tvAttachment.setBackgroundTintList(getResources().getColorStateList(R.color.general_view_color, context.getTheme()));
            tvAttachment.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.varelaroundregular));
            tvAttachment.setGravity(Gravity.CENTER);
            tvAttachment.setTextSize(16f);

            if (counter % 2 != 0)
                tvAttachment.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black, context.getTheme())).withAlpha(40));

            tvAttachment.setLayoutParams(params);

            counter++;

            attachmentLayout.addView(tvAttachment);
        }

        attachmentLayout.setVisibility(View.VISIBLE);
        obscureButton.setVisibility(View.VISIBLE);
    }

    private void onClickDocument(Newsletter newsletter) {
        if (!isDownloading) {
            downloadAndOpenFile(newsletter.detailsUrl);
        }
    }

    private void onScrollViewScrolled(View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (!loadedAllPages && !loadingPage && !view.canScrollVertically(100) && scrollY - oldScrollY > 10) {
            addNewslettersToViewAsync();
        }
        if (scrollY - oldScrollY > 0)
            root.findViewById(R.id.newsletter_fragment_btn_go_up).setVisibility(View.VISIBLE);
        if (!view.canScrollVertically(-500))
            root.findViewById(R.id.newsletter_fragment_btn_go_up).setVisibility(View.GONE);
    }

    private void onRefresh() {
        layout.removeViews(1, layout.getChildCount() - 1);
        loadedAllPages = false;
        currentPage = 1;
        isFilterApplied = false;
        addNewslettersToViewAsync();
    }

    private void btnFilterConfirmOnClick(View view) {
        onlyNotRead = ((CheckBox) root.findViewById(R.id.newsletter_filter_checkbox)).isChecked();
        filterDate = ((TextView) root.findViewById(R.id.newsletter_filter_date)).getText().toString();
        filterText = ((TextView) root.findViewById(R.id.newsletter_filter_text)).getText().toString();

        //il regex funziona dal 2009 fino al 2039
        if (!filterDate.matches("^((2009)|(20[1-3][0-9])-((0[1-9])|(1[0-2])))$") && !filterDate.equals("")) {
            setErrorMessage("Data non valida", root);
            //btnFilterOnClick(view);
            return;
        }

        layout.removeViews(1, layout.getChildCount() - 1);
        filterLayout.setVisibility(View.GONE);
        obscureButton.setVisibility(View.GONE);
        isFilterApplied = false;
        currentPage = 1;
        loadedAllPages = false;
        tvNoElements.setVisibility(View.GONE);
        addNewslettersToViewAsync();
    }

    private void btnFilterOnClick(View view) {
        obscureButton.setVisibility(View.VISIBLE);
        filterLayout.setVisibility(View.VISIBLE);
        ((CheckBox) root.findViewById(R.id.newsletter_filter_checkbox)).setChecked(onlyNotRead);
        ((EditText) root.findViewById(R.id.newsletter_filter_date)).setText(filterDate);
        ((EditText) root.findViewById(R.id.newsletter_filter_text)).setText(filterText);
    }

    /**
     * Scarica e salva dall'url un file col nome di "circolare" e lo apre chiamando openFile
     */
    private void downloadAndOpenFile(String url) {
        isDownloading = true;
        progressBarLoadingPage.setZ(10f);
        progressBarLoadingPage.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                DownloadedFile downloadedFile = GlobalVariables.gS.download(url);
                FileOutputStream out = new FileOutputStream(requireContext().getFilesDir() + "/" + "circolare." + downloadedFile.fileExtension);
                if (downloadedFile.data != null && downloadedFile.data.length > 0) {
                    out.write(downloadedFile.data);
                    out.close();
                    openFile("circolare." + downloadedFile.fileExtension, downloadedFile.fileExtension);
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
            activity.runOnUiThread(() -> progressBarLoadingPage.setVisibility(View.GONE));
        }).start();
    }

    /**
     * Apre il file col nome di "circolare"
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

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

package com.giua.app.ui.fragments.newsletters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
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

import com.giua.app.AppData;
import com.giua.app.GlobalVariables;
import com.giua.app.IGiuaAppFragment;
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.app.ThreadManager;
import com.giua.app.ui.fragments.ObscureLayoutView;
import com.giua.objects.Newsletter;
import com.giua.utils.JsonHelper;
import com.giua.webscraper.DownloadedFile;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class NewslettersFragment extends Fragment implements IGiuaAppFragment {

    LinearLayout layout;
    Context context;
    List<Newsletter> allNewsletter = new Vector<>();
    List<Newsletter> allNewsletterOld = new Vector<>();
    List<Newsletter> allNewsletterToSave = new Vector<>();
    ProgressBar pbLoadingPage;
    ScrollView scrollView;
    LinearLayout attachmentLayout;
    ObscureLayoutView obscureLayoutView;
    ImageView ivFilter;
    LinearLayout filterLayout;
    ProgressBar pbLoadingNewsletters;
    TextView tvNoElements;
    FragmentActivity activity;
    SwipeRefreshLayout swipeRefreshLayout;
    ThreadManager threadManager;
    View root;
    boolean isDownloading = false;
    int currentPage = 1;
    boolean loadedAllPages = false;
    boolean loadingPage = false;
    boolean hasCompletedLoading = false;
    boolean isFilterApplied = false;
    boolean onlyNotRead = false;
    boolean canSendErrorMessage = true;
    boolean offlineMode = false;
    String filterDate = "";
    String filterText = "";
    boolean demoMode = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        demoMode = SettingsData.getSettingBoolean(requireContext(), SettingKey.DEMO_MODE);
        if (getArguments() != null)
            offlineMode = getArguments().getBoolean("offline");
        root = inflater.inflate(R.layout.fragment_newsletters, container, false);

        allNewsletter = new Vector<>();
        allNewsletterOld = new Vector<>();
        allNewsletterToSave = new Vector<>();
        isDownloading = false;
        currentPage = 1;
        loadedAllPages = false;
        loadingPage = false;
        hasCompletedLoading = false;
        isFilterApplied = false;
        onlyNotRead = false;
        canSendErrorMessage = true;
        offlineMode = false;
        filterDate = "";
        filterText = "";

        context = getContext();
        layout = root.findViewById(R.id.newsletter_linear_layout);
        pbLoadingPage = root.findViewById(R.id.circolari_loading_page_bar);
        scrollView = root.findViewById(R.id.newsletter_scroll_view);
        attachmentLayout = root.findViewById(R.id.attachment_layout);
        obscureLayoutView = root.findViewById(R.id.newsletter_obscure_layout);
        tvNoElements = root.findViewById(R.id.newsletter_fragment_no_elements_view);
        ivFilter = root.findViewById(R.id.newsletter_filter_button);
        filterLayout = root.findViewById(R.id.newsletter_filter_layout);
        swipeRefreshLayout = root.findViewById(R.id.newsletter_swipe_refresh_layout);

        activity = requireActivity();
        threadManager = new ThreadManager();

        pbLoadingNewsletters = new ProgressBar(getContext());
        pbLoadingNewsletters.setId(View.generateViewId());

        scrollView.setOnScrollChangeListener(this::onScrollViewScrolled);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        attachmentLayout.setOnClickListener((view) -> {
        });
        ivFilter.setOnClickListener(this::btnFilterOnClick);

        obscureLayoutView.setOnClickListener(this::obscureViewOnClick);

        root.findViewById(R.id.newsletter_fragment_btn_go_up).setOnClickListener((view -> scrollView.smoothScrollTo(0, 0)));

        root.findViewById(R.id.newsletter_filter_btn_confirm).setOnClickListener(this::btnFilterConfirmOnClick);

        return root;
    }

    @Override
    public void loadDataAndViews() {
        loadingPage = true;
        hasCompletedLoading = false;
        if (currentPage > 1 && pbLoadingNewsletters.getParent() == null)
            layout.addView(pbLoadingNewsletters);

        if (!loadedAllPages) {
            threadManager.addAndRun(() -> {
                try {
                    if (!offlineMode) {
                        if (!isFilterApplied) {
                            if (!allNewsletter.isEmpty())
                                allNewsletterOld = allNewsletter;
                            allNewsletter = GlobalVariables.gS.getAllNewslettersWithFilter(onlyNotRead, filterDate, filterText, currentPage, true);
                            isFilterApplied = true;
                        } else
                            allNewsletter = GlobalVariables.gS.getAllNewsletters(currentPage, true);
                    } else {
                        loadedAllPages = true;
                        try {
                            allNewsletter = new JsonHelper().parseJsonForNewsletters(AppData.getNewslettersString(requireContext()));
                        } catch (Exception ignored) {
                        }
                    }

                    if (allNewsletter != null) {
                        hasCompletedLoading = true;
                        if (currentPage == 1)
                            allNewsletterToSave = allNewsletter;
                        if (allNewsletter.isEmpty() && currentPage == 1)
                            activity.runOnUiThread(() -> tvNoElements.setVisibility(View.VISIBLE));
                        else if (allNewsletter.isEmpty())
                            loadedAllPages = true;
                        activity.runOnUiThread(this::addViews);
                        currentPage++;
                    }
                } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.your_connection_error), root);
                        if (currentPage == 1)
                            tvNoElements.setVisibility(View.VISIBLE);
                        pbLoadingPage.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                    allNewsletter = new Vector<>();

                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.site_connection_error), root);
                        if (currentPage == 1)
                            tvNoElements.setVisibility(View.VISIBLE);
                        pbLoadingPage.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                    allNewsletter = new Vector<>();
                } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                    activity.runOnUiThread(() -> {
                        setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                        if (currentPage == 1)
                            tvNoElements.setVisibility(View.VISIBLE);
                        pbLoadingPage.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    });
                    allNewsletter = new Vector<>();
                }
                activity.runOnUiThread(() -> layout.removeView(pbLoadingNewsletters));
                swipeRefreshLayout.setRefreshing(false);
                loadingPage = false;
            });
        } else {
            layout.removeView(pbLoadingNewsletters);
            swipeRefreshLayout.setRefreshing(false);
            loadingPage = false;
        }

    }

    @Override
    public void addViews() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 40, 0, 0);

        for (Newsletter newsletter : allNewsletter) {
            NewsletterView newsletterView = new NewsletterView(context, null, newsletter);
            newsletterView.findViewById(R.id.newsletter_view_btn_document).setOnClickListener((view) -> onClickDocument(newsletter));

            if (newsletter.attachments != null && !newsletter.attachments.isEmpty()) {
                newsletterView.findViewById(R.id.newsletter_view_btn_attachment).setOnClickListener((view) -> onClickAttachmentImage(newsletter));
            } else {
                newsletterView.findViewById(R.id.newsletter_view_btn_attachment).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.non_vote, context.getTheme())));
                newsletterView.findViewById(R.id.newsletter_view_btn_attachment).setAlpha(0.3f);
            }

            newsletterView.setLayoutParams(params);

            layout.addView(newsletterView);
        }

        pbLoadingPage.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    //region Listeners
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

        attachmentLayout.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_show_effect));
        attachmentLayout.setVisibility(View.VISIBLE);
        obscureLayoutView.show(activity);
    }

    private void onClickDocument(Newsletter newsletter) {
        if (!isDownloading) {
            downloadAndOpenFile(newsletter.detailsUrl);
        }
    }

    private void onScrollViewScrolled(View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (!loadedAllPages && !loadingPage && !view.canScrollVertically(100) && scrollY - oldScrollY > 10) {
            loadDataAndViews();
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
        loadDataAndViews();
    }

    private void btnFilterConfirmOnClick(View view) {
        boolean onlyNotReadTemp = ((CheckBox) root.findViewById(R.id.newsletter_filter_checkbox)).isChecked();
        String filterDateTemp = ((TextView) root.findViewById(R.id.newsletter_filter_date)).getText().toString();
        String filterTextTemp = ((TextView) root.findViewById(R.id.newsletter_filter_text)).getText().toString();

        if (onlyNotReadTemp != onlyNotRead || !filterDateTemp.equals(filterDate) || !filterTextTemp.equals(filterText)) {   //Se è cambiato qualcosa allora procedi

            //il regex funziona dal 2009 fino al 2039
            if (!filterDateTemp.matches("^((2009)|(20[1-3][0-9])-((0[1-9])|(1[0-2])))$") && !filterDateTemp.equals("")) {
                setErrorMessage("Data non valida", root);
                //btnFilterOnClick(view);
                return;
            }

            onlyNotRead = onlyNotReadTemp;
            filterDate = filterDateTemp;
            filterText = filterTextTemp;
            layout.removeViews(1, layout.getChildCount() - 1);
            filterLayout.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_hide_effect));
            filterLayout.setVisibility(View.GONE);
            obscureLayoutView.hide(activity);
            isFilterApplied = false;
            currentPage = 1;
            loadedAllPages = false;
            tvNoElements.setVisibility(View.GONE);
            loadDataAndViews();
        } else {
            filterLayout.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_hide_effect));
            filterLayout.setVisibility(View.GONE);
            obscureLayoutView.hide(activity);
        }
    }

    private void btnFilterOnClick(View view) {
        filterLayout.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_show_effect));
        filterLayout.setVisibility(View.VISIBLE);
        obscureLayoutView.show(activity);
        ((CheckBox) root.findViewById(R.id.newsletter_filter_checkbox)).setChecked(onlyNotRead);
        ((EditText) root.findViewById(R.id.newsletter_filter_date)).setText(filterDate);
        ((EditText) root.findViewById(R.id.newsletter_filter_text)).setText(filterText);
    }

    private void obscureViewOnClick(View view) {
        if (filterLayout.getVisibility() == View.VISIBLE) {
            filterLayout.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_hide_effect));
            filterLayout.setVisibility(View.GONE);
        }
        obscureLayoutView.hide(activity);
        if (attachmentLayout.getVisibility() == View.VISIBLE) {
            attachmentLayout.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.visualizer_hide_effect));
            attachmentLayout.setVisibility(View.GONE);
        }
        attachmentLayout.removeAllViews();
        ((CheckBox) root.findViewById(R.id.newsletter_filter_checkbox)).setChecked(onlyNotRead);
        ((EditText) root.findViewById(R.id.newsletter_filter_date)).setText(filterDate);
        ((EditText) root.findViewById(R.id.newsletter_filter_text)).setText(filterText);
    }

    //endregion

    //region Metodi

    private boolean compareNewsletterLists(List<Newsletter> l1, List<Newsletter> l2) {
        int l1length = l1.size();

        if (l1length != l2.size())
            return false;

        for (int i = 0; i < l1length; i++)
            if (l1.get(i).number != l2.get(i).number)
                return false;

        return true;
    }

    /**
     * Scarica e salva dall'url un file col nome di "circolare" e lo apre chiamando openFile
     */
    private void downloadAndOpenFile(String url) {
        isDownloading = true;
        pbLoadingPage.setZ(10f);
        pbLoadingPage.setVisibility(View.VISIBLE);
        threadManager.addAndRun(() -> {
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
                activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.your_connection_error), root));
            } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.site_connection_error), root));
            } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                activity.runOnUiThread(() -> setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root));
            } catch (IOException e) {
                e.printStackTrace();
            }
            isDownloading = false;
            activity.runOnUiThread(() -> pbLoadingPage.setVisibility(View.GONE));
        });
    }

    /**
     * Apre il file col nome di "circolare"
     */
    private void openFile(String fileName, String fileExtension) {
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setData(FileProvider.getUriForFile(activity, "com.giua.app.fileprovider", new File(requireContext().getFilesDir() + "/" + fileName)));
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent intent = Intent.createChooser(target, "Apri con:");
        try {
            startActivity(intent);
        } catch (Exception e) {
            setErrorMessage("Non è stata trovata alcuna app compatibile con il tipo di file " + fileExtension, root);
        }
    }

    public void setErrorMessage(String message, View root) {
        if (!threadManager.isDestroyed() && canSendErrorMessage)
            Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    //endregion

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
        super.onPause();
    }

    @Override
    public void onStop() {
        if (!allNewsletterToSave.isEmpty() && !offlineMode && !demoMode)
            AppData.saveNewslettersString(activity, new JsonHelper().saveNewslettersToString(allNewsletterToSave));
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        threadManager.destroyAllAndNullMe();
        super.onDestroyView();
    }
}

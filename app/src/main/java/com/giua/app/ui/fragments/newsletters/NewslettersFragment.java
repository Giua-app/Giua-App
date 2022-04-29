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

package com.giua.app.ui.fragments.newsletters;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.giua.app.R;
import com.giua.app.SettingKey;
import com.giua.app.SettingsData;
import com.giua.app.ui.activities.DrawerActivity;
import com.giua.app.ui.views.ObscureLayoutView;
import com.giua.objects.Newsletter;
import com.giua.webscraper.DownloadedFile;
import com.giua.webscraper.GiuaScraperExceptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class NewslettersFragment extends Fragment implements IGiuaAppFragment {

    FragmentActivity activity;
    View root;

    List<Newsletter> allNewsletters = new Vector<>();
    List<Newsletter> allNewsletterToSave = new Vector<>();

    LinearLayout newslettersLayout;
    SwipeRefreshLayout swipeRefreshLayout;
    ScrollView scrollView;
    ProgressBar pbDownloading;
    ProgressBar pbLoadingNewsletters;
    TextView tvNoElements;
    FloatingActionButton buttonGoUp;

    ObscureLayoutView obscureLayoutView;
    LinearLayout attachmentLayout;
    LinearLayout filterLayout;

    String filterDate = "";
    String filterText = "";

    int currentPage = 1;

    boolean isDownloading = false;
    boolean loadedAllPages = false;
    boolean loadingPage = false;
    boolean isFilterApplied = false;
    boolean onlyNotRead = false;
    boolean canSendErrorMessage = true;
    boolean offlineMode = false;
    boolean isFragmentDestroyed = false;
    boolean demoMode = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        demoMode = SettingsData.getSettingBoolean(requireActivity(), SettingKey.DEMO_MODE);
        root = inflater.inflate(R.layout.fragment_newsletters, container, false);

        newslettersLayout = root.findViewById(R.id.newsletter_linear_layout);
        pbDownloading = root.findViewById(R.id.circolari_loading_page_bar);
        scrollView = root.findViewById(R.id.newsletter_scroll_view);
        attachmentLayout = root.findViewById(R.id.attachment_layout);
        obscureLayoutView = root.findViewById(R.id.newsletter_obscure_layout);
        tvNoElements = root.findViewById(R.id.newsletter_fragment_no_elements_view);
        filterLayout = root.findViewById(R.id.newsletter_filter_layout);
        swipeRefreshLayout = root.findViewById(R.id.newsletter_swipe_refresh_layout);
        View filterCardView = root.findViewById(R.id.newsletter_filter_cardview);
        buttonGoUp = root.findViewById(R.id.newsletter_fragment_btn_go_up);
        View buttonFilterConfirm = root.findViewById(R.id.newsletter_filter_btn_confirm);

        activity = requireActivity();
        pbLoadingNewsletters = new ProgressBar(getContext());

        scrollView.setOnScrollChangeListener(this::onScrollViewScrolled);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        obscureLayoutView.setOnClickListener(this::obscureViewOnClick);
        filterCardView.setOnClickListener(this::btnFilterOnClick);
        buttonGoUp.setOnClickListener((view -> scrollView.smoothScrollTo(0, 0)));
        buttonFilterConfirm.setOnClickListener(this::btnFilterConfirmOnClick);

        offlineMode = activity.getIntent().getBooleanExtra("offline", false);

        activity.getSystemService(NotificationManager.class).cancel(10);

        return root;
    }

    @Override
    public void loadOfflineDataAndViews() {
        tvNoElements.setText("Non disponibile offline");
        tvNoElements.setVisibility(View.VISIBLE);
        root.findViewById(R.id.newsletter_filter_cardview).setVisibility(View.INVISIBLE);
    }

    @Override
    public void loadDataAndViews() {
        loadingPage = true;
        if (currentPage == 1)
            swipeRefreshLayout.setRefreshing(true);
        else if (currentPage > 1 && pbLoadingNewsletters.getParent() == null)
            newslettersLayout.addView(pbLoadingNewsletters);

        if (loadedAllPages) {
            activity.runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                newslettersLayout.removeView(pbLoadingNewsletters);
            });
            loadingPage = false;
            return;
        }

        GlobalVariables.gsThread.addTask(() -> {
            try {
                if (offlineMode) loadedAllPages = true;
                else {
                    if (isFilterApplied)
                        allNewsletters = GlobalVariables.gS.getNewslettersPage(false).getAllNewsletters(currentPage);
                    else {
                        allNewsletters = GlobalVariables.gS.getNewslettersPage(false).getAllNewslettersWithFilter(onlyNotRead, filterDate, filterText);
                        isFilterApplied = true;
                    }
                }

                if (allNewsletters == null || isFragmentDestroyed) return;

                activity.runOnUiThread(() -> tvNoElements.setVisibility(View.GONE));
                if (currentPage == 1)   //Salviamo solo la prima pagina per l'offline
                    allNewsletterToSave = allNewsletters;
                if (allNewsletters.isEmpty() && currentPage == 1)
                    activity.runOnUiThread(() -> tvNoElements.setVisibility(View.VISIBLE));
                else if (allNewsletters.isEmpty())
                    loadedAllPages = true;
                activity.runOnUiThread(this::addViews);
                currentPage++;
            } catch (GiuaScraperExceptions.YourConnectionProblems e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.your_connection_error), root);
                    if (currentPage == 1)
                        tvNoElements.setVisibility(View.VISIBLE);
                });
                allNewsletters = new Vector<>();

            } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.site_connection_error), root);
                    if (currentPage == 1)
                        tvNoElements.setVisibility(View.VISIBLE);
                });
                allNewsletters = new Vector<>();
            } catch (GiuaScraperExceptions.MaintenanceIsActiveException e) {
                activity.runOnUiThread(() -> {
                    setErrorMessage(activity.getString(R.string.maintenance_is_active_error), root);
                    if (currentPage == 1)
                        tvNoElements.setVisibility(View.VISIBLE);
                });
                allNewsletters = new Vector<>();
            } catch (GiuaScraperExceptions.NotLoggedIn e) {
                activity.runOnUiThread(() -> {
                    ((DrawerActivity) activity).startActivityManager();
                });
            }
            activity.runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                newslettersLayout.removeView(pbLoadingNewsletters);
            });
            loadingPage = false;
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void addViews() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 40, 0, 0);

        for (Newsletter newsletter : allNewsletters) {
            NewsletterView newsletterView = new NewsletterView(activity, null, newsletter);
            View btnNewsletterDocument = newsletterView.findViewById(R.id.newsletter_view_btn_document);
            View btnNewsletterAttachment = newsletterView.findViewById(R.id.newsletter_view_btn_attachment);

            newsletterView.setOnTouchListener(this::newsletterViewOnTouchListener);
            btnNewsletterDocument.setOnClickListener((view) -> {
                onClickDocument(newsletter);
                newsletterView.markAsRead();
            });

            if (newsletter.attachmentsUrl != null && !newsletter.attachmentsUrl.isEmpty()) {
                btnNewsletterAttachment.setOnClickListener((view) -> onClickAttachmentImage(newsletter));
            } else {
                btnNewsletterAttachment.setForeground(null);
                btnNewsletterAttachment.setAlpha(0.3f);
            }

            newsletterView.setLayoutParams(params);

            newslettersLayout.addView(newsletterView);
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onBackPressed() {
        if (obscureLayoutView.isShown()) {
            obscureLayoutView.performClick();
            return true;
        }
        return false;
    }

    //region

    //Qui viene gestito lo swipe della circolare
    private boolean newsletterViewOnTouchListener(View view, MotionEvent motionEvent) {
        NewsletterView newsletterView = (NewsletterView) view;

        //Se ce' un animazione in corso allora non fare nulla
        if (newsletterView.upperView.getAnimation() != null) return false;

        DisplayMetrics realMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(realMetrics);
        float adaptiveHalfWidth = (float) realMetrics.widthPixels * 240 / 1080;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                int historyLength = motionEvent.getHistorySize();
                if (historyLength > 1 && newsletterView.upperView.getTranslationX() >= newsletterView.getNormalTranslationX()) {
                    if (newsletterView.upperView.getTranslationX() > 20) {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        swipeRefreshLayout.setEnabled(false);
                    }

                    newsletterView.moveTo(motionEvent.getRawX());

                    LinearLayout leftLayout = newsletterView.findViewById(R.id.newsletter_left_layout);
                    float alpha = newsletterView.upperView.getTranslationX() / adaptiveHalfWidth;
                    leftLayout.setAlpha(alpha);
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (!newsletterView.newsletter.isRead() && newsletterView.upperView.getTranslationX() >= adaptiveHalfWidth) {
                    newsletterView.markAsReadWithAnimation();
                    GlobalVariables.gsThread.addTask(() -> GlobalVariables.gS.getNewslettersPage(false).markNewsletterAsRead(newsletterView.newsletter));
                } else
                    newsletterView.makeComeBackAnimation();

                scrollView.requestDisallowInterceptTouchEvent(false);
                swipeRefreshLayout.setEnabled(true);
                return true;
            case MotionEvent.ACTION_CANCEL:
                newsletterView.makeComeBackAnimation();
                scrollView.requestDisallowInterceptTouchEvent(false);
                swipeRefreshLayout.setEnabled(true);
                return true;
        }
        return false;
    }

    private void onClickSingleAttachment(String url) {
        if (!isDownloading) {
            downloadAndOpenFile(url);
        }
    }

    private void onClickAttachmentImage(Newsletter newsletter) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 130);
        List<String> allAttachments = newsletter.attachmentsUrl;

        int counter = 0;
        for (String attachment : allAttachments) {
            TextView tvAttachment = new TextView(getContext());
            tvAttachment.setText("Allegato " + (counter + 1));
            tvAttachment.setOnClickListener((view) -> onClickSingleAttachment(attachment));
            tvAttachment.setId(View.generateViewId());
            tvAttachment.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.corner_radius_10dp, activity.getTheme()));
            tvAttachment.setBackgroundTintList(activity.getResources().getColorStateList(R.color.general_view_color, activity.getTheme()));
            tvAttachment.setTypeface(ResourcesCompat.getFont(requireActivity(), R.font.varelaroundregular));
            tvAttachment.setGravity(Gravity.CENTER);
            tvAttachment.setTextSize(16f);
            tvAttachment.setForeground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ripple_effect, activity.getTheme()));

            tvAttachment.setLayoutParams(params);

            counter++;

            attachmentLayout.addView(tvAttachment);
        }

        attachmentLayout.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.visualizer_show_effect));
        attachmentLayout.setVisibility(View.VISIBLE);
        obscureLayoutView.show();
    }

    private void onClickDocument(Newsletter newsletter) {
        if (!isDownloading)
            downloadAndOpenFile(newsletter.detailsUrl);
    }

    private void onScrollViewScrolled(View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (!loadedAllPages && !loadingPage && !view.canScrollVertically(100) && scrollY - oldScrollY > 10)
            loadDataAndViews();
        if (scrollY - oldScrollY > 0)
            buttonGoUp.setVisibility(View.VISIBLE);
        if (!view.canScrollVertically(-500))
            buttonGoUp.setVisibility(View.GONE);
    }

    private void onRefresh() {
        newslettersLayout.removeViews(1, newslettersLayout.getChildCount() - 1);
        loadedAllPages = false;
        currentPage = 1;
        isFilterApplied = false;
        if (!offlineMode)
            loadDataAndViews();
        else
            loadOfflineDataAndViews();
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
            newslettersLayout.removeViews(1, newslettersLayout.getChildCount() - 1);
            filterLayout.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.visualizer_hide_effect));
            filterLayout.setVisibility(View.GONE);
            obscureLayoutView.hide();
            isFilterApplied = false;
            currentPage = 1;
            loadedAllPages = false;
            tvNoElements.setVisibility(View.GONE);
            loadDataAndViews();
        } else {
            filterLayout.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.visualizer_hide_effect));
            filterLayout.setVisibility(View.GONE);
            obscureLayoutView.hide();
        }
    }

    private void btnFilterOnClick(View view) {
        filterLayout.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.visualizer_show_effect));
        filterLayout.setVisibility(View.VISIBLE);
        obscureLayoutView.show();
        ((CheckBox) root.findViewById(R.id.newsletter_filter_checkbox)).setChecked(onlyNotRead);
        ((EditText) root.findViewById(R.id.newsletter_filter_date)).setText(filterDate);
        ((EditText) root.findViewById(R.id.newsletter_filter_text)).setText(filterText);
    }

    private void obscureViewOnClick(View view) {
        if (filterLayout.getVisibility() == View.VISIBLE) {
            filterLayout.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.visualizer_hide_effect));
            filterLayout.setVisibility(View.GONE);
        }
        obscureLayoutView.hide();
        if (attachmentLayout.getVisibility() == View.VISIBLE) {
            attachmentLayout.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.visualizer_hide_effect));
            attachmentLayout.setVisibility(View.GONE);
            attachmentLayout.removeAllViews();
        }
        ((CheckBox) root.findViewById(R.id.newsletter_filter_checkbox)).setChecked(onlyNotRead);
        ((EditText) root.findViewById(R.id.newsletter_filter_date)).setText(filterDate);
        ((EditText) root.findViewById(R.id.newsletter_filter_text)).setText(filterText);
    }

    //endregion

    //region Metodi

    private void makeComeBackAnimation(NewsletterView v, float fromX, boolean isOnlyClosing) {
        TranslateAnimation comeBackAnimation = new TranslateAnimation(fromX, v.getNormalTranslationX(), v.upperView.getTranslationY(), v.upperView.getTranslationY());
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        comeBackAnimation.setDuration(100);
        alphaAnimation.setDuration(100);
        comeBackAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.findViewById(R.id.newsletter_left_layout).startAnimation(alphaAnimation);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.findViewById(R.id.newsletter_left_layout).setAlpha(0);
                if (!isOnlyClosing)
                    v.markAsReadWithAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        v.upperView.startAnimation(comeBackAnimation);
    }

    private void makeMarkAsReadAnimation(NewsletterView v, DisplayMetrics dm) {
        TranslateAnimation goAnimation = new TranslateAnimation(v.upperView.getTranslationX(), dm.widthPixels, v.upperView.getTranslationY(), v.upperView.getTranslationY());
        goAnimation.setDuration(150);

        goAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                makeComeBackAnimation(v, dm.widthPixels, false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.upperView.startAnimation(goAnimation);
    }

    /**
     * Scarica e salva dall'url un file col nome di "circolare" e lo apre chiamando openFile
     */
    private void downloadAndOpenFile(String url) {
        isDownloading = true;
        pbDownloading.setZ(10f);
        pbDownloading.setVisibility(View.VISIBLE);
        GlobalVariables.gsThread.addTask(() -> {
            try {
                DownloadedFile downloadedFile = GlobalVariables.gS.download(url);

                if (isFragmentDestroyed) return;

                FileOutputStream out = new FileOutputStream(requireActivity().getCacheDir() + "/" + "circolare." + downloadedFile.fileExtension);
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
            } catch (IOException ignored) {
            }

            isDownloading = false;
            activity.runOnUiThread(() -> pbDownloading.setVisibility(View.GONE));
        });
    }

    /**
     * Apre il file col nome di "circolare"
     */
    private void openFile(String fileName, String fileExtension) {
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setData(FileProvider.getUriForFile(activity, "com.giua.app.fileprovider", new File(requireActivity().getCacheDir() + "/" + fileName)));
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(target);
        } catch (Exception e) {
            setErrorMessage("Non è stata trovata alcuna app compatibile con il tipo di file " + fileExtension.toUpperCase(), root);
        }
    }

    public void setErrorMessage(String message, View root) {
        if (!isFragmentDestroyed && canSendErrorMessage)
            Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    //endregion

    @Override
    public void onStart() {
        if (!offlineMode)
            loadDataAndViews();
        else
            loadOfflineDataAndViews();
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
        //Cose per offline
        /*if (!allNewsletterToSave.isEmpty() && !offlineMode && !demoMode)
            AppData.saveNewslettersString(activity, new JsonHelper().saveNewslettersToString(allNewsletterToSave));*/
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        isFragmentDestroyed = true;
        super.onDestroyView();
    }
}

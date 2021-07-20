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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.giua.app.DrawerActivity;
import com.giua.app.GlobalVariables;
import com.giua.app.R;
import com.giua.objects.Newsletter;
import com.giua.webscraper.GiuaScraperExceptions;

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
    ImageButton obscureButton;
    ProgressBar progressBarLoadingNewsletters;
    TextView tvNoElements;
    FragmentActivity activity;
    boolean isDownloading = false;
    int currentPage = 1;
    boolean loadedAllPages = false;
    boolean loadingPage = false;
    boolean hasCompletedLoading = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_circolari, container, false);

        context = getContext();
        layout = root.findViewById(R.id.newsletter_linear_layout);
        progressBarLoadingPage = root.findViewById(R.id.circolari_loading_page_bar);
        scrollView = root.findViewById(R.id.newsletter_scroll_view);
        attachmentLayout = root.findViewById(R.id.attachment_layout);
        obscureButton = root.findViewById(R.id.obscure_layout_image_button2);
        tvNoElements = root.findViewById(R.id.newsletter_fragment_no_elements_view);
        activity = requireActivity();

        progressBarLoadingNewsletters = new ProgressBar(getContext());
        progressBarLoadingNewsletters.setId(View.generateViewId());

        scrollView.setOnScrollChangeListener(this::onScrollViewScrolled);

        attachmentLayout.setOnClickListener((view) -> {
        });

        obscureButton.setOnClickListener((view) -> {
            view.setVisibility(View.GONE);
            attachmentLayout.setVisibility(View.GONE);
            attachmentLayout.removeAllViews();
        });

        addNewslettersToViewAsync();

        return root;
    }

    private void addNewslettersToViewAsync() {
        loadingPage = true;
        hasCompletedLoading = false;
        if (progressBarLoadingPage.getVisibility() == View.GONE && progressBarLoadingNewsletters.getParent() == null)
            layout.addView(progressBarLoadingNewsletters);

        new Thread(() -> {
            if (!loadedAllPages) {
                try {
                    preventInfiniteLoading(System.nanoTime());
                    allNewsletter = GlobalVariables.gS.getAllNewsletters(currentPage, true);
                    if (allNewsletter == null)
                        return;
                    hasCompletedLoading = true;
                    if (allNewsletter.isEmpty() && currentPage == 1)
                        tvNoElements.setVisibility(View.VISIBLE);
                    else if (allNewsletter.isEmpty())
                        loadedAllPages = true;
                    activity.runOnUiThread(this::addNewslettersToView);
                    currentPage++;
                } catch (GiuaScraperExceptions.InternetProblems e) {
                    DrawerActivity.setErrorMessage(getString(R.string.your_connection_error), layout);
                    activity.runOnUiThread(() -> {
                        if (currentPage == 1)
                            tvNoElements.setVisibility(View.VISIBLE);
                        progressBarLoadingPage.setVisibility(View.GONE);
                    });
                    allNewsletter = new Vector<>();
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    DrawerActivity.setErrorMessage(getString(R.string.site_connection_error), layout);
                    activity.runOnUiThread(() -> {
                        if (currentPage == 1)
                            tvNoElements.setVisibility(View.VISIBLE);
                        progressBarLoadingPage.setVisibility(View.GONE);
                    });
                    allNewsletter = new Vector<>();
                }
            }
            activity.runOnUiThread(() -> layout.removeView(progressBarLoadingNewsletters));
            loadingPage = false;
        }).start();
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
    }

    private void preventInfiniteLoading(long firstTime) {
        new Thread(() -> {
            while (!hasCompletedLoading) {
                if (hasCompletedLoading)
                    return;
                else {
                    if (System.nanoTime() - firstTime > 5000000000L) {
                        DrawerActivity.setErrorMessage(getString(R.string.your_connection_error), layout);
                        activity.runOnUiThread(() -> {
                            if (currentPage == 1)
                                tvNoElements.setVisibility(View.VISIBLE);
                            progressBarLoadingPage.setVisibility(View.GONE);
                        });
                        return;
                    }
                }
            }
        }).start();
    }

    /**
     * Scarica e salva dall'url un pdf col nome di circolare.pdf e lo mette nella cartella Download
     *
     * @param url
     */
    private void downloadFile(String url) {
        isDownloading = true;
        progressBarLoadingPage.setZ(10f);
        progressBarLoadingPage.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                FileOutputStream out = new FileOutputStream(requireContext().getFilesDir() + "/circolare.pdf");
                out.write(GlobalVariables.gS.download(url));
                out.close();
                openFile();
            } catch (GiuaScraperExceptions.InternetProblems e) {
                DrawerActivity.setErrorMessage(getString(R.string.your_connection_error), attachmentLayout);
            } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                DrawerActivity.setErrorMessage(getString(R.string.site_connection_error), attachmentLayout);
            } catch (IOException e) {
                e.printStackTrace();
            }
            isDownloading = false;
            activity.runOnUiThread(() -> progressBarLoadingPage.setVisibility(View.GONE));
        }).start();
    }

    /**
     * Apre il pdf col nome di circolare.pdf nella cartella Download
     */
    private void openFile() {
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(FileProvider.getUriForFile(requireContext(), "com.giua.app.provider", new File(requireContext().getFilesDir() + "/circolare.pdf")), "application/pdf");
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent intent = Intent.createChooser(target, "Apri con:");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void onClickSingleAttachment(String url) {
        if (!isDownloading) {
            downloadFile(url);
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
            downloadFile(newsletter.detailsUrl);
        }
    }

    private void onScrollViewScrolled(View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (!loadedAllPages && !loadingPage && !view.canScrollVertically(100) && scrollY - oldScrollY > 10) {
            addNewslettersToViewAsync();
        }
    }
}

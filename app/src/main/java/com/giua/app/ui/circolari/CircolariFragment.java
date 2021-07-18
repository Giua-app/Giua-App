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

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.giua.app.DrawerActivity;
import com.giua.app.R;
import com.giua.objects.Newsletter;
import com.giua.webscraper.GiuaScraper;
import com.giua.webscraper.GiuaScraperExceptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class CircolariFragment extends Fragment {

    //TODO: aggiungere la ricerca delle circolari per oggetto e anche i filtri per i mesi come nel sito
    //TODO: Rivedere un po la UI perchè non è il massimo

    GiuaScraper gS;
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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_circolari, container, false);

        Intent intent = requireActivity().getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");

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
        if (progressBarLoadingPage.getVisibility() == View.GONE && progressBarLoadingNewsletters.getParent() == null)
            layout.addView(progressBarLoadingNewsletters);

        new Thread(() -> {
            if (!loadedAllPages) {
                try {
                    allNewsletter = gS.getAllNewsletters(currentPage, true);
                    if (allNewsletter.isEmpty() && currentPage == 1)
                        tvNoElements.setVisibility(View.VISIBLE);
                    else if (allNewsletter.isEmpty())
                        loadedAllPages = true;
                    activity.runOnUiThread(this::addNewslettersToView);
                    currentPage++;
                } catch (GiuaScraperExceptions.InternetProblems e) {
                    DrawerActivity.setErrorMessage("E' stato riscontrato qualche problema con la tua connessione", layout);
                    activity.runOnUiThread(() -> {
                        tvNoElements.setVisibility(View.VISIBLE);
                        progressBarLoadingPage.setVisibility(View.GONE);
                    });
                    allNewsletter = new Vector<>();
                } catch (GiuaScraperExceptions.SiteConnectionProblems e) {
                    DrawerActivity.setErrorMessage("E' stato riscontrato qualche problema con la connessione del registro", layout);
                    activity.runOnUiThread(() -> {
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
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                try {
                    FileOutputStream out = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/circolare.pdf");
                    out.write(gS.download(url));
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                DrawerActivity.setErrorMessage("Impossibile salvare il file: permesso negato", layout);
            }
            openFile();
            isDownloading = false;
            activity.runOnUiThread(() -> progressBarLoadingPage.setVisibility(View.GONE));
        }).start();
    }

    /**
     * Apre il pdf col nome di circolare.pdf nella cartella Download
     */
    private void openFile() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(FileProvider.getUriForFile(getContext(), "com.giua.app.provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/circolare.pdf")), "application/pdf");
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent intent = Intent.createChooser(target, "Apri la circolare con:");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            DrawerActivity.setErrorMessage("Impossibile leggere i file: permesso negato", layout);
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
            tvAttachment.setTypeface(ResourcesCompat.getFont(getContext(), R.font.varelaroundregular));
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

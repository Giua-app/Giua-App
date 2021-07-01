package com.giua.app.ui.circolari;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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

import com.giua.app.DrawerActivity;
import com.giua.app.R;
import com.giua.objects.Newsletter;
import com.giua.webscraper.GiuaScraper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CircolariFragment extends Fragment {

    //TODO: aggiungere la ricerca delle circolari per oggetto e anche i filtri per i mesi come nel sito
    //TODO: Rivedere un po la UI perchè non è il massimo
    //TODO: Aggiungere qualche caricamento quando si scaricano i pdf e quando si stanno ottenendo altre circolari

    GiuaScraper gS;
    LinearLayout layout;
    Context context;
    List<Newsletter> allNewsletter;
    ProgressBar progressBar;
    ScrollView scrollView;
    LinearLayout attachmentLayout;
    ImageButton obscureButton;
    Handler handler = new Handler();
    boolean isDownloading = false;
    int currentPage = 1;
    boolean loadedAllPages = false;
    boolean loadingPage = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_circolari, container, false);

        Intent intent = getActivity().getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");

        context = getContext();
        layout = root.findViewById(R.id.newsletter_linear_layout);
        progressBar = root.findViewById(R.id.circolari_loading_page_bar);
        scrollView = root.findViewById(R.id.newsletter_scroll_view);
        attachmentLayout = root.findViewById(R.id.attachment_layout);
        obscureButton = root.findViewById(R.id.obscure_layout_image_button2);

        scrollView.setOnScrollChangeListener(this::onScrollViewScrolled);

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
        handler.post(() -> {
            if (!loadedAllPages) {
                allNewsletter = gS.getAllNewsletters(currentPage, true);
                if (allNewsletter.size() == 0)
                    loadedAllPages = true;
                addNewslettersToView();
                currentPage++;
            }
            loadingPage = false;
        });
    }

    private void addNewslettersToView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 40, 0, 0);

        for (Newsletter newsletter : allNewsletter) {
            NewsletterView newsletterView = new NewsletterView(context, null, newsletter, () -> onClickDocument(newsletter), () -> onClickAttachmentImage(newsletter));
            newsletterView.setLayoutParams(params);

            layout.addView(newsletterView);
        }

        progressBar.setVisibility(View.GONE);
    }

    /**
     * Scarica e salva dall'url un pdf col nome di circolare.pdf e lo mette nella cartella Download
     *
     * @param url
     */
    private void downloadFile(String url) {
        isDownloading = true;
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
        isDownloading = false;
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
        downloadFile(url);
        openFile();
    }

    private void onClickAttachmentImage(Newsletter newsletter) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        List<String> allAttachments = newsletter.attachments;

        params.setMargins(0, 50, 0, 0);

        int counter = 0;
        for (String attachment : allAttachments) {
            TextView tvAttachment = new TextView(getContext());
            tvAttachment.setText("Allegato " + (counter + 1));
            tvAttachment.setOnClickListener((view) -> onClickSingleAttachment(attachment));
            tvAttachment.setId(View.generateViewId());
            tvAttachment.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.corner_radius_10dp, context.getTheme()));
            tvAttachment.setTypeface(ResourcesCompat.getFont(getContext(), R.font.varelaroundregular));
            tvAttachment.setTextSize(16f);
            //tvAttachment.setHeight(120);

            if (counter != 0)
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
            openFile();
        }
    }

    private void onScrollViewScrolled(View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (!view.canScrollVertically(100) && !loadedAllPages && !loadingPage) {
            addNewslettersToViewAsync();
        }
    }
}

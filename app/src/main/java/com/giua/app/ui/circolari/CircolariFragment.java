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
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.giua.app.R;
import com.giua.objects.Newsletter;
import com.giua.webscraper.GiuaScraper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CircolariFragment extends Fragment {
    GiuaScraper gS;
    LinearLayout layout;
    Context context;
    Handler h;
    List<Newsletter> allNewsletter;
    ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_circolari, container, false);

        Intent intent = getActivity().getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");

        context = getContext();
        layout = root.findViewById(R.id.newsletter_linear_layout);
        progressBar = root.findViewById(R.id.circolari_loading_page_bar);

        h = new Handler();
        h.post(this::addNewslettersToViewAsync);

        return root;
    }

    private void addNewslettersToViewAsync() {
        Handler handler = new Handler();
        handler.post(() -> {
            allNewsletter = gS.getAllNewsletters(0, false);
            addNewslettersToView();
        });
    }

    private void addNewslettersToView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 40, 0, 0);

        for (Newsletter newsletter : allNewsletter) {
            NewsletterView newsletterView = new NewsletterView(context, null, newsletter);
            newsletterView.setLayoutParams(params);

            newsletterView.setOnClickListener(this::onClickNewsletterView);

            layout.addView(newsletterView);
        }

        progressBar.setVisibility(View.GONE);
    }

    private void downloadFile(String url) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {
                FileOutputStream out = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/circolare.pdf");
                out.write(gS.download(url));
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onClickNewsletterView(View view) {
        downloadFile(((NewsletterView) view).newsletter.detailsUrl);

        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(FileProvider.getUriForFile(getContext(), "com.giua.app.provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/circolare.pdf")), "application/pdf");
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent intent = Intent.createChooser(target, "Apri la circolare con:");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}

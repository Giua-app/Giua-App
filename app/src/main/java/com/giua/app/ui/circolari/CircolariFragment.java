package com.giua.app.ui.circolari;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.giua.app.R;
import com.giua.objects.Newsletter;
import com.giua.webscraper.GiuaScraper;

import java.util.List;

public class CircolariFragment extends Fragment {
    GiuaScraper gS;
    LinearLayout layout;
    Context context;
    WebView pdfViewer;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_circolari, container, false);

        Intent intent = getActivity().getIntent();
        gS = (GiuaScraper) intent.getSerializableExtra("giuascraper");

        context = getContext();
        layout = root.findViewById(R.id.newsletter_linear_layout);
        pdfViewer = root.findViewById(R.id.newsletter_webview);

        addNewslettersToView();

        return root;
    }

    private void addNewslettersToView() {
        List<Newsletter> allNewsletter = gS.getAllNewsletters(0, false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 40, 0, 0);

        for (Newsletter newsletter : allNewsletter) {
            NewsletterView newsletterView = new NewsletterView(context, null, newsletter.status, newsletter.number, newsletter.date, newsletter.newslettersObject, newsletter.detailsUrl);
            newsletterView.setLayoutParams(params);

            newsletterView.setOnClickListener(this::onClickNewsletterView);

            layout.addView(newsletterView);
        }
    }

    public void onClickNewsletterView(View view) {
        pdfViewer.setVisibility(View.VISIBLE);
        pdfViewer.getSettings().setJavaScriptEnabled(true);
        pdfViewer.getSettings().setPluginState(WebSettings.PluginState.ON);
        pdfViewer.loadUrl(gS.getSiteURL() + ((NewsletterView) view).url);
    }
}

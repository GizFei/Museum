package com.giz.museum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WebViewActivity extends AppCompatActivity {
    /**
     * 内置浏览器页面
     */
    public static final String INTENT_URL = "intent_url";

    public static Intent newIntent(Context context, String url){
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(INTENT_URL, url);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view_activity);

        String url = getIntent().getStringExtra(INTENT_URL);
        WebView webView = findViewById(R.id.view_web_view);
        final TextView textView = findViewById(R.id.back_bar_tv);
        final ProgressBar progressBar = findViewById(R.id.view_progress_bar);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        progressBar.setMax(100);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress == 100){
                    progressBar.setVisibility(View.GONE);
                }else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                textView.setText(title);
            }
        });
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);
    }
}

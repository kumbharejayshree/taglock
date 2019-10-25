package com.tagloy.taglock.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tagloy.taglock.R;

public class WebActivity extends AppCompatActivity {

    WebView webView;
    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mContext = this;
        webView = findViewById(R.id.webView);
        webView.loadUrl("https://www.google.in");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, String host, String realm) {
//                super.onReceivedHttpAuthRequest(view, handler, host, realm);
                handler.proceed("tagloc","India@150");
            }
        });
    }
}

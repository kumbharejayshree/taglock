package com.tagloy.taglock.activity;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.HttpAuthHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.tagloy.taglock.R;
import com.tagloy.taglock.utils.TaglockDeviceInfo;

public class WebActivity extends AppCompatActivity {

    WebView webView;
    Context mContext;
    TaglockDeviceInfo taglockDeviceInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mContext = this;
        webView = findViewById(R.id.webView);
        taglockDeviceInfo = new TaglockDeviceInfo(mContext);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(getApplicationContext().getFilesDir().getAbsolutePath() + "/cache");
        webSettings.setDatabaseEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, String host, String realm) {
                handler.proceed("tagloc","India@150");
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                taglockDeviceInfo.showMessage("Your Internet Connection May not be active Or " + error);
            }
        });
        webView.loadUrl("http://fast.com");
    }
}

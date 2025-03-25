package com.cometchat.chatuikit.extensions.collaborative;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;

/**
 * CometChatCollaborativeActivity is a Activity class which is used to load
 * extension such as whiteboard and write board in webView.
 *
 * <p>
 * Created On - 26 November 2020
 */

@SuppressLint("SetJavaScriptEnabled")
public class CometChatWebViewActivity extends AppCompatActivity {
    private static final String TAG = CometChatWebViewActivity.class.getSimpleName();

    private String url = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cometchat_activity_cometchat_webview);
        WebView webView = findViewById(R.id.web_view);
        ImageView backIcon = findViewById(R.id.iv_back);
        TextView textView = findViewById(R.id.tv_title);
        backIcon.setOnClickListener(v -> finish());
        if (getIntent().hasExtra(UIKitConstants.IntentStrings.TITLE)) {
            textView.setText(getIntent().getStringExtra(UIKitConstants.IntentStrings.TITLE));
        }
        if (getIntent().hasExtra(UIKitConstants.IntentStrings.URL)) {
            url = getIntent().getStringExtra(UIKitConstants.IntentStrings.URL);
        }
        if (url != null) {
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setSupportZoom(true);
            webView.getSettings().setUseWideViewPort(true);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return super.shouldOverrideUrlLoading(view, request);
                }
            });
            webView.loadUrl(url);
        }
    }
}

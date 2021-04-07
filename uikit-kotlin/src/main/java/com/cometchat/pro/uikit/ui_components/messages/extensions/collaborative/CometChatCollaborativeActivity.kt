package com.cometchat.pro.uikit.ui_components.messages.extensions.collaborative

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants

class CometChatCollaborativeActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_webview)
        webView = findViewById(R.id.webview)
        if (intent.hasExtra(UIKitConstants.IntentStrings.URL)) {
            url = intent.getStringExtra(UIKitConstants.IntentStrings.URL)
        }
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.loadUrl(url)
    }
}
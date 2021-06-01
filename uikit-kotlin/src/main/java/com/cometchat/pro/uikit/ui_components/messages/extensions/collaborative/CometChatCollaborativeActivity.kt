package com.cometchat.pro.uikit.ui_components.messages.extensions.collaborative

import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants

class CometChatCollaborativeActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var url: String
    private lateinit var toolbar : Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cometchat_webview)
        webView = findViewById(R.id.webview)
        toolbar = findViewById(R.id.toolbar)

        toolbar.title = ""
        setSupportActionBar(toolbar)
        toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (intent.hasExtra(UIKitConstants.IntentStrings.URL)) {
            url = intent.getStringExtra(UIKitConstants.IntentStrings.URL)
        }
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.loadUrl(url)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
package screen

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.cometchat.pro.uikit.R
import constant.StringContract

class CometChatWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comet_chat_web_view)
        webView = findViewById(R.id.webview)
        if (intent.hasExtra(StringContract.IntentStrings.URL)) {
            url = intent.getStringExtra(StringContract.IntentStrings.URL)
        }
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.loadUrl(url)
    }
}
package com.cometchat.sampleapp.compose.e2e.helpers

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Drives a SECOND CometChat session (e.g. "d1") on the SAME emulator by running the CometChat
 * JavaScript SDK inside an in-test [WebView].
 *
 * The WebView's JS engine is fully independent of the app's NATIVE CometChat singleton — it has
 * its own WebSocket and its own (IndexedDB) storage — so it can be ONLINE and emit TYPING toward
 * the app user without disturbing the app's session. This enables single-emulator presence/typing
 * tests with no second device, no extra app module, and no host tooling (WebView ships on every
 * Android image, so it doesn't need a Chrome/Google-Play system image either).
 *
 * Loads the JS SDK from a CDN, so the emulator needs internet access (the suite already does).
 */
object CometChatJsDriver {

    private var webView: WebView? = null

    @Volatile
    private var status: String = ""

    /**
     * Starts the driver: loads the JS SDK, logs in [uid], and for action "typing" loops
     * startTyping toward [receiver]/[receiverType]. For action "presence" it just stays online.
     * Blocks until login succeeds or [loginTimeoutMs] elapses.
     *
     * @return true if the JS session logged in successfully.
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun start(
        uid: String,
        action: String,
        receiver: String = "",
        receiverType: String = "user",
        loginTimeoutMs: Long = 45_000
    ): Boolean {
        val instr = InstrumentationRegistry.getInstrumentation()
        val html = instr.context.assets.open("cometchat_driver.html")
            .bufferedReader().use { it.readText() }
        status = ""

        instr.runOnMainSync {
            val wv = WebView(instr.targetContext)
            wv.settings.javaScriptEnabled = true
            wv.settings.domStorageEnabled = true
            @Suppress("DEPRECATION")
            wv.settings.databaseEnabled = true
            wv.addJavascriptInterface(object {
                @JavascriptInterface
                fun onStatus(s: String) { status = s }
            }, "AndroidDriver")
            wv.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String?) {
                    val js = "startDriver(" +
                        "${q(E2ETestHelper.appId)},${q(E2ETestHelper.region)},${q(E2ETestHelper.authKey)}," +
                        "${q(uid)},${q(action)},${q(receiver)},${q(receiverType)})"
                    view.evaluateJavascript(js, null)
                }
            }
            webView = wv
            // An https base URL gives a secure origin so the JS SDK's IndexedDB/WebSocket work.
            wv.loadDataWithBaseURL("https://cometchat-e2e.local/", html, "text/html", "utf-8", null)
        }

        val deadline = System.currentTimeMillis() + loginTimeoutMs
        while (System.currentTimeMillis() < deadline) {
            val s = status
            if (s.startsWith("login-ok")) return true
            if (s.startsWith("ERR")) return false
            Thread.sleep(500)
        }
        return status.startsWith("login-ok")
    }

    /**
     * Stops typing the proper way: clears the typing loop AND sends endTyping, so the receiver's
     * typing indicator clears immediately (without this, only a timeout would eventually clear it).
     * Keeps the session alive — call [stop] for full teardown.
     */
    fun stopTyping() {
        val instr = InstrumentationRegistry.getInstrumentation()
        instr.runOnMainSync { webView?.evaluateJavascript("stopTyping()", null) }
        Thread.sleep(500)
    }

    /** Cancels the call this driver initiated (action "call_audio"/"call_video"), dismissing the
     *  receiver's incoming-call UI. Keeps the session alive — call [stop] for full teardown. */
    fun cancelCall() {
        val instr = InstrumentationRegistry.getInstrumentation()
        instr.runOnMainSync { webView?.evaluateJavascript("cancelCall()", null) }
        Thread.sleep(500)
    }

    /** Rapidly toggles startTyping/endTyping [count] times toward [receiver] (stress test). */
    fun rapidTyping(receiver: String, receiverType: String = "user", count: Int = 12) {
        val instr = InstrumentationRegistry.getInstrumentation()
        instr.runOnMainSync {
            webView?.evaluateJavascript("rapidTyping($count, ${q(receiver)}, ${q(receiverType)})", null)
        }
    }

    /** Latest status reported by the JS page (e.g. "login-ok:d1", "ERR:..."). For diagnostics. */
    fun status(): String = status

    /** Tears down the WebView session. Call in @After. */
    fun stop() {
        val instr = InstrumentationRegistry.getInstrumentation()
        instr.runOnMainSync {
            try { webView?.destroy() } catch (_: Exception) {}
            webView = null
        }
    }

    private fun q(s: String): String = "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'"
}

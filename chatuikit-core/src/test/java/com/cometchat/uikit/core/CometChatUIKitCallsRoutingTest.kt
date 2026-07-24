package com.cometchat.uikit.core

import android.content.Context
import android.content.res.AssetManager
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.calls.core.CometChatCalls
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream

/**
 * Tests for the Calls SDK routing on CometChatUIKit's file-based init door (ENG-37369).
 *
 * The settings door (initFromSettings) must:
 *  - route the Calls SDK through CometChatCalls.initFromSettings (ai-agent
 *    attribution) when uiKit.enableCalling is true — never the plain init,
 *  - skip the Calls SDK entirely when enableCalling is absent (defaults false),
 *  - still report Chat success to the caller when the Calls SDK init fails.
 *
 * Uses Kotest FunSpec style (required by project's test runner configuration).
 * Chat/Calls SDK statics are intercepted with Mockito's inline mockStatic.
 */
class CometChatUIKitCallsRoutingTest : FunSpec({

    lateinit var chatStatic: MockedStatic<CometChat>
    lateinit var callsStatic: MockedStatic<CometChatCalls>

    // Captured listeners so tests can drive SDK callbacks deterministically
    var chatListener: CometChat.CallbackListener<String>? = null
    var callsListener: CometChatCalls.CallbackListener<String>? = null

    fun contextWithSettings(json: String): Context {
        val assets = mock<AssetManager>()
        whenever(assets.open("cometchat-settings.json"))
            .thenReturn(ByteArrayInputStream(json.toByteArray()))
        val context = mock<Context>()
        whenever(context.assets).thenReturn(assets)
        return context
    }

    class RecordingCallback : CometChat.CallbackListener<String>() {
        var successResult: String? = null
        var error: CometChatException? = null
        override fun onSuccess(result: String) { successResult = result }
        override fun onError(e: CometChatException?) { error = e }
    }

    beforeTest {
        chatListener = null
        callsListener = null

        chatStatic = Mockito.mockStatic(CometChat::class.java)
        chatStatic.`when`<Unit> { CometChat.initFromSettings(any(), any()) }.thenAnswer { inv ->
            @Suppress("UNCHECKED_CAST")
            chatListener = inv.getArgument(1) as CometChat.CallbackListener<String>
            null
        }

        callsStatic = Mockito.mockStatic(CometChatCalls::class.java)
        callsStatic.`when`<Unit> { CometChatCalls.initFromSettings(any(), any()) }.thenAnswer { inv ->
            @Suppress("UNCHECKED_CAST")
            callsListener = inv.getArgument(1) as CometChatCalls.CallbackListener<String>
            null
        }
    }

    afterTest {
        chatStatic.close()
        callsStatic.close()
    }

    test("settings door with enableCalling true routes Calls SDK through initFromSettings, not plain init") {
        val context = contextWithSettings(
            """{"appId":"app1","region":"us","credentials":{"authKey":"k"},"uiKit":{"enableCalling":true}}"""
        )
        val callback = RecordingCallback()

        CometChatUIKit.initFromSettings(context, callback)

        chatListener.shouldNotBeNull()
        chatListener!!.onSuccess("Chat OK")

        // The telemetry-aware door must be used…
        callsStatic.verify({ CometChatCalls.initFromSettings(any(), any()) }, Mockito.times(1))
        // …and the plain init (integrationSource = "manual") must NOT be
        callsStatic.verify({ CometChatCalls.init(any(), any(), any()) }, Mockito.never())

        // Overall success is reported only after the Calls SDK completes
        callback.successResult shouldBe null
        callsListener.shouldNotBeNull()
        callsListener!!.onSuccess("Calls OK")
        callback.successResult shouldBe "Chat OK"
        callback.error shouldBe null
    }

    test("enableCalling absent defaults to false and skips the Calls SDK entirely") {
        val context = contextWithSettings(
            """{"appId":"app1","region":"us","credentials":{"authKey":"k"}}"""
        )
        val callback = RecordingCallback()

        CometChatUIKit.initFromSettings(context, callback)

        chatListener.shouldNotBeNull()
        chatListener!!.onSuccess("Chat OK")

        callback.successResult shouldBe "Chat OK"
        callsStatic.verifyNoInteractions()
    }

    test("Calls SDK init failure on the settings door still reports Chat success") {
        val context = contextWithSettings(
            """{"appId":"app1","region":"us","uiKit":{"enableCalling":true}}"""
        )
        val callback = RecordingCallback()

        CometChatUIKit.initFromSettings(context, callback)
        chatListener!!.onSuccess("Chat OK")

        callsListener.shouldNotBeNull()
        callsListener!!.onError(
            com.cometchat.calls.exceptions.CometChatException("ERR_CALLS", "calls init failed", "")
        )

        callback.successResult shouldBe "Chat OK"
        callback.error shouldBe null
    }

    test("missing settings file reports ERR_SETTINGS_FILE_NOT_FOUND without touching either SDK") {
        val assets = mock<AssetManager>()
        whenever(assets.open("cometchat-settings.json")).thenThrow(java.io.FileNotFoundException("missing"))
        val context = mock<Context>()
        whenever(context.assets).thenReturn(assets)
        val callback = RecordingCallback()

        CometChatUIKit.initFromSettings(context, callback)

        callback.error.shouldNotBeNull()
        callback.error!!.code shouldBe "ERR_SETTINGS_FILE_NOT_FOUND"
        chatStatic.verifyNoInteractions()
        callsStatic.verifyNoInteractions()
    }

    test("missing appId reports ERR_SETTINGS_INVALID without touching either SDK") {
        val context = contextWithSettings("""{"region":"us"}""")
        val callback = RecordingCallback()

        CometChatUIKit.initFromSettings(context, callback)

        callback.error.shouldNotBeNull()
        callback.error!!.code shouldBe "ERR_SETTINGS_INVALID"
        chatStatic.verifyNoInteractions()
        callsStatic.verifyNoInteractions()
    }
})

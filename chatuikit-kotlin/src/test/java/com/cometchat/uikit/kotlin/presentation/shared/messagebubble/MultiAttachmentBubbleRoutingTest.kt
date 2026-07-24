package com.cometchat.uikit.kotlin.presentation.shared.messagebubble

import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiobubble.CometChatAudioBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiosbubble.CometChatAudiosBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filebubble.CometChatFileBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filesbubble.CometChatFilesBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagebubble.CometChatImageBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagesbubble.CometChatImagesBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.textbubble.CometChatTextBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.videobubble.CometChatVideoBubble
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.videosbubble.CometChatVideosBubble
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ENG-36737 — Robolectric tests for the `enableMultipleAttachments` bubble routing in
 * [InternalContentRenderer.createContentView]: the flag decides whether a media factory key
 * resolves to the NEW per-type multi-attachment bubble or the DEPRECATED single bubble
 * (default false, so existing integrations keep their current rendering until they opt in).
 *
 * Audio deliberately routes to [CometChatAudiosBubble] for BOTH voice notes and picker audio when
 * the flag is on — the per-message voice-note split happens inside the bubble, because only the
 * factory key (not the message) is known at view-creation time and RecyclerView recycling
 * requires one view type per factory key.
 *
 * Run: ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*MultiAttachmentBubbleRoutingTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MultiAttachmentBubbleRoutingTest {

    private fun mediaKey(type: String): String =
        BubbleFactory.getKey(CometChatConstants.CATEGORY_MESSAGE, type)

    /** Runs [block] inside a themed activity — the bubble views resolve CometChat theme attrs. */
    private fun withThemedActivity(block: (ComponentActivity) -> Unit) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            block(activity)
        }
        scenario.close()
    }

    @Test
    fun `flag ON routes every media type to its new per-type multi-attachment bubble`() {
        withThemedActivity { activity ->
            assertTrue(
                InternalContentRenderer.createContentView(
                    activity, mediaKey(CometChatConstants.MESSAGE_TYPE_IMAGE), enableMultipleAttachments = true
                ) is CometChatImagesBubble
            )
            assertTrue(
                InternalContentRenderer.createContentView(
                    activity, mediaKey(CometChatConstants.MESSAGE_TYPE_VIDEO), enableMultipleAttachments = true
                ) is CometChatVideosBubble
            )
            assertTrue(
                InternalContentRenderer.createContentView(
                    activity, mediaKey(CometChatConstants.MESSAGE_TYPE_AUDIO), enableMultipleAttachments = true
                ) is CometChatAudiosBubble
            )
            assertTrue(
                InternalContentRenderer.createContentView(
                    activity, mediaKey(CometChatConstants.MESSAGE_TYPE_FILE), enableMultipleAttachments = true
                ) is CometChatFilesBubble
            )
        }
    }

    @Test
    fun `flag OFF routes every media type to the deprecated single bubble`() {
        withThemedActivity { activity ->
            assertTrue(
                InternalContentRenderer.createContentView(
                    activity, mediaKey(CometChatConstants.MESSAGE_TYPE_IMAGE), enableMultipleAttachments = false
                ) is CometChatImageBubble
            )
            assertTrue(
                InternalContentRenderer.createContentView(
                    activity, mediaKey(CometChatConstants.MESSAGE_TYPE_VIDEO), enableMultipleAttachments = false
                ) is CometChatVideoBubble
            )
            assertTrue(
                InternalContentRenderer.createContentView(
                    activity, mediaKey(CometChatConstants.MESSAGE_TYPE_AUDIO), enableMultipleAttachments = false
                ) is CometChatAudioBubble
            )
            assertTrue(
                InternalContentRenderer.createContentView(
                    activity, mediaKey(CometChatConstants.MESSAGE_TYPE_FILE), enableMultipleAttachments = false
                ) is CometChatFileBubble
            )
        }
    }

    @Test
    fun `flag defaults to OFF so existing integrations keep the deprecated bubbles`() {
        withThemedActivity { activity ->
            assertTrue(
                InternalContentRenderer.createContentView(
                    activity, mediaKey(CometChatConstants.MESSAGE_TYPE_IMAGE)
                ) is CometChatImageBubble
            )
        }
    }

    @Test
    fun `the flag does not leak into non-media routing`() {
        withThemedActivity { activity ->
            // Text stays a text bubble either way; unknown keys stay null (fallback path).
            val textKey = BubbleFactory.getKey(CometChatConstants.CATEGORY_MESSAGE, CometChatConstants.MESSAGE_TYPE_TEXT)
            assertTrue(
                InternalContentRenderer.createContentView(activity, textKey, enableMultipleAttachments = true)
                    is CometChatTextBubble
            )
            assertNull(
                InternalContentRenderer.createContentView(activity, "unknown_key", enableMultipleAttachments = true)
            )
        }
    }
}

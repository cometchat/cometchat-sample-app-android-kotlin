package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagebubble

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.kotlin.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ENG-36737 — proves the deprecated single image bubble renders a markdown caption as block-level
 * child views (as the text bubble does) rather than flattening it into one TextView. The caption
 * mixes a fenced code block, a blockquote and a bullet list, so the block container must hold more
 * than one child view and the flat caption TextView must stay hidden.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CometChatImageBubbleCaptionBlockRenderTest {

    private fun withThemedActivity(block: (ComponentActivity) -> Unit) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            block(activity)
        }
        scenario.close()
    }

    private fun imageMessage(caption: String) = MediaMessage().apply {
        id = 1
        type = "image"
        this.caption = caption
        attachment = Attachment().apply {
            fileUrl = "https://example.com/a.jpg"
            fileMimeType = "image/jpeg"
            fileName = "a.jpg"
        }
    }

    @Test
    fun `block-level markdown caption produces multiple child views, not one TextView`() {
        val caption = buildString {
            append("```\n")
            append("val x = 1\n")
            append("```\n")
            append("> a quoted line\n")
            append("- first bullet\n")
            append("- second bullet")
        }

        withThemedActivity { activity ->
            val bubble = CometChatImageBubble(activity)
            bubble.setMessage(imageMessage(caption))
            bubble.setCaption(caption)

            // The block container is the sibling inserted right after the flat caption TextView.
            val captionTextView = bubble.getCaptionTextView()
            val parent = captionTextView.parent as ViewGroup
            val blockContainer = parent.getChildAt(parent.indexOfChild(captionTextView) + 1)

            assertTrue("block container should be a LinearLayout", blockContainer is LinearLayout)
            blockContainer as LinearLayout

            assertEquals("flat caption TextView must be hidden", View.GONE, captionTextView.visibility)
            assertEquals("block container must be visible", View.VISIBLE, blockContainer.visibility)
            assertTrue(
                "block container should hold multiple segment views, got ${blockContainer.childCount}",
                blockContainer.childCount > 1
            )
            assertTrue(
                "block container must not be a single TextView",
                !(blockContainer.childCount == 1 && blockContainer.getChildAt(0) is TextView)
            )
        }
    }

    @Test
    fun `SpannableString caption uses the flat TextView and clears the block container`() {
        withThemedActivity { activity ->
            val bubble = CometChatImageBubble(activity)
            bubble.setMessage(imageMessage("ignored"))
            // First render a markdown caption into the block container...
            bubble.setCaption("- one\n- two")
            // ...then the legacy Spannable path must take over and clear it.
            bubble.setCaption(android.text.SpannableString("plain"))

            val captionTextView = bubble.getCaptionTextView()
            val parent = captionTextView.parent as ViewGroup
            val blockContainer = parent.getChildAt(parent.indexOfChild(captionTextView) + 1) as LinearLayout

            assertEquals(View.VISIBLE, captionTextView.visibility)
            assertEquals("plain", captionTextView.text.toString())
            assertEquals(View.GONE, blockContainer.visibility)
            assertEquals(0, blockContainer.childCount)
        }
    }

    /** Guards the empty-caption reset path a recycled bubble relies on. */
    @Test
    fun `empty caption hides both the flat TextView and the block container`() {
        withThemedActivity { activity ->
            val bubble = CometChatImageBubble(activity)
            bubble.setMessage(imageMessage(""))
            bubble.setCaption("- one\n- two")
            bubble.setCaption(null as String?)

            val captionTextView = bubble.getCaptionTextView()
            val parent = captionTextView.parent as ViewGroup
            val blockContainer = parent.getChildAt(parent.indexOfChild(captionTextView) + 1) as LinearLayout

            assertEquals(View.GONE, captionTextView.visibility)
            assertEquals(View.GONE, blockContainer.visibility)
            assertEquals(0, blockContainer.childCount)
        }
    }
}

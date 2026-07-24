package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiosbubble

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.kotlin.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ENG-37014 — overflow toggle label on [CometChatAudiosBubble].
 *
 * The bug: with more than three audio attachments the collapsed bubble rendered a garbled
 * overflow label ("vShow 7 more") instead of the intended "Show +7 more" with the chevron
 * as a separate, properly-tinted glyph. The fix moved the "+" into the
 * `cometchat_show_n_more` string resource and tints the chevron with the accent color.
 *
 * These tests pin the rendered label text for the collapsed ("Show +N more") and expanded
 * ("Show less") states, the expand/collapse card counts, and the no-toggle case at exactly
 * the collapse threshold.
 *
 * Run: ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatAudiosBubbleToggleTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CometChatAudiosBubbleToggleTest {

    private fun withThemedActivity(block: (ComponentActivity) -> Unit) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            block(activity)
        }
        scenario.close()
    }

    private fun audioAttachment(index: Int): Attachment = Attachment().apply {
        fileUrl = "https://cdn.example.com/audio-$index.mp3"
        fileName = "audio-$index.mp3"
        fileMimeType = "audio/mpeg"
        fileSize = 1024
    }

    private fun audioMessage(count: Int, id: Long = 0L): MediaMessage =
        MediaMessage("receiver-uid", CometChatConstants.MESSAGE_TYPE_AUDIO, CometChatConstants.RECEIVER_TYPE_USER).apply {
            attachments = (1..count).map { audioAttachment(it) }
            this.id = id
        }

    /** Expands the collapsed bubble by clicking its overflow toggle card. */
    private fun expand(bubble: CometChatAudiosBubble) {
        var clickable: View = toggleLabel(bubble)!!
        while (!clickable.hasOnClickListeners() && clickable.parent is View) {
            clickable = clickable.parent as View
        }
        clickable.performClick()
    }

    /** Depth-first search for the first [TextView] whose text matches [predicate]. */
    private fun findTextView(root: View, predicate: (String) -> Boolean): TextView? {
        if (root is TextView && predicate(root.text.toString())) return root
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                findTextView(root.getChildAt(i), predicate)?.let { return it }
            }
        }
        return null
    }

    private fun toggleLabel(bubble: CometChatAudiosBubble): TextView? =
        findTextView(bubble) { it.startsWith("Show") }

    @Test
    fun `collapsed bubble with ten audios labels the overflow as Show +7 more`() {
        withThemedActivity { activity ->
            val bubble = CometChatAudiosBubble(activity)
            bubble.setMessage(audioMessage(10))

            val label = toggleLabel(bubble)
            assertNotNull("expected an overflow toggle for 10 audios", label)
            // The regression rendered "vShow 7 more" — the label must carry the "+" and no glyph text
            assertEquals("Show +7 more", label!!.text.toString())
        }
    }

    @Test
    fun `tapping the toggle expands all cards and flips the label to Show less`() {
        withThemedActivity { activity ->
            val bubble = CometChatAudiosBubble(activity)
            bubble.setMessage(audioMessage(5))

            val collapsedLabel = toggleLabel(bubble)!!
            assertEquals("Show +2 more", collapsedLabel.text.toString())

            // The whole toggle card is the click target — climb from the label to the card
            var clickable: View = collapsedLabel
            while (!clickable.hasOnClickListeners() && clickable.parent is View) {
                clickable = clickable.parent as View
            }
            clickable.performClick()

            assertEquals("Show less", toggleLabel(bubble)!!.text.toString())
        }
    }

    @Test
    fun `re-binding the same message keeps the expanded toggle open`() {
        withThemedActivity { activity ->
            val bubble = CometChatAudiosBubble(activity)
            val message = audioMessage(5, id = 101L)
            bubble.setMessage(message)

            expand(bubble)
            assertEquals("Show less", toggleLabel(bubble)!!.text.toString())

            // A rebind of the same message — e.g. the search-highlight fade ending in
            // notifyItemChanged — must not collapse the toggle. (ENG-37143)
            bubble.setMessage(message)
            assertEquals("Show less", toggleLabel(bubble)!!.text.toString())
        }
    }

    @Test
    fun `binding a different message into a recycled bubble resets the toggle to collapsed`() {
        withThemedActivity { activity ->
            val bubble = CometChatAudiosBubble(activity)
            bubble.setMessage(audioMessage(5, id = 201L))

            expand(bubble)
            assertEquals("Show less", toggleLabel(bubble)!!.text.toString())

            // The same recycled view now binds a different message — it must collapse. (ENG-37143)
            bubble.setMessage(audioMessage(5, id = 202L))
            assertEquals("Show +2 more", toggleLabel(bubble)!!.text.toString())
        }
    }

    @Test
    fun `exactly three audios renders without any overflow toggle`() {
        withThemedActivity { activity ->
            val bubble = CometChatAudiosBubble(activity)
            bubble.setMessage(audioMessage(3))

            assertNull("no toggle expected at the collapse threshold", toggleLabel(bubble))
        }
    }

    @Test
    fun `string resource carries the plus sign inside the label`() {
        withThemedActivity { activity ->
            // Pin the resource itself so a localization edit can't silently regress the format
            assertEquals("Show +7 more", activity.getString(R.string.cometchat_show_n_more, 7))
            assertEquals("Show less", activity.getString(R.string.cometchat_show_less))
        }
    }
}

package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filesbubble

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
 * ENG-37143 — the "Show +N more" overflow toggle on [CometChatFilesBubble] collapsed itself the
 * moment it was expanded when the bubble was opened from a conversation search result.
 *
 * Cause: the search-navigation highlight fade ends in `notifyItemChanged`, which rebinds the
 * ViewHolder and calls [CometChatFilesBubble.setMessage] again. That path used to unconditionally
 * reset `expanded = false`, wiping the just-expanded state on the first tap. The fix preserves the
 * toggle across a rebind of the *same* message and only collapses when the recycled view binds a
 * different message.
 *
 * These tests pin the collapsed/expanded labels plus the two rebind cases that guard the fix.
 *
 * Run: ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CometChatFilesBubbleToggleTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CometChatFilesBubbleToggleTest {

    private fun withThemedActivity(block: (ComponentActivity) -> Unit) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            block(activity)
        }
        scenario.close()
    }

    private fun fileAttachment(index: Int): Attachment = Attachment().apply {
        fileUrl = "https://cdn.example.com/file-$index.pdf"
        fileName = "file-$index.pdf"
        fileMimeType = "application/pdf"
        fileExtension = "pdf"
        fileSize = 2048
    }

    private fun fileMessage(count: Int, id: Long = 0L): MediaMessage =
        MediaMessage("receiver-uid", CometChatConstants.MESSAGE_TYPE_FILE, CometChatConstants.RECEIVER_TYPE_USER).apply {
            attachments = (1..count).map { fileAttachment(it) }
            this.id = id
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

    private fun toggleLabel(bubble: CometChatFilesBubble): TextView? =
        findTextView(bubble) { it.startsWith("Show") }

    /** Expands the collapsed bubble by clicking its overflow toggle card. */
    private fun expand(bubble: CometChatFilesBubble) {
        var clickable: View = toggleLabel(bubble)!!
        while (!clickable.hasOnClickListeners() && clickable.parent is View) {
            clickable = clickable.parent as View
        }
        clickable.performClick()
    }

    @Test
    fun `collapsed bubble with ten files labels the overflow as Show +7 more`() {
        withThemedActivity { activity ->
            val bubble = CometChatFilesBubble(activity)
            bubble.setMessage(fileMessage(10))

            val label = toggleLabel(bubble)
            assertNotNull("expected an overflow toggle for 10 files", label)
            assertEquals("Show +7 more", label!!.text.toString())
        }
    }

    @Test
    fun `tapping the toggle expands all cards and flips the label to Show less`() {
        withThemedActivity { activity ->
            val bubble = CometChatFilesBubble(activity)
            bubble.setMessage(fileMessage(5))

            assertEquals("Show +2 more", toggleLabel(bubble)!!.text.toString())
            expand(bubble)
            assertEquals("Show less", toggleLabel(bubble)!!.text.toString())
        }
    }

    @Test
    fun `re-binding the same message keeps the expanded toggle open`() {
        withThemedActivity { activity ->
            val bubble = CometChatFilesBubble(activity)
            val message = fileMessage(5, id = 301L)
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
            val bubble = CometChatFilesBubble(activity)
            bubble.setMessage(fileMessage(5, id = 401L))

            expand(bubble)
            assertEquals("Show less", toggleLabel(bubble)!!.text.toString())

            // The same recycled view now binds a different message — it must collapse. (ENG-37143)
            bubble.setMessage(fileMessage(5, id = 402L))
            assertEquals("Show +2 more", toggleLabel(bubble)!!.text.toString())
        }
    }

    @Test
    fun `exactly three files renders without any overflow toggle`() {
        withThemedActivity { activity ->
            val bubble = CometChatFilesBubble(activity)
            bubble.setMessage(fileMessage(3))

            assertNull("no toggle expected at the collapse threshold", toggleLabel(bubble))
        }
    }
}

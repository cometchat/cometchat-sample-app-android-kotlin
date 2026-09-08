package com.cometchat.uikit.kotlin.presentation.messagecomposer.ui

import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import com.cometchat.uikit.core.formatter.RichTextFormat
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests the Approach-2 Classic facade [ClassicComposerInputController] over a real [EditText]'s
 * Editable (the source of truth in the View/XML composer). Uses Robolectric for a real EditText.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ClassicComposerInputControllerTest {

    private fun editTextWith(text: String, selStart: Int, selEnd: Int): EditText =
        EditText(ApplicationProvider.getApplicationContext()).apply {
            setText(text)
            setSelection(selStart, selEnd)
        }

    @Test
    fun `reads reflect the edit text`() {
        val et = editTextWith("hello", 5, 5)
        val toggled = mutableListOf<RichTextFormat>()
        val facade = ClassicComposerInputController(et) { toggled.add(it) }

        facade.text shouldBe "hello"
        facade.selection shouldBe 5..5
        facade.isCursorCollapsed shouldBe true
    }

    @Test
    fun `insertAtCursor at a collapsed caret inserts and advances the caret`() {
        val et = editTextWith("hello", 5, 5)
        val facade = ClassicComposerInputController(et) { }

        facade.insertAtCursor("!")

        et.text.toString() shouldBe "hello!"
        et.selectionStart shouldBe 6
    }

    @Test
    fun `insertAtCursor replaces an active selection`() {
        val et = editTextWith("hello", 1, 4) // selects "ell"
        val facade = ClassicComposerInputController(et) { }

        facade.replaceSelection("i")

        et.text.toString() shouldBe "hio"
        et.selectionStart shouldBe 2
    }

    @Test
    fun `insertAtCursor tolerates a reversed selection`() {
        val et = editTextWith("hello", 4, 1) // reversed
        val facade = ClassicComposerInputController(et) { }

        facade.insertAtCursor("i")

        et.text.toString() shouldBe "hio"
    }

    @Test
    fun `toggleFormat delegates to the composer's format handler`() {
        val et = editTextWith("hello", 0, 5)
        val toggled = mutableListOf<RichTextFormat>()
        val facade = ClassicComposerInputController(et) { toggled.add(it) }

        facade.toggleFormat(RichTextFormat.BOLD)

        toggled shouldBe listOf(RichTextFormat.BOLD)
    }

    @Test
    fun `mentionRanges is empty when there are no mention spans`() {
        val facade = ClassicComposerInputController(editTextWith("hello", 0, 0)) { }
        facade.mentionRanges().shouldBeEmpty()
    }
}

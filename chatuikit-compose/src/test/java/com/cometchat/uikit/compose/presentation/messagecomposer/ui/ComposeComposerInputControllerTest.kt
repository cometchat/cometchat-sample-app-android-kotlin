package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import com.cometchat.uikit.core.formatter.ComposerSegment
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.core.formatter.SegmentComposerController
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

/**
 * Tests the Approach-2 Compose facade [ComposeComposerInputController]. It resolves the focused
 * Normal segment's [com.cometchat.uikit.core.formatter.RichTextEditorController] on each call and
 * fires `onMutated` after every mutation (the composer bumps `formatVersion` there to resync the
 * segment's TextFieldValue).
 */
class ComposeComposerInputControllerTest : FunSpec({

    /** A controller with one Normal segment focused, holding [text] with the caret at the end. */
    fun focusedWith(text: String): SegmentComposerController =
        SegmentComposerController().apply {
            val normal = segments.first() as ComposerSegment.Normal
            normal.controller.onTextChanged(text, text.length, text.length)
            setFocusedSegment(normal.id)
        }

    test("reads reflect the focused segment") {
        val sc = focusedWith("hello")
        val facade = ComposeComposerInputController(sc) { }

        facade.text shouldBe "hello"
        facade.selection shouldBe 5..5
        facade.isCursorCollapsed.shouldBeTrue()
    }

    test("insertAtCursor mutates the focused segment and fires onMutated once") {
        val sc = focusedWith("hello")
        var mutations = 0
        val facade = ComposeComposerInputController(sc) { mutations++ }

        facade.insertAtCursor("!")

        facade.text shouldBe "hello!"
        mutations shouldBe 1
    }

    test("replaceSelection swaps the focused segment's selection") {
        val sc = SegmentComposerController().apply {
            val normal = segments.first() as ComposerSegment.Normal
            normal.controller.onTextChanged("hello world", 6, 11) // selects "world"
            setFocusedSegment(normal.id)
        }
        var mutations = 0
        val facade = ComposeComposerInputController(sc) { mutations++ }

        facade.replaceSelection("there")

        facade.text shouldBe "hello there"
        mutations shouldBe 1
    }

    test("toggleFormat delegates to the focused controller and fires onMutated") {
        val sc = focusedWith("hi")
        var mutations = 0
        val facade = ComposeComposerInputController(sc) { mutations++ }

        facade.toggleFormat(RichTextFormat.BOLD)

        val normal = sc.segments.first() as ComposerSegment.Normal
        // At a collapsed caret, toggling adds the format as pending.
        normal.controller.state.pendingFormats shouldContain RichTextFormat.BOLD
        mutations shouldBe 1
    }

    test("mutations are no-ops and onMutated does not fire when no Normal segment is focused") {
        val sc = SegmentComposerController() // nothing focused
        var mutations = 0
        val facade = ComposeComposerInputController(sc) { mutations++ }

        facade.text shouldBe ""
        facade.isCursorCollapsed.shouldBeTrue()

        facade.insertAtCursor("x")
        facade.toggleFormat(RichTextFormat.BOLD)

        mutations shouldBe 0
    }

    test("mentionRanges is empty (not surfaced by the segment composer yet)") {
        val facade = ComposeComposerInputController(focusedWith("hello")) { }
        facade.mentionRanges().shouldBeEmpty()
    }
})

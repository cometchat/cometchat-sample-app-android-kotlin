package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Tests that line format toggles (bullet list, ordered list, blockquote)
 * correctly modify the text and that the text change is visible via
 * the controller's state.
 */
class LineFormatToggleTest : FunSpec({

    test("toggleFormat BULLET_LIST adds prefix to empty text") {
        val controller = RichTextEditorController()
        controller.onTextChanged("hello", 5, 5)
        controller.toggleFormat(RichTextFormat.BULLET_LIST)
        controller.state.text shouldBe "- hello"
        controller.state.selectionStart shouldBe 7
    }

    test("toggleFormat BULLET_LIST removes prefix when already present") {
        val controller = RichTextEditorController()
        controller.onTextChanged("- hello", 7, 7)
        controller.toggleFormat(RichTextFormat.BULLET_LIST)
        controller.state.text shouldBe "hello"
        controller.state.selectionStart shouldBe 5
    }

    test("toggleFormat ORDERED_LIST adds prefix") {
        val controller = RichTextEditorController()
        controller.onTextChanged("hello", 5, 5)
        controller.toggleFormat(RichTextFormat.ORDERED_LIST)
        controller.state.text shouldBe "1. hello"
        controller.state.selectionStart shouldBe 8
    }

    test("toggleFormat BLOCKQUOTE adds prefix") {
        val controller = RichTextEditorController()
        controller.onTextChanged("hello", 5, 5)
        controller.toggleFormat(RichTextFormat.BLOCKQUOTE)
        controller.state.text shouldBe "> hello"
        controller.state.selectionStart shouldBe 7
    }

    test("segment controller toggleFormat on focused Normal modifies text") {
        val segController = SegmentComposerController()
        val firstNormal = segController.segments.first() as ComposerSegment.Normal
        firstNormal.controller.onTextChanged("hello", 5, 5)
        segController.setFocusedSegment(firstNormal.id)

        // Simulate what onFormatClick does
        val focused = segController.focusedSegment as? ComposerSegment.Normal
        focused?.controller?.toggleFormat(RichTextFormat.BULLET_LIST)

        // Verify the text was modified
        firstNormal.controller.state.text shouldBe "- hello"
        firstNormal.controller.state.selectionStart shouldBe 7
    }

    test("line format is detectable from text prefix after toggle") {
        val controller = RichTextEditorController()
        controller.onTextChanged("hello", 5, 5)
        controller.toggleFormat(RichTextFormat.BULLET_LIST)

        val text = controller.state.text
        val cursorPos = controller.state.selectionStart
        val lineStart = text.lastIndexOf('\n', (cursorPos - 1).coerceAtLeast(0)) + 1
        val lineEnd = text.indexOf('\n', cursorPos).let { if (it == -1) text.length else it }
        val currentLine = text.substring(lineStart, lineEnd)

        (currentLine.startsWith("- ") || currentLine.startsWith("• ")) shouldBe true
    }

    // ==================== Double-Enter Exit Tests ====================

    test("double-enter exits bullet list - enter on empty '- ' removes prefix") {
        val controller = RichTextEditorController()
        controller.onTextChanged("- hello", 7, 7)
        controller.onTextChanged("- hello\n", 8, 8)
        controller.state.text shouldBe "- hello\n- "

        // User presses Enter again on the empty "- " line
        controller.onTextChanged("- hello\n- \n", 11, 11)
        // Empty prefix removed, cursor on new empty line after "- hello"
        controller.state.text shouldBe "- hello\n"
        controller.state.selectionStart shouldBe 8
    }

    test("double-enter exits ordered list - enter on empty '2. ' removes prefix") {
        val controller = RichTextEditorController()
        controller.onTextChanged("1. hello", 8, 8)
        controller.onTextChanged("1. hello\n", 9, 9)
        controller.state.text shouldBe "1. hello\n2. "

        controller.onTextChanged("1. hello\n2. \n", 13, 13)
        controller.state.text shouldBe "1. hello\n"
        controller.state.selectionStart shouldBe 9
    }

    test("double-enter exits blockquote - enter on empty '> ' removes prefix") {
        val controller = RichTextEditorController()
        controller.onTextChanged("> hello", 7, 7)
        controller.onTextChanged("> hello\n", 8, 8)
        controller.state.text shouldBe "> hello\n> "

        controller.onTextChanged("> hello\n> \n", 11, 11)
        controller.state.text shouldBe "> hello\n"
        controller.state.selectionStart shouldBe 8
    }
})

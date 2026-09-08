package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Tests for the programmatic text-edit entry points added for custom trailing-toolbar buttons
 * ([RichTextEditorController.insertAtCursor] / [RichTextEditorController.replaceSelection]).
 * These are the mutation primitives the Approach-2 [ComposerInputController] facade delegates to.
 */
class RichTextEditorControllerInsertTest : FunSpec({

    fun controllerWith(text: String, selStart: Int, selEnd: Int): RichTextEditorController =
        RichTextEditorController().apply { onTextChanged(text, selStart, selEnd) }

    // ==================== insertAtCursor ====================

    test("insertAtCursor at a collapsed caret inserts and advances the caret") {
        val controller = controllerWith("hello", 5, 5)

        controller.insertAtCursor("!")

        controller.state.text shouldBe "hello!"
        controller.state.selectionStart shouldBe 6
        controller.state.selectionEnd shouldBe 6
    }

    test("insertAtCursor in the middle inserts at the caret") {
        val controller = controllerWith("hello", 2, 2)

        controller.insertAtCursor("XY")

        controller.state.text shouldBe "heXYllo"
        controller.state.selectionStart shouldBe 4
    }

    test("insertAtCursor replaces an active selection") {
        val controller = controllerWith("hello", 1, 4) // selects "ell"

        controller.insertAtCursor("i")

        controller.state.text shouldBe "hio"
        controller.state.selectionStart shouldBe 2
    }

    test("insertAtCursor tolerates a reversed selection (anchor after focus)") {
        val controller = controllerWith("hello", 4, 1) // reversed

        controller.insertAtCursor("i")

        controller.state.text shouldBe "hio"
        controller.state.selectionStart shouldBe 2
    }

    test("insertAtCursor into empty text appends") {
        val controller = controllerWith("", 0, 0)

        controller.insertAtCursor("hi")

        controller.state.text shouldBe "hi"
        controller.state.selectionStart shouldBe 2
    }

    // ==================== replaceSelection ====================

    test("replaceSelection swaps the selected range") {
        val controller = controllerWith("hello world", 6, 11) // selects "world"

        controller.replaceSelection("there")

        controller.state.text shouldBe "hello there"
        controller.state.selectionStart shouldBe 11
    }

    test("replaceSelection degrades to insert when the caret is collapsed") {
        val controller = controllerWith("hello", 5, 5)

        controller.replaceSelection("!")

        controller.state.text shouldBe "hello!"
        controller.state.selectionStart shouldBe 6
    }
})

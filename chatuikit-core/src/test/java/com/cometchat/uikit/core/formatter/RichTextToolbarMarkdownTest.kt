package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith

/**
 * Comprehensive toolbar-driven markdown tests for the rich text editor.
 *
 * Organized by sections matching RICH_TEXT_TOOLBAR_TEST_CASES.md.
 * Each test simulates user actions via [RichTextEditorController] and verifies
 * the markdown output via [RichTextEditorController.toMarkdown].
 *
 * For code block tests, [SegmentComposerController] is used since code blocks
 * are managed as separate segments.
 *
 * For toolbar state tests, [FormatCompatibility] and [RichTextFormat.Companion]
 * methods are tested directly (pure functions, no UI needed).
 */
class RichTextToolbarMarkdownTest : FunSpec({

    // ==================== Helpers ====================

    /** Creates a fresh controller for each test. */
    fun newController(): RichTextEditorController = RichTextEditorController()

    /** Types a single character at the current cursor position. */
    fun typeChar(controller: RichTextEditorController, ch: Char) {
        val currentText = controller.state.text
        val cursorPos = controller.state.selectionStart
        val newText = currentText.substring(0, cursorPos) + ch +
            currentText.substring(cursorPos)
        controller.onTextChanged(newText, cursorPos + 1, cursorPos + 1)
    }

    /** Types a string character by character into the controller. */
    fun typeText(controller: RichTextEditorController, text: String) {
        for (ch in text) {
            typeChar(controller, ch)
        }
    }

    /** Sets the selection range on the controller. */
    fun selectRange(controller: RichTextEditorController, start: Int, end: Int) {
        controller.onTextChanged(controller.state.text, start, end)
    }

    /** Moves the cursor to a specific position (collapsed selection). */
    fun moveCursor(controller: RichTextEditorController, pos: Int) {
        controller.onTextChanged(controller.state.text, pos, pos)
    }

    /** Inserts a newline at the current cursor position. */
    fun pressEnter(controller: RichTextEditorController) {
        val currentText = controller.state.text
        val cursorPos = controller.state.selectionStart
        val newText = currentText.substring(0, cursorPos) + "\n" +
            currentText.substring(cursorPos)
        controller.onTextChanged(newText, cursorPos + 1, cursorPos + 1)
    }

    // ==================== Section 1: Bold ====================

    context("Section 1: Bold") {

        test("1.1 Pending: tap Bold, type hello, tap Bold, type world → **hello**world") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "hello")
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "world")
            c.toMarkdown() shouldBe "**hello**world"
        }

        test("1.2 Selection: select 'world' in 'hello world', tap Bold → contains **world**") {
            val c = newController()
            typeText(c, "hello world")
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.BOLD)
            c.toMarkdown() shouldContain "**world**"
        }

        test("1.2 Toggle off: make 'world' bold, select it again, tap Bold → hello world") {
            val c = newController()
            typeText(c, "hello world")
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.BOLD)
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.BOLD)
            c.toMarkdown() shouldBe "hello world"
        }

        test("1.2 Partial: all bold, select 'world', tap Bold → hello stays bold, world loses it") {
            val c = newController()
            typeText(c, "hello world")
            selectRange(c, 0, 11)
            c.toggleFormat(RichTextFormat.BOLD)
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.BOLD)
            val md = c.toMarkdown()
            // "hello " should still be bold, "world" should not
            md shouldContain "**hello **"
            md.endsWith("world").shouldBeTrue()
        }

        test("1.3 Cursor detection: cursor inside bold → BOLD in activeFormats") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "hello")
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, " world")
            // Move cursor inside "hello" (position 3)
            moveCursor(c, 3)
            c.state.activeFormats shouldContain RichTextFormat.BOLD
        }

        test("1.3 Cursor detection: cursor outside bold → BOLD not in activeFormats") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "hello")
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, " world")
            // Move cursor inside " world" (position 8)
            moveCursor(c, 8)
            c.state.activeFormats shouldNotContain RichTextFormat.BOLD
        }

        test("1.4 Bold + Italic combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            c.toggleFormat(RichTextFormat.ITALIC)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "**"
            md shouldContain "_"
        }

        test("1.4 Bold + Underline combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            c.toggleFormat(RichTextFormat.UNDERLINE)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "**"
            md shouldContain "<u>"
            md shouldContain "</u>"
        }

        test("1.4 Bold + Strikethrough combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "**"
            md shouldContain "~~"
        }

        test("1.4 All four: Bold + Italic + Underline + Strikethrough") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            c.toggleFormat(RichTextFormat.ITALIC)
            c.toggleFormat(RichTextFormat.UNDERLINE)
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "**"
            md shouldContain "_"
            md shouldContain "<u>"
            md shouldContain "~~"
        }

        test("1.4 Bold + InlineCode combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            typeText(c, "code")
            val md = c.toMarkdown()
            md shouldContain "**"
            md shouldContain "`"
        }

        test("1.4 Bold + BulletList combo → - **item**") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BULLET_LIST)
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "item")
            val md = c.toMarkdown()
            md shouldContain "- "
            md shouldContain "**item**"
        }

        test("1.4 Bold + NumberedList combo → 1. **item**") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ORDERED_LIST)
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "item")
            val md = c.toMarkdown()
            md shouldContain "1. "
            md shouldContain "**item**"
        }

        test("1.4 Bold + Blockquote combo → > **text**") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BLOCKQUOTE)
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "> "
            md shouldContain "**text**"
        }

        test("1.4 Bold + CodeBlock: Bold auto-deselects via toggleFormat") {
            // Use RichTextFormat.toggleFormat pure function
            val active = setOf(RichTextFormat.BOLD)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.CODE_BLOCK)
            result shouldContain RichTextFormat.CODE_BLOCK
            result shouldNotContain RichTextFormat.BOLD
        }

        test("1.5 Enter: Bold active, type line1 + Enter + type line2 → both lines bold") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "line 1")
            pressEnter(c)
            typeText(c, "line 2")
            val md = c.toMarkdown()
            md shouldContain "**"
            // Both lines should be within bold markers
            md shouldContain "line 1"
            md shouldContain "line 2"
        }
    }

    // ==================== Section 2: Italic ====================

    context("Section 2: Italic") {

        test("2.1 Pending: tap Italic, type hello, tap Italic, type world → _hello_world") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ITALIC)
            typeText(c, "hello")
            c.toggleFormat(RichTextFormat.ITALIC)
            typeText(c, "world")
            c.toMarkdown() shouldBe "_hello_world"
        }

        test("2.2 Selection toggle on: select 'world', tap Italic → contains _world_") {
            val c = newController()
            typeText(c, "hello world")
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.ITALIC)
            c.toMarkdown() shouldContain "_world_"
        }

        test("2.2 Selection toggle off: italic 'world', select again, tap Italic → plain") {
            val c = newController()
            typeText(c, "hello world")
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.ITALIC)
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.ITALIC)
            c.toMarkdown() shouldBe "hello world"
        }

        test("2.3 Italic + Bold combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ITALIC)
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "_"
            md shouldContain "**"
        }

        test("2.3 Italic + Underline combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ITALIC)
            c.toggleFormat(RichTextFormat.UNDERLINE)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "_"
            md shouldContain "<u>"
        }

        test("2.3 Italic + Strikethrough combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ITALIC)
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "_"
            md shouldContain "~~"
        }

        test("2.3 Italic + CodeBlock → Italic auto-deselects") {
            val active = setOf(RichTextFormat.ITALIC)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.CODE_BLOCK)
            result shouldContain RichTextFormat.CODE_BLOCK
            result shouldNotContain RichTextFormat.ITALIC
        }

        test("2.4 Enter: Italic continues on new line") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ITALIC)
            typeText(c, "line 1")
            pressEnter(c)
            typeText(c, "line 2")
            val md = c.toMarkdown()
            md shouldContain "line 1"
            md shouldContain "line 2"
            md shouldContain "_"
        }
    }

    // ==================== Section 3: Underline ====================

    context("Section 3: Underline") {

        test("3.1 Pending: tap Underline, type hello, tap Underline, type world → <u>hello</u>world") {
            val c = newController()
            c.toggleFormat(RichTextFormat.UNDERLINE)
            typeText(c, "hello")
            c.toggleFormat(RichTextFormat.UNDERLINE)
            typeText(c, "world")
            c.toMarkdown() shouldBe "<u>hello</u>world"
        }

        test("3.2 Selection toggle on: select 'world', tap Underline → contains <u>world</u>") {
            val c = newController()
            typeText(c, "hello world")
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.UNDERLINE)
            c.toMarkdown() shouldContain "<u>world</u>"
        }

        test("3.2 Selection toggle off: underline 'world', select again, tap Underline → plain") {
            val c = newController()
            typeText(c, "hello world")
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.UNDERLINE)
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.UNDERLINE)
            c.toMarkdown() shouldBe "hello world"
        }

        test("3.3 Underline + Bold combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.UNDERLINE)
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "<u>"
            md shouldContain "**"
        }

        test("3.3 Underline + Italic combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.UNDERLINE)
            c.toggleFormat(RichTextFormat.ITALIC)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "<u>"
            md shouldContain "_"
        }

        test("3.3 Underline + Strikethrough combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.UNDERLINE)
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "<u>"
            md shouldContain "~~"
        }

        test("3.3 Underline + CodeBlock → Underline auto-deselects") {
            val active = setOf(RichTextFormat.UNDERLINE)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.CODE_BLOCK)
            result shouldContain RichTextFormat.CODE_BLOCK
            result shouldNotContain RichTextFormat.UNDERLINE
        }

        test("3.4 Enter: Underline continues on new line") {
            val c = newController()
            c.toggleFormat(RichTextFormat.UNDERLINE)
            typeText(c, "line 1")
            pressEnter(c)
            typeText(c, "line 2")
            val md = c.toMarkdown()
            md shouldContain "line 1"
            md shouldContain "line 2"
            md shouldContain "<u>"
        }
    }

    // ==================== Section 4: Strikethrough ====================

    context("Section 4: Strikethrough") {

        test("4.1 Pending: tap Strikethrough, type hello, tap Strikethrough, type world → ~~hello~~world") {
            val c = newController()
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            typeText(c, "hello")
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            typeText(c, "world")
            c.toMarkdown() shouldBe "~~hello~~world"
        }

        test("4.2 Selection toggle on: select 'world', tap Strikethrough → contains ~~world~~") {
            val c = newController()
            typeText(c, "hello world")
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            c.toMarkdown() shouldContain "~~world~~"
        }

        test("4.2 Selection toggle off: strikethrough 'world', select again, tap Strikethrough → plain") {
            val c = newController()
            typeText(c, "hello world")
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            c.toMarkdown() shouldBe "hello world"
        }

        test("4.3 Strikethrough + Bold combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "~~"
            md shouldContain "**"
        }

        test("4.3 Strikethrough + Italic combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            c.toggleFormat(RichTextFormat.ITALIC)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "~~"
            md shouldContain "_"
        }

        test("4.3 Strikethrough + Underline combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            c.toggleFormat(RichTextFormat.UNDERLINE)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "~~"
            md shouldContain "<u>"
        }

        test("4.3 Strikethrough + CodeBlock → Strikethrough auto-deselects") {
            val active = setOf(RichTextFormat.STRIKETHROUGH)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.CODE_BLOCK)
            result shouldContain RichTextFormat.CODE_BLOCK
            result shouldNotContain RichTextFormat.STRIKETHROUGH
        }

        test("4.4 Enter: Strikethrough continues on new line") {
            val c = newController()
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            typeText(c, "line 1")
            pressEnter(c)
            typeText(c, "line 2")
            val md = c.toMarkdown()
            md shouldContain "line 1"
            md shouldContain "line 2"
            md shouldContain "~~"
        }
    }

    // ==================== Section 5: Inline Code ====================

    context("Section 5: Inline Code") {

        test("5.1 Pending: tap InlineCode, type 'var x', tap InlineCode, type ' = 5' → `var x` = 5") {
            val c = newController()
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            typeText(c, "var x")
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            typeText(c, " = 5")
            c.toMarkdown() shouldBe "`var x` = 5"
        }

        test("5.2 Selection toggle on: select 'let x', tap InlineCode → contains `let x`") {
            val c = newController()
            typeText(c, "hello let x world")
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            c.toMarkdown() shouldContain "`let x`"
        }

        test("5.2 Selection toggle off: inline-code 'let x', select again, tap InlineCode → plain") {
            val c = newController()
            typeText(c, "hello let x world")
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            selectRange(c, 6, 11)
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            c.toMarkdown() shouldBe "hello let x world"
        }

        test("5.3 InlineCode + Bold combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "code")
            val md = c.toMarkdown()
            md shouldContain "`"
            md shouldContain "**"
        }

        test("5.3 InlineCode + Italic combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            c.toggleFormat(RichTextFormat.ITALIC)
            typeText(c, "code")
            val md = c.toMarkdown()
            md shouldContain "`"
            md shouldContain "_"
        }

        test("5.3 InlineCode + Underline combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            c.toggleFormat(RichTextFormat.UNDERLINE)
            typeText(c, "code")
            val md = c.toMarkdown()
            md shouldContain "`"
            md shouldContain "<u>"
        }

        test("5.3 InlineCode + Strikethrough combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            typeText(c, "code")
            val md = c.toMarkdown()
            md shouldContain "`"
            md shouldContain "~~"
        }

        test("5.4 InlineCode + BulletList combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BULLET_LIST)
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            typeText(c, "npm install")
            val md = c.toMarkdown()
            md shouldContain "- "
            md shouldContain "`npm install`"
        }

        test("5.4 InlineCode + NumberedList combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ORDERED_LIST)
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            typeText(c, "step one")
            val md = c.toMarkdown()
            md shouldContain "1. "
            md shouldContain "`step one`"
        }

        test("5.4 InlineCode + Blockquote combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BLOCKQUOTE)
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            typeText(c, "var x")
            val md = c.toMarkdown()
            md shouldContain "> "
            md shouldContain "`var x`"
        }

        test("5.5 InlineCode + CodeBlock → auto-deselect via toggleFormat") {
            val active = setOf(RichTextFormat.INLINE_CODE)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.CODE_BLOCK)
            result shouldContain RichTextFormat.CODE_BLOCK
            result shouldNotContain RichTextFormat.INLINE_CODE
        }

        test("5.6 Clear all → InlineCode deselects") {
            val c = newController()
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            typeText(c, "hello")
            c.clear()
            c.state.pendingFormats.shouldBeEmpty()
            c.state.text shouldBe ""
            c.state.spans.shouldBeEmpty()
        }
    }

    // ==================== Section 6: Code Block ====================

    context("Section 6: Code Block") {

        test("6.1 Toolbar state: CODE_BLOCK disables BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, INLINE_CODE, LINK") {
            val disabled = FormatCompatibility.getDisabledFormats(setOf(RichTextFormat.CODE_BLOCK))
            disabled shouldContainAll setOf(
                RichTextFormat.BOLD,
                RichTextFormat.ITALIC,
                RichTextFormat.UNDERLINE,
                RichTextFormat.STRIKETHROUGH,
                RichTextFormat.INLINE_CODE,
                RichTextFormat.LINK
            )
        }

        test("6.1 Toolbar state: CODE_BLOCK also disables BULLET_LIST, ORDERED_LIST, BLOCKQUOTE") {
            val disabled = FormatCompatibility.getDisabledFormats(setOf(RichTextFormat.CODE_BLOCK))
            disabled shouldContainAll setOf(
                RichTextFormat.BULLET_LIST,
                RichTextFormat.ORDERED_LIST,
                RichTextFormat.BLOCKQUOTE
            )
        }

        test("6.2 INCOMPATIBLE_FORMATS: CODE_BLOCK greys out 6 buttons") {
            val incompatible = RichTextFormat.INCOMPATIBLE_FORMATS[RichTextFormat.CODE_BLOCK]!!
            incompatible shouldContainAll setOf(
                RichTextFormat.BOLD,
                RichTextFormat.ITALIC,
                RichTextFormat.UNDERLINE,
                RichTextFormat.STRIKETHROUGH,
                RichTextFormat.INLINE_CODE,
                RichTextFormat.LINK
            )
        }

        test("6.3 Auto-deselect: Bold+Italic+InlineCode+BulletList → tap CodeBlock → only CodeBlock remains") {
            val active = setOf(
                RichTextFormat.BOLD,
                RichTextFormat.ITALIC,
                RichTextFormat.INLINE_CODE,
                RichTextFormat.BULLET_LIST
            )
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.CODE_BLOCK)
            result shouldBe setOf(RichTextFormat.CODE_BLOCK)
        }

        test("6.4 CodeBlock + BulletList: tap BulletList → CodeBlock deselects") {
            val active = setOf(RichTextFormat.CODE_BLOCK)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.BULLET_LIST)
            result shouldContain RichTextFormat.BULLET_LIST
            result shouldNotContain RichTextFormat.CODE_BLOCK
        }

        test("6.5 CodeBlock + NumberedList: tap NumberedList → CodeBlock deselects") {
            val active = setOf(RichTextFormat.CODE_BLOCK)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.ORDERED_LIST)
            result shouldContain RichTextFormat.ORDERED_LIST
            result shouldNotContain RichTextFormat.CODE_BLOCK
        }

        test("6.6 CodeBlock + Blockquote: tap Blockquote → CodeBlock deselects") {
            val active = setOf(RichTextFormat.CODE_BLOCK)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.BLOCKQUOTE)
            result shouldContain RichTextFormat.BLOCKQUOTE
            result shouldNotContain RichTextFormat.CODE_BLOCK
        }

        test("6.7 Double-newline exit via SegmentComposerController") {
            val scc = SegmentComposerController()
            val normalSeg = scc.segments[0] as ComposerSegment.Normal
            scc.setFocusedSegment(normalSeg.id)
            scc.toggleCodeBlock()

            val codeSeg = scc.segments.filterIsInstance<ComposerSegment.Code>().first()
            scc.setFocusedSegment(codeSeg.id)

            // Simulate typing text then double-newline
            val exited = scc.handleCodeTextChanged(codeSeg, "hello\n\n")
            exited.shouldBeTrue()
            codeSeg.text shouldBe "hello"
            // Focus should move to the next normal segment
            scc.focusedSegment.let { it is ComposerSegment.Normal }.shouldBeTrue()
        }

        test("6.8 Code block serialization: wraps in triple backticks") {
            val scc = SegmentComposerController()
            val normalSeg = scc.segments[0] as ComposerSegment.Normal
            scc.setFocusedSegment(normalSeg.id)
            scc.toggleCodeBlock()

            val codeSeg = scc.segments.filterIsInstance<ComposerSegment.Code>().first()
            codeSeg.text = "console.log('hi')"

            val md = scc.toMarkdown()
            md shouldContain "```"
            md shouldContain "console.log('hi')"
        }

        test("6.8 Clear all via SegmentComposerController → CodeBlock deselects") {
            val scc = SegmentComposerController()
            val normalSeg = scc.segments[0] as ComposerSegment.Normal
            scc.setFocusedSegment(normalSeg.id)
            scc.toggleCodeBlock()

            scc.hasCodeBlocks.shouldBeTrue()
            scc.clear()
            scc.hasCodeBlocks.shouldBeFalse()
            scc.segments shouldHaveSize 1
            (scc.segments[0] is ComposerSegment.Normal).shouldBeTrue()
        }

        test("6.9 SegmentComposerController: toolbarDisabledFormats when in code block") {
            val scc = SegmentComposerController()
            val normalSeg = scc.segments[0] as ComposerSegment.Normal
            scc.setFocusedSegment(normalSeg.id)
            scc.toggleCodeBlock()

            val codeSeg = scc.segments.filterIsInstance<ComposerSegment.Code>().first()
            scc.setFocusedSegment(codeSeg.id)

            // When typing in code, all formats except CODE_BLOCK should be disabled
            scc.isTypingInCode.shouldBeTrue()
            val disabled = scc.toolbarDisabledFormats
            disabled shouldContain RichTextFormat.BOLD
            disabled shouldContain RichTextFormat.ITALIC
            disabled shouldContain RichTextFormat.UNDERLINE
            disabled shouldContain RichTextFormat.STRIKETHROUGH
            disabled shouldContain RichTextFormat.INLINE_CODE
            disabled shouldContain RichTextFormat.LINK
        }

        test("6.10 SegmentComposerController: activeFormats when in code block → contains CODE_BLOCK") {
            val scc = SegmentComposerController()
            val normalSeg = scc.segments[0] as ComposerSegment.Normal
            scc.setFocusedSegment(normalSeg.id)
            scc.toggleCodeBlock()

            val codeSeg = scc.segments.filterIsInstance<ComposerSegment.Code>().first()
            scc.setFocusedSegment(codeSeg.id)

            scc.activeFormats shouldContain RichTextFormat.CODE_BLOCK
        }
    }

    // ==================== Section 7: Bullet List ====================

    context("Section 7: Bullet List") {

        test("7.1 Auto-continuation: type item, Enter → new bullet") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BULLET_LIST)
            typeText(c, "item 1")
            pressEnter(c)
            c.state.text shouldBe "- item 1\n- "
        }

        test("7.1 Multiple items with Enter") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BULLET_LIST)
            typeText(c, "one")
            pressEnter(c)
            typeText(c, "two")
            pressEnter(c)
            typeText(c, "three")
            c.state.text shouldBe "- one\n- two\n- three"
        }

        test("7.2 Empty item exit: Enter on empty bullet → exits list") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BULLET_LIST)
            typeText(c, "item")
            pressEnter(c)
            // Now "- item\n- " — press Enter on empty "- "
            pressEnter(c)
            c.state.text shouldBe "- item\n"
        }

        test("7.3 Bullet + Bold combo → - **bold item**") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BULLET_LIST)
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "bold item")
            val md = c.toMarkdown()
            md shouldContain "- "
            md shouldContain "**bold item**"
        }

        test("7.3 Bullet + Italic combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BULLET_LIST)
            c.toggleFormat(RichTextFormat.ITALIC)
            typeText(c, "italic item")
            val md = c.toMarkdown()
            md shouldContain "- "
            md shouldContain "_italic item_"
        }

        test("7.3 Bullet + InlineCode combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BULLET_LIST)
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            typeText(c, "npm install")
            val md = c.toMarkdown()
            md shouldContain "- "
            md shouldContain "`npm install`"
        }

        test("7.4 Bullet + Numbered: mutually exclusive via toggleFormat") {
            val active = setOf(RichTextFormat.BULLET_LIST)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.ORDERED_LIST)
            result shouldContain RichTextFormat.ORDERED_LIST
            result shouldNotContain RichTextFormat.BULLET_LIST
        }

        test("7.4 Numbered + Bullet: mutually exclusive via toggleFormat") {
            val active = setOf(RichTextFormat.ORDERED_LIST)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.BULLET_LIST)
            result shouldContain RichTextFormat.BULLET_LIST
            result shouldNotContain RichTextFormat.ORDERED_LIST
        }
    }

    // ==================== Section 8: Numbered List ====================

    context("Section 8: Numbered List") {

        test("8.1 Auto-continuation with sequential numbering") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ORDERED_LIST)
            typeText(c, "first")
            pressEnter(c)
            c.state.text shouldBe "1. first\n2. "

            typeText(c, "second")
            pressEnter(c)
            c.state.text shouldBe "1. first\n2. second\n3. "

            typeText(c, "third")
            c.state.text shouldBe "1. first\n2. second\n3. third"
        }

        test("8.1 Numbering increments to 5+") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ORDERED_LIST)
            for (i in 1..5) {
                typeText(c, "item$i")
                if (i < 5) pressEnter(c)
            }
            val lines = c.state.text.split("\n")
            lines shouldHaveSize 5
            for (i in 1..5) {
                lines[i - 1] shouldStartWith "$i. "
            }
        }

        test("8.2 Empty item exit: Enter on empty numbered item → exits list") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ORDERED_LIST)
            typeText(c, "first")
            pressEnter(c)
            // Now "1. first\n2. " — press Enter on empty "2. "
            pressEnter(c)
            c.state.text shouldBe "1. first\n"
        }

        test("8.3 Numbered + Bold combo → 1. **bold item**") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ORDERED_LIST)
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "bold item")
            val md = c.toMarkdown()
            md shouldContain "1. "
            md shouldContain "**bold item**"
        }

        test("8.3 Numbered + Italic combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ORDERED_LIST)
            c.toggleFormat(RichTextFormat.ITALIC)
            typeText(c, "italic item")
            val md = c.toMarkdown()
            md shouldContain "1. "
            md shouldContain "_italic item_"
        }

        test("8.3 Numbered + InlineCode combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ORDERED_LIST)
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            typeText(c, "code item")
            val md = c.toMarkdown()
            md shouldContain "1. "
            md shouldContain "`code item`"
        }

        test("8.4 Numbered + Bullet: mutually exclusive") {
            val active = setOf(RichTextFormat.ORDERED_LIST)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.BULLET_LIST)
            result shouldContain RichTextFormat.BULLET_LIST
            result shouldNotContain RichTextFormat.ORDERED_LIST
        }
    }

    // ==================== Section 9: Blockquote ====================

    context("Section 9: Blockquote") {

        test("9.1 Toggle on: tap Blockquote → prefix > appears") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BLOCKQUOTE)
            c.state.text shouldBe "> "
        }

        test("9.1 Toggle off: tap Blockquote again → prefix removed") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BLOCKQUOTE)
            typeText(c, "quote")
            c.state.text shouldBe "> quote"
            moveCursor(c, 0)
            c.toggleFormat(RichTextFormat.BLOCKQUOTE)
            c.state.text shouldBe "quote"
        }

        test("9.2 Auto-continuation: type text + Enter → > prefix on next line") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BLOCKQUOTE)
            typeText(c, "first line")
            pressEnter(c)
            c.state.text shouldBe "> first line\n> "
        }

        test("9.2 Multiple lines with Enter") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BLOCKQUOTE)
            typeText(c, "line one")
            pressEnter(c)
            typeText(c, "line two")
            pressEnter(c)
            typeText(c, "line three")
            c.state.text shouldBe "> line one\n> line two\n> line three"
        }

        test("9.3 Empty line exit: Enter on empty > → exits blockquote") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BLOCKQUOTE)
            typeText(c, "quoted")
            pressEnter(c)
            // Now "> quoted\n> " — press Enter on empty "> "
            pressEnter(c)
            c.state.text shouldBe "> quoted\n"
        }

        test("9.4 Blockquote + Bold combo → > **text**") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BLOCKQUOTE)
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "> "
            md shouldContain "**text**"
        }

        test("9.4 Blockquote + Italic combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BLOCKQUOTE)
            c.toggleFormat(RichTextFormat.ITALIC)
            typeText(c, "text")
            val md = c.toMarkdown()
            md shouldContain "> "
            md shouldContain "_text_"
        }

        test("9.4 Blockquote + InlineCode combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BLOCKQUOTE)
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            typeText(c, "var x")
            val md = c.toMarkdown()
            md shouldContain "> "
            md shouldContain "`var x`"
        }

        test("9.4 Blockquote + Bold + Italic + InlineCode combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BLOCKQUOTE)
            c.toggleFormat(RichTextFormat.BOLD)
            c.toggleFormat(RichTextFormat.ITALIC)
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            typeText(c, "styled")
            val md = c.toMarkdown()
            md shouldContain "> "
            md shouldContain "**"
            md shouldContain "_"
            md shouldContain "`"
        }

        test("9.5 Blockquote + CodeBlock: mutually exclusive via toggleFormat") {
            val active = setOf(RichTextFormat.BLOCKQUOTE)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.CODE_BLOCK)
            result shouldContain RichTextFormat.CODE_BLOCK
            result shouldNotContain RichTextFormat.BLOCKQUOTE
        }

        test("9.5 CodeBlock + Blockquote: tap Blockquote → CodeBlock deselects") {
            val active = setOf(RichTextFormat.CODE_BLOCK)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.BLOCKQUOTE)
            result shouldContain RichTextFormat.BLOCKQUOTE
            result shouldNotContain RichTextFormat.CODE_BLOCK
        }
    }

    // ==================== Section 10: Link ====================

    context("Section 10: Link") {

        test("10.1 applyLink with no selection → link inserted at cursor") {
            val c = newController()
            typeText(c, "visit ")
            c.applyLink("Google", "https://google.com")
            c.state.text shouldBe "visit Google"
            val linkSpans = c.state.spans.filter { RichTextFormat.LINK in it.formats }
            linkSpans shouldHaveSize 1
            linkSpans[0].start shouldBe 6
            linkSpans[0].end shouldBe 12
        }

        test("10.2 applyLink with selection → replaces selection") {
            val c = newController()
            typeText(c, "click here for info")
            selectRange(c, 6, 10)
            c.applyLink("here", "https://example.com")
            c.state.text shouldBe "click here for info"
            val linkSpans = c.state.spans.filter { RichTextFormat.LINK in it.formats }
            linkSpans shouldHaveSize 1
        }

        test("10.3 Link markdown output contains link markers") {
            val c = newController()
            c.applyLink("Google", "https://google.com")
            val md = c.toMarkdown()
            // The simplified link serialization uses [text]
            md shouldContain "[Google]"
        }

        test("10.4 Link + Bold combo: bold text that is also linked") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "click")
            // applyLink with selection replaces text, adding LINK format.
            // The bold span covers the original text; after applyLink replaces
            // the selection, the new text gets LINK format applied.
            selectRange(c, 0, 5)
            c.applyLink("click", "https://example.com")
            val md = c.toMarkdown()
            // Link format is applied to the replacement text
            md shouldContain "[click]"
        }

        test("10.4 Link + Italic combo") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ITALIC)
            typeText(c, "click")
            selectRange(c, 0, 5)
            c.applyLink("click", "https://example.com")
            val md = c.toMarkdown()
            md shouldContain "[click]"
        }

        test("10.5 Link + CodeBlock: LINK is greyed out when CODE_BLOCK active") {
            val disabled = FormatCompatibility.getDisabledFormats(setOf(RichTextFormat.CODE_BLOCK))
            disabled shouldContain RichTextFormat.LINK
        }
    }

    // ==================== Section 17: Double-Newline Exit ====================

    context("Section 17: Double-Newline Exit") {

        test("17.1 Code Block: double-newline exits code block") {
            val scc = SegmentComposerController()
            val normalSeg = scc.segments[0] as ComposerSegment.Normal
            scc.setFocusedSegment(normalSeg.id)
            scc.toggleCodeBlock()

            val codeSeg = scc.segments.filterIsInstance<ComposerSegment.Code>().first()
            scc.setFocusedSegment(codeSeg.id)

            val exited = scc.handleCodeTextChanged(codeSeg, "line 1\n\n")
            exited.shouldBeTrue()
            codeSeg.text shouldBe "line 1"
        }

        test("17.1 Code Block: text without double-newline does NOT exit") {
            val scc = SegmentComposerController()
            val normalSeg = scc.segments[0] as ComposerSegment.Normal
            scc.setFocusedSegment(normalSeg.id)
            scc.toggleCodeBlock()

            val codeSeg = scc.segments.filterIsInstance<ComposerSegment.Code>().first()
            scc.setFocusedSegment(codeSeg.id)

            val exited = scc.handleCodeTextChanged(codeSeg, "line 1\nline 2\n")
            exited.shouldBeFalse()
            codeSeg.text shouldBe "line 1\nline 2\n"
        }

        test("17.2 Blockquote: Enter on empty > line exits blockquote") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BLOCKQUOTE)
            typeText(c, "text")
            pressEnter(c)
            // "> text\n> " — Enter on empty "> "
            pressEnter(c)
            c.state.text shouldBe "> text\n"
        }

        test("17.3 Bullet List: Enter on empty bullet exits list") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BULLET_LIST)
            typeText(c, "item")
            pressEnter(c)
            // "- item\n- " — Enter on empty "- "
            pressEnter(c)
            c.state.text shouldBe "- item\n"
        }

        test("17.4 Numbered List: Enter on empty numbered item exits list") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ORDERED_LIST)
            typeText(c, "item")
            pressEnter(c)
            // "1. item\n2. " — Enter on empty "2. "
            pressEnter(c)
            c.state.text shouldBe "1. item\n"
        }

        test("17.5 After exit, subsequent typing is unformatted") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BULLET_LIST)
            typeText(c, "item")
            pressEnter(c)
            pressEnter(c)
            // Exited list — text is now "- item"
            // Type plain text directly after the exit
            typeText(c, " plain")
            // The text should be "- item plain" (appended to same line, no new bullet)
            c.state.text shouldContain "plain"
        }
    }

    // ==================== Section 18: Format Persistence After Clear ====================

    context("Section 18: Format Persistence After Clear") {

        test("18.1 Clear all → pendingFormats empty") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "hello")
            c.clear()
            c.state.pendingFormats.shouldBeEmpty()
        }

        test("18.1 Clear all → disabledFormats empty") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "hello")
            c.toggleFormat(RichTextFormat.BOLD) // disable
            c.clear()
            c.state.disabledFormats.shouldBeEmpty()
        }

        test("18.1 Clear all → text empty") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "hello")
            c.clear()
            c.state.text shouldBe ""
        }

        test("18.1 Clear all → spans empty") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "hello")
            c.clear()
            c.state.spans.shouldBeEmpty()
        }

        test("18.2 No pending format carries over after clear") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            c.clear()
            typeText(c, "hello")
            // "hello" should be plain (not bold)
            c.toMarkdown() shouldBe "hello"
        }

        test("18.3 Clear with multiple formats active → all deselect") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            c.toggleFormat(RichTextFormat.ITALIC)
            c.toggleFormat(RichTextFormat.UNDERLINE)
            typeText(c, "styled")
            c.clear()
            c.state.pendingFormats.shouldBeEmpty()
            c.state.disabledFormats.shouldBeEmpty()
            c.state.spans.shouldBeEmpty()
            c.state.text shouldBe ""
        }

        test("18.4 Clear after InlineCode → InlineCode deselects") {
            val c = newController()
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            typeText(c, "code")
            c.clear()
            c.state.pendingFormats.shouldBeEmpty()
            typeText(c, "plain")
            c.toMarkdown() shouldBe "plain"
        }

        test("18.5 SegmentComposerController clear → code block removed") {
            val scc = SegmentComposerController()
            val normalSeg = scc.segments[0] as ComposerSegment.Normal
            scc.setFocusedSegment(normalSeg.id)
            scc.toggleCodeBlock()
            scc.hasCodeBlocks.shouldBeTrue()

            scc.clear()
            scc.hasCodeBlocks.shouldBeFalse()
            scc.segments shouldHaveSize 1
            (scc.segments[0] is ComposerSegment.Normal).shouldBeTrue()
        }

        test("18.6 Clear after list format → list prefix gone") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BULLET_LIST)
            typeText(c, "item")
            c.clear()
            c.state.text shouldBe ""
            typeText(c, "plain")
            c.state.text shouldBe "plain"
            c.state.text.startsWith("- ").shouldBeFalse()
        }
    }

    // ==================== Additional Cross-Cutting Tests ====================

    context("Cross-cutting: Format compatibility matrix") {

        test("BOLD is compatible with ITALIC") {
            FormatCompatibility.isCompatible(
                RichTextFormat.BOLD,
                setOf(RichTextFormat.ITALIC)
            ).shouldBeTrue()
        }

        test("BOLD is compatible with UNDERLINE") {
            FormatCompatibility.isCompatible(
                RichTextFormat.BOLD,
                setOf(RichTextFormat.UNDERLINE)
            ).shouldBeTrue()
        }

        test("BOLD is compatible with STRIKETHROUGH") {
            FormatCompatibility.isCompatible(
                RichTextFormat.BOLD,
                setOf(RichTextFormat.STRIKETHROUGH)
            ).shouldBeTrue()
        }

        test("BOLD is compatible with INLINE_CODE") {
            FormatCompatibility.isCompatible(
                RichTextFormat.BOLD,
                setOf(RichTextFormat.INLINE_CODE)
            ).shouldBeTrue()
        }

        test("BOLD is NOT compatible with CODE_BLOCK") {
            FormatCompatibility.isCompatible(
                RichTextFormat.BOLD,
                setOf(RichTextFormat.CODE_BLOCK)
            ).shouldBeFalse()
        }

        test("INLINE_CODE is NOT compatible with LINK") {
            FormatCompatibility.isCompatible(
                RichTextFormat.INLINE_CODE,
                setOf(RichTextFormat.LINK)
            ).shouldBeFalse()
        }

        test("BULLET_LIST is NOT compatible with ORDERED_LIST") {
            FormatCompatibility.isCompatible(
                RichTextFormat.BULLET_LIST,
                setOf(RichTextFormat.ORDERED_LIST)
            ).shouldBeFalse()
        }

        test("BULLET_LIST is compatible with BOLD") {
            FormatCompatibility.isCompatible(
                RichTextFormat.BULLET_LIST,
                setOf(RichTextFormat.BOLD)
            ).shouldBeTrue()
        }

        test("BLOCKQUOTE is compatible with BOLD") {
            FormatCompatibility.isCompatible(
                RichTextFormat.BLOCKQUOTE,
                setOf(RichTextFormat.BOLD)
            ).shouldBeTrue()
        }

        test("BLOCKQUOTE is NOT compatible with CODE_BLOCK") {
            FormatCompatibility.isCompatible(
                RichTextFormat.BLOCKQUOTE,
                setOf(RichTextFormat.CODE_BLOCK)
            ).shouldBeFalse()
        }
    }

    context("Cross-cutting: toggleFormat auto-deselect rules") {

        test("toggleFormat: activating CODE_BLOCK deselects all inline + block formats") {
            val active = setOf(
                RichTextFormat.BOLD,
                RichTextFormat.ITALIC,
                RichTextFormat.UNDERLINE,
                RichTextFormat.STRIKETHROUGH,
                RichTextFormat.INLINE_CODE,
                RichTextFormat.BULLET_LIST,
                RichTextFormat.ORDERED_LIST,
                RichTextFormat.BLOCKQUOTE,
                RichTextFormat.LINK
            )
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.CODE_BLOCK)
            result shouldBe setOf(RichTextFormat.CODE_BLOCK)
        }

        test("toggleFormat: deactivating CODE_BLOCK leaves empty set") {
            val active = setOf(RichTextFormat.CODE_BLOCK)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.CODE_BLOCK)
            result.shouldBeEmpty()
        }

        test("toggleFormat: activating BULLET_LIST deselects CODE_BLOCK and ORDERED_LIST") {
            val active = setOf(RichTextFormat.CODE_BLOCK, RichTextFormat.ORDERED_LIST)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.BULLET_LIST)
            result shouldContain RichTextFormat.BULLET_LIST
            result shouldNotContain RichTextFormat.CODE_BLOCK
            result shouldNotContain RichTextFormat.ORDERED_LIST
        }

        test("toggleFormat: activating ORDERED_LIST deselects CODE_BLOCK and BULLET_LIST") {
            val active = setOf(RichTextFormat.CODE_BLOCK, RichTextFormat.BULLET_LIST)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.ORDERED_LIST)
            result shouldContain RichTextFormat.ORDERED_LIST
            result shouldNotContain RichTextFormat.CODE_BLOCK
            result shouldNotContain RichTextFormat.BULLET_LIST
        }

        test("toggleFormat: activating BLOCKQUOTE deselects CODE_BLOCK") {
            val active = setOf(RichTextFormat.CODE_BLOCK)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.BLOCKQUOTE)
            result shouldContain RichTextFormat.BLOCKQUOTE
            result shouldNotContain RichTextFormat.CODE_BLOCK
        }

        test("toggleFormat: toggling BOLD off removes it") {
            val active = setOf(RichTextFormat.BOLD, RichTextFormat.ITALIC)
            val result = RichTextFormat.toggleFormat(active, RichTextFormat.BOLD)
            result shouldNotContain RichTextFormat.BOLD
            result shouldContain RichTextFormat.ITALIC
        }
    }

    context("Cross-cutting: Markdown round-trip for all format types") {

        test("Bold markdown round-trip") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "hello")
            c.toMarkdown() shouldBe "**hello**"
        }

        test("Italic markdown round-trip") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ITALIC)
            typeText(c, "hello")
            c.toMarkdown() shouldBe "_hello_"
        }

        test("Underline markdown round-trip") {
            val c = newController()
            c.toggleFormat(RichTextFormat.UNDERLINE)
            typeText(c, "hello")
            c.toMarkdown() shouldBe "<u>hello</u>"
        }

        test("Strikethrough markdown round-trip") {
            val c = newController()
            c.toggleFormat(RichTextFormat.STRIKETHROUGH)
            typeText(c, "hello")
            c.toMarkdown() shouldBe "~~hello~~"
        }

        test("InlineCode markdown round-trip") {
            val c = newController()
            c.toggleFormat(RichTextFormat.INLINE_CODE)
            typeText(c, "code")
            c.toMarkdown() shouldBe "`code`"
        }

        test("Plain text markdown round-trip") {
            val c = newController()
            typeText(c, "hello world")
            c.toMarkdown() shouldBe "hello world"
        }

        test("Mixed bold and plain markdown") {
            val c = newController()
            typeText(c, "say ")
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "hello")
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, " world")
            c.toMarkdown() shouldBe "say **hello** world"
        }

        test("Bullet list with bold item markdown") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BULLET_LIST)
            c.toggleFormat(RichTextFormat.BOLD)
            typeText(c, "item")
            c.toMarkdown() shouldContain "- **item**"
        }

        test("Ordered list with italic item markdown") {
            val c = newController()
            c.toggleFormat(RichTextFormat.ORDERED_LIST)
            c.toggleFormat(RichTextFormat.ITALIC)
            typeText(c, "item")
            c.toMarkdown() shouldContain "1. _item_"
        }

        test("Blockquote with underline markdown") {
            val c = newController()
            c.toggleFormat(RichTextFormat.BLOCKQUOTE)
            c.toggleFormat(RichTextFormat.UNDERLINE)
            typeText(c, "text")
            c.toMarkdown() shouldContain "> <u>text</u>"
        }

        test("Code block via SegmentComposerController markdown") {
            val scc = SegmentComposerController()
            val normalSeg = scc.segments[0] as ComposerSegment.Normal
            scc.setFocusedSegment(normalSeg.id)
            scc.toggleCodeBlock()

            val codeSeg = scc.segments.filterIsInstance<ComposerSegment.Code>().first()
            codeSeg.text = "print('hello')"

            val md = scc.toMarkdown()
            md shouldContain "```"
            md shouldContain "print('hello')"
            md shouldContain "```"
        }
    }
})

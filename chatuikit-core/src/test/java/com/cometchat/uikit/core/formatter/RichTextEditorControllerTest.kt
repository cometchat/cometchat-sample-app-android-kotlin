package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith

/**
 * Tests for [RichTextEditorController] — the orchestrator that bridges UI and span engine.
 * Tests simulate real user interactions: typing, toggling formats, selecting text, sending.
 */
class RichTextEditorControllerTest : FunSpec({

    lateinit var controller: RichTextEditorController
    var stateChangedCount: Int = 0

    beforeEach {
        controller = RichTextEditorController()
        stateChangedCount = 0
        controller.setListener(object : RichTextEditorController.Listener {
            override fun onStateChanged() {
                stateChangedCount++
            }
        })
    }

    // ==================== Helpers ====================

    /** Simulates typing a string character by character. */
    fun typeText(text: String) {
        for (ch in text) {
            val currentText = controller.state.text
            val cursorPos = controller.state.selectionStart
            val newText = currentText.substring(0, cursorPos) + ch +
                currentText.substring(cursorPos)
            controller.onTextChanged(newText, cursorPos + 1, cursorPos + 1)
        }
    }

    /** Simulates pressing Enter (inserts \n at cursor). */
    fun pressEnter() {
        val currentText = controller.state.text
        val cursorPos = controller.state.selectionStart
        val newText = currentText.substring(0, cursorPos) + "\n" +
            currentText.substring(cursorPos)
        controller.onTextChanged(newText, cursorPos + 1, cursorPos + 1)
    }

    /** Simulates moving cursor to a position (selection-only change). */
    fun moveCursor(pos: Int) {
        controller.onTextChanged(controller.state.text, pos, pos)
    }

    /** Simulates selecting a range. */
    fun selectRange(start: Int, end: Int) {
        controller.onTextChanged(controller.state.text, start, end)
    }

    /** Simulates pressing backspace at current cursor position. */
    fun pressBackspace() {
        val text = controller.state.text
        val pos = controller.state.selectionStart
        if (pos > 0) {
            val newText = text.substring(0, pos - 1) + text.substring(pos)
            controller.onTextChanged(newText, pos - 1, pos - 1)
        }
    }

    // ==================== Test 1: Cursor Toggle ====================

    context("cursor toggle") {
        test("tap bold, type hello — text is bold") {
            controller.toggleFormat(RichTextFormat.BOLD)
            controller.state.pendingFormats shouldContain RichTextFormat.BOLD

            typeText("hello")

            controller.state.text shouldBe "hello"
            val spans = controller.state.spans
            spans shouldHaveSize 1
            spans[0].start shouldBe 0
            spans[0].end shouldBe 5
            spans[0].formats shouldContain RichTextFormat.BOLD
        }

        test("tap bold, type hello, tap bold off, type world — only hello is bold") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText(" world")

            controller.state.text shouldBe "hello world"
            val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans shouldHaveSize 1
            boldSpans[0].start shouldBe 0
            boldSpans[0].end shouldBe 5
        }

        test("type plain, tap bold, type bold, tap bold off, type plain") {
            typeText("aaa")
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("BBB")
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("ccc")

            controller.state.text shouldBe "aaaBBBccc"
            val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans shouldHaveSize 1
            boldSpans[0].start shouldBe 3
            boldSpans[0].end shouldBe 6
        }
    }

    // ==================== Test 2: Selection Toggle ====================

    context("selection toggle") {
        test("type text, select range, tap bold") {
            typeText("hello world")
            selectRange(6, 11)
            controller.toggleFormat(RichTextFormat.BOLD)

            val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans shouldHaveSize 1
            boldSpans[0].start shouldBe 6
            boldSpans[0].end shouldBe 11
        }

        test("bold then unbold same range") {
            typeText("hello world")
            selectRange(0, 5)
            controller.toggleFormat(RichTextFormat.BOLD)

            var boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans shouldHaveSize 1

            selectRange(0, 5)
            controller.toggleFormat(RichTextFormat.BOLD)

            boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans.shouldBeEmpty()
        }

        test("select partial bold range removes bold from selection only") {
            typeText("hello world")
            selectRange(0, 11)
            controller.toggleFormat(RichTextFormat.BOLD)

            selectRange(6, 11)
            controller.toggleFormat(RichTextFormat.BOLD)

            val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans shouldHaveSize 1
            boldSpans[0].start shouldBe 0
            boldSpans[0].end shouldBe 6
        }
    }

    // ==================== Test 3: Multiple Formats ====================

    context("multiple formats") {
        test("bold and italic on same range") {
            typeText("hello")
            selectRange(0, 5)
            controller.toggleFormat(RichTextFormat.BOLD)
            selectRange(0, 5)
            controller.toggleFormat(RichTextFormat.ITALIC)

            val spans = controller.state.spans
            spans shouldHaveSize 1
            spans[0].formats shouldContain RichTextFormat.BOLD
            spans[0].formats shouldContain RichTextFormat.ITALIC
        }

        test("overlapping bold and italic") {
            typeText("hello world")
            selectRange(0, 5)
            controller.toggleFormat(RichTextFormat.BOLD)
            selectRange(3, 9)
            controller.toggleFormat(RichTextFormat.ITALIC)

            controller.state.spanManager.getFormatsAt(1) shouldBe setOf(RichTextFormat.BOLD)
            val formatsAt4 = controller.state.spanManager.getFormatsAt(4)
            formatsAt4 shouldContain RichTextFormat.BOLD
            formatsAt4 shouldContain RichTextFormat.ITALIC
            controller.state.spanManager.getFormatsAt(6) shouldBe setOf(RichTextFormat.ITALIC)
            controller.state.spanManager.getFormatsAt(10).shouldBeEmpty()
        }

        test("bold italic underline strikethrough all at once") {
            controller.toggleFormat(RichTextFormat.BOLD)
            controller.toggleFormat(RichTextFormat.ITALIC)
            controller.toggleFormat(RichTextFormat.UNDERLINE)
            controller.toggleFormat(RichTextFormat.STRIKETHROUGH)
            typeText("styled")

            val spans = controller.state.spans
            spans shouldHaveSize 1
            spans[0].formats shouldContain RichTextFormat.BOLD
            spans[0].formats shouldContain RichTextFormat.ITALIC
            spans[0].formats shouldContain RichTextFormat.UNDERLINE
            spans[0].formats shouldContain RichTextFormat.STRIKETHROUGH
        }
    }

    // ==================== Test 4: Typing Continues Format ====================

    context("typing continues format") {
        test("typing at end of bold span continues bold") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            typeText(" world")

            controller.state.text shouldBe "hello world"
            val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans shouldHaveSize 1
            boldSpans[0].start shouldBe 0
            boldSpans[0].end shouldBe 11
        }

        test("typing in middle of bold span inherits bold") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("helloworld")
            moveCursor(5)
            controller.onTextChanged("hello world", 6, 6)

            val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans shouldHaveSize 1
            boldSpans[0].start shouldBe 0
            boldSpans[0].end shouldBe 11
        }
    }

    // ==================== Test 5: Disabled Formats ====================

    context("disabled formats") {
        test("disabled format prevents inheritance") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            controller.toggleFormat(RichTextFormat.BOLD) // disable
            controller.state.disabledFormats shouldContain RichTextFormat.BOLD

            typeText("X")
            controller.state.text shouldBe "helloX"
            (RichTextFormat.BOLD in controller.state.spanManager.getFormatsAt(4)).shouldBeTrue()
            (RichTextFormat.BOLD in controller.state.spanManager.getFormatsAt(5)).shouldBeFalse()
        }
    }

    // ==================== Test 6: Line-Based Formats ====================

    context("line-based formats") {
        test("toggle bullet list adds prefix") {
            typeText("item one")
            moveCursor(0)
            controller.toggleFormat(RichTextFormat.BULLET_LIST)
            controller.state.text shouldStartWith "- "
        }

        test("toggle bullet list removes prefix") {
            typeText("- item one")
            moveCursor(0)
            controller.toggleFormat(RichTextFormat.BULLET_LIST)
            controller.state.text shouldBe "item one"
        }

        test("toggle ordered list adds prefix") {
            typeText("item one")
            moveCursor(0)
            controller.toggleFormat(RichTextFormat.ORDERED_LIST)
            controller.state.text shouldStartWith "1. "
        }

        test("toggle blockquote adds prefix") {
            typeText("some quote")
            moveCursor(0)
            controller.toggleFormat(RichTextFormat.BLOCKQUOTE)
            controller.state.text shouldStartWith "> "
        }

        test("toggle blockquote removes prefix") {
            typeText("> some quote")
            moveCursor(0)
            controller.toggleFormat(RichTextFormat.BLOCKQUOTE)
            controller.state.text shouldBe "some quote"
        }
    }

    // ==================== Test 7: Markdown Serialization ====================

    context("toMarkdown") {
        test("serializes bold text") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            controller.toMarkdown() shouldBe "**hello**"
        }

        test("serializes mixed formatted and plain text") {
            typeText("say ")
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText(" world")
            controller.toMarkdown() shouldBe "say **hello** world"
        }

        test("serializes italic") {
            controller.toggleFormat(RichTextFormat.ITALIC)
            typeText("hello")
            controller.toMarkdown() shouldBe "_hello_"
        }

        test("serializes underline") {
            controller.toggleFormat(RichTextFormat.UNDERLINE)
            typeText("hello")
            controller.toMarkdown() shouldBe "<u>hello</u>"
        }

        test("serializes strikethrough") {
            controller.toggleFormat(RichTextFormat.STRIKETHROUGH)
            typeText("hello")
            controller.toMarkdown() shouldBe "~~hello~~"
        }

        test("serializes inline code") {
            controller.toggleFormat(RichTextFormat.INLINE_CODE)
            typeText("code")
            controller.toMarkdown() shouldBe "`code`"
        }

        test("with no formatting returns plain text") {
            typeText("hello world")
            controller.toMarkdown() shouldBe "hello world"
        }
    }

    // ==================== Test 8: fromMarkdown ====================

    context("fromMarkdown") {
        test("loads bold text") {
            controller.fromMarkdown("**hello**")
            controller.state.text shouldBe "hello"
            controller.state.spans.any { RichTextFormat.BOLD in it.formats }.shouldBeTrue()
        }

        test("loads mixed text") {
            controller.fromMarkdown("say **hello** world")
            controller.state.text shouldBe "say hello world"
            val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans shouldHaveSize 1
            boldSpans[0].start shouldBe 4
            boldSpans[0].end shouldBe 9
        }
    }

    // ==================== Test 9: Clear ====================

    test("clear resets everything") {
        controller.toggleFormat(RichTextFormat.BOLD)
        typeText("hello")
        controller.clear()

        controller.state.text shouldBe ""
        controller.state.spans.shouldBeEmpty()
        controller.state.pendingFormats.shouldBeEmpty()
        controller.state.disabledFormats.shouldBeEmpty()
        controller.state.selectionStart shouldBe 0
        controller.state.selectionEnd shouldBe 0
    }

    // ==================== Test 10: Active Formats ====================

    context("activeFormats") {
        test("includes pending formats") {
            controller.toggleFormat(RichTextFormat.BOLD)
            controller.state.activeFormats shouldContain RichTextFormat.BOLD
        }

        test("includes span formats at cursor") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            controller.state.activeFormats shouldContain RichTextFormat.BOLD
        }

        test("excludes disabled formats") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            controller.toggleFormat(RichTextFormat.BOLD) // disable
            controller.state.activeFormats shouldNotContain RichTextFormat.BOLD
        }
    }

    // ==================== Test 11: Selection Clears Pending/Disabled ====================

    context("selection clears pending and disabled") {
        test("moving cursor clears pending formats") {
            typeText("hi")
            controller.toggleFormat(RichTextFormat.ITALIC)
            controller.state.pendingFormats shouldContain RichTextFormat.ITALIC
            moveCursor(0)
            controller.state.pendingFormats.shouldBeEmpty()
        }

        test("moving cursor clears disabled formats") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            controller.toggleFormat(RichTextFormat.BOLD) // disable
            controller.state.disabledFormats shouldContain RichTextFormat.BOLD
            moveCursor(0)
            controller.state.disabledFormats.shouldBeEmpty()
        }
    }

    // ==================== Test 12: Backspace ====================

    context("backspace") {
        test("in bold span shrinks span") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            pressBackspace()

            controller.state.text shouldBe "hell"
            controller.state.spans shouldHaveSize 1
            controller.state.spans[0].start shouldBe 0
            controller.state.spans[0].end shouldBe 4
        }

        test("removes entire single-char span") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("X")
            pressBackspace()

            controller.state.text shouldBe ""
            controller.state.spans.shouldBeEmpty()
        }
    }

    // ==================== Test 13: Compatibility ====================

    test("toggling incompatible format is rejected") {
        controller.toggleFormat(RichTextFormat.CODE_BLOCK)
        typeText("code")
        selectRange(0, 4)
        controller.toggleFormat(RichTextFormat.BOLD)

        val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
        boldSpans.shouldBeEmpty()
    }

    // ==================== Test 14: Listener ====================

    context("listener") {
        test("notified on text change") {
            val countBefore = stateChangedCount
            typeText("h")
            (stateChangedCount > countBefore).shouldBeTrue()
        }

        test("notified on format toggle") {
            val countBefore = stateChangedCount
            controller.toggleFormat(RichTextFormat.BOLD)
            (stateChangedCount > countBefore).shouldBeTrue()
        }
    }

    // ==================== Test 15: Toolbar Disabled Formats ====================

    test("toolbarDisabledFormats reflects compatibility") {
        controller.toggleFormat(RichTextFormat.BOLD)
        typeText("hello")
        controller.state.toolbarDisabledFormats shouldContain RichTextFormat.CODE_BLOCK
        controller.state.toolbarDisabledFormats shouldNotContain RichTextFormat.ITALIC
    }

    // ==================== Test 16: Bold + Enter — No Text Duplication ====================

    context("bold + Enter — no text duplication") {
        test("bold hello + Enter — new line is empty, no duplication of hello") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            pressEnter()

            // Text must be exactly "hello\n", NOT "hello\nhello"
            controller.state.text shouldBe "hello\n"
            controller.state.text.length shouldBe 6
            controller.state.text.count { it == 'h' } shouldBe 1
        }

        test("bold hello + Enter + type world — world on new line, no duplication") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            pressEnter()
            typeText("world")

            controller.state.text shouldBe "hello\nworld"
            controller.state.text.length shouldBe 11
        }

        test("bold hello + Enter twice — only two newlines, no extra text") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            pressEnter()
            pressEnter()

            controller.state.text shouldBe "hello\n\n"
            controller.state.text.length shouldBe 7
        }

        test("bold formatting persists on new line after Enter") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            pressEnter()
            typeText("world")

            val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans shouldHaveSize 1
            boldSpans[0].start shouldBe 0
            boldSpans[0].end shouldBe 11
        }

        test("italic + Enter — no duplication") {
            controller.toggleFormat(RichTextFormat.ITALIC)
            typeText("test")
            pressEnter()

            controller.state.text shouldBe "test\n"
            controller.state.text.length shouldBe 5
        }

        test("bold + italic + Enter — no duplication") {
            controller.toggleFormat(RichTextFormat.BOLD)
            controller.toggleFormat(RichTextFormat.ITALIC)
            typeText("styled")
            pressEnter()

            controller.state.text shouldBe "styled\n"
            controller.state.text.length shouldBe 7
        }

        test("underline + Enter — no duplication") {
            controller.toggleFormat(RichTextFormat.UNDERLINE)
            typeText("underlined")
            pressEnter()

            controller.state.text shouldBe "underlined\n"
            controller.state.text.count { it == 'u' } shouldBe 1
        }

        test("strikethrough + Enter — no duplication") {
            controller.toggleFormat(RichTextFormat.STRIKETHROUGH)
            typeText("struck")
            pressEnter()

            controller.state.text shouldBe "struck\n"
            controller.state.text.count { it == 's' } shouldBe 1
        }

        test("bold hello + Enter + type world + Enter + type foo — three lines, no duplication") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            pressEnter()
            typeText("world")
            pressEnter()
            typeText("foo")

            controller.state.text shouldBe "hello\nworld\nfoo"
            controller.state.text.length shouldBe 15
        }
    }

    // ==================== Test 17: Ordered List Auto-Continuation ====================

    context("ordered list auto-continuation") {
        test("toggle ordered list on empty — prefix 1. appears") {
            controller.toggleFormat(RichTextFormat.ORDERED_LIST)
            controller.state.text shouldBe "1. "
            controller.state.selectionStart shouldBe 3
        }

        test("ordered list: type first item + Enter — auto-adds 2. prefix") {
            controller.toggleFormat(RichTextFormat.ORDERED_LIST)
            typeText("first")
            controller.state.text shouldBe "1. first"

            pressEnter()

            controller.state.text shouldBe "1. first\n2. "
            controller.state.selectionStart shouldBe 12
        }

        test("ordered list increments: 1 → 2 → 3 → 4 → 5") {
            controller.toggleFormat(RichTextFormat.ORDERED_LIST)
            typeText("one")
            pressEnter()
            controller.state.text shouldBe "1. one\n2. "

            typeText("two")
            pressEnter()
            controller.state.text shouldBe "1. one\n2. two\n3. "

            typeText("three")
            pressEnter()
            controller.state.text shouldBe "1. one\n2. two\n3. three\n4. "

            typeText("four")
            pressEnter()
            controller.state.text shouldBe "1. one\n2. two\n3. three\n4. four\n5. "

            typeText("five")
            controller.state.text shouldBe "1. one\n2. two\n3. three\n4. four\n5. five"
        }

        test("ordered list: Enter on empty prefix exits list mode") {
            controller.toggleFormat(RichTextFormat.ORDERED_LIST)
            typeText("first")
            pressEnter()
            // Now we have "1. first\n2. " — press Enter again on empty "2. "
            pressEnter()

            // Should exit list mode: remove the empty "2. " and the newline
            controller.state.text shouldBe "1. first"
        }

        test("ordered list prefix persists after typing") {
            controller.toggleFormat(RichTextFormat.ORDERED_LIST)
            typeText("hello")

            controller.state.text shouldBe "1. hello"
            controller.state.text shouldStartWith "1. "
        }

        test("toggling ordered list off removes prefix") {
            controller.toggleFormat(RichTextFormat.ORDERED_LIST)
            typeText("item")
            controller.state.text shouldBe "1. item"

            moveCursor(0)
            controller.toggleFormat(RichTextFormat.ORDERED_LIST)
            controller.state.text shouldBe "item"
        }

        test("ordered list: type, Enter, type, Enter, type — all numbers correct") {
            controller.toggleFormat(RichTextFormat.ORDERED_LIST)
            typeText("a")
            pressEnter()
            typeText("b")
            pressEnter()
            typeText("c")

            controller.state.text shouldBe "1. a\n2. b\n3. c"
        }

        test("ordered list: 10+ items still increment correctly") {
            controller.toggleFormat(RichTextFormat.ORDERED_LIST)
            for (i in 1..10) {
                typeText("item$i")
                if (i < 10) pressEnter()
            }

            val lines = controller.state.text.split("\n")
            lines.size shouldBe 10
            for (i in 1..10) {
                lines[i - 1] shouldStartWith "$i. "
            }
        }
    }

    // ==================== Test 18: Bullet List Auto-Continuation ====================

    context("bullet list auto-continuation") {
        test("toggle bullet list on empty — prefix - appears") {
            controller.toggleFormat(RichTextFormat.BULLET_LIST)
            controller.state.text shouldBe "- "
            controller.state.selectionStart shouldBe 2
        }

        test("bullet list: type item + Enter — auto-adds - prefix") {
            controller.toggleFormat(RichTextFormat.BULLET_LIST)
            typeText("first")
            pressEnter()

            controller.state.text shouldBe "- first\n- "
            controller.state.selectionStart shouldBe 10
        }

        test("bullet list: multiple items with Enter") {
            controller.toggleFormat(RichTextFormat.BULLET_LIST)
            typeText("one")
            pressEnter()
            typeText("two")
            pressEnter()
            typeText("three")

            controller.state.text shouldBe "- one\n- two\n- three"
        }

        test("bullet list: Enter on empty prefix exits list mode") {
            controller.toggleFormat(RichTextFormat.BULLET_LIST)
            typeText("first")
            pressEnter()
            // Now "- first\n- " — press Enter on empty "- "
            pressEnter()

            controller.state.text shouldBe "- first"
        }

        test("bullet list prefix persists after typing") {
            controller.toggleFormat(RichTextFormat.BULLET_LIST)
            typeText("item")

            controller.state.text shouldBe "- item"
            controller.state.text shouldStartWith "- "
        }
    }

    // ==================== Test 19: Blockquote Auto-Continuation ====================

    context("blockquote auto-continuation") {
        test("toggle blockquote on empty — prefix > appears") {
            controller.toggleFormat(RichTextFormat.BLOCKQUOTE)
            controller.state.text shouldBe "> "
            controller.state.selectionStart shouldBe 2
        }

        test("blockquote: type text + Enter — auto-adds > prefix") {
            controller.toggleFormat(RichTextFormat.BLOCKQUOTE)
            typeText("first line")
            pressEnter()

            controller.state.text shouldBe "> first line\n> "
            controller.state.selectionStart shouldBe 15
        }

        test("blockquote: multiple lines with Enter") {
            controller.toggleFormat(RichTextFormat.BLOCKQUOTE)
            typeText("line one")
            pressEnter()
            typeText("line two")
            pressEnter()
            typeText("line three")

            controller.state.text shouldBe "> line one\n> line two\n> line three"
        }

        test("blockquote: Enter on empty prefix exits quote mode") {
            controller.toggleFormat(RichTextFormat.BLOCKQUOTE)
            typeText("quoted")
            pressEnter()
            // Now "> quoted\n> " — press Enter on empty "> "
            pressEnter()

            controller.state.text shouldBe "> quoted"
        }

        test("blockquote prefix persists after typing") {
            controller.toggleFormat(RichTextFormat.BLOCKQUOTE)
            typeText("quote")

            controller.state.text shouldBe "> quote"
            controller.state.text shouldStartWith "> "
        }
    }

    // ==================== Test 20: Formatting + Enter Edge Cases ====================

    context("formatting + Enter edge cases") {
        test("bold text + Enter does NOT duplicate text on new line") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("abc")
            pressEnter()

            // The new line must be empty — "abc" must NOT appear again
            val lines = controller.state.text.split("\n")
            lines.size shouldBe 2
            lines[0] shouldBe "abc"
            lines[1] shouldBe ""
        }

        test("italic text + Enter does NOT duplicate text on new line") {
            controller.toggleFormat(RichTextFormat.ITALIC)
            typeText("xyz")
            pressEnter()

            val lines = controller.state.text.split("\n")
            lines.size shouldBe 2
            lines[0] shouldBe "xyz"
            lines[1] shouldBe ""
        }

        test("bold + type + Enter + type + Enter + type — each line has only its own text") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("line1")
            pressEnter()
            typeText("line2")
            pressEnter()
            typeText("line3")

            val lines = controller.state.text.split("\n")
            lines.size shouldBe 3
            lines[0] shouldBe "line1"
            lines[1] shouldBe "line2"
            lines[2] shouldBe "line3"
        }

        test("formatting spans cover all lines after multiple Enters") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("a")
            pressEnter()
            typeText("b")
            pressEnter()
            typeText("c")

            // All text should be bold: "a\nb\nc"
            val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans shouldHaveSize 1
            boldSpans[0].start shouldBe 0
            boldSpans[0].end shouldBe 5 // "a\nb\nc" = 5 chars
        }

        test("plain text + Enter — no auto-continuation prefix added") {
            typeText("hello")
            pressEnter()

            controller.state.text shouldBe "hello\n"
            // No "- " or "1. " or "> " should be added
            controller.state.text.endsWith("\n").shouldBeTrue()
        }

        test("bold on first line, plain on second — Enter doesn't carry formatting if disabled") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("bold")
            controller.toggleFormat(RichTextFormat.BOLD) // disable bold
            pressEnter()
            typeText("plain")

            controller.state.text shouldBe "bold\nplain"
            // "bold" should be bold, "plain" should not
            (RichTextFormat.BOLD in controller.state.spanManager.getFormatsAt(2)).shouldBeTrue()
            (RichTextFormat.BOLD in controller.state.spanManager.getFormatsAt(6)).shouldBeFalse()
        }
    }

    // ==================== Test 21: Ordered List Incrementing Edge Cases ====================

    context("ordered list incrementing edge cases") {
        test("ordered list number keeps incrementing — not stuck at 2") {
            controller.toggleFormat(RichTextFormat.ORDERED_LIST)
            typeText("a")
            pressEnter()
            controller.state.text shouldBe "1. a\n2. "

            typeText("b")
            pressEnter()
            controller.state.text shouldBe "1. a\n2. b\n3. "

            typeText("c")
            pressEnter()
            controller.state.text shouldBe "1. a\n2. b\n3. c\n4. "

            typeText("d")
            pressEnter()
            controller.state.text shouldBe "1. a\n2. b\n3. c\n4. d\n5. "
        }

        test("ordered list: exit at item 3, then plain text after") {
            controller.toggleFormat(RichTextFormat.ORDERED_LIST)
            typeText("a")
            pressEnter()
            typeText("b")
            pressEnter()
            // "1. a\n2. b\n3. " — exit by pressing Enter on empty "3. "
            pressEnter()
            controller.state.text shouldBe "1. a\n2. b"

            // After exiting, if user presses Enter again, the previous line "2. b"
            // still matches ordered list pattern, so auto-continuation kicks in.
            // This is standard editor behavior (Google Docs, Notion, etc.)
            pressEnter()
            controller.state.text shouldBe "1. a\n2. b\n3. "

            // To type truly plain text, user would need to backspace the "3. " prefix
        }

        test("ordered list: cursor position is after prefix on each new line") {
            controller.toggleFormat(RichTextFormat.ORDERED_LIST)
            typeText("x")
            pressEnter()

            // Cursor should be at end of "1. x\n2. " = position 9
            controller.state.selectionStart shouldBe "1. x\n2. ".length
        }
    }

    // ==================== Test 22: Paste ====================

    test("pasting text inside bold span inherits bold") {
        controller.toggleFormat(RichTextFormat.BOLD)
        typeText("ab")
        moveCursor(1)
        controller.onTextChanged("aXYZb", 4, 4)

        controller.state.text shouldBe "aXYZb"
        val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
        boldSpans shouldHaveSize 1
        boldSpans[0].start shouldBe 0
        boldSpans[0].end shouldBe 5
    }

    // ==================== Test 23: Link ====================

    test("applyLink adds link format") {
        typeText("click here for info")
        selectRange(6, 10)
        controller.applyLink("here", "https://example.com")

        val linkSpans = controller.state.spans.filter { RichTextFormat.LINK in it.formats }
        linkSpans shouldHaveSize 1
    }

    // ==================== Test 24: Round-trip Markdown ====================

    context("round-trip markdown") {
        test("bold round-trip") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            val md = controller.toMarkdown()
            md shouldBe "**hello**"

            controller.clear()
            controller.fromMarkdown(md)
            controller.state.text shouldBe "hello"
            controller.state.spans.any { RichTextFormat.BOLD in it.formats }.shouldBeTrue()
        }

        test("mixed formatting round-trip") {
            typeText("say ")
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText(" world")

            val md = controller.toMarkdown()
            controller.clear()
            controller.fromMarkdown(md)

            controller.state.text shouldBe "say hello world"
            val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans shouldHaveSize 1
            boldSpans[0].start shouldBe 4
            boldSpans[0].end shouldBe 9
        }
    }

    // ==================== Test 25: Mixed Formatting + List ====================

    context("mixed formatting with lists") {
        test("bold text inside ordered list") {
            controller.toggleFormat(RichTextFormat.ORDERED_LIST)
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("bold item")

            controller.state.text shouldBe "1. bold item"
            val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans shouldHaveSize 1
        }

        test("bold text inside bullet list + Enter continues both") {
            controller.toggleFormat(RichTextFormat.BULLET_LIST)
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("item1")
            pressEnter()
            typeText("item2")

            controller.state.text shouldBe "- item1\n- item2"
            // Bold should cover the typed content
            val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans.size shouldBe 1
        }
    }

    // ==================== Test 26: Enter Key with Formatting (legacy compat) ====================

    context("enter key with formatting") {
        test("newline inside bold span inherits bold") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            pressEnter()

            val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans shouldHaveSize 1
            boldSpans[0].start shouldBe 0
            boldSpans[0].end shouldBe 6 // "hello\n"
        }

        test("typing after enter continues bold") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("hello")
            pressEnter()
            typeText("world")

            val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
            boldSpans shouldHaveSize 1
            boldSpans[0].start shouldBe 0
            boldSpans[0].end shouldBe 11 // "hello\nworld"
        }
    }

    // ==================== Test 27: Rapid Enter Presses ====================

    context("rapid Enter presses") {
        test("plain text + 5 rapid Enters — correct number of newlines") {
            typeText("start")
            pressEnter()
            pressEnter()
            pressEnter()
            pressEnter()
            pressEnter()

            controller.state.text shouldBe "start\n\n\n\n\n"
            controller.state.text.count { it == '\n' } shouldBe 5
        }

        test("bold + 3 rapid Enters — no text duplication") {
            controller.toggleFormat(RichTextFormat.BOLD)
            typeText("x")
            pressEnter()
            pressEnter()
            pressEnter()

            controller.state.text shouldBe "x\n\n\n"
            controller.state.text.count { it == 'x' } shouldBe 1
        }
    }
})

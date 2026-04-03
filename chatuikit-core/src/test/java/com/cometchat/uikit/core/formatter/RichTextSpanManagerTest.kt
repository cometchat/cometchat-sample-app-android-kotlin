package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain as shouldContainStr

/**
 * Tests for [RichTextSpanManager] — the core span engine.
 */
class RichTextSpanManagerTest : FunSpec({

    lateinit var manager: RichTextSpanManager

    beforeEach {
        manager = RichTextSpanManager()
    }

    // ==================== addFormat ====================

    context("addFormat") {
        test("creates span on empty manager") {
            manager.addFormat(0, 5, RichTextFormat.BOLD)
            manager.spans shouldHaveSize 1
            manager.spans[0] shouldBe RichTextSpan(0, 5, setOf(RichTextFormat.BOLD))
        }

        test("ignores empty range") {
            manager.addFormat(3, 3, RichTextFormat.BOLD)
            manager.spans.shouldBeEmpty()
        }

        test("ignores reversed range") {
            manager.addFormat(5, 3, RichTextFormat.BOLD)
            manager.spans.shouldBeEmpty()
        }

        test("merges overlapping spans with same format") {
            manager.addFormat(0, 5, RichTextFormat.BOLD)
            manager.addFormat(3, 8, RichTextFormat.BOLD)
            manager.spans shouldHaveSize 1
            manager.spans[0].start shouldBe 0
            manager.spans[0].end shouldBe 8
            manager.spans[0].formats shouldContain RichTextFormat.BOLD
        }

        test("splits span when adding different format to partial range") {
            manager.addFormat(0, 10, RichTextFormat.BOLD)
            manager.addFormat(3, 7, RichTextFormat.ITALIC)
            manager.spans shouldHaveSize 3
            manager.spans[0] shouldBe RichTextSpan(0, 3, setOf(RichTextFormat.BOLD))
            manager.spans[1].formats shouldBe setOf(RichTextFormat.BOLD, RichTextFormat.ITALIC)
            manager.spans[1].start shouldBe 3
            manager.spans[1].end shouldBe 7
            manager.spans[2] shouldBe RichTextSpan(7, 10, setOf(RichTextFormat.BOLD))
        }

        test("fills gaps between existing spans") {
            manager.addFormat(0, 3, RichTextFormat.BOLD)
            manager.addFormat(7, 10, RichTextFormat.BOLD)
            manager.addFormat(0, 10, RichTextFormat.ITALIC)
            val spans = manager.spans
            spans shouldHaveSize 3
            spans[0].formats shouldBe setOf(RichTextFormat.BOLD, RichTextFormat.ITALIC)
            spans[0].start shouldBe 0
            spans[0].end shouldBe 3
            spans[1].formats shouldBe setOf(RichTextFormat.ITALIC)
            spans[1].start shouldBe 3
            spans[1].end shouldBe 7
            spans[2].formats shouldBe setOf(RichTextFormat.BOLD, RichTextFormat.ITALIC)
            spans[2].start shouldBe 7
            spans[2].end shouldBe 10
        }

        test("adjacent spans with same formats merge") {
            manager.addFormat(0, 5, RichTextFormat.BOLD)
            manager.addFormat(5, 10, RichTextFormat.BOLD)
            manager.spans shouldHaveSize 1
            manager.spans[0].start shouldBe 0
            manager.spans[0].end shouldBe 10
        }
    }

    // ==================== removeFormat ====================

    context("removeFormat") {
        test("removes format from entire span") {
            manager.addFormat(0, 10, RichTextFormat.BOLD)
            manager.removeFormat(0, 10, RichTextFormat.BOLD)
            manager.spans.shouldBeEmpty()
        }

        test("removes format from partial range") {
            manager.addFormat(0, 10, RichTextFormat.BOLD)
            manager.removeFormat(3, 7, RichTextFormat.BOLD)
            manager.spans shouldHaveSize 2
            manager.spans[0] shouldBe RichTextSpan(0, 3, setOf(RichTextFormat.BOLD))
            manager.spans[1] shouldBe RichTextSpan(7, 10, setOf(RichTextFormat.BOLD))
        }

        test("only removes specified format, keeps others") {
            manager.addFormat(0, 10, RichTextFormat.BOLD)
            manager.addFormat(0, 10, RichTextFormat.ITALIC)
            manager.removeFormat(0, 10, RichTextFormat.BOLD)
            manager.spans shouldHaveSize 1
            manager.spans[0].formats shouldBe setOf(RichTextFormat.ITALIC)
        }

        test("ignores empty range") {
            manager.addFormat(0, 10, RichTextFormat.BOLD)
            manager.removeFormat(5, 5, RichTextFormat.BOLD)
            manager.spans shouldHaveSize 1
            manager.spans[0].end shouldBe 10
        }
    }

    // ==================== getFormatsAt ====================

    context("getFormatsAt") {
        test("returns formats at position") {
            manager.addFormat(0, 5, RichTextFormat.BOLD)
            manager.addFormat(2, 8, RichTextFormat.ITALIC)
            manager.getFormatsAt(3) shouldBe setOf(RichTextFormat.BOLD, RichTextFormat.ITALIC)
        }

        test("returns empty for unformatted position") {
            manager.addFormat(0, 5, RichTextFormat.BOLD)
            manager.getFormatsAt(5).shouldBeEmpty()
            manager.getFormatsAt(6).shouldBeEmpty()
        }

        test("at span boundary end returns empty (exclusive)") {
            manager.addFormat(0, 5, RichTextFormat.BOLD)
            manager.getFormatsAt(5).shouldBeEmpty()
        }
    }

    // ==================== getFormatsInRange ====================

    context("getFormatsInRange") {
        test("returns formats covering entire range") {
            manager.addFormat(0, 10, RichTextFormat.BOLD)
            manager.addFormat(3, 7, RichTextFormat.ITALIC)
            manager.getFormatsInRange(3, 7) shouldBe setOf(RichTextFormat.BOLD, RichTextFormat.ITALIC)
        }

        test("excludes format not covering entire range") {
            manager.addFormat(0, 10, RichTextFormat.BOLD)
            manager.addFormat(3, 7, RichTextFormat.ITALIC)
            manager.getFormatsInRange(0, 10) shouldBe setOf(RichTextFormat.BOLD)
        }

        test("returns empty for unformatted range") {
            manager.addFormat(0, 3, RichTextFormat.BOLD)
            manager.getFormatsInRange(5, 10).shouldBeEmpty()
        }

        test("returns empty for empty range") {
            manager.addFormat(0, 10, RichTextFormat.BOLD)
            manager.getFormatsInRange(5, 5).shouldBeEmpty()
        }
    }

    // ==================== onTextInserted ====================

    context("onTextInserted") {
        test("extends span when inserting at end") {
            manager.addFormat(0, 5, RichTextFormat.BOLD)
            manager.onTextInserted(5, 3)
            manager.spans shouldHaveSize 1
            manager.spans[0].start shouldBe 0
            manager.spans[0].end shouldBe 8
        }

        test("extends span when inserting in middle") {
            manager.addFormat(0, 5, RichTextFormat.BOLD)
            manager.onTextInserted(3, 2)
            manager.spans shouldHaveSize 1
            manager.spans[0].start shouldBe 0
            manager.spans[0].end shouldBe 7
        }

        test("shifts span after insertion point") {
            manager.addFormat(5, 10, RichTextFormat.BOLD)
            manager.onTextInserted(2, 3)
            manager.spans shouldHaveSize 1
            manager.spans[0].start shouldBe 8
            manager.spans[0].end shouldBe 13
        }

        test("does not affect span before insertion") {
            manager.addFormat(0, 5, RichTextFormat.BOLD)
            manager.onTextInserted(7, 3)
            manager.spans shouldHaveSize 1
            manager.spans[0].start shouldBe 0
            manager.spans[0].end shouldBe 5
        }

        test("at span start extends span") {
            manager.addFormat(3, 8, RichTextFormat.BOLD)
            manager.onTextInserted(3, 2)
            manager.spans shouldHaveSize 1
            manager.spans[0].start shouldBe 3
            manager.spans[0].end shouldBe 10
        }

        test("with zero length does nothing") {
            manager.addFormat(0, 5, RichTextFormat.BOLD)
            manager.onTextInserted(3, 0)
            manager.spans[0].end shouldBe 5
        }

        test("handles multiple spans correctly") {
            manager.addFormat(0, 3, RichTextFormat.BOLD)
            manager.addFormat(5, 8, RichTextFormat.ITALIC)
            manager.onTextInserted(4, 2)
            manager.spans shouldHaveSize 2
            manager.spans[0].start shouldBe 0
            manager.spans[0].end shouldBe 3
            manager.spans[1].start shouldBe 7
            manager.spans[1].end shouldBe 10
        }
    }

    // ==================== onTextDeleted ====================

    context("onTextDeleted") {
        test("removes span entirely within deletion") {
            manager.addFormat(3, 7, RichTextFormat.BOLD)
            manager.onTextDeleted(2, 8)
            manager.spans.shouldBeEmpty()
        }

        test("trims span overlapping deletion start") {
            manager.addFormat(0, 10, RichTextFormat.BOLD)
            manager.onTextDeleted(5, 10)
            manager.spans shouldHaveSize 1
            manager.spans[0].start shouldBe 0
            manager.spans[0].end shouldBe 5
        }

        test("shrinks span containing deletion") {
            manager.addFormat(0, 10, RichTextFormat.BOLD)
            manager.onTextDeleted(3, 7)
            manager.spans shouldHaveSize 1
            manager.spans[0].start shouldBe 0
            manager.spans[0].end shouldBe 6
        }

        test("shifts span after deletion") {
            manager.addFormat(10, 15, RichTextFormat.BOLD)
            manager.onTextDeleted(3, 7)
            manager.spans shouldHaveSize 1
            manager.spans[0].start shouldBe 6
            manager.spans[0].end shouldBe 11
        }

        test("does not affect span before deletion") {
            manager.addFormat(0, 3, RichTextFormat.BOLD)
            manager.onTextDeleted(5, 8)
            manager.spans shouldHaveSize 1
            manager.spans[0].start shouldBe 0
            manager.spans[0].end shouldBe 3
        }
    }

    // ==================== toMarkdown ====================

    context("toMarkdown") {
        test("wraps bold text") {
            manager.addFormat(0, 5, RichTextFormat.BOLD)
            manager.toMarkdown("hello") shouldBe "**hello**"
        }

        test("wraps italic text") {
            manager.addFormat(0, 5, RichTextFormat.ITALIC)
            manager.toMarkdown("hello") shouldBe "_hello_"
        }

        test("wraps underline text") {
            manager.addFormat(0, 5, RichTextFormat.UNDERLINE)
            manager.toMarkdown("hello") shouldBe "<u>hello</u>"
        }

        test("wraps strikethrough text") {
            manager.addFormat(0, 5, RichTextFormat.STRIKETHROUGH)
            manager.toMarkdown("hello") shouldBe "~~hello~~"
        }

        test("wraps inline code") {
            manager.addFormat(0, 5, RichTextFormat.INLINE_CODE)
            manager.toMarkdown("hello") shouldBe "`hello`"
        }

        test("wraps code block") {
            manager.addFormat(0, 5, RichTextFormat.CODE_BLOCK)
            manager.toMarkdown("hello") shouldBe "```\nhello\n```"
        }

        test("with no spans returns plain text") {
            manager.toMarkdown("hello world") shouldBe "hello world"
        }

        test("partial bold") {
            manager.addFormat(6, 11, RichTextFormat.BOLD)
            manager.toMarkdown("hello world") shouldBe "hello **world**"
        }

        test("nested bold and italic") {
            manager.addFormat(0, 5, RichTextFormat.BOLD)
            manager.addFormat(0, 5, RichTextFormat.ITALIC)
            manager.toMarkdown("hello") shouldBe "**_hello_**"
        }
    }

    // ==================== fromMarkdown ====================

    context("fromMarkdown") {
        test("parses bold") {
            val (text, spans) = manager.fromMarkdown("**hello**")
            text shouldBe "hello"
            spans shouldHaveSize 1
            spans[0].start shouldBe 0
            spans[0].end shouldBe 5
            spans[0].formats shouldContain RichTextFormat.BOLD
        }

        test("parses italic") {
            val (text, spans) = manager.fromMarkdown("_hello_")
            text shouldBe "hello"
            spans shouldHaveSize 1
            spans[0].formats shouldContain RichTextFormat.ITALIC
        }

        test("parses strikethrough") {
            val (text, spans) = manager.fromMarkdown("~~hello~~")
            text shouldBe "hello"
            spans shouldHaveSize 1
            spans[0].formats shouldContain RichTextFormat.STRIKETHROUGH
        }

        test("parses inline code") {
            val (text, spans) = manager.fromMarkdown("`hello`")
            text shouldBe "hello"
            spans shouldHaveSize 1
            spans[0].formats shouldContain RichTextFormat.INLINE_CODE
        }

        test("parses underline") {
            val (text, spans) = manager.fromMarkdown("<u>hello</u>")
            text shouldBe "hello"
            spans shouldHaveSize 1
            spans[0].formats shouldContain RichTextFormat.UNDERLINE
        }

        test("preserves plain text around formatted text") {
            val (text, spans) = manager.fromMarkdown("say **hello** world")
            text shouldBe "say hello world"
            spans shouldHaveSize 1
            spans[0].start shouldBe 4
            spans[0].end shouldBe 9
        }

        test("with no formatting returns plain text") {
            val (text, spans) = manager.fromMarkdown("hello world")
            text shouldBe "hello world"
            spans.shouldBeEmpty()
        }
    }

    // ==================== clear ====================

    test("clear removes all spans") {
        manager.addFormat(0, 5, RichTextFormat.BOLD)
        manager.addFormat(3, 8, RichTextFormat.ITALIC)
        manager.clear()
        manager.spans.shouldBeEmpty()
    }
})

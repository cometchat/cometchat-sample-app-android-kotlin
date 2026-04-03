package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class MarkdownRendererTest : FunSpec({

    // ==================== parseInline tests ====================

    test("parseInline - bold **text**") {
        val (plain, spans) = MarkdownRenderer.parseInline("hello **bold** world")
        plain shouldBe "hello bold world"
        spans shouldHaveSize 1
        spans[0].format shouldBe RichTextFormat.BOLD
        spans[0].start shouldBe 6
        spans[0].end shouldBe 10
        plain.substring(spans[0].start, spans[0].end) shouldBe "bold"
    }

    test("parseInline - italic _text_") {
        val (plain, spans) = MarkdownRenderer.parseInline("hello _italic_ world")
        plain shouldBe "hello italic world"
        spans shouldHaveSize 1
        spans[0].format shouldBe RichTextFormat.ITALIC
        spans[0].start shouldBe 6
        spans[0].end shouldBe 12
        plain.substring(spans[0].start, spans[0].end) shouldBe "italic"
    }

    test("parseInline - strikethrough ~~text~~") {
        val (plain, spans) = MarkdownRenderer.parseInline("hello ~~strike~~ world")
        plain shouldBe "hello strike world"
        spans shouldHaveSize 1
        spans[0].format shouldBe RichTextFormat.STRIKETHROUGH
        plain.substring(spans[0].start, spans[0].end) shouldBe "strike"
    }

    test("parseInline - underline <u>text</u>") {
        val (plain, spans) = MarkdownRenderer.parseInline("hello <u>under</u> world")
        plain shouldBe "hello under world"
        spans shouldHaveSize 1
        spans[0].format shouldBe RichTextFormat.UNDERLINE
        plain.substring(spans[0].start, spans[0].end) shouldBe "under"
    }

    test("parseInline - inline code `text`") {
        val (plain, spans) = MarkdownRenderer.parseInline("use `println()` here")
        plain shouldBe "use println() here"
        spans shouldHaveSize 1
        spans[0].format shouldBe RichTextFormat.INLINE_CODE
        plain.substring(spans[0].start, spans[0].end) shouldBe "println()"
    }

    test("parseInline - link [text](url)") {
        val (plain, spans) = MarkdownRenderer.parseInline("click [here](https://example.com) now")
        plain shouldBe "click here now"
        spans shouldHaveSize 1
        spans[0].format shouldBe RichTextFormat.LINK
        spans[0].url shouldBe "https://example.com"
        plain.substring(spans[0].start, spans[0].end) shouldBe "here"
    }

    test("parseInline - multiple formats in one line") {
        val (plain, spans) = MarkdownRenderer.parseInline("**bold** and _italic_")
        plain shouldBe "bold and italic"
        spans shouldHaveSize 2
        spans[0].format shouldBe RichTextFormat.BOLD
        plain.substring(spans[0].start, spans[0].end) shouldBe "bold"
        spans[1].format shouldBe RichTextFormat.ITALIC
        plain.substring(spans[1].start, spans[1].end) shouldBe "italic"
    }

    test("parseInline - plain text with no formatting") {
        val (plain, spans) = MarkdownRenderer.parseInline("just plain text")
        plain shouldBe "just plain text"
        spans shouldHaveSize 0
    }

    test("parseInline - empty string") {
        val (plain, spans) = MarkdownRenderer.parseInline("")
        plain shouldBe ""
        spans shouldHaveSize 0
    }

    // ==================== parse (block-level) tests ====================

    test("parse - plain text") {
        val segments = MarkdownRenderer.parse("hello world")
        segments shouldHaveSize 1
        segments[0].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.Text>()
        (segments[0] as MarkdownRenderer.RenderedSegment.Text).text shouldBe "hello world"
    }

    test("parse - bullet list") {
        val segments = MarkdownRenderer.parse("- item one\n- item two")
        segments shouldHaveSize 2
        segments[0].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.BulletItem>()
        (segments[0] as MarkdownRenderer.RenderedSegment.BulletItem).text shouldBe "item one"
        segments[1].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.BulletItem>()
        (segments[1] as MarkdownRenderer.RenderedSegment.BulletItem).text shouldBe "item two"
    }

    test("parse - ordered list") {
        val segments = MarkdownRenderer.parse("1. first\n2. second")
        segments shouldHaveSize 2
        segments[0].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.OrderedItem>()
        val item0 = segments[0] as MarkdownRenderer.RenderedSegment.OrderedItem
        item0.number shouldBe 1
        item0.text shouldBe "first"
        val item1 = segments[1] as MarkdownRenderer.RenderedSegment.OrderedItem
        item1.number shouldBe 2
        item1.text shouldBe "second"
    }

    test("parse - blockquote") {
        val segments = MarkdownRenderer.parse("> quoted text")
        segments shouldHaveSize 1
        segments[0].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.Blockquote>()
        (segments[0] as MarkdownRenderer.RenderedSegment.Blockquote).text shouldBe "quoted text"
    }

    test("parse - code block") {
        val md = "```kotlin\nval x = 1\n```"
        val segments = MarkdownRenderer.parse(md)
        segments shouldHaveSize 1
        segments[0].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.CodeBlock>()
        val cb = segments[0] as MarkdownRenderer.RenderedSegment.CodeBlock
        cb.language shouldBe "kotlin"
        cb.code shouldBe "val x = 1"
    }

    test("parse - text before and after code block") {
        val md = "before\n```\ncode\n```\nafter"
        val segments = MarkdownRenderer.parse(md)
        segments shouldHaveSize 3
        segments[0].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.Text>()
        (segments[0] as MarkdownRenderer.RenderedSegment.Text).text shouldBe "before"
        segments[1].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.CodeBlock>()
        (segments[1] as MarkdownRenderer.RenderedSegment.CodeBlock).code shouldBe "code"
        segments[2].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.Text>()
        (segments[2] as MarkdownRenderer.RenderedSegment.Text).text shouldBe "after"
    }

    test("parse - bold inside bullet list item") {
        val segments = MarkdownRenderer.parse("- **bold item**")
        segments shouldHaveSize 1
        val item = segments[0] as MarkdownRenderer.RenderedSegment.BulletItem
        item.text shouldBe "**bold item**"
        // The spans should reference the ORIGINAL text (with markers)
        // parseInline on the content should strip markers
        val (plain, spans) = MarkdownRenderer.parseInline(item.text)
        plain shouldBe "bold item"
        spans shouldHaveSize 1
        spans[0].format shouldBe RichTextFormat.BOLD
    }

    test("parse - mixed content: text, list, blockquote") {
        val md = "hello\n- bullet\n> quote"
        val segments = MarkdownRenderer.parse(md)
        segments shouldHaveSize 3
        segments[0].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.Text>()
        segments[1].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.BulletItem>()
        segments[2].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.Blockquote>()
    }

    // ==================== End-to-end: composer output → bubble parse ====================

    test("end-to-end: bold text round-trip") {
        // Simulate what the composer sends: markdown with **bold**
        val markdown = "hello **world**"
        val segments = MarkdownRenderer.parse(markdown)
        segments shouldHaveSize 1
        val textSeg = segments[0] as MarkdownRenderer.RenderedSegment.Text
        // The text segment stores the raw markdown
        textSeg.text shouldBe "hello **world**"
        // parseInline strips markers and returns spans
        val (plain, spans) = MarkdownRenderer.parseInline(textSeg.text)
        plain shouldBe "hello world"
        spans shouldHaveSize 1
        spans[0].format shouldBe RichTextFormat.BOLD
        spans[0].start shouldBe 6
        spans[0].end shouldBe 11
    }

    test("end-to-end: multiple inline formats") {
        val markdown = "**bold** _italic_ ~~strike~~ `code`"
        val (plain, spans) = MarkdownRenderer.parseInline(markdown)
        plain shouldBe "bold italic strike code"
        spans shouldHaveSize 4
        spans[0].format shouldBe RichTextFormat.BOLD
        spans[1].format shouldBe RichTextFormat.ITALIC
        spans[2].format shouldBe RichTextFormat.STRIKETHROUGH
        spans[3].format shouldBe RichTextFormat.INLINE_CODE
    }

    test("end-to-end: code block with surrounding text") {
        val markdown = "before\n```js\nconsole.log('hi')\n```\nafter"
        val segments = MarkdownRenderer.parse(markdown)
        segments shouldHaveSize 3
        (segments[0] as MarkdownRenderer.RenderedSegment.Text).text shouldBe "before"
        val code = segments[1] as MarkdownRenderer.RenderedSegment.CodeBlock
        code.language shouldBe "js"
        code.code shouldBe "console.log('hi')"
        (segments[2] as MarkdownRenderer.RenderedSegment.Text).text shouldBe "after"
    }

    // ==================== Nested/combined format tests ====================

    test("parseInline - bold + italic nested: **_text_**") {
        val (plain, spans) = MarkdownRenderer.parseInline("**_bold italic_**")
        plain shouldBe "bold italic"
        spans shouldHaveSize 2
        val formats = spans.map { it.format }.toSet()
        formats shouldBe setOf(RichTextFormat.BOLD, RichTextFormat.ITALIC)
        // Both spans should cover the same text
        for (span in spans) {
            plain.substring(span.start, span.end) shouldBe "bold italic"
        }
    }

    test("parseInline - bold + strikethrough nested: ~~**text**~~") {
        val (plain, spans) = MarkdownRenderer.parseInline("~~**struck bold**~~")
        plain shouldBe "struck bold"
        spans shouldHaveSize 2
        val formats = spans.map { it.format }.toSet()
        formats shouldBe setOf(RichTextFormat.BOLD, RichTextFormat.STRIKETHROUGH)
    }

    test("parseInline - bold + underline nested: <u>**text**</u>") {
        val (plain, spans) = MarkdownRenderer.parseInline("<u>**underline bold**</u>")
        plain shouldBe "underline bold"
        spans shouldHaveSize 2
        val formats = spans.map { it.format }.toSet()
        formats shouldBe setOf(RichTextFormat.BOLD, RichTextFormat.UNDERLINE)
    }

    test("parseInline - triple nested: **~~_text_~~**") {
        val (plain, spans) = MarkdownRenderer.parseInline("**~~_triple_~~**")
        plain shouldBe "triple"
        spans shouldHaveSize 3
        val formats = spans.map { it.format }.toSet()
        formats shouldBe setOf(RichTextFormat.BOLD, RichTextFormat.STRIKETHROUGH, RichTextFormat.ITALIC)
    }

    test("parseInline - adjacent different formats: **bold** _italic_") {
        val (plain, spans) = MarkdownRenderer.parseInline("**bold** _italic_")
        plain shouldBe "bold italic"
        spans shouldHaveSize 2
        spans[0].format shouldBe RichTextFormat.BOLD
        plain.substring(spans[0].start, spans[0].end) shouldBe "bold"
        spans[1].format shouldBe RichTextFormat.ITALIC
        plain.substring(spans[1].start, spans[1].end) shouldBe "italic"
    }

    test("parseInline - nested with surrounding text: hello **_world_** end") {
        val (plain, spans) = MarkdownRenderer.parseInline("hello **_world_** end")
        plain shouldBe "hello world end"
        spans shouldHaveSize 2
        val formats = spans.map { it.format }.toSet()
        formats shouldBe setOf(RichTextFormat.BOLD, RichTextFormat.ITALIC)
        for (span in spans) {
            plain.substring(span.start, span.end) shouldBe "world"
        }
    }
})


class MarkdownRendererRoundTripTest : FunSpec({

    test("round-trip: controller bold → toMarkdown → parse → spans") {
        val controller = RichTextEditorController()
        // Type "hello "
        controller.onTextChanged("hello ", 6, 6)
        // Toggle bold on
        controller.toggleFormat(RichTextFormat.BOLD)
        // Type "world"
        controller.onTextChanged("hello world", 11, 11)
        // Toggle bold off
        controller.toggleFormat(RichTextFormat.BOLD)

        val markdown = controller.toMarkdown()
        println("Markdown output: [$markdown]")

        // Parse it back
        val segments = MarkdownRenderer.parse(markdown)
        segments shouldHaveSize 1
        val textSeg = segments[0] as MarkdownRenderer.RenderedSegment.Text

        val (plain, spans) = MarkdownRenderer.parseInline(textSeg.text)
        println("Plain text: [$plain]")
        println("Spans: $spans")

        plain shouldBe "hello world"
        spans shouldHaveSize 1
        spans[0].format shouldBe RichTextFormat.BOLD
        plain.substring(spans[0].start, spans[0].end) shouldBe "world"
    }

    test("round-trip: controller italic → toMarkdown → parse → spans") {
        val controller = RichTextEditorController()
        controller.onTextChanged("normal ", 7, 7)
        controller.toggleFormat(RichTextFormat.ITALIC)
        controller.onTextChanged("normal italic", 13, 13)
        controller.toggleFormat(RichTextFormat.ITALIC)

        val markdown = controller.toMarkdown()
        println("Italic markdown: [$markdown]")

        val (plain, spans) = MarkdownRenderer.parseInline(markdown)
        println("Italic plain: [$plain], spans: $spans")

        plain shouldBe "normal italic"
        spans shouldHaveSize 1
        spans[0].format shouldBe RichTextFormat.ITALIC
        plain.substring(spans[0].start, spans[0].end) shouldBe "italic"
    }

    test("round-trip: controller strikethrough → toMarkdown → parse → spans") {
        val controller = RichTextEditorController()
        controller.onTextChanged("before ", 7, 7)
        controller.toggleFormat(RichTextFormat.STRIKETHROUGH)
        controller.onTextChanged("before struck", 13, 13)
        controller.toggleFormat(RichTextFormat.STRIKETHROUGH)

        val markdown = controller.toMarkdown()
        println("Strike markdown: [$markdown]")

        val (plain, spans) = MarkdownRenderer.parseInline(markdown)
        plain shouldBe "before struck"
        spans shouldHaveSize 1
        spans[0].format shouldBe RichTextFormat.STRIKETHROUGH
        plain.substring(spans[0].start, spans[0].end) shouldBe "struck"
    }

    test("round-trip: controller underline → toMarkdown → parse → spans") {
        val controller = RichTextEditorController()
        controller.onTextChanged("before ", 7, 7)
        controller.toggleFormat(RichTextFormat.UNDERLINE)
        controller.onTextChanged("before under", 12, 12)
        controller.toggleFormat(RichTextFormat.UNDERLINE)

        val markdown = controller.toMarkdown()
        println("Underline markdown: [$markdown]")

        val (plain, spans) = MarkdownRenderer.parseInline(markdown)
        plain shouldBe "before under"
        spans shouldHaveSize 1
        spans[0].format shouldBe RichTextFormat.UNDERLINE
        plain.substring(spans[0].start, spans[0].end) shouldBe "under"
    }

    test("round-trip: controller inline code → toMarkdown → parse → spans") {
        val controller = RichTextEditorController()
        controller.onTextChanged("use ", 4, 4)
        controller.toggleFormat(RichTextFormat.INLINE_CODE)
        controller.onTextChanged("use foo()", 9, 9)
        controller.toggleFormat(RichTextFormat.INLINE_CODE)

        val markdown = controller.toMarkdown()
        println("Code markdown: [$markdown]")

        val (plain, spans) = MarkdownRenderer.parseInline(markdown)
        plain shouldBe "use foo()"
        spans shouldHaveSize 1
        spans[0].format shouldBe RichTextFormat.INLINE_CODE
        plain.substring(spans[0].start, spans[0].end) shouldBe "foo()"
    }

    test("round-trip: bullet list") {
        val controller = RichTextEditorController()
        controller.toggleFormat(RichTextFormat.BULLET_LIST)
        // After toggling, text becomes "- " with cursor at 2
        val afterToggle = controller.state.text
        println("After bullet toggle: [$afterToggle]")
        controller.onTextChanged(afterToggle + "item one", afterToggle.length + 8, afterToggle.length + 8)

        val markdown = controller.toMarkdown()
        println("Bullet markdown: [$markdown]")

        val segments = MarkdownRenderer.parse(markdown)
        segments.forEach { println("Segment: $it") }
        segments shouldHaveSize 1
        segments[0].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.BulletItem>()
        (segments[0] as MarkdownRenderer.RenderedSegment.BulletItem).text shouldBe "item one"
    }

    test("round-trip: blockquote") {
        val controller = RichTextEditorController()
        controller.toggleFormat(RichTextFormat.BLOCKQUOTE)
        val afterToggle = controller.state.text
        println("After blockquote toggle: [$afterToggle]")
        controller.onTextChanged(afterToggle + "quoted", afterToggle.length + 6, afterToggle.length + 6)

        val markdown = controller.toMarkdown()
        println("Blockquote markdown: [$markdown]")

        val segments = MarkdownRenderer.parse(markdown)
        segments shouldHaveSize 1
        segments[0].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.Blockquote>()
        (segments[0] as MarkdownRenderer.RenderedSegment.Blockquote).text shouldBe "quoted"
    }

    test("round-trip: ordered list") {
        val controller = RichTextEditorController()
        controller.toggleFormat(RichTextFormat.ORDERED_LIST)
        val afterToggle = controller.state.text
        println("After ordered toggle: [$afterToggle]")
        controller.onTextChanged(afterToggle + "first", afterToggle.length + 5, afterToggle.length + 5)

        val markdown = controller.toMarkdown()
        println("Ordered markdown: [$markdown]")

        val segments = MarkdownRenderer.parse(markdown)
        segments shouldHaveSize 1
        segments[0].shouldBeInstanceOf<MarkdownRenderer.RenderedSegment.OrderedItem>()
        val item = segments[0] as MarkdownRenderer.RenderedSegment.OrderedItem
        item.number shouldBe 1
        item.text shouldBe "first"
    }
})

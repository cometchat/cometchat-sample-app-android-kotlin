package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Example-based unit tests for [PlainTextStripper].
 *
 * Feature: formatted-text-previews
 * Complements the property-based tests with specific known-input/known-output cases.
 */
class PlainTextStripperTest : FunSpec({

    test("strips bold markers") {
        PlainTextStripper.strip("**hello**") shouldBe "hello"
    }

    test("strips italic markers") {
        PlainTextStripper.strip("_hello_") shouldBe "hello"
    }

    test("strips strikethrough markers") {
        PlainTextStripper.strip("~~hello~~") shouldBe "hello"
    }

    test("strips underline markers") {
        PlainTextStripper.strip("<u>hello</u>") shouldBe "hello"
    }

    test("strips inline code markers") {
        PlainTextStripper.strip("`code`") shouldBe "code"
    }

    test("strips link syntax keeping display text") {
        PlainTextStripper.strip("[click here](https://example.com)") shouldBe "click here"
    }

    test("strips fenced code block markers") {
        val input = "```kotlin\nval x = 1\n```"
        PlainTextStripper.strip(input) shouldBe "val x = 1"
    }

    test("strips bullet list prefix") {
        PlainTextStripper.strip("- item one") shouldBe "item one"
    }

    test("strips ordered list prefix") {
        PlainTextStripper.strip("1. first item") shouldBe "first item"
    }

    test("strips blockquote prefix") {
        PlainTextStripper.strip("> quoted text") shouldBe "quoted text"
    }

    test("strips mixed inline formats") {
        PlainTextStripper.strip("**bold** and _italic_ and ~~struck~~") shouldBe "bold and italic and struck"
    }

    test("strips nested formats") {
        PlainTextStripper.strip("**_bold italic_**") shouldBe "bold italic"
    }

    test("handles plain text without markdown") {
        PlainTextStripper.strip("just plain text") shouldBe "just plain text"
    }

    test("handles empty string") {
        PlainTextStripper.strip("") shouldBe ""
    }

    test("handles multiple segments joined with spaces") {
        val input = "hello\n- item\n> quote"
        val result = PlainTextStripper.strip(input)
        result shouldBe "hello item quote"
    }

    test("strips code block with language identifier") {
        val input = "```java\nSystem.out.println();\n```"
        PlainTextStripper.strip(input) shouldBe "System.out.println();"
    }

    test("handles text before and after code block") {
        val input = "before\n```\ncode\n```\nafter"
        val result = PlainTextStripper.strip(input)
        result shouldBe "before code after"
    }

    test("strips inline formatting inside list items") {
        PlainTextStripper.strip("- **bold item**") shouldBe "bold item"
    }

    test("strips inline formatting inside blockquotes") {
        PlainTextStripper.strip("> _italic quote_") shouldBe "italic quote"
    }
})

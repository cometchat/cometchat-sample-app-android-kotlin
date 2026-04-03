package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

/**
 * Tests for [FormatCompatibility].
 */
class FormatCompatibilityTest : FunSpec({

    test("code block disables all other formats") {
        val disabled = FormatCompatibility.getDisabledFormats(setOf(RichTextFormat.CODE_BLOCK))
        disabled shouldContain RichTextFormat.BOLD
        disabled shouldContain RichTextFormat.ITALIC
        disabled shouldContain RichTextFormat.UNDERLINE
        disabled shouldContain RichTextFormat.STRIKETHROUGH
        disabled shouldContain RichTextFormat.INLINE_CODE
        disabled shouldContain RichTextFormat.LINK
        disabled shouldContain RichTextFormat.BULLET_LIST
        disabled shouldContain RichTextFormat.ORDERED_LIST
        disabled shouldContain RichTextFormat.BLOCKQUOTE
    }

    test("bold only disables code block") {
        val disabled = FormatCompatibility.getDisabledFormats(setOf(RichTextFormat.BOLD))
        disabled shouldContain RichTextFormat.CODE_BLOCK
        disabled shouldNotContain RichTextFormat.ITALIC
        disabled shouldNotContain RichTextFormat.UNDERLINE
        disabled shouldNotContain RichTextFormat.STRIKETHROUGH
    }

    test("bullet list and ordered list are mutually exclusive") {
        FormatCompatibility.getDisabledFormats(setOf(RichTextFormat.BULLET_LIST)) shouldContain RichTextFormat.ORDERED_LIST
        FormatCompatibility.getDisabledFormats(setOf(RichTextFormat.ORDERED_LIST)) shouldContain RichTextFormat.BULLET_LIST
    }

    test("link and inline code are incompatible") {
        FormatCompatibility.getDisabledFormats(setOf(RichTextFormat.LINK)) shouldContain RichTextFormat.INLINE_CODE
        FormatCompatibility.getDisabledFormats(setOf(RichTextFormat.INLINE_CODE)) shouldContain RichTextFormat.LINK
    }

    test("inline formats are mutually compatible") {
        val active = setOf(RichTextFormat.BOLD, RichTextFormat.ITALIC, RichTextFormat.UNDERLINE)
        FormatCompatibility.isCompatible(RichTextFormat.STRIKETHROUGH, active) shouldBe true
        FormatCompatibility.isCompatible(RichTextFormat.INLINE_CODE, active) shouldBe true
    }

    test("isCompatible returns false for incompatible format") {
        FormatCompatibility.isCompatible(RichTextFormat.BOLD, setOf(RichTextFormat.CODE_BLOCK)) shouldBe false
        FormatCompatibility.isCompatible(RichTextFormat.ORDERED_LIST, setOf(RichTextFormat.BULLET_LIST)) shouldBe false
    }

    test("empty active formats disables nothing") {
        FormatCompatibility.getDisabledFormats(emptySet()).isEmpty() shouldBe true
    }

    test("multiple active formats accumulate disabled set") {
        val disabled = FormatCompatibility.getDisabledFormats(
            setOf(RichTextFormat.BULLET_LIST, RichTextFormat.LINK)
        )
        disabled shouldContain RichTextFormat.ORDERED_LIST
        disabled shouldContain RichTextFormat.INLINE_CODE
        disabled shouldContain RichTextFormat.CODE_BLOCK
    }
})

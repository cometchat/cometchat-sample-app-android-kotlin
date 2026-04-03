package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Tests for [RichTextSpan] data class.
 */
class RichTextSpanTest : FunSpec({

    test("isEmpty returns true for zero-length span") {
        RichTextSpan(5, 5, setOf(RichTextFormat.BOLD)).isEmpty shouldBe true
    }

    test("isEmpty returns true for negative-length span") {
        RichTextSpan(5, 3, setOf(RichTextFormat.BOLD)).isEmpty shouldBe true
    }

    test("isEmpty returns false for valid span") {
        RichTextSpan(0, 5, setOf(RichTextFormat.BOLD)).isEmpty shouldBe false
    }

    test("length returns correct value") {
        RichTextSpan(0, 5, setOf(RichTextFormat.BOLD)).length shouldBe 5
        RichTextSpan(3, 3, setOf(RichTextFormat.BOLD)).length shouldBe 0
        RichTextSpan(5, 3, setOf(RichTextFormat.BOLD)).length shouldBe 0
    }

    test("contains returns true for positions within span") {
        val span = RichTextSpan(2, 7, setOf(RichTextFormat.BOLD))
        span.contains(2) shouldBe true
        span.contains(4) shouldBe true
        span.contains(6) shouldBe true
    }

    test("contains returns false for position at end (exclusive)") {
        RichTextSpan(2, 7, setOf(RichTextFormat.BOLD)).contains(7) shouldBe false
    }

    test("contains returns false for positions outside span") {
        val span = RichTextSpan(2, 7, setOf(RichTextFormat.BOLD))
        span.contains(1) shouldBe false
        span.contains(8) shouldBe false
    }

    test("overlaps detects overlapping ranges") {
        val span = RichTextSpan(2, 7, setOf(RichTextFormat.BOLD))
        span.overlaps(0, 3) shouldBe true
        span.overlaps(5, 10) shouldBe true
        span.overlaps(3, 5) shouldBe true
        span.overlaps(0, 10) shouldBe true
    }

    test("overlaps returns false for non-overlapping ranges") {
        val span = RichTextSpan(2, 7, setOf(RichTextFormat.BOLD))
        span.overlaps(0, 2) shouldBe false
        span.overlaps(7, 10) shouldBe false
        span.overlaps(8, 12) shouldBe false
    }

    test("shift moves span by positive offset") {
        val shifted = RichTextSpan(2, 7, setOf(RichTextFormat.BOLD)).shift(3)
        shifted.start shouldBe 5
        shifted.end shouldBe 10
    }

    test("shift moves span by negative offset") {
        val shifted = RichTextSpan(5, 10, setOf(RichTextFormat.ITALIC)).shift(-2)
        shifted.start shouldBe 3
        shifted.end shouldBe 8
    }
})

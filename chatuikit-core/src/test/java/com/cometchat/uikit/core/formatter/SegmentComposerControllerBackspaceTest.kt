package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for backspace handling on empty Normal segments in [SegmentComposerController].
 * Validates Requirements 35.2 and 35.3.
 */
class SegmentComposerControllerBackspaceTest : FunSpec({

    lateinit var controller: SegmentComposerController

    beforeEach {
        controller = SegmentComposerController()
    }

    // ==================== handleBackspaceOnEmptyNormalSegment ====================

    test("backspace on empty Normal after Code focuses the Code segment") {
        // Setup: [Normal(seg0), Code(seg1), Normal(seg2)]
        // Put text in seg0 so insertCodeBlock creates a code block after it
        val firstNormal = controller.segments.first() as ComposerSegment.Normal
        firstNormal.controller.onTextChanged("hello", 5, 5)
        controller.setFocusedSegment(firstNormal.id)
        controller.insertCodeBlock()

        // Now segments should be: [Normal("hello"), Code(""), Normal("")]
        controller.segments.size shouldBe 3
        val normalAfterCode = controller.segments[2] as ComposerSegment.Normal
        normalAfterCode.controller.state.text shouldBe ""

        val codeSegment = controller.segments[1] as ComposerSegment.Code

        // Backspace on the empty Normal after Code
        val handled = controller.handleBackspaceOnEmptyNormalSegment(normalAfterCode)

        handled.shouldBeTrue()
        controller.focusedSegmentId shouldBe codeSegment.id
        controller.pendingFocusSegmentId shouldBe codeSegment.id
    }

    test("backspace on non-empty Normal segment returns false") {
        val firstNormal = controller.segments.first() as ComposerSegment.Normal
        firstNormal.controller.onTextChanged("hello", 5, 5)
        controller.setFocusedSegment(firstNormal.id)
        controller.insertCodeBlock()

        val normalAfterCode = controller.segments[2] as ComposerSegment.Normal
        normalAfterCode.controller.onTextChanged("world", 5, 5)

        val handled = controller.handleBackspaceOnEmptyNormalSegment(normalAfterCode)
        handled.shouldBeFalse()
    }

    test("backspace on last remaining Normal segment returns false (Req 35.3)") {
        // Only one Normal segment exists — should not be removed
        val onlyNormal = controller.segments.first() as ComposerSegment.Normal
        val handled = controller.handleBackspaceOnEmptyNormalSegment(onlyNormal)
        handled.shouldBeFalse()
        controller.segments.size shouldBe 1
    }

    test("backspace on empty Normal with no preceding Code returns false") {
        // Setup: [Normal(seg0), Code(seg1), Normal(seg2)]
        val firstNormal = controller.segments.first() as ComposerSegment.Normal
        firstNormal.controller.onTextChanged("hello", 5, 5)
        controller.setFocusedSegment(firstNormal.id)
        controller.insertCodeBlock()

        // Clear seg0 text to make it empty
        val seg0 = controller.segments[0] as ComposerSegment.Normal
        seg0.controller.clear()

        // Backspace on seg0 — no preceding Code segment
        val handled = controller.handleBackspaceOnEmptyNormalSegment(seg0)
        handled.shouldBeFalse()
    }

    // ==================== focusSegment ====================

    test("focusSegment sets both focusedSegmentId and pendingFocusSegmentId") {
        val firstNormal = controller.segments.first() as ComposerSegment.Normal
        firstNormal.controller.onTextChanged("hello", 5, 5)
        controller.setFocusedSegment(firstNormal.id)
        controller.insertCodeBlock()

        val codeSegment = controller.segments[1] as ComposerSegment.Code

        controller.focusSegment(codeSegment.id)

        controller.focusedSegmentId shouldBe codeSegment.id
        controller.pendingFocusSegmentId shouldBe codeSegment.id
    }

    test("focusSegment notifies listener") {
        var notified = false
        controller.setListener(object : SegmentComposerController.Listener {
            override fun onSegmentsChanged() { notified = true }
        })

        val firstNormal = controller.segments.first() as ComposerSegment.Normal
        controller.focusSegment(firstNormal.id)

        notified.shouldBeTrue()
    }

    // ==================== Segment invariant after backspace ====================

    test("after backspace removes Normal, at least one Normal remains (Req 35.3)") {
        val firstNormal = controller.segments.first() as ComposerSegment.Normal
        firstNormal.controller.onTextChanged("hello", 5, 5)
        controller.setFocusedSegment(firstNormal.id)
        controller.insertCodeBlock()

        // Segments: [Normal("hello"), Code(""), Normal("")]
        val normalAfterCode = controller.segments[2] as ComposerSegment.Normal
        controller.handleBackspaceOnEmptyNormalSegment(normalAfterCode)

        // Should still have at least one Normal
        controller.segments.any { it is ComposerSegment.Normal }.shouldBeTrue()
    }
})

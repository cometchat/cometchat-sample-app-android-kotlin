package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll

/**
 * Property-based tests for [RichTextFormat.Companion.computeDisabledFormats].
 *
 * Feature: v6-compact-composer, Property 1: Format compatibility is a pure function of active formats
 *
 * **Validates: Requirements 1.1, 1.2, 1.4, 1.5**
 */
class RichTextFormatComputeDisabledPropertyTest : StringSpec({

    /**
     * Arbitrary generator for random subsets of [RichTextFormat.entries].
     * Each entry is independently included or excluded with 50/50 probability.
     */
    val arbFormatSubset: Arb<Set<RichTextFormat>> = arbitrary { rs ->
        RichTextFormat.entries.filter { Arb.boolean().bind() }.toSet()
    }

    /**
     * Property 1: computeDisabledFormats returns the union of INCOMPATIBLE_FORMATS for each active format.
     *
     * For any random subset of RichTextFormat.entries, calling computeDisabledFormats()
     * must return exactly the union of INCOMPATIBLE_FORMATS[f] for each f in the active set.
     *
     * **Validates: Requirements 1.1, 1.2, 1.4, 1.5**
     */
    "Property 1: result equals union of INCOMPATIBLE_FORMATS for each active format" {
        checkAll(100, arbFormatSubset) { activeFormats ->
            val result = RichTextFormat.computeDisabledFormats(activeFormats)

            // Compute expected: union of INCOMPATIBLE_FORMATS[f] for each f in activeFormats
            val expected = activeFormats.flatMap { f ->
                RichTextFormat.INCOMPATIBLE_FORMATS[f] ?: emptySet()
            }.toSet()

            result shouldBe expected
        }
    }

    /**
     * Property 1: disabled set has no overlap with the active set when the active set
     * is a valid (UI-reachable) state.
     *
     * A valid active set is one built by only toggling formats that are NOT currently
     * disabled. This mirrors real usage: the UI disables buttons via computeDisabledFormats(),
     * so users can only toggle enabled formats.
     *
     * **Validates: Requirements 1.1, 1.2, 1.4, 1.5**
     */
    "Property 1: disabled formats have no overlap with a UI-reachable active format set" {
        checkAll(100, arbFormatSubset) { rawFormats ->
            // Build a valid active set by only toggling formats that are not disabled
            var validActiveFormats = emptySet<RichTextFormat>()
            for (fmt in rawFormats) {
                val disabled = RichTextFormat.computeDisabledFormats(validActiveFormats)
                if (fmt !in disabled) {
                    validActiveFormats = RichTextFormat.toggleFormat(validActiveFormats, fmt)
                }
            }

            val disabled = RichTextFormat.computeDisabledFormats(validActiveFormats)

            val overlap = validActiveFormats.intersect(disabled)
            overlap shouldBe emptySet()
        }
    }
})

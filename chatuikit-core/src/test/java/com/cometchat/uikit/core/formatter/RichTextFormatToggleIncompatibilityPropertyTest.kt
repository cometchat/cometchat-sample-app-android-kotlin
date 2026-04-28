package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

/**
 * Property-based tests for [RichTextFormat.Companion.toggleFormat] and
 * [RichTextFormat.Companion.computeDisabledFormats] working together.
 *
 * Feature: v6-rich-text-composer, Property 1: Format toggle respects incompatibility and auto-deselect rules
 *
 * **Validates: Requirements 4.1, 4.2, 5.1, 5.2, 5.3**
 */
class RichTextFormatToggleIncompatibilityPropertyTest : StringSpec({

    /**
     * Generates a UI-reachable active format set by simulating a sequence of
     * valid toggles. Each format is only toggled if it is not currently disabled,
     * mirroring real toolbar behavior where disabled buttons reject taps.
     */
    val arbReachableFormatSet: Arb<Set<RichTextFormat>> = arbitrary {
        val candidates = RichTextFormat.entries.filter { Arb.boolean().bind() }
        var active = emptySet<RichTextFormat>()
        for (fmt in candidates) {
            val disabled = RichTextFormat.computeDisabledFormats(active)
            if (fmt !in disabled) {
                active = RichTextFormat.toggleFormat(active, fmt)
            }
        }
        active
    }

    val arbFormat: Arb<RichTextFormat> = Arb.enum<RichTextFormat>()

    /**
     * Property 1a: After toggling any format on a UI-reachable active set,
     * the resulting active set contains no pair of formats where one appears
     * in the other's INCOMPATIBLE_FORMATS set.
     *
     * This verifies that toggleFormat enforces the incompatibility invariant:
     * for every pair (a, b) in the result, b is NOT in INCOMPATIBLE_FORMATS[a]
     * and a is NOT in INCOMPATIBLE_FORMATS[b].
     *
     * **Validates: Requirements 4.1, 4.2, 5.1, 5.2, 5.3**
     */
    "Property 1: after toggle, no pair in active set violates INCOMPATIBLE_FORMATS" {
        checkAll(100, arbReachableFormatSet, arbFormat) { activeFormats, format ->
            // Only toggle if the format is not disabled (simulating UI behavior)
            val disabled = RichTextFormat.computeDisabledFormats(activeFormats)
            if (format !in disabled || format in activeFormats) {
                val result = RichTextFormat.toggleFormat(activeFormats, format)

                // Check every pair in the result for incompatibility violations
                val resultList = result.toList()
                for (i in resultList.indices) {
                    for (j in i + 1 until resultList.size) {
                        val a = resultList[i]
                        val b = resultList[j]
                        val aIncompatible = RichTextFormat.INCOMPATIBLE_FORMATS[a] ?: emptySet()
                        val bIncompatible = RichTextFormat.INCOMPATIBLE_FORMATS[b] ?: emptySet()

                        // b should not be in a's incompatible set
                        (b in aIncompatible) shouldBe false
                        // a should not be in b's incompatible set
                        (a in bIncompatible) shouldBe false
                    }
                }
            }
        }
    }

    /**
     * Property 1b: After toggling any format on a UI-reachable active set,
     * computeDisabledFormats() on the resulting active set returns exactly
     * the union of INCOMPATIBLE_FORMATS[f] for every f in the active set.
     *
     * This verifies that computeDisabledFormats is a pure function that faithfully
     * reflects the INCOMPATIBLE_FORMATS map.
     *
     * **Validates: Requirements 4.1, 4.2, 5.1, 5.2, 5.3**
     */
    "Property 1: computeDisabledFormats equals union of INCOMPATIBLE_FORMATS for post-toggle active set" {
        checkAll(100, arbReachableFormatSet, arbFormat) { activeFormats, format ->
            val disabled = RichTextFormat.computeDisabledFormats(activeFormats)
            if (format !in disabled || format in activeFormats) {
                val result = RichTextFormat.toggleFormat(activeFormats, format)
                val resultDisabled = RichTextFormat.computeDisabledFormats(result)

                // Expected: union of INCOMPATIBLE_FORMATS[f] for each f in result
                val expected = result.flatMap { f ->
                    RichTextFormat.INCOMPATIBLE_FORMATS[f] ?: emptySet()
                }.toSet()

                resultDisabled shouldBe expected
            }
        }
    }
})

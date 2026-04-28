package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll

/**
 * Property-based tests for [RichTextFormat.Companion.toggleFormat].
 *
 * Feature: v6-compact-composer, Property 2: Toggle format correctly auto-deselects conflicting formats
 *
 * **Validates: Requirements 1.3, 1.4**
 */
class RichTextFormatToggleFormatPropertyTest : StringSpec({

    /**
     * Arbitrary generator for random subsets of [RichTextFormat.entries].
     */
    val arbFormatSubset: Arb<Set<RichTextFormat>> = arbitrary {
        RichTextFormat.entries.filter { Arb.boolean().bind() }.toSet()
    }

    val arbFormat: Arb<RichTextFormat> = Arb.enum<RichTextFormat>()

    /**
     * Property 2a: When toggling a format ON, all AUTO_DESELECT_FORMATS[f] are removed,
     * all INCOMPATIBLE_FORMATS[f] are removed, and f is present in the result.
     *
     * **Validates: Requirements 1.3, 1.4**
     */
    "Property 2: toggling ON removes auto-deselect and incompatible formats, adds f" {
        checkAll(100, arbFormatSubset, arbFormat) { activeFormats, format ->
            // Ensure format is NOT in activeFormats so we test the "toggle ON" path
            val withoutFormat = activeFormats - format
            val result = RichTextFormat.toggleFormat(withoutFormat, format)

            // f must be present
            result.contains(format) shouldBe true

            // All AUTO_DESELECT_FORMATS[f] must be removed
            val autoDeselect = RichTextFormat.AUTO_DESELECT_FORMATS[format] ?: emptySet()
            for (ad in autoDeselect) {
                result shouldNotContain ad
            }

            // All INCOMPATIBLE_FORMATS[f] must be removed
            val incompatible = RichTextFormat.INCOMPATIBLE_FORMATS[format] ?: emptySet()
            for (inc in incompatible) {
                result shouldNotContain inc
            }
        }
    }

    /**
     * Property 2b: When toggling a format OFF (already active), f is removed from the result.
     *
     * **Validates: Requirements 1.3, 1.4**
     */
    "Property 2: toggling OFF removes f from the result" {
        checkAll(100, arbFormatSubset, arbFormat) { activeFormats, format ->
            // Ensure format IS in activeFormats so we test the "toggle OFF" path
            val withFormat = activeFormats + format
            val result = RichTextFormat.toggleFormat(withFormat, format)

            result.contains(format) shouldBe false
        }
    }

    /**
     * Property 2c: Toggle ON then toggle OFF returns a set without f.
     *
     * **Validates: Requirements 1.3, 1.4**
     */
    "Property 2: toggle ON then toggle OFF returns set without f" {
        checkAll(100, arbFormatSubset, arbFormat) { activeFormats, format ->
            // Start without format, toggle ON, then toggle OFF
            val withoutFormat = activeFormats - format
            val afterToggleOn = RichTextFormat.toggleFormat(withoutFormat, format)
            val afterToggleOff = RichTextFormat.toggleFormat(afterToggleOn, format)

            afterToggleOff.contains(format) shouldBe false
        }
    }
})

package com.cometchat.uikit.compose.presentation.messagecomposer

import com.cometchat.uikit.compose.presentation.messagecomposer.ui.StickerButtonVisualState
import com.cometchat.uikit.compose.presentation.messagecomposer.ui.resolveStickerVisualState
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll

/**
 * Property-based tests for [resolveStickerVisualState].
 *
 * Feature: v6-compact-composer, Property 3: Sticker button visual state is determined by keyboard visibility
 *
 * **Validates: Requirements 5.1, 5.2**
 */
class StickerButtonVisualStatePropertyTest : StringSpec({

    /**
     * Property 3: The sticker button visual state is fully determined by the
     * `isStickerKeyboardOpen` boolean. The mapping is bijective:
     *   - `true`  → ACTIVE  (filled icon + highlight tint)
     *   - `false` → INACTIVE (outline icon + secondary tint)
     *
     * **Validates: Requirements 5.1, 5.2**
     */
    "Property 3: isStickerKeyboardOpen=true yields ACTIVE, false yields INACTIVE" {
        checkAll(100, Arb.boolean()) { isStickerKeyboardOpen ->
            val result = resolveStickerVisualState(isStickerKeyboardOpen)

            if (isStickerKeyboardOpen) {
                result shouldBe StickerButtonVisualState.ACTIVE
            } else {
                result shouldBe StickerButtonVisualState.INACTIVE
            }
        }
    }

    /**
     * Property 3 (bijectivity): Each visual state maps back to exactly one boolean.
     * ACTIVE always comes from `true`, INACTIVE always comes from `false`.
     *
     * **Validates: Requirements 5.1, 5.2**
     */
    "Property 3: mapping is bijective — each state corresponds to exactly one input" {
        checkAll(100, Arb.boolean()) { isStickerKeyboardOpen ->
            val state = resolveStickerVisualState(isStickerKeyboardOpen)

            // Verify the reverse mapping is consistent
            when (state) {
                StickerButtonVisualState.ACTIVE -> isStickerKeyboardOpen shouldBe true
                StickerButtonVisualState.INACTIVE -> isStickerKeyboardOpen shouldBe false
            }
        }
    }

    /**
     * Property 3 (determinism): Calling the function twice with the same input
     * always produces the same output.
     *
     * **Validates: Requirements 5.1, 5.2**
     */
    "Property 3: resolveStickerVisualState is deterministic" {
        checkAll(100, Arb.boolean()) { isStickerKeyboardOpen ->
            val first = resolveStickerVisualState(isStickerKeyboardOpen)
            val second = resolveStickerVisualState(isStickerKeyboardOpen)

            first shouldBe second
        }
    }
})

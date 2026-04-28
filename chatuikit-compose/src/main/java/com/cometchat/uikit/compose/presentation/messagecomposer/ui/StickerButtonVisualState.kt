package com.cometchat.uikit.compose.presentation.messagecomposer.ui

/**
 * Represents the visual configuration of the sticker button.
 *
 * The sticker button has exactly two visual states, determined by whether
 * the sticker keyboard is open or closed. Each state maps to a unique
 * icon variant and tint color pair.
 */
enum class StickerButtonVisualState {
    /**
     * Sticker keyboard is open — filled icon with active/highlight tint.
     */
    ACTIVE,

    /**
     * Sticker keyboard is closed — outline icon with secondary tint.
     */
    INACTIVE
}

/**
 * Pure function that determines the sticker button visual state based on
 * whether the sticker keyboard is currently open.
 *
 * This is a bijective mapping: each boolean input maps to exactly one
 * [StickerButtonVisualState], and each state maps back to exactly one boolean.
 *
 * - `true`  → [StickerButtonVisualState.ACTIVE]  (filled icon + highlight tint)
 * - `false` → [StickerButtonVisualState.INACTIVE] (outline icon + secondary tint)
 *
 * @param isStickerKeyboardOpen whether the sticker keyboard is currently visible
 * @return the visual state that should be applied to the sticker button
 */
fun resolveStickerVisualState(isStickerKeyboardOpen: Boolean): StickerButtonVisualState {
    return if (isStickerKeyboardOpen) StickerButtonVisualState.ACTIVE else StickerButtonVisualState.INACTIVE
}

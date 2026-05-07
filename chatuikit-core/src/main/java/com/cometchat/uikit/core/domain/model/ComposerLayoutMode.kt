package com.cometchat.uikit.core.domain.model

/**
 * Defines the layout mode for the CometChatMessageComposer.
 *
 * Controls how the composer arranges its text input and action buttons:
 * - [SINGLE_LINE]: Default single-row layout with text input and buttons in the same row.
 * - [MULTI_LINE]: Two-row layout with full-width text input in Row 1 and
 *   action buttons (attachment, mic, sticker, Aa toggle, send) in Row 2.
 *   In this mode, clicking the Aa formatting toggle replaces Row 2 with
 *   a rich text formatting toolbar and a close button.
 *
 * This enum is shared between `chatuikit-compose` and `chatuikit-kotlin` modules
 * to ensure consistent behavior across both UI frameworks.
 */
enum class ComposerLayoutMode {
    /**
     * Default single-row layout.
     * Text input and action buttons share the same row.
     */
    SINGLE_LINE,

    /**
     * Two-row multiline layout.
     * - Row 1: Full-width text input
     * - Row 2: Action buttons + Aa formatting toggle + send button
     *
     * When the Aa toggle is clicked, Row 2 switches to show the
     * rich text formatting toolbar with a close (X) button.
     */
    MULTI_LINE
}

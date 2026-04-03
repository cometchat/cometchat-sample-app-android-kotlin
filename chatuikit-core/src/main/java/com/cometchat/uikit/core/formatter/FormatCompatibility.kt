package com.cometchat.uikit.core.formatter

/**
 * Defines compatibility rules between rich text formats.
 *
 * Rules:
 * - Code block is incompatible with ALL other formats
 * - Bullet list and ordered list are mutually exclusive
 * - All other inline formats are mutually compatible
 * - Link is incompatible with inline code
 */
object FormatCompatibility {

    private val incompatiblePairs: Set<Pair<RichTextFormat, RichTextFormat>> = setOf(
        // Code block incompatible with everything
        RichTextFormat.CODE_BLOCK to RichTextFormat.BOLD,
        RichTextFormat.CODE_BLOCK to RichTextFormat.ITALIC,
        RichTextFormat.CODE_BLOCK to RichTextFormat.UNDERLINE,
        RichTextFormat.CODE_BLOCK to RichTextFormat.STRIKETHROUGH,
        RichTextFormat.CODE_BLOCK to RichTextFormat.INLINE_CODE,
        RichTextFormat.CODE_BLOCK to RichTextFormat.LINK,
        RichTextFormat.CODE_BLOCK to RichTextFormat.BULLET_LIST,
        RichTextFormat.CODE_BLOCK to RichTextFormat.ORDERED_LIST,
        RichTextFormat.CODE_BLOCK to RichTextFormat.BLOCKQUOTE,
        // Bullet and ordered lists are mutually exclusive
        RichTextFormat.BULLET_LIST to RichTextFormat.ORDERED_LIST,
        // Link incompatible with inline code
        RichTextFormat.LINK to RichTextFormat.INLINE_CODE,
    )

    /**
     * Returns the set of formats that should be disabled given the currently active formats.
     */
    fun getDisabledFormats(activeFormats: Set<RichTextFormat>): Set<RichTextFormat> {
        val disabled = mutableSetOf<RichTextFormat>()
        for (active in activeFormats) {
            for ((a, b) in incompatiblePairs) {
                if (active == a) disabled.add(b)
                if (active == b) disabled.add(a)
            }
        }
        return disabled
    }

    /**
     * Returns true if [format] is compatible with all [activeFormats].
     */
    fun isCompatible(format: RichTextFormat, activeFormats: Set<RichTextFormat>): Boolean {
        return format !in getDisabledFormats(activeFormats)
    }
}

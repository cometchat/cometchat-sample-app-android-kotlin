package com.cometchat.uikit.core.formatter

/**
 * Enum representing the different rich text formatting types.
 * Each format type corresponds to a specific markdown syntax.
 */
enum class RichTextFormat {
    /**
     * Bold text formatting using **text** syntax.
     */
    BOLD,
    
    /**
     * Italic text formatting using _text_ syntax.
     */
    ITALIC,
    
    /**
     * Strikethrough text formatting using ~~text~~ syntax.
     */
    STRIKETHROUGH,
    
    /**
     * Inline code formatting using `text` syntax.
     */
    INLINE_CODE,
    
    /**
     * Code block formatting using ```text``` syntax.
     */
    CODE_BLOCK,
    
    /**
     * Link formatting using [text](url) syntax.
     */
    LINK,
    
    /**
     * Bullet list formatting using - item syntax.
     */
    BULLET_LIST,
    
    /**
     * Ordered list formatting using 1. item syntax.
     */
    ORDERED_LIST,
    
    /**
     * Blockquote formatting using > text syntax.
     */
    BLOCKQUOTE,

    /**
     * Underline text formatting using &lt;u&gt;text&lt;/u&gt; syntax.
     */
    UNDERLINE;

    companion object {
        /**
         * Maps each format to the set of formats it DISABLES (grays out) when active.
         * Ported from v5 CometChatRichTextToolbar.INCOMPATIBLE_FORMATS.
         */
        val INCOMPATIBLE_FORMATS: Map<RichTextFormat, Set<RichTextFormat>> = mapOf(
            BOLD to emptySet(),
            ITALIC to emptySet(),
            UNDERLINE to emptySet(),
            STRIKETHROUGH to emptySet(),
            INLINE_CODE to emptySet(),
            LINK to emptySet(),
            BLOCKQUOTE to emptySet(),
            BULLET_LIST to emptySet(),
            ORDERED_LIST to emptySet(),
            CODE_BLOCK to setOf(BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, INLINE_CODE, LINK)
        )

        /**
         * Maps each format to the set of formats that are AUTO-DESELECTED when it is activated.
         * Ported from v5 CometChatRichTextToolbar.AUTO_DESELECT_FORMATS.
         */
        val AUTO_DESELECT_FORMATS: Map<RichTextFormat, Set<RichTextFormat>> = mapOf(
            BULLET_LIST to setOf(CODE_BLOCK, ORDERED_LIST),
            ORDERED_LIST to setOf(CODE_BLOCK, BULLET_LIST),
            BLOCKQUOTE to setOf(CODE_BLOCK),
            CODE_BLOCK to setOf(
                BULLET_LIST, ORDERED_LIST, BLOCKQUOTE,
                BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, INLINE_CODE, LINK
            )
        )

        /**
         * Pure function: given a set of active formats, returns the set of formats
         * that should be disabled (grayed out, non-clickable).
         */
        fun computeDisabledFormats(activeFormats: Set<RichTextFormat>): Set<RichTextFormat> {
            val disabled = mutableSetOf<RichTextFormat>()
            for (active in activeFormats) {
                INCOMPATIBLE_FORMATS[active]?.let { disabled.addAll(it) }
            }
            return disabled
        }

        /**
         * Pure function: given the current active formats and a format being toggled,
         * returns the new active formats set after applying auto-deselect rules.
         */
        fun toggleFormat(
            activeFormats: Set<RichTextFormat>,
            format: RichTextFormat
        ): Set<RichTextFormat> {
            val result = activeFormats.toMutableSet()
            if (format in result) {
                result.remove(format)
            } else {
                // Auto-deselect conflicting formats
                AUTO_DESELECT_FORMATS[format]?.let { result.removeAll(it) }
                // Also remove any formats that would become incompatible
                INCOMPATIBLE_FORMATS[format]?.let { result.removeAll(it) }
                result.add(format)
            }
            return result
        }
    }
}

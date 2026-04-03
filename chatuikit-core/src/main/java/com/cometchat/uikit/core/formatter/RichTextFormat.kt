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
    UNDERLINE
}

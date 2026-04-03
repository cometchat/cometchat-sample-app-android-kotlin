package com.cometchat.uikit.core.formatter

/**
 * Configuration class for rich text formatting options.
 * Controls which formatting options are enabled in the message composer.
 * 
 * By default, all formatters are disabled as per requirements.
 * Enable specific formatters as needed for your use case.
 * 
 * @param enableBold Enable bold formatting (**text**)
 * @param enableItalic Enable italic formatting (_text_)
 * @param enableUnderline Enable underline formatting (<u>text</u>)
 * @param enableStrikethrough Enable strikethrough formatting (~~text~~)
 * @param enableInlineCode Enable inline code formatting (`text`)
 * @param enableCodeBlock Enable code block formatting (```text```)
 * @param enableLink Enable link formatting ([text](url))
 * @param enableBulletList Enable bullet list formatting (- item)
 * @param enableOrderedList Enable ordered list formatting (1. item)
 * @param enableBlockquote Enable blockquote formatting (> text)
 */
data class RichTextConfiguration(
    val enableBold: Boolean = false,
    val enableItalic: Boolean = false,
    val enableUnderline: Boolean = false,
    val enableStrikethrough: Boolean = false,
    val enableInlineCode: Boolean = false,
    val enableCodeBlock: Boolean = false,
    val enableLink: Boolean = false,
    val enableBulletList: Boolean = false,
    val enableOrderedList: Boolean = false,
    val enableBlockquote: Boolean = false
) {
    /**
     * Returns true if any formatting option is enabled.
     */
    fun hasAnyEnabled(): Boolean {
        return enableBold || enableItalic || enableUnderline || enableStrikethrough ||
               enableInlineCode || enableCodeBlock || enableLink ||
               enableBulletList || enableOrderedList || enableBlockquote
    }
    
    /**
     * Returns a list of enabled format types.
     */
    fun getEnabledFormats(): List<RichTextFormat> {
        return buildList {
            if (enableBold) add(RichTextFormat.BOLD)
            if (enableItalic) add(RichTextFormat.ITALIC)
            if (enableUnderline) add(RichTextFormat.UNDERLINE)
            if (enableStrikethrough) add(RichTextFormat.STRIKETHROUGH)
            if (enableInlineCode) add(RichTextFormat.INLINE_CODE)
            if (enableCodeBlock) add(RichTextFormat.CODE_BLOCK)
            if (enableLink) add(RichTextFormat.LINK)
            if (enableBulletList) add(RichTextFormat.BULLET_LIST)
            if (enableOrderedList) add(RichTextFormat.ORDERED_LIST)
            if (enableBlockquote) add(RichTextFormat.BLOCKQUOTE)
        }
    }
    
    companion object {
        /**
         * Creates a configuration with all formatters enabled.
         */
        fun allEnabled(): RichTextConfiguration = RichTextConfiguration(
            enableBold = true,
            enableItalic = true,
            enableUnderline = true,
            enableStrikethrough = true,
            enableInlineCode = true,
            enableCodeBlock = true,
            enableLink = true,
            enableBulletList = true,
            enableOrderedList = true,
            enableBlockquote = true
        )
        
        /**
         * Creates a configuration with basic formatters enabled (bold, italic, underline, strikethrough).
         */
        fun basicFormatting(): RichTextConfiguration = RichTextConfiguration(
            enableBold = true,
            enableItalic = true,
            enableUnderline = true,
            enableStrikethrough = true
        )
        
        /**
         * Creates a configuration with code formatters enabled (inline code, code block).
         */
        fun codeFormatting(): RichTextConfiguration = RichTextConfiguration(
            enableInlineCode = true,
            enableCodeBlock = true
        )
        
        /**
         * Creates a configuration with list formatters enabled (bullet, ordered).
         */
        fun listFormatting(): RichTextConfiguration = RichTextConfiguration(
            enableBulletList = true,
            enableOrderedList = true
        )
    }
}

package com.cometchat.chatuikit.shared.views.richtexttoolbar;

/**
 * FormatType is an enumeration representing different markdown format types
 * for rich text formatting in the message composer.
 * <p>
 * Each format type has a prefix and suffix that are used to wrap text
 * with the appropriate markdown syntax.
 * </p>
 */
public enum FormatType {
    /**
     * Bold format using ** markers.
     * Example: **bold text**
     */
    BOLD("**", "**"),

    /**
     * Italic format using _ markers.
     * Example: _italic text_
     */
    ITALIC("_", "_"),

    /**
     * Strikethrough format using ~~ markers.
     * Example: ~~strikethrough text~~
     */
    STRIKETHROUGH("~~", "~~"),

    /**
     * Underline format using HTML-style &lt;u&gt; tags.
     * Example: &lt;u&gt;underlined text&lt;/u&gt;
     */
    UNDERLINE("<u>", "</u>"),

    /**
     * Inline code format using single backtick markers.
     * Example: `inline code`
     */
    INLINE_CODE("`", "`"),

    /**
     * Code block format using triple backticks on new lines.
     * Example:
     * ```
     * code block
     * ```
     */
    CODE_BLOCK("```\n", "\n```"),

    /**
     * Link format using markdown link syntax.
     * Example: [link text](url)
     */
    LINK("[", "](url)"),

    /**
     * Bullet list format using "- " prefix.
     * Example: - list item
     */
    BULLET_LIST("- ", ""),

    /**
     * Ordered list format using "1. " prefix.
     * Example: 1. list item
     */
    ORDERED_LIST("1. ", ""),

    /**
     * Blockquote format using "> " prefix.
     * Example: > quoted text
     */
    BLOCKQUOTE("> ", "");

    private final String prefix;
    private final String suffix;

    /**
     * Constructor for FormatType enum.
     *
     * @param prefix The prefix marker for the format.
     * @param suffix The suffix marker for the format.
     */
    FormatType(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /**
     * Returns the prefix marker for this format type.
     *
     * @return The prefix string.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the suffix marker for this format type.
     *
     * @return The suffix string.
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Wraps the given text with the format's prefix and suffix markers.
     *
     * @param text The text to wrap.
     * @return The wrapped text with format markers.
     */
    public String wrap(String text) {
        return prefix + text + suffix;
    }

    /**
     * Checks if the given text is wrapped with this format's markers.
     * <p>
     * For formats with empty suffix (like BULLET_LIST, ORDERED_LIST, BLOCKQUOTE),
     * only checks if the text starts with the prefix.
     * </p>
     *
     * @param text The text to check.
     * @return true if the text is wrapped with this format's markers, false otherwise.
     */
    public boolean isWrapped(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        if (suffix.isEmpty()) {
            return text.startsWith(prefix);
        }
        return text.startsWith(prefix) && text.endsWith(suffix) && text.length() >= prefix.length() + suffix.length();
    }

    /**
     * Removes the format markers from the given text if it is wrapped.
     * <p>
     * If the text is not wrapped with this format's markers, returns the original text.
     * </p>
     *
     * @param text The text to unwrap.
     * @return The unwrapped text without format markers, or the original text if not wrapped.
     */
    public String unwrap(String text) {
        if (!isWrapped(text)) {
            return text;
        }
        if (suffix.isEmpty()) {
            return text.substring(prefix.length());
        }
        return text.substring(prefix.length(), text.length() - suffix.length());
    }
}

package com.cometchat.chatuikit.shared.formatters.richtext;

/**
 * FormattedResult represents the result of a text formatting operation.
 * <p>
 * This class encapsulates the formatted text along with the new cursor positions
 * after the formatting has been applied. It is used by {@link RichTextFormatter}
 * implementations to return the result of format operations.
 * </p>
 * <p>
 * The cursor positions are important for maintaining a good user experience,
 * as they allow the cursor to be placed logically after formatting is applied.
 * </p>
 *
 * @see RichTextFormatter
 */
public class FormattedResult {

    /**
     * The formatted text after the formatting operation.
     */
    private final String text;

    /**
     * The new cursor start position after formatting.
     */
    private final int newCursorStart;

    /**
     * The new cursor end position after formatting.
     */
    private final int newCursorEnd;

    /**
     * Constructs a new FormattedResult with the specified text and cursor positions.
     *
     * @param text           The formatted text after the formatting operation.
     * @param newCursorStart The new cursor start position after formatting.
     * @param newCursorEnd   The new cursor end position after formatting.
     */
    public FormattedResult(String text, int newCursorStart, int newCursorEnd) {
        this.text = text;
        this.newCursorStart = newCursorStart;
        this.newCursorEnd = newCursorEnd;
    }

    /**
     * Returns the formatted text.
     *
     * @return The formatted text string.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the new cursor start position after formatting.
     * <p>
     * This position indicates where the cursor selection should start
     * after the formatting operation has been applied.
     * </p>
     *
     * @return The new cursor start position.
     */
    public int getNewCursorStart() {
        return newCursorStart;
    }

    /**
     * Returns the new cursor end position after formatting.
     * <p>
     * This position indicates where the cursor selection should end
     * after the formatting operation has been applied. If equal to
     * {@link #getNewCursorStart()}, the cursor is a simple caret with no selection.
     * </p>
     *
     * @return The new cursor end position.
     */
    public int getNewCursorEnd() {
        return newCursorEnd;
    }
}

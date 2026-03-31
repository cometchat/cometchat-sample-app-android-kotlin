package com.cometchat.chatuikit.shared.spans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.style.LeadingMarginSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

/**
 * Span for numbered list items in WYSIWYG rich text editing.
 * <p>
 * This span extends {@link LeadingMarginSpan.Standard} to provide visual numbered
 * list rendering with appropriate indentation. It implements {@link RichTextFormatSpan}
 * to enable format detection and markdown conversion.
 * </p>
 * <p>
 * When applied to text, this span displays each line with:
 * <ul>
 *   <li>A sequential number prefix (e.g., "1.", "2.", "3.")</li>
 *   <li>Appropriate indentation from the left margin</li>
 *   <li>Configurable text color and gap width</li>
 * </ul>
 * No markdown syntax markers (like "1. ") are shown as raw text. The markdown markers
 * are only generated when converting to markdown for sending messages.
 * </p>
 * <p>
 * Validates: Requirements 7.1
 * </p>
 *
 * @see RichTextFormatSpan
 * @see FormatType#ORDERED_LIST
 */
public class NumberedListFormatSpan extends LeadingMarginSpan.Standard implements RichTextFormatSpan {

    /**
     * Default gap width between number and text (in pixels).
     */
    private static final int DEFAULT_GAP_WIDTH = 16;

    /**
     * Default leading margin for single-digit numbers (total indentation in pixels).
     */
    private static final int DEFAULT_LEADING_MARGIN = 48;

    /**
     * Additional margin per extra digit beyond the first (in pixels).
     */
    private static final int MARGIN_PER_EXTRA_DIGIT = 16;

    /**
     * The item number for this list item.
     */
    private int number;

    /**
     * Color of the number text.
     */
    @ColorInt
    private int textColor;

    /**
     * Gap width between the number and the text.
     */
    private int gapWidth;

    /**
     * Context for accessing theme colors. May be null if colors are set directly.
     */
    @Nullable
    private final Context context;

    /**
     * Creates a new NumberedListFormatSpan with the specified item number.
     * <p>
     * Uses default values for gap width. Leading margin is calculated based on
     * the number of digits to ensure proper spacing for double-digit numbers.
     * Text color will be set to 0 (transparent) until explicitly set or a context is provided.
     * </p>
     *
     * @param number The item number for this list item (e.g., 1, 2, 3).
     */
    public NumberedListFormatSpan(int number) {
        super(calculateLeadingMargin(number));
        this.number = number;
        this.textColor = 0;
        this.gapWidth = DEFAULT_GAP_WIDTH;
        this.context = null;
    }

    /**
     * Creates a new NumberedListFormatSpan with context for theme colors.
     * <p>
     * Uses CometChatTheme to obtain appropriate colors for the current theme.
     * Leading margin is calculated based on the number of digits.
     * </p>
     *
     * @param number  The item number for this list item.
     * @param context The context used to access theme colors.
     */
    public NumberedListFormatSpan(int number, @NonNull Context context) {
        super(calculateLeadingMargin(number));
        this.number = number;
        this.context = context;
        this.textColor = CometChatTheme.getTextColorPrimary(context);
        this.gapWidth = DEFAULT_GAP_WIDTH;
    }

    /**
     * Creates a new NumberedListFormatSpan with custom styling.
     * <p>
     * Leading margin is calculated based on the number of digits.
     * </p>
     *
     * @param number    The item number for this list item.
     * @param textColor The color of the number text.
     * @param gapWidth  The gap width between the number and the text.
     */
    public NumberedListFormatSpan(int number, @ColorInt int textColor, int gapWidth) {
        super(calculateLeadingMargin(number));
        this.number = number;
        this.textColor = textColor;
        this.gapWidth = gapWidth;
        this.context = null;
    }

    /**
     * Creates a new NumberedListFormatSpan with custom leading margin.
     *
     * @param number        The item number for this list item.
     * @param leadingMargin The total leading margin (indentation) in pixels.
     */
    public NumberedListFormatSpan(int number, int leadingMargin) {
        super(leadingMargin);
        this.number = number;
        this.textColor = 0;
        this.gapWidth = DEFAULT_GAP_WIDTH;
        this.context = null;
    }

    /**
     * Returns the format type associated with this span.
     *
     * @return {@link FormatType#ORDERED_LIST} indicating this is a numbered list format span.
     */
    @Override
    public FormatType getFormatType() {
        return FormatType.ORDERED_LIST;
    }

    /**
     * Draws the leading margin (number prefix) for a line of text.
     * <p>
     * This method is called for each line of text that this span covers. It draws
     * the item number (e.g., "1.", "2.") at the appropriate position in the leading margin.
     * The number is only drawn for the first line of a paragraph (when {@code first} is true).
     * </p>
     *
     * @param canvas   The Canvas to draw on.
     * @param paint    The Paint object used for drawing.
     * @param x        The current position of the margin.
     * @param dir      The direction of the paragraph (1 for LTR, -1 for RTL).
     * @param top      The top of the line.
     * @param baseline The baseline of the text.
     * @param bottom   The bottom of the line.
     * @param text     The text being rendered.
     * @param start    The start index of the line in the text.
     * @param end      The end index of the line in the text.
     * @param first    Whether this is the first line of the paragraph.
     * @param layout   The Layout object (may be null).
     */
    @Override
    public void drawLeadingMargin(@NonNull Canvas canvas, @NonNull Paint paint,
                                   int x, int dir, int top, int baseline, int bottom,
                                   @NonNull CharSequence text, int start, int end,
                                   boolean first, @Nullable Layout layout) {
        // Only draw number for the first line of the paragraph
        if (first) {
            // Save original paint properties
            Paint.Style originalStyle = paint.getStyle();
            int originalColor = paint.getColor();
            Typeface originalTypeface = paint.getTypeface();

            // Configure paint for number drawing
            paint.setStyle(Paint.Style.FILL);
            int effectiveColor = getEffectiveTextColor();
            if (effectiveColor != 0) {
                paint.setColor(effectiveColor);
            }

            // Build the number string (e.g., "1.", "2.", "3.")
            String numberText = number + ".";

            // Calculate horizontal position based on direction
            float drawX;
            if (dir > 0) {
                // LTR: position number in the leading margin
                drawX = x + (gapWidth / 4f);
            } else {
                // RTL: position number on the right side
                float textWidth = paint.measureText(numberText);
                drawX = x - textWidth - (gapWidth / 4f);
            }

            // Draw the number text at the baseline
            canvas.drawText(numberText, drawX, baseline, paint);

            // Restore original paint properties
            paint.setStyle(originalStyle);
            paint.setColor(originalColor);
            paint.setTypeface(originalTypeface);
        }
    }

    /**
     * Gets the effective text color, using theme color if available.
     *
     * @return The text color to use.
     */
    @ColorInt
    private int getEffectiveTextColor() {
        if (textColor != 0) {
            return textColor;
        }
        if (context != null) {
            return CometChatTheme.getTextColorPrimary(context);
        }
        return 0;
    }

    // ==================== Getters and Setters ====================

    /**
     * Gets the item number.
     *
     * @return The item number for this list item.
     */
    public int getNumber() {
        return number;
    }

    /**
     * Sets the item number.
     *
     * @param number The item number to set.
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Gets the text color.
     *
     * @return The text color.
     */
    @ColorInt
    public int getTextColor() {
        return textColor;
    }

    /**
     * Sets the text color.
     *
     * @param textColor The text color to set.
     */
    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
    }

    /**
     * Gets the gap width.
     *
     * @return The gap width in pixels.
     */
    public int getGapWidth() {
        return gapWidth;
    }

    /**
     * Sets the gap width.
     *
     * @param gapWidth The gap width to set in pixels.
     */
    public void setGapWidth(int gapWidth) {
        this.gapWidth = gapWidth;
    }

    // ==================== Static Helper Methods ====================

    /**
     * Calculates the leading margin based on the number of digits.
     * <p>
     * Single-digit numbers (1-9) use the default margin.
     * Double-digit numbers (10-99) get additional margin.
     * Triple-digit numbers (100-999) get even more margin, etc.
     * </p>
     *
     * @param number The item number.
     * @return The calculated leading margin in pixels.
     */
    private static int calculateLeadingMargin(int number) {
        int digits = String.valueOf(Math.abs(number)).length();
        // For single digit, use default. For each additional digit, add extra margin.
        int extraDigits = Math.max(0, digits - 1);
        return DEFAULT_LEADING_MARGIN + (extraDigits * MARGIN_PER_EXTRA_DIGIT);
    }
}

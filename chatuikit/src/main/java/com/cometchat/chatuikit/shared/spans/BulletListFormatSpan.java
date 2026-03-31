package com.cometchat.chatuikit.shared.spans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

/**
 * Span for bullet list items in WYSIWYG rich text editing.
 * <p>
 * This span extends {@link LeadingMarginSpan.Standard} to provide visual bullet point
 * rendering with appropriate indentation. It implements {@link RichTextFormatSpan}
 * to enable format detection and markdown conversion.
 * </p>
 * <p>
 * When applied to text, this span displays each line with:
 * <ul>
 *   <li>A visual bullet point prefix (filled circle)</li>
 *   <li>Appropriate indentation from the left margin</li>
 *   <li>Configurable bullet color, radius, and gap width</li>
 * </ul>
 * No markdown syntax markers (like "- ") are shown. The markdown markers are only
 * generated when converting to markdown for sending messages.
 * </p>
 * <p>
 * Validates: Requirements 6.1, 6.5
 * </p>
 *
 * @see RichTextFormatSpan
 * @see FormatType#BULLET_LIST
 */
public class BulletListFormatSpan extends LeadingMarginSpan.Standard implements RichTextFormatSpan {

    private static final String TAG = "BulletListFormatSpan";

    /**
     * Default bullet radius (in pixels).
     */
    private static final int DEFAULT_BULLET_RADIUS = 4;

    /**
     * Default gap width between bullet and text (in pixels).
     */
    private static final int DEFAULT_GAP_WIDTH = 16;

    /**
     * Default leading margin (total indentation in pixels).
     * Matches {@code NumberedListFormatSpan.DEFAULT_LEADING_MARGIN} so that
     * bullet and numbered list text content aligns at the same horizontal position.
     */
    private static final int DEFAULT_LEADING_MARGIN = 48;

    /**
     * Color of the bullet point.
     */
    @ColorInt
    private int bulletColor;

    /**
     * Radius of the bullet point circle.
     */
    private int bulletRadius;

    /**
     * Gap width between the bullet and the text.
     */
    private int gapWidth;

    /**
     * Context for accessing theme colors. May be null if colors are set directly.
     */
    @Nullable
    private final Context context;

    /**
     * Creates a new BulletListFormatSpan with default styling.
     * <p>
     * Uses default values for bullet radius, gap width, and leading margin.
     * Bullet color will be set to 0 (transparent) until explicitly set or a context is provided.
     * </p>
     */
    public BulletListFormatSpan() {
        super(DEFAULT_LEADING_MARGIN);
        this.bulletColor = 0;
        this.bulletRadius = DEFAULT_BULLET_RADIUS;
        this.gapWidth = DEFAULT_GAP_WIDTH;
        this.context = null;
    }

    /**
     * Creates a new BulletListFormatSpan with context for theme colors.
     * <p>
     * Uses CometChatTheme to obtain appropriate colors for the current theme.
     * </p>
     *
     * @param context The context used to access theme colors.
     */
    public BulletListFormatSpan(@NonNull Context context) {
        super(DEFAULT_LEADING_MARGIN);
        this.context = context;
        this.bulletColor = CometChatTheme.getTextColorPrimary(context);
        this.bulletRadius = DEFAULT_BULLET_RADIUS;
        this.gapWidth = DEFAULT_GAP_WIDTH;
    }

    /**
     * Creates a new BulletListFormatSpan with custom styling.
     *
     * @param bulletColor   The color of the bullet point.
     * @param bulletRadius  The radius of the bullet point circle.
     * @param gapWidth      The gap width between the bullet and the text.
     */
    public BulletListFormatSpan(@ColorInt int bulletColor, int bulletRadius, int gapWidth) {
        super(bulletRadius * 2 + gapWidth);
        this.bulletColor = bulletColor;
        this.bulletRadius = bulletRadius;
        this.gapWidth = gapWidth;
        this.context = null;
    }

    /**
     * Creates a new BulletListFormatSpan with custom leading margin.
     *
     * @param leadingMargin The total leading margin (indentation) in pixels.
     */
    public BulletListFormatSpan(int leadingMargin) {
        super(leadingMargin);
        this.bulletColor = 0;
        this.bulletRadius = DEFAULT_BULLET_RADIUS;
        this.gapWidth = DEFAULT_GAP_WIDTH;
        this.context = null;
    }

    /**
     * Returns the format type associated with this span.
     *
     * @return {@link FormatType#BULLET_LIST} indicating this is a bullet list format span.
     */
    @Override
    public FormatType getFormatType() {
        return FormatType.BULLET_LIST;
    }

    /**
     * Draws the leading margin (bullet point) for a line of text.
     * <p>
     * This method is called for each line of text that this span covers. It draws
     * a filled circle (bullet point) at the appropriate position in the leading margin.
     * The bullet is only drawn for the first line of a paragraph (when {@code first} is true).
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
        // Only draw bullet for the first line of the paragraph
        if (first) {
            // Save original paint properties
            Paint.Style originalStyle = paint.getStyle();
            int originalColor = paint.getColor();

            // Configure paint for bullet drawing
            paint.setStyle(Paint.Style.FILL);
            int effectiveColor = getEffectiveBulletColor();
            if (effectiveColor != 0) {
                paint.setColor(effectiveColor);
            }

            // Calculate bullet position
            // The bullet should be centered vertically between top and bottom
            float bulletCenterY = (top + bottom) / 2f;

            // Calculate horizontal position based on direction.
            // The bullet is placed at the same horizontal offset where the
            // number text center would be in NumberedListFormatSpan, so both
            // list marker types are visually aligned.
            float bulletCenterX;
            if (dir > 0) {
                bulletCenterX = x + gapWidth;
            } else {
                bulletCenterX = x - gapWidth;
            }

            // Draw the bullet point as a filled circle
            canvas.drawCircle(bulletCenterX, bulletCenterY, bulletRadius, paint);

            // Restore original paint properties
            paint.setStyle(originalStyle);
            paint.setColor(originalColor);
        }
    }

    /**
     * Gets the effective bullet color, using theme color if available.
     *
     * @return The bullet color to use.
     */
    @ColorInt
    private int getEffectiveBulletColor() {
        if (bulletColor != 0) {
            return bulletColor;
        }
        if (context != null) {
            return CometChatTheme.getTextColorPrimary(context);
        }
        return 0;
    }

    // ==================== Getters and Setters ====================

    /**
     * Gets the bullet color.
     *
     * @return The bullet color.
     */
    @ColorInt
    public int getBulletColor() {
        return bulletColor;
    }

    /**
     * Sets the bullet color.
     *
     * @param bulletColor The bullet color to set.
     */
    public void setBulletColor(@ColorInt int bulletColor) {
        this.bulletColor = bulletColor;
    }

    /**
     * Gets the bullet radius.
     *
     * @return The bullet radius in pixels.
     */
    public int getBulletRadius() {
        return bulletRadius;
    }

    /**
     * Sets the bullet radius.
     *
     * @param bulletRadius The bullet radius to set in pixels.
     */
    public void setBulletRadius(int bulletRadius) {
        this.bulletRadius = bulletRadius;
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
}

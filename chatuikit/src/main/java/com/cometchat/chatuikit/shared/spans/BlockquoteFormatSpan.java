package com.cometchat.chatuikit.shared.spans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

/**
 * Span for blockquote formatting in WYSIWYG rich text editing.
 * <p>
 * This span extends {@link LeadingMarginSpan.Standard} to indent text and
 * implements {@link LineBackgroundSpan} to draw the left stripe indicator
 * and an optional background fill.
 * </p>
 * <p>
 * Drawing the stripe via {@code LineBackgroundSpan.drawBackground} instead of
 * {@code LeadingMarginSpan.drawLeadingMargin} ensures the stripe is always
 * positioned at the absolute left edge of the text area, even when other
 * {@link LeadingMarginSpan}s (e.g., code block padding) shift the {@code x}
 * parameter passed to {@code drawLeadingMargin}.
 * </p>
 * <p>
 * Theme-aware colors vary by context:
 * <ul>
 *   <li>Composer: stroke dark for the stripe, no background</li>
 *   <li>Received bubble: stroke highlight for the stripe, background 3 fill</li>
 *   <li>Sender bubble: white stripe, white at 20% opacity fill</li>
 * </ul>
 * </p>
 * <p>
 * Validates: Requirements 8.1, 8.3
 * </p>
 */
public class BlockquoteFormatSpan extends LeadingMarginSpan.Standard
        implements RichTextFormatSpan, LineBackgroundSpan {

    private static final int DEFAULT_STRIPE_WIDTH = 7;
    private static final int DEFAULT_GAP_WIDTH = 16;
    private static final int DEFAULT_LEADING_MARGIN = 32;

    /** White at 20% opacity — sender bubble background fill. */
    @ColorInt
    private static final int SENDER_BACKGROUND_COLOR = 0x33FFFFFF;

    /**
     * Default fallback stripe color used when no context is available.
     * A medium-gray that provides reasonable contrast on both light and dark backgrounds.
     */
    @ColorInt
    private static final int DEFAULT_STRIPE_COLOR = 0xFF888888;

    /** Fallback corner radius in pixels when context is unavailable. */
    private static final float DEFAULT_CORNER_RADIUS = 8f;

    /** Vertical padding above the first line and below the last line. */
    private static final float VERTICAL_PADDING = 8f;

    @ColorInt private int stripeColor;
    @ColorInt private int backgroundColor;
    private int stripeWidth;
    private int gapWidth;

    @Nullable
    private final Context context;

    private final android.graphics.RectF rectF = new android.graphics.RectF();
    private final android.graphics.Path path = new android.graphics.Path();
    private final float[] radii = new float[8];

    // ==================== Constructors ====================

    /** Fallback constructor with no theme awareness. */
    public BlockquoteFormatSpan() {
        super(DEFAULT_LEADING_MARGIN);
        this.context = null;
        this.stripeColor = DEFAULT_STRIPE_COLOR;
        this.backgroundColor = 0;
        this.stripeWidth = DEFAULT_STRIPE_WIDTH;
        this.gapWidth = DEFAULT_GAP_WIDTH;
    }

    /**
     * Composer constructor — uses stroke dark for the stripe, no background.
     *
     * @param context The context for resolving theme colors.
     */
    public BlockquoteFormatSpan(@NonNull Context context) {
        super(DEFAULT_LEADING_MARGIN);
        this.context = context;
        this.stripeColor = CometChatTheme.getStrokeColorDark(context);
        this.backgroundColor = 0;
        this.stripeWidth = DEFAULT_STRIPE_WIDTH;
        this.gapWidth = DEFAULT_GAP_WIDTH;
    }

    /**
     * Message-bubble constructor — colors differ for sender vs receiver.
     * <ul>
     *   <li>Sender: white stripe, white at 20% opacity background</li>
     *   <li>Receiver: stroke highlight stripe, background 3 fill</li>
     * </ul>
     *
     * @param context        The context for resolving theme colors.
     * @param isSenderBubble True for the sent (right) bubble, false for received (left).
     */
    public BlockquoteFormatSpan(@NonNull Context context, boolean isSenderBubble) {
        super(DEFAULT_LEADING_MARGIN);
        this.context = context;
        this.stripeWidth = DEFAULT_STRIPE_WIDTH;
        this.gapWidth = DEFAULT_GAP_WIDTH;
        if (isSenderBubble) {
            this.stripeColor = CometChatTheme.getColorWhite(context);
            this.backgroundColor = SENDER_BACKGROUND_COLOR;
        } else {
            this.stripeColor = CometChatTheme.getStrokeColorHighlight(context);
            this.backgroundColor = CometChatTheme.getBackgroundColor3(context);
        }
    }

    public BlockquoteFormatSpan(@ColorInt int stripeColor, int stripeWidth, int gapWidth) {
        super(stripeWidth + gapWidth);
        this.context = null;
        this.stripeColor = stripeColor;
        this.backgroundColor = 0;
        this.stripeWidth = stripeWidth;
        this.gapWidth = gapWidth;
    }

    public BlockquoteFormatSpan(int leadingMargin) {
        super(leadingMargin);
        this.context = null;
        this.stripeColor = DEFAULT_STRIPE_COLOR;
        this.backgroundColor = 0;
        this.stripeWidth = DEFAULT_STRIPE_WIDTH;
        this.gapWidth = DEFAULT_GAP_WIDTH;
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.BLOCKQUOTE;
    }

    // ==================== LeadingMarginSpan ====================

    /**
     * Provides text indentation but does NOT draw the stripe.
     * <p>
     * The stripe is drawn exclusively in {@link #drawBackground} so that its
     * horizontal position is always the absolute {@code left} of the text area,
     * unaffected by other {@link LeadingMarginSpan}s on the same line.
     * </p>
     */
    @Override
    public void drawLeadingMargin(@NonNull Canvas canvas, @NonNull Paint paint,
                                   int x, int dir, int top, int baseline, int bottom,
                                   @NonNull CharSequence text, int start, int end,
                                   boolean first, @Nullable Layout layout) {
        // Intentionally empty — stripe drawing is handled by drawBackground().
        // The superclass (LeadingMarginSpan.Standard) still provides the margin
        // indentation via getLeadingMargin().
    }

    // ==================== LineBackgroundSpan ====================

    /**
     * Draws the blockquote background fill (with uniform rounded corners)
     * and left stripe for each line covered by this span.
     * <p>
     * The background is drawn with rounded corners on all four sides. The
     * stripe is drawn flush against the left edge of the background, clipped
     * to the rounded shape. On the first line the background extends upward
     * by {@link #VERTICAL_PADDING}, and on the last line it extends downward
     * by the same amount.
     * </p>
     * <p>
     * When the span ends with a newline, both the background and stripe are
     * extended downward to cover the empty trailing line, providing immediate
     * visual feedback when the user presses Enter.
     * </p>
     */
    @Override
    public void drawBackground(@NonNull Canvas canvas, @NonNull Paint paint,
                                int left, int right, int top, int baseline, int bottom,
                                @NonNull CharSequence text, int start, int end, int lineNumber) {
        if (!(text instanceof Spanned)) {
            return;
        }

        Spanned spanned = (Spanned) text;
        int spanStart = spanned.getSpanStart(this);
        int spanEnd = spanned.getSpanEnd(this);

        // Skip lines outside this span's range
        if (end <= spanStart || start >= spanEnd) {
            return;
        }

        Paint.Style originalStyle = paint.getStyle();
        int originalColor = paint.getColor();
        boolean originalAntiAlias = paint.isAntiAlias();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        boolean isFirstLine = (start <= spanStart);
        boolean isLastDrawnLine = (spanEnd > start && spanEnd <= end) || end >= spanEnd;
        boolean hasTrailingNewline = isLastDrawnLine && spanEnd > 0
                && spanEnd <= text.length() && text.charAt(spanEnd - 1) == '\n';

        // Extend top/bottom for padding on first/last lines
        float drawTop = isFirstLine ? top - VERTICAL_PADDING : top;
        float drawBottom;
        if (hasTrailingNewline) {
            int lineHeight = bottom - top;
            drawBottom = bottom + lineHeight + (isLastDrawnLine ? VERTICAL_PADDING : 0);
        } else {
            drawBottom = isLastDrawnLine ? bottom + VERTICAL_PADDING : bottom;
        }

        float cornerRadius = resolveCornerRadius();

        // Determine per-corner radii: rounded only on first/last lines
        float topR = isFirstLine ? cornerRadius : 0;
        float bottomR = (hasTrailingNewline || isLastDrawnLine) ? cornerRadius : 0;

        // Draw background fill with uniform rounded corners
        if (backgroundColor != 0) {
            paint.setColor(backgroundColor);
            rectF.set(left, drawTop, right, drawBottom);
            drawRoundRectWithCorners(canvas, rectF, paint, topR, topR, bottomR, bottomR);
        }

        // Draw the stripe
        int effectiveColor = getEffectiveStripeColor();
        if (effectiveColor != 0) {
            paint.setColor(effectiveColor);
            float stripeLeft = left;
            float stripeRight = stripeLeft + stripeWidth;

            if (backgroundColor != 0) {
                // When a background is present, clip the stripe to the
                // rounded-rect so it respects the corner rounding.
                canvas.save();
                rectF.set(left, drawTop, right, drawBottom);
                path.reset();
                radii[0] = topR; radii[1] = topR;
                radii[2] = topR; radii[3] = topR;
                radii[4] = bottomR; radii[5] = bottomR;
                radii[6] = bottomR; radii[7] = bottomR;
                path.addRoundRect(rectF, radii, android.graphics.Path.Direction.CW);
                canvas.clipPath(path);
                canvas.drawRect(stripeLeft, drawTop, stripeRight, drawBottom, paint);
                canvas.restore();
            } else {
                // No background (e.g. composer) — draw a plain rectangle
                // so the stripe isn't clipped by corner rounding.
                canvas.drawRect(stripeLeft, drawTop, stripeRight, drawBottom, paint);
            }
        }

        paint.setStyle(originalStyle);
        paint.setColor(originalColor);
        paint.setAntiAlias(originalAntiAlias);
    }

    // ==================== Drawing Helpers ====================

    /**
     * Draws a rounded rectangle with individually specified corner radii.
     */
    private void drawRoundRectWithCorners(@NonNull Canvas canvas, @NonNull android.graphics.RectF rect,
                                           @NonNull Paint paint,
                                           float topLeft, float topRight,
                                           float bottomRight, float bottomLeft) {
        path.reset();
        radii[0] = topLeft;  radii[1] = topLeft;
        radii[2] = topRight; radii[3] = topRight;
        radii[4] = bottomRight; radii[5] = bottomRight;
        radii[6] = bottomLeft;  radii[7] = bottomLeft;
        path.addRoundRect(rect, radii, android.graphics.Path.Direction.CW);
        canvas.drawPath(path, paint);
    }

    // ==================== Helpers ====================

    /**
     * Resolves the corner radius from the theme dimension {@code cometchat_radius_2}.
     * Falls back to {@link #DEFAULT_CORNER_RADIUS} when no context is available.
     */
    private float resolveCornerRadius() {
        if (context != null) {
            return context.getResources().getDimension(
                    com.cometchat.chatuikit.R.dimen.cometchat_radius_2);
        }
        return DEFAULT_CORNER_RADIUS;
    }

    @ColorInt
    private int getEffectiveStripeColor() {
        if (stripeColor != 0) {
            return stripeColor;
        }
        if (context != null) {
            return CometChatTheme.getStrokeColorDark(context);
        }
        return 0;
    }

    // ==================== Getters and Setters ====================

    @ColorInt
    public int getStripeColor() {
        return stripeColor;
    }

    public void setStripeColor(@ColorInt int stripeColor) {
        this.stripeColor = stripeColor;
    }

    @ColorInt
    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getStripeWidth() {
        return stripeWidth;
    }

    public void setStripeWidth(int stripeWidth) {
        this.stripeWidth = stripeWidth;
    }

    public int getGapWidth() {
        return gapWidth;
    }

    public void setGapWidth(int gapWidth) {
        this.gapWidth = gapWidth;
    }
}

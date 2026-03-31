package com.cometchat.chatuikit.shared.spans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.LineHeightSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

/**
 * Span for code blocks in WYSIWYG rich text editing.
 * <p>
 * This span implements {@link LineBackgroundSpan} to draw a unified background
 * behind code block text. It tracks line positions to draw rounded corners only
 * on the first and last lines, creating a continuous block appearance.
 * </p>
 * <p>
 * The span draws the background per-line but maintains state to ensure:
 * - Rounded top corners only on the first line
 * - Rounded bottom corners only on the last line
 * - Square corners for middle lines to create a unified block
 * </p>
 * <p>
 * Note: Vertical separation from surrounding text is achieved by inserting
 * newline characters before/after the code block in RichTextSpanManager,
 * not through this span.
 * </p>
 *
 * @see RichTextFormatSpan
 * @see LineBackgroundSpan
 * @see FormatType#CODE_BLOCK
 */
public class CodeBlockFormatSpan implements RichTextFormatSpan, LineBackgroundSpan, LineHeightSpan, LeadingMarginSpan {

    /**
     * Default corner radius for rounded background (in pixels).
     */
    public static final float DEFAULT_CORNER_RADIUS = 16f;

    /**
     * Default border width (in pixels).
     */
    public static final float DEFAULT_BORDER_WIDTH = 1f;

    /**
     * Default vertical padding inside the background (in pixels).
     */
    public static final float DEFAULT_PADDING = 12f;

    /**
     * Default horizontal padding inside the background (in pixels).
     */
    public static final float DEFAULT_HORIZONTAL_PADDING = 24f;

    /**
     * Default background color for code block (light gray for composer).
     */
    public static final int DEFAULT_BACKGROUND_COLOR = 0xFFF8F8F8;

    /**
     * Default border color for code block (medium gray for composer).
     */
    public static final int DEFAULT_BORDER_COLOR = 0xFFDDDDDD;

    /**
     * Vertical margin (in pixels) between the code block and surrounding text.
     */
    public static final float DEFAULT_VERTICAL_MARGIN = 16f;

    @ColorInt
    private int backgroundColor;

    @ColorInt
    private int borderColor;

    private float borderWidth;
    private float cornerRadius;
    private float padding;
    private float horizontalPadding;

    @Nullable
    private final Context context;

    // Paint objects for drawing
    private final Paint backgroundPaint;
    private final Paint borderPaint;
    private final RectF rectF;

    /**
     * Creates a new CodeBlockFormatSpan with default styling.
     */
    public CodeBlockFormatSpan() {
        this.backgroundColor = DEFAULT_BACKGROUND_COLOR;
        this.borderColor = DEFAULT_BORDER_COLOR;
        this.borderWidth = DEFAULT_BORDER_WIDTH;
        this.cornerRadius = DEFAULT_CORNER_RADIUS;
        this.padding = DEFAULT_PADDING;
        this.horizontalPadding = DEFAULT_HORIZONTAL_PADDING;
        this.context = null;

        this.backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.backgroundPaint.setStyle(Paint.Style.FILL);
        this.borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.borderPaint.setStyle(Paint.Style.STROKE);
        this.rectF = new RectF();
    }

    /**
     * Creates a new CodeBlockFormatSpan with context for theme colors.
     *
     * @param context The context used to access theme colors.
     */
    public CodeBlockFormatSpan(@NonNull Context context) {
        this.context = context;
        this.backgroundColor = CometChatTheme.getBackgroundColor2(context);
        this.borderColor = CometChatTheme.getStrokeColorDefault(context);
        this.borderWidth = DEFAULT_BORDER_WIDTH;
        this.cornerRadius = DEFAULT_CORNER_RADIUS;
        this.padding = DEFAULT_PADDING;
        this.horizontalPadding = DEFAULT_HORIZONTAL_PADDING;

        this.backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.backgroundPaint.setStyle(Paint.Style.FILL);
        this.borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.borderPaint.setStyle(Paint.Style.STROKE);
        this.rectF = new RectF();
    }

    /**
     * Creates a new CodeBlockFormatSpan with custom styling.
     */
    public CodeBlockFormatSpan(@ColorInt int backgroundColor, @ColorInt int borderColor,
                                float borderWidth, float cornerRadius) {
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        this.cornerRadius = cornerRadius;
        this.padding = DEFAULT_PADDING;
        this.horizontalPadding = DEFAULT_HORIZONTAL_PADDING;
        this.context = null;

        this.backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.backgroundPaint.setStyle(Paint.Style.FILL);
        this.borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.borderPaint.setStyle(Paint.Style.STROKE);
        this.rectF = new RectF();
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.CODE_BLOCK;
    }

    /**
     * Adds vertical margin above the first line and below the last line of the
     * code block by adjusting font metrics. This creates real spacing between
     * the code block and surrounding text, plus internal padding inside the block.
     */
    @Override
    public void chooseHeight(CharSequence text, int start, int end,
                             int spanstartv, int lineHeight,
                             Paint.FontMetricsInt fm) {
        if (text instanceof Spanned) {
            Spanned spanned = (Spanned) text;
            int spanStart = spanned.getSpanStart(this);
            int spanEnd = spanned.getSpanEnd(this);

            int margin = (int) DEFAULT_VERTICAL_MARGIN;
            int internalPad = (int) padding;

            // First line of the span: the line that contains spanStart
            boolean isFirstLine = (spanStart >= start && spanStart < end)
                    || (start == 0 && spanStart == 0);

            // Last line of the span: spanEnd falls within or at this line's boundary
            boolean isLastLine = (spanEnd > start && spanEnd <= end)
                    || (end >= text.length() && spanEnd >= text.length());

            if (isFirstLine) {
                fm.ascent -= internalPad;
                fm.top -= internalPad;
                if (spanStart > 0) {
                    fm.ascent -= margin;
                    fm.top -= margin;
                }
            }

            if (isLastLine) {
                fm.descent += internalPad;
                fm.bottom += internalPad;
                if (spanEnd < text.length()) {
                    fm.descent += margin;
                    fm.bottom += margin;
                }
            }
        }
    }

    /**
     * Adds horizontal leading margin (left padding) inside the code block.
     */
    @Override
    public int getLeadingMargin(boolean first) {
        return (int) horizontalPadding;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                   int top, int baseline, int bottom,
                                   CharSequence text, int start, int end,
                                   boolean first, Layout layout) {
        // No custom drawing needed — the margin offset is sufficient
    }

    /**
     * Called by the text layout to draw the background for each line.
     * <p>
     * This method draws a unified code block background by:
     * - Drawing rounded top corners only on the first line
     * - Drawing rounded bottom corners only on the last line
     * - Drawing square corners for middle lines
     * - Extending non-last lines to the top of the next line to eliminate gaps
     *   (especially visible with empty lines in code blocks)
     * </p>
     */
    @Override
    public void drawBackground(@NonNull Canvas canvas, @NonNull Paint paint,
                                int left, int right, int top, int baseline, int bottom,
                                @NonNull CharSequence text, int start, int end, int lineNumber) {
        // Calculate which lines this span covers
        if (text instanceof android.text.Spanned) {
            android.text.Spanned spanned = (android.text.Spanned) text;
            int spanStart = spanned.getSpanStart(this);
            int spanEnd = spanned.getSpanEnd(this);

            // If this line doesn't contain any part of the span, skip drawing
            if (end <= spanStart || start >= spanEnd) {
                return;
            }

            // If a BlockquoteFormatSpan is also present on this range, offset the
            // code block background to the right so the blockquote stripe stays
            // visually outside the code block.
            int adjustedLeft = left;
            BlockquoteFormatSpan[] blockquoteSpans = spanned.getSpans(
                    start, end, BlockquoteFormatSpan.class);
            if (blockquoteSpans != null && blockquoteSpans.length > 0) {
                adjustedLeft = left + blockquoteSpans[0].getLeadingMargin(true);
            }

            // Determine if this is the first line of the span
            // First line: this line contains the span start position
            boolean isFirstLine = (spanStart >= start && spanStart < end) || (start == 0 && spanStart == 0);
            
            // Determine if this is the last line of the span
            // We need to check if any subsequent line would contain part of the span.
            boolean isLastLine = false;
            int textLen = text.length();
            
            // If span covers entire text and this is the first line, it's also the last
            if (spanStart == 0 && spanEnd >= textLen && isFirstLine) {
                isLastLine = true;
            }
            
            // Primary check: span ends within or at this line's end
            if (!isLastLine && spanEnd <= end) {
                isLastLine = true;
            }
            
            // Secondary check: both line and span reach text end
            if (!isLastLine && end >= textLen && spanEnd >= textLen) {
                isLastLine = true;
            }
            
            // Tertiary check: line extends past span end
            if (!isLastLine && end > spanEnd) {
                isLastLine = true;
            }
            
            // If this is the first line and the span doesn't contain any newlines,
            // it must also be the last line (single-line code block)
            if (!isLastLine && isFirstLine) {
                String spanContent = text.subSequence(spanStart, Math.min(spanEnd, textLen)).toString();
                if (!spanContent.contains("\n")) {
                    isLastLine = true;
                }
            }
            
            // Fallback: if this is line 0 and text has no newlines, it's both first and last
            if (!isLastLine && lineNumber == 0 && !text.toString().contains("\n")) {
                isLastLine = true;
            }

            // For non-last lines, extend the bottom to the top of the next line
            // to eliminate gaps caused by line spacing (especially visible with empty lines).
            int adjustedBottom = bottom;
            if (!isLastLine) {
                adjustedBottom = getNextLineTop(text, lineNumber, bottom);
            } else {
                // When the last drawn line of the span ends with a newline, Android's
                // Layout won't call drawBackground for the resulting empty trailing line.
                // Extend the bottom to cover that empty line so the code block background
                // appears continuous when the user presses Enter.
                // Only do this when the span reaches the end of the text — if there is
                // content after the span (e.g., a plain text line between split code
                // blocks), we must NOT extend to avoid bleeding into that content.
                if (spanEnd > 0 && spanEnd <= text.length()
                        && text.charAt(spanEnd - 1) == '\n'
                        && spanEnd >= text.length()) {
                    int lineHeight = bottom - top;
                    adjustedBottom = bottom + lineHeight;
                }
                // Pull back the bottom by the margin when there's text after the code block
                if (spanEnd < text.length()) {
                    adjustedBottom = bottom - (int) DEFAULT_VERTICAL_MARGIN;
                }
            }

            // Adjust top/bottom to exclude the vertical margin added by chooseHeight.
            // The margin creates spacing between the code block and surrounding text,
            // but the background should not extend into that margin area.
            int adjustedTop = top;
            if (isFirstLine && spanStart > 0) {
                adjustedTop = top + (int) DEFAULT_VERTICAL_MARGIN;
            }

            drawCodeBlockBackground(canvas, adjustedLeft, right, adjustedTop, adjustedBottom, isFirstLine, isLastLine);
        } else {
            // Fallback: draw with rounded corners on all sides
            drawCodeBlockBackground(canvas, left, right, top, bottom, true, true);
        }
    }

    /**
     * Gets the top position of the next line to ensure seamless background coverage.
     * <p>
     * Android's text layout can introduce gaps between lines due to line spacing
     * (lineSpacingExtra, lineSpacingMultiplier, or default font metrics).
     * Empty lines in particular can have visible gaps. We extend the bottom
     * of non-last lines by a generous amount to guarantee overlap with the
     * next line. Since the next line's background draw will paint over any
     * excess, this overlap is visually safe.
     * </p>
     *
     * @param text       The text being rendered.
     * @param lineNumber The current line number.
     * @param bottom     The bottom of the current line.
     * @return The extended bottom position to cover any line spacing gap.
     */
    private int getNextLineTop(@NonNull CharSequence text, int lineNumber, int bottom) {
        // Extend by the full padding value to generously cover any line spacing gap.
        // On high-density screens, line spacing can be several pixels.
        // The next line's background will paint over any overlap, so this is safe.
        return bottom + (int) Math.ceil(padding);
    }

    /**
     * Draws the code block background for a single line.
     *
     * @param canvas      The canvas to draw on.
     * @param left        Left edge of the line.
     * @param right       Right edge of the line.
     * @param top         Top of the line.
     * @param bottom      Bottom of the line.
     * @param isFirstLine Whether this is the first line of the code block.
     * @param isLastLine  Whether this is the last line of the code block.
     */
    private void drawCodeBlockBackground(Canvas canvas, int left, int right,
                                          int top, int bottom,
                                          boolean isFirstLine, boolean isLastLine) {
        // Set up colors
        int bgColor = getBackgroundColor();
        backgroundPaint.setColor(bgColor);

        // Calculate rect bounds with padding
        // For non-last lines, the bottom value is already adjusted by drawBackground()
        // to extend to the next line's top, eliminating gaps between lines.
        // For first/last lines, we need to extend by at least cornerRadius to show rounded corners
        // Use the larger of padding or cornerRadius to ensure rounded corners are fully visible
        float cursorOverflow = 4f;
        float rectTop = isFirstLine ? top - cursorOverflow : top;
        float rectBottom = isLastLine ? bottom + cursorOverflow : bottom;
        /*float verticalExtension = Math.max(padding, cornerRadius);
        float rectTop = isFirstLine ? top - verticalExtension : top;
        float rectBottom = isLastLine ? bottom + verticalExtension : bottom;*/

        // Determine corner radii based on position
        float topLeftRadius = isFirstLine ? cornerRadius : 0;
        float topRightRadius = isFirstLine ? cornerRadius : 0;
        float bottomLeftRadius = isLastLine ? cornerRadius : 0;
        float bottomRightRadius = isLastLine ? cornerRadius : 0;

        // Draw the background with appropriate corners
        rectF.set(left, rectTop, right, rectBottom);

        if (isFirstLine && isLastLine) {
            // Single line: all corners rounded
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, backgroundPaint);
        } else if (isFirstLine) {
            // First line: only top corners rounded
            drawRoundRectWithCorners(canvas, rectF, backgroundPaint,
                    topLeftRadius, topRightRadius, 0, 0);
        } else if (isLastLine) {
            // Last line: only bottom corners rounded
            drawRoundRectWithCorners(canvas, rectF, backgroundPaint,
                    0, 0, bottomRightRadius, bottomLeftRadius);
        } else {
            // Middle line: no rounded corners
            canvas.drawRect(rectF, backgroundPaint);
        }

        // Draw border only on the outer edges to create a unified block
        // We avoid drawing any internal horizontal lines between text lines
        int borderCol = getBorderColor();
        if (borderCol != 0 && borderWidth > 0) {
            borderPaint.setColor(borderCol);
            borderPaint.setStrokeWidth(borderWidth);

            if (isFirstLine && isLastLine) {
                // Single line: draw complete rounded border
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint);
            } else {
                // Multi-line: draw only the outer edges, no internal horizontal lines
                
                // Always draw left and right vertical borders
                float leftX = rectF.left + borderWidth / 2;
                float rightX = rectF.right - borderWidth / 2;
                
                if (isFirstLine) {
                    // First line: draw top arc and vertical sides
                    android.graphics.Path topPath = new android.graphics.Path();
                    topPath.moveTo(leftX, rectF.bottom);
                    topPath.lineTo(leftX, rectF.top + cornerRadius);
                    topPath.arcTo(rectF.left, rectF.top, rectF.left + cornerRadius * 2, rectF.top + cornerRadius * 2, 
                                  180, 90, false);
                    topPath.lineTo(rectF.right - cornerRadius, rectF.top);
                    topPath.arcTo(rectF.right - cornerRadius * 2, rectF.top, rectF.right, rectF.top + cornerRadius * 2,
                                  270, 90, false);
                    topPath.lineTo(rightX, rectF.bottom);
                    canvas.drawPath(topPath, borderPaint);
                } else if (isLastLine) {
                    // Last line: draw bottom arc and vertical sides
                    android.graphics.Path bottomPath = new android.graphics.Path();
                    bottomPath.moveTo(leftX, rectF.top);
                    bottomPath.lineTo(leftX, rectF.bottom - cornerRadius);
                    bottomPath.arcTo(rectF.left, rectF.bottom - cornerRadius * 2, rectF.left + cornerRadius * 2, rectF.bottom,
                                     180, -90, false);
                    bottomPath.lineTo(rectF.right - cornerRadius, rectF.bottom);
                    bottomPath.arcTo(rectF.right - cornerRadius * 2, rectF.bottom - cornerRadius * 2, rectF.right, rectF.bottom,
                                     90, -90, false);
                    bottomPath.lineTo(rightX, rectF.top);
                    canvas.drawPath(bottomPath, borderPaint);
                } else {
                    // Middle line: draw only left and right vertical borders
                    canvas.drawLine(leftX, rectF.top, leftX, rectF.bottom, borderPaint);
                    canvas.drawLine(rightX, rectF.top, rightX, rectF.bottom, borderPaint);
                }
            }
        }
    }

    /**
     * Draws a rectangle with individually specified corner radii.
     * <p>
     * This is used to draw rounded corners only on specific corners
     * (e.g., top corners for first line, bottom corners for last line).
     * </p>
     */
    private void drawRoundRectWithCorners(Canvas canvas, RectF rect, Paint paint,
                                           float topLeft, float topRight,
                                           float bottomRight, float bottomLeft) {
        android.graphics.Path path = new android.graphics.Path();
        float[] radii = {
                topLeft, topLeft,           // Top-left
                topRight, topRight,         // Top-right
                bottomRight, bottomRight,   // Bottom-right
                bottomLeft, bottomLeft      // Bottom-left
        };
        path.addRoundRect(rect, radii, android.graphics.Path.Direction.CW);
        canvas.drawPath(path, paint);
    }

    // ==================== Getters and Setters ====================

    @ColorInt
    public int getBackgroundColor() {
        if (backgroundColor != 0) {
            return backgroundColor;
        }
        if (context != null) {
            return CometChatTheme.getNeutralColor200(context);
        }
        return DEFAULT_BACKGROUND_COLOR;
    }

    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @ColorInt
    public int getBorderColor() {
        if (borderColor != 0) {
            return borderColor;
        }
        if (context != null) {
            return CometChatTheme.getStrokeColorDefault(context);
        }
        return DEFAULT_BORDER_COLOR;
    }

    public void setBorderColor(@ColorInt int borderColor) {
        this.borderColor = borderColor;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
    }

    public float getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    public float getPadding() {
        return padding;
    }

    public void setPadding(float padding) {
        this.padding = padding;
    }

    public float getHorizontalPadding() {
        return horizontalPadding;
    }

    public void setHorizontalPadding(float horizontalPadding) {
        this.horizontalPadding = horizontalPadding;
    }

    /**
     * Gets the monospace typeface for code block text.
     *
     * @return The monospace {@link Typeface}.
     */
    public static Typeface getCodeTypeface() {
        return Typeface.MONOSPACE;
    }
}

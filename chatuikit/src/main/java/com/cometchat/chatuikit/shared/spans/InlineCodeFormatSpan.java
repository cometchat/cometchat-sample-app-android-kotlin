package com.cometchat.chatuikit.shared.spans;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

/**
 * Span for inline code with background styling in WYSIWYG rich text editing.
 * <p>
 * This span extends {@link MetricAffectingSpan} to provide monospace font and
 * custom text/background colors for inline code, while allowing character-level
 * cursor movement and normal backspace behavior (unlike ReplacementSpan which
 * treats the entire span as an atomic unit).
 * </p>
 * <p>
 * When applied to text, this span displays the text with:
 * <ul>
 *   <li>A subtle background color (from CometChatTheme)</li>
 *   <li>Monospace font for code appearance</li>
 *   <li>Custom text color</li>
 * </ul>
 * No markdown syntax markers (like backticks) are shown. The markdown markers are only
 * generated when converting to markdown for sending messages.
 * </p>
 *
 * @see RichTextFormatSpan
 * @see FormatType#INLINE_CODE
 */
public class InlineCodeFormatSpan extends MetricAffectingSpan implements RichTextFormatSpan {

    private static final String TAG = "InlineCodeFormatSpan";

    /**
     * Default background color for inline code (light red/pink like Slack).
     * This is a light pink/red color: #FCEAE8
     */
    private static final int DEFAULT_BACKGROUND_COLOR = 0xFFFCEAE8;

    /**
     * Default text color for inline code (dark red like Slack).
     * This is a dark red color: #C41E3A
     */
    private static final int DEFAULT_TEXT_COLOR = 0xFFC41E3A;

    /**
     * Background color for sender (right) bubble inline code.
     */
    private static final int SENDER_BACKGROUND_COLOR = 0xFFF8F8F8;

    /**
     * Text color for sender (right) bubble inline code.
     */
    private static final int SENDER_TEXT_COLOR = 0xFFFFFFFF;

    /**
     * Default padding inside the background (in pixels).
     */
    private static final float DEFAULT_PADDING = 8f;

    /**
     * Default corner radius for rounded background (in pixels).
     */
    private static final float DEFAULT_CORNER_RADIUS = 4f;

    /**
     * Default stroke/border width (in pixels).
     */
    private static final float DEFAULT_STROKE_WIDTH = 1f;

    /**
     * Default alpha value for background transparency (0-255).
     */
    private static final int DEFAULT_BACKGROUND_ALPHA = 255;

    /**
     * Default stroke/border color for inline code (medium gray).
     */
    private static final int DEFAULT_STROKE_COLOR = 0xFFDDDDDD;

    /**
     * Background color for receiver (left) bubble inline code.
     */
    private static final int RECEIVER_BACKGROUND_COLOR = 0xFFE0E0E0;

    /**
     * Stroke/border color for sender (right) bubble inline code.
     */
    private static final int SENDER_STROKE_COLOR = 0x80F5F5F5;

    private boolean isSenderBubble = false;
    private boolean isMessageBubble = false;

    @ColorInt private int backgroundColor;
    @ColorInt private int textColor;
    @ColorInt private int strokeColor;
    private float cornerRadius;
    private float padding;
    private float strokeWidth;
    private int backgroundAlpha;

    @Nullable
    private final Context context;

    /**
     * Creates a new InlineCodeFormatSpan with default styling.
     */
    public InlineCodeFormatSpan() {
        this.backgroundColor = DEFAULT_BACKGROUND_COLOR;
        this.textColor = DEFAULT_TEXT_COLOR;
        this.strokeColor = DEFAULT_STROKE_COLOR;
        this.cornerRadius = DEFAULT_CORNER_RADIUS;
        this.padding = DEFAULT_PADDING;
        this.strokeWidth = DEFAULT_STROKE_WIDTH;
        this.backgroundAlpha = DEFAULT_BACKGROUND_ALPHA;
        this.context = null;
    }

    /**
     * Creates a new InlineCodeFormatSpan with context for theme colors.
     *
     * @param context The context used to access theme colors.
     */
    public InlineCodeFormatSpan(@NonNull Context context) {
        this.context = context;
        this.backgroundColor = CometChatTheme.getBackgroundColor3(context);
        this.textColor = CometChatTheme.getPrimaryColor(context);
        this.strokeColor = CometChatTheme.getStrokeColorDefault(context);
        this.cornerRadius = DEFAULT_CORNER_RADIUS;
        this.padding = DEFAULT_PADDING;
        this.strokeWidth = DEFAULT_STROKE_WIDTH;
        this.backgroundAlpha = DEFAULT_BACKGROUND_ALPHA;
        this.isSenderBubble = false;
    }

    /**
     * Creates a new InlineCodeFormatSpan with custom styling.
     */
    public InlineCodeFormatSpan(@ColorInt int backgroundColor, @ColorInt int textColor,
                                 float cornerRadius, float padding) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.strokeColor = DEFAULT_STROKE_COLOR;
        this.cornerRadius = cornerRadius;
        this.padding = padding;
        this.strokeWidth = DEFAULT_STROKE_WIDTH;
        this.backgroundAlpha = DEFAULT_BACKGROUND_ALPHA;
        this.context = null;
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.INLINE_CODE;
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint textPaint) {
        textPaint.setTypeface(Typeface.MONOSPACE);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTypeface(Typeface.MONOSPACE);

        int txtColor = getEffectiveTextColor();
        if (txtColor != 0) {
            tp.setColor(txtColor);
        }

        int bgColor = getEffectiveBackgroundColor();
        if (bgColor != 0) {
            tp.bgColor = bgColor;
        }
    }

    @ColorInt
    private int getEffectiveBackgroundColor() {
        if (backgroundColor != 0) {
            return backgroundColor;
        }
        if (context != null) {
            return CometChatTheme.getNeutralColor200(context);
        }
        return DEFAULT_BACKGROUND_COLOR;
    }

    @ColorInt
    private int getEffectiveTextColor() {
        if (textColor != 0) {
            return textColor;
        }
        if (context != null) {
            return CometChatTheme.getTextColorPrimary(context);
        }
        return DEFAULT_TEXT_COLOR;
    }

    @ColorInt
    private int getEffectiveStrokeColor() {
        if (strokeColor != 0) {
            return strokeColor;
        }
        if (context != null) {
            return CometChatTheme.getStrokeColorDefault(context);
        }
        return DEFAULT_STROKE_COLOR;
    }

    @ColorInt
    private int getEffectiveLinkColor() {
        if (context != null) {
            return isSenderBubble
                    ? CometChatTheme.getTextColorWhite(context)
                    : CometChatTheme.getInfoColor(context);
        }
        return 0;
    }

    // ==================== Getters and Setters ====================

    @ColorInt
    public int getBackgroundColor() { return backgroundColor; }

    public void setBackgroundColor(@ColorInt int backgroundColor) { this.backgroundColor = backgroundColor; }

    @ColorInt
    public int getTextColor() { return textColor; }

    public void setTextColor(@ColorInt int textColor) { this.textColor = textColor; }

    public float getCornerRadius() { return cornerRadius; }

    public void setCornerRadius(float cornerRadius) { this.cornerRadius = cornerRadius; }

    @ColorInt
    public int getStrokeColor() { return strokeColor; }

    public void setStrokeColor(@ColorInt int strokeColor) { this.strokeColor = strokeColor; }

    public float getStrokeWidth() { return strokeWidth; }

    public void setStrokeWidth(float strokeWidth) { this.strokeWidth = strokeWidth; }

    public float getPadding() { return padding; }

    public void setPadding(float padding) { this.padding = padding; }

    public int getBackgroundAlpha() { return backgroundAlpha; }

    public void setBackgroundAlpha(int backgroundAlpha) { this.backgroundAlpha = backgroundAlpha; }

    public boolean isSenderBubble() { return isSenderBubble; }

    public void setSenderBubble(boolean senderBubble) {
        this.isSenderBubble = senderBubble;
        if (context != null) {
            if (senderBubble) {
                this.backgroundColor = CometChatTheme.getExtendedPrimaryColor700(context);
                this.strokeColor = CometChatTheme.getExtendedPrimaryColor600(context);
                this.backgroundAlpha = DEFAULT_BACKGROUND_ALPHA;
            } else {
                this.backgroundColor = CometChatTheme.getBackgroundColor3(context);
                this.textColor = CometChatTheme.getPrimaryColor(context);
                int darkStroke = CometChatTheme.getStrokeColorDark(context);
                this.strokeColor = darkStroke != 0 ? darkStroke : DEFAULT_STROKE_COLOR;
                this.backgroundAlpha = DEFAULT_BACKGROUND_ALPHA;
            }
        }
    }
}

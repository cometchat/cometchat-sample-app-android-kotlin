package com.cometchat.chatuikit.shared.views.actionbubble;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.StyleRes;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.spans.MentionMovementMethod;
import com.google.android.material.card.MaterialCardView;

/**
 * A custom view that represents a text bubble used for displaying messages in a
 * chat interface. It provides several customization options such as text color,
 * background color, font, border styling, and more. This class extends
 * {@link MaterialCardView} to provide rich material design support.
 */
public class CometChatActionBubble extends MaterialCardView {
    private static final String TAG = CometChatActionBubble.class.getSimpleName();

    private TextView textView;
    private @ColorInt int textColor;
    private @StyleRes int textAppearance;
    private @ColorInt int backgroundColor;
    private int cornerRadius;
    private int strokeWidth;
    private @ColorInt int strokeColor;
    private Drawable backgroundDrawable;
    private @StyleRes int style;

    /**
     * Constructor for initializing the CometChatTextBubble with only a context.
     *
     * @param context The context in which this view is used.
     */
    public CometChatActionBubble(Context context) {
        this(context, null);
    }

    /**
     * Constructor for initializing the CometChatTextBubble with a context and
     * attribute set.
     *
     * @param context The context in which this view is used.
     * @param attrs   The attribute set for styling this view.
     */
    public CometChatActionBubble(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatTextBubbleStyle);
    }

    /**
     * Constructor for initializing the CometChatTextBubble with a context,
     * attribute set, and a default style.
     *
     * @param context      The context in which this view is used.
     * @param attrs        The attribute set for styling this view.
     * @param defStyleAttr The default style attribute for the view.
     */
    public CometChatActionBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndInitializeView(attrs, defStyleAttr);
    }

    /**
     * Initializes the view by inflating the layout and setting up the text bubble.
     *
     * @param attrs        The attribute set for customization.
     * @param defStyleAttr The default style attribute.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        Utils.initMaterialCard(this);
        View view = View.inflate(getContext(), R.layout.cometchat_action_bubble, null);
        textView = view.findViewById(R.id.cometchat_action_bubble_text_view);
        textView.setOnLongClickListener(v -> {
            Utils.performAdapterClick(v);
            return true;
        });
        addView(view);
        applyStyleAttributes(attrs, defStyleAttr, 0);
    }

    /**
     * Applies style attributes based on the XML layout or theme.
     *
     * @param attrs        The attribute set containing customization.
     * @param defStyleAttr The default style attribute.
     * @param defStyleRes  The default style resource.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatActionBubble, defStyleAttr, defStyleRes);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatActionBubble_cometchatActionBubbleStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatActionBubble, defStyleAttr, style);
        extractAttributesAndApplyDefaults(typedArray);
    }

    /**
     * Extracts attributes from the given {@link TypedArray} and applies default
     * values.
     *
     * @param typedArray The TypedArray containing the view's attributes.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        try {
            setTextAppearance(typedArray.getResourceId(R.styleable.CometChatActionBubble_cometchatActionBubbleTextAppearance, 0));
            setTextColor(typedArray.getColor(R.styleable.CometChatActionBubble_cometchatActionBubbleTextColor,
                                             CometChatTheme.getTextColorSecondary(getContext())));
            setCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatActionBubble_cometchatActionBubbleCornerRadius, 0));
            setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatActionBubble_cometchatActionBubbleStrokeWidth, 0));
            setStrokeColor(typedArray.getColor(R.styleable.CometChatActionBubble_cometchatActionBubbleStrokeColor,
                                               CometChatTheme.getStrokeColorDefault(getContext())));
            setBackgroundColor(typedArray.getColor(R.styleable.CometChatActionBubble_cometchatActionBubbleBackgroundColor,
                                                   CometChatTheme.getBackgroundColor2(getContext())));
            setBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatActionBubble_cometchatActionBubbleBackgroundDrawable));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the text content of the text bubble.
     *
     * @param text The text to display in the bubble.
     */
    public void setText(String text) {
        textView.setText(text);
    }

    /**
     * Sets the text content of the text bubble using a {@link SpannableString}.
     *
     * @param text The spannable string to display in the bubble.
     */
    public void setText(SpannableString text) {
        textView.setText(text, TextView.BufferType.SPANNABLE);
        textView.setMovementMethod(MentionMovementMethod.getInstance());
    }

    /**
     * Gets the {@link TextView} associated with this text bubble.
     *
     * @return The TextView within the bubble.
     */
    public TextView getTextView() {
        return textView;
    }

    // Getters for testing or direct access if needed
    public int getTextColor() {
        return textColor;
    }

    /**
     * Sets the text color of the text bubble.
     *
     * @param color The color to set for the text.
     */
    public void setTextColor(@ColorInt int color) {
        textColor = color;
        textView.setTextColor(color);
    }    /**
     * Sets the border width of the text bubble.
     *
     * @param width The width of the border, in pixels.
     */
    public void setStrokeWidth(@Dimension int width) {
        strokeWidth = width;
        super.setStrokeWidth(width);
    }

    public int getTextAppearance() {
        return textAppearance;
    }    /**
     * Sets the border color of the text bubble.
     *
     * @param color The color to set for the border.
     */
    public void setStrokeColor(@ColorInt int color) {
        strokeColor = color;
        super.setStrokeColor(color);
    }

    /**
     * Sets the text appearance (style) of the text bubble.
     *
     * @param appearance The resource ID of the text appearance.
     */
    public void setTextAppearance(@StyleRes int appearance) {
        textAppearance = appearance;
        textView.setTextAppearance(appearance);
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color of the text bubble.
     *
     * @param color The color to set for the background.
     */
    @Override
    public void setBackgroundColor(@ColorInt int color) {
        backgroundColor = color;
        setCardBackgroundColor(color);
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius of the text bubble.
     *
     * @param radius The radius to set for the corners, in pixels.
     */
    public void setCornerRadius(@Dimension int radius) {
        cornerRadius = radius;
        setRadius(radius);
    }

    public Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    /**
     * Sets the background drawable for the text bubble.
     *
     * @param backgroundDrawable The drawable to set as the background.
     */
    @Override
    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        this.backgroundDrawable = backgroundDrawable;
        super.setBackgroundDrawable(backgroundDrawable);
    }

    public int getStyle() {
        return style;
    }

    /**
     * Sets the style of the text bubble from a specific style resource.
     *
     * @param style The resource ID of the style to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            this.style = style;
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatActionBubble);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    @Override
    public int getStrokeColor() {
        return strokeColor;
    }




}

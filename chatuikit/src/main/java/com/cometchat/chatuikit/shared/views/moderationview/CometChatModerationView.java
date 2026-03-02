package com.cometchat.chatuikit.shared.views.moderationview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import androidx.annotation.ColorInt;
import androidx.annotation.StyleRes;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatModerationMessageBinding;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

/**
 * CometChatModerationView is a UI component that displays a moderation message with
 * customizable background color, text color, icon tint and text appearance. The view reads style
 * attributes (via XML or programmatically using {@link #setStyle(int)}) and applies them to its
 * internal layout (icon + message container).
 */
public class CometChatModerationView extends MaterialCardView {
    private CometchatModerationMessageBinding binding;
    private @ColorInt int textColor;
    private @ColorInt int backgroundColor;
    private @ColorInt int iconTint;
    private @StyleRes int textAppearance;

    /**
     * Simple constructor used when creating the view programmatically.
     * @param context current context.
     */
    public CometChatModerationView(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating the view from XML.
     * @param context current context.
     * @param attrs the attribute set from XML.
     */
    public CometChatModerationView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatModerationViewStyle);
    }

    /**
     * Constructor allowing a default style attribute to be supplied.
     * @param context current context.
     * @param attrs the attribute set from XML.
     * @param defStyleAttr an attribute in the current theme that contains a reference to a style
     *                     resource to apply.
     */
    public CometChatModerationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndInitializeView(context, attrs, defStyleAttr);
    }

    /**
     * Inflates the view layout and initializes attributes.
     * @param context current context.
     * @param attrs attribute set from XML (may be null).
     * @param defStyleAttr default style attribute.
     */
    private void inflateAndInitializeView(Context context, AttributeSet attrs, int defStyleAttr) {
        Utils.initMaterialCard(this);
        binding = CometchatModerationMessageBinding.inflate(LayoutInflater.from(getContext()), this, true);
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Obtains style attributes either directly or via a provided style resource reference.
     * @param attrs attribute set from XML.
     * @param defStyleAttr default style attribute.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatModerationView, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(R.styleable.CometChatModerationView_cometchatModerationViewStyle, 0);
        directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatModerationView, defStyleAttr, styleResId);
        extractAttributesAndApplyDefaults(directAttributes);
    }

    /**
     * Extracts custom attributes from the typed array and applies default fallbacks before updating the UI.
     * @param typedArray the array containing styled attributes.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            backgroundColor = typedArray.getColor(R.styleable.CometChatModerationView_cometchatModerationViewBackgroundColor, 0);
            textColor = typedArray.getColor(R.styleable.CometChatModerationView_cometchatModerationViewTextColor, CometChatTheme.getErrorColor(getContext()));
            iconTint = typedArray.getColor(R.styleable.CometChatModerationView_cometchatModerationViewIconTint, CometChatTheme.getErrorColor(getContext()));
            textAppearance = typedArray.getResourceId(R.styleable.CometChatModerationView_cometchatModerationViewTextAppearance, 0);
            applyDefault();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Applies a style resource programmatically to update all customizable attributes.
     * @param style style resource id containing CometChatModerationView attributes.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatModerationView);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Applies all current attribute values to the UI elements.
     */
    private void applyDefault() {
        setModerationViewBackgroundColor(backgroundColor);
        setModerationViewIconTint(iconTint);
        setModerationViewMessageTextAppearance(textAppearance);
        setModerationViewMessageTextColor(textColor);
    }

    /**
     * Sets the text color of the moderation message.
     * @param color ARGB color int.
     */
    public void setModerationViewMessageTextColor(@ColorInt int color) {
        this.textColor = color;
        binding.message.setTextColor(color);
    }

    /**
     * Sets the background color of the moderation container.
     * @param color ARGB color int.
     */
    public void setModerationViewBackgroundColor(@ColorInt int color) {
        this.backgroundColor = color;
        binding.container.setBackgroundColor(color);
    }

    /**
     * Sets the tint color for the moderation icon.
     * @param tint ARGB color int.
     */
    public void setModerationViewIconTint(@ColorInt int tint) {
        this.iconTint = tint;
        binding.icon.setImageTintList(ColorStateList.valueOf(tint));
    }

    /**
     * Applies a text appearance style to the moderation message TextView.
     * @param styleRes style resource id for text appearance.
     */
    public void setModerationViewMessageTextAppearance(@StyleRes int styleRes) {
        binding.message.setTextAppearance(styleRes);
    }

    /**
     * Sets the text of the moderation message.
     * @param text the message text to display.
     */
    public void setModerationViewMessageText(String text) {
        if (text != null) {
            binding.message.setText(text);
        }
    }

    /**
     * Sets the text of the moderation message from a string resource.
     * @param resId the string resource id.
     */
    public void setModerationViewMessageText(int resId) {
        binding.message.setText(resId);
    }
}

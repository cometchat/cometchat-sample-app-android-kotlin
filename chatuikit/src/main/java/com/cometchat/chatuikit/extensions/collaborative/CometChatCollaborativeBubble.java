package com.cometchat.chatuikit.extensions.collaborative;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatCollaborativeBubbleBinding;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

public class CometChatCollaborativeBubble extends MaterialCardView {
    private static final String TAG = CometChatCollaborativeBubble.class.getSimpleName();
    private CometchatCollaborativeBubbleBinding binding;
    private String url;
    private OnClick onClick;

    private @StyleRes int titleTextAppearance;
    private @ColorInt int titleTextColor;
    private @StyleRes int subtitleTextAppearance;
    private @ColorInt int subtitleTextColor;
    private @ColorInt int iconTint;
    private Drawable iconDrawable;
    private @StyleRes int buttonTextAppearance;
    private @ColorInt int buttonTextColor;
    private @ColorInt int separatorColor;
    private @ColorInt int imageStrokeColor;
    private @Dimension int imageStrokeWidth;
    private @Dimension int imageCornerRadius;
    private @StyleRes int style;
    private String title;

    /**
     * Creates a new instance of {@link CometChatCollaborativeBubble} with the
     * specified context.
     *
     * <p>
     * This constructor initializes the bubble with default attributes by calling
     * the constructor with additional parameters.
     *
     * @param context The context in which this bubble will be created.
     */
    public CometChatCollaborativeBubble(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Creates a new instance of {@link CometChatCollaborativeBubble} with the
     * specified context and attributes.
     *
     * <p>
     * This constructor initializes the bubble with the provided attributes and a
     * default style attribute.
     *
     * @param context The context in which this bubble will be created.
     * @param attrs   The attributes set containing the XML attributes for the bubble.
     */
    public CometChatCollaborativeBubble(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatCollaborativeBubbleStyle);
    }

    /**
     * Creates a new instance of {@link CometChatCollaborativeBubble} with the
     * specified context, attributes, and default style attribute.
     *
     * <p>
     * This constructor initializes the bubble and calls the method to inflate and
     * set up the view components.
     *
     * @param context      The context in which this bubble will be created.
     * @param attrs        The attributes set containing the XML attributes for the bubble.
     * @param defStyleAttr The default style attribute to apply to this bubble.
     */
    public CometChatCollaborativeBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateAndInitializeView(attrs, defStyleAttr);
    }

    /**
     * Inflates the layout and initializes the view components for this bubble.
     *
     * <p>
     * This method inflates the bubble layout from XML, retrieves the views from the
     * inflated layout, sets up the button click listener, and applies style
     * attributes based on the provided attributes and style.
     *
     * @param attrs        The attributes set containing the XML attributes for the bubble.
     * @param defStyleAttr The default style attribute to apply to this bubble.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        binding = CometchatCollaborativeBubbleBinding.inflate(LayoutInflater.from(getContext()), this, true);
        Utils.initMaterialCard(this);
        Utils.initMaterialCard(binding.bubbleImageContainer);
        binding.joinButton.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick();
            else {
                Intent intent = new Intent(getContext(), CometChatWebViewActivity.class);
                intent.putExtra(UIKitConstants.IntentStrings.TITLE, title);
                intent.putExtra(UIKitConstants.IntentStrings.URL, url);
                getContext().startActivity(intent);
            }
        });
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
        TypedArray typedArray = getContext()
            .getTheme()
            .obtainStyledAttributes(attrs, R.styleable.CometChatCollaborativeBubble, defStyleAttr, defStyleRes);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatCollaborativeBubble, defStyleAttr, style);
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
            setTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleTitleTextAppearance,
                                                            0));
            setTitleTextColor(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleTitleTextColor, 0));
            setSubtitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleSubtitleTextAppearance,
                                                               0));
            setSubtitleTextColor(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleSubtitleTextColor, 0));
            setIcon(typedArray.getDrawable(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleIconDrawable));
            setIconTint(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleIconTint,
                                            CometChatTheme.getIconTintHighlight(getContext())));
            setIconDrawable(typedArray.getDrawable(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleIconDrawable));
            setButtonTextAppearance(typedArray.getResourceId(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleButtonTextAppearance,
                                                             0));
            setButtonTextColor(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleButtonTextColor,
                                                   CometChatTheme.getPrimaryColor(getContext())));
            setSeparatorColor(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleSeparatorColor,
                                                  CometChatTheme.getExtendedPrimaryColor800(getContext())));
            setImageStrokeColor(typedArray.getColor(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleImageStrokeColor, 0));
            setImageStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleImageStrokeWidth,
                                                                 0));
            setImageCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatCollaborativeBubble_cometchatCollaborativeBubbleImageCornerRadius,
                                                                  0));
        } finally {
            typedArray.recycle();
        }
    }

    public @StyleRes int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Sets the text appearance style for the title.
     *
     * <p>
     * This method applies the specified text appearance style to the title view.
     *
     * @param titleTextAppearance The resource ID of the text appearance style to apply.
     */
    public void setTitleTextAppearance(@StyleRes int titleTextAppearance) {
        this.titleTextAppearance = titleTextAppearance;
        binding.title.setTextAppearance(titleTextAppearance);
    }

    public @ColorInt int getTitleTextColor() {
        return titleTextColor;
    }

    /**
     * Sets the text color for the title.
     *
     * <p>
     * This method updates the color of the title text.
     *
     * @param titleTextColor The color to set for the title text.
     */
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        this.titleTextColor = titleTextColor;
        binding.title.setTextColor(titleTextColor);
    }

    public @StyleRes int getSubtitleTextAppearance() {
        return subtitleTextAppearance;
    }

    /**
     * Sets the text appearance style for the subtitle.
     *
     * <p>
     * This method applies the specified text appearance style to the subtitle view.
     *
     * @param subtitleTextAppearance The resource ID of the text appearance style to apply.
     */
    public void setSubtitleTextAppearance(@StyleRes int subtitleTextAppearance) {
        this.subtitleTextAppearance = subtitleTextAppearance;
        binding.tvLastMessageText.setTextAppearance(subtitleTextAppearance);
    }

    public @ColorInt int getSubtitleTextColor() {
        return subtitleTextColor;
    }

    /**
     * Sets the text color for the subtitle.
     *
     * <p>
     * This method updates the color of the subtitle text.
     *
     * @param subtitleTextColor The color to set for the subtitle text.
     */
    public void setSubtitleTextColor(@ColorInt int subtitleTextColor) {
        this.subtitleTextColor = subtitleTextColor;
        binding.tvLastMessageText.setTextColor(subtitleTextColor);
    }

    public @ColorInt int getIconTint() {
        return iconTint;
    }

    /**
     * Sets the tint color for the icon.
     *
     * <p>
     * This method applies the specified tint color to the icon drawable.
     *
     * @param iconTint The color to set as the tint for the icon.
     */
    public void setIconTint(@ColorInt int iconTint) {
        this.iconTint = iconTint;
        binding.icon.setImageTintList(ColorStateList.valueOf(iconTint));
    }

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    /**
     * Sets the drawable for the icon.
     *
     * <p>
     * This method updates the icon view with the specified drawable.
     *
     * @param iconDrawable The drawable to set as the icon.
     */
    public void setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
        binding.icon.setImageDrawable(iconDrawable);
    }

    public @StyleRes int getButtonTextAppearance() {
        return buttonTextAppearance;
    }

    /**
     * Sets the text appearance style for the button.
     *
     * <p>
     * This method applies the specified text appearance style to the button view.
     *
     * @param buttonTextAppearance The resource ID of the text appearance style to apply.
     */
    public void setButtonTextAppearance(@StyleRes int buttonTextAppearance) {
        this.buttonTextAppearance = buttonTextAppearance;
        binding.joinButton.setTextAppearance(buttonTextAppearance);
    }

    public @ColorInt int getButtonTextColor() {
        return buttonTextColor;
    }

    /**
     * Sets the text color for the button.
     *
     * <p>
     * This method updates the color of the button text.
     *
     * @param buttonTextColor The color to set for the button text.
     */
    public void setButtonTextColor(@ColorInt int buttonTextColor) {
        this.buttonTextColor = buttonTextColor;
        binding.joinButton.setTextColor(buttonTextColor);
    }

    public @ColorInt int getSeparatorColor() {
        return separatorColor;
    }

    /**
     * Sets the color for the separator.
     *
     * <p>
     * This method updates the background color of the separator view.
     *
     * @param separatorColor The color to set for the separator.
     */
    public void setSeparatorColor(@ColorInt int separatorColor) {
        this.separatorColor = separatorColor;
        binding.separator.setBackgroundColor(separatorColor);
    }

    public @ColorInt int getImageStrokeColor() {
        return imageStrokeColor;
    }

    // Getters for testing or direct access if needed

    /**
     * Sets the stroke color for the image container.
     *
     * <p>
     * This method applies the specified stroke color to the bubble image container.
     *
     * @param imageStrokeColor The color to set for the image stroke.
     */
    public void setImageStrokeColor(@ColorInt int imageStrokeColor) {
        this.imageStrokeColor = imageStrokeColor;
        binding.bubbleImageContainer.setStrokeColor(imageStrokeColor);
    }

    public @Dimension int getImageStrokeWidth() {
        return imageStrokeWidth;
    }

    /**
     * Sets the stroke width for the image container.
     *
     * <p>
     * This method updates the stroke width of the bubble image container.
     *
     * @param imageStrokeWidth The width of the stroke to apply.
     */
    public void setImageStrokeWidth(@Dimension int imageStrokeWidth) {
        this.imageStrokeWidth = imageStrokeWidth;
        binding.bubbleImageContainer.setStrokeWidth(imageStrokeWidth);
    }

    public @Dimension int getImageCornerRadius() {
        return imageCornerRadius;
    }

    /**
     * Sets the corner radius for the image container.
     *
     * <p>
     * This method applies the specified corner radius to the bubble image
     * container.
     *
     * @param imageCornerRadius The radius to set for the corners of the image container.
     */
    public void setImageCornerRadius(@Dimension int imageCornerRadius) {
        this.imageCornerRadius = imageCornerRadius;
        binding.bubbleImageContainer.setRadius(imageCornerRadius);
    }

    public Drawable getIcon() {
        return binding.icon.getDrawable();
    }

    /**
     * Sets the drawable for the icon.
     *
     * <p>
     * This method updates the icon view with the specified drawable.
     *
     * @param drawable The drawable to set as the icon.
     */
    public void setIcon(Drawable drawable) {
        binding.icon.setImageDrawable(drawable);
    }

    public String getTitle() {
        return binding.title.getText().toString();
    }

    /**
     * Sets the title text.
     *
     * <p>
     * This method updates the title view with the specified text.
     *
     * @param title The text to set as the title.
     */
    public void setTitle(String title) {
        if (title != null) {
            this.title = title;
            binding.title.setText(title);
        }
    }

    public String getSubTitle() {
        return binding.tvLastMessageText.getText().toString();
    }

    /**
     * Sets the subtitle text.
     *
     * <p>
     * This method updates the subtitle view with the specified text.
     *
     * @param subTitle The text to set as the subtitle.
     */
    public void setSubTitle(String subTitle) {
        this.binding.tvLastMessageText.setText(subTitle);
    }

    public String getButtonText() {
        return binding.joinButton.getText().toString();
    }

    /**
     * Sets the button text.
     *
     * <p>
     * This method updates the button view with the specified text.
     *
     * @param buttonText The text to set as the button label.
     */
    public void setButtonText(String buttonText) {
        binding.joinButton.setText(buttonText);
    }

    public String getBoardUrl() {
        return url;
    }

    /**
     * Sets the URL for the board.
     *
     * <p>
     * This method stores the specified URL for later use.
     *
     * @param url The URL to set for the board.
     */
    public void setBoardUrl(String url) {
        this.url = url;
    }

    public OnClick getOnClick() {
        return onClick;
    }

    /**
     * Sets the click listener for the bubble.
     *
     * <p>
     * This method allows setting a custom click action for the bubble.
     *
     * @param onClick The click listener to set for the bubble.
     */
    public void setOnClick(OnClick onClick) {
        this.onClick = onClick;
    }

    public @StyleRes int getStyle() {
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
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatCollaborativeBubble);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }
}

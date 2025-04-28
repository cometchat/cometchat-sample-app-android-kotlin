package com.cometchat.chatuikit.calls.callbubble;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.StyleRes;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.databinding.CometchatMeetCallBubbleBinding;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.interfaces.OnClick;
import com.cometchat.chatuikit.shared.resources.localise.CometChatLocalize;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A custom widget representing a call bubble for the Calling feature.
 *
 * <p>
 * It extends the MaterialCardView class and provides methods to customize its
 * appearance and behavior.
 */
public class CometChatMeetCallBubble extends MaterialCardView {
    private static final String TAG = CometChatMeetCallBubble.class.getSimpleName();

    /**
     * The binding object for the CometchatMeetCallBubble layout, used to bind UI
     * components in the view.
     */
    private CometchatMeetCallBubbleBinding binding;

    /**
     * Click event listener for the call bubble, providing callback functionality
     * for user interactions.
     */
    private OnClick onClick;

    /**
     * Icon drawable representing an incoming voice call.
     */
    private Drawable incomingVoiceCallIcon;

    /**
     * Icon drawable representing an incoming video call.
     */
    private Drawable incomingVideoCallIcon;

    /**
     * Icon drawable representing an outgoing voice call.
     */
    private Drawable outgoingVoiceCallIcon;

    /**
     * Icon drawable representing an outgoing video call.
     */
    private Drawable outgoingVideoCallIcon;

    /**
     * Color tint applied to all call icons.
     */
    private @ColorInt int callIconTint;

    /**
     * Background color for the call icon's background area.
     */
    private @ColorInt int iconBackgroundColor;

    /**
     * Text color for the title text in the call bubble.
     */
    private @ColorInt int titleTextColor;

    /**
     * Text appearance style for the title text in the call bubble.
     */
    private @StyleRes int titleTextAppearance;

    /**
     * Text color for the subtitle text in the call bubble.
     */
    private @ColorInt int subtitleTextColor;

    /**
     * Text appearance style for the subtitle text in the call bubble.
     */
    private @StyleRes int subtitleTextAppearance;

    /**
     * Color of the separator line in the call bubble layout.
     */
    private @ColorInt int separatorColor;

    /**
     * Text color for the join button in the call bubble.
     */
    private @ColorInt int joinButtonTextColor;

    /**
     * Text appearance style for the join button text in the call bubble.
     */
    private @StyleRes int joinButtonTextAppearance;

    /**
     * Background color for the call bubble.
     */
    private @ColorInt int backgroundColor;

    /**
     * Color of the stroke (border) around the call bubble.
     */
    private @ColorInt int strokeColor;

    /**
     * Width of the stroke (border) around the call bubble.
     */
    private @Dimension int strokeWidth;

    /**
     * Corner radius for the call bubble, defining its roundness.
     */
    private @Dimension int cornerRadius;

    /**
     * Drawable used as the background for the call bubble.
     */
    private Drawable backgroundDrawable;

    /**
     * Style resource ID used for styling the call bubble's appearance.
     */
    private @StyleRes int style;

    /**
     * Constructs a new CometChatCallBubble object with the given context.
     *
     * @param context The context in which the call bubble will be used.
     */
    public CometChatMeetCallBubble(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatCallBubble object with the given context and
     * attribute set.
     *
     * @param context The context in which the call bubble will be used.
     * @param attrs   The attribute set for initializing the call bubble.
     */
    public CometChatMeetCallBubble(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatMeetCallBubbleStyle);
    }

    /**
     * Constructs a new CometChatCallBubble object with the given context, attribute
     * set, and default style.
     *
     * @param context      The context in which the call bubble will be used.
     * @param attrs        The attribute set for initializing the call bubble.
     * @param defStyleAttr The default style attribute for the call bubble.
     */
    public CometChatMeetCallBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(attrs, defStyleAttr);
    }

    /**
     * Initializes the view components and applies style attributes for the
     * CometchatMeetCallBubble. Sets up the view binding, initializes material card
     * properties, and assigns a click listener to the join call button. This method
     * also applies style attributes defined in XML or theme.
     *
     * @param attrs        AttributeSet containing custom attributes applied via XML.
     * @param defStyleAttr Default style attribute reference, allowing fallback styles to be
     *                     applied.
     */
    private void initializeView(AttributeSet attrs, int defStyleAttr) {
        binding = CometchatMeetCallBubbleBinding.inflate(LayoutInflater.from(getContext()), this, true);
        Utils.initMaterialCard(this);
        Utils.initMaterialCard(binding.callIconCard);
        binding.joinCall.setOnClickListener(view1 -> {
            if (onClick != null) onClick.onClick();
        });
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Applies style attributes based on the XML layout or theme.
     *
     * @param attrs        The attribute set containing customization.
     * @param defStyleAttr The default style attribute.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatMeetCallBubble, defStyleAttr, 0);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatMeetCallBubble, defStyleAttr, style);
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
            setIncomingVideoCallIcon(typedArray.getDrawable(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleIncomingVideoCallIcon));
            setIncomingVoiceCallIcon(typedArray.getDrawable(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleIncomingVoiceCallIcon));
            setOutgoingVideoCallIcon(typedArray.getDrawable(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleOutgoingVideoCallIcon));
            setOutgoingVoiceCallIcon(typedArray.getDrawable(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleOutgoingVoiceCallIcon));
            setIconBackgroundColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleIconBackgroundColor, 0));
            setCallIconTint(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleCallIconTint, 0));
            setSeparatorColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleSeparatorColor,
                                                  CometChatTheme.getExtendedPrimaryColor800(getContext())
            ));
            setTitleTextColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleTitleTextColor, 0));
            setTitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleTitleTextAppearance, 0));
            setSubtitleTextColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleSubtitleTextColor, 0));
            setSubtitleTextAppearance(typedArray.getResourceId(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleSubtitleTextAppearance, 0));
            setButtonTextColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleJoinButtonTextColor, 0));
            setButtonTextAppearance(typedArray.getResourceId(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleJoinButtonTextAppearance, 0));

            setBackgroundColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleBackgroundColor, 0));
            setStrokeColor(typedArray.getColor(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleStrokeColor, 0));
            setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleStrokeWidth, 0));
            setCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleCornerRadius, 0));
            Drawable drawable = typedArray.getDrawable(R.styleable.CometChatMeetCallBubble_cometchatMeetCallBubbleBackgroundDrawable);
            if (drawable != null) super.setBackground(drawable);
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the text color of the join call button.
     *
     * @param color The color to be applied to the button text.
     */
    public void setButtonTextColor(@ColorInt int color) {
        this.joinButtonTextColor = color;
        binding.tvJoinCall.setTextColor(color);
    }

    /**
     * Sets the appearance of the join call button text using a text style resource.
     *
     * @param appearance The text style resource defining the appearance of the button
     *                   text.
     */
    public void setButtonTextAppearance(@StyleRes int appearance) {
        this.joinButtonTextAppearance = appearance;
        binding.tvJoinCall.setTextAppearance(appearance);
    }

    /**
     * Sets the stroke color of the card's border.
     *
     * @param strokeColor The color to apply to the card's stroke.
     */
    @Override
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        super.setStrokeColor(strokeColor);
    }

    /**
     * Sets the background color resource of the join call button.
     *
     * @param color The drawable resource to be used as the background of the button.
     */
    public void setButtonBackgroundColor(@ColorInt int color) {
        this.iconBackgroundColor = color;
    }

    /**
     * Sets the text of the join call button.
     *
     * @param text The text to be displayed on the button.
     */
    public void setButtonText(String text) {
        binding.tvJoinCall.setText(text);
    }

    /**
     * Configures the UI elements based on the provided CustomMessage data,
     * determining the call type, direction (incoming or outgoing), and setting
     * appropriate icons and texts.
     *
     * @param customMessage The CustomMessage object containing details of the call, including
     *                      its type and timestamp.
     */
    public void setMessage(CustomMessage customMessage) {
        if (customMessage.getCustomData() != null) {
            String callType = "";
            try {
                callType = customMessage.getCustomData().getString("callType");
            } catch (Exception e) {
                CometChatLogger.e(TAG, e.toString());
            }
            boolean isIncoming = !CometChatUIKit.getLoggedInUser().getUid().equals(customMessage.getSender().getUid());
            switch (callType) {
                case CometChatConstants.CALL_TYPE_AUDIO:
                    binding.callIcon.setImageDrawable(isIncoming ? incomingVoiceCallIcon : outgoingVoiceCallIcon);
                    break;
                case CometChatConstants.CALL_TYPE_VIDEO:
                    binding.callIcon.setImageDrawable(isIncoming ? incomingVideoCallIcon : outgoingVideoCallIcon);
                    break;
            }
            setTitleText(CometChatConstants.CALL_TYPE_VIDEO.equals(callType) ? getContext().getString(R.string.cometchat_video_call) : getContext().getString(
                R.string.cometchat_audio_call));
            setSubtitleText(formatSeconds(customMessage.getSentAt()));
        }
    }

    /**
     * Sets the title text of the call bubble.
     *
     * @param title The text to be displayed as the title.
     */
    public void setTitleText(String title) {
        binding.titleText.setText(title);
    }

    /**
     * Sets the title text of the call bubble.
     *
     * @param title The text to be displayed as the title.
     */
    public void setSubtitleText(String title) {
        binding.subtitleText.setText(title);
    }

    /**
     * Converts a time value in seconds to a formatted date and time string.
     *
     * @param seconds The time value in seconds since epoch.
     * @return A string representation of the date and time in the format "dd MMM,
     * HH:mm a".
     */
    public static String formatSeconds(long seconds) {
        // Convert seconds to milliseconds
        long milliseconds = seconds * 1000;
        // Create a Date object with the milliseconds
        Date date = new Date(milliseconds);
        // Format the date to "dd MMM, HH:mm a"
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM, hh:mm a", CometChatLocalize.getDefault());
        return formatter.format(date);
    }

    /**
     * Gets the current OnClick action.
     *
     * @return The OnClick listener instance for this view.
     */
    public OnClick getOnClick() {
        return onClick;
    }

    /**
     * Sets the click listener for the join call button.
     *
     * @param onClick The callback to be invoked when the button is clicked.
     */
    public void setOnClick(OnClick onClick) {
        if (onClick != null) this.onClick = onClick;
    }

    /**
     * Retrieves the drawable for the incoming voice call icon.
     *
     * @return The Drawable used as the icon for incoming voice calls.
     */
    public Drawable getIncomingVoiceCallIcon() {
        return incomingVoiceCallIcon;
    }

    /**
     * Sets the icon to display for incoming voice calls.
     *
     * @param incomingVoiceCallIcon The Drawable representing the incoming voice call icon.
     */
    public void setIncomingVoiceCallIcon(Drawable incomingVoiceCallIcon) {
        this.incomingVoiceCallIcon = incomingVoiceCallIcon;
    }

    /**
     * Gets the binding associated with this view.
     *
     * @return The CometchatMeetCallBubbleBinding instance for binding UI elements.
     */
    public CometchatMeetCallBubbleBinding getBinding() {
        return binding;
    }

    /**
     * Retrieves the drawable for the incoming video call icon.
     *
     * @return The Drawable used as the icon for incoming video calls.
     */
    public Drawable getIncomingVideoCallIcon() {
        return incomingVideoCallIcon;
    }

    /**
     * Sets the icon to display for incoming video calls.
     *
     * @param incomingVideoCallIcon The Drawable representing the incoming video call icon.
     */
    public void setIncomingVideoCallIcon(Drawable incomingVideoCallIcon) {
        this.incomingVideoCallIcon = incomingVideoCallIcon;
    }

    /**
     * Retrieves the drawable for the outgoing voice call icon.
     *
     * @return The Drawable used as the icon for outgoing voice calls.
     */
    public Drawable getOutgoingVoiceCallIcon() {
        return outgoingVoiceCallIcon;
    }

    /**
     * Sets the icon to display for outgoing voice calls.
     *
     * @param outgoingVoiceCallIcon The Drawable representing the outgoing voice call icon.
     */
    public void setOutgoingVoiceCallIcon(Drawable outgoingVoiceCallIcon) {
        this.outgoingVoiceCallIcon = outgoingVoiceCallIcon;
    }

    /**
     * Retrieves the drawable for the outgoing video call icon.
     *
     * @return The Drawable used as the icon for outgoing video calls.
     */
    public Drawable getOutgoingVideoCallIcon() {
        return outgoingVideoCallIcon;
    }

    /**
     * Sets the icon to display for outgoing video calls.
     *
     * @param outgoingVideoCallIcon The Drawable representing the outgoing video call icon.
     */
    public void setOutgoingVideoCallIcon(Drawable outgoingVideoCallIcon) {
        this.outgoingVideoCallIcon = outgoingVideoCallIcon;
    }

    /**
     * Gets the tint color applied to call icons.
     *
     * @return The integer color value used to tint call icons.
     */
    public int getCallIconTint() {
        return callIconTint;
    }

    /**
     * Sets the tint color of the call icon.
     *
     * @param incomingVoiceCallIconTint The color to apply as a tint to the call icon.
     */
    public void setCallIconTint(@ColorInt int incomingVoiceCallIconTint) {
        this.callIconTint = incomingVoiceCallIconTint;
        binding.callIcon.setColorFilter(incomingVoiceCallIconTint);
    }

    /**
     * Gets the background color for the call icon.
     *
     * @return The integer color value of the call icon's background.
     */
    public int getIconBackgroundColor() {
        return iconBackgroundColor;
    }

    /**
     * Sets the background color of the icon's container card.
     *
     * @param iconBackgroundColor The color to set as the background of the call icon container.
     */
    public void setIconBackgroundColor(@ColorInt int iconBackgroundColor) {
        this.iconBackgroundColor = iconBackgroundColor;
        binding.callIconCard.setRadius(1000);
        binding.callIconCard.setCardBackgroundColor(iconBackgroundColor);
    }

    /**
     * Retrieves the text color used for the title.
     *
     * @return The integer color value of the title text.
     */
    public int getTitleTextColor() {
        return titleTextColor;
    }

    /**
     * Sets the color of the title text.
     *
     * @param color The color to be applied to the title text.
     */
    public void setTitleTextColor(@ColorInt int color) {
        this.titleTextColor = color;
        binding.titleText.setTextColor(color);
    }    /**
     * Sets the width of the card's border stroke.
     *
     * @param strokeWidth The width, in pixels, to set for the card's stroke.
     */
    @Override
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
        super.setStrokeWidth(strokeWidth);
    }

    /**
     * Retrieves the text appearance style resource ID for the title.
     *
     * @return The resource ID for the title's text appearance style.
     */
    public int getTitleTextAppearance() {
        return titleTextAppearance;
    }

    /**
     * Sets the appearance of the title text using a text style resource.
     *
     * @param appearance The text style resource defining the appearance of the title text.
     */
    public void setTitleTextAppearance(@StyleRes int appearance) {
        this.titleTextAppearance = appearance;
        binding.titleText.setTextAppearance(appearance);
    }

    /**
     * Retrieves the text color used for the subtitle.
     *
     * @return The integer color value of the subtitle text.
     */
    public int getSubtitleTextColor() {
        return subtitleTextColor;
    }

    /**
     * Sets the color of the title text.
     *
     * @param color The color to be applied to the title text.
     */
    public void setSubtitleTextColor(@ColorInt int color) {
        this.subtitleTextColor = color;
        binding.subtitleText.setTextColor(color);
    }

    /**
     * Retrieves the text appearance style resource ID for the subtitle.
     *
     * @return The resource ID for the subtitle's text appearance style.
     */
    public int getSubtitleTextAppearance() {
        return subtitleTextAppearance;
    }

    /**
     * Sets the appearance of the title text using a text style resource.
     *
     * @param appearance The text style resource defining the appearance of the title text.
     */
    public void setSubtitleTextAppearance(@StyleRes int appearance) {
        this.subtitleTextAppearance = appearance;
        binding.subtitleText.setTextAppearance(appearance);
    }

    /**
     * Gets the color of the separator line.
     *
     * @return The integer color value of the separator line.
     */
    public int getSeparatorColor() {
        return separatorColor;
    }

    /**
     * Sets the color of the separator line between elements.
     *
     * @param separatorColor The color to set as the background of the separator line.
     */
    public void setSeparatorColor(@ColorInt int separatorColor) {
        this.separatorColor = separatorColor;
        binding.separator.setBackgroundColor(separatorColor);
    }

    /**
     * Retrieves the text color for the join button.
     *
     * @return The integer color value of the join button text.
     */
    public int getJoinButtonTextColor() {
        return joinButtonTextColor;
    }

    /**
     * Retrieves the text appearance style resource ID for the join button.
     *
     * @return The resource ID for the join button's text appearance style.
     */
    public int getJoinButtonTextAppearance() {
        return joinButtonTextAppearance;
    }

    /**
     * Gets the background color of the call bubble.
     *
     * @return The integer color value of the call bubble's background.
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color for the card.
     *
     * @param backgroundColor The color to apply as the background.
     */
    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        super.setCardBackgroundColor(backgroundColor);
        setButtonBackgroundColor(backgroundColor);
    }

    /**
     * Retrieves the stroke color for the call bubble's border.
     *
     * @return The integer color value of the stroke color.
     */
    public ColorStateList getStrokeColorSateList() {
        return ColorStateList.valueOf(strokeColor);
    }

    /**
     * Retrieves the corner radius for the call bubble.
     *
     * @return The corner radius in pixels.
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius for the card.
     *
     * @param cornerRadius The radius, in pixels, to apply to the card's corners.
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
        super.setRadius(cornerRadius);
    }

    /**
     * Retrieves the drawable used as the background for the call bubble.
     *
     * @return The Drawable for the background.
     */
    public Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    /**
     * Sets the background drawable for the card, if provided.
     *
     * @param backgroundDrawable The Drawable to set as the card's background.
     */
    @Override
    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        if (backgroundDrawable != null) {
            this.backgroundDrawable = backgroundDrawable;
            super.setBackgroundDrawable(backgroundDrawable);
        }
    }

    /**
     * Gets the style resource ID applied to the call bubble.
     *
     * @return The resource ID for the call bubble's style.
     */
    public int getStyle() {
        return style;
    }

    /**
     * Sets the style of the call action bubble from a specific style resource.
     *
     * @param style The resource ID of the style to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            this.style = style;
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatMeetCallBubble);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }




    /**
     * Retrieves the stroke width of the call bubble's border.
     *
     * @return The width of the stroke in pixels.
     */
    @Override
    public int getStrokeWidth() {
        return strokeWidth;
    }


}

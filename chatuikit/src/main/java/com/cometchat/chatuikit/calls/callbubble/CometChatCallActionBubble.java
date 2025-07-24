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
import com.cometchat.chat.core.Call;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.calls.utils.CallUtils;
import com.cometchat.chatuikit.databinding.CometchatCallActionBubbleBinding;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

/**
 * A custom view component extending {@link MaterialCardView}, used to display
 * call action bubbles for CometChat calls. The view supports customization of
 * various attributes such as colors, styles, text, and icons for both regular
 * and missed call states.
 */
public class CometChatCallActionBubble extends MaterialCardView {
    private static final String TAG = CometChatCallActionBubble.class.getSimpleName();


    /**
     * Binding for the call action bubble layout.
     */
    private CometchatCallActionBubbleBinding binding;

    /**
     * Radius for the corners of the card.
     */
    private @ColorInt int cornerRadius;

    /**
     * Width of the border stroke for the card.
     */
    private @Dimension int strokeWidth;

    /**
     * Color of the border stroke for the card.
     */
    private @ColorInt int strokeColor;

    /**
     * Background color for the call action bubble.
     */
    private @ColorInt int backgroundColor;

    /**
     * Drawable used as the background of the call action bubble.
     */
    private Drawable backgroundDrawable;

    /**
     * Color of the text within the call action bubble.
     */
    private @ColorInt int textColor;

    /**
     * Style resource for the appearance of the text.
     */
    private @StyleRes int textAppearance;

    /**
     * Tint color applied to the call type icon.
     */
    private @ColorInt int iconTint;

    /**
     * Text color for missed call messages.
     */
    private @ColorInt int missedCallTextColor;

    /**
     * Text appearance style resource for missed call messages.
     */
    private @StyleRes int missedCallTextAppearance;

    /**
     * Background color for missed call messages.
     */
    private @ColorInt int missedCallBackgroundColor;

    /**
     * Icon tint color for missed call messages.
     */
    private @ColorInt int missedCallIconTint;

    /**
     * Drawable used as the background for missed call messages.
     */
    private Drawable missedCallBackgroundDrawable;

    /**
     * Style resource used for configuring this component.
     */
    private @StyleRes int style;

    /**
     * Creates a CometChatCallActionBubble instance with the specified context.
     *
     * @param context The Context in which the view is being used.
     */
    public CometChatCallActionBubble(Context context) {
        this(context, null);
    }

    /**
     * Creates a CometChatCallActionBubble instance with attributes defined in XML.
     *
     * @param context The Context in which the view is being used.
     * @param attrs   The attribute set containing customization options.
     */
    public CometChatCallActionBubble(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatCallActionBubbleStyle);
    }

    /**
     * Creates a CometChatCallActionBubble instance with XML attributes and a
     * default style.
     *
     * @param context      The Context in which the view is being used.
     * @param attrs        The attribute set containing customization options.
     * @param defStyleAttr The default style attribute to apply.
     */
    public CometChatCallActionBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(attrs, defStyleAttr);
    }

    /**
     * Initializes the view by inflating the layout and applying style attributes.
     *
     * @param attrs        The attribute set containing customization options.
     * @param defStyleAttr The default style attribute.
     */
    private void initializeView(AttributeSet attrs, int defStyleAttr) {
        Utils.initMaterialCard(this);
        binding = CometchatCallActionBubbleBinding.inflate(LayoutInflater.from(getContext()), this, true);
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Applies style attributes based on the XML layout or theme.
     *
     * @param attrs        The attribute set containing customization.
     * @param defStyleAttr The default style attribute.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatCallActionBubble, defStyleAttr, 0);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatCallActionBubble, defStyleAttr, style);
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
            setCornerRadius(typedArray.getDimensionPixelSize(R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleCornerRadius, 0));
            setStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleStrokeWidth, 0));
            setStrokeColor(typedArray.getColor(R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleStrokeColor,
                                               CometChatTheme.getStrokeColorDefault(getContext())));
            setBackgroundColor(typedArray.getColor(R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleBackgroundColor,
                                                   CometChatTheme.getBackgroundColor2(getContext())));
            setBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleBackgroundDrawable));

            setTextColor(typedArray.getColor(R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleTextColor,
                                             CometChatTheme.getTextColorSecondary(getContext())));
            setTextAppearance(typedArray.getResourceId(R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleTextAppearance, 0));

            setIconTint(typedArray.getColor(R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleIconTint,
                                            CometChatTheme.getIconTintSecondary(getContext())));
            setMissedCallTextColor(typedArray.getColor(R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleMissedCallTextColor,
                                                       CometChatTheme.getErrorColor(getContext())));
            setMissedCallTextAppearance(typedArray.getResourceId(
                R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleMissedCallTextAppearance,
                0
            ));
            setMissedCallBackgroundColor(typedArray.getColor(
                R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleMissedCallBackgroundColor,
                CometChatTheme.getBackgroundColor2(getContext())
            ));
            setMissedCallIconTint(typedArray.getColor(R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleMissedCallIconTint,
                                                      CometChatTheme.getErrorColor(getContext())));
            setMissedCallBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatCallActionBubble_cometchatCallActionBubbleMissedCallBackgroundDrawable));

        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Sets the stroke color of the bubble.
     *
     * @param strokeColor Color of the stroke.
     */
    @Override
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
    }

    /**
     * Retrieves the stroke color as a {@link ColorStateList}.
     *
     * @return A ColorStateList containing the stroke color.
     */
    @Override
    public ColorStateList getStrokeColorStateList() {
        return ColorStateList.valueOf(strokeColor);
    }

    /**
     * Updates the message display in the call action bubble based on the call
     * status.
     *
     * @param call The {@link Call} object containing call details.
     */
    public void setMessage(Call call) {
        binding.cometchatCallActionBubbleText.setTextColor(textColor);
        binding.cometchatCallActionBubbleText.setTextAppearance(textAppearance);
        binding.cometchatCallActionBubbleIcon.setColorFilter(iconTint);
        super.setCardBackgroundColor(backgroundColor);
        if (backgroundDrawable != null) {
            super.setBackgroundDrawable(backgroundDrawable);
        }
        super.setRadius(cornerRadius);
        super.setStrokeWidth(strokeWidth);
        super.setStrokeColor(strokeColor);

        binding.cometchatCallActionBubbleText.setText(CallUtils.getCallStatus(getContext(), call));
        User user = (User) call.getCallInitiator();
        boolean isVideoCall = call.getType().equals(CometChatConstants.CALL_TYPE_VIDEO);
        boolean isIncomingCall = user.getUid().equals(CometChatUIKit.getLoggedInUser().getUid());
        boolean isMissedCall = false;
        if (isVideoCall) {
            if (call.getCallStatus().equals(CometChatConstants.CALL_STATUS_INITIATED)) {
                if (isIncomingCall) {
                    binding.cometchatCallActionBubbleIcon.setImageResource(R.drawable.cometchat_ic_outgoing_video_call);
                } else {
                    binding.cometchatCallActionBubbleIcon.setImageResource(R.drawable.cometchat_ic_incoming_video_call);
                }
            } else if (call.getCallStatus().equals(UIKitConstants.CallStatusConstants.UNANSWERED)) {
                if (isIncomingCall) binding.cometchatCallActionBubbleIcon.setImageResource(R.drawable.cometchat_ic_video_call);
                else {
                    isMissedCall = true;
                    binding.cometchatCallActionBubbleIcon.setImageResource(R.drawable.cometchat_ic_missed_video_call);
                }
            } else {
                binding.cometchatCallActionBubbleIcon.setImageResource(R.drawable.cometchat_ic_video_call);
            }
        } else if (call.getType().equals(CometChatConstants.CALL_TYPE_AUDIO)) {
            if (call.getCallStatus().equals(CometChatConstants.CALL_STATUS_INITIATED)) {
                if (isIncomingCall) {
                    binding.cometchatCallActionBubbleIcon.setImageResource(R.drawable.cometchat_ic_outgoing_voice_call);
                } else {
                    binding.cometchatCallActionBubbleIcon.setImageResource(R.drawable.cometchat_ic_incoming_voice_call);
                }
            } else if (call.getCallStatus().equals(UIKitConstants.CallStatusConstants.UNANSWERED)) {
                if (isIncomingCall) binding.cometchatCallActionBubbleIcon.setImageResource(R.drawable.cometchat_ic_default_voice_call);
                else {
                    isMissedCall = true;
                    binding.cometchatCallActionBubbleIcon.setImageResource(R.drawable.cometchat_ic_missed_voice_call);
                }
            } else {
                binding.cometchatCallActionBubbleIcon.setImageResource(R.drawable.cometchat_ic_default_voice_call);
            }
        }

        if (isMissedCall) {
            binding.cometchatCallActionBubbleText.setTextColor(missedCallTextColor);
            binding.cometchatCallActionBubbleText.setTextAppearance(missedCallTextAppearance);
            binding.cometchatCallActionBubbleIcon.setColorFilter(missedCallIconTint);
            super.setCardBackgroundColor(missedCallBackgroundColor);
            if (missedCallBackgroundDrawable != null) {
                super.setBackgroundDrawable(missedCallBackgroundDrawable);
            }
        }
    }

    /**
     * Retrieves the background drawable for the bubble.
     *
     * @return The background drawable.
     */
    public Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    /**
     * Sets a custom drawable as the background for the bubble.
     *
     * @param backgroundDrawable Drawable resource for the bubble background.
     */
    @Override
    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        this.backgroundDrawable = backgroundDrawable;
    }    /**
     * Sets the stroke width of the bubble.
     *
     * @param strokeWidth Width of the stroke in pixels.
     */
    @Override
    public void setStrokeWidth(@Dimension int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    /**
     * Retrieves the binding for this bubble view.
     *
     * @return The binding instance for accessing the view components.
     */
    public CometchatCallActionBubbleBinding getBinding() {
        return binding;
    }

    /**
     * Retrieves the corner radius of the bubble.
     *
     * @return The corner radius in pixels.
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius for the call action bubble.
     *
     * @param cornerRadius The corner radius in pixels.
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    /**
     * Retrieves the background color of the bubble.
     *
     * @return The background color.
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color of the bubble.
     *
     * @param backgroundColor Background color for the bubble.
     */
    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Retrieves the text color of the bubble.
     *
     * @return The text color.
     */
    public int getTextColor() {
        return textColor;
    }

    /**
     * Sets the text color within the bubble.
     *
     * @param textColor Color of the bubble's text.
     */
    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
    }

    /**
     * Retrieves the text appearance style resource for the bubble's text.
     *
     * @return The resource ID of the text appearance style.
     */
    public int getTextAppearance() {
        return textAppearance;
    }

    /**
     * Sets the text appearance style for the text in the bubble.
     *
     * @param textAppearance Resource ID of the text appearance style.
     */
    public void setTextAppearance(@StyleRes int textAppearance) {
        this.textAppearance = textAppearance;
    }

    /**
     * Retrieves the tint color for the icon in the bubble.
     *
     * @return The icon tint color.
     */
    public int getIconTint() {
        return iconTint;
    }

    /**
     * Sets the tint color for the icon in the bubble.
     *
     * @param iconTint Color tint to apply to the icon.
     */
    public void setIconTint(@ColorInt int iconTint) {
        this.iconTint = iconTint;
    }

    /**
     * Retrieves the text color for missed call notifications.
     *
     * @return The missed call text color.
     */
    public int getMissedCallTextColor() {
        return missedCallTextColor;
    }

    /**
     * Sets the text color for missed call notifications in the bubble.
     *
     * @param missedCallTextColor Color to use for missed call text.
     */
    public void setMissedCallTextColor(@ColorInt int missedCallTextColor) {
        this.missedCallTextColor = missedCallTextColor;
    }

    /**
     * Retrieves the text appearance style resource for missed call notifications.
     *
     * @return The resource ID of the missed call text appearance style.
     */
    public int getMissedCallTextAppearance() {
        return missedCallTextAppearance;
    }

    /**
     * Sets the text appearance style for missed call text in the bubble.
     *
     * @param missedCallTextAppearance Resource ID of the missed call text appearance style.
     */
    public void setMissedCallTextAppearance(@StyleRes int missedCallTextAppearance) {
        this.missedCallTextAppearance = missedCallTextAppearance;
    }

    /**
     * Retrieves the background color for missed call notifications.
     *
     * @return The missed call background color.
     */
    public int getMissedCallBackgroundColor() {
        return missedCallBackgroundColor;
    }

    /**
     * Sets the background color for missed call notifications in the bubble.
     *
     * @param missedCallBackgroundColor Color to use for missed call background.
     */
    public void setMissedCallBackgroundColor(@ColorInt int missedCallBackgroundColor) {
        this.missedCallBackgroundColor = missedCallBackgroundColor;
    }

    /**
     * Retrieves the tint color for the icon in missed call notifications.
     *
     * @return The missed call icon tint color.
     */
    public int getMissedCallIconTint() {
        return missedCallIconTint;
    }    /**
     * Retrieves the stroke width of the bubble.
     *
     * @return The width of the stroke in pixels.
     */
    @Override
    public int getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Sets the icon tint color for missed call notifications in the bubble.
     *
     * @param missedCallIconTint Color tint to apply to missed call icon.
     */
    public void setMissedCallIconTint(@ColorInt int missedCallIconTint) {
        this.missedCallIconTint = missedCallIconTint;
    }

    /**
     * Retrieves the background drawable for missed call notifications.
     *
     * @return The missed call background drawable.
     */
    public Drawable getMissedCallBackgroundDrawable() {
        return missedCallBackgroundDrawable;
    }

    /**
     * Sets a custom drawable as the background for missed call notifications.
     *
     * @param missedCallBackgroundDrawable Drawable resource for missed call background.
     */
    public void setMissedCallBackgroundDrawable(Drawable missedCallBackgroundDrawable) {
        this.missedCallBackgroundDrawable = missedCallBackgroundDrawable;
    }

    /**
     * Retrieves the style resource ID applied to this bubble.
     *
     * @return The resource ID of the applied style.
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
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatCallActionBubble);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }






}

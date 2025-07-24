package com.cometchat.chatuikit.shared.views.messagereceipt;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.AppCompatImageView;

import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;

/**
 * CometChatMessageReceipt is a custom AppCompatImageView that represents the
 * message receipt
 *
 * <p>
 * It provides methods to customize the appearance of the message receipt
 *
 * <p>
 * such as setting the waiting icon, sent icon, delivered icon, read icon, error
 * icon, and their colors. Created on: 10 September 2024 Modified on:
 */
@SuppressWarnings("unused")
public class CometChatMessageReceipt extends AppCompatImageView {
    private static final String TAG = CometChatMessageReceipt.class.getSimpleName();


    private Receipt receiptState = Receipt.SENT;

    private Drawable messageReceiptWaitIcon;
    private Drawable messageReceiptSentIcon;
    private Drawable messageReceiptDeliveredIcon;
    private Drawable messageReceiptErrorIcon;
    private Drawable messageReceiptReadIcon;

    private @ColorInt int messageReceiptWaitIconTint;
    private @ColorInt int messageReceiptSentIconTint;
    private @ColorInt int messageReceiptDeliveredIconTint;
    private @ColorInt int messageReceiptErrorIconTint;
    private @ColorInt int messageReceiptReadIconTint;

    /**
     * Constructor for creating a CometChatMessageReceipt without any attribute.
     *
     * @param context The context in which the CometChatMessageReceipt is created.
     */
    public CometChatMessageReceipt(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Constructor for creating a CometChatMessageReceipt with attributes.
     *
     * @param context The context in which the CometChatMessageReceipt is created.
     * @param attrs   The attribute set containing the custom attributes.
     */
    public CometChatMessageReceipt(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatMessageReceiptStyle);
    }

    /**
     * Constructor for creating a CometChatMessageReceipt with attributes and a
     * default style.
     *
     * @param context      The context in which the CometChatMessageReceipt is created.
     * @param attrs        The attribute set containing the custom attributes.
     * @param defStyleAttr The default style resource.
     */
    public CometChatMessageReceipt(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            inflateAndInitializeView(attrs, defStyleAttr);
        }
    }

    /**
     * Inflates and initializes the view by setting up the layout, retrieving the
     * attributes, and applying styles.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void inflateAndInitializeView(AttributeSet attrs, int defStyleAttr) {
        applyStyleAttributes(attrs, defStyleAttr);
    }

    /**
     * Applies the style attributes from XML, allowing direct attribute overrides.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr The default style to apply to this view.
     */
    private void applyStyleAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray directAttributes = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatMessageReceipt, defStyleAttr, 0);
        @StyleRes int styleResId = directAttributes.getResourceId(R.styleable.CometChatMessageReceipt_cometchatMessageReceiptStyle, 0);
        directAttributes = styleResId != 0 ? getContext()
            .getTheme()
            .obtainStyledAttributes(attrs, R.styleable.CometChatMessageReceipt, defStyleAttr, styleResId) : null;
        extractAttributesAndApplyDefaults(directAttributes);
    }

    /**
     * Extracts the attributes and applies the default values if they are not set in
     * the XML.
     *
     * @param typedArray The TypedArray containing the attributes to be extracted.
     */
    private void extractAttributesAndApplyDefaults(TypedArray typedArray) {
        if (typedArray == null) return;
        try {
            // Extract attributes or apply default values
            // Icons
            messageReceiptWaitIcon = typedArray.getDrawable(R.styleable.CometChatMessageReceipt_cometchatMessageReceiptWaitIcon);
            messageReceiptSentIcon = typedArray.getDrawable(R.styleable.CometChatMessageReceipt_cometchatMessageReceiptSentIcon);
            messageReceiptDeliveredIcon = typedArray.getDrawable(R.styleable.CometChatMessageReceipt_cometchatMessageReceiptDeliveredIcon);
            messageReceiptReadIcon = typedArray.getDrawable(R.styleable.CometChatMessageReceipt_cometchatMessageReceiptReadIcon);
            messageReceiptErrorIcon = typedArray.getDrawable(R.styleable.CometChatMessageReceipt_cometchatMessageReceiptErrorIcon);
            // Colors
            messageReceiptWaitIconTint = typedArray.getColor(R.styleable.CometChatMessageReceipt_cometchatMessageReceiptWaitIconTint,
                                                             CometChatTheme.getIconTintSecondary(getContext()));
            messageReceiptSentIconTint = typedArray.getColor(R.styleable.CometChatMessageReceipt_cometchatMessageReceiptSentIconTint,
                                                             CometChatTheme.getIconTintSecondary(getContext()));
            messageReceiptDeliveredIconTint = typedArray.getColor(R.styleable.CometChatMessageReceipt_cometchatMessageReceiptDeliveredIconTint,
                                                                  CometChatTheme.getIconTintSecondary(getContext()));
            messageReceiptReadIconTint = typedArray.getColor(R.styleable.CometChatMessageReceipt_cometchatMessageReceiptReadIconTint,
                                                             CometChatTheme.getMessageReadColor(getContext()));
            messageReceiptErrorIconTint = typedArray.getColor(R.styleable.CometChatMessageReceipt_cometchatMessageReceiptErrorIconTint,
                                                              CometChatTheme.getErrorColor(getContext()));
            // Apply default styles
            applyDefault();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Applies the extracted or default values to the CometChatMessageReceipt.
     */
    private void applyDefault() {
        setMessageReceipt(receiptState);
    }

    /**
     * Sets the receipt icon based on the receipt status.
     *
     * @param receipt The receipt status.
     */
    public void setMessageReceipt(Receipt receipt) {
        receiptState = receipt;
        if (Receipt.SENT.equals(receipt))
            setReceiptIcon(messageReceiptSentIcon, messageReceiptSentIconTint);
        else if (Receipt.IN_PROGRESS.equals(receipt))
            setReceiptIcon(messageReceiptWaitIcon, messageReceiptWaitIconTint);
        else if (Receipt.ERROR.equals(receipt))
            setReceiptIcon(messageReceiptErrorIcon, messageReceiptErrorIconTint);
        else if (Receipt.READ.equals(receipt))
            setReceiptIcon(messageReceiptReadIcon, messageReceiptReadIconTint);
        else if (Receipt.DELIVERED.equals(receipt))
            setReceiptIcon(messageReceiptDeliveredIcon, messageReceiptDeliveredIconTint);
    }

    /**
     * Sets the receipt icon to the specified drawable.
     *
     * @param drawable The drawable to set as the receipt icon.
     */
    private void setReceiptIcon(Drawable drawable, @ColorInt int color) {
        if (drawable != null) setImageDrawable(drawable);
        setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Sets the style for the CometChatMessageReceipt view by applying a style
     * resource.
     *
     * @param style The style resource to apply.
     */
    public void setStyle(@StyleRes int style) {
        if (style != 0) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatMessageReceipt);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    /**
     * Gets the color of the waiting icon.
     *
     * @return The color of the waiting icon.
     */
    public @ColorInt int getMessageReceiptWaitIconTint() {
        return messageReceiptWaitIconTint;
    }

    /**
     * Sets the color of the waiting icon.
     *
     * @param messageReceiptWaitIconTint The color of the waiting icon.
     */
    public void setMessageReceiptWaitIconTint(@ColorInt int messageReceiptWaitIconTint) {
        this.messageReceiptWaitIconTint = messageReceiptWaitIconTint;
        setColorFilter(messageReceiptWaitIconTint, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Gets the color of the sent icon.
     *
     * @return The color of the sent icon.
     */
    public @ColorInt int getMessageReceiptSentIconTint() {
        return messageReceiptSentIconTint;
    }

    /**
     * Sets the color of the sent icon.
     *
     * @param messageReceiptSentIconTint The color of the sent icon.
     */
    public void setMessageReceiptSentIconTint(@ColorInt int messageReceiptSentIconTint) {
        this.messageReceiptSentIconTint = messageReceiptSentIconTint;
        setColorFilter(messageReceiptSentIconTint, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Gets the color of the delivered icon.
     *
     * @return The color of the delivered icon.
     */
    public @ColorInt int getMessageReceiptDeliveredIconTint() {
        return messageReceiptDeliveredIconTint;
    }

    /**
     * Sets the color of the delivered icon.
     *
     * @param messageReceiptDeliveredIconTint The color of the delivered icon.
     */
    public void setMessageReceiptDeliveredIconTint(@ColorInt int messageReceiptDeliveredIconTint) {
        this.messageReceiptDeliveredIconTint = messageReceiptDeliveredIconTint;
        setColorFilter(messageReceiptDeliveredIconTint, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Gets the color of the error icon.
     *
     * @return The color of the error icon.
     */
    public @ColorInt int getMessageReceiptErrorIconTint() {
        return messageReceiptErrorIconTint;
    }

    /**
     * Sets the color of the error icon.
     *
     * @param messageReceiptErrorIconTint The color of the error icon.
     */
    public void setMessageReceiptErrorIconTint(@ColorInt int messageReceiptErrorIconTint) {
        this.messageReceiptErrorIconTint = messageReceiptErrorIconTint;
        setColorFilter(messageReceiptErrorIconTint, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Gets the color of the read icon.
     *
     * @return The color of the read icon.
     */
    public @ColorInt int getMessageReceiptReadIconTint() {
        return messageReceiptReadIconTint;
    }

    /**
     * Sets the color of the read icon.
     *
     * @param messageReceiptReadIconTint The color of the read icon.
     */
    public void setMessageReceiptReadIconTint(@ColorInt int messageReceiptReadIconTint) {
        this.messageReceiptReadIconTint = messageReceiptReadIconTint;
        setColorFilter(messageReceiptReadIconTint, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Gets the wait icon drawable for the receipt.
     *
     * @return The drawable for the wait icon.
     */
    public Drawable getMessageReceiptWaitIcon() {
        return messageReceiptWaitIcon;
    }

    /**
     * Sets the wait icon drawable for the receipt.
     *
     * @param icon The drawable for the wait icon.
     */
    public void setMessageReceiptWaitIcon(Drawable icon) {
        if (icon != null) {
            this.messageReceiptWaitIcon = icon;
            setImageDrawable(icon);
        }
    }

    /**
     * Gets the read icon drawable for the receipt.
     *
     * @return The drawable for the read icon.
     */
    public Drawable getMessageReceiptReadIcon() {
        return messageReceiptReadIcon;
    }

    /**
     * Sets the read icon drawable for the receipt.
     *
     * @param icon The drawable for the read icon.
     */
    public void setMessageReceiptReadIcon(Drawable icon) {
        if (icon != null) {
            this.messageReceiptReadIcon = icon;
            setImageDrawable(icon);
        }
    }

    /**
     * Gets the delivered icon drawable for the receipt.
     *
     * @return The drawable for the delivered icon.
     */
    public Drawable getMessageReceiptDeliveredIcon() {
        return messageReceiptDeliveredIcon;
    }

    /**
     * Sets the delivered icon drawable for the receipt.
     *
     * @param icon The drawable for the delivered icon.
     */
    public void setMessageReceiptDeliveredIcon(Drawable icon) {
        if (icon != null) {
            this.messageReceiptDeliveredIcon = icon;
            setImageDrawable(icon);
        }
    }

    /**
     * Gets the sent icon drawable for the receipt.
     *
     * @return The drawable for the sent icon.
     */
    public Drawable getMessageReceiptSentIcon() {
        return messageReceiptSentIcon;
    }

    /**
     * Sets the sent icon drawable for the receipt.
     *
     * @param icon The drawable for the sent icon.
     */
    public void setMessageReceiptSentIcon(@Nullable Drawable icon) {
        if (icon != null) {
            this.messageReceiptSentIcon = icon;
            setImageDrawable(icon);
        }
    }

    /**
     * Gets the error icon drawable for the receipt.
     *
     * @return The drawable for the error icon.
     */
    public Drawable getMessageReceiptErrorIcon() {
        return messageReceiptErrorIcon;
    }

    /**
     * Sets the error icon drawable for the receipt.
     *
     * @param icon The drawable for the error icon.
     */
    public void setMessageReceiptErrorIcon(Drawable icon) {
        if (icon != null) {
            this.messageReceiptErrorIcon = icon;
            setImageDrawable(icon);
        }
    }
}

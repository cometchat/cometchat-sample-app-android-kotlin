package com.cometchat.chatuikit.shared.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.cometchat.chatuikit.databinding.CometchatSubtitleViewBinding;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.spans.MentionMovementMethod;
import com.cometchat.chatuikit.shared.views.messagereceipt.CometChatMessageReceipt;
import com.cometchat.chatuikit.shared.views.messagereceipt.Receipt;
import com.google.android.material.card.MaterialCardView;

/**
 * SubtitleView is a custom view that extends MaterialCardView and is used to
 * display various types of text information like the last message, typing
 * indicator, and message receipts.
 */
public class SubtitleView extends MaterialCardView {
    private static final String TAG = SubtitleView.class.getSimpleName();
    // Binding object to access the views defined in the layout file
    private CometchatSubtitleViewBinding binding;

    /**
     * Constructor used when creating the view programmatically.
     *
     * @param context The Context the view is running in, through which it can access
     *                the current theme, resources, etc.
     */
    public SubtitleView(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating the view from XML.
     *
     * @param context The Context the view is running in.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public SubtitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor called when inflating the view from XML with a default style
     * attribute.
     *
     * @param context      The Context the view is running in.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a
     *                     style resource that supplies default values for the view. Can be 0
     *                     to not look for defaults.
     */
    public SubtitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initializes the view by setting up the layout and customizing the
     * MaterialCardView properties.
     */
    private void init() {
        Utils.initMaterialCard(this); // Initialize MaterialCardView properties
        setStrokeWidth(0); // Set the stroke width of the card to 0
        setBackgroundColor(getResources().getColor(android.R.color.transparent, null)); // Set a transparent background
        binding = CometchatSubtitleViewBinding.inflate(LayoutInflater.from(getContext()), this, true); // Inflate the
        // layout
    }

    /**
     * Sets the text for the typing indicator.
     *
     * @param text The typing indicator text to display.
     */
    public void setTypingIndicatorText(String text) {
        if (text != null && !text.isEmpty()) {
            binding.tvTypingIndicator.setText(text);
        }
    }

    /**
     * Sets the text color for the typing indicator text view.
     *
     * @param color The color to set.
     */
    public void setTypingIndicatorTextColor(@ColorInt int color) {
        if (color != 0) {
            binding.tvTypingIndicator.setTextColor(color);
        }
    }

    /**
     * Sets the text appearance for the typing indicator text view.
     *
     * @param appearance The style resource to apply.
     */
    public void setTypingIndicatorTextAppearance(@StyleRes int appearance) {
        if (appearance != 0) {
            binding.tvTypingIndicator.setTextAppearance(appearance);
        }
    }

    /**
     * Shows or hides the typing indicator view.
     *
     * @param show If true, shows the typing indicator; otherwise, hides it and shows
     *             the subtitle container.
     */
    public void showTypingIndicator(boolean show) {
        binding.tvTypingIndicator.setVisibility(show ? VISIBLE : GONE);
        binding.subtitleContainer.setVisibility(show ? GONE : VISIBLE);
    }

    /**
     * Hides or shows the subtitle container view.
     *
     * @param hide If true, hides the subtitle container; otherwise, shows it.
     */
    public void showSubtitleViewContainer(boolean hide) {
        binding.subtitleContainer.setVisibility(hide ? VISIBLE : GONE);
    }

    /**
     * Sets the icon for the message receipt, which indicates the status of the
     * message (sent, delivered, read).
     *
     * @param receipt The Receipt object representing the message status.
     */
    public void setMessageReceiptIcon(Receipt receipt) {
        binding.messageReceipts.setMessageReceipt(receipt);
    }

    /**
     * Hides or shows the message receipt icon.
     *
     * @param hide If true, hides the message receipt icon; otherwise, shows it.
     */
    public void hideMessageReceiptIcon(boolean hide) {
        binding.messageReceipts.setVisibility(hide ? GONE : VISIBLE);
    }

    /**
     * Shows or hides the text message type view.
     *
     * @param show If true, shows the message type; otherwise, hides it.
     */
    public void showMessageTypeIconView(boolean show) {
        binding.ivMessageType.setVisibility(show ? VISIBLE : GONE);
    }

    public void setSenderNameText(String name) {
        binding.tvSenderName.setText(name);
    }

    public void setSenderNameTextColor(@ColorInt int color) {
        binding.tvSenderName.setTextColor(color);
    }

    public void setSenderNameTextAppearance(@StyleRes int appearance) {
        binding.tvSenderName.setTextAppearance(appearance);
    }

    public void hideSenderName(boolean hideSenderName) {
        binding.tvSenderName.setVisibility(hideSenderName ? GONE : VISIBLE);
    }

    /**
     * Sets the tint color for the message type icon. If the provided color is not
     * 0, it clears the message type text and applies the tint color to the message
     * type background.
     *
     * @param color The tint color to apply, represented as an integer color value. If
     *              the color is not 0, the message type text is cleared, and the
     *              background is tinted with the provided color.
     */
    public void setMessageTypeIconTint(@ColorInt int color) {
        if (color != 0) {
            binding.ivMessageType.setImageTintList(ColorStateList.valueOf(color));
        }
    }

    /**
     * Sets the text color for the last message text view.
     *
     * @param color The color to set.
     */
    public void setLastMessageTextColor(@ColorInt int color) {
        if (color != 0) {
            binding.tvLastMessageText.setTextColor(color);
        }
    }

    /**
     * Sets the text appearance for the last message text view.
     *
     * @param appearance The style resource to apply.
     */
    public void setLastMessageTextAppearance(@StyleRes int appearance) {
        if (appearance != 0) {
            binding.tvLastMessageText.setTextAppearance(appearance);
        }
    }

    /**
     * Sets the last message text to display.
     *
     * @param text The message text to display.
     */
    public void setLastMessageText(@Nullable String text) {
        if (text != null && !text.isEmpty()) {
            binding.tvLastMessageText.setVisibility(VISIBLE);
            binding.tvLastMessageText.setText(text);
        } else {
            binding.tvLastMessageText.setVisibility(GONE);
            binding.tvLastMessageText.setText("");
        }
    }

    /**
     * Sets the last message text using a SpannableString to allow for styled text.
     *
     * @param text The styled text to display.
     */
    public void setLastMessageText(SpannableString text) {
        if (text != null) {
            binding.tvLastMessageText.setVisibility(VISIBLE);
            binding.tvLastMessageText.setText(text);
            binding.tvLastMessageText.setMovementMethod(MentionMovementMethod.getInstance());
            binding.tvLastMessageText.setMaxLines(1);
            binding.tvLastMessageText.setEllipsize(TextUtils.TruncateAt.END);
        } else {
            binding.tvLastMessageText.setVisibility(GONE);
            binding.tvLastMessageText.setText("");
        }
    }

    /**
     * Gets the typing indicator TextView.
     *
     * @return The TextView displaying the typing indicator.
     */
    public TextView getTypingIndicatorView() {
        return binding.tvTypingIndicator;
    }

    /**
     * Gets the message type Icon.
     *
     * @return The ImageView displaying the message type icon.
     */
    @NonNull
    public ImageView getMessageTypeIcon() {
        return binding.ivMessageType;
    }

    /**
     * Sets the message type icon.
     *
     * @param icon The message type icon.
     */
    public void setMessageTypeIcon(Drawable icon) {
        binding.ivMessageType.setImageDrawable(icon);
    }

    /**
     * Gets the last message TextView.
     *
     * @return The TextView displaying the last message.
     */
    public TextView getLastMessageView() {
        return binding.tvLastMessageText;
    }

    /**
     * Gets the CometChatMessageReceipt view.
     *
     * @return The CometChatMessageReceipt displaying the message receipt status.
     */
    public CometChatMessageReceipt getCometChatMessageReceipt() {
        return binding.messageReceipts;
    }
}

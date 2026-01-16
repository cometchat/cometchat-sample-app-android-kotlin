package com.cometchat.chatuikit.extensions.sticker.bubble;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.google.android.material.card.MaterialCardView;

public class CometChatStickerBubble extends MaterialCardView {
    private static final String TAG = CometChatStickerBubble.class.getSimpleName();
    private ImageView stickerImageView;
    private String imageUrl;
    private LinearLayout rootLayout;
    private CustomMessage message;

    /**
     * Represents a bubble view for displaying stickers in the CometChat UI.
     *
     * <p>
     * This class provides constructors to create a sticker bubble with different
     * initialization options, allowing customization of styles and attributes. The
     * sticker bubble is designed to present sticker messages within a chat
     * interface.
     *
     * @param context The context of the application, typically an Activity or Fragment.
     */
    public CometChatStickerBubble(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new CometChatStickerBubble with specified attributes.
     *
     * @param context The context of the application, typically an Activity or Fragment.
     * @param attrs   A set of attributes from the XML that can be used to customize the
     *                view.
     */
    public CometChatStickerBubble(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.cometchatStickerBubbleStyle);
    }

    /**
     * Constructs a new CometChatStickerBubble with specified attributes and default
     * style.
     *
     * @param context      The context of the application, typically an Activity or Fragment.
     * @param attrs        A set of attributes from the XML that can be used to customize the
     *                     view.
     * @param defStyleAttr A reference to a default style resource that will be applied to
     *                     this view.
     */
    public CometChatStickerBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    /**
     * Initializes the view by inflating the layout and setting up initial
     * properties.
     *
     * @param context The context in which the view is created.
     */
    private void initialize(Context context) {
        // Inflate and set up the view
        Utils.initMaterialCard(this);
        View view = View.inflate(context, R.layout.cometchat_message_sticker_bubble, null);
        this.rootLayout = (LinearLayout) view;
        stickerImageView = view.findViewById(R.id.cometchat_sticker_bubble_image_view);
        // Add the inflated view to this view
        addView(view);
    }

    /**
     * Sets the drawable image for the sticker bubble.
     *
     * @param drawable The drawable to be set as the image for the sticker. If the
     *                 stickerImageView is not null, this drawable will be displayed.
     */
    public void setDrawable(Drawable drawable) {
        if (stickerImageView != null)
            stickerImageView.setImageDrawable(drawable);
    }

    /**
     * Loads and sets the sticker image from the provided URL into the sticker image
     * view.
     *
     * @param url The URL of the image to be loaded. If the stickerImageView is not
     *            null and the URL is valid, the image will be fetched using Glide
     *            and displayed.
     */
    public void setImageUrl(String url) {
        if (stickerImageView != null && url != null) {
            this.imageUrl = url;
            Glide.with(getContext()).load(url).skipMemoryCache(false).into(stickerImageView);
        }
    }

    /**
     * Sets the sticker image for the bubble based on the provided CustomMessage.
     *
     * @param message The CustomMessage object that contains the sticker URL in its
     *                custom data. This method attempts to retrieve the sticker URL from
     *                the message's custom data and sets it as the sticker image using
     *                {@link #setImageUrl(String)}. If the sticker URL cannot be
     *                retrieved due to a JSONException, the error is ignored.
     */
    public void setMessage(CustomMessage message) {
        try {
            setImageUrl(message.getCustomData().getString("sticker_url"));
        } catch (Exception ignored) {
        }
    }

    public void setAlignment(UIKitConstants.MessageBubbleAlignment alignment) {
        if (rootLayout == null) return;

        if (UIKitConstants.MessageBubbleAlignment.LEFT.equals(alignment)) {
            rootLayout.setGravity(Gravity.START);
        } else if (UIKitConstants.MessageBubbleAlignment.RIGHT.equals(alignment)) {
            rootLayout.setGravity(Gravity.END);
        }
    }

    public CustomMessage getMessage() {
        return message;
    }

    public ImageView getStickerImageView() {
        return stickerImageView;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

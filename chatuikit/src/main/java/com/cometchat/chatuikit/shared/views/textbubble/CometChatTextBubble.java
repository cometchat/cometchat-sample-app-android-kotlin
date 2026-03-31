package com.cometchat.chatuikit.shared.views.textbubble;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;
import androidx.core.widget.TextViewCompat;

import com.bumptech.glide.Glide;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chatuikit.CometChatTheme;
import com.cometchat.chatuikit.R;
import com.cometchat.chatuikit.extensions.ExtensionConstants;
import com.cometchat.chatuikit.extensions.Extensions;
import com.cometchat.chatuikit.logger.CometChatLogger;
import com.cometchat.chatuikit.shared.constants.UIKitConstants;
import com.cometchat.chatuikit.shared.formatters.CometChatTextFormatter;
import com.cometchat.chatuikit.shared.formatters.FormatterUtils;
import com.cometchat.chatuikit.shared.resources.utils.Utils;
import com.cometchat.chatuikit.shared.spans.LinkFormatSpan;
import com.cometchat.chatuikit.shared.spans.MentionMovementMethod;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A custom view that represents a text bubble used for displaying messages in a
 * chat interface. It provides several customization options such as text color,
 * background color, font, border styling, and more. This class extends
 * {@link MaterialCardView} to provide rich material design support.
 */
public class CometChatTextBubble extends MaterialCardView {
    private static final String TAG = CometChatTextBubble.class.getSimpleName();
    private TextView messageTextView, separator, translateTextView, textTranslatedTextView, editedTextView, headingTextView, descriptionTextView, linkTextView;
    private LinearLayout translationContainer, linkPreviewContainer, parentViewLayout;
    private MaterialCardView linkMessageContainerCard;
    private ImageView bannerPreviewImageView, fabIconImageView;

    private @ColorInt int textColor;
    private @ColorInt int textLinkColor;
    private @ColorInt int separatorColor;
    private @ColorInt int translatedTextColor;
    private @StyleRes int translatedTextAppearance;
    private @ColorInt int linkPreviewTitleColor;
    private @StyleRes int linkPreviewTitleAppearance;
    private @ColorInt int linkPreviewDescriptionColor;
    private @StyleRes int linkPreviewDescriptionAppearance;
    private @ColorInt int linkPreviewLinkColor;
    private @StyleRes int linkPreviewLinkAppearance;
    private @ColorInt int linkPreviewBackgroundColor;
    private Drawable linkPreviewBackgroundDrawable;
    private @ColorInt int linkPreviewStrokeColor;
    private @ColorInt int linkPreviewStrokeWidth;
    private @ColorInt int linkPreviewCornerRadius;
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
    public CometChatTextBubble(Context context) {
        this(context, null);
    }

    /**
     * Constructor for initializing the CometChatTextBubble with a context and
     * attribute set.
     *
     * @param context The context in which this view is used.
     * @param attrs   The attribute set for styling this view.
     */
    public CometChatTextBubble(Context context, AttributeSet attrs) {
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
    public CometChatTextBubble(Context context, AttributeSet attrs, int defStyleAttr) {
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
        View view = View.inflate(getContext(), R.layout.cometchat_text_bubble, null);
        messageTextView = view.findViewById(R.id.cometchat_text_bubble_text_view);
        separator = view.findViewById(R.id.separator);
        translateTextView = view.findViewById(R.id.translate_message);
        textTranslatedTextView = view.findViewById(R.id.text_translated);
        editedTextView = view.findViewById(R.id.text_edited);
        translationContainer = view.findViewById(R.id.translation_message_container);
        linkPreviewContainer = view.findViewById(R.id.link_Message_container);
        linkMessageContainerCard = view.findViewById(R.id.link_message_card_container);
        parentViewLayout = view.findViewById(R.id.text_bubble_parent_view);
        Utils.initMaterialCard(linkMessageContainerCard);
        bannerPreviewImageView = view.findViewById(R.id.preview_banner);
        fabIconImageView = view.findViewById(R.id.fab_icon_image);
        headingTextView = view.findViewById(R.id.link_heading);
        descriptionTextView = view.findViewById(R.id.link_description);
        linkTextView = view.findViewById(R.id.link);

        messageTextView.setOnLongClickListener(v -> {
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
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatTextBubble, defStyleAttr, defStyleRes);
        @StyleRes int style = typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleStyle, 0);
        typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CometChatTextBubble, defStyleAttr, style);
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
            setTextAppearance(typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleTextAppearance, 0));
            setTextColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleTextColor, 0));
            setSeparatorColor(typedArray.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleSeparatorColor,
                    CometChatTheme.getExtendedPrimaryColor800(getContext())
            ));
            setTranslatedTextAppearance(typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleTranslatedTextAppearance, 0));
            setTranslatedTextColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleTranslatedTextColor, 0));
            setLinkPreviewTitleAppearance(typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewTitleAppearance, 0));
            setLinkPreviewTitleColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewTitleColor, 0));
            setLinkPreviewDescriptionColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewDescriptionColor, 0));
            setLinkPreviewDescriptionAppearance(typedArray.getResourceId(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewDescriptionAppearance,
                    0
            ));
            setLinkPreviewLinkAppearance(typedArray.getResourceId(R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewLinkAppearance, 0));
            setLinkPreviewLinkColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewLinkColor, 0));
            setLinkPreviewBackgroundDrawable(typedArray.getDrawable(R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewBackgroundDrawable));
            setLinkPreviewBackgroundColor(typedArray.getColor(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewBackgroundColor,
                    CometChatTheme.getExtendedPrimaryColor900(getContext())
            ));
            setLinkPreviewStrokeColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewStrokeColor, 0));
            setLinkPreviewStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewStrokeWidth, 0));
            setLinkPreviewCornerRadius(typedArray.getDimensionPixelSize(
                    R.styleable.CometChatTextBubble_cometchatTextBubbleLinkPreviewCornerRadius,
                    0
            ));
            setTextLinkColor(typedArray.getColor(R.styleable.CometChatTextBubble_cometchatTextBubbleTextLinkColor, 0));
        } finally {
            typedArray.recycle();
        }
    }

    public void setEditedTextColor(@ColorInt int color) {
        editedTextView.setTextColor(color);
    }

    public void setMessage(TextMessage message, List<CometChatTextFormatter> textFormatters, UIKitConstants.MessageBubbleAlignment alignment) {
        if (message != null) {
            setText(SpannableString.valueOf(FormatterUtils.getFormattedText(
                    getContext(),
                    message,
                    UIKitConstants.FormattingType.MESSAGE_BUBBLE,
                    alignment,
                    message.getText(),
                    textFormatters != null ? textFormatters : new ArrayList<>()
            )));
            linkPreviewContainer.setVisibility(GONE);
            editedTextView.setVisibility(message.getEditedAt() == 0 ? View.GONE : View.VISIBLE);
            HashMap<String, JSONObject> extensionList = Extensions.extensionCheck(message);
            if (extensionList != null) {
                if (extensionList.containsKey(ExtensionConstants.ExtensionJSONField.LINK_PREVIEW)) {
                    JSONObject linkPreviewJsonObject = extensionList.get(ExtensionConstants.ExtensionJSONField.LINK_PREVIEW);
                    try {
                        if (linkPreviewJsonObject != null) {
                            linkPreviewContainer.setVisibility(View.VISIBLE);
                            String strDescription = linkPreviewJsonObject.getString(ExtensionConstants.ExtensionJSONField.DESCRIPTION);
                            String strImage = linkPreviewJsonObject.getString(ExtensionConstants.ExtensionJSONField.IMAGE);
                            String strTitle = linkPreviewJsonObject.getString(ExtensionConstants.ExtensionJSONField.TITLE);
                            String url = linkPreviewJsonObject.getString(ExtensionConstants.ExtensionJSONField.URL);
                            String favIcon = linkPreviewJsonObject.getString(ExtensionConstants.ExtensionJSONField.FAV_ICON);
                            setLinkPreview(strTitle, strDescription, url, strImage, favIcon);
                            return;
                        } else {
                            linkPreviewContainer.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        linkPreviewContainer.setVisibility(View.GONE);
                        CometChatLogger.e(TAG, e.toString());
                    }
                } else {
                    if (message.getMetadata() != null && message.getMetadata().has(ExtensionConstants.ExtensionJSONField.MESSAGE_TRANSLATED)) {
                        try {
                            String translatedStr = message.getMetadata().getString(ExtensionConstants.ExtensionJSONField.MESSAGE_TRANSLATED);
                            if (!translatedStr.isEmpty()) {
                                String translatedMessage = String.valueOf(FormatterUtils.getFormattedText(
                                        getContext(),
                                        message,
                                        UIKitConstants.FormattingType.MESSAGE_BUBBLE,
                                        alignment,
                                        translatedStr,
                                        textFormatters != null ? textFormatters : new ArrayList<>()
                                ));
                                setTranslatedText(translatedMessage);
                                translationContainer.setVisibility(View.VISIBLE);
                            } else {
                                translationContainer.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                            translationContainer.setVisibility(View.GONE);
                        }
                    } else {
                        translationContainer.setVisibility(View.GONE);
                    }
                    linkPreviewContainer.setVisibility(View.GONE);
                    linkPreviewContainer.setVisibility(View.GONE);
                }
            }
        }
        resetWidth();
    }

    /**
     * Sets the text content of the text bubble using a {@link SpannableString}.
     *
     * @param text The spannable string to display in the bubble.
     */
    public void setText(SpannableString text) {
        messageTextView.setText(text, TextView.BufferType.SPANNABLE);
        messageTextView.setMovementMethod(MentionMovementMethod.getInstance());
        // Apply the stored link color to any LinkFormatSpan instances in the new text
        if (textLinkColor != 0) {
            updateLinkFormatSpanColors(textLinkColor);
        }
    }

    public void setLinkPreview(String title, String description, String url, String bannerImage, String fabIcon) {
        headingTextView.setText(title);
        descriptionTextView.setText(description);
        linkTextView.setText(url);
        if (bannerImage == null || bannerImage.isEmpty()) {
            bannerPreviewImageView.setVisibility(View.GONE);
            if (fabIcon != null && !fabIcon.isEmpty()) {
                fabIconImageView.setVisibility(View.VISIBLE);
                Glide.with(getContext()).load(fabIcon).placeholder(R.drawable.cometchat_image_placeholder).skipMemoryCache(false).into(
                        fabIconImageView);
            } else {
                fabIconImageView.setVisibility(View.GONE);
            }
        } else {
            fabIconImageView.setVisibility(View.GONE);
            bannerPreviewImageView.setVisibility(View.VISIBLE);
            Glide.with(getContext()).load(bannerImage).placeholder(R.drawable.cometchat_image_placeholder).skipMemoryCache(false).into(
                    bannerPreviewImageView);
        }
        adjustWidthForLinkPreview();
    }

    public void setTranslatedText(String text) {
        translateTextView.setText(text);
    }

    private void resetWidth() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        parentViewLayout.setLayoutParams(layoutParams);
    }

    private void adjustWidthForLinkPreview() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                getContext()
                        .getResources()
                        .getDimensionPixelSize(R.dimen.cometchat_240dp),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        parentViewLayout.setLayoutParams(layoutParams);
    }

    /**
     * Sets the text content of the text bubble.
     *
     * @param text The text to display in the bubble.
     */
    public void setText(String text) {
        messageTextView.setText(text);
    }

    /**
     * Gets the {@link TextView} associated with this text bubble.
     *
     * @return The TextView within the bubble.
     */
    public TextView getTextView() {
        return messageTextView;
    }

    /**
     * Sets compound drawables (icons) around the text within the bubble.
     *
     * @param start  The drawable resource ID for the start (left) drawable.
     * @param top    The drawable resource ID for the top drawable.
     * @param end    The drawable resource ID for the end (right) drawable.
     * @param bottom The drawable resource ID for the bottom drawable.
     */
    public void setCompoundDrawable(@DrawableRes int start, @DrawableRes int top, @DrawableRes int end, @DrawableRes int bottom) {
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(messageTextView, start, top, end, bottom);
    }

    /**
     * Sets the tint color for the compound drawables around the text.
     *
     * @param color The color to apply to the drawables.
     */
    public void setCompoundDrawableIconTint(@ColorInt int color) {
        TextViewCompat.setCompoundDrawableTintList(messageTextView, ColorStateList.valueOf(color));
    }

    /**
     * Sets the margin around the {@link TextView} inside the text bubble.
     *
     * @param leftMargin   The left margin, in dp.
     * @param topMargin    The top margin, in dp.
     * @param rightMargin  The right margin, in dp.
     * @param bottomMargin The bottom margin, in dp.
     */
    public void setTextViewMargin(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        ViewGroup.LayoutParams layoutParams = messageTextView.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            marginLayoutParams.setMargins(
                    leftMargin > -1 ? Utils.convertDpToPx(getContext(), leftMargin) : 0,
                    topMargin > -1 ? Utils.convertDpToPx(getContext(), topMargin) : 0,
                    rightMargin > -1 ? Utils.convertDpToPx(getContext(), rightMargin) : 0,
                    bottomMargin > -1 ? Utils.convertDpToPx(getContext(), bottomMargin) : 0
            );
            messageTextView.setLayoutParams(marginLayoutParams);
        }
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
        messageTextView.setTextColor(color);
    }

    public int getTextAppearance() {
        return textAppearance;
    }

    /**
     * Sets the text appearance (style) of the text bubble.
     *
     * @param appearance The resource ID of the text appearance.
     */
    public void setTextAppearance(@StyleRes int appearance) {
        textAppearance = appearance;
        messageTextView.setTextAppearance(appearance);
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
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(style, R.styleable.CometChatTextBubble);
            extractAttributesAndApplyDefaults(typedArray);
        }
    }

    public int getTextLinkColor() {
        return textLinkColor;
    }

    public void setTextLinkColor(@ColorInt int color) {
        this.textLinkColor = color;
        messageTextView.setLinkTextColor(color);
        // Also update LinkFormatSpan instances in the text to override their default color
        updateLinkFormatSpanColors(color);
    }

    /**
     * Updates the color of all LinkFormatSpan instances in the message text.
     * This ensures that links created by MarkdownConverter use the correct color
     * based on the bubble style (white for outgoing, blue for incoming).
     *
     * @param color The color to apply to link spans.
     */
    private void updateLinkFormatSpanColors(@ColorInt int color) {
        CharSequence text = messageTextView.getText();
        if (text instanceof Spannable) {
            Spannable spannable = (Spannable) text;
            LinkFormatSpan[] linkSpans = spannable.getSpans(0, spannable.length(), LinkFormatSpan.class);
            for (LinkFormatSpan span : linkSpans) {
                span.setLinkColor(color);
            }
            // Force redraw to apply the new colors
            messageTextView.invalidate();
        }
    }

    public int getSeparatorColor() {
        return separatorColor;
    }    /**
     * Sets the border width of the text bubble.
     *
     * @param width The width of the border, in pixels.
     */
    public void setStrokeWidth(@Dimension int width) {
        strokeWidth = width;
        super.setStrokeWidth(width);
    }

    public void setSeparatorColor(@ColorInt int color) {
        this.separatorColor = color;
        separator.setBackgroundColor(color);
    }

    public int getTranslatedTextColor() {
        return translatedTextColor;
    }

    public void setTranslatedTextColor(@ColorInt int color) {
        this.translatedTextColor = color;
        translateTextView.setTextColor(color);
        textTranslatedTextView.setTextColor(color);
        setEditedTextColor(color);
    }    /**
     * Sets the border color of the text bubble.
     *
     * @param color The color to set for the border.
     */
    public void setStrokeColor(@ColorInt int color) {
        strokeColor = color;
        super.setStrokeColor(color);
    }

    public int getTranslatedTextAppearance() {
        return translatedTextAppearance;
    }

    public void setTranslatedTextAppearance(@StyleRes int appearance) {
        this.translatedTextAppearance = appearance;
        translateTextView.setTextAppearance(appearance);
    }

    public int getLinkPreviewTitleColor() {
        return linkPreviewTitleColor;
    }

    public void setLinkPreviewTitleColor(@ColorInt int color) {
        this.linkPreviewTitleColor = color;
        headingTextView.setTextColor(color);
    }

    public int getLinkPreviewTitleAppearance() {
        return linkPreviewTitleAppearance;
    }

    public void setLinkPreviewTitleAppearance(@StyleRes int appearance) {
        this.linkPreviewTitleAppearance = appearance;
        headingTextView.setTextAppearance(appearance);
    }

    public int getLinkPreviewDescriptionColor() {
        return linkPreviewDescriptionColor;
    }

    public void setLinkPreviewDescriptionColor(@ColorInt int color) {
        this.linkPreviewDescriptionColor = color;
        descriptionTextView.setTextColor(color);
    }

    public int getLinkPreviewDescriptionAppearance() {
        return linkPreviewDescriptionAppearance;
    }

    public void setLinkPreviewDescriptionAppearance(@StyleRes int appearance) {
        this.linkPreviewDescriptionAppearance = appearance;
        descriptionTextView.setTextAppearance(appearance);
    }

    public int getLinkPreviewLinkColor() {
        return linkPreviewLinkColor;
    }

    public void setLinkPreviewLinkColor(@ColorInt int color) {
        this.linkPreviewLinkColor = color;
        linkTextView.setTextColor(color);
    }

    public int getLinkPreviewLinkAppearance() {
        return linkPreviewLinkAppearance;
    }

    public void setLinkPreviewLinkAppearance(@StyleRes int appearance) {
        this.linkPreviewLinkAppearance = appearance;
        linkTextView.setTextAppearance(appearance);
    }

    public int getLinkPreviewBackgroundColor() {
        return linkPreviewBackgroundColor;
    }

    public void setLinkPreviewBackgroundColor(@ColorInt int color) {
        this.linkPreviewBackgroundColor = color;
        linkMessageContainerCard.setCardBackgroundColor(color);
    }    public int getStrokeWidth() {
        return strokeWidth;
    }

    public Drawable getLinkPreviewBackgroundDrawable() {
        return linkPreviewBackgroundDrawable;
    }

    public void setLinkPreviewBackgroundDrawable(Drawable drawable) {
        this.linkPreviewBackgroundDrawable = drawable;
        if (drawable != null) linkMessageContainerCard.setBackground(drawable);
    }

    public int getLinkPreviewStrokeColor() {
        return linkPreviewStrokeColor;
    }    public int getStrokeColor() {
        return strokeColor;
    }

    public void setLinkPreviewStrokeColor(@ColorInt int color) {
        this.linkPreviewStrokeColor = color;
        linkMessageContainerCard.setStrokeColor(color);
    }

    public int getLinkPreviewStrokeWidth() {
        return linkPreviewStrokeWidth;
    }

    public void setLinkPreviewStrokeWidth(@Dimension int width) {
        this.linkPreviewStrokeWidth = width;
        linkMessageContainerCard.setStrokeWidth(width);
    }

    public int getLinkPreviewCornerRadius() {
        return linkPreviewCornerRadius;
    }

    public void setLinkPreviewCornerRadius(@Dimension int radius) {
        this.linkPreviewCornerRadius = radius;
        linkMessageContainerCard.setRadius(radius);
    }









}

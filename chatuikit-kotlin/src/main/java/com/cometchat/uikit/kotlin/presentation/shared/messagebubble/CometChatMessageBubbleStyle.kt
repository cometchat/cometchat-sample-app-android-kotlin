package com.cometchat.uikit.kotlin.presentation.shared.messagebubble

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.aiassistantbubble.CometChatAIAssistantBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiobubble.CometChatAudioBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.collaborativebubble.CometChatCollaborativeBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.deletebubble.CometChatDeleteBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filebubble.CometChatFileBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagebubble.CometChatImageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.meetcallbubble.CometChatMeetCallBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.pollbubble.CometChatPollBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.stickerbubble.CometChatStickerBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.textbubble.CometChatTextBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.videobubble.CometChatVideoBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagepreview.CometChatMessagePreviewStyle
import com.cometchat.uikit.kotlin.presentation.shared.moderation.CometChatModerationViewStyle
import com.cometchat.uikit.kotlin.presentation.shared.reaction.CometChatReactionStyle
import com.cometchat.uikit.kotlin.presentation.shared.receipts.CometChatReceiptStyle
import com.cometchat.uikit.kotlin.shared.formatters.style.CometChatMentionStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatMessageBubble component.
 *
 * This data class holds all styling properties for the message bubble view,
 * matching the XML attributes defined in attr_cometchat_message_bubble.xml.
 * Properties include bubble container styling, thread indicator, sender name,
 * timestamp styling, and sub-component styles.
 */
data class CometChatMessageBubbleStyle(
    // Bubble container styling
    @ColorInt val backgroundColor: Int = 0,
    val backgroundDrawable: Drawable? = null,
    @Dimension val cornerRadius: Float = 0f,
    @Dimension val strokeWidth: Float = 0f,
    @ColorInt val strokeColor: Int = 0,

    // Sender name styling
    @StyleRes val senderNameTextAppearance: Int = 0,
    @ColorInt val senderNameTextColor: Int = 0,

    // Thread indicator styling
    @StyleRes val threadIndicatorTextAppearance: Int = 0,
    @ColorInt val threadIndicatorTextColor: Int = 0,
    @ColorInt val threadIndicatorIconTint: Int = 0,

    // Timestamp styling
    @StyleRes val timestampTextAppearance: Int = 0,
    @ColorInt val timestampTextColor: Int = 0,

    val mentionStyle: CometChatMentionStyle? = null,
    val reactionStyle: CometChatReactionStyle? = null,
    val moderationViewStyle: CometChatModerationViewStyle? = null,
    val messagePreviewStyle: CometChatMessagePreviewStyle? = null,
    val dateStyle: CometChatDateStyle? = null,
    val messageReceiptStyle: CometChatReceiptStyle? = null,

    // Sub-component styles (typed style objects)
    val avatarStyle: CometChatAvatarStyle? = null,
    val textBubbleStyle: CometChatTextBubbleStyle? = null,
    val imageBubbleStyle: CometChatImageBubbleStyle? = null,
    val videoBubbleStyle: CometChatVideoBubbleStyle? = null,
    val fileBubbleStyle: CometChatFileBubbleStyle? = null,
    val audioBubbleStyle: CometChatAudioBubbleStyle? = null,
    val deleteBubbleStyle: CometChatDeleteBubbleStyle? = null,
    val stickerBubbleStyle: CometChatStickerBubbleStyle? = null,
    val pollBubbleStyle: CometChatPollBubbleStyle? = null,
    val collaborativeBubbleStyle: CometChatCollaborativeBubbleStyle? = null,
    val meetCallBubbleStyle: CometChatMeetCallBubbleStyle? = null,
    val aiAssistantBubbleStyle: CometChatAIAssistantBubbleStyle? = null

) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatOutgoingMessageBubbleStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatMessageBubbleStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatMessageBubbleStyle {
            return outgoing(context)
        }

        /**
         * Creates a style for outgoing messages by extracting values from the theme's
         * outgoing message bubble style.
         *
         * @param context The context to access theme resources
         * @return A CometChatMessageBubbleStyle configured for outgoing messages
         */
        fun outgoing(context: Context): CometChatMessageBubbleStyle {
            return extractFromThemeStyle(
                context,
                R.attr.cometchatOutgoingMessageBubbleStyle,
                CometChatTheme.getPrimaryColor(context)
            )
        }

        /**
         * Creates a style for incoming messages by extracting values from the theme's
         * incoming message bubble style.
         *
         * @param context The context to access theme resources
         * @return A CometChatMessageBubbleStyle configured for incoming messages
         */
        fun incoming(context: Context): CometChatMessageBubbleStyle {
            return extractFromThemeStyle(
                context,
                R.attr.cometchatIncomingMessageBubbleStyle,
                CometChatTheme.getNeutralColor300(context)
            )
        }

        /**
         * Extracts the message bubble style from a theme style attribute.
         *
         * @param context The context to access theme resources
         * @param themeStyleAttr The attribute reference to the message bubble style in theme
         * @param defaultBackgroundColor The default background color to use when not specified in theme
         * @return A CometChatMessageBubbleStyle with values from the theme style
         */
        private fun extractFromThemeStyle(
            context: Context,
            themeStyleAttr: Int,
            defaultBackgroundColor: Int
        ): CometChatMessageBubbleStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(themeStyleAttr))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()

            return extractFromStyleResource(context, styleResId, defaultBackgroundColor)
        }

        /**
         * Extracts style values from a specific style resource ID.
         * If styleResId is 0, returns a style with CometChatTheme defaults.
         *
         * @param context The context to access theme resources
         * @param styleResId The style resource ID to extract from (0 for defaults only)
         * @param defaultBackgroundColor The default background color to use when not specified
         * @return A CometChatMessageBubbleStyle with values from the style resource or defaults
         */
        private fun extractFromStyleResource(
            context: Context,
            styleResId: Int,
            defaultBackgroundColor: Int
        ): CometChatMessageBubbleStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null, defaultBackgroundColor)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatMessageBubble)
            return try {
                extractFromTypedArray(context, typedArray, defaultBackgroundColor)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a style by extracting values from XML TypedArray.
         *
         * This method handles TypedArray recycling internally using try-finally.
         * Uses getBackgroundColor3 as the default background color for backward compatibility.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray containing XML attribute values (will be recycled)
         * @return A CometChatMessageBubbleStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatMessageBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray, CometChatTheme.getBackgroundColor3(context))
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a style for incoming messages by extracting values from XML TypedArray.
         *
         * This method handles TypedArray recycling internally using try-finally.
         * Uses getNeutralColor300 as the default background color for incoming messages.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray containing XML attribute values (will be recycled)
         * @return A CometChatMessageBubbleStyle configured for incoming messages
         */
        fun fromTypedArrayIncoming(context: Context, typedArray: TypedArray): CometChatMessageBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray, CometChatTheme.getNeutralColor300(context))
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a style for outgoing messages by extracting values from XML TypedArray.
         *
         * This method handles TypedArray recycling internally using try-finally.
         * Uses getPrimaryColor as the default background color for outgoing messages.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray containing XML attribute values (will be recycled)
         * @return A CometChatMessageBubbleStyle configured for outgoing messages
         */
        fun fromTypedArrayOutgoing(context: Context, typedArray: TypedArray): CometChatMessageBubbleStyle {
            return try {
                extractFromTypedArray(context, typedArray, CometChatTheme.getPrimaryColor(context))
            } finally {
                typedArray.recycle()
            }
        }


        /**
         * Core extraction method that reads style values from a TypedArray.
         * Does NOT handle recycling - caller is responsible for recycling the TypedArray.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray to extract from, or null for defaults only
         * @param defaultBackgroundColor The default background color to use when not specified in TypedArray
         * @return A CometChatMessageBubbleStyle with extracted values or theme defaults
         */
        private fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?,
            defaultBackgroundColor: Int
        ): CometChatMessageBubbleStyle {
            // Extract sub-component style resource IDs
            val dateStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatMessageBubbleDateStyle, 0
            ) ?: 0
            val receiptStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatMessageBubbleMessageReceiptStyle, 0
            ) ?: 0
            val avatarStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatMessageBubbleAvatarStyle, 0
            ) ?: 0
            val textBubbleStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatTextBubbleStyle, 0
            ) ?: 0
            val imageBubbleStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatImageBubbleStyle, 0
            ) ?: 0
            val videoBubbleStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatVideoBubbleStyle, 0
            ) ?: 0
            val fileBubbleStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatFileBubbleStyle, 0
            ) ?: 0
            val audioBubbleStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatAudioBubbleStyle, 0
            ) ?: 0
            val deleteBubbleStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatDeleteBubbleStyle, 0
            ) ?: 0
            val stickerBubbleStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatStickerBubbleStyle, 0
            ) ?: 0
            val pollBubbleStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatPollBubbleStyle, 0
            ) ?: 0
            val collaborativeBubbleStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatCollaborativeBubbleStyle, 0
            ) ?: 0
            val meetCallBubbleStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageBubble_cometchatMeetCallBubbleStyle, 0
            ) ?: 0

            return CometChatMessageBubbleStyle(
                // Bubble container styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageBubble_cometchatMessageBubbleBackgroundColor,
                    defaultBackgroundColor
                ) ?: defaultBackgroundColor,
                backgroundDrawable = typedArray?.getDrawable(
                    R.styleable.CometChatMessageBubble_cometchatMessageBubbleBackgroundDrawable
                ),
                cornerRadius = typedArray?.getDimension(
                    R.styleable.CometChatMessageBubble_cometchatMessageBubbleCornerRadius,
                    0f
                ) ?: 0f,
                strokeWidth = typedArray?.getDimension(
                    R.styleable.CometChatMessageBubble_cometchatMessageBubbleStrokeWidth,
                    0f
                ) ?: 0f,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatMessageBubble_cometchatMessageBubbleStrokeColor,
                    0
                ) ?: 0,

                // Sender name styling
                senderNameTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageBubble_cometchatMessageBubbleSenderNameTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Medium(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Medium(context),
                senderNameTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageBubble_cometchatMessageBubbleSenderNameTextColor,
                    CometChatTheme.getTextColorHighlight(context)
                ) ?: CometChatTheme.getTextColorHighlight(context),

                // Thread indicator styling
                threadIndicatorTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageBubble_cometchatMessageBubbleThreadIndicatorTextAppearance,
                    CometChatTheme.getTextAppearanceCaption1Regular(context)
                ) ?: CometChatTheme.getTextAppearanceCaption1Regular(context),
                threadIndicatorTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageBubble_cometchatMessageBubbleThreadIndicatorTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                threadIndicatorIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageBubble_cometchatMessageBubbleThreadIndicatorIconTint,
                    CometChatTheme.getIconTintSecondary(context)
                ) ?: CometChatTheme.getIconTintSecondary(context),

                // Timestamp styling
                timestampTextAppearance = CometChatTheme.getTextAppearanceCaption1Regular(context),
                timestampTextColor = CometChatTheme.getTextColorSecondary(context),

                // Sub-component styles (parse from resource IDs)
                dateStyle = if (dateStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(dateStyleRes, R.styleable.CometChatDate)
                    CometChatDateStyle.fromTypedArray(context, ta)
                } else null,
                messageReceiptStyle = if (receiptStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(receiptStyleRes, R.styleable.CometChatMessageReceipt)
                    CometChatReceiptStyle.fromTypedArray(context, ta)
                } else null,
                avatarStyle = if (avatarStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(avatarStyleRes, R.styleable.CometChatAvatar)
                    CometChatAvatarStyle.fromTypedArray(context, ta)
                } else null,
                textBubbleStyle = if (textBubbleStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(textBubbleStyleRes, R.styleable.CometChatTextBubble)
                    CometChatTextBubbleStyle.fromTypedArray(context, ta)
                } else null,
                imageBubbleStyle = if (imageBubbleStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(imageBubbleStyleRes, R.styleable.CometChatImageBubble)
                    CometChatImageBubbleStyle.fromTypedArray(context, ta)
                } else null,
                videoBubbleStyle = if (videoBubbleStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(videoBubbleStyleRes, R.styleable.CometChatVideoBubble)
                    CometChatVideoBubbleStyle.fromTypedArray(context, ta)
                } else null,
                fileBubbleStyle = if (fileBubbleStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(fileBubbleStyleRes, R.styleable.CometChatFileBubble)
                    CometChatFileBubbleStyle.fromTypedArray(context, ta)
                } else null,
                audioBubbleStyle = if (audioBubbleStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(audioBubbleStyleRes, R.styleable.CometChatAudioBubble)
                    CometChatAudioBubbleStyle.fromTypedArray(context, ta)
                } else null,
                deleteBubbleStyle = if (deleteBubbleStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(deleteBubbleStyleRes, R.styleable.CometChatDeleteBubble)
                    CometChatDeleteBubbleStyle.fromTypedArray(context, ta)
                } else null,
                stickerBubbleStyle = if (stickerBubbleStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(stickerBubbleStyleRes, R.styleable.CometChatStickerBubble)
                    CometChatStickerBubbleStyle.fromTypedArray(context, ta)
                } else null,
                pollBubbleStyle = if (pollBubbleStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(pollBubbleStyleRes, R.styleable.CometChatPollBubble)
                    CometChatPollBubbleStyle.fromTypedArray(context, ta)
                } else null,
                collaborativeBubbleStyle = if (collaborativeBubbleStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(collaborativeBubbleStyleRes, R.styleable.CometChatCollaborativeBubble)
                    CometChatCollaborativeBubbleStyle.fromTypedArray(context, ta)
                } else null,
                meetCallBubbleStyle = if (meetCallBubbleStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(meetCallBubbleStyleRes, R.styleable.CometChatMeetCallBubble)
                    CometChatMeetCallBubbleStyle.fromTypedArray(context, ta)
                } else null,

                // Additional sub-component styles
                reactionStyle = run {
                    val res = typedArray?.getResourceId(R.styleable.CometChatMessageBubble_cometchatReactionStyle, 0) ?: 0
                    if (res != 0) {
                        val ta = context.obtainStyledAttributes(res, R.styleable.CometChatReaction)
                        CometChatReactionStyle.fromTypedArray(context, ta)
                    } else null
                },
                mentionStyle = run {
                    val res = typedArray?.getResourceId(R.styleable.CometChatMessageBubble_cometchatMessageBubbleMentionsStyle, 0) ?: 0
                    if (res != 0) {
                        val ta = context.obtainStyledAttributes(res, R.styleable.CometChatMentionStyle)
                        CometChatMentionStyle.fromTypedArray(context, ta)
                    } else null
                },
                aiAssistantBubbleStyle = run {
                    val res = typedArray?.getResourceId(R.styleable.CometChatMessageBubble_cometchatAIAssistantBubbleStyle, 0) ?: 0
                    if (res != 0) {
                        val ta = context.obtainStyledAttributes(res, R.styleable.CometChatAIAssistantBubble)
                        CometChatAIAssistantBubbleStyle.fromTypedArray(context, ta)
                    } else null
                },
                moderationViewStyle = run {
                    val res = typedArray?.getResourceId(R.styleable.CometChatMessageBubble_cometchatModerationViewStyle, 0) ?: 0
                    if (res != 0) {
                        val ta = context.obtainStyledAttributes(res, R.styleable.CometChatModerationView)
                        CometChatModerationViewStyle.fromTypedArray(context, ta)
                    } else null
                },
                messagePreviewStyle = run {
                    val res = typedArray?.getResourceId(R.styleable.CometChatMessageBubble_cometChatMessagePreviewStyle, 0) ?: 0
                    if (res != 0) {
                        val ta = context.obtainStyledAttributes(res, R.styleable.CometChatMessagePreview)
                        CometChatMessagePreviewStyle.fromTypedArray(context, ta)
                    } else null
                }
            )
        }
    }
}

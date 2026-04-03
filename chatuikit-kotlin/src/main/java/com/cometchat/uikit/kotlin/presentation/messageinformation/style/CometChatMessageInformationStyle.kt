package com.cometchat.uikit.kotlin.presentation.messageinformation.style

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.presentation.shared.receipts.CometChatReceiptStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatMessageInformation component.
 *
 * This data class holds all styling properties for the message information view,
 * matching the XML attributes defined in attr_cometchat_message_information.xml.
 */
data class CometChatMessageInformationStyle(
    // Title styling
    @StyleRes val titleTextAppearance: Int = 0,
    @ColorInt val titleTextColor: Int = 0,

    // Container styling
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val backgroundHighlightColor: Int = 0,
    @Dimension val cornerRadius: Int = 0,
    @Dimension val strokeWidth: Int = 0,
    @ColorInt val strokeColor: Int = 0,

    // Item name styling
    @StyleRes val itemNameTextAppearance: Int = 0,
    @ColorInt val itemNameTextColor: Int = 0,

    // Item read styling
    @StyleRes val itemReadTextAppearance: Int = 0,
    @ColorInt val itemReadTextColor: Int = 0,
    @StyleRes val itemReadDateTextAppearance: Int = 0,
    @ColorInt val itemReadDateTextColor: Int = 0,

    // Item delivered styling
    @StyleRes val itemDeliveredTextAppearance: Int = 0,
    @ColorInt val itemDeliveredTextColor: Int = 0,
    @StyleRes val itemDeliveredDateTextAppearance: Int = 0,
    @ColorInt val itemDeliveredDateTextColor: Int = 0,

    // Nested component styles
    val itemAvatarStyle: CometChatAvatarStyle? = null,
    val messageReceiptStyle: CometChatReceiptStyle? = null
) {
    companion object {
        /**
         * Creates a default style with theme-appropriate values.
         *
         * @param context The context to access theme resources
         * @return A CometChatMessageInformationStyle with default values
         */
        fun default(context: Context): CometChatMessageInformationStyle {
            return extractFromTypedArray(context, null)
        }

        /**
         * Creates a [CometChatMessageInformationStyle] from a [TypedArray].
         *
         * This method extracts all properties from the TypedArray and handles
         * recycling the TypedArray after extraction.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes
         * @return A CometChatMessageInformationStyle with values from the TypedArray
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatMessageInformationStyle {
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Extracts style properties from a TypedArray without recycling it.
         *
         * @param context The context to access theme resources
         * @param typedArray The TypedArray containing style attributes, or null for defaults
         * @return A CometChatMessageInformationStyle with extracted values
         */
        fun extractFromTypedArray(
            context: Context,
            typedArray: TypedArray?
        ): CometChatMessageInformationStyle {
            // Extract nested style resource IDs
            val avatarStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageInformation_cometchatMessageInformationItemAvatarStyle, 0
            ) ?: 0
            val receiptStyleRes = typedArray?.getResourceId(
                R.styleable.CometChatMessageInformation_cometchatMessageInformationMessageReceiptStyle, 0
            ) ?: 0

            return CometChatMessageInformationStyle(
                // Title styling
                titleTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationTitleTextAppearance, 0
                ) ?: 0,
                titleTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationTitleTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),

                // Container styling
                backgroundColor = typedArray?.getColor(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationBackgroundColor,
                    CometChatTheme.getBackgroundColor1(context)
                ) ?: CometChatTheme.getBackgroundColor1(context),
                backgroundHighlightColor = typedArray?.getColor(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationBackgroundHighlightColor,
                    CometChatTheme.getBackgroundColor2(context)
                ) ?: CometChatTheme.getBackgroundColor2(context),
                cornerRadius = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationCornerRadius, 0
                ) ?: 0,
                strokeWidth = typedArray?.getDimensionPixelSize(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationStrokeWidth, 0
                ) ?: 0,
                strokeColor = typedArray?.getColor(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationStrokeColor, 0
                ) ?: 0,

                // Item name styling
                itemNameTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationItemNameTextAppearance, 0
                ) ?: 0,
                itemNameTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationItemNameTextColor,
                    CometChatTheme.getTextColorPrimary(context)
                ) ?: CometChatTheme.getTextColorPrimary(context),

                // Item read styling
                itemReadTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationItemReadTextAppearance, 0
                ) ?: 0,
                itemReadTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationItemReadTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                itemReadDateTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationItemReadDateTextAppearance, 0
                ) ?: 0,
                itemReadDateTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationItemReadDateTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),

                // Item delivered styling
                itemDeliveredTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationItemDeliveredTextAppearance, 0
                ) ?: 0,
                itemDeliveredTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationItemDeliveredTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                itemDeliveredDateTextAppearance = typedArray?.getResourceId(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationItemDeliveredDateTextAppearance, 0
                ) ?: 0,
                itemDeliveredDateTextColor = typedArray?.getColor(
                    R.styleable.CometChatMessageInformation_cometchatMessageInformationItemDeliveredDateTextColor,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),

                // Nested component styles
                itemAvatarStyle = if (avatarStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(avatarStyleRes, R.styleable.CometChatAvatar)
                    CometChatAvatarStyle.fromTypedArray(context, ta)
                } else null,
                messageReceiptStyle = if (receiptStyleRes != 0) {
                    val ta = context.obtainStyledAttributes(receiptStyleRes, R.styleable.CometChatMessageReceipt)
                    CometChatReceiptStyle.fromTypedArray(context, ta)
                } else null
            )
        }
    }
}

package com.cometchat.uikit.kotlin.presentation.shared.receipts

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Style configuration for CometChatReceipt component.
 */
data class CometChatReceiptStyle(
    val waitIcon: Drawable? = null,
    val sentIcon: Drawable? = null,
    val deliveredIcon: Drawable? = null,
    val readIcon: Drawable? = null,
    val errorIcon: Drawable? = null,
    @ColorInt val waitIconTint: Int = 0,
    @ColorInt val sentIconTint: Int = 0,
    @ColorInt val deliveredIconTint: Int = 0,
    @ColorInt val readIconTint: Int = 0,
    @ColorInt val errorIconTint: Int = 0,
    @Dimension val iconSize: Int = 0
) {
    companion object {
        /**
         * Creates a default style by extracting values from the theme's cometchatMessageReceiptStyle.
         *
         * @param context The context to access theme resources
         * @return A CometChatReceiptStyle with values from theme or fallback defaults
         */
        fun default(context: Context): CometChatReceiptStyle {
            return extractFromThemeStyle(context, R.attr.cometchatMessageReceiptStyle)
        }

        /**
         * Extracts the receipt style from a theme style attribute.
         *
         * @param context The context to access theme resources
         * @param themeStyleAttr The attribute reference to the receipt style in theme
         * @return A CometChatReceiptStyle with values from the theme style
         */
        private fun extractFromThemeStyle(context: Context, themeStyleAttr: Int): CometChatReceiptStyle {
            val themeTypedArray = context.obtainStyledAttributes(intArrayOf(themeStyleAttr))
            val styleResId = themeTypedArray.getResourceId(0, 0)
            themeTypedArray.recycle()

            return extractFromStyleResource(context, styleResId)
        }

        /**
         * Creates a style from a specific style resource ID.
         * If styleResId is 0, returns a style with CometChatTheme defaults.
         *
         * @param context The context to access theme resources
         * @param styleResId The style resource ID to extract from (0 for defaults only)
         * @return A CometChatReceiptStyle with values from the style resource or defaults
         */
        fun fromStyleResource(context: Context, styleResId: Int): CometChatReceiptStyle {
            return extractFromStyleResource(context, styleResId)
        }

        /**
         * Extracts style values from a specific style resource ID.
         * If styleResId is 0, returns a style with CometChatTheme defaults.
         *
         * @param context The context to access theme resources
         * @param styleResId The style resource ID to extract from (0 for defaults only)
         * @return A CometChatReceiptStyle with values from the style resource or defaults
         */
        private fun extractFromStyleResource(context: Context, styleResId: Int): CometChatReceiptStyle {
            if (styleResId == 0) {
                return extractFromTypedArray(context, null)
            }
            val typedArray = context.obtainStyledAttributes(styleResId, R.styleable.CometChatMessageReceipt)
            return try {
                extractFromTypedArray(context, typedArray)
            } finally {
                typedArray.recycle()
            }
        }

        /**
         * Creates a style by extracting values from XML TypedArray.
         *
         * This method handles TypedArray recycling internally using try-finally.
         *
         * @param context The Android context for accessing theme resources
         * @param typedArray The TypedArray containing XML attribute values (will be recycled)
         * @return A CometChatReceiptStyle with values from XML or theme defaults
         */
        fun fromTypedArray(context: Context, typedArray: TypedArray): CometChatReceiptStyle {
            return try {
                extractFromTypedArray(context, typedArray)
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
         * @return A CometChatReceiptStyle with extracted values or theme defaults
         */
        private fun extractFromTypedArray(context: Context, typedArray: TypedArray?): CometChatReceiptStyle {
            return CometChatReceiptStyle(
                // Icon drawables
                waitIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageReceipt_cometchatMessageReceiptWaitIcon
                ),
                sentIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageReceipt_cometchatMessageReceiptSentIcon
                ),
                deliveredIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageReceipt_cometchatMessageReceiptDeliveredIcon
                ),
                readIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageReceipt_cometchatMessageReceiptReadIcon
                ),
                errorIcon = typedArray?.getDrawable(
                    R.styleable.CometChatMessageReceipt_cometchatMessageReceiptErrorIcon
                ),
                // Icon tints
                waitIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageReceipt_cometchatMessageReceiptWaitIconTint,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                sentIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageReceipt_cometchatMessageReceiptSentIconTint,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                deliveredIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageReceipt_cometchatMessageReceiptDeliveredIconTint,
                    CometChatTheme.getTextColorSecondary(context)
                ) ?: CometChatTheme.getTextColorSecondary(context),
                readIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageReceipt_cometchatMessageReceiptReadIconTint,
                    CometChatTheme.getMessageReadColor(context)
                ) ?: CometChatTheme.getMessageReadColor(context),
                errorIconTint = typedArray?.getColor(
                    R.styleable.CometChatMessageReceipt_cometchatMessageReceiptErrorIconTint,
                    CometChatTheme.getErrorColor(context)
                ) ?: CometChatTheme.getErrorColor(context)
            )
        }
    }
}

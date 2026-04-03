package com.cometchat.uikit.core.domain.model

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes

/**
 * Represents a contextual action available on a message (e.g., reply, copy, edit, delete).
 * Shared across both `chatuikit-kotlin` and `chatuikit-jetpack` UI modules.
 *
 * Example usage:
 * ```kotlin
 * val option = CometChatMessageOption(
 *     id = UIKitConstants.MessageOption.COPY,
 *     title = context.getString(R.string.cometchat_copy),
 *     icon = R.drawable.cometchat_ic_copy,
 *     onClick = { /* handle click */ }
 * )
 * ```
 */
data class CometChatMessageOption(
    val id: String,
    val title: String,
    @ColorInt val titleColor: Int = 0,
    @DrawableRes val icon: Int = 0,
    @ColorInt val iconTintColor: Int = 0,
    @StyleRes val titleAppearance: Int = 0,
    @ColorInt val backgroundColor: Int = 0,
    val onClick: (() -> Unit)? = null
)

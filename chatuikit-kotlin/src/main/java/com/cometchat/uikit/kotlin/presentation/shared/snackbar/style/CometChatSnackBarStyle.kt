package com.cometchat.uikit.kotlin.presentation.shared.snackbar.style

import androidx.annotation.ColorInt
import androidx.annotation.StyleRes

/**
 * A style class for [com.cometchat.uikit.kotlin.presentation.shared.snackbar.CometChatSnackBar],
 * the Views transient banner shown above the composer (e.g. an attachment rejection reason).
 *
 * Every property is optional: a value of `0` (colors / text appearance) or a negative
 * [cornerRadius] means "use the CometChat theme default" (error red background, white text and
 * close icon, Caption1 Medium font — matching the iOS `CometChatSnackBar`). Set only the
 * properties you want to override — the same fluent pattern as the other Views `…Style` classes.
 */
class CometChatSnackBarStyle {

    @ColorInt
    private var backgroundColor: Int = 0

    @ColorInt
    private var textColor: Int = 0

    @ColorInt
    private var closeIconTint: Int = 0

    @StyleRes
    private var textAppearance: Int = 0

    /** Corner radius in pixels; a negative value keeps the theme default. */
    private var cornerRadius: Float = -1f

    /** Sets the banner background color. */
    fun setBackgroundColor(@ColorInt backgroundColor: Int): CometChatSnackBarStyle {
        this.backgroundColor = backgroundColor
        return this
    }

    /** Sets the message text color. */
    fun setTextColor(@ColorInt textColor: Int): CometChatSnackBarStyle {
        this.textColor = textColor
        return this
    }

    /** Sets the close (✕) icon tint. */
    fun setCloseIconTint(@ColorInt closeIconTint: Int): CometChatSnackBarStyle {
        this.closeIconTint = closeIconTint
        return this
    }

    /** Sets the message text appearance (a `TextAppearance` style resource). */
    fun setTextAppearance(@StyleRes textAppearance: Int): CometChatSnackBarStyle {
        this.textAppearance = textAppearance
        return this
    }

    /** Sets the corner radius in pixels. */
    fun setCornerRadius(cornerRadius: Float): CometChatSnackBarStyle {
        this.cornerRadius = cornerRadius
        return this
    }

    @ColorInt
    fun getBackgroundColor(): Int = backgroundColor

    @ColorInt
    fun getTextColor(): Int = textColor

    @ColorInt
    fun getCloseIconTint(): Int = closeIconTint

    @StyleRes
    fun getTextAppearance(): Int = textAppearance

    fun getCornerRadius(): Float = cornerRadius
}

package com.cometchat.uikit.kotlin.theme

import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.FontRes
import androidx.annotation.StyleRes
import com.cometchat.uikit.kotlin.R
import kotlin.math.roundToInt

/**
 * CometChatTheme provides programmatic access to theme colors, typography, and fonts
 * defined in the CometChat UIKit theme attributes.
 *
 * This class mirrors the functionality of the Java-based CometChatTheme from the chatuikit module,
 * providing a Kotlin-native implementation for the chatuikit-kotlin module.
 */
object CometChatTheme {
    private val themeAttributeCache = mutableMapOf<Int, Int>()

    // region Primary Color
    fun setPrimaryColor(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatPrimaryColor] = color
    }

    @ColorInt
    fun getPrimaryColor(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatPrimaryColor)
    }
    // endregion

    // region Extended Primary Colors
    fun setExtendedPrimaryColor50(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatExtendedPrimaryColor50] = color
    }

    @ColorInt
    fun getExtendedPrimaryColor50(context: Context): Int {
        return getExtendedPrimaryColor(context, R.attr.cometchatExtendedPrimaryColor50, 0.96, 0.80)
    }

    fun setExtendedPrimaryColor100(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatExtendedPrimaryColor100] = color
    }

    @ColorInt
    fun getExtendedPrimaryColor100(context: Context): Int {
        return getExtendedPrimaryColor(context, R.attr.cometchatExtendedPrimaryColor100, 0.88, 0.72)
    }

    fun setExtendedPrimaryColor200(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatExtendedPrimaryColor200] = color
    }

    @ColorInt
    fun getExtendedPrimaryColor200(context: Context): Int {
        return getExtendedPrimaryColor(context, R.attr.cometchatExtendedPrimaryColor200, 0.77, 0.64)
    }

    fun setExtendedPrimaryColor300(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatExtendedPrimaryColor300] = color
    }

    @ColorInt
    fun getExtendedPrimaryColor300(context: Context): Int {
        return getExtendedPrimaryColor(context, R.attr.cometchatExtendedPrimaryColor300, 0.66, 0.56)
    }

    fun setExtendedPrimaryColor400(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatExtendedPrimaryColor400] = color
    }

    @ColorInt
    fun getExtendedPrimaryColor400(context: Context): Int {
        return getExtendedPrimaryColor(context, R.attr.cometchatExtendedPrimaryColor400, 0.55, 0.48)
    }

    fun setExtendedPrimaryColor500(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatExtendedPrimaryColor500] = color
    }

    @ColorInt
    fun getExtendedPrimaryColor500(context: Context): Int {
        return getExtendedPrimaryColor(context, R.attr.cometchatExtendedPrimaryColor500, 0.44, 0.40)
    }

    fun setExtendedPrimaryColor600(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatExtendedPrimaryColor600] = color
    }

    @ColorInt
    fun getExtendedPrimaryColor600(context: Context): Int {
        return getExtendedPrimaryColor(context, R.attr.cometchatExtendedPrimaryColor600, 0.33, 0.32)
    }

    fun setExtendedPrimaryColor700(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatExtendedPrimaryColor700] = color
    }

    @ColorInt
    fun getExtendedPrimaryColor700(context: Context): Int {
        return getExtendedPrimaryColor(context, R.attr.cometchatExtendedPrimaryColor700, 0.22, 0.24)
    }

    fun setExtendedPrimaryColor800(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatExtendedPrimaryColor800] = color
    }

    @ColorInt
    fun getExtendedPrimaryColor800(context: Context): Int {
        return getExtendedPrimaryColor(context, R.attr.cometchatExtendedPrimaryColor800, 0.11, 0.16)
    }

    fun setExtendedPrimaryColor900(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatExtendedPrimaryColor900] = color
    }

    @ColorInt
    fun getExtendedPrimaryColor900(context: Context): Int {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val blendingColor = if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) Color.BLACK else Color.WHITE
        val percentage = if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) 0.11 else 0.08
        return getBlendedColor(getPrimaryColor(context), blendingColor, percentage)
    }
    // endregion

    // region Neutral Colors
    fun setNeutralColor50(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatNeutralColor50] = color
    }

    @ColorInt
    fun getNeutralColor50(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatNeutralColor50)
    }

    fun setNeutralColor100(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatNeutralColor100] = color
    }

    @ColorInt
    fun getNeutralColor100(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatNeutralColor100)
    }

    fun setNeutralColor200(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatNeutralColor200] = color
    }

    @ColorInt
    fun getNeutralColor200(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatNeutralColor200)
    }

    fun setNeutralColor300(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatNeutralColor300] = color
    }

    @ColorInt
    fun getNeutralColor300(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatNeutralColor300)
    }

    fun setNeutralColor400(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatNeutralColor400] = color
    }

    @ColorInt
    fun getNeutralColor400(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatNeutralColor400)
    }

    fun setNeutralColor500(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatNeutralColor500] = color
    }

    @ColorInt
    fun getNeutralColor500(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatNeutralColor500)
    }

    fun setNeutralColor600(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatNeutralColor600] = color
    }

    @ColorInt
    fun getNeutralColor600(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatNeutralColor600)
    }

    fun setNeutralColor700(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatNeutralColor700] = color
    }

    @ColorInt
    fun getNeutralColor700(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatNeutralColor700)
    }

    fun setNeutralColor800(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatNeutralColor800] = color
    }

    @ColorInt
    fun getNeutralColor800(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatNeutralColor800)
    }

    fun setNeutralColor900(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatNeutralColor900] = color
    }

    @ColorInt
    fun getNeutralColor900(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatNeutralColor900)
    }
    // endregion

    // region Alert Colors
    fun setSuccessColor(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatSuccessColor] = color
    }

    @ColorInt
    fun getSuccessColor(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatSuccessColor)
    }

    fun setErrorColor(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatErrorColor] = color
    }

    @ColorInt
    fun getErrorColor(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatErrorColor)
    }

    fun setWarningColor(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatWarningColor] = color
    }

    @ColorInt
    fun getWarningColor(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatWarningColor)
    }

    fun setInfoColor(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatInfoColor] = color
    }

    @ColorInt
    fun getInfoColor(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatInfoColor)
    }

    fun setMessageReadColor(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatMessageReadColor] = color
    }

    @ColorInt
    fun getMessageReadColor(context: Context): Int {
        return getColorFromAttr(context, R.attr.cometchatMessageReadColor)
    }
    // endregion

    // region Background Colors
    fun setBackgroundColor1(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatBackgroundColor1] = color
    }

    @ColorInt
    fun getBackgroundColor1(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatBackgroundColor1)
        return if (color == 0) getNeutralColor50(context) else color
    }

    fun setBackgroundColor2(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatBackgroundColor2] = color
    }

    @ColorInt
    fun getBackgroundColor2(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatBackgroundColor2)
        return if (color == 0) getNeutralColor100(context) else color
    }

    fun setBackgroundColor3(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatBackgroundColor3] = color
    }

    @ColorInt
    fun getBackgroundColor3(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatBackgroundColor3)
        return if (color == 0) getNeutralColor200(context) else color
    }

    fun setBackgroundColor4(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatBackgroundColor4] = color
    }

    @ColorInt
    fun getBackgroundColor4(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatBackgroundColor4)
        return if (color == 0) getNeutralColor300(context) else color
    }
    // endregion

    // region Stroke Colors
    fun setStrokeColorDefault(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatStrokeColorDefault] = color
    }

    @ColorInt
    fun getStrokeColorDefault(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatStrokeColorDefault)
        return if (color == 0) getNeutralColor300(context) else color
    }

    fun setStrokeColorLight(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatStrokeColorLight] = color
    }

    @ColorInt
    fun getStrokeColorLight(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatStrokeColorLight)
        return if (color == 0) getNeutralColor200(context) else color
    }

    fun setStrokeColorDark(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatStrokeColorDark] = color
    }

    @ColorInt
    fun getStrokeColorDark(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatStrokeColorDark)
        return if (color == 0) getNeutralColor400(context) else color
    }

    fun setStrokeColorHighlight(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatStrokeColorHighlight] = color
    }

    @ColorInt
    fun getStrokeColorHighlight(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatStrokeColorHighlight)
        return if (color == 0) getPrimaryColor(context) else color
    }

    // Border color aliases (for consistency with Compose naming)
    fun setBorderColorLight(@ColorInt color: Int) {
        setStrokeColorLight(color)
    }

    @ColorInt
    fun getBorderColorLight(context: Context): Int {
        return getStrokeColorLight(context)
    }

    fun setBorderColorDefault(@ColorInt color: Int) {
        setStrokeColorDefault(color)
    }

    @ColorInt
    fun getBorderColorDefault(context: Context): Int {
        return getStrokeColorDefault(context)
    }

    fun setBorderColorDark(@ColorInt color: Int) {
        setStrokeColorDark(color)
    }

    @ColorInt
    fun getBorderColorDark(context: Context): Int {
        return getStrokeColorDark(context)
    }
    // endregion

    // region Text Colors
    fun setTextColorPrimary(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatTextColorPrimary] = color
    }

    @ColorInt
    fun getTextColorPrimary(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatTextColorPrimary)
        return if (color == 0) getNeutralColor900(context) else color
    }

    fun setTextColorSecondary(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatTextColorSecondary] = color
    }

    @ColorInt
    fun getTextColorSecondary(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatTextColorSecondary)
        return if (color == 0) getNeutralColor600(context) else color
    }

    fun setTextColorTertiary(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatTextColorTertiary] = color
    }

    @ColorInt
    fun getTextColorTertiary(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatTextColorTertiary)
        return if (color == 0) getNeutralColor500(context) else color
    }

    fun setTextColorDisabled(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatTextColorDisabled] = color
    }

    @ColorInt
    fun getTextColorDisabled(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatTextColorDisabled)
        return if (color == 0) getNeutralColor400(context) else color
    }

    fun setTextColorWhite(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatTextColorWhite] = color
    }

    @ColorInt
    fun getTextColorWhite(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatTextColorWhite)
        return if (color == 0) getNeutralColor50(context) else color
    }

    fun setTextColorHighlight(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatTextColorHighlight] = color
    }

    @ColorInt
    fun getTextColorHighlight(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatTextColorHighlight)
        return if (color == 0) getPrimaryColor(context) else color
    }
    // endregion

    // region Icon Tint Colors
    fun setIconTintPrimary(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatIconTintPrimary] = color
    }

    @ColorInt
    fun getIconTintPrimary(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatIconTintPrimary)
        return if (color == 0) getNeutralColor900(context) else color
    }

    fun setIconTintSecondary(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatIconTintSecondary] = color
    }

    @ColorInt
    fun getIconTintSecondary(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatIconTintSecondary)
        return if (color == 0) getNeutralColor500(context) else color
    }

    fun setIconTintTertiary(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatIconTintTertiary] = color
    }

    @ColorInt
    fun getIconTintTertiary(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatIconTintTertiary)
        return if (color == 0) getNeutralColor400(context) else color
    }

    fun setIconTintWhite(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatIconTintWhite] = color
    }

    @ColorInt
    fun getIconTintWhite(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatIconTintWhite)
        return if (color == 0) getNeutralColor50(context) else color
    }

    fun setIconTintHighlight(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatIconTintHighlight] = color
    }

    @ColorInt
    fun getIconTintHighlight(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatIconTintHighlight)
        return if (color == 0) getPrimaryColor(context) else color
    }
    // endregion

    // region Button Colors
    fun setPrimaryButtonBackgroundColor(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatPrimaryButtonBackgroundColor] = color
    }

    @ColorInt
    fun getPrimaryButtonBackgroundColor(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatPrimaryButtonBackgroundColor)
        return if (color == 0) getPrimaryColor(context) else color
    }

    fun setPrimaryButtonIconTint(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatPrimaryButtonIconTint] = color
    }

    @ColorInt
    fun getPrimaryButtonIconTint(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatPrimaryButtonIconTint)
        return if (color == 0) getColorWhite(context) else color
    }

    fun setPrimaryButtonTextColor(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatPrimaryButtonTextColor] = color
    }

    @ColorInt
    fun getPrimaryButtonTextColor(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatPrimaryButtonTextColor)
        return if (color == 0) getColorWhite(context) else color
    }

    fun setSecondaryButtonBackgroundColor(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatSecondaryButtonBackgroundColor] = color
    }

    @ColorInt
    fun getSecondaryButtonBackgroundColor(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatSecondaryButtonBackgroundColor)
        return if (color == 0) getNeutralColor900(context) else color
    }

    fun setSecondaryButtonIconTint(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatSecondaryButtonIconTint] = color
    }

    @ColorInt
    fun getSecondaryButtonIconTint(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatSecondaryButtonIconTint)
        return if (color == 0) getNeutralColor900(context) else color
    }

    fun setSecondaryButtonTextColor(@ColorInt color: Int) {
        themeAttributeCache[R.attr.cometchatSecondaryButtonTextColor] = color
    }

    @ColorInt
    fun getSecondaryButtonTextColor(context: Context): Int {
        val color = getColorFromAttr(context, R.attr.cometchatSecondaryButtonTextColor)
        return if (color == 0) getNeutralColor900(context) else color
    }
    // endregion

    // region Static Colors
    @ColorInt
    fun getColorWhite(context: Context): Int {
        return context.resources.getColor(R.color.cometchat_color_white, context.theme)
    }

    @ColorInt
    fun getColorBlack(context: Context): Int {
        return context.resources.getColor(R.color.cometchat_color_black, context.theme)
    }

    @ColorInt
    fun getColorTransparent(context: Context): Int {
        return context.resources.getColor(R.color.cometchat_color_transparent, context.theme)
    }
    // endregion


    // region Typography
    @StyleRes
    fun getTextAppearanceTitleRegular(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceTitleRegular)
    }

    @StyleRes
    fun getTextAppearanceTitleMedium(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceTitleMedium)
    }

    @StyleRes
    fun getTextAppearanceTitleBold(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceTitleBold)
    }

    @StyleRes
    fun getTextAppearanceHeading1Regular(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceHeading1Regular)
    }

    @StyleRes
    fun getTextAppearanceHeading1Medium(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceHeading1Medium)
    }

    @StyleRes
    fun getTextAppearanceHeading1Bold(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceHeading1Bold)
    }

    @StyleRes
    fun getTextAppearanceHeading2Regular(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceHeading2Regular)
    }

    @StyleRes
    fun getTextAppearanceHeading2Medium(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceHeading2Medium)
    }

    @StyleRes
    fun getTextAppearanceHeading2Bold(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceHeading2Bold)
    }

    @StyleRes
    fun getTextAppearanceHeading3Regular(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceHeading3Regular)
    }

    @StyleRes
    fun getTextAppearanceHeading3Medium(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceHeading3Medium)
    }

    @StyleRes
    fun getTextAppearanceHeading3Bold(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceHeading3Bold)
    }

    @StyleRes
    fun getTextAppearanceHeading4Regular(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceHeading4Regular)
    }

    @StyleRes
    fun getTextAppearanceHeading4Medium(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceHeading4Medium)
    }

    @StyleRes
    fun getTextAppearanceHeading4Bold(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceHeading4Bold)
    }

    @StyleRes
    fun getTextAppearanceBodyRegular(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceBodyRegular)
    }

    @StyleRes
    fun getTextAppearanceBodyMedium(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceBodyMedium)
    }

    @StyleRes
    fun getTextAppearanceBodyBold(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceBodyBold)
    }

    @StyleRes
    fun getTextAppearanceCaption1Regular(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceCaption1Regular)
    }

    @StyleRes
    fun getTextAppearanceCaption1Medium(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceCaption1Medium)
    }

    @StyleRes
    fun getTextAppearanceCaption1Bold(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceCaption1Bold)
    }

    @StyleRes
    fun getTextAppearanceCaption2Regular(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceCaption2Regular)
    }

    @StyleRes
    fun getTextAppearanceCaption2Medium(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceCaption2Medium)
    }

    @StyleRes
    fun getTextAppearanceCaption2Bold(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceCaption2Bold)
    }

    @StyleRes
    fun getTextAppearanceButtonRegular(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceButtonRegular)
    }

    @StyleRes
    fun getTextAppearanceButtonMedium(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceButtonMedium)
    }

    @StyleRes
    fun getTextAppearanceButtonBold(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceButtonBold)
    }

    @StyleRes
    fun getTextAppearanceLinkRegular(context: Context): Int {
        return getTextAppearanceFromAttr(context, R.attr.cometchatTextAppearanceLinkRegular)
    }
    // endregion

    // region Fonts
    @FontRes
    fun getFontRegular(context: Context): Int {
        return getFontFromAttr(context, R.attr.cometchatFontRegular)
    }

    @FontRes
    fun getFontMedium(context: Context): Int {
        return getFontFromAttr(context, R.attr.cometchatFontMedium)
    }

    @FontRes
    fun getFontBold(context: Context): Int {
        return getFontFromAttr(context, R.attr.cometchatFontBold)
    }
    // endregion

    // region Private Helper Methods
    @ColorInt
    private fun getColorFromAttr(context: Context?, @AttrRes attr: Int): Int {
        if (context == null) return 0
        if (themeAttributeCache.containsKey(attr)) {
            return themeAttributeCache[attr] ?: 0
        }

        return try {
            context.obtainStyledAttributes(intArrayOf(attr)).use { typedArray ->
                typedArray.getColor(0, 0)
            }
        } catch (e: Exception) {
            0
        }
    }

    @ColorInt
    private fun getExtendedPrimaryColor(
        context: Context,
        @AttrRes attr: Int,
        dayPercentage: Double,
        nightPercentage: Double
    ): Int {
        val color = getColorFromAttr(context, attr)
        if (color == 0) {
            val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val blendingColor = if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) Color.WHITE else Color.BLACK
            val percentage = if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) dayPercentage else nightPercentage
            return getBlendedColor(getPrimaryColor(context), blendingColor, percentage)
        }
        return color
    }

    private fun getBlendedColor(baseColor: Int, blendColor: Int, percentage: Double): Int {
        val r = (Color.red(baseColor) * (1 - percentage) + Color.red(blendColor) * percentage).roundToInt()
        val g = (Color.green(baseColor) * (1 - percentage) + Color.green(blendColor) * percentage).roundToInt()
        val b = (Color.blue(baseColor) * (1 - percentage) + Color.blue(blendColor) * percentage).roundToInt()
        return Color.rgb(r, g, b)
    }

    @StyleRes
    private fun getTextAppearanceFromAttr(context: Context?, @AttrRes attr: Int): Int {
        if (context == null) return 0
        return try {
            context.obtainStyledAttributes(intArrayOf(attr)).use { typedArray ->
                typedArray.getResourceId(0, 0)
            }
        } catch (e: Exception) {
            0
        }
    }

    @FontRes
    private fun getFontFromAttr(context: Context?, @AttrRes attr: Int): Int {
        if (context == null) return 0
        return try {
            context.obtainStyledAttributes(intArrayOf(attr)).use { typedArray ->
                typedArray.getResourceId(0, 0)
            }
        } catch (e: Exception) {
            0
        }
    }
    // endregion

    /**
     * Clears the theme attribute cache.
     * Call this when the theme changes to ensure fresh values are loaded.
     */
    fun clearCache() {
        themeAttributeCache.clear()
    }
}

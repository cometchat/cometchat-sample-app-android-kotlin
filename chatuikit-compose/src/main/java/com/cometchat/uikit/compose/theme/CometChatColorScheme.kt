package com.cometchat.uikit.compose.theme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.roundToInt

@Immutable
class CometChatColorScheme(

    // Primary Colors
    val primary: Color,

    // Extended Primary Colors
    val extendedPrimaryColor50: Color,
    val extendedPrimaryColor100: Color,
    val extendedPrimaryColor200: Color,
    val extendedPrimaryColor300: Color,
    val extendedPrimaryColor400: Color,
    val extendedPrimaryColor500: Color,
    val extendedPrimaryColor600: Color,
    val extendedPrimaryColor700: Color,
    val extendedPrimaryColor800: Color,
    val extendedPrimaryColor900: Color,

    // Neutral Colors
    val neutralColor50: Color,
    val neutralColor100: Color,
    val neutralColor200: Color,
    val neutralColor300: Color,
    val neutralColor400: Color,
    val neutralColor500: Color,
    val neutralColor600: Color,
    val neutralColor700: Color,
    val neutralColor800: Color,
    val neutralColor900: Color,

    // Alert Colors
    val infoColor: Color,
    val successColor: Color,
    val warningColor: Color,
    val errorColor: Color,
    val messageReadColor: Color,

    // Background Colors
    val backgroundColor1: Color,
    val backgroundColor2: Color,
    val backgroundColor3: Color,
    val backgroundColor4: Color,

    // Border Colors
    val strokeColorDefault: Color,
    val strokeColorLight: Color,
    val strokeColorDark: Color,
    val strokeColorHighlight: Color,

    // Text Colors
    val textColorPrimary: Color,
    val textColorSecondary: Color,
    val textColorTertiary: Color,
    val textColorDisabled: Color,
    val textColorWhite: Color,
    val textColorHighlight: Color,

    // Icon Colors
    val iconTintPrimary: Color,
    val iconTintSecondary: Color,
    val iconTintTertiary: Color,
    val iconTintWhite: Color,
    val iconTintHighlight: Color,

    // Button Colors
    val primaryButtonBackgroundColor: Color,
    val primaryButtonIconTint: Color,
    val primaryButtonTextColor: Color,
    val secondaryButtonBackgroundColor: Color,
    val secondaryButtonIconTint: Color,
    val secondaryButtonTextColor: Color,
    val linkButtonColor: Color,
    val fabButtonBackgroundColor: Color,
    val fabButtonIconTint: Color,
    val whiteButtonPressed: Color,

    // Static Colors
    val colorWhite: Color,
    val colorBlack: Color
) {
    // Aliases for border colors (for naming consistency with other components)
    val borderColorLight: Color get() = strokeColorLight
    val borderColorDefault: Color get() = strokeColorDefault
    val borderColorDark: Color get() = strokeColorDark
    val borderColorHighlight: Color get() = strokeColorHighlight

    override fun toString(): String {
        return "CometChatColorScheme(" +
            "primary=$primary, " +
            "extendedPrimaryColor50=$extendedPrimaryColor50, " +
            "extendedPrimaryColor100=$extendedPrimaryColor100, " +
            "extendedPrimaryColor200=$extendedPrimaryColor200, " +
            "extendedPrimaryColor300=$extendedPrimaryColor300, " +
            "extendedPrimaryColor400=$extendedPrimaryColor400, " +
            "extendedPrimaryColor500=$extendedPrimaryColor500, " +
            "extendedPrimaryColor600=$extendedPrimaryColor600, " +
            "extendedPrimaryColor700=$extendedPrimaryColor700, " +
            "extendedPrimaryColor800=$extendedPrimaryColor800, " +
            "extendedPrimaryColor900=$extendedPrimaryColor900, " +
            "neutralColor50=$neutralColor50, " +
            "neutralColor100=$neutralColor100, " +
            "neutralColor200=$neutralColor200, " +
            "neutralColor300=$neutralColor300, " +
            "neutralColor400=$neutralColor400, " +
            "neutralColor500=$neutralColor500, " +
            "neutralColor600=$neutralColor600, " +
            "neutralColor700=$neutralColor700, " +
            "neutralColor800=$neutralColor800, " +
            "neutralColor900=$neutralColor900, " +
            "infoColor=$infoColor, " +
            "successColor=$successColor, " +
            "warningColor=$warningColor, " +
            "errorColor=$errorColor, " +
            "messageReadColor=$messageReadColor, " +
            "backgroundColor1=$backgroundColor1, " +
            "backgroundColor2=$backgroundColor2, " +
            "backgroundColor3=$backgroundColor3, " +
            "backgroundColor4=$backgroundColor4, " +
            "strokeColorDefault=$strokeColorDefault, " +
            "strokeColorLight=$strokeColorLight, " +
            "strokeColorDark=$strokeColorDark, " +
            "strokeColorHighlight=$strokeColorHighlight, " +
            "textColorPrimary=$textColorPrimary, " +
            "textColorSecondary=$textColorSecondary, " +
            "textColorTertiary=$textColorTertiary, " +
            "textColorDisabled=$textColorDisabled, " +
            "textColorWhite=$textColorWhite, " +
            "textColorHighlight=$textColorHighlight, " +
            "iconTintPrimary=$iconTintPrimary, " +
            "iconTintSecondary=$iconTintSecondary, " +
            "iconTintTertiary=$iconTintTertiary, " +
            "iconTintWhite=$iconTintWhite, " +
            "iconTintHighlight=$iconTintHighlight, " +
            "primaryButtonBackgroundColor=$primaryButtonBackgroundColor, " +
            "primaryButtonIconTint=$primaryButtonIconTint, " +
            "primaryButtonTextColor=$primaryButtonTextColor, " +
            "secondaryButtonBackgroundColor=$secondaryButtonBackgroundColor, " +
            "secondaryButtonIconTint=$secondaryButtonIconTint, " +
            "secondaryButtonTextColor=$secondaryButtonTextColor, " +
            "linkButtonColor=$linkButtonColor, " +
            "fabButtonBackgroundColor=$fabButtonBackgroundColor, " +
            "fabButtonIconTint=$fabButtonIconTint, " +
            "whiteButtonPressed=$whiteButtonPressed, " +
            "colorWhite=$colorWhite, " +
            "colorBlack=$colorBlack)"
    }
}

/**
 * Generates an extended primary color by blending the base primary color with white (light mode)
 * or black (dark mode) based on the specified percentages.
 *
 * @param baseColor The primary color to blend
 * @param dayPercentage The blending percentage to use for light mode (0.0 to 1.0)
 * @param nightPercentage The blending percentage to use for dark mode (0.0 to 1.0)
 * @param isNightMode Whether the current theme is in dark mode
 * @return The blended extended primary color
 */
fun getExtendedPrimaryColor(
    baseColor: Color,
    dayPercentage: Double,
    nightPercentage: Double,
    isNightMode: Boolean
): Color {
    val blendingColor = if (isNightMode) Color.Black else Color.White
    val percentage = if (isNightMode) nightPercentage else dayPercentage
    return blendColors(baseColor, blendingColor, percentage)
}

// Function to blend two colors based on a percentage
fun blendColors(baseColor: Color, blendColor: Color, percentage: Double): Color {
    val baseColorInt = baseColor.toArgb()
    val blendColorInt = blendColor.toArgb()
    val r = (android.graphics.Color.red(baseColorInt) * (1 - percentage) +
        android.graphics.Color.red(blendColorInt) * percentage).roundToInt()
    val g = (android.graphics.Color.green(baseColorInt) * (1 - percentage) +
        android.graphics.Color.green(blendColorInt) * percentage).roundToInt()
    val b = (android.graphics.Color.blue(baseColorInt) * (1 - percentage) +
        android.graphics.Color.blue(blendColorInt) * percentage).roundToInt()
    return Color(android.graphics.Color.rgb(r, g, b))
}

fun lightColorScheme(
    primary: Color = primaryColorLight,
    extendedPrimaryColor50: Color = getExtendedPrimaryColor(primary, 0.96, 0.80, false),
    extendedPrimaryColor100: Color = getExtendedPrimaryColor(primary, 0.88, 0.72, false),
    extendedPrimaryColor200: Color = getExtendedPrimaryColor(primary, 0.77, 0.64, false),
    extendedPrimaryColor300: Color = getExtendedPrimaryColor(primary, 0.66, 0.56, false),
    extendedPrimaryColor400: Color = getExtendedPrimaryColor(primary, 0.55, 0.48, false),
    extendedPrimaryColor500: Color = getExtendedPrimaryColor(primary, 0.44, 0.40, false),
    extendedPrimaryColor600: Color = getExtendedPrimaryColor(primary, 0.33, 0.32, false),
    extendedPrimaryColor700: Color = getExtendedPrimaryColor(primary, 0.22, 0.24, false),
    extendedPrimaryColor800: Color = getExtendedPrimaryColor(primary, 0.11, 0.16, false),
    extendedPrimaryColor900: Color = blendColors(primary, Color.Black, 0.08),

    neutralColor50: Color = neutral50Light,
    neutralColor100: Color = neutral100Light,
    neutralColor200: Color = neutral200Light,
    neutralColor300: Color = neutral300Light,
    neutralColor400: Color = neutral400Light,
    neutralColor500: Color = neutral500Light,
    neutralColor600: Color = neutral600Light,
    neutralColor700: Color = neutral700Light,
    neutralColor800: Color = neutral800Light,
    neutralColor900: Color = neutral900Light,

    infoColor: Color = infoColorLight,
    successColor: Color = successColorLight,
    warningColor: Color = warningColorLight,
    errorColor: Color = errorColorLight,
    messageReadColor: Color = messageReadColorLight,

    colorWhite: Color = white,
    colorBlack: Color = black,

    backgroundColor1: Color = neutralColor50,
    backgroundColor2: Color = neutralColor100,
    backgroundColor3: Color = neutralColor200,
    backgroundColor4: Color = neutralColor300,

    strokeColorDefault: Color = neutralColor300,
    strokeColorLight: Color = neutralColor200,
    strokeColorDark: Color = neutralColor400,
    strokeColorHighlight: Color = primary,

    textColorPrimary: Color = neutralColor900,
    textColorSecondary: Color = neutralColor600,
    textColorTertiary: Color = neutralColor500,
    textColorDisabled: Color = neutralColor400,
    textColorWhite: Color = neutralColor50,
    textColorHighlight: Color = primary,

    iconTintPrimary: Color = neutralColor900,
    iconTintSecondary: Color = neutralColor500,
    iconTintTertiary: Color = neutralColor400,
    iconTintWhite: Color = neutralColor50,
    iconTintHighlight: Color = primary,

    primaryButtonBackgroundColor: Color = primary,
    primaryButtonIconTint: Color = colorWhite,
    primaryButtonTextColor: Color = colorBlack,
    secondaryButtonBackgroundColor: Color = neutralColor900,
    secondaryButtonIconTint: Color = neutralColor900,
    secondaryButtonTextColor: Color = neutralColor900,
    linkButtonColor: Color = infoColor,
    fabButtonBackgroundColor: Color = primary,
    fabButtonIconTint: Color = colorWhite,
    whiteButtonPressed: Color = neutralColor300

): CometChatColorScheme = CometChatColorScheme(
    primary = primary,
    extendedPrimaryColor50 = extendedPrimaryColor50,
    extendedPrimaryColor100 = extendedPrimaryColor100,
    extendedPrimaryColor200 = extendedPrimaryColor200,
    extendedPrimaryColor300 = extendedPrimaryColor300,
    extendedPrimaryColor400 = extendedPrimaryColor400,
    extendedPrimaryColor500 = extendedPrimaryColor500,
    extendedPrimaryColor600 = extendedPrimaryColor600,
    extendedPrimaryColor700 = extendedPrimaryColor700,
    extendedPrimaryColor800 = extendedPrimaryColor800,
    extendedPrimaryColor900 = extendedPrimaryColor900,

    neutralColor50 = neutralColor50,
    neutralColor100 = neutralColor100,
    neutralColor200 = neutralColor200,
    neutralColor300 = neutralColor300,
    neutralColor400 = neutralColor400,
    neutralColor500 = neutralColor500,
    neutralColor600 = neutralColor600,
    neutralColor700 = neutralColor700,
    neutralColor800 = neutralColor800,
    neutralColor900 = neutralColor900,

    infoColor = infoColor,
    successColor = successColor,
    warningColor = warningColor,
    errorColor = errorColor,
    messageReadColor = messageReadColor,

    backgroundColor1 = backgroundColor1,
    backgroundColor2 = backgroundColor2,
    backgroundColor3 = backgroundColor3,
    backgroundColor4 = backgroundColor4,

    strokeColorDefault = strokeColorDefault,
    strokeColorLight = strokeColorLight,
    strokeColorDark = strokeColorDark,
    strokeColorHighlight = strokeColorHighlight,

    textColorPrimary = textColorPrimary,
    textColorSecondary = textColorSecondary,
    textColorTertiary = textColorTertiary,
    textColorDisabled = textColorDisabled,
    textColorWhite = textColorWhite,
    textColorHighlight = textColorHighlight,

    iconTintPrimary = iconTintPrimary,
    iconTintSecondary = iconTintSecondary,
    iconTintTertiary = iconTintTertiary,
    iconTintWhite = iconTintWhite,
    iconTintHighlight = iconTintHighlight,

    primaryButtonBackgroundColor = primaryButtonBackgroundColor,
    primaryButtonIconTint = primaryButtonIconTint,
    primaryButtonTextColor = primaryButtonTextColor,
    secondaryButtonBackgroundColor = secondaryButtonBackgroundColor,
    secondaryButtonIconTint = secondaryButtonIconTint,
    secondaryButtonTextColor = secondaryButtonTextColor,
    linkButtonColor = linkButtonColor,
    fabButtonBackgroundColor = fabButtonBackgroundColor,
    fabButtonIconTint = fabButtonIconTint,
    whiteButtonPressed = whiteButtonPressed,

    colorWhite = colorWhite,
    colorBlack = colorBlack
)

fun darkColorScheme(
    primary: Color = primaryColorDark,
    extendedPrimaryColor50: Color = getExtendedPrimaryColor(primary, 0.96, 0.80, true),
    extendedPrimaryColor100: Color = getExtendedPrimaryColor(primary, 0.88, 0.72, true),
    extendedPrimaryColor200: Color = getExtendedPrimaryColor(primary, 0.77, 0.64, true),
    extendedPrimaryColor300: Color = getExtendedPrimaryColor(primary, 0.66, 0.56, true),
    extendedPrimaryColor400: Color = getExtendedPrimaryColor(primary, 0.55, 0.48, true),
    extendedPrimaryColor500: Color = getExtendedPrimaryColor(primary, 0.44, 0.40, true),
    extendedPrimaryColor600: Color = getExtendedPrimaryColor(primary, 0.33, 0.32, true),
    extendedPrimaryColor700: Color = getExtendedPrimaryColor(primary, 0.22, 0.24, true),
    extendedPrimaryColor800: Color = getExtendedPrimaryColor(primary, 0.11, 0.16, true),
    extendedPrimaryColor900: Color = blendColors(primary, Color.White, 0.11),

    neutralColor50: Color = neutral50Dark,
    neutralColor100: Color = neutral100Dark,
    neutralColor200: Color = neutral200Dark,
    neutralColor300: Color = neutral300Dark,
    neutralColor400: Color = neutral400Dark,
    neutralColor500: Color = neutral500Dark,
    neutralColor600: Color = neutral600Dark,
    neutralColor700: Color = neutral700Dark,
    neutralColor800: Color = neutral800Dark,
    neutralColor900: Color = neutral900Dark,

    infoColor: Color = infoColorDark,
    successColor: Color = successColorDark,
    warningColor: Color = warningColorDark,
    errorColor: Color = errorColorDark,
    messageReadColor: Color = messageReadColorDark,

    colorWhite: Color = white,
    colorBlack: Color = black,

    backgroundColor1: Color = neutralColor50,
    backgroundColor2: Color = neutralColor100,
    backgroundColor3: Color = neutralColor200,
    backgroundColor4: Color = neutralColor300,

    strokeColorDefault: Color = neutralColor300,
    strokeColorLight: Color = neutralColor200,
    strokeColorDark: Color = neutralColor400,
    strokeColorHighlight: Color = primary,

    textColorPrimary: Color = neutralColor900,
    textColorSecondary: Color = neutralColor600,
    textColorTertiary: Color = neutralColor500,
    textColorDisabled: Color = neutralColor400,
    textColorWhite: Color = neutralColor50,
    textColorHighlight: Color = primary,

    iconTintPrimary: Color = neutralColor900,
    iconTintSecondary: Color = neutralColor500,
    iconTintTertiary: Color = neutralColor400,
    iconTintWhite: Color = neutralColor50,
    iconTintHighlight: Color = primary,

    primaryButtonBackgroundColor: Color = primary,
    primaryButtonIconTint: Color = colorWhite,
    primaryButtonTextColor: Color = colorBlack,
    secondaryButtonBackgroundColor: Color = neutralColor900,
    secondaryButtonIconTint: Color = neutralColor900,
    secondaryButtonTextColor: Color = neutralColor900,
    linkButtonColor: Color = infoColor,
    fabButtonBackgroundColor: Color = primary,
    fabButtonIconTint: Color = colorWhite,
    whiteButtonPressed: Color = neutralColor300

): CometChatColorScheme = CometChatColorScheme(
    primary = primary,
    extendedPrimaryColor50 = extendedPrimaryColor50,
    extendedPrimaryColor100 = extendedPrimaryColor100,
    extendedPrimaryColor200 = extendedPrimaryColor200,
    extendedPrimaryColor300 = extendedPrimaryColor300,
    extendedPrimaryColor400 = extendedPrimaryColor400,
    extendedPrimaryColor500 = extendedPrimaryColor500,
    extendedPrimaryColor600 = extendedPrimaryColor600,
    extendedPrimaryColor700 = extendedPrimaryColor700,
    extendedPrimaryColor800 = extendedPrimaryColor800,
    extendedPrimaryColor900 = extendedPrimaryColor900,

    neutralColor50 = neutralColor50,
    neutralColor100 = neutralColor100,
    neutralColor200 = neutralColor200,
    neutralColor300 = neutralColor300,
    neutralColor400 = neutralColor400,
    neutralColor500 = neutralColor500,
    neutralColor600 = neutralColor600,
    neutralColor700 = neutralColor700,
    neutralColor800 = neutralColor800,
    neutralColor900 = neutralColor900,

    infoColor = infoColor,
    successColor = successColor,
    warningColor = warningColor,
    errorColor = errorColor,
    messageReadColor = messageReadColor,

    backgroundColor1 = backgroundColor1,
    backgroundColor2 = backgroundColor2,
    backgroundColor3 = backgroundColor3,
    backgroundColor4 = backgroundColor4,

    strokeColorDefault = strokeColorDefault,
    strokeColorLight = strokeColorLight,
    strokeColorDark = strokeColorDark,
    strokeColorHighlight = strokeColorHighlight,

    textColorPrimary = textColorPrimary,
    textColorSecondary = textColorSecondary,
    textColorTertiary = textColorTertiary,
    textColorDisabled = textColorDisabled,
    textColorWhite = textColorWhite,
    textColorHighlight = textColorHighlight,

    iconTintPrimary = iconTintPrimary,
    iconTintSecondary = iconTintSecondary,
    iconTintTertiary = iconTintTertiary,
    iconTintWhite = iconTintWhite,
    iconTintHighlight = iconTintHighlight,

    primaryButtonBackgroundColor = primaryButtonBackgroundColor,
    primaryButtonIconTint = primaryButtonIconTint,
    primaryButtonTextColor = primaryButtonTextColor,
    secondaryButtonBackgroundColor = secondaryButtonBackgroundColor,
    secondaryButtonIconTint = secondaryButtonIconTint,
    secondaryButtonTextColor = secondaryButtonTextColor,
    linkButtonColor = linkButtonColor,
    fabButtonBackgroundColor = fabButtonBackgroundColor,
    fabButtonIconTint = fabButtonIconTint,
    whiteButtonPressed = whiteButtonPressed,

    colorWhite = colorWhite,
    colorBlack = colorBlack
)

val LocalColorScheme = staticCompositionLocalOf { lightColorScheme() }

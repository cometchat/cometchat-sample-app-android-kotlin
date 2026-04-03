package com.cometchat.uikit.compose.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

@Immutable
class CometChatTypography(
    val titleBold: TextStyle = CometChatTextAppearanceTitleBold,
    val titleMedium: TextStyle = CometChatTextAppearanceTitleMedium,
    val titleRegular: TextStyle = CometChatTextAppearanceTitleRegular,
    val heading1Bold: TextStyle = CometChatTextAppearanceHeading1Bold,
    val heading1Medium: TextStyle = CometChatTextAppearanceHeading1Medium,
    val heading1Regular: TextStyle = CometChatTextAppearanceHeading1,
    val heading2Bold: TextStyle = CometChatTextAppearanceHeading2Bold,
    val heading2Medium: TextStyle = CometChatTextAppearanceHeading2Medium,
    val heading2Regular: TextStyle = CometChatTextAppearanceHeading2,
    val heading3Bold: TextStyle = CometChatTextAppearanceHeading3Bold,
    val heading3Medium: TextStyle = CometChatTextAppearanceHeading3Medium,
    val heading3Regular: TextStyle = CometChatTextAppearanceHeading3,
    val heading4Bold: TextStyle = CometChatTextAppearanceHeading4Bold,
    val heading4Medium: TextStyle = CometChatTextAppearanceHeading4Medium,
    val heading4Regular: TextStyle = CometChatTextAppearanceHeading4,
    val bodyBold: TextStyle = CometChatTextAppearanceBodyBold,
    val bodyMedium: TextStyle = CometChatTextAppearanceBodyMedium,
    val bodyRegular: TextStyle = CometChatTextAppearanceBody,
    val caption1Bold: TextStyle = CometChatTextAppearanceCaption1Bold,
    val caption1Medium: TextStyle = CometChatTextAppearanceCaption1Medium,
    val caption1Regular: TextStyle = CometChatTextAppearanceCaption1,
    val caption2Bold: TextStyle = CometChatTextAppearanceCaption2Bold,
    val caption2Medium: TextStyle = CometChatTextAppearanceCaption2Medium,
    val caption2Regular: TextStyle = CometChatTextAppearanceCaption2,
    val buttonBold: TextStyle = CometChatTextAppearanceButtonBold,
    val buttonMedium: TextStyle = CometChatTextAppearanceButtonMedium,
    val buttonRegular: TextStyle = CometChatTextAppearanceButton,
    val linkRegular: TextStyle = CometChatTextAppearanceLink,
) {

    override fun toString(): String {
        return "CometChatTypography(" +
            "titleBold=$titleBold, " +
            "titleMedium=$titleMedium, " +
            "titleRegular=$titleRegular, " +
            "heading1Bold=$heading1Bold, " +
            "heading1Medium=$heading1Medium, " +
            "heading1Regular=$heading1Regular, " +
            "heading2Bold=$heading2Bold, " +
            "heading2Medium=$heading2Medium, " +
            "heading2Regular=$heading2Regular, " +
            "heading3Bold=$heading3Bold, " +
            "heading3Medium=$heading3Medium, " +
            "heading3Regular=$heading3Regular, " +
            "heading4Bold=$heading4Bold, " +
            "heading4Medium=$heading4Medium, " +
            "heading4Regular=$heading4Regular, " +
            "bodyBold=$bodyBold, " +
            "bodyMedium=$bodyMedium, " +
            "bodyRegular=$bodyRegular, " +
            "caption1Bold=$caption1Bold, " +
            "caption1Medium=$caption1Medium, " +
            "caption1Regular=$caption1Regular, " +
            "caption2Bold=$caption2Bold, " +
            "caption2Medium=$caption2Medium, " +
            "caption2Regular=$caption2Regular, " +
            "buttonBold=$buttonBold, " +
            "buttonMedium=$buttonMedium, " +
            "buttonRegular=$buttonRegular, " +
            "linkRegular=$linkRegular" +
            ")"
    }
}

val LocalTypography = staticCompositionLocalOf { CometChatTypography() }

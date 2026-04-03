package com.cometchat.uikit.compose.presentation.report

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatFlagMessageDialog.
 *
 * This style class provides full parity with the XML attributes defined in
 * `attr_cometchat_flag_message.xml` from chatuikit-kotlin.
 *
 * Contains all visual styling properties for the flag message dialog component
 * including container, chip, remark field, and button styling.
 */
@Immutable
data class CometChatFlagMessageDialogStyle(
    // Dialog styling
    val backgroundColor: Color,
    val borderRadius: Dp,
    val titleColor: Color,
    val titleTextStyle: TextStyle,
    val subtitleColor: Color,
    val subtitleTextStyle: TextStyle,
    val closeIconColor: Color,
    val strokeColor: Color,
    val strokeWidth: Dp,

    // Chip styling
    val chipCornerRadius: Dp,
    val chipStrokeWidth: Dp,
    val chipTextStyle: TextStyle,
    val chipActiveBackgroundColor: Color,
    val chipInactiveBackgroundColor: Color,
    val chipActiveTextColor: Color,
    val chipInactiveTextColor: Color,
    val chipActiveBorderColor: Color,
    val chipInactiveBorderColor: Color,

    // Separator styling
    val separatorColor: Color,

    // Remark field styling
    val remarkFieldTitleTextColor: Color,
    val remarkFieldTitleTextStyle: TextStyle,
    val remarkFieldHintTextColor: Color,
    val remarkFieldTextColor: Color,
    val remarkFieldTextStyle: TextStyle,
    val remarkFieldBackgroundColor: Color,
    val remarkFieldStrokeColor: Color,

    // Button styling - General
    val buttonCornerRadius: Dp,
    val buttonStrokeColor: Color,
    val buttonStrokeWidth: Dp,

    // Report button styling
    val reportButtonEnabledBackgroundColor: Color,
    val reportButtonDisabledBackgroundColor: Color,
    val reportButtonEnabledTextColor: Color,
    val reportButtonDisabledTextColor: Color,
    val reportButtonTextStyle: TextStyle,

    // Cancel button styling
    val cancelButtonEnabledBackgroundColor: Color,
    val cancelButtonDisabledBackgroundColor: Color,
    val cancelButtonEnabledTextColor: Color,
    val cancelButtonDisabledTextColor: Color,
    val cancelButtonTextStyle: TextStyle,

    // Error styling
    val errorTextColor: Color,
    val errorTextStyle: TextStyle,

    // Progress indicator styling
    val progressIndicatorColor: Color
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         *
         * Default values match the Java CometChatFlagMessage implementation.
         */
        @Composable
        fun default(
            // Dialog styling
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            borderRadius: Dp = 16.dp,
            titleColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading3Medium,
            subtitleColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            closeIconColor: Color = CometChatTheme.colorScheme.iconTintPrimary,
            strokeColor: Color = CometChatTheme.colorScheme.strokeColorDefault,
            strokeWidth: Dp = 1.dp,

            // Chip styling
            chipCornerRadius: Dp = 1000.dp,
            chipStrokeWidth: Dp = 1.dp,
            chipTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            chipActiveBackgroundColor: Color = CometChatTheme.colorScheme.extendedPrimaryColor100,
            chipInactiveBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            chipActiveTextColor: Color = CometChatTheme.colorScheme.textColorHighlight,
            chipInactiveTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            chipActiveBorderColor: Color = CometChatTheme.colorScheme.extendedPrimaryColor200,
            chipInactiveBorderColor: Color = CometChatTheme.colorScheme.strokeColorDefault,

            // Separator styling
            separatorColor: Color = CometChatTheme.colorScheme.strokeColorLight,

            // Remark field styling
            remarkFieldTitleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            remarkFieldTitleTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            remarkFieldHintTextColor: Color = CometChatTheme.colorScheme.textColorTertiary,
            remarkFieldTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            remarkFieldTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            remarkFieldBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            remarkFieldStrokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,

            // Button styling - General
            buttonCornerRadius: Dp = 8.dp,
            buttonStrokeColor: Color = CometChatTheme.colorScheme.strokeColorDark,
            buttonStrokeWidth: Dp = 1.dp,

            // Report button styling
            reportButtonEnabledBackgroundColor: Color = CometChatTheme.colorScheme.primaryButtonBackgroundColor,
            reportButtonDisabledBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor4,
            reportButtonEnabledTextColor: Color = CometChatTheme.colorScheme.colorWhite,
            reportButtonDisabledTextColor: Color = CometChatTheme.colorScheme.colorWhite,
            reportButtonTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,

            // Cancel button styling
            cancelButtonEnabledBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            cancelButtonDisabledBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            cancelButtonEnabledTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            cancelButtonDisabledTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            cancelButtonTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,

            // Error styling
            errorTextColor: Color = CometChatTheme.colorScheme.errorColor,
            errorTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,

            // Progress indicator styling
            progressIndicatorColor: Color = CometChatTheme.colorScheme.colorWhite
        ): CometChatFlagMessageDialogStyle = CometChatFlagMessageDialogStyle(
            backgroundColor = backgroundColor,
            borderRadius = borderRadius,
            titleColor = titleColor,
            titleTextStyle = titleTextStyle,
            subtitleColor = subtitleColor,
            subtitleTextStyle = subtitleTextStyle,
            closeIconColor = closeIconColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            chipCornerRadius = chipCornerRadius,
            chipStrokeWidth = chipStrokeWidth,
            chipTextStyle = chipTextStyle,
            chipActiveBackgroundColor = chipActiveBackgroundColor,
            chipInactiveBackgroundColor = chipInactiveBackgroundColor,
            chipActiveTextColor = chipActiveTextColor,
            chipInactiveTextColor = chipInactiveTextColor,
            chipActiveBorderColor = chipActiveBorderColor,
            chipInactiveBorderColor = chipInactiveBorderColor,
            remarkFieldTitleTextColor = remarkFieldTitleTextColor,
            remarkFieldTitleTextStyle = remarkFieldTitleTextStyle,
            remarkFieldHintTextColor = remarkFieldHintTextColor,
            remarkFieldTextColor = remarkFieldTextColor,
            remarkFieldTextStyle = remarkFieldTextStyle,
            remarkFieldBackgroundColor = remarkFieldBackgroundColor,
            remarkFieldStrokeColor = remarkFieldStrokeColor,
            separatorColor = separatorColor,
            buttonCornerRadius = buttonCornerRadius,
            buttonStrokeColor = buttonStrokeColor,
            buttonStrokeWidth = buttonStrokeWidth,
            reportButtonEnabledBackgroundColor = reportButtonEnabledBackgroundColor,
            reportButtonDisabledBackgroundColor = reportButtonDisabledBackgroundColor,
            reportButtonEnabledTextColor = reportButtonEnabledTextColor,
            reportButtonDisabledTextColor = reportButtonDisabledTextColor,
            reportButtonTextStyle = reportButtonTextStyle,
            cancelButtonEnabledBackgroundColor = cancelButtonEnabledBackgroundColor,
            cancelButtonDisabledBackgroundColor = cancelButtonDisabledBackgroundColor,
            cancelButtonEnabledTextColor = cancelButtonEnabledTextColor,
            cancelButtonDisabledTextColor = cancelButtonDisabledTextColor,
            cancelButtonTextStyle = cancelButtonTextStyle,
            errorTextColor = errorTextColor,
            errorTextStyle = errorTextStyle,
            progressIndicatorColor = progressIndicatorColor
        )
    }
}

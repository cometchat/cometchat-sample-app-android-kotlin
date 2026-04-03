package com.cometchat.uikit.compose.presentation.createpoll.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatCreatePoll component.
 *
 * This immutable data class encapsulates all visual styling properties for the create poll screen,
 * following Kotlin standards and integrating with the CometChatTheme system.
 *
 * @param backgroundColor Background color for the create poll container
 * @param cornerRadius Corner radius for the container
 * @param strokeWidth Stroke width for the container border
 * @param strokeColor Stroke color for the container border
 * @param titleTextColor Text color for the title
 * @param titleTextStyle Text style for the title
 * @param questionTitleTextColor Text color for the "Question" label
 * @param questionTitleTextStyle Text style for the "Question" label
 * @param questionTextColor Text color for the question input
 * @param questionTextStyle Text style for the question input
 * @param questionHintColor Hint color for the question input
 * @param questionBackgroundColor Background color for the question input container
 * @param questionCornerRadius Corner radius for the question input container
 * @param questionStrokeWidth Stroke width for the question input container
 * @param questionStrokeColor Stroke color for the question input container
 * @param optionTitleTextColor Text color for the "Set the answers" label
 * @param optionTitleTextStyle Text style for the "Set the answers" label
 * @param optionTextColor Text color for option inputs
 * @param optionTextStyle Text style for option inputs
 * @param optionHintColor Hint color for option inputs
 * @param optionBackgroundColor Background color for option input containers
 * @param optionCornerRadius Corner radius for option input containers
 * @param optionStrokeWidth Stroke width for option input containers
 * @param optionStrokeColor Stroke color for option input containers
 * @param dragIconTint Tint color for the drag handle icon
 * @param backIconTint Tint color for the back button icon
 * @param separatorColor Color for the separator line
 * @param submitButtonBackgroundColor Background color for the submit button
 * @param submitButtonDisabledBackgroundColor Background color for the disabled submit button
 * @param submitButtonTextColor Text color for the submit button
 * @param submitButtonTextStyle Text style for the submit button
 * @param submitButtonCornerRadius Corner radius for the submit button
 * @param submitButtonStrokeWidth Stroke width for the submit button
 * @param submitButtonStrokeColor Stroke color for the submit button
 * @param progressIndicatorColor Color for the progress indicator
 * @param errorTextColor Text color for error messages
 * @param errorTextStyle Text style for error messages
 */
@Immutable
data class CometChatCreatePollStyle(
    // Container
    val backgroundColor: Color,
    val cornerRadius: Dp,
    val strokeWidth: Dp,
    val strokeColor: Color,
    
    // Title
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    
    // Question section
    val questionTitleTextColor: Color,
    val questionTitleTextStyle: TextStyle,
    val questionTextColor: Color,
    val questionTextStyle: TextStyle,
    val questionHintColor: Color,
    val questionBackgroundColor: Color,
    val questionCornerRadius: Dp,
    val questionStrokeWidth: Dp,
    val questionStrokeColor: Color,
    
    // Options section
    val optionTitleTextColor: Color,
    val optionTitleTextStyle: TextStyle,
    val optionTextColor: Color,
    val optionTextStyle: TextStyle,
    val optionHintColor: Color,
    val optionBackgroundColor: Color,
    val optionCornerRadius: Dp,
    val optionStrokeWidth: Dp,
    val optionStrokeColor: Color,
    
    // Icons
    val dragIconTint: Color,
    val backIconTint: Color,
    
    // Separator
    val separatorColor: Color,
    
    // Submit button
    val submitButtonBackgroundColor: Color,
    val submitButtonDisabledBackgroundColor: Color,
    val submitButtonTextColor: Color,
    val submitButtonTextStyle: TextStyle,
    val submitButtonCornerRadius: Dp,
    val submitButtonStrokeWidth: Dp,
    val submitButtonStrokeColor: Color,
    
    // Progress indicator
    val progressIndicatorColor: Color,
    
    // Error
    val errorTextColor: Color,
    val errorTextStyle: TextStyle
) {
    companion object {
        /**
         * Creates a default CometChatCreatePollStyle with values sourced from CometChatTheme.
         *
         * @return A new CometChatCreatePollStyle instance with theme-based default values
         */
        @Composable
        fun default(
            // Container
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            cornerRadius: Dp = 0.dp,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            
            // Title
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading3Bold,
            
            // Question section
            questionTitleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            questionTitleTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            questionTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            questionTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            questionHintColor: Color = CometChatTheme.colorScheme.textColorTertiary,
            questionBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            questionCornerRadius: Dp = 8.dp,
            questionStrokeWidth: Dp = 1.dp,
            questionStrokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            
            // Options section
            optionTitleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            optionTitleTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            optionTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            optionTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            optionHintColor: Color = CometChatTheme.colorScheme.textColorTertiary,
            optionBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            optionCornerRadius: Dp = 8.dp,
            optionStrokeWidth: Dp = 1.dp,
            optionStrokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            
            // Icons
            dragIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            backIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            
            // Separator
            separatorColor: Color = CometChatTheme.colorScheme.strokeColorDefault,
            
            // Submit button
            submitButtonBackgroundColor: Color = CometChatTheme.colorScheme.primary,
            submitButtonDisabledBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor4,
            submitButtonTextColor: Color = CometChatTheme.colorScheme.colorWhite,
            submitButtonTextStyle: TextStyle = CometChatTheme.typography.buttonMedium,
            submitButtonCornerRadius: Dp = 8.dp,
            submitButtonStrokeWidth: Dp = 0.dp,
            submitButtonStrokeColor: Color = Color.Transparent,
            
            // Progress indicator
            progressIndicatorColor: Color = CometChatTheme.colorScheme.colorWhite,
            
            // Error
            errorTextColor: Color = CometChatTheme.colorScheme.errorColor,
            errorTextStyle: TextStyle = CometChatTheme.typography.caption1Regular
        ): CometChatCreatePollStyle = CometChatCreatePollStyle(
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            questionTitleTextColor = questionTitleTextColor,
            questionTitleTextStyle = questionTitleTextStyle,
            questionTextColor = questionTextColor,
            questionTextStyle = questionTextStyle,
            questionHintColor = questionHintColor,
            questionBackgroundColor = questionBackgroundColor,
            questionCornerRadius = questionCornerRadius,
            questionStrokeWidth = questionStrokeWidth,
            questionStrokeColor = questionStrokeColor,
            optionTitleTextColor = optionTitleTextColor,
            optionTitleTextStyle = optionTitleTextStyle,
            optionTextColor = optionTextColor,
            optionTextStyle = optionTextStyle,
            optionHintColor = optionHintColor,
            optionBackgroundColor = optionBackgroundColor,
            optionCornerRadius = optionCornerRadius,
            optionStrokeWidth = optionStrokeWidth,
            optionStrokeColor = optionStrokeColor,
            dragIconTint = dragIconTint,
            backIconTint = backIconTint,
            separatorColor = separatorColor,
            submitButtonBackgroundColor = submitButtonBackgroundColor,
            submitButtonDisabledBackgroundColor = submitButtonDisabledBackgroundColor,
            submitButtonTextColor = submitButtonTextColor,
            submitButtonTextStyle = submitButtonTextStyle,
            submitButtonCornerRadius = submitButtonCornerRadius,
            submitButtonStrokeWidth = submitButtonStrokeWidth,
            submitButtonStrokeColor = submitButtonStrokeColor,
            progressIndicatorColor = progressIndicatorColor,
            errorTextColor = errorTextColor,
            errorTextStyle = errorTextStyle
        )
    }
}

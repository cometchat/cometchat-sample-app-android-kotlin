package com.cometchat.uikit.compose.presentation.shared.mediarecorder

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTypography

/**
 * Configuration class for customizing the appearance of CometChatMediaRecorder.
 *
 * @param backgroundColor Background color for the media recorder container.
 *                        If null, uses backgroundColor1 from theme
 * @param strokeWidth Width of the recorder border. Default is 1.dp
 * @param strokeColor Color of the recorder border/stroke. If null, uses strokeColorLight from theme
 * @param cornerRadius Corner radius of the recorder container. Default is 6.dp
 * @param recordingIconTint Tint color for the recording icon. If null, uses colorWhite from theme
 * @param recordingIconBackgroundColor Background color for the recording icon. If null, uses iconTintHighlight from theme
 * @param textColor Color of the timer text. If null, uses textColorPrimary from theme
 * @param textStyle Typography style for the timer text. If null, uses heading4Regular from theme
 * @param deleteIconTint Tint color for the delete icon. If null, uses iconTintSecondary from theme
 * @param deleteIconBackgroundColor Background color for the delete icon. If null, uses backgroundColor1 from theme
 * @param deleteIconRadius Corner radius for the delete icon button. Default is 20.dp (circular)
 * @param deleteIconStrokeWidth Width of the delete icon border. Default is 1.dp
 * @param deleteIconStrokeColor Color of the delete icon border. If null, uses strokeColorLight from theme
 * @param deleteIconElevation Elevation for the delete icon button. Default is 4.dp
 * @param startIconTint Tint color for the start/play icon. If null, uses errorColor from theme
 * @param startIconBackgroundColor Background color for the start icon. If null, uses backgroundColor1 from theme
 * @param startIconRadius Corner radius for the start icon button. Default is 24.dp (circular)
 * @param startIconStrokeWidth Width of the start icon border. Default is 1.dp
 * @param startIconStrokeColor Color of the start icon border. If null, uses strokeColorLight from theme
 * @param startIconElevation Elevation for the start icon button. Default is 4.dp
 * @param pauseIconTint Tint color for the pause icon. If null, uses errorColor from theme
 * @param pauseIconBackgroundColor Background color for the pause icon. If null, uses backgroundColor1 from theme
 * @param pauseIconRadius Corner radius for the pause icon button. Default is 24.dp (circular)
 * @param pauseIconStrokeWidth Width of the pause icon border. Default is 1.dp
 * @param pauseIconStrokeColor Color of the pause icon border. If null, uses strokeColorLight from theme
 * @param pauseIconElevation Elevation for the pause icon button. Default is 4.dp
 * @param stopIconTint Tint color for the stop icon. If null, uses iconTintSecondary from theme
 * @param stopIconBackgroundColor Background color for the stop icon. If null, uses backgroundColor1 from theme
 * @param stopIconRadius Corner radius for the stop icon button. Default is 20.dp (circular)
 * @param stopIconStrokeWidth Width of the stop icon border. Default is 1.dp
 * @param stopIconStrokeColor Color of the stop icon border. If null, uses strokeColorLight from theme
 * @param stopIconElevation Elevation for the stop icon button. Default is 4.dp
 * @param sendIconTint Tint color for the send icon. If null, uses iconTintHighlight from theme
 * @param sendIconBackgroundColor Background color for the send icon. If null, uses backgroundColor1 from theme
 * @param sendIconRadius Corner radius for the send icon button. Default is 24.dp (circular)
 * @param sendIconStrokeWidth Width of the send icon border. Default is 1.dp
 * @param sendIconStrokeColor Color of the send icon border. If null, uses strokeColorLight from theme
 * @param sendIconElevation Elevation for the send icon button. Default is 4.dp
 * @param restartIconTint Tint color for the restart icon. If null, uses iconTintSecondary from theme
 * @param restartIconBackgroundColor Background color for the restart icon. If null, uses backgroundColor1 from theme
 * @param restartIconRadius Corner radius for the restart icon button. Default is 20.dp (circular)
 * @param restartIconStrokeWidth Width of the restart icon border. Default is 1.dp
 * @param restartIconStrokeColor Color of the restart icon border. If null, uses strokeColorLight from theme
 * @param restartIconElevation Elevation for the restart icon button. Default is 4.dp
 * @param messageBubbleBackgroundColor Background color for the audio message bubble. If null, uses extendedPrimaryColor500 from theme
 * @param messageBubbleCornerRadius Corner radius for the audio message bubble. Default is 8.dp
 * @param messageBubbleStrokeWidth Width of the message bubble border. Default is 0.dp
 * @param messageBubbleStrokeColor Color of the message bubble border. If null, uses strokeColorLight from theme
 * @param rippleColor Color for the ripple animation. If null, uses recordingIconBackgroundColor
 * @param rippleCount Number of ripple circles in the animation. Default is 3
 * @param rippleAnimationDuration Duration of the ripple animation in milliseconds. Default is 1800
 * @param typography Custom typography. If null, uses theme's typography
 */
@Immutable
data class MediaRecorderStyle(
    // Container styling
    val backgroundColor: Color? = null,
    val strokeWidth: Dp = 1.dp,
    val strokeColor: Color? = null,
    val cornerRadius: Dp = 6.dp,
    
    // Recording icon styling
    val recordingIconTint: Color? = null,
    val recordingIconBackgroundColor: Color? = null,
    
    // Text styling
    val textColor: Color? = null,
    val textStyle: TextStyle? = null,
    
    // Delete icon styling
    val deleteIconTint: Color? = null,
    val deleteIconBackgroundColor: Color? = null,
    val deleteIconRadius: Dp = 20.dp,
    val deleteIconStrokeWidth: Dp = 1.dp,
    val deleteIconStrokeColor: Color? = null,
    val deleteIconElevation: Dp = 4.dp,
    
    // Start icon styling
    val startIconTint: Color? = null,
    val startIconBackgroundColor: Color? = null,
    val startIconRadius: Dp = 24.dp,
    val startIconStrokeWidth: Dp = 1.dp,
    val startIconStrokeColor: Color? = null,
    val startIconElevation: Dp = 4.dp,
    
    // Pause icon styling
    val pauseIconTint: Color? = null,
    val pauseIconBackgroundColor: Color? = null,
    val pauseIconRadius: Dp = 24.dp,
    val pauseIconStrokeWidth: Dp = 1.dp,
    val pauseIconStrokeColor: Color? = null,
    val pauseIconElevation: Dp = 4.dp,
    
    // Stop icon styling
    val stopIconTint: Color? = null,
    val stopIconBackgroundColor: Color? = null,
    val stopIconRadius: Dp = 20.dp,
    val stopIconStrokeWidth: Dp = 1.dp,
    val stopIconStrokeColor: Color? = null,
    val stopIconElevation: Dp = 4.dp,
    
    // Send icon styling
    val sendIconTint: Color? = null,
    val sendIconBackgroundColor: Color? = null,
    val sendIconRadius: Dp = 24.dp,
    val sendIconStrokeWidth: Dp = 1.dp,
    val sendIconStrokeColor: Color? = null,
    val sendIconElevation: Dp = 4.dp,
    
    // Restart icon styling
    val restartIconTint: Color? = null,
    val restartIconBackgroundColor: Color? = null,
    val restartIconRadius: Dp = 20.dp,
    val restartIconStrokeWidth: Dp = 1.dp,
    val restartIconStrokeColor: Color? = null,
    val restartIconElevation: Dp = 4.dp,
    
    // Message bubble styling
    val messageBubbleBackgroundColor: Color? = null,
    val messageBubbleCornerRadius: Dp = 8.dp,
    val messageBubbleStrokeWidth: Dp = 0.dp,
    val messageBubbleStrokeColor: Color? = null,
    
    // Ripple animation styling
    val rippleColor: Color? = null,
    val rippleCount: Int = 3,
    val rippleAnimationDuration: Int = 1800,
    
    // Typography
    val typography: CometChatTypography? = null
)
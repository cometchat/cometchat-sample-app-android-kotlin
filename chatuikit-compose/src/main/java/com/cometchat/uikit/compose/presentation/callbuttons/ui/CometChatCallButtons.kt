package com.cometchat.uikit.compose.presentation.callbuttons.ui

import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.callbuttons.style.CallButtonStyle
import com.cometchat.uikit.compose.presentation.callbuttons.style.CometChatCallButtonsStyle
import com.cometchat.uikit.core.factory.CometChatCallButtonsViewModelFactory
import com.cometchat.uikit.core.state.CallButtonsEvent
import com.cometchat.uikit.core.viewmodel.CometChatCallButtonsViewModel

/**
 * CometChatCallButtons is a composable that displays voice and video call buttons
 * for initiating calls with users or groups.
 *
 * @param modifier Modifier for the component
 * @param viewModel Optional ViewModel instance. If not provided, a default one will be created.
 * @param style Style configuration for the call buttons
 * @param user The User to call (for 1-to-1 calls)
 * @param group The Group to call (for conference calls)
 * @param voiceCallButtonVisibility Visibility of the voice call button (View.VISIBLE, View.INVISIBLE, View.GONE)
 * @param videoCallButtonVisibility Visibility of the video call button (View.VISIBLE, View.INVISIBLE, View.GONE)
 * @param voiceButtonText Optional text for the voice call button
 * @param videoButtonText Optional text for the video call button
 * @param buttonTextVisibility Visibility of button text (View.VISIBLE or View.GONE)
 * @param buttonIconVisibility Visibility of button icons (View.VISIBLE or View.GONE)
 * @param onVoiceCallClick Custom click handler for voice call button. If set, overrides default behavior.
 * @param onVideoCallClick Custom click handler for video call button. If set, overrides default behavior.
 * @param onCallInitiated Callback when a user call is successfully initiated
 * @param onStartDirectCall Callback when a group call message is successfully sent
 * @param onError Callback when an error occurs
 */
@Composable
fun CometChatCallButtons(
    modifier: Modifier = Modifier,
    viewModel: CometChatCallButtonsViewModel? = null,
    style: CometChatCallButtonsStyle = CometChatCallButtonsStyle.default(),

    // Entity configuration
    user: User? = null,
    group: Group? = null,

    // Button visibility
    voiceCallButtonVisibility: Int = View.VISIBLE,
    videoCallButtonVisibility: Int = View.VISIBLE,

    // Button text
    voiceButtonText: String? = null,
    videoButtonText: String? = null,
    buttonTextVisibility: Int = View.GONE,
    buttonIconVisibility: Int = View.VISIBLE,

    // Custom click handlers
    onVoiceCallClick: ((User?, Group?) -> Unit)? = null,
    onVideoCallClick: ((User?, Group?) -> Unit)? = null,

    // Callbacks
    onCallInitiated: ((Call) -> Unit)? = null,
    onStartDirectCall: ((BaseMessage) -> Unit)? = null,
    onError: ((CometChatException) -> Unit)? = null
) {
    val context = LocalContext.current
    val actualViewModel = viewModel ?: viewModel(factory = CometChatCallButtonsViewModelFactory())
    
    // Track if we've already launched the call screen to prevent double launches
    val hasLaunchedCallScreen = remember { mutableStateOf(false) }

    // Set user or group
    LaunchedEffect(user) {
        user?.let { actualViewModel.setUser(it) }
    }

    LaunchedEffect(group) {
        group?.let { actualViewModel.setGroup(it) }
    }

    // Collect events
    LaunchedEffect(Unit) {
        actualViewModel.events.collect { event ->
            when (event) {
                is CallButtonsEvent.CallInitiated -> {
                    if (onCallInitiated != null) {
                        // Custom callback provided - let the app handle it
                        onCallInitiated.invoke(event.call)
                    } else if (!hasLaunchedCallScreen.value) {
                        // No custom callback - automatically launch outgoing call screen
                        // This matches the Java UIKit behavior where CallingExtensionDecorator
                        // automatically handles the call flow
                        hasLaunchedCallScreen.value = true
                        com.cometchat.uikit.compose.calls.CometChatCallActivity.launchOutgoingCallScreen(
                            context,
                            event.call,
                            null
                        )
                    }
                }
                is CallButtonsEvent.StartDirectCall -> {
                    if (onStartDirectCall != null) {
                        // Custom callback provided - let the app handle it
                        onStartDirectCall.invoke(event.message)
                    } else if (!hasLaunchedCallScreen.value) {
                        // No custom callback - automatically launch conference call screen
                        hasLaunchedCallScreen.value = true
                        com.cometchat.uikit.compose.calls.CometChatCallActivity.launchConferenceCallScreen(
                            context,
                            event.message,
                            null
                        )
                    }
                }
                is CallButtonsEvent.CallRejected -> {
                    // Call rejection handled internally - reset the flag
                    hasLaunchedCallScreen.value = false
                }
            }
        }
    }

    // Collect errors
    LaunchedEffect(Unit) {
        actualViewModel.errorEvent.collect { exception ->
            onError?.invoke(exception)
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(style.marginBetweenButtons),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Voice Call Button
        if (voiceCallButtonVisibility == View.VISIBLE) {
            CallButton(
                style = style.voiceCallButtonStyle,
                text = voiceButtonText,
                showText = buttonTextVisibility == View.VISIBLE,
                showIcon = buttonIconVisibility == View.VISIBLE,
                contentDescription = "Voice Call",
                onClick = {
                    if (onVoiceCallClick != null) {
                        onVoiceCallClick.invoke(
                            actualViewModel.getUser(),
                            actualViewModel.getGroup()
                        )
                    } else {
                        actualViewModel.initiateCall(CometChatConstants.CALL_TYPE_AUDIO)
                    }
                }
            )
        }

        // Video Call Button
        if (videoCallButtonVisibility == View.VISIBLE) {
            CallButton(
                style = style.videoCallButtonStyle,
                text = videoButtonText,
                showText = buttonTextVisibility == View.VISIBLE,
                showIcon = buttonIconVisibility == View.VISIBLE,
                contentDescription = "Video Call",
                onClick = {
                    if (onVideoCallClick != null) {
                        onVideoCallClick.invoke(
                            actualViewModel.getUser(),
                            actualViewModel.getGroup()
                        )
                    } else {
                        actualViewModel.initiateCall(CometChatConstants.CALL_TYPE_VIDEO)
                    }
                }
            )
        }
    }
}

/**
 * Internal composable for rendering an individual call button.
 */
@Composable
private fun CallButton(
    style: CallButtonStyle,
    text: String?,
    showText: Boolean,
    showIcon: Boolean,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            },
        shape = RoundedCornerShape(style.cornerRadius),
        color = style.backgroundColor,
        border = if (style.strokeWidth > 0.dp) {
            BorderStroke(style.strokeWidth, style.strokeColor)
        } else null
    ) {
        Row(
            modifier = Modifier.padding(style.buttonPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showIcon && style.icon != null) {
                Icon(
                    painter = style.icon,
                    contentDescription = null,
                    modifier = Modifier.size(style.iconSize),
                    tint = style.iconTint
                )
            }

            if (showText && !text.isNullOrEmpty()) {
                if (showIcon && style.icon != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = text,
                    color = style.textColor,
                    style = style.textStyle
                )
            }
        }
    }
}

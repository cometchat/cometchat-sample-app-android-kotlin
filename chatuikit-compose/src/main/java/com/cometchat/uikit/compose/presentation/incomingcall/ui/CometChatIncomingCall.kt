package com.cometchat.uikit.compose.presentation.incomingcall.ui

import android.app.Activity
import androidx.annotation.RawRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.incomingcall.style.CometChatIncomingCallStyle
import com.cometchat.uikit.compose.presentation.ongoingcall.ui.CometChatOngoingCallActivity
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.core.constants.UIKitConstants.CallWorkFlow
import com.cometchat.uikit.core.resources.soundmanager.CometChatSoundManager
import com.cometchat.uikit.core.resources.soundmanager.Sound
import com.cometchat.uikit.core.viewmodel.CometChatIncomingCallViewModel

/**
 * CometChatIncomingCall is a Jetpack Compose component that displays an incoming call UI.
 * 
 * This component displays:
 * - Caller's name and avatar
 * - Call type (audio/video) with appropriate icon
 * - Accept and Decline buttons
 * 
 * The UI is designed to be identical to the Java implementation in chatuikit.
 * 
 * Features:
 * - Displays caller information from Call.getCallInitiator()
 * - Shows call type icon (audio/video)
 * - Accept button with success color background
 * - Decline button with error color background
 * - Sound management for incoming call ringtone
 * - Auto-launches CometChatOngoingCallActivity when call is accepted (if no custom callback)
 * - Custom view slots for customization
 * - Full accessibility support
 * 
 * @param call The incoming Call object containing caller information
 * @param modifier Modifier for the component container
 * @param viewModel Optional ViewModel for managing call state. If null, a default one is created
 * @param style Style configuration for the component appearance
 * @param disableSoundForCalls Whether to disable incoming call sound. Default is false
 * @param customSoundForCalls Custom sound resource ID for incoming call. Pass 0 to use default
 * @param callSettingsBuilder Call settings builder for ongoing call configuration
 * @param itemView Custom composable to replace the entire item content
 * @param leadingView Custom composable for the leading area (left side)
 * @param titleView Custom composable for the title (caller name)
 * @param subtitleView Custom composable for the subtitle (call type)
 * @param trailingView Custom composable for the trailing area (avatar)
 * @param onAcceptClick Callback when accept button is clicked. If null, auto-launches ongoing call
 * @param onRejectClick Callback when decline button is clicked. If null, uses viewModel.rejectCall()
 * @param onError Callback for error events from the ViewModel
 * 
 * Validates: Requirements 7.1-7.21, 2.3, 2.5
 */
@Composable
fun CometChatIncomingCall(
    call: Call,
    modifier: Modifier = Modifier,
    viewModel: CometChatIncomingCallViewModel? = null,
    style: CometChatIncomingCallStyle = CometChatIncomingCallStyle.default(),
    // Sound configuration
    disableSoundForCalls: Boolean = false,
    @RawRes customSoundForCalls: Int = 0,
    // Call settings
    callSettingsBuilder: Any? = null,
    // Custom views
    itemView: (@Composable (Call) -> Unit)? = null,
    leadingView: (@Composable (Call) -> Unit)? = null,
    titleView: (@Composable (Call) -> Unit)? = null,
    subtitleView: (@Composable (Call) -> Unit)? = null,
    trailingView: (@Composable (Call) -> Unit)? = null,
    // Callbacks
    onAcceptClick: ((Call) -> Unit)? = null,
    onRejectClick: ((Call) -> Unit)? = null,
    onError: ((CometChatException) -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Create or use provided ViewModel - use remember to ensure stable instance
    // Note: We use remember instead of viewModel() because the incoming call UI
    // is shown as an overlay and needs its own ViewModel instance per call
    val incomingCallViewModel = remember(call.sessionId) {
        (viewModel ?: CometChatIncomingCallViewModel(enableListeners = true)).also {
            // Set the call immediately to avoid race condition with button click
            it.setCall(call)
        }
    }
    
    // Update call in ViewModel if it changes (e.g., call object updated)
    LaunchedEffect(call) {
        incomingCallViewModel.setCall(call)
    }
    
    // Clean up ViewModel listeners when disposed
    DisposableEffect(incomingCallViewModel) {
        onDispose {
            incomingCallViewModel.removeListeners()
        }
    }
    
    // Sound manager for incoming call sound
    val soundManager = remember { CometChatSoundManager(context) }
    
    // Play incoming call sound when component is displayed
    LaunchedEffect(disableSoundForCalls, customSoundForCalls) {
        if (!disableSoundForCalls) {
            soundManager.play(Sound.INCOMING_CALL, customSoundForCalls)
        }
    }
    
    // Stop sound when component is disposed
    DisposableEffect(Unit) {
        onDispose {
            soundManager.pauseSilently()
        }
    }
    
    // Observe error events
    val errorEvent by incomingCallViewModel.errorEvent.collectAsState(initial = null)
    LaunchedEffect(errorEvent) {
        errorEvent?.let { exception ->
            onError?.invoke(exception)
        }
    }
    
    // Observe accepted call and ALWAYS launch ongoing call activity
    // Then invoke onAcceptClick callback if provided
    // Bug fix: Finish the current activity after launching ongoing call to prevent
    // blank screen when call ends
    // Validates: Requirements 2.1, 2.2, 2.3
    val acceptedCall by incomingCallViewModel.acceptedCall.collectAsState()
    LaunchedEffect(acceptedCall) {
        acceptedCall?.let { accepted ->
            // Pause sound before launching ongoing call
            soundManager.pauseSilently()
            // Always launch ongoing call activity with correct parameters
            CometChatOngoingCallActivity.launchOngoingCallActivity(
                context,
                accepted.sessionId,
                accepted.type,
                CallWorkFlow.DEFAULT,
                callSettingsBuilder as? CometChatCalls.CallSettingsBuilder,
                null
            )
            // Invoke callback if provided (for custom app logic after call starts)
            onAcceptClick?.invoke(accepted)
            // Finish the current activity to prevent it from showing when ongoing call ends
            (context as? Activity)?.finish()
        }
    }
    
    // Get caller information
    val caller = call.callInitiator as? User
    val callerName = caller?.name ?: ""
    val callerAvatar = caller?.avatar
    val isAudioCall = call.type == CometChatConstants.CALL_TYPE_AUDIO
    
    // Get localized strings
    val acceptText = stringResource(R.string.cometchat_incoming_call_accept)
    val declineText = stringResource(R.string.cometchat_incoming_call_decline)
    val callTypeText = if (isAudioCall) {
        stringResource(R.string.cometchat_incoming_call_audio)
    } else {
        stringResource(R.string.cometchat_incoming_call_video)
    }
    val incomingCallTypeText = stringResource(R.string.cometchat_incoming_call_type, callTypeText)
    
    // Accessibility descriptions
    val acceptButtonDescription = "$acceptText $callerName"
    val declineButtonDescription = "$declineText $callerName"
    val callTypeDescription = incomingCallTypeText
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Incoming call from $callerName"
            },
        shape = RoundedCornerShape(style.cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = style.backgroundColor
        ),
        border = if (style.strokeWidth > 0.dp) {
            BorderStroke(style.strokeWidth, style.strokeColor)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp) // cometchat_padding_5 = 20dp
        ) {
            // Item view - either custom or default
            if (itemView != null) {
                itemView(call)
            } else {
                // Default item layout matching Java implementation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Leading view
                    if (leadingView != null) {
                        leadingView(call)
                    }
                    
                    // Title and Subtitle column
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Title view (caller name)
                        if (titleView != null) {
                            titleView(call)
                        } else {
                            Text(
                                text = callerName,
                                style = style.titleTextStyle,
                                color = style.titleTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.semantics {
                                    contentDescription = "Caller: $callerName"
                                }
                            )
                        }
                        
                        // Subtitle view (call type)
                        if (subtitleView != null) {
                            subtitleView(call)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.semantics {
                                    contentDescription = callTypeDescription
                                }
                            ) {
                                // Call type icon
                                val callIcon = if (isAudioCall) {
                                    style.voiceCallIcon
                                } else {
                                    style.videoCallIcon
                                }
                                
                                callIcon?.let { icon ->
                                    Icon(
                                        painter = icon,
                                        contentDescription = null,
                                        tint = style.iconTint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp)) // cometchat_margin_2 = 8dp
                                }
                                
                                Text(
                                    text = incomingCallTypeText,
                                    style = style.subtitleTextStyle,
                                    color = style.subtitleTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    // Trailing view (avatar)
                    Spacer(modifier = Modifier.width(32.dp))
                    
                    if (trailingView != null) {
                        trailingView(call)
                    } else {
                        CometChatAvatar(
                            name = callerName,
                            avatarUrl = callerAvatar,
                            style = style.avatarStyle,
                            modifier = Modifier
                                .size(48.dp)
                                .semantics {
                                    contentDescription = "Avatar of $callerName"
                                }
                        )
                    }
                }
            }
            
            // Action buttons row - cometchat_margin_2 = 8dp spacing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp) // 8dp margin on each side = 16dp total
            ) {
                // Decline button
                Button(
                    onClick = {
                        if (onRejectClick != null) {
                            onRejectClick(call)
                        } else {
                            incomingCallViewModel.rejectCall()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = declineButtonDescription
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = style.rejectButtonBackgroundColor
                    ),
                    shape = RoundedCornerShape(8.dp), // cometchat_radius_2 = 8dp
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp) // cometchat_padding_5 = 20dp, cometchat_padding_2 = 8dp
                ) {
                    Text(
                        text = declineText,
                        style = style.rejectButtonTextStyle,
                        color = style.rejectButtonTextColor
                    )
                }
                
                // Accept button
                // Always call viewModel.acceptCall() to trigger SDK accept flow
                // The LaunchedEffect(acceptedCall) handles launching ongoing call and invoking callback
                Button(
                    onClick = {
                        incomingCallViewModel.acceptCall()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = acceptButtonDescription
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = style.acceptButtonBackgroundColor
                    ),
                    shape = RoundedCornerShape(8.dp), // cometchat_radius_2 = 8dp
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp) // cometchat_padding_5 = 20dp, cometchat_padding_2 = 8dp
                ) {
                    Text(
                        text = acceptText,
                        style = style.acceptButtonTextStyle,
                        color = style.acceptButtonTextColor
                    )
                }
            }
        }
    }
}

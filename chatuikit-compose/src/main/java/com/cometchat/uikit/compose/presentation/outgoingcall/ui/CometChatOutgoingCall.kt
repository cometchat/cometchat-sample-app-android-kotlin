package com.cometchat.uikit.compose.presentation.outgoingcall.ui

import android.app.Activity
import androidx.annotation.RawRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.ongoingcall.ui.CometChatOngoingCallActivity
import com.cometchat.uikit.compose.presentation.outgoingcall.style.CometChatOutgoingCallStyle
import com.cometchat.uikit.core.constants.UIKitConstants.CallWorkFlow
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.core.resources.soundmanager.CometChatSoundManager
import com.cometchat.uikit.core.resources.soundmanager.Sound
import com.cometchat.uikit.core.viewmodel.CometChatOutgoingCallViewModel

/**
 * CometChatOutgoingCall is a Jetpack Compose component that displays an outgoing call UI.
 * 
 * This component displays:
 * - Recipient's name (centered at top)
 * - "Calling..." subtitle text (centered below name)
 * - Recipient's avatar (large, centered)
 * - End Call button (circular, at bottom)
 * 
 * The UI is designed to be identical to the Java implementation in chatuikit.
 * 
 * Features:
 * - Displays recipient information from Call.getReceiver()
 * - Shows "Calling..." subtitle text
 * - End Call button with error color background
 * - End Call button disabled when endCallButtonEnabled is false
 * - Sound management for outgoing call ringtone
 * - Transition to CometChatOngoingCall when call is accepted
 * - Custom view slots for customization
 * - Full accessibility support
 * 
 * @param call The outgoing Call object containing recipient information
 * @param modifier Modifier for the component container
 * @param viewModel Optional ViewModel for managing call state. If null, a default one is created
 * @param style Style configuration for the component appearance
 * @param disableSoundForCalls Whether to disable outgoing call sound. Default is false
 * @param customSoundForCalls Custom sound resource ID for outgoing call. Pass 0 to use default
 * @param callSettingsBuilder Call settings builder for ongoing call configuration
 * @param titleView Custom composable for the title (recipient name)
 * @param subtitleView Custom composable for the subtitle ("Calling...")
 * @param avatarView Custom composable for the avatar
 * @param endCallView Custom composable for the end call button
 * @param onEndCallClick Callback when end call button is clicked. If null, uses viewModel.cancelCall()
 * @param onError Callback for error events from the ViewModel
 * @param onBackPress Callback when back is pressed or call is rejected
 * @param onCallAccepted Callback when the call is accepted by the recipient
 * 
 * Validates: Requirements 11.1-11.21
 */
@Composable
fun CometChatOutgoingCall(
    call: Call,
    modifier: Modifier = Modifier,
    viewModel: CometChatOutgoingCallViewModel? = null,
    style: CometChatOutgoingCallStyle = CometChatOutgoingCallStyle.default(),
    // Sound configuration
    disableSoundForCalls: Boolean = false,
    @RawRes customSoundForCalls: Int = 0,
    // Call settings
    callSettingsBuilder: Any? = null,
    // Custom views
    titleView: (@Composable (Call) -> Unit)? = null,
    subtitleView: (@Composable (Call) -> Unit)? = null,
    avatarView: (@Composable (Call) -> Unit)? = null,
    endCallView: (@Composable (Call) -> Unit)? = null,
    // Callbacks
    onEndCallClick: ((Call) -> Unit)? = null,
    onError: ((CometChatException) -> Unit)? = null,
    onBackPress: (() -> Unit)? = null,
    onCallAccepted: ((Call) -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Create or use provided ViewModel
    val outgoingCallViewModel = remember {
        viewModel ?: CometChatOutgoingCallViewModel()
    }
    
    // Set the call in ViewModel
    LaunchedEffect(call) {
        outgoingCallViewModel.setCall(call)
    }
    
    // Sound manager for outgoing call sound
    val soundManager = remember { CometChatSoundManager(context) }
    
    // Observe ViewModel states
    val endCallButtonEnabled by outgoingCallViewModel.endCallButtonEnabled.collectAsState()
    val acceptedCall by outgoingCallViewModel.acceptedCall.collectAsState()
    val rejectedCall by outgoingCallViewModel.rejectedCall.collectAsState()
    val errorEvent by outgoingCallViewModel.errorEvent.collectAsState(initial = null)
    
    // Play outgoing call sound when component is displayed
    LaunchedEffect(disableSoundForCalls, customSoundForCalls) {
        if (!disableSoundForCalls) {
            soundManager.play(Sound.OUTGOING_CALL, customSoundForCalls)
        }
    }
    
    // Stop sound when component is disposed
    DisposableEffect(Unit) {
        onDispose {
            soundManager.pauseSilently()
        }
    }
    
    // Handle call accepted - transition to ongoing call
    // Bug fix: When onCallAccepted is null, automatically launch CometChatOngoingCallActivity
    // and finish the current activity to prevent blank screen when call ends
    // Validates: Requirements 2.4, 2.5
    LaunchedEffect(acceptedCall) {
        acceptedCall?.let { accepted ->
            soundManager.pauseSilently()
            if (onCallAccepted != null) {
                // Custom callback provided - invoke it
                onCallAccepted.invoke(accepted)
            } else {
                // No custom callback - auto-launch ongoing call activity
                CometChatOngoingCallActivity.launchOngoingCallActivity(
                    context,
                    accepted.sessionId,
                    accepted.type,
                    CallWorkFlow.DEFAULT,
                    callSettingsBuilder as? CometChatCalls.CallSettingsBuilder,
                    null
                )
                // Finish the current activity to prevent it from showing when ongoing call ends
                (context as? Activity)?.finish()
            }
        }
    }
    
    // Handle call rejected - invoke back press callback
    LaunchedEffect(rejectedCall) {
        rejectedCall?.let {
            onBackPress?.invoke()
        }
    }
    
    // Observe error events
    LaunchedEffect(errorEvent) {
        errorEvent?.let { exception ->
            onError?.invoke(exception)
        }
    }
    
    // Get recipient information
    val receiver = call.receiver
    val recipientName: String
    val recipientAvatar: String?
    
    when (receiver) {
        is User -> {
            recipientName = receiver.name ?: ""
            recipientAvatar = receiver.avatar
        }
        is Group -> {
            recipientName = receiver.name ?: ""
            recipientAvatar = receiver.icon
        }
        else -> {
            recipientName = ""
            recipientAvatar = null
        }
    }
    
    // Get localized strings
    val callingText = stringResource(R.string.cometchat_calling) + " ..."
    val endCallDescription = "End call with $recipientName"
    
    Card(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Outgoing call to $recipientName"
            },
        shape = RoundedCornerShape(style.cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = style.backgroundColor
        ),
        border = if (style.strokeWidth > 0.dp) {
            BorderStroke(style.strokeWidth, style.strokeColor)
        } else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp) // cometchat_margin_5 = 20dp
        ) {
            // Main content column - title, subtitle, avatar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title (recipient name) - with top margin matching Java layout
                Spacer(modifier = Modifier.height(80.dp)) // cometchat_80dp = 80dp
                
                if (titleView != null) {
                    titleView(call)
                } else {
                    Text(
                        text = recipientName,
                        style = style.titleTextStyle,
                        color = style.titleTextColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Calling $recipientName"
                            }
                    )
                }
                
                // Subtitle ("Calling...") - cometchat_margin_2 = 8dp on ALL sides
                // Java XML uses layout_margin which applies to all sides
                if (subtitleView != null) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        subtitleView(call)
                    }
                } else {
                    Text(
                        text = callingText,
                        style = style.subtitleTextStyle,
                        color = style.subtitleTextColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp) // cometchat_margin_2 = 8dp all sides
                            .semantics {
                                contentDescription = callingText
                            }
                    )
                }
                
                // Avatar - large, centered - cometchat_margin_10 = 40dp top margin
                Spacer(modifier = Modifier.height(40.dp))
                
                if (avatarView != null) {
                    avatarView(call)
                } else {
                    // Avatar with circular shape (100dp corner radius for 120dp size)
                    CometChatAvatar(
                        name = recipientName,
                        avatarUrl = recipientAvatar,
                        style = style.avatarStyle.copy(cornerRadius = 100.dp), // cometchat_100dp = 100dp (circular)
                        modifier = Modifier
                            .size(120.dp) // cometchat_120dp = 120dp
                            .clip(CircleShape)
                            .semantics {
                                contentDescription = "Avatar of $recipientName"
                            }
                    )
                }
            }
            
            // End Call button - at bottom center - cometchat_80dp = 80dp bottom margin
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (endCallView != null) {
                    endCallView(call)
                } else {
                    IconButton(
                        onClick = {
                            if (onEndCallClick != null) {
                                onEndCallClick(call)
                            } else {
                                outgoingCallViewModel.cancelCall()
                            }
                        },
                        enabled = endCallButtonEnabled,
                        modifier = Modifier
                            .size(50.dp) // cometchat_50dp = 50dp
                            .clip(CircleShape)
                            .semantics {
                                contentDescription = endCallDescription
                            },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = style.endCallButtonBackgroundColor,
                            disabledContainerColor = style.endCallButtonBackgroundColor.copy(alpha = 0.5f)
                        )
                    ) {
                        style.endCallIcon?.let { icon ->
                            Icon(
                                painter = icon,
                                contentDescription = null,
                                tint = style.endCallIconTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

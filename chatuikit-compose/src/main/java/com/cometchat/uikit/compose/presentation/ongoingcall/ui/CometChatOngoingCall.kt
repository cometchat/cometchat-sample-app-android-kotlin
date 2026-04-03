package com.cometchat.uikit.compose.presentation.ongoingcall.ui

import android.widget.RelativeLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.compose.presentation.ongoingcall.style.CometChatOngoingCallStyle
import com.cometchat.uikit.core.constants.UIKitConstants.CallWorkFlow
import com.cometchat.uikit.core.models.OngoingCallEvent
import com.cometchat.uikit.core.models.OngoingCallUIState
import com.cometchat.uikit.core.viewmodel.CometChatOngoingCallViewModel

/**
 * CometChatOngoingCall is a Jetpack Compose component that displays an active call interface.
 *
 * This component:
 * - Shows a loading indicator while connecting to the call
 * - Hosts the CometChat Calls SDK's call UI in a RelativeLayout container
 * - Handles call lifecycle events (end, timeout, user join/leave)
 * - Manages call state using the shared ViewModel from chatuikit-core
 *
 * Features:
 * - StateFlow-based state observation using collectAsState()
 * - AndroidView integration for hosting the SDK's native call UI
 * - Lifecycle management with DisposableEffect
 * - Full accessibility support
 *
 * @param sessionId The unique identifier for the call session (required)
 * @param callType The type of call - "audio" or "video" (required)
 * @param modifier Modifier for the component container
 * @param viewModel Optional ViewModel for managing call state. If null, a default one is created
 * @param callWorkFlow The workflow type - DEFAULT for 1:1 calls, MEETING for group calls
 * @param callSettingsBuilder Optional call settings builder for custom call configuration
 * @param style Style configuration for the component appearance
 * @param onCallEnded Callback when the call ends
 * @param onError Callback for error events from the ViewModel
 *
 * Validates: Requirements 15.1, 15.2, 15.3, 15.4, 15.5
 */
@Composable
fun CometChatOngoingCall(
    sessionId: String,
    callType: String,
    modifier: Modifier = Modifier,
    viewModel: CometChatOngoingCallViewModel = viewModel(),
    callWorkFlow: CallWorkFlow = CallWorkFlow.DEFAULT,
    callSettingsBuilder: CometChatCalls.CallSettingsBuilder? = null,
    style: CometChatOngoingCallStyle = CometChatOngoingCallStyle.default(),
    onCallEnded: (() -> Unit)? = null,
    onError: ((CometChatException) -> Unit)? = null
) {
    val context = LocalContext.current

    // Track if call has been started to prevent multiple starts
    var callStarted by remember { mutableStateOf(false) }

    // Track the RelativeLayout container for the call UI
    var callViewContainer by remember { mutableStateOf<RelativeLayout?>(null) }

    // Observe ViewModel states using collectAsState() (Requirement 15.4)
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Configure ViewModel with session parameters
    LaunchedEffect(sessionId, callType, callWorkFlow, callSettingsBuilder) {
        viewModel.setSessionId(sessionId)
        viewModel.setCallType(callType)
        viewModel.setCallWorkFlow(callWorkFlow)
        viewModel.setCallSettingsBuilder(callSettingsBuilder)
    }

    // Lifecycle management with DisposableEffect (Requirement 15.5)
    DisposableEffect(Unit) {
        // Add listeners on composition
        viewModel.addListeners()

        onDispose {
            // Remove listeners on disposal
            viewModel.removeListeners()
        }
    }

    // Observe events and handle callbacks
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OngoingCallEvent.CallEnded -> {
                    onCallEnded?.invoke()
                }
                is OngoingCallEvent.SessionTimeout -> {
                    onCallEnded?.invoke()
                }
                is OngoingCallEvent.Error -> {
                    onError?.invoke(event.exception)
                }
                is OngoingCallEvent.UserJoined -> {
                    // Handle user joined if needed
                }
                is OngoingCallEvent.UserLeft -> {
                    // Handle user left if needed
                }
            }
        }
    }

    // Start call when container is ready (Requirement 15.5)
    LaunchedEffect(callViewContainer) {
        callViewContainer?.let { container ->
            if (!callStarted) {
                callStarted = true
                viewModel.startCall(container)
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Ongoing call"
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
                .background(style.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            // Show CircularProgressIndicator for Loading state (Requirement 15.3)
            if (isLoading) {
                CircularProgressIndicator(
                    color = style.progressIndicatorColor,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics {
                            contentDescription = "Connecting to call"
                        }
                )
            }

            // Use AndroidView to host RelativeLayout container (Requirement 15.3)
            AndroidView(
                factory = { ctx ->
                    RelativeLayout(ctx).apply {
                        layoutParams = RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT
                        )
                        keepScreenOn = true
                        minimumWidth = (450 * ctx.resources.displayMetrics.density).toInt()
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    // Store reference to container for call start
                    if (callViewContainer == null) {
                        callViewContainer = view
                    }
                    // Control visibility based on loading state
                    view.visibility = if (isLoading) {
                        android.view.View.GONE
                    } else {
                        android.view.View.VISIBLE
                    }
                }
            )
        }
    }
}

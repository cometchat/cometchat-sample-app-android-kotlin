package com.cometchat.uikit.compose.presentation.calllogs.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.calls.constants.CometChatCallsConstants
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.calllogs.style.CometChatCallLogsListItemStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.Pattern
import com.cometchat.uikit.compose.presentation.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.utils.CallLogsUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Formats a timestamp for call logs display.
 * Uses format "d MMMM, h:mm a" for current year (e.g., "17 February, 11:30 AM")
 * Uses format "d MMMM yyyy, h:mm a" for other years (e.g., "17 February 2023, 11:30 AM")
 *
 * @param timestamp The timestamp in seconds
 * @return Formatted date string
 */
private fun formatCallLogsTimestamp(timestamp: Long): String {
    var timestampMs = timestamp
    
    // Convert seconds to milliseconds if needed
    if (timestamp.toString().length == 10) {
        timestampMs = timestamp * 1000
    }
    
    val date = Date(timestampMs)
    val calendar = Calendar.getInstance()
    calendar.time = date
    
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val inputYear = calendar.get(Calendar.YEAR)
    
    val format = if (inputYear == currentYear) {
        SimpleDateFormat("d MMMM, h:mm a", Locale.getDefault())
    } else {
        SimpleDateFormat("d MMMM yyyy, h:mm a", Locale.getDefault())
    }
    
    return format.format(date)
}

/**
 * Default leading view composable that displays the avatar.
 *
 * @param callLog The call log to display
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultLeadingView(
    callLog: CallLog,
    style: CometChatCallLogsListItemStyle
) {
    val avatarSize = 48.dp
    
    CometChatAvatar(
        modifier = Modifier.size(avatarSize),
        name = CallLogsUtils.getDisplayName(callLog),
        avatarUrl = CallLogsUtils.getAvatarUrl(callLog),
        style = style.avatarStyle
    )
}

/**
 * Default title view composable that displays the caller/callee name.
 * Uses error color for missed calls.
 *
 * @param callLog The call log to display
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultTitleView(
    callLog: CallLog,
    style: CometChatCallLogsListItemStyle
) {
    val isMissed = CallLogsUtils.isMissedCall(callLog)
    val textColor = if (isMissed) style.missedCallTitleColor else style.titleTextColor
    
    Text(
        text = CallLogsUtils.getDisplayName(callLog),
        color = textColor,
        style = style.titleTextStyle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Default subtitle view composable that displays the call direction icon and date.
 *
 * @param callLog The call log to display
 * @param dateTimeFormatter Optional custom date/time formatter
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultSubtitleView(
    callLog: CallLog,
    dateTimeFormatter: DateTimeFormatterCallback?,
    style: CometChatCallLogsListItemStyle
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Direction icon
        val (directionIcon, directionIconTint) = getDirectionIconAndTint(callLog, style)
        
        if (directionIcon != null) {
            Icon(
                painter = directionIcon,
                contentDescription = null,
                tint = directionIconTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        
        // Date/time - use call logs timestamp format (e.g., "17 February, 11:30 AM")
        val timestamp = callLog.initiatedAt.toLong()
        if (timestamp > 0) {
            val formattedDate = remember(timestamp) {
                formatCallLogsTimestamp(timestamp)
            }
            Text(
                text = formattedDate,
                style = style.dateStyle.textStyle ?: CometChatTheme.typography.caption1Regular,
                color = style.dateStyle.textColor ?: CometChatTheme.colorScheme.textColorSecondary
            )
        }
    }
}

/**
 * Gets the direction icon and tint based on call direction.
 */
@Composable
private fun getDirectionIconAndTint(
    callLog: CallLog,
    style: CometChatCallLogsListItemStyle
): Pair<Painter?, Color> {
    return when {
        CallLogsUtils.isMissedCall(callLog) -> {
            style.missedCallIcon to style.missedCallIconTint
        }
        CallLogsUtils.isOutgoingCall(callLog) -> {
            style.outgoingCallIcon to style.outgoingCallIconTint
        }
        else -> {
            style.incomingCallIcon to style.incomingCallIconTint
        }
    }
}

/**
 * Default trailing view composable that displays the call type icon (audio/video).
 * The icon is clickable to initiate a new call.
 *
 * @param callLog The call log to display
 * @param onCallIconClick Callback when the call icon is clicked
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultTrailingView(
    callLog: CallLog,
    onCallIconClick: ((CallLog) -> Unit)?,
    style: CometChatCallLogsListItemStyle
) {
    val context = LocalContext.current
    val isVideo = CallLogsUtils.isVideoCall(callLog)
    
    val (callIcon, callIconTint) = if (isVideo) {
        style.videoCallIcon to style.videoCallIconTint
    } else {
        style.audioCallIcon to style.audioCallIconTint
    }
    
    val callTypeDescription = if (isVideo) {
        context.getString(R.string.cometchat_video_call)
    } else {
        context.getString(R.string.cometchat_audio_call)
    }
    
    if (callIcon != null) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false),
                    onClick = { onCallIconClick?.invoke(callLog) }
                )
                .semantics {
                    contentDescription = callTypeDescription
                    role = Role.Button
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = callIcon,
                contentDescription = callTypeDescription,
                tint = callIconTint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * CometChatCallLogsListItem is a composable that displays a single call log item in a list.
 *
 * Features:
 * - Displays avatar of the caller/callee
 * - Shows caller/callee name (with error color for missed calls)
 * - Displays call direction icon and timestamp
 * - Shows call type icon (audio/video) that can initiate a new call
 * - Fully customizable through style and custom view lambdas
 * - Integrates with CometChatTheme for consistent styling
 *
 * @param callLog The CometChat CallLog object to display (required)
 * @param modifier Modifier applied to the parent container
 * @param style Style configuration for the component
 * @param dateTimeFormatter Optional custom date/time formatter
 * @param hideSeparator Whether to hide the separator line
 * @param leadingView Optional custom composable for the leading section (avatar)
 * @param titleView Optional custom composable for the title section (name)
 * @param subtitleView Optional custom composable for the subtitle section (direction + date)
 * @param trailingView Optional custom composable for the trailing section (call type icon)
 * @param onItemClick Callback invoked when the item is clicked
 * @param onItemLongClick Optional callback for long-click events
 * @param onCallIconClick Callback invoked when the call type icon is clicked
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatCallLogsListItem(
    callLog: CallLog,
    modifier: Modifier = Modifier,
    style: CometChatCallLogsListItemStyle = CometChatCallLogsListItemStyle.default(),
    dateTimeFormatter: DateTimeFormatterCallback? = null,
    hideSeparator: Boolean = false,
    leadingView: (@Composable (CallLog) -> Unit)? = null,
    titleView: (@Composable (CallLog) -> Unit)? = null,
    subtitleView: (@Composable (CallLog) -> Unit)? = null,
    trailingView: (@Composable (CallLog) -> Unit)? = null,
    onItemClick: ((CallLog) -> Unit)? = null,
    onItemLongClick: ((CallLog) -> Unit)? = null,
    onCallIconClick: ((CallLog) -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Build content description for accessibility
    val displayName = CallLogsUtils.getDisplayName(callLog)
    val callDirection = when {
        CallLogsUtils.isMissedCall(callLog) -> context.getString(R.string.cometchat_missed_call)
        CallLogsUtils.isOutgoingCall(callLog) -> context.getString(R.string.cometchat_outgoing)
        else -> context.getString(R.string.cometchat_incoming)
    }
    val callType = if (CallLogsUtils.isVideoCall(callLog)) {
        context.getString(R.string.cometchat_video_call)
    } else {
        context.getString(R.string.cometchat_audio_call)
    }
    
    val accessibilityDescription = "$displayName, $callDirection, $callType"
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(style.backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .focusable()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                    onClick = { onItemClick?.invoke(callLog) },
                    onLongClick = onItemLongClick?.let { { it(callLog) } }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .semantics {
                    contentDescription = accessibilityDescription
                    role = Role.Button
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading view (avatar)
            if (leadingView != null) {
                leadingView(callLog)
            } else {
                DefaultLeadingView(
                    callLog = callLog,
                    style = style
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content area (title and subtitle)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Title view
                if (titleView != null) {
                    titleView(callLog)
                } else {
                    DefaultTitleView(
                        callLog = callLog,
                        style = style
                    )
                }
                
                Spacer(modifier = Modifier.size(2.dp))
                
                // Subtitle view
                if (subtitleView != null) {
                    subtitleView(callLog)
                } else {
                    DefaultSubtitleView(
                        callLog = callLog,
                        dateTimeFormatter = dateTimeFormatter,
                        style = style
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Trailing view (call type icon)
            if (trailingView != null) {
                trailingView(callLog)
            } else {
                DefaultTrailingView(
                    callLog = callLog,
                    onCallIconClick = onCallIconClick,
                    style = style
                )
            }
        }
        
        // Separator
        if (!hideSeparator) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 76.dp) // Align with content after avatar
                    .height(style.separatorHeight)
                    .background(style.separatorColor)
            )
        }
    }
}

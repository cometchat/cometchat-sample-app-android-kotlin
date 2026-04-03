package com.cometchat.sampleapp.compose.ui.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cometchat.calls.constants.CometChatCallsConstants
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.utils.CallLogsUtils
import com.cometchat.uikit.compose.presentation.messageheader.ui.CometChatMessageHeader
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Call details screen composable for displaying call log information.
 *
 * This screen displays detailed information about a call:
 * - Custom toolbar with back icon and title (matching master-app-java)
 * - Call recipient information via CometChatMessageHeader
 * - Call type (incoming/outgoing/missed), date, and duration
 * - TabRow with History/Participants/Recordings tabs (matching master-app-java)
 * - HorizontalPager for tab content
 *
 * Matches master-app-java UI parity.
 * Validates: Requirement 9.2
 */
@Composable
fun CallDetailsScreen(
    callLogJson: String,
    onBackPress: () -> Unit,
    onUserClick: ((User) -> Unit)? = null
) {
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography

    // Parse call log from JSON
    val callLog = remember(callLogJson) {
        try {
            Gson().fromJson(callLogJson, CallLog::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // State for the receiver user
    var receiverUser by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Tab state
    val tabs = listOf("History", "Participants", "Recordings")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    // Load receiver user data
    LaunchedEffect(callLog) {
        if (callLog == null) {
            isLoading = false
            return@LaunchedEffect
        }

        val isLoggedInUser = CallLogsUtils.isOutgoingCall(callLog)

        val targetUser = if (isLoggedInUser) {
            callLog.receiver as? CallUser
        } else {
            callLog.initiator as? CallUser
        }

        if (targetUser != null) {
            CometChat.getUser(targetUser.uid, object : CometChat.CallbackListener<User>() {
                override fun onSuccess(user: User) {
                    receiverUser = user
                    isLoading = false
                }

                override fun onError(e: CometChatException) {
                    isLoading = false
                }
            })
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.backgroundColor1)
    ) {
        // Custom Toolbar (matching master-app-java)
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackPress) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.iconTintPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Call Details",
                    style = typography.heading1Bold,
                    color = colorScheme.textColorPrimary
                )
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = colorScheme.strokeColorLight
            )
        }

        if (callLog != null && !isLoading) {
            // Message Header showing call recipient
            if (receiverUser != null) {
                CometChatMessageHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    user = receiverUser,
                    hideBackButton = true,
                    hideUserStatus = true,
                    hideVideoCallButton = receiverUser?.isHasBlockedMe == true || receiverUser?.isBlockedByMe == true,
                    hideVoiceCallButton = receiverUser?.isHasBlockedMe == true || receiverUser?.isBlockedByMe == true
                )
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = colorScheme.strokeColorLight
            )

            // Call Info Section
            CallInfoSection(callLog = callLog)

            HorizontalDivider(
                thickness = 1.dp,
                color = colorScheme.strokeColorLight
            )

            // TabRow for History/Participants/Recordings (matching master-app-java)
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = colorScheme.backgroundColor1,
                contentColor = colorScheme.primary,
                indicator = { /* Default indicator */ }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                style = typography.bodyMedium,
                                color = if (pagerState.currentPage == index) {
                                    colorScheme.primary
                                } else {
                                    colorScheme.textColorSecondary
                                }
                            )
                        }
                    )
                }
            }

            // HorizontalPager for tab content (matching master-app-java)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (page) {
                        0 -> {
                            // History tab content
                            Text(
                                text = "Call History",
                                style = typography.bodyRegular,
                                color = colorScheme.textColorSecondary
                            )
                        }
                        1 -> {
                            // Participants tab content
                            Text(
                                text = "Participants",
                                style = typography.bodyRegular,
                                color = colorScheme.textColorSecondary
                            )
                        }
                        2 -> {
                            // Recordings tab content
                            Text(
                                text = "Recordings",
                                style = typography.bodyRegular,
                                color = colorScheme.textColorSecondary
                            )
                        }
                    }
                }
            }
        } else if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading...",
                    style = typography.bodyRegular,
                    color = colorScheme.textColorSecondary
                )
            }
        } else {
            // Error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Unable to load call details",
                    style = typography.bodyRegular,
                    color = colorScheme.errorColor
                )
            }
        }
    }
}

/**
 * Composable for displaying call information section.
 */
@Composable
private fun CallInfoSection(callLog: CallLog) {
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography

    val isOutgoing = CallLogsUtils.isOutgoingCall(callLog)
    val isMissedOrUnanswered = CallLogsUtils.isMissedCall(callLog)

    // Determine call type info
    val (callTypeText, callTypeColor, callIcon) = when {
        callLog.type == CometChatCallsConstants.CALL_TYPE_AUDIO ||
        callLog.type == CometChatCallsConstants.CALL_TYPE_VIDEO ||
        callLog.type == CometChatCallsConstants.CALL_TYPE_AUDIO_VIDEO -> {
            when {
                isOutgoing -> Triple("Outgoing Call", Color(0xFF4CAF50), Icons.Default.KeyboardArrowUp)
                isMissedOrUnanswered -> Triple("Missed Call", colorScheme.errorColor, Icons.Default.Call)
                else -> Triple("Incoming Call", Color(0xFF4CAF50), Icons.Default.KeyboardArrowDown)
            }
        }
        else -> Triple("Call", colorScheme.textColorPrimary, Icons.Default.Call)
    }

    // Format call duration
    val decimalValue = callLog.totalDurationInMinutes
    val minutes = decimalValue.toInt()
    val seconds = ((decimalValue - minutes) * 60).toInt()
    val durationText = String.format(Locale.US, "%dm %ds", minutes, seconds)

    // Format call date
    val dateText = formatCallLogsTimestamp(callLog.initiatedAt.toLong())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.backgroundColor2)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = callIcon,
            contentDescription = callTypeText,
            tint = callTypeColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = callTypeText,
                style = typography.heading4Medium,
                color = if (isMissedOrUnanswered && !isOutgoing) {
                    colorScheme.errorColor
                } else {
                    colorScheme.textColorPrimary
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateText,
                style = typography.caption1Regular,
                color = colorScheme.textColorSecondary
            )
        }

        Text(
            text = durationText,
            style = typography.heading4Medium,
            color = colorScheme.textColorSecondary
        )
    }
}

/**
 * Serializes a CallLog object to JSON string for navigation.
 */
fun CallLog.toJson(): String {
    return Gson().toJson(this)
}

/**
 * Formats a timestamp for call logs display.
 * Shows "Today" or "Yesterday" for recent calls, otherwise shows the full date.
 *
 * @param timestamp The timestamp in seconds or milliseconds
 * @return Formatted date string (e.g., "Today, 11:30 AM" or "17 February, 11:30 AM")
 */
private fun formatCallLogsTimestamp(timestamp: Long): String {
    var timestampMs = timestamp
    
    // Convert seconds to milliseconds if needed
    if (timestampMs.toString().length == 10) {
        timestampMs *= 1000
    }
    
    val date = Date(timestampMs)
    val calendar = Calendar.getInstance().apply { time = date }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeString = timeFormat.format(date)
    
    return when {
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> {
            "Today, $timeString"
        }
        calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
        calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> {
            "Yesterday, $timeString"
        }
        else -> {
            val dateFormat = SimpleDateFormat("d MMMM, h:mm a", Locale.getDefault())
            dateFormat.format(date)
        }
    }
}

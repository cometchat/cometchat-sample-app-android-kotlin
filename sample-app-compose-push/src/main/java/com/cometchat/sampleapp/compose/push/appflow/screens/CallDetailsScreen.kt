package com.cometchat.sampleapp.compose.push.appflow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.calls.constants.CometChatCallsConstants
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messageheader.ui.CometChatMessageHeader
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.AvatarStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.utils.CallLogsUtils
import com.cometchat.sampleapp.compose.push.appflow.components.AppFlowToolbar
import com.cometchat.sampleapp.compose.push.appflow.viewmodels.CallDetailsViewModel
import com.cometchat.sampleapp.compose.push.appflow.viewmodels.CallHistoryUIState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Call details screen showing call information and tabs.
 * Styled to match master-app-kotlin2 CallDetailsActivity.
 *
 * @param callLog The call log to display details for
 * @param onBackPress Callback when back is pressed
 */
@Composable
fun CallDetailsScreen(
    callLog: CallLog,
    onBackPress: () -> Unit,
    viewModel: CallDetailsViewModel = viewModel()
) {
    val backgroundColor = CometChatTheme.colorScheme.backgroundColor1
    val backgroundColor2 = CometChatTheme.colorScheme.backgroundColor2
    val textColorPrimary = CometChatTheme.colorScheme.textColorPrimary
    val textColorSecondary = CometChatTheme.colorScheme.textColorSecondary
    val strokeColorLight = CometChatTheme.colorScheme.strokeColorLight
    
    val user by viewModel.user.collectAsState()
    val historyState by viewModel.historyState.collectAsState()
    
    // Initialize ViewModel
    LaunchedEffect(callLog) {
        viewModel.initialize(callLog)
    }
    
    val initiator = callLog.initiator as? CallUser
    val isLoggedInUser = CometChatUIKit.getLoggedInUser()?.uid == initiator?.uid
    val isMissedOrUnanswered = callLog.status == CometChatCallsConstants.CALL_STATUS_UNANSWERED ||
            callLog.status == CometChatCallsConstants.CALL_STATUS_MISSED
    
    val tabs = listOf("Participants", "Recordings", "History")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            AppFlowToolbar(
                title = "Call Details",
                onBackPress = onBackPress,
                titleStyle = CometChatTheme.typography.heading1Bold
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Divider after toolbar
            HorizontalDivider(color = strokeColorLight, thickness = 1.dp)
            
            // Message Header section with user info and call buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                user?.let { fetchedUser ->
                    val isBlocked = fetchedUser.isHasBlockedMe || fetchedUser.isBlockedByMe
                    CometChatMessageHeader(
                        user = fetchedUser,
                        hideBackButton = true,
                        hideVideoCallButton = isBlocked,
                        hideVoiceCallButton = isBlocked
                    )
                } ?: run {
                    // Fallback when user is not yet loaded
                    val displayName = CallLogsUtils.getDisplayName(callLog)
                    val avatarUrl = CallLogsUtils.getAvatarUrl(callLog)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CometChatAvatar(
                            modifier = Modifier.size(48.dp),
                            name = displayName,
                            avatarUrl = avatarUrl,
                            style = AvatarStyle.default(cornerRadius = 24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = displayName,
                            style = CometChatTheme.typography.heading4Medium,
                            color = textColorPrimary
                        )
                    }
                }
            }
            
            // Divider after message header
            HorizontalDivider(color = strokeColorLight, thickness = 1.dp)
            
            // Call info section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor2)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call direction icon
                val (directionIcon, directionColor) = getCallDirectionIconAndColor(
                    isLoggedInUser = isLoggedInUser,
                    isMissedOrUnanswered = isMissedOrUnanswered
                )
                Icon(
                    painter = painterResource(id = directionIcon),
                    contentDescription = null,
                    tint = directionColor,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Title and date column
                Column {
                    Text(
                        text = getCallTypeText(isLoggedInUser, isMissedOrUnanswered),
                        style = CometChatTheme.typography.heading4Medium,
                        color = if (isMissedOrUnanswered) CometChatTheme.colorScheme.errorColor else textColorPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CometChatDate(
                        timestamp = callLog.initiatedAt,
                        customDateString = formatCallTimestamp(callLog.initiatedAt)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Duration
                Text(
                    text = formatDuration(callLog.totalDurationInMinutes),
                    style = CometChatTheme.typography.heading4Medium,
                    color = textColorSecondary
                )
            }
            
            // Divider after info section
            HorizontalDivider(color = strokeColorLight, thickness = 1.dp)
            
            // Tab row
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = backgroundColor,
                contentColor = CometChatTheme.colorScheme.textColorHighlight,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = CometChatTheme.colorScheme.textColorHighlight
                    )
                }
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
                                text = title.replaceFirstChar { it.uppercase() },
                                style = CometChatTheme.typography.heading4Medium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (pagerState.currentPage == index) {
                                    CometChatTheme.colorScheme.textColorHighlight
                                } else {
                                    textColorSecondary
                                }
                            )
                        }
                    )
                }
            }
            
            // Tab content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> ParticipantsTab(callLog)
                    1 -> RecordingsTab(callLog)
                    2 -> HistoryTab(
                        callLog = callLog,
                        historyState = historyState,
                        onLoadMore = { viewModel.fetchCallHistory() }
                    )
                }
            }
            
            // Fetch history when History tab is selected
            LaunchedEffect(pagerState.currentPage) {
                if (pagerState.currentPage == 2) {
                    viewModel.fetchCallHistory()
                }
            }
        }
    }
}

/**
 * Participants tab content.
 */
@Composable
private fun ParticipantsTab(callLog: CallLog) {
    val participants = callLog.participants ?: emptyList()
    
    if (participants.isEmpty()) {
        EmptyTabContent("No participants")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(participants) { participant ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CometChatAvatar(
                        modifier = Modifier.size(48.dp),
                        name = participant.name ?: "Unknown",
                        avatarUrl = participant.avatar,
                        style = AvatarStyle.default(cornerRadius = 24.dp)
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = participant.name ?: "Unknown",
                            style = CometChatTheme.typography.heading4Medium,
                            color = CometChatTheme.colorScheme.textColorPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CometChatDate(
                            timestamp = callLog.initiatedAt,
                            customDateString = formatCallTimestamp(callLog.initiatedAt)
                        )
                    }
                    
                    Text(
                        text = formatParticipantDuration(participant.totalDurationInMinutes),
                        style = CometChatTheme.typography.caption1Medium,
                        color = CometChatTheme.colorScheme.textColorTertiary
                    )
                }
            }
        }
    }
}

/**
 * Recordings tab content.
 */
@Composable
private fun RecordingsTab(callLog: CallLog) {
    val recordings = callLog.recordings ?: emptyList()
    
    if (recordings.isEmpty()) {
        EmptyTabContent("No recordings available")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(recordings) { recording ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.cometchat_ic_video_outlined),
                        contentDescription = null,
                        tint = CometChatTheme.colorScheme.iconTintPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Text(
                        text = "Recording",
                        style = CometChatTheme.typography.heading4Medium,
                        color = CometChatTheme.colorScheme.textColorPrimary
                    )
                }
            }
        }
    }
}

/**
 * History tab content with proper call history fetching.
 */
@Composable
private fun HistoryTab(
    callLog: CallLog,
    historyState: CallHistoryUIState,
    onLoadMore: () -> Unit
) {
    when (historyState) {
        is CallHistoryUIState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = CometChatTheme.colorScheme.primary
                )
            }
        }
        is CallHistoryUIState.Empty -> {
            EmptyTabContent("No call history")
        }
        is CallHistoryUIState.Error -> {
            EmptyTabContent("Error loading history")
        }
        is CallHistoryUIState.Content -> {
            val listState = rememberLazyListState()
            
            // Load more when reaching end
            LaunchedEffect(listState) {
                snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                    .collect { lastVisibleIndex ->
                        if (lastVisibleIndex != null && 
                            lastVisibleIndex >= historyState.callLogs.size - 3) {
                            onLoadMore()
                        }
                    }
            }
            
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(historyState.callLogs) { historyCallLog ->
                    HistoryItem(historyCallLog)
                }
            }
        }
    }
}

/**
 * Single history item row.
 */
@Composable
private fun HistoryItem(callLog: CallLog) {
    val initiator = callLog.initiator as? CallUser
    val isLoggedInUser = CometChatUIKit.getLoggedInUser()?.uid == initiator?.uid
    val isMissedOrUnanswered = callLog.status == CometChatCallsConstants.CALL_STATUS_UNANSWERED ||
            callLog.status == CometChatCallsConstants.CALL_STATUS_MISSED
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Call direction icon
        val (directionIcon, directionColor) = getCallDirectionIconAndColor(
            isLoggedInUser = isLoggedInUser,
            isMissedOrUnanswered = isMissedOrUnanswered
        )
        Icon(
            painter = painterResource(id = directionIcon),
            contentDescription = null,
            tint = directionColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Title and date column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = getCallTypeText(isLoggedInUser, isMissedOrUnanswered),
                style = CometChatTheme.typography.heading4Medium,
                color = if (isMissedOrUnanswered) CometChatTheme.colorScheme.errorColor 
                       else CometChatTheme.colorScheme.textColorPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            CometChatDate(
                timestamp = callLog.initiatedAt,
                customDateString = formatCallTimestamp(callLog.initiatedAt)
            )
        }
        
        // Duration
        Text(
            text = formatDuration(callLog.totalDurationInMinutes),
            style = CometChatTheme.typography.heading4Medium,
            color = CometChatTheme.colorScheme.textColorSecondary
        )
    }
}

/**
 * Empty tab content placeholder.
 */
@Composable
private fun EmptyTabContent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = CometChatTheme.typography.heading3Medium,
            color = CometChatTheme.colorScheme.textColorSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Get call direction icon and color based on call status.
 */
@Composable
private fun getCallDirectionIconAndColor(
    isLoggedInUser: Boolean,
    isMissedOrUnanswered: Boolean
): Pair<Int, androidx.compose.ui.graphics.Color> {
    return when {
        isMissedOrUnanswered -> Pair(R.drawable.cometchat_ic_missed_call, CometChatTheme.colorScheme.errorColor)
        isLoggedInUser -> Pair(R.drawable.cometchat_ic_outgoing_call, CometChatTheme.colorScheme.successColor)
        else -> Pair(R.drawable.cometchat_ic_incoming_call, CometChatTheme.colorScheme.successColor)
    }
}

/**
 * Get call type text.
 */
private fun getCallTypeText(isLoggedInUser: Boolean, isMissedOrUnanswered: Boolean): String {
    return when {
        isMissedOrUnanswered -> "Missed"
        isLoggedInUser -> "Outgoing"
        else -> "Incoming"
    }
}

/**
 * Format duration in minutes to readable string.
 */
private fun formatDuration(minutes: Double): String {
    val totalMinutes = minutes.toInt()
    val seconds = ((minutes - totalMinutes) * 60).toInt()
    return String.format(Locale.US, "%dm %ds", totalMinutes, seconds)
}

/**
 * Format participant duration with seconds.
 */
private fun formatParticipantDuration(totalMinutes: Double): String {
    val hours = totalMinutes.toInt() / 60
    val remainingMinutes = totalMinutes.toInt() % 60
    val seconds = ((totalMinutes - totalMinutes.toInt()) * 60).toInt()
    
    return when {
        hours > 0 -> when {
            remainingMinutes == 0 && seconds == 0 -> String.format(Locale.US, "%d hr", hours)
            seconds == 0 -> String.format(Locale.US, "%d hr %d min", hours, remainingMinutes)
            else -> String.format(Locale.US, "%d hr %d min %d sec", hours, remainingMinutes, seconds)
        }
        remainingMinutes == 0 && seconds == 0 -> "0 min"
        seconds == 0 -> String.format(Locale.US, "%d min", remainingMinutes)
        else -> String.format(Locale.US, "%d min %d sec", remainingMinutes, seconds)
    }
}

/**
 * Format timestamp to readable string for call logs.
 */
private fun formatCallTimestamp(timestamp: Long): String {
    val date = Date(timestamp * 1000)
    val format = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return format.format(date)
}

package com.cometchat.uikit.compose.presentation.calllogs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.calllogs.style.CometChatCallLogsListItemStyle
import com.cometchat.uikit.compose.presentation.calllogs.style.CometChatCallLogsStyle
import com.cometchat.uikit.compose.presentation.shared.interfaces.DateTimeFormatterCallback
import kotlinx.coroutines.flow.SharedFlow

/**
 * Content composable for the call logs list.
 * Uses LazyColumn with pagination support.
 * 
 * @param callLogs List of call logs to display
 * @param style Style configuration for the component
 * @param hideSeparator Whether to hide item separators
 * @param dateTimeFormatter Custom date/time formatter callback
 * @param listItemView Custom item composable replacing entire item
 * @param leadingView Custom leading section composable
 * @param titleView Custom title section composable
 * @param subtitleView Custom subtitle section composable
 * @param trailingView Custom trailing section composable
 * @param onItemClick Callback for item clicks
 * @param onItemLongClick Callback for item long-clicks
 * @param onCallIconClick Callback when call icon is clicked
 * @param onLoadMore Callback when more items should be loaded
 * @param scrollToTopEvent SharedFlow that emits when list should scroll to top
 */
@Composable
internal fun CallLogsListContent(
    callLogs: List<CallLog>,
    style: CometChatCallLogsStyle,
    hideSeparator: Boolean,
    dateTimeFormatter: DateTimeFormatterCallback?,
    listItemView: (@Composable (CallLog) -> Unit)?,
    leadingView: (@Composable (CallLog) -> Unit)?,
    titleView: (@Composable (CallLog) -> Unit)?,
    subtitleView: (@Composable (CallLog) -> Unit)?,
    trailingView: (@Composable (CallLog) -> Unit)?,
    onItemClick: ((CallLog) -> Unit)?,
    onItemLongClick: ((CallLog) -> Unit)?,
    onCallIconClick: ((CallLog) -> Unit)?,
    onLoadMore: () -> Unit,
    scrollToTopEvent: SharedFlow<Unit>? = null
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    
    // Detect when we need to load more items
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= callLogs.size - 5
        }
    }
    
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && callLogs.isNotEmpty()) {
            onLoadMore()
        }
    }
    
    // Auto-scroll to top when triggered
    LaunchedEffect(scrollToTopEvent) {
        scrollToTopEvent?.collect {
            if (listState.firstVisibleItemIndex < 3) {
                listState.animateScrollToItem(0)
            }
        }
    }
    
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(style.backgroundColor)
            .semantics { 
                contentDescription = context.getString(R.string.cometchat_call_logs_title)
                collectionInfo = CollectionInfo(
                    rowCount = callLogs.size,
                    columnCount = 1
                )
            }
    ) {
        itemsIndexed(
            items = callLogs,
            key = { index, callLog -> index } // Using index as key since CallLog equality is based on object equality
        ) { index, callLog ->
            if (listItemView != null) {
                // Use custom item view
                listItemView(callLog)
            } else {
                // Use default CometChatCallLogsListItem
                CometChatCallLogsListItem(
                    callLog = callLog,
                    style = style.itemStyle,
                    dateTimeFormatter = dateTimeFormatter,
                    hideSeparator = hideSeparator || index == callLogs.size - 1,
                    leadingView = leadingView,
                    titleView = titleView,
                    subtitleView = subtitleView,
                    trailingView = trailingView,
                    onItemClick = onItemClick,
                    onItemLongClick = onItemLongClick,
                    onCallIconClick = onCallIconClick
                )
            }
        }
    }
}

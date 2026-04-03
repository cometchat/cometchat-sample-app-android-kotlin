package com.cometchat.uikit.compose.presentation.calllogs.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.exceptions.CometChatException
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.calllogs.style.CometChatCallLogsListItemStyle
import com.cometchat.uikit.compose.presentation.calllogs.style.CometChatCallLogsStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingState
import com.cometchat.uikit.compose.presentation.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.compose.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbar
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbarStyle
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.core.factory.CometChatCallLogsViewModelFactory
import com.cometchat.uikit.core.state.CallLogsUIState
import com.cometchat.uikit.core.viewmodel.CometChatCallLogsViewModel

/**
 * CometChatCallLogs displays a list of call logs with support for
 * real-time updates, custom views, and full styling customization.
 *
 * @param modifier Modifier applied to the parent container
 * @param viewModel The ViewModel managing call logs state (optional, creates default if not provided)
 * @param callLogRequestBuilder Custom request builder for fetching call logs
 * @param style Style configuration for the component
 * @param title Toolbar title text
 * @param hideToolbar Whether to hide the entire toolbar
 * @param hideBackButton Whether to hide the back navigation icon
 * @param hideTitle Whether to hide the title text
 * @param overflowMenu Optional custom overflow menu composable
 * @param hideSeparator Whether to hide item separators
 * @param hideLoadingState Whether to hide loading state
 * @param hideEmptyState Whether to hide empty state
 * @param hideErrorState Whether to hide error state
 * @param dateTimeFormatter Custom date/time formatter callback
 * @param loadingView Custom loading state composable
 * @param emptyView Custom empty state composable
 * @param errorView Custom error state composable with retry callback
 * @param listItemView Custom item composable replacing entire item
 * @param leadingView Custom leading section composable (avatar)
 * @param titleView Custom title section composable (name)
 * @param subtitleView Custom subtitle section composable (direction + date)
 * @param trailingView Custom trailing section composable (call type icon)
 * @param incomingCallIcon Custom icon for incoming calls
 * @param outgoingCallIcon Custom icon for outgoing calls
 * @param missedCallIcon Custom icon for missed calls
 * @param audioCallIcon Custom icon for audio calls
 * @param videoCallIcon Custom icon for video calls
 * @param options Function to replace all menu options shown on long-press
 * @param addOptions Function to add options to default menu shown on long-press
 * @param onItemClick Callback for item clicks
 * @param onItemLongClick Callback for item long-clicks
 * @param onCallIconClick Callback when call icon is clicked
 * @param onBackPress Callback for back navigation
 * @param onError Callback for errors
 * @param onLoad Callback when call logs are loaded with the list of call logs
 * @param onEmpty Callback when the call logs list is empty
 */
@Composable
fun CometChatCallLogs(
    modifier: Modifier = Modifier,
    viewModel: CometChatCallLogsViewModel? = null,
    callLogRequestBuilder: CallLogRequest.CallLogRequestBuilder? = null,
    style: CometChatCallLogsStyle = CometChatCallLogsStyle.default(),
    // Toolbar configuration
    title: String? = null,
    hideToolbar: Boolean = false,
    hideBackButton: Boolean = true,
    hideTitle: Boolean = false,
    overflowMenu: (@Composable () -> Unit)? = null,
    // Visibility controls
    hideSeparator: Boolean = false,
    hideLoadingState: Boolean = false,
    hideEmptyState: Boolean = false,
    hideErrorState: Boolean = false,
    // Date formatting
    dateTimeFormatter: DateTimeFormatterCallback? = null,
    // Custom views
    loadingView: (@Composable () -> Unit)? = null,
    emptyView: (@Composable () -> Unit)? = null,
    errorView: (@Composable (onRetry: () -> Unit) -> Unit)? = null,
    listItemView: (@Composable (CallLog) -> Unit)? = null,
    leadingView: (@Composable (CallLog) -> Unit)? = null,
    titleView: (@Composable (CallLog) -> Unit)? = null,
    subtitleView: (@Composable (CallLog) -> Unit)? = null,
    trailingView: (@Composable (CallLog) -> Unit)? = null,
    // Custom icons
    incomingCallIcon: Painter? = null,
    outgoingCallIcon: Painter? = null,
    missedCallIcon: Painter? = null,
    audioCallIcon: Painter? = null,
    videoCallIcon: Painter? = null,
    // Menu options
    options: ((Context, CallLog) -> List<MenuItem>)? = null,
    addOptions: ((Context, CallLog) -> List<MenuItem>)? = null,
    // Callbacks
    onItemClick: ((CallLog) -> Unit)? = null,
    onItemLongClick: ((CallLog) -> Unit)? = null,
    onCallIconClick: ((CallLog) -> Unit)? = null,
    onBackPress: (() -> Unit)? = null,
    onError: ((CometChatException) -> Unit)? = null,
    onLoad: ((List<CallLog>) -> Unit)? = null,
    onEmpty: (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Use localized strings with fallback to provided values
    val localizedTitle = title ?: context.getString(R.string.cometchat_call_logs_title)
    
    // Create default ViewModel if none provided
    val callLogsViewModel = viewModel ?: viewModel(
        factory = CometChatCallLogsViewModelFactory()
    )
    
    // Apply custom request builder if provided
    LaunchedEffect(callLogRequestBuilder) {
        callLogRequestBuilder?.let {
            callLogsViewModel.setCallLogRequestBuilder(it)
        }
    }
    
    // Collect state from ViewModel
    val uiState by callLogsViewModel.uiState.collectAsState()
    val callLogs by callLogsViewModel.callLogs.collectAsState()
    
    // Handle error callback
    LaunchedEffect(uiState) {
        when (uiState) {
            is CallLogsUIState.Error -> {
                onError?.invoke((uiState as CallLogsUIState.Error).exception)
            }
            is CallLogsUIState.Empty -> {
                onEmpty?.invoke()
            }
            is CallLogsUIState.Content -> {
                onLoad?.invoke(callLogs)
            }
            else -> { /* Loading state - no callback */ }
        }
    }
    
    // Create item style with custom icons if provided
    val effectiveItemStyle = createEffectiveItemStyle(
        baseStyle = style.itemStyle,
        incomingCallIcon = incomingCallIcon,
        outgoingCallIcon = outgoingCallIcon,
        missedCallIcon = missedCallIcon,
        audioCallIcon = audioCallIcon,
        videoCallIcon = videoCallIcon
    )
    
    // Create effective style with updated item style
    val effectiveStyle = style.copy(itemStyle = effectiveItemStyle)
    
    // Popup menu state
    var showPopupMenu by remember { mutableStateOf(false) }
    var popupMenuCallLog by remember { mutableStateOf<CallLog?>(null) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(style.backgroundColor)
    ) {
        // Toolbar
        if (!hideToolbar) {
            CallLogsToolbar(
                title = if (hideTitle) "" else localizedTitle,
                style = style,
                hideBackButton = hideBackButton,
                overflowMenu = overflowMenu,
                onBackPress = onBackPress
            )
        }
        
        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (uiState) {
                is CallLogsUIState.Loading -> {
                    if (!hideLoadingState) {
                        loadingView?.invoke() ?: CometChatLoadingState(
                            style = style.loadingStateStyle
                        )
                    }
                }
                
                is CallLogsUIState.Empty -> {
                    if (!hideEmptyState) {
                        emptyView?.invoke() ?: CometChatEmptyState(
                            style = style.emptyStateStyle,
                            title = context.getString(R.string.cometchat_call_logs_empty_title),
                            subtitle = context.getString(R.string.cometchat_call_logs_empty_subtitle)
                        )
                    }
                }
                
                is CallLogsUIState.Error -> {
                    if (!hideErrorState) {
                        errorView?.invoke { callLogsViewModel.fetchCallLogs() }
                            ?: CometChatErrorState(
                                style = style.errorStateStyle,
                                title = context.getString(R.string.cometchat_call_logs_error_title),
                                subtitle = context.getString(R.string.cometchat_call_logs_error_subtitle),
                                onRetry = { callLogsViewModel.fetchCallLogs() }
                            )
                    }
                }
                
                is CallLogsUIState.Content -> {
                    CallLogsListContent(
                        callLogs = callLogs,
                        style = effectiveStyle,
                        hideSeparator = hideSeparator,
                        dateTimeFormatter = dateTimeFormatter,
                        listItemView = listItemView,
                        leadingView = leadingView,
                        titleView = titleView,
                        subtitleView = subtitleView,
                        trailingView = trailingView,
                        onItemClick = onItemClick,
                        onItemLongClick = { callLog ->
                            if (options != null || addOptions != null) {
                                popupMenuCallLog = callLog
                                showPopupMenu = true
                            }
                            onItemLongClick?.invoke(callLog)
                        },
                        onCallIconClick = onCallIconClick,
                        onLoadMore = { callLogsViewModel.fetchCallLogs() },
                        scrollToTopEvent = callLogsViewModel.scrollToTopEvent
                    )
                }
            }
        }
    }
    
    // Popup menu
    if (showPopupMenu && popupMenuCallLog != null) {
        val menuItems = buildCallLogsMenuItems(
            context = context,
            callLog = popupMenuCallLog!!,
            options = options,
            addOptions = addOptions
        )
        
        if (menuItems.isNotEmpty()) {
            CometChatPopupMenu(
                expanded = showPopupMenu,
                onDismissRequest = {
                    showPopupMenu = false
                    popupMenuCallLog = null
                },
                menuItems = menuItems,
                onMenuItemClick = { _, _ ->
                    showPopupMenu = false
                    popupMenuCallLog = null
                },
                content = { /* Anchor content - empty since we're showing as overlay */ }
            )
        }
    }
}

/**
 * Internal composable for the call logs toolbar.
 */
@Composable
private fun CallLogsToolbar(
    title: String,
    style: CometChatCallLogsStyle,
    hideBackButton: Boolean,
    overflowMenu: (@Composable () -> Unit)?,
    onBackPress: (() -> Unit)?
) {
    val toolbarStyle = CometChatToolbarStyle.default(
        backgroundColor = style.backgroundColor,
        titleTextColor = style.titleTextColor,
        titleTextStyle = style.titleTextStyle,
        navigationIcon = style.backIcon,
        navigationIconTint = style.backIconTint,
        separatorColor = style.toolbarSeparatorColor,
        separatorHeight = style.toolbarSeparatorHeight,
        showSeparator = style.showToolbarSeparator
    )
    
    CometChatToolbar(
        title = title,
        style = toolbarStyle,
        hideBackIcon = hideBackButton,
        navigationContentDescription = "Go back",
        onNavigationClick = onBackPress,
        actions = if (overflowMenu != null) {
            { overflowMenu() }
        } else null
    )
}

/**
 * Creates an effective item style by applying custom icons if provided.
 */
@Composable
private fun createEffectiveItemStyle(
    baseStyle: CometChatCallLogsListItemStyle,
    incomingCallIcon: Painter?,
    outgoingCallIcon: Painter?,
    missedCallIcon: Painter?,
    audioCallIcon: Painter?,
    videoCallIcon: Painter?
): CometChatCallLogsListItemStyle {
    return baseStyle.copy(
        incomingCallIcon = incomingCallIcon ?: baseStyle.incomingCallIcon,
        outgoingCallIcon = outgoingCallIcon ?: baseStyle.outgoingCallIcon,
        missedCallIcon = missedCallIcon ?: baseStyle.missedCallIcon,
        audioCallIcon = audioCallIcon ?: baseStyle.audioCallIcon,
        videoCallIcon = videoCallIcon ?: baseStyle.videoCallIcon
    )
}

/**
 * Builds menu items for the call logs popup menu.
 * If options is set, uses those exclusively. Otherwise uses addOptions if set.
 */
private fun buildCallLogsMenuItems(
    context: Context,
    callLog: CallLog,
    options: ((Context, CallLog) -> List<MenuItem>)?,
    addOptions: ((Context, CallLog) -> List<MenuItem>)?
): List<MenuItem> {
    // If custom options are provided, use them exclusively
    if (options != null) {
        return options(context, callLog)
    }
    
    val menuItems = mutableListOf<MenuItem>()
    
    // Add additional options if provided
    if (addOptions != null) {
        menuItems.addAll(addOptions(context, callLog))
    }
    
    return menuItems
}

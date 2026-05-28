package com.cometchat.uikit.compose.presentation.notificationfeed.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyStateStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorStateStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Loading state — centered spinner with "Loading..." text below.
 * Matches Figma: Campaigns - Loading State
 */
@Composable
fun NotificationFeedLoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("notification-feed-loading-state")
            .semantics { contentDescription = "Loading notifications" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = CometChatTheme.colorScheme.primary,
            trackColor = CometChatTheme.colorScheme.strokeColorDefault,
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Loading...",
            style = CometChatTheme.typography.bodyRegular,
            color = CometChatTheme.colorScheme.textColorSecondary
        )
    }
}


/**
 * Empty state — illustration + title + description. No button.
 * Matches Figma: Campaigns - Empty State
 *
 * Uses shared CometChatEmptyState component with notification-specific styling.
 */
@Composable
fun NotificationFeedEmptyState(
    modifier: Modifier = Modifier
) {
    CometChatEmptyState(
        modifier = modifier.testTag("notification-feed-empty-state"),
        title = "Nothing here yet",
        subtitle = "New activity will appear here when available.",
        style = CometChatEmptyStateStyle.default(
            backgroundColor = CometChatTheme.colorScheme.backgroundColor2,
            icon = painterResource(R.drawable.cometchat_ic_empty_box),
            iconTint = CometChatTheme.colorScheme.neutralColor400,
            iconSize = 120.dp,
            titleTextColor = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle = CometChatTheme.typography.heading3Bold,
            subtitleTextColor = CometChatTheme.colorScheme.textColorTertiary,
            subtitleTextStyle = CometChatTheme.typography.bodyRegular
        )
    )
}

/**
 * Error state — illustration + "Oops!" + description + Retry button.
 * Matches Figma: Campaigns - Full Page Error
 *
 * Uses shared CometChatErrorState component with notification-specific styling.
 */
@Composable
fun NotificationFeedErrorState(
    exception: CometChatException,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    CometChatErrorState(
        onRetry = onRetry,
        modifier = modifier.testTag("notification-feed-error-state"),
        title = "Oops!",
        subtitle = "Looks like something went wrong.\nPlease try again.",
        style = CometChatErrorStateStyle.default(
            backgroundColor = CometChatTheme.colorScheme.backgroundColor2,
            icon = painterResource(R.drawable.cometchat_ic_error),
            iconTint = CometChatTheme.colorScheme.neutralColor400,
            iconSize = 120.dp,
            titleTextColor = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle = CometChatTheme.typography.heading3Bold,
            subtitleTextColor = CometChatTheme.colorScheme.textColorTertiary,
            subtitleTextStyle = CometChatTheme.typography.bodyRegular,
            retryButtonBackgroundColor = CometChatTheme.colorScheme.primary,
            retryButtonTextColor = CometChatTheme.colorScheme.colorWhite
        )
    )
}

package com.cometchat.uikit.compose.presentation.shared.defaultstates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Reusable empty state composable for list components.
 * Displays icon, title, and subtitle when no items exist.
 *
 * @param modifier Modifier to be applied to the component
 * @param style Style configuration for the component
 * @param title Title text to display
 * @param subtitle Subtitle text to display
 */
@Composable
fun CometChatEmptyState(
    modifier: Modifier = Modifier,
    style: CometChatEmptyStateStyle = CometChatEmptyStateStyle.default(),
    title: String = "No Items",
    subtitle: String = "There are no items to display"
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(style.backgroundColor)
            .padding(32.dp)
            .semantics {
                contentDescription = "$title. $subtitle"
                liveRegion = LiveRegionMode.Polite
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        style.icon?.let { icon ->
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(style.iconSize),
                tint = style.iconTint
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Title
        Text(
            text = title,
            style = style.titleTextStyle,
            color = style.titleTextColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = subtitle,
            style = style.subtitleTextStyle,
            color = style.subtitleTextColor,
            textAlign = TextAlign.Center
        )
    }
}

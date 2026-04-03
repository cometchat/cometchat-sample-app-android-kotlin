package com.cometchat.sampleapp.compose.ui.search

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Search screen composable placeholder.
 *
 * The CometChatSearch component is not yet implemented in chatuikit-jetpack.
 * This screen displays a "Search Coming Soon" placeholder UI.
 *
 * @param userId Optional user ID for contextual search (not yet implemented)
 * @param groupId Optional group ID for contextual search (not yet implemented)
 * @param onBackPress Callback when back button is pressed
 * @param onNavigateToMessages Callback to navigate to messages screen (not yet implemented)
 * @param onNavigateToThread Callback to navigate to thread messages screen (not yet implemented)
 */
@Composable
fun SearchScreen(
    userId: String? = null,
    groupId: String? = null,
    onBackPress: () -> Unit,
    onNavigateToMessages: (userId: String?, groupId: String?) -> Unit = { _, _ -> },
    onNavigateToThread: (parentMessageId: Long) -> Unit = {}
) {
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.backgroundColor1)
    ) {
        // Toolbar
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
                    text = "Search",
                    style = typography.heading1Bold,
                    color = colorScheme.textColorPrimary
                )
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = colorScheme.strokeColorLight
            )
        }

        // Placeholder content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = colorScheme.iconTintSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Search Coming Soon",
                    style = typography.heading2Bold,
                    color = colorScheme.textColorPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The search feature is not yet available in the Jetpack Compose UI Kit.",
                    style = typography.bodyRegular,
                    color = colorScheme.textColorSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

package com.cometchat.sampleapp.compose.push.appflow.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Reusable toolbar component for App Flow screens.
 * Matches the 64dp height and 1dp separator styling from master-app-kotlin2.
 *
 * @param title The title text to display
 * @param onBackPress Callback when back button is pressed
 * @param modifier Modifier for the toolbar
 * @param titleStyle Text style for the title (defaults to Heading2Bold)
 * @param subtitle Optional subtitle text
 * @param subtitleStyle Text style for the subtitle
 * @param trailingContent Optional trailing content (e.g., menu icons)
 */
@Composable
fun AppFlowToolbar(
    title: String,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = CometChatTheme.typography.heading2Bold,
    subtitle: String? = null,
    subtitleStyle: TextStyle = CometChatTheme.typography.caption1Regular,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val backgroundColor = CometChatTheme.colorScheme.backgroundColor1
    val textColorPrimary = CometChatTheme.colorScheme.textColorPrimary
    val textColorSecondary = CometChatTheme.colorScheme.textColorSecondary
    val iconTint = CometChatTheme.colorScheme.iconTintPrimary
    val separatorColor = CometChatTheme.colorScheme.strokeColorLight

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Toolbar content - 64dp height
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Icon(
                painter = painterResource(id = R.drawable.cometchat_ic_back),
                contentDescription = "Back",
                tint = iconTint,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBackPress() }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Title and subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = titleStyle,
                    color = textColorPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = subtitleStyle,
                        color = textColorSecondary
                    )
                }
            }

            // Trailing content
            trailingContent?.invoke()
        }

        // 1dp separator line
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = separatorColor
        )
    }
}

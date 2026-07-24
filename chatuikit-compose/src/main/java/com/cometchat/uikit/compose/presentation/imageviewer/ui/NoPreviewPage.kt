package com.cometchat.uikit.compose.presentation.imageviewer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Viewer page shown for an attachment whose type can't be previewed (kind-mismatched item of a
 * server-sent mixed payload, reached by tapping its broken tile or swiping to it in the
 * image/video viewer carousel). Google Drive-style: icon in a circle, "No preview available"
 * title + subtitle, and a full-width Download button.
 */
@Composable
internal fun NoPreviewPage(
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                // Slightly translucent so the viewer's backdrop shows through the card.
                .background(CometChatTheme.colorScheme.backgroundColor1.copy(alpha = 0.85f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(CometChatTheme.colorScheme.neutralColor100),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cometchat_unsupported_file_icon),
                    contentDescription = null,
                    modifier = Modifier.size(width = 33.dp, height = 40.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.cometchat_no_preview_available),
                style = CometChatTheme.typography.heading4Bold,
                color = CometChatTheme.colorScheme.textColorPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.cometchat_no_preview_subtitle),
                style = CometChatTheme.typography.bodyRegular,
                color = CometChatTheme.colorScheme.textColorSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CometChatTheme.colorScheme.primaryButtonBackgroundColor)
                    .clickable(onClick = onDownloadClick)
                    .padding(vertical = 14.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cometchat_download_icon),
                    contentDescription = null,
                    tint = CometChatTheme.colorScheme.primaryButtonIconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.cometchat_download),
                    style = CometChatTheme.typography.buttonBold,
                    color = CometChatTheme.colorScheme.primaryButtonTextColor
                )
            }
        }
    }
}

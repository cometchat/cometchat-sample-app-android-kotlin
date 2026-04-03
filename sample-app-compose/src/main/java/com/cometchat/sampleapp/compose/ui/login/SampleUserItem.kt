package com.cometchat.sampleapp.compose.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.sampleapp.compose.R

/**
 * Composable for displaying a sample user item in the grid.
 * Matches master-app-jetpack SampleUserItem pattern exactly.
 *
 * Selection Visual Feedback:
 * - Selected: borderColor = cometchatStrokeColorHighlight, backgroundColor = cometchatExtendedPrimaryColor50
 * - Unselected: borderColor = cometchatStrokeColorLight, backgroundColor = cometchatBackgroundColor1
 */
@Composable
fun SampleUserItem(
    user: User,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography

    // Selection visual feedback colors
    val borderColor = if (isSelected) {
        colorScheme.strokeColorHighlight
    } else {
        colorScheme.strokeColorLight
    }

    val backgroundColor = if (isSelected) {
        colorScheme.extendedPrimaryColor50
    } else {
        colorScheme.backgroundColor1
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp), // cometchat_margin_1 = 4dp
        shape = RoundedCornerShape(8.dp), // cometchat_radius_2 = 8dp
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, borderColor), // cometchat_2dp = 2dp
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Selection indicator - top-right corner
            if (isSelected) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sample_user_selection),
                    contentDescription = "Selected",
                    modifier = Modifier.align(Alignment.TopEnd),
                    tint = androidx.compose.ui.graphics.Color.Unspecified
                )
            }

            // Content column - padding: 10dp (cometchat_10dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar - 48dp x 48dp using CometChatAvatar
                CometChatAvatar(
                    modifier = Modifier.size(48.dp),
                    name = user.name ?: "",
                    avatarUrl = user.avatar
                )

                // Name - marginTop: 10dp (cometchat_10dp)
                Text(
                    text = user.name ?: "",
                    style = typography.bodyMedium,
                    color = colorScheme.textColorPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp)
                )

                // UID - marginTop: 4dp (cometchat_margin_1)
                Text(
                    text = user.uid ?: "",
                    style = typography.caption1Regular,
                    color = colorScheme.textColorSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

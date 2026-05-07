package com.cometchat.ai.sampleapp.compose.ui.login

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.ai.sampleapp.compose.R
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Grid cell for a sample user. Displays avatar + name + uid with visual
 * feedback for selection.
 */
@Composable
fun SampleUserItem(
    user: User,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography

    val borderColor = if (isSelected) colorScheme.strokeColorHighlight else colorScheme.strokeColorLight
    val backgroundColor = if (isSelected) colorScheme.extendedPrimaryColor50 else colorScheme.backgroundColor1

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isSelected) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sample_user_selection),
                    contentDescription = "Selected",
                    modifier = Modifier.align(Alignment.TopEnd),
                    tint = Color.Unspecified
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CometChatAvatar(
                    modifier = Modifier.size(48.dp),
                    name = user.name ?: "",
                    avatarUrl = user.avatar
                )

                Text(
                    text = user.name ?: "",
                    style = typography.bodyMedium,
                    color = colorScheme.textColorPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp)
                )

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

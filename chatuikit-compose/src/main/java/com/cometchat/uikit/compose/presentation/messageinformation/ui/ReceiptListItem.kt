package com.cometchat.uikit.compose.presentation.messageinformation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messageinformation.style.CometChatMessageInformationStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Internal composable for displaying a single receipt list item in GROUP conversations.
 * 
 * Per design doc: Receipt List Item Layout (GROUP) section.
 * Layout matches chatuikit-kotlin XML (cometchat_message_information_list_item.xml):
 * - Display CometChatAvatar (40dp x 40dp) with user avatar/initials
 * - Display user name next to avatar with 12dp margin (cometchat_margin_3)
 * - Display read section with "Read" label and timestamp (visible if readAt > 0), 2dp top margin
 * - Display delivered section with "Delivered" label and timestamp (visible if deliveredAt > 0), 2dp top margin
 * - Apply horizontal padding 16dp (cometchat_padding_4) and vertical padding 12dp (cometchat_padding_3)
 *
 * @param receipt The MessageReceipt to display
 * @param style Style configuration for the item
 * @param modifier Modifier for the item container
 */
@Composable
internal fun ReceiptListItem(
    receipt: MessageReceipt,
    style: CometChatMessageInformationStyle,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar - 40dp x 40dp (matches cometchat_40dp)
        CometChatAvatar(
            name = receipt.sender?.name ?: "",
            avatarUrl = receipt.sender?.avatar,
            style = style.itemAvatarStyle,
            modifier = Modifier.size(40.dp)
        )
        
        // 12dp margin between avatar and text content (matches cometchat_margin_3)
        Spacer(modifier = Modifier.width(12.dp))
        
        // Text content column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // User name with heading4Medium style
            Text(
                text = receipt.sender?.name ?: "",
                style = style.itemNameTextStyle,
                color = style.itemNameTextColor
            )
            
            // Read section - visible if readAt > 0, 2dp top margin (matches cometchat_2dp)
            if (receipt.readAt > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                ReceiptTimestampRow(
                    label = stringResource(R.string.cometchat_read),
                    timestamp = receipt.readAt,
                    labelStyle = style.itemReadTextStyle,
                    labelColor = style.itemReadTextColor,
                    dateStyle = style.itemReadDateTextStyle,
                    dateColor = style.itemReadDateTextColor
                )
            }
            
            // Delivered section - visible if deliveredAt > 0, 2dp top margin (matches cometchat_2dp)
            if (receipt.deliveredAt > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                ReceiptTimestampRow(
                    label = stringResource(R.string.cometchat_deliver),
                    timestamp = receipt.deliveredAt,
                    labelStyle = style.itemDeliveredTextStyle,
                    labelColor = style.itemDeliveredTextColor,
                    dateStyle = style.itemDeliveredDateTextStyle,
                    dateColor = style.itemDeliveredDateTextColor
                )
            }
        }
    }
}

/**
 * Internal composable for displaying a label and timestamp row.
 *
 * @param label The label text (e.g., "Read", "Delivered")
 * @param timestamp The timestamp in seconds
 * @param labelStyle Text style for the label
 * @param labelColor Color for the label
 * @param dateStyle Text style for the date
 * @param dateColor Color for the date
 */
@Composable
private fun ReceiptTimestampRow(
    label: String,
    timestamp: Long,
    labelStyle: androidx.compose.ui.text.TextStyle,
    labelColor: androidx.compose.ui.graphics.Color,
    dateStyle: androidx.compose.ui.text.TextStyle,
    dateColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = labelStyle,
            color = labelColor
        )
        
        Text(
            text = formatDateTime(timestamp * 1000), // Convert seconds to milliseconds
            style = dateStyle,
            color = dateColor
        )
    }
}

/**
 * Formats a timestamp in milliseconds to "dd/M/yyyy, h:mm a" format.
 * Per design doc: Date/Time Formatting section.
 *
 * @param milliseconds The timestamp in milliseconds
 * @return Formatted date string
 */
internal fun formatDateTime(milliseconds: Long): String {
    val sdf = SimpleDateFormat("dd/M/yyyy, h:mm a", Locale.getDefault())
    return sdf.format(Date(milliseconds))
}

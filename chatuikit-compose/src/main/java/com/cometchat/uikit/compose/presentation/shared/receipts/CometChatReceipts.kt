package com.cometchat.uikit.compose.presentation.shared.receipts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import com.cometchat.chat.models.BaseMessage

/**
 * CometChatReceipts displays the message receipt status with appropriate icons and colors.
 *
 * This is a standalone reusable component that can be used across the UIKit.
 *
 * The component displays different icons and tints based on the receipt status:
 * - IN_PROGRESS: Shows wait icon with waitIconTint
 * - SENT: Shows sent icon with sentIconTint
 * - DELIVERED: Shows delivered icon with deliveredIconTint
 * - READ: Shows read icon with readIconTint
 * - ERROR: Shows error icon with errorIconTint
 *
 * @param receipt The Receipt enum value representing the current receipt status
 * @param modifier Modifier for the receipt indicator
 * @param style Style configuration for the receipt indicator
 */
@Composable
fun CometChatReceipts(
    receipt: Receipt,
    modifier: Modifier = Modifier,
    style: CometChatReceiptsStyle = CometChatReceiptsStyle.default()
) {
    // Get the appropriate icon and tint based on receipt status
    val (icon, tint) = when (receipt) {
        Receipt.IN_PROGRESS -> style.waitIcon to style.waitIconTint
        Receipt.SENT -> style.sentIcon to style.sentIconTint
        Receipt.DELIVERED -> style.deliveredIcon to style.deliveredIconTint
        Receipt.READ -> style.readIcon to style.readIconTint
        Receipt.ERROR -> style.errorIcon to style.errorIconTint
    }

    // Don't render if no icon is available
    if (icon == null) {
        return
    }

    Image(
        painter = icon,
        contentDescription = when (receipt) {
            Receipt.IN_PROGRESS -> "Message in progress"
            Receipt.SENT -> "Message sent"
            Receipt.DELIVERED -> "Message delivered"
            Receipt.READ -> "Message read"
            Receipt.ERROR -> "Message error"
        },
        modifier = modifier.size(style.size),
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(tint)
    )
}

/**
 * CometChatReceipts overload that accepts a BaseMessage and extracts the receipt status.
 * This is a convenience function that determines the receipt status based on message properties.
 *
 * Receipt status is determined as follows:
 * - READ: If message.readAt > 0
 * - DELIVERED: If message.deliveredAt > 0
 * - SENT: If message.id > 0 (message has server-assigned ID)
 * - IN_PROGRESS: Otherwise (message is being sent)
 *
 * @param message The BaseMessage to extract receipt status from
 * @param modifier Modifier for the receipt indicator
 * @param style Style configuration for the receipt indicator
 *
 * @sample
 * ```
 * CometChatReceipts(
 *     message = baseMessage,
 *     style = CometChatReceiptsStyle.default()
 * )
 * ```
 */
@Composable
fun CometChatReceipts(
    message: BaseMessage,
    modifier: Modifier = Modifier,
    style: CometChatReceiptsStyle = CometChatReceiptsStyle.default()
) {
    val receipt = getReceiptFromMessage(message)
    CometChatReceipts(
        receipt = receipt,
        modifier = modifier,
        style = style
    )
}

/**
 * Extracts the Receipt status from a BaseMessage.
 *
 * @param message The BaseMessage to extract receipt status from
 * @return The Receipt enum value representing the message's current status
 */
private fun getReceiptFromMessage(message: BaseMessage): Receipt {
    return when {
        message.readAt > 0 -> Receipt.READ
        message.deliveredAt > 0 -> Receipt.DELIVERED
        message.id > 0 -> Receipt.SENT
        else -> Receipt.IN_PROGRESS
    }
}

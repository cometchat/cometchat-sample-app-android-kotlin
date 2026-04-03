package com.cometchat.uikit.compose.presentation.shared.receipts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatReceipts.
 * Mirrors the styling capabilities of the View-based CometChatMessageReceipt.
 *
 * Corresponds to XML attributes in attr_cometchat_message_receipt.xml:
 * - cometchatMessageReceiptWaitIcon
 * - cometchatMessageReceiptSentIcon
 * - cometchatMessageReceiptDeliveredIcon
 * - cometchatMessageReceiptReadIcon
 * - cometchatMessageReceiptErrorIcon
 * - cometchatMessageReceiptWaitIconTint
 * - cometchatMessageReceiptSentIconTint
 * - cometchatMessageReceiptDeliveredIconTint
 * - cometchatMessageReceiptReadIconTint
 * - cometchatMessageReceiptErrorIconTint
 *
 * @param waitIcon Icon to display when receipt is IN_PROGRESS
 * @param sentIcon Icon to display when receipt is SENT
 * @param deliveredIcon Icon to display when receipt is DELIVERED
 * @param readIcon Icon to display when receipt is READ
 * @param errorIcon Icon to display when receipt is ERROR
 * @param waitIconTint Tint color for the wait icon
 * @param sentIconTint Tint color for the sent icon
 * @param deliveredIconTint Tint color for the delivered icon
 * @param readIconTint Tint color for the read icon
 * @param errorIconTint Tint color for the error icon
 * @param size Size of the receipt icon
 */
@Immutable
data class CometChatReceiptsStyle(
    val waitIcon: Painter?,
    val sentIcon: Painter?,
    val deliveredIcon: Painter?,
    val readIcon: Painter?,
    val errorIcon: Painter?,
    val waitIconTint: Color,
    val sentIconTint: Color,
    val deliveredIconTint: Color,
    val readIconTint: Color,
    val errorIconTint: Color,
    val size: Dp
) {
    companion object {
        /**
         * Creates a default CometChatReceiptsStyle with values sourced from CometChatTheme.
         *
         * @return A new CometChatReceiptsStyle instance with theme-based default values
         */
        @Composable
        fun default(
            waitIcon: Painter? = painterResource(R.drawable.cometchat_ic_message_waiting),
            sentIcon: Painter? = painterResource(R.drawable.cometchat_ic_message_sent),
            deliveredIcon: Painter? = painterResource(R.drawable.cometchat_ic_message_delivered),
            readIcon: Painter? = painterResource(R.drawable.cometchat_ic_message_read),
            errorIcon: Painter? = painterResource(R.drawable.cometchat_ic_message_error),
            waitIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            sentIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            deliveredIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            readIconTint: Color = CometChatTheme.colorScheme.messageReadColor,
            errorIconTint: Color = CometChatTheme.colorScheme.errorColor,
            size: Dp = 16.dp
        ): CometChatReceiptsStyle = CometChatReceiptsStyle(
            waitIcon = waitIcon,
            sentIcon = sentIcon,
            deliveredIcon = deliveredIcon,
            readIcon = readIcon,
            errorIcon = errorIcon,
            waitIconTint = waitIconTint,
            sentIconTint = sentIconTint,
            deliveredIconTint = deliveredIconTint,
            readIconTint = readIconTint,
            errorIconTint = errorIconTint,
            size = size
        )
    }
}

package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.receipts.CometChatReceipts
import com.cometchat.uikit.compose.presentation.shared.receipts.CometChatReceiptsStyle
import com.cometchat.uikit.compose.presentation.shared.receipts.Receipt
import com.cometchat.uikit.compose.theme.CometChatTheme

// ============================================================================
// Preview: Individual Receipt States
// ============================================================================

/**
 * Preview showing the IN_PROGRESS receipt status.
 */
@Preview(showBackground = true, name = "Receipt - In Progress")
@Composable
fun PreviewReceiptInProgress() {
    CometChatTheme {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CometChatReceipts(
                receipt = Receipt.IN_PROGRESS
            )
        }
    }
}


/**
 * Preview showing the SENT receipt status.
 */
@Preview(showBackground = true, name = "Receipt - Sent")
@Composable
fun PreviewReceiptSent() {
    CometChatTheme {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CometChatReceipts(
                receipt = Receipt.SENT
            )
        }
    }
}

/**
 * Preview showing the DELIVERED receipt status.
 */
@Preview(showBackground = true, name = "Receipt - Delivered")
@Composable
fun PreviewReceiptDelivered() {
    CometChatTheme {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CometChatReceipts(
                receipt = Receipt.DELIVERED
            )
        }
    }
}

/**
 * Preview showing the READ receipt status.
 */
@Preview(showBackground = true, name = "Receipt - Read")
@Composable
fun PreviewReceiptRead() {
    CometChatTheme {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CometChatReceipts(
                receipt = Receipt.READ
            )
        }
    }
}

/**
 * Preview showing the ERROR receipt status.
 */
@Preview(showBackground = true, name = "Receipt - Error")
@Composable
fun PreviewReceiptError() {
    CometChatTheme {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CometChatReceipts(
                receipt = Receipt.ERROR
            )
        }
    }
}

// ============================================================================
// Preview: All Receipt States
// ============================================================================

/**
 * Preview showing all receipt states in a column with labels.
 */
@Preview(showBackground = true, name = "Receipt - All States")
@Composable
fun PreviewReceiptAllStates() {
    CometChatTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Receipt.values().forEach { receipt ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CometChatReceipts(receipt = receipt)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = receipt.name,
                        color = CometChatTheme.colorScheme.textColorPrimary
                    )
                }
            }
        }
    }
}

/**
 * Preview showing all receipt states in a horizontal row.
 */
@Preview(showBackground = true, name = "Receipt - All States Row")
@Composable
fun PreviewReceiptAllStatesRow() {
    CometChatTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Receipt.values().forEach { receipt ->
                CometChatReceipts(receipt = receipt)
            }
        }
    }
}

// ============================================================================
// Preview: With Message Context
// ============================================================================

/**
 * Preview showing receipts in a message context (simulated message row).
 */
@Preview(showBackground = true, name = "Receipt - Message Context")
@Composable
fun PreviewReceiptMessageContext() {
    CometChatTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // In Progress message
            Row(verticalAlignment = Alignment.CenterVertically) {
                CometChatReceipts(receipt = Receipt.IN_PROGRESS)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sending message...",
                    color = CometChatTheme.colorScheme.textColorSecondary
                )
            }
            
            // Sent message
            Row(verticalAlignment = Alignment.CenterVertically) {
                CometChatReceipts(receipt = Receipt.SENT)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Hello, how are you?",
                    color = CometChatTheme.colorScheme.textColorSecondary
                )
            }
            
            // Delivered message
            Row(verticalAlignment = Alignment.CenterVertically) {
                CometChatReceipts(receipt = Receipt.DELIVERED)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "I'm doing great!",
                    color = CometChatTheme.colorScheme.textColorSecondary
                )
            }
            
            // Read message
            Row(verticalAlignment = Alignment.CenterVertically) {
                CometChatReceipts(receipt = Receipt.READ)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "See you tomorrow!",
                    color = CometChatTheme.colorScheme.textColorSecondary
                )
            }
            
            // Error message
            Row(verticalAlignment = Alignment.CenterVertically) {
                CometChatReceipts(receipt = Receipt.ERROR)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Failed to send",
                    color = CometChatTheme.colorScheme.errorColor
                )
            }
        }
    }
}

// ============================================================================
// Preview: Custom Styling
// ============================================================================

/**
 * Preview showing receipts with custom styling.
 */
@Preview(showBackground = true, name = "Receipt - Custom Style")
@Composable
fun PreviewReceiptCustomStyle() {
    CometChatTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Default size (16dp)
            CometChatReceipts(
                receipt = Receipt.READ
            )
            
            // Larger size
            CometChatReceipts(
                receipt = Receipt.READ,
                style = CometChatReceiptsStyle.default(
                    size = 24.dp
                )
            )
            
            // Custom tint colors
            CometChatReceipts(
                receipt = Receipt.DELIVERED,
                style = CometChatReceiptsStyle.default(
                    deliveredIconTint = CometChatTheme.colorScheme.primary
                )
            )
        }
    }
}

/**
 * Preview showing receipts with all custom tint colors.
 */
@Preview(showBackground = true, name = "Receipt - Custom Tints")
@Composable
fun PreviewReceiptCustomTints() {
    CometChatTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val customStyle = CometChatReceiptsStyle.default(
                waitIconTint = CometChatTheme.colorScheme.warningColor,
                sentIconTint = CometChatTheme.colorScheme.primary,
                deliveredIconTint = CometChatTheme.colorScheme.infoColor,
                readIconTint = CometChatTheme.colorScheme.successColor,
                errorIconTint = CometChatTheme.colorScheme.errorColor
            )
            
            Receipt.values().forEach { receipt ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CometChatReceipts(
                        receipt = receipt,
                        style = customStyle
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${receipt.name} (Custom Tint)",
                        color = CometChatTheme.colorScheme.textColorPrimary
                    )
                }
            }
        }
    }
}

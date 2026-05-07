package com.cometchat.uikit.compose.preview.presentation.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatDialog
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatDialogStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

@Preview(showBackground = true, name = "Dialog - Confirmation")
@Composable
fun PreviewDialogConfirmation() {
    CometChatTheme {
        CometChatDialog(
            title = "Delete Message",
            message = "Are you sure you want to delete this message?",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel",
            onPositiveClick = { },
            onNegativeClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Dialog - Alert (No Negative)")
@Composable
fun PreviewDialogAlert() {
    CometChatTheme {
        CometChatDialog(
            title = "Error",
            message = "Something went wrong. Please try again.",
            positiveButtonText = "OK",
            hideNegativeButton = true,
            onPositiveClick = { },
            onNegativeClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Dialog - No Title")
@Composable
fun PreviewDialogNoTitle() {
    CometChatTheme {
        CometChatDialog(
            title = "",
            message = "This action cannot be undone.",
            positiveButtonText = "Confirm",
            negativeButtonText = "Cancel",
            hideTitle = true,
            onPositiveClick = { },
            onNegativeClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Dialog - No Message")
@Composable
fun PreviewDialogNoMessage() {
    CometChatTheme {
        CometChatDialog(
            title = "Leave Group",
            message = "",
            positiveButtonText = "Leave",
            negativeButtonText = "Stay",
            hideMessage = true,
            onPositiveClick = { },
            onNegativeClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Dialog - With Progress")
@Composable
fun PreviewDialogWithProgress() {
    CometChatTheme {
        CometChatDialog(
            title = "Deleting...",
            message = "Please wait while the message is being deleted.",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel",
            showPositiveButtonProgress = true,
            onPositiveClick = { },
            onNegativeClick = { }
        )
    }
}

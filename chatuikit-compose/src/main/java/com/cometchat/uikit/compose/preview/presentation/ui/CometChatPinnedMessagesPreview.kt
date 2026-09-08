package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.compose.presentation.pinnedmessages.ui.CometChatPinnedMessages
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.viewmodel.CometChatPinnedMessagesViewModel

/**
 * Previews for the CometChatPinnedMessages screen.
 *
 * The screen renders from its ViewModel's state, so each preview seeds a listener-free ViewModel
 * through the external live-upkeep hooks (onMessagePinnedExternally) — the same synchronous path
 * realtime pin events use — instead of hitting the SDK fetch. Static previews don't run
 * LaunchedEffect, so the seeded state is exactly what renders.
 */
private const val PREVIEW_PEER_UID = "receiver_1"

private fun createPinnedMessage(id: Long, text: String, senderName: String): BaseMessage {
    return PreviewMockData.createMockTextMessage(
        id = id,
        text = text,
        sender = PreviewMockData.createMockUser(uid = "user_$id", name = senderName)
    ).apply {
        pinnedAt = sentAt
        pinnedBy = PREVIEW_PEER_UID
    }
}

/** ViewModel seeded with pinned messages; pass `emptyList()` for the empty state. */
private fun previewPinnedViewModel(messages: List<BaseMessage>): CometChatPinnedMessagesViewModel {
    val vm = CometChatPinnedMessagesViewModel(enableListeners = false)
    vm.configure(uid = PREVIEW_PEER_UID, guid = null)
    // Seed newest-first ordering: each call prepends, so feed oldest → newest reversed.
    messages.asReversed().forEach { vm.onMessagePinnedExternally(it) }
    if (messages.isEmpty()) {
        // Drive the Empty state through the same public surface: seed one row and remove it.
        val placeholder = createPinnedMessage(999L, "placeholder", "Placeholder")
        vm.onMessagePinnedExternally(placeholder)
        vm.onMessageUnpinnedExternally(placeholder)
    }
    return vm
}

private fun defaultPinnedMessages(): List<BaseMessage> = listOf(
    createPinnedMessage(1L, "Team standup moved to 10am tomorrow.", "Alice Smith"),
    createPinnedMessage(2L, "Sharing the final launch checklist here.", "Iron Man"),
    createPinnedMessage(3L, "Wifi password for the office: cometchat123", "Bob Johnson")
)

/**
 * Preview showing the pinned messages list with content.
 */
@Preview(showBackground = true, name = "PinnedMessages - Content")
@Composable
fun PreviewPinnedMessagesContent() {
    CometChatTheme {
        CometChatPinnedMessages(
            viewModel = previewPinnedViewModel(defaultPinnedMessages())
        )
    }
}

/**
 * Preview showing the empty state ("No pinned messages yet").
 */
@Preview(showBackground = true, name = "PinnedMessages - Empty")
@Composable
fun PreviewPinnedMessagesEmpty() {
    CometChatTheme {
        CometChatPinnedMessages(
            viewModel = previewPinnedViewModel(emptyList())
        )
    }
}

/**
 * Preview showing a single pinned message.
 */
@Preview(showBackground = true, name = "PinnedMessages - Single Message")
@Composable
fun PreviewPinnedMessagesSingle() {
    CometChatTheme {
        CometChatPinnedMessages(
            viewModel = previewPinnedViewModel(
                listOf(createPinnedMessage(1L, "Pinned for quick reference.", "Alice Smith"))
            )
        )
    }
}

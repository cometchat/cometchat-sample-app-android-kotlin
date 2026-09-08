package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.savedmessages.ui.CometChatSavedMessages
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.viewmodel.CometChatSavedMessagesViewModel

/**
 * Previews for the CometChatSavedMessages screen.
 *
 * The screen renders from its ViewModel's state, so each preview seeds a listener-free ViewModel
 * through the external live-upkeep hook (onMessageSavedExternally) — the same synchronous path
 * realtime save events use — instead of hitting the SDK fetch. Static previews don't run
 * LaunchedEffect, so the seeded state is exactly what renders.
 */

/** A saved 1-1 message from [sender]; the row resolves the peer from sender/receiver. */
private fun createSavedUserMessage(id: Long, text: String, sender: User): BaseMessage {
    return PreviewMockData.createMockTextMessage(id = id, text = text, sender = sender).apply {
        receiver = PreviewMockData.createMockUser(uid = "receiver_1", name = "You")
        savedAt = sentAt
    }
}

/** A saved group message; the row shows the group as the conversation context. */
private fun createSavedGroupMessage(id: Long, text: String, sender: User, group: Group): BaseMessage {
    return PreviewMockData.createMockTextMessage(id = id, text = text, sender = sender).apply {
        receiverUid = group.guid
        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
        receiver = group
        savedAt = sentAt
    }
}

/** ViewModel seeded with saved messages; pass `emptyList()` for the empty state. */
private fun previewSavedViewModel(messages: List<BaseMessage>): CometChatSavedMessagesViewModel {
    val vm = CometChatSavedMessagesViewModel(enableListeners = false)
    // Seed newest-first ordering: each call prepends, so feed oldest → newest reversed.
    messages.asReversed().forEach { vm.onMessageSavedExternally(it) }
    if (messages.isEmpty()) {
        // Drive the Empty state through the same public surface: seed one row and remove it.
        val placeholder = createSavedUserMessage(999L, "placeholder", PreviewMockData.createMockUser())
        vm.onMessageSavedExternally(placeholder)
        vm.onMessageUnsavedExternally(placeholder)
    }
    return vm
}

private fun defaultSavedMessages(): List<BaseMessage> {
    val alice = PreviewMockData.createMockUser(uid = "alice", name = "Alice Smith")
    val bob = PreviewMockData.createMockUser(uid = "bob", name = "Bob Johnson")
    val group = PreviewMockData.createMockGroup(guid = "team_launch", name = "Launch Team")
    return listOf(
        createSavedUserMessage(1L, "Here is the address for Friday's dinner.", alice),
        createSavedGroupMessage(2L, "Release notes draft is in the shared doc.", bob, group),
        createSavedUserMessage(3L, "Flight lands at 6:45pm, terminal 2.", bob)
    )
}

/**
 * Preview showing the saved messages list with 1-1 and group rows.
 */
@Preview(showBackground = true, name = "SavedMessages - Content")
@Composable
fun PreviewSavedMessagesContent() {
    CometChatTheme {
        CometChatSavedMessages(
            viewModel = previewSavedViewModel(defaultSavedMessages())
        )
    }
}

/**
 * Preview showing the empty state ("No saved messages yet").
 */
@Preview(showBackground = true, name = "SavedMessages - Empty")
@Composable
fun PreviewSavedMessagesEmpty() {
    CometChatTheme {
        CometChatSavedMessages(
            viewModel = previewSavedViewModel(emptyList())
        )
    }
}

/**
 * Preview showing a single saved message row.
 */
@Preview(showBackground = true, name = "SavedMessages - Single Message")
@Composable
fun PreviewSavedMessagesSingle() {
    CometChatTheme {
        CometChatSavedMessages(
            viewModel = previewSavedViewModel(
                listOf(
                    createSavedUserMessage(
                        1L, "Saved for later reading.",
                        PreviewMockData.createMockUser(uid = "alice", name = "Alice Smith")
                    )
                )
            )
        )
    }
}

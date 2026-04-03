package com.cometchat.uikit.compose.presentation.threadheader.viewmodel

import com.cometchat.uikit.core.viewmodel.CometChatThreadHeaderViewModel

/**
 * ViewModel for CometChatThreadHeader composable in chatuikit-jetpack.
 *
 * This ViewModel extends [CometChatThreadHeaderViewModel] from chatuikit-core.
 * The core ViewModel already uses StateFlow which is ideal for Compose, so this
 * class simply provides a type alias with the expected package location.
 *
 * ## Key Responsibilities
 * 1. Extend core ViewModel functionality
 * 2. Maintain backward compatibility with existing Compose code
 *
 * ## StateFlow Observables (inherited from core)
 * - [parentMessageListStateFlow]: Parent message list for composable (single-item list)
 * - [replyCountStateFlow]: Current reply count
 * - [sentMessage]: Sent message events (SharedFlow for one-time events)
 * - [receiveMessage]: Received message events (SharedFlow for one-time events)
 * - [updateParentMessage]: Parent message updates (SharedFlow for one-time events)
 *
 * ## Usage
 * ```kotlin
 * @Composable
 * fun ThreadHeaderScreen(parentMessage: BaseMessage) {
 *     val viewModel: ThreadHeaderViewModel = viewModel()
 *
 *     LaunchedEffect(parentMessage) {
 *         viewModel.setParentMessage(parentMessage)
 *     }
 *
 *     val replyCount by viewModel.replyCountStateFlow.collectAsState()
 *     val messageList by viewModel.parentMessageListStateFlow.collectAsState()
 *
 *     // Render UI with replyCount and messageList
 * }
 * ```
 *
 * @see CometChatThreadHeaderViewModel
 * @see com.cometchat.uikit.compose.presentation.threadheader.ui.CometChatThreadHeader
 */
class ThreadHeaderViewModel(
    enableListeners: Boolean = true
) : CometChatThreadHeaderViewModel(enableListeners)

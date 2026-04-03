/**
 * AI Conversation Summary component for CometChat UIKit.
 *
 * This package contains the Kotlin XML Views implementation of the AI Conversation Summary feature,
 * which displays an AI-generated summary of the conversation when there are many unread messages.
 *
 * Components:
 * - [CometChatAIConversationSummaryView] - Main view component extending MaterialCardView
 *   - Displays an AI-generated conversation summary text
 *   - Supports loading state with shimmer effect
 *   - Supports error state with customizable error message
 *   - Includes close icon for dismissing the summary
 *   - Customizable styling for container and summary text
 * - [AIConversationSummaryAdapter] - RecyclerView adapter for displaying summary item
 *   - Handles item styling (background, stroke, corner radius, text)
 *   - Displays single summary text item
 *
 * Usage:
 * ```kotlin
 * val view = CometChatAIConversationSummaryView(context)
 * view.setSummary("This conversation discussed project deadlines and team assignments.")
 * view.setOnCloseClickListener {
 *     // Handle close click - dismiss the summary
 * }
 * ```
 *
 * The summary is automatically shown when:
 * - enableConversationSummary is true
 * - Unread message count exceeds the threshold (default 30)
 * - User is in main conversation (not thread view)
 *
 * @see com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel for ViewModel integration
 * @see com.cometchat.uikit.core.state.ConversationSummaryUIState for UI state management
 */
package com.cometchat.uikit.kotlin.presentation.shared.aiconversationsummary

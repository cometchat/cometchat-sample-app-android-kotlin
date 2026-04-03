/**
 * AI Conversation Starter component for CometChat UIKit.
 *
 * This package contains the Kotlin XML Views implementation of the AI Conversation Starter feature,
 * which displays AI-generated conversation starter suggestions when starting a new conversation.
 *
 * Components:
 * - [CometChatAIConversationStarterView] - Main view component extending MaterialCardView
 *   - Displays a list of AI-generated conversation starter suggestions
 *   - Supports loading state with shimmer effect
 *   - Supports error state with customizable error message
 *   - Customizable styling for container and items
 * - [ConversationStarterAdapter] - RecyclerView adapter for displaying starter items
 *   - Handles item styling (background, stroke, corner radius, text)
 *   - Supports click handling via tag-based item identification
 *
 * Usage:
 * ```kotlin
 * val view = CometChatAIConversationStarterView(context)
 * view.setReplyList(listOf("Hello!", "How are you?", "Let's chat!"))
 * view.setOnItemClickListener { uid, reply, position ->
 *     // Handle the click event - send message or callback
 * }
 * ```
 *
 * @see com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel for ViewModel integration
 * @see com.cometchat.uikit.core.state.ConversationStarterUIState for UI state management
 */
package com.cometchat.uikit.kotlin.presentation.shared.aiconversationstarter

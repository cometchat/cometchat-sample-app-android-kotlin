package com.cometchat.ai.sampleapp.compose.navigation

import kotlinx.serialization.Serializable

/** Entry screen — handles SDK init, credential checks, and login routing. */
@Serializable
object SplashRoute

/** App credentials entry screen (App ID, Region, Auth Key). */
@Serializable
object AppCredentialsRoute

/** Login screen with sample users grid + manual UID. */
@Serializable
object LoginRoute

/** Top-level screen listing AI agents (users with `agentic` role). */
@Serializable
object AIAssistantUsersRoute

/**
 * AI Assistant chat screen.
 *
 * @param userId UID of the AI agent to chat with.
 * @param parentMessageId Parent message id — non-null when opening a historical
 *                        conversation from the chat-history drawer.
 */
@Serializable
data class AIAssistantChatRoute(
    val userId: String,
    val parentMessageId: Long? = null
)

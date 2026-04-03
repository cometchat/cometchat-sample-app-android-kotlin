package com.cometchat.uikit.core.state

import android.content.Context
import android.view.View
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage

/**
 * Sealed class representing UI states for the message composer.
 * Used by the CometChatMessageComposerViewModel to communicate current state to the UI.
 * 
 * The message composer can be in various states depending on user actions:
 * - Idle: Default state, ready for input
 * - Sending: Message is being sent
 * - Editing: User is editing an existing message
 * - Replying: User is replying to a message
 * - AIGenerating: AI is generating a response
 * - Success: Message operation completed successfully
 * - Error: An error occurred during message operation
 * 
 * @see com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
 */
sealed class MessageComposerUIState {
    
    /**
     * Idle state - default state when the composer is ready for input.
     * This is the initial state and the state returned to after successful operations.
     */
    object Idle : MessageComposerUIState()
    
    /**
     * Sending state - displayed while a message is being sent.
     * The UI should show appropriate loading indicators during this state.
     */
    object Sending : MessageComposerUIState()
    
    /**
     * Editing state - displayed when the user is editing an existing message.
     * Contains the original message being edited so the UI can display
     * the edit preview panel with the original message content.
     * 
     * @param message The original BaseMessage being edited
     */
    data class Editing(val message: BaseMessage) : MessageComposerUIState()
    
    /**
     * Replying state - displayed when the user is replying to a message.
     * Contains the message being replied to so the UI can display
     * the reply preview panel with the quoted message content.
     * 
     * @param message The BaseMessage being replied to (quoted)
     */
    data class Replying(val message: BaseMessage) : MessageComposerUIState()
    
    /**
     * AI generating state - displayed when AI is generating a response.
     * The UI should show a stop button instead of the send button
     * to allow the user to interrupt the AI generation.
     */
    object AIGenerating : MessageComposerUIState()
    
    /**
     * Success state - displayed when a message operation completes successfully.
     * Contains the sent/edited message for callback purposes.
     * The UI typically transitions back to Idle after handling this state.
     * 
     * @param message The BaseMessage that was successfully sent or edited
     */
    data class Success(val message: BaseMessage) : MessageComposerUIState()
    
    /**
     * Error state - displayed when a message operation fails.
     * The UI should handle this state by showing an error message
     * and allowing the user to retry or dismiss.
     * 
     * @param exception The CometChatException that caused the error
     */
    data class Error(val exception: CometChatException) : MessageComposerUIState()
}

/**
 * Sealed class representing panel events for the message composer.
 * Used to show or hide custom panels above (top) or below (bottom) the composer.
 * 
 * Panel events allow external components to inject custom views into the composer
 * at designated positions, enabling extensibility for features like:
 * - AI suggestion panels
 * - Sticker/emoji panels
 * - Custom action panels
 * 
 * @see com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
 */
sealed class ComposerPanelEvent {
    
    /**
     * Event to show a custom view in the top panel (header area).
     * The view provider function receives a Context and returns the View to display.
     * 
     * @param viewProvider A function that creates the View to display given a Context
     */
    data class ShowTopPanel(val viewProvider: (Context) -> View) : ComposerPanelEvent()
    
    /**
     * Event to show a custom view in the bottom panel (footer area).
     * The view provider function receives a Context and returns the View to display.
     * 
     * @param viewProvider A function that creates the View to display given a Context
     */
    data class ShowBottomPanel(val viewProvider: (Context) -> View) : ComposerPanelEvent()
    
    /**
     * Event to close/hide the top panel.
     * The UI should remove any custom view from the header area.
     */
    object CloseTopPanel : ComposerPanelEvent()
    
    /**
     * Event to close/hide the bottom panel.
     * The UI should remove any custom view from the footer area.
     */
    object CloseBottomPanel : ComposerPanelEvent()
}

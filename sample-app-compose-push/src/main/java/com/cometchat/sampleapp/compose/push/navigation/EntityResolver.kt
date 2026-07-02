package com.cometchat.sampleapp.compose.push.navigation

import com.cometchat.calls.model.CallLog
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Represents the state of entity resolution for use in destination composables.
 * 
 * This sealed class provides a type-safe way to represent loading, success, and error
 * states when resolving CometChat SDK entities from navigation route parameters.
 * 
 * Example usage in a composable:
 * ```kotlin
 * var state by remember { mutableStateOf<EntityResolutionState<User>>(EntityResolutionState.Loading()) }
 * 
 * LaunchedEffect(userId) {
 *     EntityResolver.resolveUser(userId)
 *         .onSuccess { user -> state = EntityResolutionState.Success(user) }
 *         .onFailure { e -> state = EntityResolutionState.Error(e as CometChatException) }
 * }
 * 
 * when (val currentState = state) {
 *     is EntityResolutionState.Loading -> LoadingIndicator()
 *     is EntityResolutionState.Success -> UserContent(currentState.entity)
 *     is EntityResolutionState.Error -> ErrorView(currentState.exception)
 * }
 * ```
 * 
 * @param T The type of entity being resolved (e.g., User, Group, BaseMessage, CallLog)
 */
sealed class EntityResolutionState<T> {
    /**
     * Represents the loading state while the entity is being resolved.
     */
    class Loading<T> : EntityResolutionState<T>()
    
    /**
     * Represents a successful resolution with the resolved entity.
     * 
     * @property entity The successfully resolved entity
     */
    data class Success<T>(val entity: T) : EntityResolutionState<T>()
    
    /**
     * Represents an error state when entity resolution fails.
     * 
     * @property exception The exception that caused the resolution to fail
     */
    data class Error<T>(val exception: CometChatException) : EntityResolutionState<T>()
}

/**
 * Utility object for resolving CometChat SDK entities from their IDs.
 * 
 * This resolver wraps the callback-based CometChat SDK APIs in suspend functions
 * with Result return types for cleaner error handling in coroutines.
 * 
 * Used by Navigation Compose destinations to resolve entities from route parameters,
 * since complex SDK objects cannot be passed directly as navigation arguments.
 * 
 * Example usage:
 * ```kotlin
 * val userResult = EntityResolver.resolveUser(userId)
 * userResult.onSuccess { user ->
 *     // Use the resolved user
 * }.onFailure { exception ->
 *     // Handle the error
 * }
 * ```
 */
object EntityResolver {

    /**
     * Resolves a User from the CometChat SDK by user ID.
     *
     * @param userId The unique identifier of the user to resolve
     * @return Result containing the User on success, or CometChatException on failure
     */
    suspend fun resolveUser(userId: String): Result<User> = suspendCancellableCoroutine { continuation ->
        CometChat.getUser(userId, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User) {
                continuation.resume(Result.success(user))
            }

            override fun onError(e: CometChatException) {
                continuation.resume(Result.failure(e))
            }
        })
    }

    /**
     * Resolves a Group from the CometChat SDK by group ID.
     *
     * @param groupId The unique identifier (GUID) of the group to resolve
     * @return Result containing the Group on success, or CometChatException on failure
     */
    suspend fun resolveGroup(groupId: String): Result<Group> = suspendCancellableCoroutine { continuation ->
        CometChat.getGroup(groupId, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(group: Group) {
                continuation.resume(Result.success(group))
            }

            override fun onError(e: CometChatException) {
                continuation.resume(Result.failure(e))
            }
        })
    }

    /**
     * Resolves a BaseMessage from the CometChat SDK by message ID.
     *
     * @param messageId The unique identifier of the message to resolve
     * @return Result containing the BaseMessage on success, or CometChatException on failure
     */
    suspend fun resolveMessage(messageId: Long): Result<BaseMessage> = suspendCancellableCoroutine { continuation ->
        CometChat.getMessageDetails(messageId, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(message: BaseMessage) {
                continuation.resume(Result.success(message))
            }

            override fun onError(e: CometChatException) {
                continuation.resume(Result.failure(e))
            }
        })
    }

    /**
     * Resolves a CallLog from the EntityCache by session ID.
     * 
     * Note: The CometChat SDK does not support fetching CallLog by session ID directly.
     * CallLogs must be stored in the EntityCache before navigation and retrieved here.
     * 
     * @param sessionId The session ID of the call log to resolve
     * @return Result containing the CallLog on success, or IllegalStateException if not found in cache
     */
    suspend fun resolveCallLog(sessionId: String): Result<CallLog> {
        val callLog = EntityCache.getCallLog(sessionId)
        return if (callLog != null) {
            Result.success(callLog)
        } else {
            Result.failure(
                IllegalStateException("CallLog with session ID '$sessionId' not found in cache. " +
                    "Ensure the CallLog is stored in EntityCache before navigating.")
            )
        }
    }
}

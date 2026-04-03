package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementation of MessageHeaderDataSource that communicates with CometChat SDK.
 * Contains NO business logic - just raw data fetching.
 * This is the default implementation used for remote data operations.
 */
class MessageHeaderDataSourceImpl : MessageHeaderDataSource {
    
    /**
     * Fetches a user by their UID from CometChat SDK.
     * @param uid The unique identifier of the user
     * @return The User object from SDK
     * @throws com.cometchat.chat.exceptions.CometChatException if SDK call fails
     */
    override suspend fun getUser(uid: String): User = suspendCancellableCoroutine { continuation ->
        CometChat.getUser(uid, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User) {
                continuation.resume(user)
            }
            
            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }
    
    /**
     * Fetches a group by their GUID from CometChat SDK.
     * @param guid The unique identifier of the group
     * @return The Group object from SDK
     * @throws com.cometchat.chat.exceptions.CometChatException if SDK call fails
     */
    override suspend fun getGroup(guid: String): Group = suspendCancellableCoroutine { continuation ->
        CometChat.getGroup(guid, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(group: Group) {
                continuation.resume(group)
            }
            
            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }
}

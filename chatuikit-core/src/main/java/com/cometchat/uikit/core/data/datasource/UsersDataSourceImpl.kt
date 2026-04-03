package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementation of UsersDataSource that communicates with CometChat SDK.
 * Contains NO business logic - just raw data fetching.
 * This is the default implementation used for remote data operations.
 */
class UsersDataSourceImpl : UsersDataSource {
    
    /**
     * Fetches users from CometChat SDK.
     * @param request The configured UsersRequest
     * @return Raw list of User objects from SDK
     * @throws com.cometchat.chat.exceptions.CometChatException if SDK call fails
     */
    override suspend fun fetchUsers(request: UsersRequest): List<User> = 
        suspendCancellableCoroutine { continuation ->
            request.fetchNext(object : CometChat.CallbackListener<List<User>>() {
                override fun onSuccess(users: List<User>) {
                    continuation.resume(users)
                }
                
                override fun onError(exception: CometChatException) {
                    continuation.resumeWithException(exception)
                }
            })
        }
}

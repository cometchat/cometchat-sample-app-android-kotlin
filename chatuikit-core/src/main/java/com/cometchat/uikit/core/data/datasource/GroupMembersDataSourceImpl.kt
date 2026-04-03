package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.GroupMembersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.GroupMember
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementation of GroupMembersDataSource that communicates with CometChat SDK.
 * Contains NO business logic - just raw data fetching/saving.
 * This is the default implementation used for remote data operations.
 * 
 * @param groupMembersRequestBuilder Builder for creating group members requests
 */
class GroupMembersDataSourceImpl(
    private var groupMembersRequestBuilder: GroupMembersRequest.GroupMembersRequestBuilder
) : GroupMembersDataSource {
    
    private var groupMembersRequest: GroupMembersRequest? = null
    private var hasMore = true
    
    /**
     * Fetches group members from CometChat SDK.
     * Rebuilds the request when it's null (after resetRequest or first call).
     * When searchKeyword is non-null, builds a fresh request with the keyword.
     * When searchKeyword is null, builds from a fresh builder to avoid stale keywords.
     * @param guid The group ID
     * @param limit Number of members to fetch per page
     * @param searchKeyword Optional search keyword to filter members
     * @return Raw list of GroupMember objects from SDK
     * @throws com.cometchat.chat.exceptions.CometChatException if SDK call fails
     */
    override suspend fun fetchGroupMembers(
        guid: String,
        limit: Int,
        searchKeyword: String?
    ): List<GroupMember> = suspendCancellableCoroutine { continuation ->
        // Build request if not yet built (first call or after resetRequest)
        if (groupMembersRequest == null) {
            groupMembersRequest = if (!searchKeyword.isNullOrEmpty()) {
                // Search mode: fresh builder with keyword to avoid stale state
                GroupMembersRequest.GroupMembersRequestBuilder(guid)
                    .setLimit(limit)
                    .setSearchKeyword(searchKeyword)
                    .build()
            } else {
                // Normal mode: fresh builder without keyword
                GroupMembersRequest.GroupMembersRequestBuilder(guid)
                    .setLimit(limit)
                    .build()
            }
        }
        
        groupMembersRequest?.fetchNext(object : CometChat.CallbackListener<List<GroupMember>>() {
            override fun onSuccess(members: List<GroupMember>) {
                hasMore = members.isNotEmpty()
                continuation.resume(members)
            }
            
            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }
    
    /**
     * Kicks a member from the group via CometChat SDK.
     * @param guid The group ID
     * @param uid The user ID to kick
     * @return Success message from SDK
     * @throws com.cometchat.chat.exceptions.CometChatException if SDK call fails
     */
    override suspend fun kickGroupMember(
        guid: String,
        uid: String
    ): String = suspendCancellableCoroutine { continuation ->
        CometChat.kickGroupMember(
            uid,
            guid,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String) {
                    continuation.resume(result)
                }
                
                override fun onError(exception: CometChatException) {
                    continuation.resumeWithException(exception)
                }
            }
        )
    }
    
    /**
     * Bans a member from the group via CometChat SDK.
     * @param guid The group ID
     * @param uid The user ID to ban
     * @return Success message from SDK
     * @throws com.cometchat.chat.exceptions.CometChatException if SDK call fails
     */
    override suspend fun banGroupMember(
        guid: String,
        uid: String
    ): String = suspendCancellableCoroutine { continuation ->
        CometChat.banGroupMember(
            uid,
            guid,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String) {
                    continuation.resume(result)
                }
                
                override fun onError(exception: CometChatException) {
                    continuation.resumeWithException(exception)
                }
            }
        )
    }
    
    /**
     * Changes a member's scope in the group via CometChat SDK.
     * @param guid The group ID
     * @param uid The user ID
     * @param scope The new scope (admin/moderator/participant)
     * @return Success message from SDK
     * @throws com.cometchat.chat.exceptions.CometChatException if SDK call fails
     */
    override suspend fun changeMemberScope(
        guid: String,
        uid: String,
        scope: String
    ): String = suspendCancellableCoroutine { continuation ->
        CometChat.updateGroupMemberScope(
            uid,
            guid,
            scope,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String) {
                    continuation.resume(result)
                }
                
                override fun onError(exception: CometChatException) {
                    continuation.resumeWithException(exception)
                }
            }
        )
    }
    
    /**
     * Checks if there are more members available for pagination.
     * @return true if more members can be fetched
     */
    override fun hasMoreMembers(): Boolean = hasMore
    
    /**
     * Resets the current request so the next fetch builds a fresh one.
     * Call this when the search keyword changes or when refreshing.
     */
    override fun resetRequest() {
        this.groupMembersRequest = null
        this.hasMore = true
    }
    
    /**
     * Resets the request builder for a new search or refresh.
     * @param builder New request builder
     */
    fun resetRequestBuilder(builder: GroupMembersRequest.GroupMembersRequestBuilder) {
        this.groupMembersRequestBuilder = builder
        this.groupMembersRequest = null
        this.hasMore = true
    }
}

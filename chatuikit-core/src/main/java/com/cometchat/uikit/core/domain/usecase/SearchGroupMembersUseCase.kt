package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.GroupMembersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.GroupMember
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Use case for searching group members by keyword.
 * Builds a new [GroupMembersRequest] from the provided builder with the search keyword,
 * then fetches the first page of results directly from the CometChat SDK.
 */
open class SearchGroupMembersUseCase {

    /**
     * Searches group members using the given keyword and request builder.
     *
     * @param keyword The search keyword
     * @param builder The request builder to use (search builder or fallback)
     * @return Result containing the list of matching group members or an error
     */
    open suspend operator fun invoke(
        keyword: String,
        builder: GroupMembersRequest.GroupMembersRequestBuilder
    ): Result<List<GroupMember>> {
        return try {
            val request = builder.setSearchKeyword(keyword).build()
            val members = fetchMembers(request)
            Result.success(members)
        } catch (e: CometChatException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchMembers(
        request: GroupMembersRequest
    ): List<GroupMember> = suspendCancellableCoroutine { continuation ->
        request.fetchNext(object : CometChat.CallbackListener<List<GroupMember>>() {
            override fun onSuccess(members: List<GroupMember>) {
                continuation.resume(members)
            }

            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }
}

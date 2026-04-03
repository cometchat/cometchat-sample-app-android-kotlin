package com.cometchat.uikit.core.data.datasource

import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.calls.exceptions.CometChatException
import com.cometchat.calls.model.CallLog
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Implementation of [CallLogsDataSource] that uses the CometChatCalls SDK.
 * Converts callback-based SDK calls to suspend functions using coroutines.
 */
class CallLogsDataSourceImpl : CallLogsDataSource {
    
    /**
     * Fetches call logs from the CometChatCalls SDK.
     * Uses suspendCoroutine to convert the callback-based API to a suspend function.
     * 
     * @param request The CallLogRequest configured with pagination and filters
     * @return Result containing list of CallLog on success, or exception on failure
     */
    override suspend fun fetchCallLogs(request: CallLogRequest): Result<List<CallLog>> {
        return suspendCoroutine { continuation ->
            request.fetchNext(object : CometChatCalls.CallbackListener<List<CallLog>>() {
                override fun onSuccess(callLogs: List<CallLog>) {
                    continuation.resume(Result.success(callLogs))
                }
                
                override fun onError(exception: CometChatException) {
                    continuation.resume(Result.failure(exception))
                }
            })
        }
    }
}

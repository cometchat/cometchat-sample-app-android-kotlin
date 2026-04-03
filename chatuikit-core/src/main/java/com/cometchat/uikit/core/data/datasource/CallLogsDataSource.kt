package com.cometchat.uikit.core.data.datasource

import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.model.CallLog

/**
 * Data source interface for call logs operations.
 * Defines the contract for CometChatCalls SDK interactions.
 */
interface CallLogsDataSource {
    /**
     * Fetches call logs from the CometChatCalls SDK.
     * 
     * @param request The CallLogRequest configured with pagination and filters
     * @return Result containing list of CallLog on success, or exception on failure
     */
    suspend fun fetchCallLogs(request: CallLogRequest): Result<List<CallLog>>
}

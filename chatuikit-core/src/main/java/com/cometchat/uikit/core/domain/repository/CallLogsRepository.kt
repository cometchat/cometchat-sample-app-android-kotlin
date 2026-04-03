package com.cometchat.uikit.core.domain.repository

import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.model.CallLog

/**
 * Repository interface for call logs operations.
 * Abstracts the data layer from the ViewModel.
 */
interface CallLogsRepository {
    /**
     * Fetches call logs using the provided request.
     * 
     * @param request The CallLogRequest configured with pagination and filters
     * @return Result containing list of CallLog on success, or exception on failure
     */
    suspend fun getCallLogs(request: CallLogRequest): Result<List<CallLog>>
    
    /**
     * Checks if there are more call logs available for pagination.
     * 
     * @return true if more call logs can be fetched, false otherwise
     */
    fun hasMoreCallLogs(): Boolean
}

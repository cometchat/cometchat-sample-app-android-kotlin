package com.cometchat.uikit.core.data.repository

import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.core.data.datasource.CallLogsDataSource
import com.cometchat.uikit.core.domain.repository.CallLogsRepository

/**
 * Implementation of [CallLogsRepository] that uses [CallLogsDataSource].
 * Tracks pagination state based on fetch results.
 * 
 * @param dataSource The data source for fetching call logs from SDK
 */
class CallLogsRepositoryImpl(
    private val dataSource: CallLogsDataSource
) : CallLogsRepository {
    
    private var hasMore = true
    
    /**
     * Fetches call logs using the provided request.
     * Updates the hasMore flag based on whether results were returned.
     * 
     * @param request The CallLogRequest configured with pagination and filters
     * @return Result containing list of CallLog on success, or exception on failure
     */
    override suspend fun getCallLogs(request: CallLogRequest): Result<List<CallLog>> {
        return dataSource.fetchCallLogs(request).also { result ->
            result.onSuccess { callLogs ->
                hasMore = callLogs.isNotEmpty()
            }
        }
    }
    
    /**
     * Checks if there are more call logs available for pagination.
     * 
     * @return true if more call logs can be fetched, false otherwise
     */
    override fun hasMoreCallLogs(): Boolean = hasMore
}

package com.cometchat.uikit.core.domain.usecase

import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.core.domain.repository.CallLogsRepository

/**
 * Use case for fetching call logs with pagination support.
 * Encapsulates the business logic for fetching call logs from the repository.
 * 
 * @param repository The repository for call logs operations
 */
open class FetchCallLogsUseCase(
    private val repository: CallLogsRepository
) {
    /**
     * Fetches call logs using the provided request.
     * 
     * @param request The CallLogRequest configured with pagination and filters
     * @return Result containing list of CallLog on success, or exception on failure
     */
    open suspend operator fun invoke(request: CallLogRequest): Result<List<CallLog>> {
        return repository.getCallLogs(request)
    }
    
    /**
     * Checks if there are more call logs available for pagination.
     * 
     * @return true if more call logs can be fetched, false otherwise
     */
    open fun hasMore(): Boolean = repository.hasMoreCallLogs()
}

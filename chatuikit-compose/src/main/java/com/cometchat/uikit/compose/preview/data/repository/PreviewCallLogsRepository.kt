package com.cometchat.uikit.compose.preview.data.repository

import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.core.domain.repository.CallLogsRepository

/**
 * A mock repository implementation for Compose Previews of CometChatCallLogs.
 * Returns pre-populated mock data without making any SDK calls.
 *
 * Note: CallLog objects from the Calls SDK cannot be easily constructed with mock data
 * since they lack public setters. This repository returns an empty list by default,
 * which triggers the empty state — still better than a crash from SDK initialization.
 * Set simulateError = true to test error state.
 *
 * @param simulateError If true, all operations will return failure results.
 * @param simulateEmpty If true, returns an empty list (default behavior).
 */
class PreviewCallLogsRepository(
    private val simulateError: Boolean = false,
    private val simulateEmpty: Boolean = true
) : CallLogsRepository {

    override suspend fun getCallLogs(request: CallLogRequest): Result<List<CallLog>> {
        if (simulateError) {
            return Result.failure(
                com.cometchat.calls.exceptions.CometChatException(
                    "PREVIEW_ERROR",
                    "Simulated error for preview"
                )
            )
        }
        return Result.success(emptyList())
    }

    override fun hasMoreCallLogs(): Boolean = false
}

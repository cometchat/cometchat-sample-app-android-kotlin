package com.cometchat.uikit.core.data.datasource

import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.calls.exceptions.CometChatException
import com.cometchat.calls.model.CallLog
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for CallLogsDataSourceImpl.
 *
 * This is the lowest layer — it bridges CometChatCalls SDK callbacks to coroutines.
 * We mock the CallLogRequest object and simulate SDK callback behavior.
 *
 * The DataSource returns Result<List<CallLog>> — wrapping success/failure in Result.
 *
 * Note: CallLogRequest has a static initializer that calls Looper.getMainLooper().
 * The build.gradle testOptions has isReturnDefaultValues = true so Android framework
 * methods return defaults (null/0/false) instead of throwing.
 *
 * Reference: ConversationListDataSourceImplTest.kt
 *
 * Validates: Requirements 1.1, 1.2, 1.3, 1.5, 1.7
 */
class CallLogsDataSourceImplTest : FunSpec({

    lateinit var dataSource: CallLogsDataSourceImpl

    beforeTest {
        dataSource = CallLogsDataSourceImpl()
    }

    // ==================== fetchCallLogs ====================

    test("fetchCallLogs should return Result.success with call logs when SDK callback succeeds") {
        runTest {
            val mockRequest = mock<CallLogRequest>()
            val expectedCallLogs = listOf(mock<CallLog>(), mock<CallLog>(), mock<CallLog>())

            println("  → Testing fetchCallLogs success with ${expectedCallLogs.size} call logs")

            whenever(mockRequest.fetchNext(any())).thenAnswer { invocation ->
                @Suppress("UNCHECKED_CAST")
                val callback = invocation.getArgument<CometChatCalls.CallbackListener<List<CallLog>>>(0)
                callback.onSuccess(expectedCallLogs)
            }

            val result = dataSource.fetchCallLogs(mockRequest)

            result.isSuccess shouldBe true
            result.getOrNull() shouldBe expectedCallLogs
            result.getOrNull()?.size shouldBe 3
            println("  ✅ Returned Result.success with ${result.getOrNull()?.size} call logs")
        }
    }

    test("fetchCallLogs should return Result.failure with CometChatException when SDK callback fails") {
        runTest {
            val mockRequest = mock<CallLogRequest>()
            val expectedException = CometChatException("ERR_FETCH", "Network error")

            println("  → Testing fetchCallLogs failure with code=${expectedException.code}")

            whenever(mockRequest.fetchNext(any())).thenAnswer { invocation ->
                @Suppress("UNCHECKED_CAST")
                val callback = invocation.getArgument<CometChatCalls.CallbackListener<List<CallLog>>>(0)
                callback.onError(expectedException)
            }

            val result = dataSource.fetchCallLogs(mockRequest)

            result.isFailure shouldBe true
            val exception = result.exceptionOrNull() as CometChatException
            exception.code shouldBe "ERR_FETCH"
            exception.message shouldBe "Network error"
            println("  ✅ Returned Result.failure with code=${exception.code}, message=${exception.message}")
        }
    }

    test("fetchCallLogs should return Result.success with empty list when SDK returns empty") {
        runTest {
            val mockRequest = mock<CallLogRequest>()

            println("  → Testing fetchCallLogs with empty result")

            whenever(mockRequest.fetchNext(any())).thenAnswer { invocation ->
                @Suppress("UNCHECKED_CAST")
                val callback = invocation.getArgument<CometChatCalls.CallbackListener<List<CallLog>>>(0)
                callback.onSuccess(emptyList())
            }

            val result = dataSource.fetchCallLogs(mockRequest)

            result.isSuccess shouldBe true
            result.getOrNull() shouldBe emptyList()
            println("  ✅ Returned Result.success with empty list")
        }
    }
})

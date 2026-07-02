package com.cometchat.uikit.core.domain.usecase

import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.core.domain.repository.CallLogsRepository
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for FetchCallLogsUseCase.
 *
 * Use cases are thin delegation layers. We mock the Repository interface
 * and verify parameters are forwarded and results are returned unchanged.
 *
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5
 *
 * Reference: ConversationUseCasesTest.kt
 */
class CallLogsUseCasesTest : FunSpec({

    // ==================== FetchCallLogsUseCase ====================

    context("FetchCallLogsUseCase") {

        lateinit var repository: CallLogsRepository
        lateinit var useCase: FetchCallLogsUseCase

        beforeTest {
            repository = mock()
            useCase = FetchCallLogsUseCase(repository)
        }

        test("invoke should delegate to repository.getCallLogs with same request") {
            runTest {
                val request = mock<CallLogRequest>()
                val callLogs = listOf(mock<CallLog>(), mock<CallLog>(), mock<CallLog>())
                whenever(repository.getCallLogs(request)).thenReturn(Result.success(callLogs))

                println("  → Testing FetchCallLogsUseCase delegation")

                val result = useCase(request)

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe callLogs
                result.getOrNull()?.size shouldBe 3
                verify(repository).getCallLogs(request)
                println("  ✅ Delegated correctly, returned ${result.getOrNull()?.size} call logs")
            }
        }

        test("invoke should propagate Result.failure unchanged") {
            runTest {
                val request = mock<CallLogRequest>()
                val exception = MockFactory.createCometChatException("ERR_FETCH", "Failed to fetch call logs")
                whenever(repository.getCallLogs(request)).thenReturn(Result.failure(exception))

                println("  → Testing FetchCallLogsUseCase failure propagation")

                val result = useCase(request)

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ Failure propagated unchanged")
            }
        }

        test("hasMore should delegate to repository.hasMoreCallLogs") {
            println("  → Testing hasMore delegation")

            whenever(repository.hasMoreCallLogs()).thenReturn(true)
            useCase.hasMore() shouldBe true

            whenever(repository.hasMoreCallLogs()).thenReturn(false)
            useCase.hasMore() shouldBe false

            println("  ✅ hasMore delegates correctly")
        }
    }
})

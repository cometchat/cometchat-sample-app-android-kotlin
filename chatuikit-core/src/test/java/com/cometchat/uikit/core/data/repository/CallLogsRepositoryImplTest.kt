package com.cometchat.uikit.core.data.repository

import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.core.data.datasource.CallLogsDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.objenesis.ObjenesisStd

/**
 * Tests for CallLogsRepositoryImpl.
 *
 * This layer delegates to CallLogsDataSource (which returns Result<List<CallLog>>)
 * and manages pagination state via hasMore.
 * We mock only the CallLogsDataSource interface — no SDK dependencies.
 *
 * Note: CallLogRequest and CallLog are Calls SDK classes with static initializers
 * that depend on Android (android.os.Handler). We use Objenesis (already a Mockito
 * transitive dependency) to allocate instances without triggering class initialization.
 *
 * Reference: ConversationListRepositoryImplTest.kt
 *
 * Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6
 */
class CallLogsRepositoryImplTest : FunSpec({

    val objenesis = ObjenesisStd()

    lateinit var dataSource: CallLogsDataSource
    lateinit var repository: CallLogsRepositoryImpl

    /**
     * Allocates a CallLogRequest instance using Objenesis, bypassing the static
     * initializer and constructor. This avoids the android.os.Handler dependency.
     */
    fun unsafeCallLogRequest(): CallLogRequest {
        return objenesis.newInstance(CallLogRequest::class.java)
    }

    /**
     * Creates a non-empty list typed as List<CallLog> using Objenesis allocation.
     * The Repository only checks isEmpty() — it never inspects individual CallLog fields.
     */
    fun createCallLogs(count: Int): List<CallLog> {
        return (1..count).map {
            objenesis.newInstance(CallLog::class.java)
        }
    }

    beforeTest {
        dataSource = mock()
        repository = CallLogsRepositoryImpl(dataSource)
    }

    // ==================== getCallLogs — Result.success ====================

    test("getCallLogs should delegate to dataSource and return Result.success with call logs") {
        runTest {
            val callLogs = createCallLogs(3)
            val request = unsafeCallLogRequest()
            whenever(dataSource.fetchCallLogs(anyOrNull())).thenReturn(Result.success(callLogs))

            println("  → Testing getCallLogs delegation with ${callLogs.size} call logs")

            val result = repository.getCallLogs(request)

            result.isSuccess shouldBe true
            result.getOrNull() shouldBe callLogs
            result.getOrNull()?.size shouldBe 3
            verify(dataSource).fetchCallLogs(anyOrNull())
            println("  ✅ Result.success with ${result.getOrNull()?.size} call logs")
        }
    }

    // ==================== getCallLogs — Result.failure ====================

    test("getCallLogs should return Result.failure when dataSource returns failure") {
        runTest {
            val request = unsafeCallLogRequest()
            val exception = RuntimeException("Network error")
            whenever(dataSource.fetchCallLogs(anyOrNull())).thenReturn(Result.failure(exception))

            println("  → Testing getCallLogs failure wrapping")

            val result = repository.getCallLogs(request)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("  ✅ Result.failure with message=${result.exceptionOrNull()?.message}")
        }
    }

    test("getCallLogs should propagate generic Exception in Result.failure") {
        runTest {
            val request = unsafeCallLogRequest()
            val exception = IllegalStateException("Unexpected error")
            whenever(dataSource.fetchCallLogs(anyOrNull())).thenReturn(Result.failure(exception))

            println("  → Testing getCallLogs generic exception propagation")

            val result = repository.getCallLogs(request)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("  ✅ Result.failure with IllegalStateException")
        }
    }

    // ==================== Pagination State ====================

    test("hasMoreCallLogs should be true initially") {
        println("  → Testing initial pagination state")
        repository.hasMoreCallLogs() shouldBe true
        println("  ✅ hasMoreCallLogs = true initially")
    }

    test("hasMoreCallLogs should be true when dataSource returns non-empty list") {
        runTest {
            val callLogs = createCallLogs(5)
            val request = unsafeCallLogRequest()
            whenever(dataSource.fetchCallLogs(anyOrNull())).thenReturn(Result.success(callLogs))

            println("  → Testing pagination after non-empty fetch")

            repository.getCallLogs(request)

            repository.hasMoreCallLogs() shouldBe true
            println("  ✅ hasMoreCallLogs = true after ${callLogs.size} results")
        }
    }

    test("hasMoreCallLogs should be false when dataSource returns empty list") {
        runTest {
            val request = unsafeCallLogRequest()
            whenever(dataSource.fetchCallLogs(anyOrNull())).thenReturn(Result.success(emptyList()))

            println("  → Testing pagination after empty fetch")

            repository.getCallLogs(request)

            repository.hasMoreCallLogs() shouldBe false
            println("  ✅ hasMoreCallLogs = false after empty result")
        }
    }

    test("hasMoreCallLogs should remain true when dataSource returns failure") {
        runTest {
            val request = unsafeCallLogRequest()
            val exception = RuntimeException("Network error")
            whenever(dataSource.fetchCallLogs(anyOrNull())).thenReturn(Result.failure(exception))

            println("  → Testing pagination state after failure")

            repository.getCallLogs(request)

            // Failure path doesn't update hasMore — it stays true
            repository.hasMoreCallLogs() shouldBe true
            println("  ✅ hasMoreCallLogs = true (failure doesn't change state)")
        }
    }

    test("pagination state should track across multiple fetches") {
        runTest {
            val ds = mock<CallLogsDataSource>()
            val repo = CallLogsRepositoryImpl(ds)
            val request = unsafeCallLogRequest()
            val nonEmptyCallLogs = createCallLogs(10)

            println("  → Testing pagination state across multiple fetches")

            // First fetch: non-empty → hasMore = true
            whenever(ds.fetchCallLogs(anyOrNull())).thenReturn(Result.success(nonEmptyCallLogs))
            repo.getCallLogs(request)
            repo.hasMoreCallLogs() shouldBe true
            println("    Fetch 1: 10 results → hasMore=true")

            // Second fetch: empty → hasMore = false (end of list)
            whenever(ds.fetchCallLogs(anyOrNull())).thenReturn(Result.success(emptyList()))
            repo.getCallLogs(request)
            repo.hasMoreCallLogs() shouldBe false
            println("    Fetch 2: 0 results → hasMore=false")

            println("  ✅ Pagination state tracked correctly")
        }
    }

    test("pagination state should not be corrupted by interleaved failures") {
        runTest {
            val ds = mock<CallLogsDataSource>()
            val repo = CallLogsRepositoryImpl(ds)
            val request = unsafeCallLogRequest()
            val callLogs = createCallLogs(3)

            println("  → Testing pagination state with interleaved failures")

            // First fetch: success with items → hasMore = true
            whenever(ds.fetchCallLogs(anyOrNull())).thenReturn(Result.success(callLogs))
            repo.getCallLogs(request)
            repo.hasMoreCallLogs() shouldBe true
            println("    Fetch 1: success with 3 items → hasMore=true")

            // Second fetch: failure → hasMore stays true (not corrupted)
            whenever(ds.fetchCallLogs(anyOrNull())).thenReturn(Result.failure(RuntimeException("Error")))
            repo.getCallLogs(request)
            repo.hasMoreCallLogs() shouldBe true
            println("    Fetch 2: failure → hasMore=true (unchanged)")

            // Third fetch: empty → hasMore = false
            whenever(ds.fetchCallLogs(anyOrNull())).thenReturn(Result.success(emptyList()))
            repo.getCallLogs(request)
            repo.hasMoreCallLogs() shouldBe false
            println("    Fetch 3: empty → hasMore=false")

            println("  ✅ Pagination state not corrupted by failures")
        }
    }
})

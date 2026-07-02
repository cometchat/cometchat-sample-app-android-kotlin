package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.data.datasource.PollDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.json.JSONArray
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for PollRepositoryImpl.
 * Delegates to DataSource — mocks the DataSource interface.
 *
 * Verifies:
 * - Delegation to dataSource with correct parameters
 * - Result.success propagation
 * - Result.failure propagation
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.PollRepositoryImplTest"
 */
class PollRepositoryImplTest : FunSpec({

    lateinit var dataSource: PollDataSource
    lateinit var repository: PollRepositoryImpl

    beforeTest {
        dataSource = mock()
        repository = PollRepositoryImpl(dataSource)
        println("  🧪 ${it.name.testName}")
    }

    test("createPoll delegates to dataSource with correct parameters") {
        runTest {
            println("    → Invoking repository.createPoll")
            val options = JSONArray().apply {
                put("Option A")
                put("Option B")
            }
            whenever(dataSource.createPoll(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.success(Unit))

            repository.createPoll(
                question = "Best language?",
                options = options,
                receiverId = "group-123",
                receiverType = "group",
                quotedMessageId = 42L
            )

            verify(dataSource).createPoll(
                question = eq("Best language?"),
                options = any(),
                receiverId = eq("group-123"),
                receiverType = eq("group"),
                quotedMessageId = eq(42L)
            )
            println("    ✓ dataSource.createPoll called with correct parameters")
        }
    }

    test("createPoll propagates Result.success from dataSource") {
        runTest {
            println("    → Mocking dataSource success")
            whenever(dataSource.createPoll(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.success(Unit))

            val result = repository.createPoll(
                question = "Q?",
                options = JSONArray(),
                receiverId = "user-1",
                receiverType = "user"
            )

            result.isSuccess shouldBe true
            println("    ✓ Result.success propagated from dataSource")
        }
    }

    test("createPoll propagates Result.failure from dataSource") {
        runTest {
            println("    → Mocking dataSource failure")
            val exception = CometChatException("ERR_POLL", "Network error")
            whenever(dataSource.createPoll(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.failure(exception))

            val result = repository.createPoll(
                question = "Q?",
                options = JSONArray(),
                receiverId = "user-1",
                receiverType = "user"
            )

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("    ✓ Result.failure propagated from dataSource")
        }
    }

    test("createPoll passes null quotedMessageId when not provided") {
        runTest {
            println("    → Invoking without quotedMessageId")
            whenever(dataSource.createPoll(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.success(Unit))

            repository.createPoll(
                question = "Q?",
                options = JSONArray(),
                receiverId = "user-1",
                receiverType = "user",
                quotedMessageId = null
            )

            verify(dataSource).createPoll(
                question = any(),
                options = any(),
                receiverId = any(),
                receiverType = any(),
                quotedMessageId = eq(null)
            )
            println("    ✓ null quotedMessageId forwarded to dataSource")
        }
    }

    test("createPoll with CometChatException wraps in Result.failure") {
        runTest {
            println("    → Verifying CometChatException handling")
            val exception = CometChatException("ERR_EXTENSION", "Extension not found", "polls")
            whenever(dataSource.createPoll(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.failure(exception))

            val result = repository.createPoll(
                question = "Q?",
                options = JSONArray(),
                receiverId = "group-1",
                receiverType = "group"
            )

            result.isFailure shouldBe true
            val error = result.exceptionOrNull() as CometChatException
            error.code shouldBe "ERR_EXTENSION"
            println("    ✓ CometChatException preserved in Result.failure")
        }
    }
})

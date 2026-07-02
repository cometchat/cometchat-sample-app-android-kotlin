package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.repository.PollRepository
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
 * Tests for CreatePollUseCase.
 * Use case is a thin delegation layer — mock Repository, verify forwarding.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.CreatePollUseCaseTest"
 */
class CreatePollUseCaseTest : FunSpec({

    lateinit var repository: PollRepository
    lateinit var useCase: CreatePollUseCase

    beforeTest {
        repository = mock()
        useCase = CreatePollUseCase(repository)
        println("  🧪 ${it.name.testName}")
    }

    test("invoke delegates to repository with correct parameters") {
        runTest {
            println("    → Invoking use case with specific parameters")
            val options = JSONArray().apply {
                put("Option A")
                put("Option B")
            }
            whenever(repository.createPoll(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.success(Unit))

            useCase(
                question = "Best language?",
                options = options,
                receiverId = "group-123",
                receiverType = "group",
                quotedMessageId = 42L
            )

            verify(repository).createPoll(
                question = eq("Best language?"),
                options = any(),
                receiverId = eq("group-123"),
                receiverType = eq("group"),
                quotedMessageId = eq(42L)
            )
            println("    ✓ repository.createPoll called with correct parameters")
        }
    }

    test("invoke propagates Result.success from repository") {
        runTest {
            println("    → Mocking repository success")
            whenever(repository.createPoll(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.success(Unit))

            val result = useCase(
                question = "Q?",
                options = JSONArray(),
                receiverId = "user-1",
                receiverType = "user"
            )

            result.isSuccess shouldBe true
            println("    ✓ Result.success propagated")
        }
    }

    test("invoke propagates Result.failure from repository") {
        runTest {
            println("    → Mocking repository failure")
            val exception = CometChatException("ERR_POLL", "Network error")
            whenever(repository.createPoll(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.failure(exception))

            val result = useCase(
                question = "Q?",
                options = JSONArray(),
                receiverId = "user-1",
                receiverType = "user"
            )

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("    ✓ Result.failure propagated with correct exception")
        }
    }

    test("invoke passes null quotedMessageId when not provided") {
        runTest {
            println("    → Invoking without quotedMessageId")
            whenever(repository.createPoll(any(), any(), any(), any(), anyOrNull()))
                .thenReturn(Result.success(Unit))

            useCase(
                question = "Q?",
                options = JSONArray(),
                receiverId = "user-1",
                receiverType = "user"
            )

            verify(repository).createPoll(
                question = any(),
                options = any(),
                receiverId = any(),
                receiverType = any(),
                quotedMessageId = eq(null)
            )
            println("    ✓ null quotedMessageId forwarded correctly")
        }
    }
})

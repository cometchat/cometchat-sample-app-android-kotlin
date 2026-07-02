package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.core.domain.repository.CallButtonsRepository
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for InitiateUserCallUseCase and StartGroupCallUseCase.
 *
 * Use cases check for active calls before delegating to the repository.
 * If an active call exists, they return Result.failure with "ACTIVE_CALL" error code.
 * Otherwise, they delegate to the repository.
 *
 * We mock the CallButtonsRepository interface — no SDK dependencies.
 *
 * Reference: CallLogsUseCasesTest.kt
 *
 * Validates: Requirements 3.1, 3.2
 *
 * Run with:
 * ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CallButtonsUseCasesTest"
 */
class CallButtonsUseCasesTest : FunSpec({

    // ==================== InitiateUserCallUseCase ====================

    context("InitiateUserCallUseCase") {

        lateinit var repository: CallButtonsRepository
        lateinit var useCase: InitiateUserCallUseCase

        beforeTest {
            repository = mock()
            useCase = InitiateUserCallUseCase(repository)
        }

        test("invoke should delegate to repository when no active call exists") {
            runTest {
                val mockCall = mock<Call>()
                whenever(repository.hasActiveCall()).thenReturn(false)
                whenever(repository.initiateUserCall("user-1", "audio")).thenReturn(Result.success(mockCall))

                println("  → Testing InitiateUserCallUseCase delegation (no active call)")

                val result = useCase("user-1", "audio")

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe mockCall
                verify(repository).initiateUserCall("user-1", "audio")
                println("  ✅ Delegated to repository, returned Result.success with Call")
            }
        }

        test("invoke should return Result.failure with ACTIVE_CALL when active call exists") {
            runTest {
                whenever(repository.hasActiveCall()).thenReturn(true)

                println("  → Testing InitiateUserCallUseCase with active call blocking")

                val result = useCase("user-1", "video")

                result.isFailure shouldBe true
                val exception = result.exceptionOrNull() as CometChatException
                exception.code shouldBe "ACTIVE_CALL"
                println("  ✅ Returned Result.failure with code=ACTIVE_CALL")
            }
        }

        test("invoke should propagate Result.failure from repository unchanged") {
            runTest {
                val exception = MockFactory.createCometChatException("ERR_CALL", "Call initiation failed")
                whenever(repository.hasActiveCall()).thenReturn(false)
                whenever(repository.initiateUserCall("user-1", "audio")).thenReturn(Result.failure(exception))

                println("  → Testing InitiateUserCallUseCase failure propagation from repository")

                val result = useCase("user-1", "audio")

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ Failure propagated unchanged with code=${exception.code}")
            }
        }

        test("invoke should pass correct receiverId and callType to repository") {
            runTest {
                val mockCall = mock<Call>()
                whenever(repository.hasActiveCall()).thenReturn(false)
                whenever(repository.initiateUserCall("user-42", "video")).thenReturn(Result.success(mockCall))

                println("  → Testing parameter forwarding: receiverId=user-42, callType=video")

                useCase("user-42", "video")

                verify(repository).initiateUserCall("user-42", "video")
                println("  ✅ Parameters forwarded correctly")
            }
        }
    }

    // ==================== StartGroupCallUseCase ====================

    context("StartGroupCallUseCase") {

        lateinit var repository: CallButtonsRepository
        lateinit var useCase: StartGroupCallUseCase

        beforeTest {
            repository = mock()
            useCase = StartGroupCallUseCase(repository)
        }

        test("invoke should delegate to repository when no active call exists") {
            runTest {
                val mockMessage = mock<CustomMessage>()
                whenever(repository.hasActiveCall()).thenReturn(false)
                whenever(repository.startGroupCall("group-1", "video")).thenReturn(Result.success(mockMessage))

                println("  → Testing StartGroupCallUseCase delegation (no active call)")

                val result = useCase("group-1", "video")

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe mockMessage
                verify(repository).startGroupCall("group-1", "video")
                println("  ✅ Delegated to repository, returned Result.success with CustomMessage")
            }
        }

        test("invoke should return Result.failure with ACTIVE_CALL when active call exists") {
            runTest {
                whenever(repository.hasActiveCall()).thenReturn(true)

                println("  → Testing StartGroupCallUseCase with active call blocking")

                val result = useCase("group-1", "audio")

                result.isFailure shouldBe true
                val exception = result.exceptionOrNull() as CometChatException
                exception.code shouldBe "ACTIVE_CALL"
                println("  ✅ Returned Result.failure with code=ACTIVE_CALL")
            }
        }

        test("invoke should propagate Result.failure from repository unchanged") {
            runTest {
                val exception = MockFactory.createCometChatException("ERR_GROUP_CALL", "Group call failed")
                whenever(repository.hasActiveCall()).thenReturn(false)
                whenever(repository.startGroupCall("group-1", "video")).thenReturn(Result.failure(exception))

                println("  → Testing StartGroupCallUseCase failure propagation from repository")

                val result = useCase("group-1", "video")

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("  ✅ Failure propagated unchanged with code=${exception.code}")
            }
        }

        test("invoke should pass correct groupId and callType to repository") {
            runTest {
                val mockMessage = mock<CustomMessage>()
                whenever(repository.hasActiveCall()).thenReturn(false)
                whenever(repository.startGroupCall("group-99", "audio")).thenReturn(Result.success(mockMessage))

                println("  → Testing parameter forwarding: groupId=group-99, callType=audio")

                useCase("group-99", "audio")

                verify(repository).startGroupCall("group-99", "audio")
                println("  ✅ Parameters forwarded correctly")
            }
        }
    }
})

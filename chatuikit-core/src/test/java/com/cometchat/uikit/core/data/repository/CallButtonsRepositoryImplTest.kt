package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.core.Call
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.core.data.datasource.CallButtonsDataSource
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for CallButtonsRepositoryImpl.
 *
 * This layer delegates to CallButtonsDataSource and provides:
 * - initiateUserCall: delegates to dataSource.initiateUserCall
 * - startGroupCall: delegates to dataSource.sendGroupCallMessage
 * - hasActiveCall: checks 3 sources (getActiveCall, getActiveCallingExtensionCall, isActiveMeeting)
 *
 * We mock only the CallButtonsDataSource interface — no SDK dependencies.
 *
 * Reference: CallLogsRepositoryImplTest.kt
 *
 * Validates: Requirements 2.1, 2.2
 *
 * Run with:
 * ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CallButtonsRepositoryImplTest"
 */
class CallButtonsRepositoryImplTest : FunSpec({

    lateinit var dataSource: CallButtonsDataSource
    lateinit var repository: CallButtonsRepositoryImpl

    beforeTest {
        dataSource = mock()
        repository = CallButtonsRepositoryImpl(dataSource)
    }

    // ==================== initiateUserCall — Success ====================

    test("initiateUserCall should delegate to dataSource and return Result.success") {
        runTest {
            val mockCall = mock<Call>()
            whenever(dataSource.initiateUserCall("user-1", "audio")).thenReturn(Result.success(mockCall))

            println("  → Testing initiateUserCall delegation with receiverId=user-1, callType=audio")

            val result = repository.initiateUserCall("user-1", "audio")

            result.isSuccess shouldBe true
            result.getOrNull() shouldBe mockCall
            verify(dataSource).initiateUserCall("user-1", "audio")
            println("  ✅ Result.success with Call object")
        }
    }

    // ==================== initiateUserCall — Failure ====================

    test("initiateUserCall should return Result.failure when dataSource returns failure") {
        runTest {
            val exception = MockFactory.createCometChatException("ERR_CALL", "Call initiation failed")
            whenever(dataSource.initiateUserCall("user-1", "video")).thenReturn(Result.failure(exception))

            println("  → Testing initiateUserCall failure wrapping")

            val result = repository.initiateUserCall("user-1", "video")

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("  ✅ Result.failure with code=${(result.exceptionOrNull() as CometChatException).code}")
        }
    }

    // ==================== startGroupCall — Success ====================

    test("startGroupCall should delegate to dataSource.sendGroupCallMessage and return Result.success") {
        runTest {
            val mockMessage = mock<CustomMessage>()
            whenever(dataSource.sendGroupCallMessage("group-1", "video")).thenReturn(Result.success(mockMessage))

            println("  → Testing startGroupCall delegation with groupId=group-1, callType=video")

            val result = repository.startGroupCall("group-1", "video")

            result.isSuccess shouldBe true
            result.getOrNull() shouldBe mockMessage
            verify(dataSource).sendGroupCallMessage("group-1", "video")
            println("  ✅ Result.success with CustomMessage object")
        }
    }

    // ==================== startGroupCall — Failure ====================

    test("startGroupCall should return Result.failure when dataSource returns failure") {
        runTest {
            val exception = MockFactory.createCometChatException("ERR_GROUP_CALL", "Group call failed")
            whenever(dataSource.sendGroupCallMessage("group-1", "audio")).thenReturn(Result.failure(exception))

            println("  → Testing startGroupCall failure wrapping")

            val result = repository.startGroupCall("group-1", "audio")

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("  ✅ Result.failure with code=${(result.exceptionOrNull() as CometChatException).code}")
        }
    }

    // ==================== hasActiveCall — No Active Call ====================

    test("hasActiveCall should return false when all sources return null/false") {
        whenever(dataSource.getActiveCall()).thenReturn(null)
        whenever(dataSource.getActiveCallingExtensionCall()).thenReturn(null)
        whenever(dataSource.isActiveMeeting()).thenReturn(false)

        println("  → Testing hasActiveCall when no active call exists")

        val result = repository.hasActiveCall()

        result shouldBe false
        println("  ✅ hasActiveCall = false (no active call)")
    }

    // ==================== hasActiveCall — SDK Active Call ====================

    test("hasActiveCall should return true when getActiveCall returns non-null") {
        val mockCall = mock<Call>()
        whenever(dataSource.getActiveCall()).thenReturn(mockCall)
        whenever(dataSource.getActiveCallingExtensionCall()).thenReturn(null)
        whenever(dataSource.isActiveMeeting()).thenReturn(false)

        println("  → Testing hasActiveCall when SDK has active call")

        val result = repository.hasActiveCall()

        result shouldBe true
        println("  ✅ hasActiveCall = true (SDK active call)")
    }

    // ==================== hasActiveCall — CallingExtension Active Call ====================

    test("hasActiveCall should return true when getActiveCallingExtensionCall returns non-null") {
        val mockCall = mock<Call>()
        whenever(dataSource.getActiveCall()).thenReturn(null)
        whenever(dataSource.getActiveCallingExtensionCall()).thenReturn(mockCall)
        whenever(dataSource.isActiveMeeting()).thenReturn(false)

        println("  → Testing hasActiveCall when CallingExtension has active call")

        val result = repository.hasActiveCall()

        result shouldBe true
        println("  ✅ hasActiveCall = true (CallingExtension active call)")
    }

    // ==================== hasActiveCall — Active Meeting ====================

    test("hasActiveCall should return true when isActiveMeeting returns true") {
        whenever(dataSource.getActiveCall()).thenReturn(null)
        whenever(dataSource.getActiveCallingExtensionCall()).thenReturn(null)
        whenever(dataSource.isActiveMeeting()).thenReturn(true)

        println("  → Testing hasActiveCall when active meeting exists")

        val result = repository.hasActiveCall()

        result shouldBe true
        println("  ✅ hasActiveCall = true (active meeting)")
    }

    // ==================== hasActiveCall — Multiple Active Sources ====================

    test("hasActiveCall should return true when multiple sources indicate active call") {
        val mockCall = mock<Call>()
        whenever(dataSource.getActiveCall()).thenReturn(mockCall)
        whenever(dataSource.getActiveCallingExtensionCall()).thenReturn(mockCall)
        whenever(dataSource.isActiveMeeting()).thenReturn(true)

        println("  → Testing hasActiveCall when all sources indicate active call")

        val result = repository.hasActiveCall()

        result shouldBe true
        println("  ✅ hasActiveCall = true (all sources active)")
    }
})

package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.MessageHeaderDataSource
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for MessageHeaderRepositoryImpl.
 * Layer 2 — wraps DataSource calls in Result<T> for error handling.
 * Mocks the DataSource interface — no SDK involved.
 *
 * Reference: ConversationListRepositoryImplTest.kt
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageHeaderRepositoryImplTest"
 */
class MessageHeaderRepositoryImplTest : FunSpec({

    lateinit var dataSource: MessageHeaderDataSource
    lateinit var repository: MessageHeaderRepositoryImpl

    beforeTest {
        dataSource = mock()
        repository = MessageHeaderRepositoryImpl(dataSource)
        println("  🧪 ${it.name.testName}")
    }

    afterTest { println() }

    // ==================== getUser ====================

    test("getUser should delegate to dataSource and wrap in Result.success") {
        runTest {
            val user = MockFactory.createUser(uid = "user-1", name = "Alice")
            whenever(dataSource.getUser("user-1")).thenReturn(user)
            println("    → DataSource configured to return user: ${user.name}")

            val result = repository.getUser("user-1")

            println("    → Result: isSuccess=${result.isSuccess}, user=${result.getOrNull()?.name}")
            result.isSuccess shouldBe true
            result.getOrNull()?.uid shouldBe "user-1"
            result.getOrNull()?.name shouldBe "Alice"
            verify(dataSource).getUser("user-1")
        }
    }

    test("getUser should wrap CometChatException in Result.failure") {
        runTest {
            val exception = MockFactory.createCometChatException("ERR_USER", "User not found")
            whenever(dataSource.getUser("invalid")).thenAnswer { throw exception }
            println("    → DataSource configured to throw: ${exception.message}")

            val result = repository.getUser("invalid")

            println("    → Result: isFailure=${result.isFailure}, exception=${result.exceptionOrNull()?.message}")
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
            (result.exceptionOrNull() as CometChatException).code shouldBe "ERR_USER"
        }
    }

    test("getUser should wrap generic Exception in Result.failure") {
        runTest {
            val exception = RuntimeException("Unexpected error")
            whenever(dataSource.getUser("user-x")).thenAnswer { throw exception }
            println("    → DataSource configured to throw generic: ${exception.message}")

            val result = repository.getUser("user-x")

            println("    → Result: isFailure=${result.isFailure}, exception type=${result.exceptionOrNull()?.javaClass?.simpleName}")
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<RuntimeException>()
        }
    }

    // ==================== getGroup ====================

    test("getGroup should delegate to dataSource and wrap in Result.success") {
        runTest {
            val group = MockFactory.createGroup(guid = "group-1", name = "Developers", membersCount = 10)
            whenever(dataSource.getGroup("group-1")).thenReturn(group)
            println("    → DataSource configured to return group: ${group.name}")

            val result = repository.getGroup("group-1")

            println("    → Result: isSuccess=${result.isSuccess}, group=${result.getOrNull()?.name}")
            result.isSuccess shouldBe true
            result.getOrNull()?.guid shouldBe "group-1"
            result.getOrNull()?.name shouldBe "Developers"
            result.getOrNull()?.membersCount shouldBe 10
            verify(dataSource).getGroup("group-1")
        }
    }

    test("getGroup should wrap CometChatException in Result.failure") {
        runTest {
            val exception = MockFactory.createCometChatException("ERR_GROUP", "Group not found")
            whenever(dataSource.getGroup("invalid")).thenAnswer { throw exception }
            println("    → DataSource configured to throw: ${exception.message}")

            val result = repository.getGroup("invalid")

            println("    → Result: isFailure=${result.isFailure}, exception=${result.exceptionOrNull()?.message}")
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
            (result.exceptionOrNull() as CometChatException).code shouldBe "ERR_GROUP"
        }
    }

    test("getGroup should wrap generic Exception in Result.failure") {
        runTest {
            val exception = RuntimeException("Network timeout")
            whenever(dataSource.getGroup("group-x")).thenAnswer { throw exception }
            println("    → DataSource configured to throw generic: ${exception.message}")

            val result = repository.getGroup("group-x")

            println("    → Result: isFailure=${result.isFailure}, exception type=${result.exceptionOrNull()?.javaClass?.simpleName}")
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<RuntimeException>()
        }
    }

    // ==================== Cancellation (ENG-37016) ====================

    test("getUser should rethrow CancellationException instead of wrapping in Result.failure") {
        runTest {
            whenever(dataSource.getUser("user-1")).thenAnswer { throw CancellationException("scope cancelled") }
            println("    → DataSource configured to throw CancellationException")

            shouldThrow<CancellationException> { repository.getUser("user-1") }
            println("    → CancellationException propagated (not wrapped)")
        }
    }

    test("getGroup should rethrow CancellationException instead of wrapping in Result.failure") {
        runTest {
            whenever(dataSource.getGroup("group-1")).thenAnswer { throw CancellationException("scope cancelled") }
            println("    → DataSource configured to throw CancellationException")

            shouldThrow<CancellationException> { repository.getGroup("group-1") }
            println("    → CancellationException propagated (not wrapped)")
        }
    }

    // ==================== Delegation verification ====================

    test("getUser should pass uid parameter unchanged to dataSource") {
        runTest {
            val uid = "special-uid-123"
            val user = MockFactory.createUser(uid = uid)
            whenever(dataSource.getUser(uid)).thenReturn(user)
            println("    → Testing parameter forwarding with uid=$uid")

            repository.getUser(uid)

            verify(dataSource).getUser(uid)
            println("    → Verified: dataSource.getUser called with exact uid")
        }
    }

    test("getGroup should pass guid parameter unchanged to dataSource") {
        runTest {
            val guid = "special-guid-456"
            val group = MockFactory.createGroup(guid = guid)
            whenever(dataSource.getGroup(guid)).thenReturn(group)
            println("    → Testing parameter forwarding with guid=$guid")

            repository.getGroup(guid)

            verify(dataSource).getGroup(guid)
            println("    → Verified: dataSource.getGroup called with exact guid")
        }
    }
})

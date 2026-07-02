package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.repository.MessageHeaderRepository
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for GetUserUseCase and GetGroupUseCase.
 * Layer 3 — thin delegation layers that forward to Repository.
 * Mocks the Repository interface — no DataSource or SDK involved.
 *
 * Reference: ConversationUseCasesTest.kt
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageHeaderUseCasesTest"
 */
class MessageHeaderUseCasesTest : FunSpec({

    lateinit var repository: MessageHeaderRepository

    beforeTest {
        repository = mock()
        println("  🧪 ${it.name.testName}")
    }

    afterTest { println() }

    // ==================== GetUserUseCase ====================

    context("GetUserUseCase") {

        test("invoke should delegate to repository.getUser with same uid") {
            runTest {
                val useCase = GetUserUseCase(repository)
                val user = MockFactory.createUser(uid = "user-1", name = "Alice")
                whenever(repository.getUser("user-1")).thenReturn(Result.success(user))
                println("    → Repository configured to return user: ${user.name}")

                val result = useCase("user-1")

                println("    → Result: isSuccess=${result.isSuccess}, user=${result.getOrNull()?.name}")
                result.isSuccess shouldBe true
                result.getOrNull()?.uid shouldBe "user-1"
                result.getOrNull()?.name shouldBe "Alice"
                verify(repository).getUser("user-1")
            }
        }

        test("invoke should propagate Result.failure unchanged") {
            runTest {
                val useCase = GetUserUseCase(repository)
                val exception = MockFactory.createCometChatException("ERR_USER", "User not found")
                whenever(repository.getUser("invalid")).thenReturn(Result.failure(exception))
                println("    → Repository configured to return failure: ${exception.message}")

                val result = useCase("invalid")

                println("    → Result: isFailure=${result.isFailure}, exception=${result.exceptionOrNull()?.message}")
                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
            }
        }

        test("invoke should forward uid parameter unchanged") {
            runTest {
                val useCase = GetUserUseCase(repository)
                val uid = "special-uid-xyz"
                val user = MockFactory.createUser(uid = uid)
                whenever(repository.getUser(uid)).thenReturn(Result.success(user))
                println("    → Testing parameter forwarding with uid=$uid")

                useCase(uid)

                verify(repository).getUser(uid)
                println("    → Verified: repository.getUser called with exact uid")
            }
        }
    }

    // ==================== GetGroupUseCase ====================

    context("GetGroupUseCase") {

        test("invoke should delegate to repository.getGroup with same guid") {
            runTest {
                val useCase = GetGroupUseCase(repository)
                val group = MockFactory.createGroup(guid = "group-1", name = "Developers", membersCount = 10)
                whenever(repository.getGroup("group-1")).thenReturn(Result.success(group))
                println("    → Repository configured to return group: ${group.name}")

                val result = useCase("group-1")

                println("    → Result: isSuccess=${result.isSuccess}, group=${result.getOrNull()?.name}")
                result.isSuccess shouldBe true
                result.getOrNull()?.guid shouldBe "group-1"
                result.getOrNull()?.name shouldBe "Developers"
                result.getOrNull()?.membersCount shouldBe 10
                verify(repository).getGroup("group-1")
            }
        }

        test("invoke should propagate Result.failure unchanged") {
            runTest {
                val useCase = GetGroupUseCase(repository)
                val exception = MockFactory.createCometChatException("ERR_GROUP", "Group not found")
                whenever(repository.getGroup("invalid")).thenReturn(Result.failure(exception))
                println("    → Repository configured to return failure: ${exception.message}")

                val result = useCase("invalid")

                println("    → Result: isFailure=${result.isFailure}, exception=${result.exceptionOrNull()?.message}")
                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
            }
        }

        test("invoke should forward guid parameter unchanged") {
            runTest {
                val useCase = GetGroupUseCase(repository)
                val guid = "special-guid-abc"
                val group = MockFactory.createGroup(guid = guid)
                whenever(repository.getGroup(guid)).thenReturn(Result.success(group))
                println("    → Testing parameter forwarding with guid=$guid")

                useCase(guid)

                verify(repository).getGroup(guid)
                println("    → Verified: repository.getGroup called with exact guid")
            }
        }
    }
})

package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.repository.GroupsRepository
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for FetchGroupsUseCase and JoinGroupUseCase.
 *
 * Use cases are thin delegation layers. We mock the GroupsRepository interface
 * and verify parameters are forwarded and results are returned unchanged.
 *
 * The tests validate:
 * - FetchGroupsUseCase.invoke delegates to repository
 * - FetchGroupsUseCase.invoke propagates Result.failure
 * - FetchGroupsUseCase.hasMore delegates to repository
 * - JoinGroupUseCase.invoke delegates with correct params
 * - JoinGroupUseCase.invoke propagates Result.failure
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.GroupsUseCasesTest"
 */
class GroupsUseCasesTest : FunSpec({

    // ==================== FetchGroupsUseCase ====================

    context("FetchGroupsUseCase") {

        lateinit var repository: GroupsRepository
        lateinit var useCase: FetchGroupsUseCase

        beforeTest {
            repository = mock()
            useCase = FetchGroupsUseCase(repository)
            println("\n    🧪 ${it.name.testName}")
            println("    ─────────────────────────────────────────────────")
        }

        test("invoke should delegate to repository.fetchGroups with same request") {
            runTest {
                val request = mock<GroupsRequest>()
                val groups = listOf(
                    MockFactory.createGroup(guid = "g1", name = "Group 1"),
                    MockFactory.createGroup(guid = "g2", name = "Group 2")
                )
                whenever(repository.fetchGroups(request)).thenReturn(Result.success(groups))
                println("    [SETUP] Configured repository.fetchGroups to return Result.success(2 groups)")

                println("    [ACTION] Calling useCase(request)")
                val result = useCase(request)

                println("    [RESULT] result.isSuccess = ${result.isSuccess}")
                println("    [RESULT] result.getOrNull()?.size = ${result.getOrNull()?.size}")
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe groups
                verify(repository).fetchGroups(request)
                println("    [VERIFY] repository.fetchGroups was called with same request ✓")
                println("    ✅ PASSED — FetchGroupsUseCase.invoke delegates to repository")
            }
        }

        test("invoke should propagate Result.failure unchanged") {
            runTest {
                val request = mock<GroupsRequest>()
                val exception = MockFactory.createCometChatException("ERR_FETCH", "Network error")
                whenever(repository.fetchGroups(request)).thenReturn(Result.failure(exception))
                println("    [SETUP] Configured repository.fetchGroups to return Result.failure(ERR_FETCH)")

                println("    [ACTION] Calling useCase(request)")
                val result = useCase(request)

                println("    [RESULT] result.isFailure = ${result.isFailure}")
                println("    [RESULT] exception = ${result.exceptionOrNull()}")
                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("    ✅ PASSED — FetchGroupsUseCase.invoke propagates Result.failure")
            }
        }

        test("hasMore should delegate to repository.hasMoreGroups") {
            println("    [ACTION] Testing hasMore() delegation")

            whenever(repository.hasMoreGroups()).thenReturn(true)
            val hasMoreTrue = useCase.hasMore()
            println("    [RESULT] repository returns true → useCase.hasMore() = $hasMoreTrue")
            hasMoreTrue shouldBe true

            whenever(repository.hasMoreGroups()).thenReturn(false)
            val hasMoreFalse = useCase.hasMore()
            println("    [RESULT] repository returns false → useCase.hasMore() = $hasMoreFalse")
            hasMoreFalse shouldBe false

            println("    ✅ PASSED — FetchGroupsUseCase.hasMore delegates to repository.hasMoreGroups")
        }
    }

    // ==================== JoinGroupUseCase ====================

    context("JoinGroupUseCase") {

        lateinit var repository: GroupsRepository
        lateinit var useCase: JoinGroupUseCase

        beforeTest {
            repository = mock()
            useCase = JoinGroupUseCase(repository)
            println("\n    🧪 ${it.name.testName}")
            println("    ─────────────────────────────────────────────────")
        }

        test("invoke should delegate to repository.joinGroup with correct params") {
            runTest {
                val group = MockFactory.createGroup(guid = "join-1", name = "Join Group")
                whenever(repository.joinGroup("join-1", "public", "secret123"))
                    .thenReturn(Result.success(group))
                println("    [SETUP] Configured repository.joinGroup to return Result.success(group)")

                println("    [ACTION] Calling useCase(join-1, public, secret123)")
                val result = useCase("join-1", "public", "secret123")

                println("    [RESULT] result.isSuccess = ${result.isSuccess}")
                println("    [RESULT] group.guid = ${result.getOrNull()?.guid}")
                result.isSuccess shouldBe true
                result.getOrNull() shouldBe group
                verify(repository).joinGroup("join-1", "public", "secret123")
                println("    [VERIFY] repository.joinGroup was called with (join-1, public, secret123) ✓")
                println("    ✅ PASSED — JoinGroupUseCase.invoke delegates with correct params")
            }
        }

        test("invoke should delegate with null password") {
            runTest {
                val group = MockFactory.createGroup(guid = "pub-1", name = "Public Group")
                whenever(repository.joinGroup("pub-1", "public", null))
                    .thenReturn(Result.success(group))
                println("    [SETUP] Configured repository.joinGroup with null password")

                println("    [ACTION] Calling useCase(pub-1, public, null)")
                val result = useCase("pub-1", "public", null)

                println("    [RESULT] result.isSuccess = ${result.isSuccess}")
                result.isSuccess shouldBe true
                verify(repository).joinGroup("pub-1", "public", null)
                println("    [VERIFY] repository.joinGroup was called with null password ✓")
                println("    ✅ PASSED — JoinGroupUseCase.invoke delegates with null password")
            }
        }

        test("invoke should propagate Result.failure unchanged") {
            runTest {
                val exception = MockFactory.createCometChatException("ERR_JOIN", "Cannot join")
                whenever(repository.joinGroup("g1", "private", "wrong"))
                    .thenReturn(Result.failure(exception))
                println("    [SETUP] Configured repository.joinGroup to return Result.failure(ERR_JOIN)")

                println("    [ACTION] Calling useCase(g1, private, wrong)")
                val result = useCase("g1", "private", "wrong")

                println("    [RESULT] result.isFailure = ${result.isFailure}")
                println("    [RESULT] exception = ${result.exceptionOrNull()}")
                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
                println("    ✅ PASSED — JoinGroupUseCase.invoke propagates Result.failure")
            }
        }
    }
})

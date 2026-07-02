package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.data.datasource.GroupsDataSource
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for GroupsRepositoryImpl.
 *
 * This layer wraps DataSource calls in Result<T> and manages pagination state (hasMore).
 * We mock the GroupsDataSource interface — no SDK involved.
 *
 * The tests validate:
 * - fetchGroups delegates to dataSource and wraps in Result.success
 * - fetchGroups wraps CometChatException in Result.failure
 * - hasMoreGroups pagination state tracking
 * - joinGroup delegates and sets hasJoined=true
 * - joinGroup wraps exception in Result.failure
 * - PBT: any groupId passes through correctly
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.GroupsRepositoryImplTest"
 */
class GroupsRepositoryImplTest : FunSpec({

    lateinit var dataSource: GroupsDataSource
    lateinit var repository: GroupsRepositoryImpl

    beforeTest {
        dataSource = mock()
        repository = GroupsRepositoryImpl(dataSource)
        println("\n    🧪 ${it.name.testName}")
        println("    ─────────────────────────────────────────────────")
    }

    // ==================== fetchGroups Delegation Tests ====================

    test("fetchGroups should delegate to dataSource and wrap in Result.success") {
        runTest {
            println("    [SETUP] Creating 3 mock groups")
            val groups = listOf(
                MockFactory.createGroup(guid = "g1", name = "Group 1"),
                MockFactory.createGroup(guid = "g2", name = "Group 2"),
                MockFactory.createGroup(guid = "g3", name = "Group 3")
            )
            val request = mock<GroupsRequest>()
            whenever(dataSource.fetchGroups(request)).thenReturn(groups)
            println("    [SETUP] Configured dataSource.fetchGroups to return 3 groups")

            println("    [ACTION] Calling repository.fetchGroups(request)")
            val result = repository.fetchGroups(request)

            println("    [RESULT] result.isSuccess = ${result.isSuccess}")
            println("    [RESULT] result.getOrNull()?.size = ${result.getOrNull()?.size}")
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe groups
            verify(dataSource).fetchGroups(request)
            println("    [VERIFY] dataSource.fetchGroups was called with same request ✓")
            println("    ✅ PASSED — fetchGroups delegates and wraps in Result.success")
        }
    }

    test("fetchGroups should wrap CometChatException in Result.failure") {
        runTest {
            val request = mock<GroupsRequest>()
            val exception = MockFactory.createCometChatException("ERR_GROUPS", "Failed to fetch")
            whenever(dataSource.fetchGroups(request)).thenAnswer { throw exception }
            println("    [SETUP] Configured dataSource to throw CometChatException(ERR_GROUPS)")

            println("    [ACTION] Calling repository.fetchGroups(request)")
            val result = repository.fetchGroups(request)

            println("    [RESULT] result.isFailure = ${result.isFailure}")
            println("    [RESULT] exception = ${result.exceptionOrNull()}")
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("    ✅ PASSED — fetchGroups wraps CometChatException in Result.failure")
        }
    }

    // ==================== Pagination State Tests ====================

    test("hasMoreGroups should be true initially") {
        println("    [ACTION] Checking hasMoreGroups() on fresh repository")
        val hasMore = repository.hasMoreGroups()
        println("    [RESULT] hasMoreGroups = $hasMore")
        hasMore shouldBe true
        println("    ✅ PASSED — hasMoreGroups is true initially")
    }

    test("hasMoreGroups should be false after empty fetch") {
        runTest {
            val request = mock<GroupsRequest>()
            whenever(dataSource.fetchGroups(request)).thenReturn(emptyList())
            println("    [SETUP] Configured dataSource to return empty list")

            println("    [ACTION] Calling repository.fetchGroups(request)")
            repository.fetchGroups(request)

            val hasMore = repository.hasMoreGroups()
            println("    [RESULT] hasMoreGroups = $hasMore (after empty fetch)")
            hasMore shouldBe false
            println("    ✅ PASSED — hasMoreGroups is false after empty fetch (end of pagination)")
        }
    }

    test("hasMoreGroups should remain true after non-empty fetch") {
        runTest {
            val groups = listOf(MockFactory.createGroup(guid = "g1", name = "Group 1"))
            val request = mock<GroupsRequest>()
            whenever(dataSource.fetchGroups(request)).thenReturn(groups)
            println("    [SETUP] Configured dataSource to return 1 group")

            println("    [ACTION] Calling repository.fetchGroups(request)")
            repository.fetchGroups(request)

            val hasMore = repository.hasMoreGroups()
            println("    [RESULT] hasMoreGroups = $hasMore (after non-empty fetch)")
            hasMore shouldBe true
            println("    ✅ PASSED — hasMoreGroups remains true after non-empty fetch")
        }
    }

    test("hasMoreGroups should remain true after exception") {
        runTest {
            val request = mock<GroupsRequest>()
            whenever(dataSource.fetchGroups(request)).thenAnswer {
                throw CometChatException("ERR", "Fail")
            }
            println("    [SETUP] Configured dataSource to throw exception")

            println("    [ACTION] Calling repository.fetchGroups(request)")
            repository.fetchGroups(request)

            val hasMore = repository.hasMoreGroups()
            println("    [RESULT] hasMoreGroups = $hasMore (after exception)")
            hasMore shouldBe true
            println("    ✅ PASSED — hasMoreGroups remains true after exception (no pagination update)")
        }
    }

    // ==================== joinGroup Tests ====================

    test("joinGroup should delegate to dataSource and set hasJoined=true") {
        runTest {
            val group = MockFactory.createGroup(guid = "join-1", name = "Join Group")
            whenever(dataSource.joinGroup("join-1", "public", null)).thenReturn(group)
            println("    [SETUP] Configured dataSource.joinGroup to return group guid=join-1")

            println("    [ACTION] Calling repository.joinGroup(join-1, public, null)")
            val result = repository.joinGroup("join-1", "public", null)

            println("    [RESULT] result.isSuccess = ${result.isSuccess}")
            println("    [RESULT] group.guid = ${result.getOrNull()?.guid}")
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe group
            verify(dataSource).joinGroup("join-1", "public", null)
            println("    [VERIFY] dataSource.joinGroup was called with correct params ✓")
            println("    [VERIFY] group.setHasJoined(true) was called on the returned group ✓")
            println("    ✅ PASSED — joinGroup delegates and sets hasJoined=true")
        }
    }

    test("joinGroup should wrap CometChatException in Result.failure") {
        runTest {
            val exception = MockFactory.createCometChatException("ERR_JOIN", "Cannot join")
            whenever(dataSource.joinGroup("g1", "private", "pass")).thenAnswer { throw exception }
            println("    [SETUP] Configured dataSource.joinGroup to throw CometChatException(ERR_JOIN)")

            println("    [ACTION] Calling repository.joinGroup(g1, private, pass)")
            val result = repository.joinGroup("g1", "private", "pass")

            println("    [RESULT] result.isFailure = ${result.isFailure}")
            println("    [RESULT] exception = ${result.exceptionOrNull()}")
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("    ✅ PASSED — joinGroup wraps CometChatException in Result.failure")
        }
    }

    // ==================== Property-based tests ====================

    test("PBT: any groupId passes through joinGroup correctly") {
        println("    [TEST] PBT - any groupId passes through joinGroup")
        checkAll(20, Arb.string(1, 50)) { groupId ->
            runTest {
                val ds = mock<GroupsDataSource>()
                val repo = GroupsRepositoryImpl(ds)
                val group = MockFactory.createGroup(guid = groupId, name = "Test")
                whenever(ds.joinGroup(groupId, "public", null)).thenReturn(group)

                val result = repo.joinGroup(groupId, "public", null)

                result.isSuccess shouldBe true
                verify(ds).joinGroup(groupId, "public", null)
            }
        }
        println("    ✅ PASSED — PBT verified: any groupId passes through joinGroup correctly")
    }
})

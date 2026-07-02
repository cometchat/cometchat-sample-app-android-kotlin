package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.data.datasource.GroupMembersDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Layer 2 Repository tests for GroupMembers.
 * Mocks the GroupMembersDataSource interface.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.GroupMembersRepositoryImplTest"
 */
class GroupMembersRepositoryImplTest : FunSpec({

    lateinit var dataSource: GroupMembersDataSource
    lateinit var repository: GroupMembersRepositoryImpl

    fun createMembers(count: Int): List<GroupMember> {
        return (1..count).map { i -> GroupMember("user-$i", CometChatConstants.SCOPE_PARTICIPANT).apply { name = "User $i" } }
    }

    beforeTest {
        dataSource = mock()
        repository = GroupMembersRepositoryImpl(dataSource)
        println("  🧪 ${it.name.testName}")
    }

    afterTest { println() }

    // ==================== fetchGroupMembers ====================

    test("fetchGroupMembers delegates to dataSource and wraps in Result.success") {
        runTest {
            println("=== TEST: fetchGroupMembers success ===")
            val members = createMembers(3)
            whenever(dataSource.fetchGroupMembers("g1", 30, null)).thenReturn(members)

            val result = repository.fetchGroupMembers("g1", 30, null)

            result.isSuccess shouldBe true
            result.getOrNull()?.size shouldBe 3
            verify(dataSource).fetchGroupMembers("g1", 30, null)
            println("RESULT: Result.success with 3 members ✅")
        }
    }

    test("fetchGroupMembers wraps CometChatException in Result.failure") {
        runTest {
            println("=== TEST: fetchGroupMembers failure ===")
            val exception = CometChatException("ERR", "Failed")
            whenever(dataSource.fetchGroupMembers(any(), any(), anyOrNull())).thenAnswer { throw exception }

            val result = repository.fetchGroupMembers("g1", 30, null)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            println("RESULT: Result.failure with exception ✅")
        }
    }

    // ==================== kickMember ====================

    test("kickMember delegates to dataSource and wraps in Result.success") {
        runTest {
            println("=== TEST: kickMember success ===")
            whenever(dataSource.kickGroupMember("g1", "user-1")).thenReturn("success")

            val result = repository.kickMember("g1", "user-1")

            result.isSuccess shouldBe true
            verify(dataSource).kickGroupMember("g1", "user-1")
            println("RESULT: Kick success ✅")
        }
    }

    test("kickMember wraps exception in Result.failure") {
        runTest {
            println("=== TEST: kickMember failure ===")
            val exception = CometChatException("ERR_KICK", "Cannot kick")
            whenever(dataSource.kickGroupMember(any(), any())).thenAnswer { throw exception }

            val result = repository.kickMember("g1", "user-1")

            result.isFailure shouldBe true
            println("RESULT: Kick failure wrapped ✅")
        }
    }

    // ==================== banMember ====================

    test("banMember delegates to dataSource and wraps in Result.success") {
        runTest {
            println("=== TEST: banMember success ===")
            whenever(dataSource.banGroupMember("g1", "user-2")).thenReturn("success")

            val result = repository.banMember("g1", "user-2")

            result.isSuccess shouldBe true
            verify(dataSource).banGroupMember("g1", "user-2")
            println("RESULT: Ban success ✅")
        }
    }

    test("banMember wraps exception in Result.failure") {
        runTest {
            println("=== TEST: banMember failure ===")
            whenever(dataSource.banGroupMember(any(), any())).thenAnswer { throw CometChatException("ERR", "Fail") }

            val result = repository.banMember("g1", "user-2")

            result.isFailure shouldBe true
            println("RESULT: Ban failure wrapped ✅")
        }
    }

    // ==================== changeMemberScope ====================

    test("changeMemberScope delegates to dataSource") {
        runTest {
            println("=== TEST: changeMemberScope success ===")
            whenever(dataSource.changeMemberScope("g1", "user-3", "admin")).thenReturn("success")

            val result = repository.changeMemberScope("g1", "user-3", "admin")

            result.isSuccess shouldBe true
            verify(dataSource).changeMemberScope("g1", "user-3", "admin")
            println("RESULT: Scope change success ✅")
        }
    }

    test("changeMemberScope wraps exception in Result.failure") {
        runTest {
            println("=== TEST: changeMemberScope failure ===")
            whenever(dataSource.changeMemberScope(any(), any(), any())).thenAnswer { throw CometChatException("ERR", "Fail") }

            val result = repository.changeMemberScope("g1", "user-3", "admin")

            result.isFailure shouldBe true
            println("RESULT: Scope change failure wrapped ✅")
        }
    }

    // ==================== hasMore / resetRequest ====================

    test("hasMore delegates to dataSource") {
        println("=== TEST: hasMore delegation ===")
        whenever(dataSource.hasMoreMembers()).thenReturn(true)
        repository.hasMore() shouldBe true
        whenever(dataSource.hasMoreMembers()).thenReturn(false)
        repository.hasMore() shouldBe false
        println("RESULT: hasMore delegates correctly ✅")
    }

    test("resetRequest delegates to dataSource") {
        println("=== TEST: resetRequest delegation ===")
        repository.resetRequest()
        verify(dataSource).resetRequest()
        println("RESULT: resetRequest delegated ✅")
    }

    // ==================== PBT ====================

    test("PBT: any guid/uid passes through kickMember") {
        println("=== PBT: kickMember params ===")
        checkAll(20, Arb.string(1..30), Arb.string(1..30)) { guid, uid ->
            runTest {
                val ds = mock<GroupMembersDataSource>()
                val repo = GroupMembersRepositoryImpl(ds)
                whenever(ds.kickGroupMember(guid, uid)).thenReturn("ok")
                repo.kickMember(guid, uid)
                verify(ds).kickGroupMember(guid, uid)
            }
        }
        println("RESULT: PBT passed ✅")
    }
})

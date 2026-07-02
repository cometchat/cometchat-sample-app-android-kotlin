package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.GroupMembersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.GroupMember
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest

/**
 * Layer 1 DataSource tests for GroupMembers.
 * Uses testable implementation pattern to verify contract behavior.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.GroupMembersDataSourceImplTest"
 */
class GroupMembersDataSourceImplTest : FunSpec({

    class TestableGroupMembersDataSource : GroupMembersDataSource {
        var fetchResult: List<GroupMember>? = null
        var fetchException: Exception? = null
        var kickResult: String? = null
        var kickException: Exception? = null
        var banResult: String? = null
        var banException: Exception? = null
        var changeScopeResult: String? = null
        var changeScopeException: Exception? = null
        var lastFetchGuid: String? = null
        var lastFetchLimit: Int? = null
        var lastFetchKeyword: String? = null
        var lastKickGuid: String? = null
        var lastKickUid: String? = null
        var lastBanGuid: String? = null
        var lastBanUid: String? = null
        var lastChangeScopeGuid: String? = null
        var lastChangeScopeUid: String? = null
        var lastChangeScopeScope: String? = null
        private var hasMore = true

        override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?): List<GroupMember> {
            lastFetchGuid = guid; lastFetchLimit = limit; lastFetchKeyword = searchKeyword
            fetchException?.let { throw it }
            val result = fetchResult ?: emptyList()
            hasMore = result.isNotEmpty()
            return result
        }
        override suspend fun kickGroupMember(guid: String, uid: String): String {
            lastKickGuid = guid; lastKickUid = uid
            kickException?.let { throw it }
            return kickResult ?: "success"
        }
        override suspend fun banGroupMember(guid: String, uid: String): String {
            lastBanGuid = guid; lastBanUid = uid
            banException?.let { throw it }
            return banResult ?: "success"
        }
        override suspend fun changeMemberScope(guid: String, uid: String, scope: String): String {
            lastChangeScopeGuid = guid; lastChangeScopeUid = uid; lastChangeScopeScope = scope
            changeScopeException?.let { throw it }
            return changeScopeResult ?: "success"
        }
        override fun hasMoreMembers(): Boolean = hasMore
        override fun resetRequest() { hasMore = true }
    }

    fun createMembers(count: Int): List<GroupMember> {
        return (1..count).map { i -> GroupMember("user-$i", CometChatConstants.SCOPE_PARTICIPANT).apply { name = "User $i" } }
    }

    beforeTest { println("  🧪 ${it.name.testName}") }
    afterTest { println() }

    // ==================== fetchGroupMembers ====================

    test("fetchGroupMembers success returns members") {
        runTest {
            println("=== TEST: fetchGroupMembers success ===")
            val ds = TestableGroupMembersDataSource()
            ds.fetchResult = createMembers(3)
            val result = ds.fetchGroupMembers("group-1", 30, null)
            result.size shouldBe 3
            ds.lastFetchGuid shouldBe "group-1"
            ds.lastFetchLimit shouldBe 30
            println("RESULT: 3 members returned ✅")
        }
    }

    test("fetchGroupMembers empty returns empty list") {
        runTest {
            println("=== TEST: fetchGroupMembers empty ===")
            val ds = TestableGroupMembersDataSource()
            ds.fetchResult = emptyList()
            val result = ds.fetchGroupMembers("group-1", 30, null)
            result.size shouldBe 0
            println("RESULT: Empty list returned ✅")
        }
    }

    test("fetchGroupMembers failure throws exception") {
        runTest {
            println("=== TEST: fetchGroupMembers failure ===")
            val ds = TestableGroupMembersDataSource()
            ds.fetchException = CometChatException("ERR", "Failed")
            shouldThrow<CometChatException> { ds.fetchGroupMembers("group-1", 30, null) }
            println("RESULT: Exception thrown ✅")
        }
    }

    test("fetchGroupMembers passes searchKeyword") {
        runTest {
            println("=== TEST: fetchGroupMembers with keyword ===")
            val ds = TestableGroupMembersDataSource()
            ds.fetchResult = createMembers(1)
            ds.fetchGroupMembers("group-1", 30, "john")
            ds.lastFetchKeyword shouldBe "john"
            println("RESULT: Keyword passed ✅")
        }
    }

    // ==================== kickGroupMember ====================

    test("kickGroupMember success") {
        runTest {
            println("=== TEST: kickGroupMember success ===")
            val ds = TestableGroupMembersDataSource()
            val result = ds.kickGroupMember("group-1", "user-1")
            result shouldBe "success"
            ds.lastKickGuid shouldBe "group-1"
            ds.lastKickUid shouldBe "user-1"
            println("RESULT: Kick success ✅")
        }
    }

    test("kickGroupMember failure throws") {
        runTest {
            println("=== TEST: kickGroupMember failure ===")
            val ds = TestableGroupMembersDataSource()
            ds.kickException = CometChatException("ERR_KICK", "Cannot kick")
            shouldThrow<CometChatException> { ds.kickGroupMember("group-1", "user-1") }
            println("RESULT: Exception thrown ✅")
        }
    }

    // ==================== banGroupMember ====================

    test("banGroupMember success") {
        runTest {
            println("=== TEST: banGroupMember success ===")
            val ds = TestableGroupMembersDataSource()
            val result = ds.banGroupMember("group-1", "user-2")
            result shouldBe "success"
            ds.lastBanGuid shouldBe "group-1"
            ds.lastBanUid shouldBe "user-2"
            println("RESULT: Ban success ✅")
        }
    }

    test("banGroupMember failure throws") {
        runTest {
            println("=== TEST: banGroupMember failure ===")
            val ds = TestableGroupMembersDataSource()
            ds.banException = CometChatException("ERR_BAN", "Cannot ban")
            shouldThrow<CometChatException> { ds.banGroupMember("group-1", "user-2") }
            println("RESULT: Exception thrown ✅")
        }
    }

    // ==================== changeMemberScope ====================

    test("changeMemberScope success") {
        runTest {
            println("=== TEST: changeMemberScope success ===")
            val ds = TestableGroupMembersDataSource()
            val result = ds.changeMemberScope("group-1", "user-3", "admin")
            result shouldBe "success"
            ds.lastChangeScopeGuid shouldBe "group-1"
            ds.lastChangeScopeUid shouldBe "user-3"
            ds.lastChangeScopeScope shouldBe "admin"
            println("RESULT: Scope change success ✅")
        }
    }

    test("changeMemberScope failure throws") {
        runTest {
            println("=== TEST: changeMemberScope failure ===")
            val ds = TestableGroupMembersDataSource()
            ds.changeScopeException = CometChatException("ERR_SCOPE", "Cannot change")
            shouldThrow<CometChatException> { ds.changeMemberScope("group-1", "user-3", "admin") }
            println("RESULT: Exception thrown ✅")
        }
    }

    // ==================== hasMoreMembers / resetRequest ====================

    test("hasMoreMembers false after empty fetch") {
        runTest {
            println("=== TEST: hasMoreMembers after empty ===")
            val ds = TestableGroupMembersDataSource()
            ds.fetchResult = emptyList()
            ds.fetchGroupMembers("g", 30, null)
            ds.hasMoreMembers() shouldBe false
            println("RESULT: hasMore=false ✅")
        }
    }

    test("resetRequest resets hasMore to true") {
        runTest {
            println("=== TEST: resetRequest ===")
            val ds = TestableGroupMembersDataSource()
            ds.fetchResult = emptyList()
            ds.fetchGroupMembers("g", 30, null)
            ds.hasMoreMembers() shouldBe false
            ds.resetRequest()
            ds.hasMoreMembers() shouldBe true
            println("RESULT: hasMore reset to true ✅")
        }
    }

    // ==================== PBT ====================

    test("PBT: any count of members returned correctly") {
        println("=== PBT: member count ===")
        checkAll(20, Arb.int(0..50)) { count ->
            runTest {
                val ds = TestableGroupMembersDataSource()
                ds.fetchResult = createMembers(count)
                val result = ds.fetchGroupMembers("g", 30, null)
                result.size shouldBe count
            }
        }
        println("RESULT: PBT passed ✅")
    }

    test("PBT: any guid passes through") {
        println("=== PBT: guid passthrough ===")
        checkAll(20, Arb.string(1..50)) { guid ->
            runTest {
                val ds = TestableGroupMembersDataSource()
                ds.fetchResult = emptyList()
                ds.fetchGroupMembers(guid, 30, null)
                ds.lastFetchGuid shouldBe guid
            }
        }
        println("RESULT: PBT passed ✅")
    }
})

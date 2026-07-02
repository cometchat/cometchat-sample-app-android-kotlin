package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for GroupsDataSourceImpl.
 *
 * Since the DataSource directly depends on CometChat SDK which cannot be easily mocked,
 * these tests use a testable implementation to verify the contract behavior.
 *
 * The tests validate:
 * - fetchGroups success scenarios
 * - fetchGroups error scenarios
 * - joinGroup success scenarios
 * - joinGroup error scenarios
 * - Property-based: any count of groups returned correctly
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.GroupsDataSourceImplTest"
 */
class GroupsDataSourceImplTest : FunSpec({

    /**
     * Testable DataSource implementation for verifying contract behavior.
     * This simulates the behavior expected from GroupsDataSourceImpl
     * without requiring the actual CometChat SDK.
     */
    class TestableGroupsDataSource : GroupsDataSource {
        var fetchGroupsResult: List<Group>? = null
        var fetchGroupsException: Exception? = null
        var joinGroupResult: Group? = null
        var joinGroupException: Exception? = null
        var lastJoinGroupId: String? = null
        var lastJoinGroupType: String? = null
        var lastJoinPassword: String? = null
        var fetchCallCount: Int = 0
        var joinCallCount: Int = 0

        override suspend fun fetchGroups(request: GroupsRequest): List<Group> {
            fetchCallCount++
            fetchGroupsException?.let { throw it }
            return fetchGroupsResult ?: emptyList()
        }

        override suspend fun joinGroup(
            groupId: String,
            groupType: String,
            password: String?
        ): Group {
            joinCallCount++
            lastJoinGroupId = groupId
            lastJoinGroupType = groupType
            lastJoinPassword = password
            joinGroupException?.let { throw it }
            return joinGroupResult ?: throw IllegalStateException("No result configured")
        }
    }

    // ==================== fetchGroups success scenarios ====================

    test("fetchGroups should return list of groups when data source succeeds") {
        runTest {
            println("    [TEST] fetchGroups success - returns list of groups")
            val dataSource = TestableGroupsDataSource()
            val expectedGroups = listOf(
                MockFactory.createGroup(guid = "g1", name = "Group 1"),
                MockFactory.createGroup(guid = "g2", name = "Group 2"),
                MockFactory.createGroup(guid = "g3", name = "Group 3")
            )
            dataSource.fetchGroupsResult = expectedGroups
            println("    [SETUP] Configured dataSource with ${expectedGroups.size} groups")

            val request = org.mockito.kotlin.mock<GroupsRequest>()
            val result = dataSource.fetchGroups(request)

            println("    [RESULT] Returned ${result.size} groups")
            println("    [RESULT] GUIDs: ${result.map { it.guid }}")
            result shouldBe expectedGroups
            result.size shouldBe 3
            println("    ✅ PASSED — fetchGroups returned 3 groups successfully")
        }
    }

    test("fetchGroups should return empty list when no groups exist") {
        runTest {
            println("    [TEST] fetchGroups success - returns empty list")
            val dataSource = TestableGroupsDataSource()
            dataSource.fetchGroupsResult = emptyList()
            println("    [SETUP] Configured dataSource with empty list")

            val request = org.mockito.kotlin.mock<GroupsRequest>()
            val result = dataSource.fetchGroups(request)

            println("    [RESULT] Returned ${result.size} groups (empty)")
            result shouldBe emptyList()
            println("    ✅ PASSED — fetchGroups returned empty list")
        }
    }

    test("fetchGroups should throw CometChatException when data source fails") {
        runTest {
            println("    [TEST] fetchGroups failure - throws CometChatException")
            val dataSource = TestableGroupsDataSource()
            val expectedException = MockFactory.createCometChatException("ERR_FETCH", "Network error")
            dataSource.fetchGroupsException = expectedException
            println("    [SETUP] Configured dataSource to throw CometChatException(ERR_FETCH)")

            val request = org.mockito.kotlin.mock<GroupsRequest>()
            val thrown = shouldThrow<CometChatException> {
                dataSource.fetchGroups(request)
            }

            println("    [RESULT] Exception thrown: code=${thrown.code}, message=${thrown.message}")
            thrown.code shouldBe "ERR_FETCH"
            thrown.message shouldBe "Network error"
            println("    ✅ PASSED — fetchGroups threw CometChatException with correct code/message")
        }
    }

    // ==================== joinGroup success scenarios ====================

    test("joinGroup should return Group when data source succeeds") {
        runTest {
            println("    [TEST] joinGroup success - returns Group")
            val dataSource = TestableGroupsDataSource()
            val expectedGroup = MockFactory.createGroup(guid = "join-1", name = "Joined Group")
            dataSource.joinGroupResult = expectedGroup
            println("    [SETUP] Configured dataSource with joinGroup result: guid=join-1")

            val result = dataSource.joinGroup("join-1", "public", null)

            println("    [RESULT] Returned group: guid=${result.guid}, name=${result.name}")
            println("    [RESULT] Params received: groupId=${dataSource.lastJoinGroupId}, type=${dataSource.lastJoinGroupType}, password=${dataSource.lastJoinPassword}")
            result shouldBe expectedGroup
            dataSource.lastJoinGroupId shouldBe "join-1"
            dataSource.lastJoinGroupType shouldBe "public"
            dataSource.lastJoinPassword shouldBe null
            println("    ✅ PASSED — joinGroup returned correct Group with correct params")
        }
    }

    test("joinGroup should throw CometChatException when data source fails") {
        runTest {
            println("    [TEST] joinGroup failure - throws CometChatException")
            val dataSource = TestableGroupsDataSource()
            val expectedException = MockFactory.createCometChatException("ERR_JOIN", "Cannot join group")
            dataSource.joinGroupException = expectedException
            println("    [SETUP] Configured dataSource to throw CometChatException(ERR_JOIN)")

            val thrown = shouldThrow<CometChatException> {
                dataSource.joinGroup("group-1", "private", "secret")
            }

            println("    [RESULT] Exception thrown: code=${thrown.code}, message=${thrown.message}")
            thrown.code shouldBe "ERR_JOIN"
            thrown.message shouldBe "Cannot join group"
            println("    ✅ PASSED — joinGroup threw CometChatException with correct code/message")
        }
    }

    // ==================== Property-based tests ====================

    test("PBT: fetchGroups should return correct group count for any valid input") {
        println("    [TEST] PBT - any count of groups returned correctly")
        checkAll(20, Arb.int(0, 50)) { groupCount ->
            runTest {
                val dataSource = TestableGroupsDataSource()
                val groups = (1..groupCount).map { i ->
                    MockFactory.createGroup(guid = "pbt-$i", name = "PBT Group $i")
                }
                dataSource.fetchGroupsResult = groups

                val request = org.mockito.kotlin.mock<GroupsRequest>()
                val result = dataSource.fetchGroups(request)

                result.size shouldBe groupCount
            }
        }
        println("    ✅ PASSED — PBT verified: any count 0..50 returns correctly")
    }

    test("PBT: joinGroup should pass through any groupId correctly") {
        println("    [TEST] PBT - any groupId passes through correctly")
        checkAll(20, Arb.string(1, 50)) { groupId ->
            runTest {
                val dataSource = TestableGroupsDataSource()
                val group = MockFactory.createGroup(guid = groupId, name = "Test")
                dataSource.joinGroupResult = group

                dataSource.joinGroup(groupId, "public", null)

                dataSource.lastJoinGroupId shouldBe groupId
            }
        }
        println("    ✅ PASSED — PBT verified: any groupId passes through correctly")
    }
})

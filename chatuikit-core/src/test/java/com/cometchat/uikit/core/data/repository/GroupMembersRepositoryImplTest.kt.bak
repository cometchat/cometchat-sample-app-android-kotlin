package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.GroupMembersDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Unit tests for GroupMembersRepositoryImpl.
 * 
 * These tests verify that the repository correctly delegates to the data source
 * and handles error mapping. The repository is a thin layer that wraps data source
 * calls in Result types and handles exception mapping.
 * 
 * The tests validate:
 * - fetchGroupMembers delegation and error mapping
 * - kickMember delegation and error mapping
 * - banMember delegation and error mapping
 * - changeMemberScope delegation and error mapping
 * - hasMore delegation
 * - Pagination state consistency
 * 
 * **Validates: Requirements 1.1, 1.2, 4.1-4.5, 5.1-5.5, 6.1-6.5, 7.1-7.4**
 * 
 * ## Test Coverage
 * 
 * | Test | Property | Requirements |
 * |------|----------|--------------|
 * | fetchGroupMembers success | - | 1.1, 1.2 |
 * | fetchGroupMembers error mapping | - | 1.1, 1.2 |
 * | kickMember delegation | - | 4.1-4.5 |
 * | banMember delegation | - | 5.1-5.5 |
 * | changeMemberScope delegation | - | 6.1-6.5 |
 * | hasMore delegation | Property 1: Pagination State Consistency | 1.2, 7.1-7.4 |
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GroupMembersRepositoryImplTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    /**
     * Mock data source for testing repository delegation.
     */
    class MockGroupMembersDataSource : GroupMembersDataSource {
        var fetchResult: Result<List<GroupMember>> = Result.success(emptyList())
        var kickResult: Result<String> = Result.success("Success")
        var banResult: Result<String> = Result.success("Success")
        var changeScopeResult: Result<String> = Result.success("Success")
        var hasMore: Boolean = true
        var fetchCallCount: Int = 0
        var kickCallCount: Int = 0
        var banCallCount: Int = 0
        var changeScopeCallCount: Int = 0
        var lastGuid: String? = null
        var lastLimit: Int? = null
        var lastSearchKeyword: String? = null
        var lastKickUid: String? = null
        var lastBanUid: String? = null
        var lastChangeScopeUid: String? = null
        var lastScope: String? = null
        
        override suspend fun fetchGroupMembers(
            guid: String,
            limit: Int,
            searchKeyword: String?
        ): List<GroupMember> {
            fetchCallCount++
            lastGuid = guid
            lastLimit = limit
            lastSearchKeyword = searchKeyword
            return fetchResult.getOrThrow()
        }
        
        override suspend fun kickGroupMember(guid: String, uid: String): String {
            kickCallCount++
            lastGuid = guid
            lastKickUid = uid
            return kickResult.getOrThrow()
        }
        
        override suspend fun banGroupMember(guid: String, uid: String): String {
            banCallCount++
            lastGuid = guid
            lastBanUid = uid
            return banResult.getOrThrow()
        }
        
        override suspend fun changeMemberScope(guid: String, uid: String, scope: String): String {
            changeScopeCallCount++
            lastGuid = guid
            lastChangeScopeUid = uid
            lastScope = scope
            return changeScopeResult.getOrThrow()
        }
        
        override fun hasMoreMembers(): Boolean = hasMore
        
        fun reset() {
            fetchResult = Result.success(emptyList())
            kickResult = Result.success("Success")
            banResult = Result.success("Success")
            changeScopeResult = Result.success("Success")
            hasMore = true
            fetchCallCount = 0
            kickCallCount = 0
            banCallCount = 0
            changeScopeCallCount = 0
            lastGuid = null
            lastLimit = null
            lastSearchKeyword = null
            lastKickUid = null
            lastBanUid = null
            lastChangeScopeUid = null
            lastScope = null
        }
    }

    // ========================================
    // Test fetchGroupMembers success
    // ========================================
    
    context("fetchGroupMembers success scenarios") {
        
        /**
         * Repository should delegate to data source and return Result.success
         * when data source succeeds.
         * 
         * **Validates: Requirements 1.1, 1.2**
         */
        test("fetchGroupMembers should return Result.success with members") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val mockMembers = createMockGroupMembers(5)
                dataSource.fetchResult = Result.success(mockMembers)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.fetchGroupMembers("test_group", 30, null)
                
                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 5
                dataSource.fetchCallCount shouldBe 1
            }
        }
        
        test("fetchGroupMembers should pass correct parameters to data source") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val mockMembers = createMockGroupMembers(3)
                dataSource.fetchResult = Result.success(mockMembers)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                repository.fetchGroupMembers("group_123", 20, "search_term")
                
                dataSource.lastGuid shouldBe "group_123"
                dataSource.lastLimit shouldBe 20
                dataSource.lastSearchKeyword shouldBe "search_term"
            }
        }
        
        test("fetchGroupMembers should handle null search keyword") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val mockMembers = createMockGroupMembers(2)
                dataSource.fetchResult = Result.success(mockMembers)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                repository.fetchGroupMembers("test_group", 30, null)
                
                dataSource.lastSearchKeyword shouldBe null
            }
        }
        
        test("fetchGroupMembers should be callable multiple times") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val mockMembers = createMockGroupMembers(2)
                dataSource.fetchResult = Result.success(mockMembers)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                repository.fetchGroupMembers("test_group", 30, null)
                repository.fetchGroupMembers("test_group", 30, null)
                repository.fetchGroupMembers("test_group", 30, null)
                
                dataSource.fetchCallCount shouldBe 3
            }
        }
        
        /**
         * Property-based test: For any number of members returned,
         * the repository should correctly wrap the result.
         * 
         * **Validates: Requirements 1.1, 1.2**
         */
        test("fetchGroupMembers should return correct member count for any valid input") {
            checkAll(20, Arb.int(1, 100)) { memberCount ->
                runTest {
                    val dataSource = MockGroupMembersDataSource()
                    val mockMembers = createMockGroupMembers(memberCount)
                    dataSource.fetchResult = Result.success(mockMembers)
                    
                    val repository = GroupMembersRepositoryImpl(dataSource)
                    val result = repository.fetchGroupMembers("test_group", 30, null)
                    
                    result.isSuccess shouldBe true
                    result.getOrNull()?.size shouldBe memberCount
                }
            }
        }
    }

    // ========================================
    // Test fetchGroupMembers error mapping
    // ========================================
    
    context("fetchGroupMembers error mapping") {
        
        /**
         * Repository should catch CometChatException from data source
         * and return Result.failure.
         * 
         * **Validates: Requirements 1.1, 1.2**
         */
        test("fetchGroupMembers should return Result.failure on CometChatException") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val testException = CometChatException("ERR_NETWORK", "Network error")
                dataSource.fetchResult = Result.failure(testException)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.fetchGroupMembers("test_group", 30, null)
                
                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "Network error"
            }
        }
        
        test("fetchGroupMembers should preserve CometChatException type") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val testException = CometChatException("ERR_INVALID", "Invalid state")
                dataSource.fetchResult = Result.failure(testException)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.fetchGroupMembers("test_group", 30, null)
                
                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
            }
        }
        
        test("fetchGroupMembers should handle generic Exception") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val testException = RuntimeException("Unexpected error")
                dataSource.fetchResult = Result.failure(testException)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.fetchGroupMembers("test_group", 30, null)
                
                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "Unexpected error"
            }
        }
        
        /**
         * Property-based test: For any error message, the repository
         * should preserve the error message in the Result.
         * 
         * **Validates: Requirements 1.1, 1.2**
         */
        test("fetchGroupMembers should preserve error message for any exception") {
            checkAll(20, Arb.string(1, 100)) { errorMessage ->
                runTest {
                    val dataSource = MockGroupMembersDataSource()
                    val testException = CometChatException("ERR", errorMessage)
                    dataSource.fetchResult = Result.failure(testException)
                    
                    val repository = GroupMembersRepositoryImpl(dataSource)
                    val result = repository.fetchGroupMembers("test_group", 30, null)
                    
                    result.isFailure shouldBe true
                    result.exceptionOrNull()?.message shouldBe errorMessage
                }
            }
        }
    }

    // ========================================
    // Test kickMember delegation
    // ========================================
    
    context("kickMember delegation") {
        
        /**
         * Repository should delegate to data source and return Result.success
         * when kick succeeds.
         * 
         * **Validates: Requirements 4.1-4.5**
         */
        test("kickMember should return Result.success on success") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                dataSource.kickResult = Result.success("Member kicked successfully")
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.kickMember("test_group", "user_123")
                
                result.isSuccess shouldBe true
                dataSource.kickCallCount shouldBe 1
            }
        }
        
        test("kickMember should pass correct parameters to data source") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                dataSource.kickResult = Result.success("Success")
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                repository.kickMember("group_456", "user_789")
                
                dataSource.lastGuid shouldBe "group_456"
                dataSource.lastKickUid shouldBe "user_789"
            }
        }
        
        test("kickMember should return Result.failure on CometChatException") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val testException = CometChatException("ERR_PERMISSION", "Permission denied")
                dataSource.kickResult = Result.failure(testException)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.kickMember("test_group", "user_123")
                
                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "Permission denied"
            }
        }
        
        test("kickMember should preserve CometChatException type") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val testException = CometChatException("ERR_NOT_AUTHORIZED", "Not authorized")
                dataSource.kickResult = Result.failure(testException)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.kickMember("test_group", "user_123")
                
                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
            }
        }
        
        test("kickMember should handle generic Exception") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val testException = RuntimeException("Unexpected error")
                dataSource.kickResult = Result.failure(testException)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.kickMember("test_group", "user_123")
                
                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<RuntimeException>()
            }
        }
        
        /**
         * Property-based test: kickMember should work with any valid guid and uid.
         * 
         * **Validates: Requirements 4.1-4.5**
         */
        test("kickMember should work with any valid guid and uid") {
            checkAll(20, Arb.string(1, 50), Arb.string(1, 50)) { guid, uid ->
                runTest {
                    val dataSource = MockGroupMembersDataSource()
                    dataSource.kickResult = Result.success("Success")
                    
                    val repository = GroupMembersRepositoryImpl(dataSource)
                    val result = repository.kickMember(guid, uid)
                    
                    result.isSuccess shouldBe true
                }
            }
        }
    }

    // ========================================
    // Test banMember delegation
    // ========================================
    
    context("banMember delegation") {
        
        /**
         * Repository should delegate to data source and return Result.success
         * when ban succeeds.
         * 
         * **Validates: Requirements 5.1-5.5**
         */
        test("banMember should return Result.success on success") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                dataSource.banResult = Result.success("Member banned successfully")
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.banMember("test_group", "user_123")
                
                result.isSuccess shouldBe true
                dataSource.banCallCount shouldBe 1
            }
        }
        
        test("banMember should pass correct parameters to data source") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                dataSource.banResult = Result.success("Success")
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                repository.banMember("group_456", "user_789")
                
                dataSource.lastGuid shouldBe "group_456"
                dataSource.lastBanUid shouldBe "user_789"
            }
        }
        
        test("banMember should return Result.failure on CometChatException") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val testException = CometChatException("ERR_PERMISSION", "Permission denied")
                dataSource.banResult = Result.failure(testException)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.banMember("test_group", "user_123")
                
                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "Permission denied"
            }
        }
        
        test("banMember should preserve CometChatException type") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val testException = CometChatException("ERR_NOT_AUTHORIZED", "Not authorized")
                dataSource.banResult = Result.failure(testException)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.banMember("test_group", "user_123")
                
                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
            }
        }
        
        test("banMember should handle generic Exception") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val testException = RuntimeException("Unexpected error")
                dataSource.banResult = Result.failure(testException)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.banMember("test_group", "user_123")
                
                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<RuntimeException>()
            }
        }
        
        /**
         * Property-based test: banMember should work with any valid guid and uid.
         * 
         * **Validates: Requirements 5.1-5.5**
         */
        test("banMember should work with any valid guid and uid") {
            checkAll(20, Arb.string(1, 50), Arb.string(1, 50)) { guid, uid ->
                runTest {
                    val dataSource = MockGroupMembersDataSource()
                    dataSource.banResult = Result.success("Success")
                    
                    val repository = GroupMembersRepositoryImpl(dataSource)
                    val result = repository.banMember(guid, uid)
                    
                    result.isSuccess shouldBe true
                }
            }
        }
    }


    // ========================================
    // Test changeMemberScope delegation
    // ========================================
    
    context("changeMemberScope delegation") {
        
        /**
         * Repository should delegate to data source and return Result.success
         * when scope change succeeds.
         * 
         * **Validates: Requirements 6.1-6.5**
         */
        test("changeMemberScope should return Result.success on success") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                dataSource.changeScopeResult = Result.success("Scope changed successfully")
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.changeMemberScope("test_group", "user_123", "admin")
                
                result.isSuccess shouldBe true
                dataSource.changeScopeCallCount shouldBe 1
            }
        }
        
        test("changeMemberScope should pass correct parameters to data source") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                dataSource.changeScopeResult = Result.success("Success")
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                repository.changeMemberScope("group_456", "user_789", "moderator")
                
                dataSource.lastGuid shouldBe "group_456"
                dataSource.lastChangeScopeUid shouldBe "user_789"
                dataSource.lastScope shouldBe "moderator"
            }
        }
        
        test("changeMemberScope should work with different scope values") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                dataSource.changeScopeResult = Result.success("Success")
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val scopes = listOf("admin", "moderator", "participant")
                
                scopes.forEach { scope ->
                    val result = repository.changeMemberScope("test_group", "user_123", scope)
                    result.isSuccess shouldBe true
                }
            }
        }
        
        test("changeMemberScope should return Result.failure on CometChatException") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val testException = CometChatException("ERR_PERMISSION", "Permission denied")
                dataSource.changeScopeResult = Result.failure(testException)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.changeMemberScope("test_group", "user_123", "admin")
                
                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "Permission denied"
            }
        }
        
        test("changeMemberScope should preserve CometChatException type") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val testException = CometChatException("ERR_NOT_AUTHORIZED", "Only owner can change scope")
                dataSource.changeScopeResult = Result.failure(testException)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.changeMemberScope("test_group", "user_123", "admin")
                
                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
            }
        }
        
        test("changeMemberScope should handle generic Exception") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val testException = RuntimeException("Unexpected error")
                dataSource.changeScopeResult = Result.failure(testException)
                
                val repository = GroupMembersRepositoryImpl(dataSource)
                val result = repository.changeMemberScope("test_group", "user_123", "admin")
                
                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<RuntimeException>()
            }
        }
        
        /**
         * Property-based test: changeMemberScope should work with any valid parameters.
         * 
         * **Validates: Requirements 6.1-6.5**
         */
        test("changeMemberScope should work with any valid parameters") {
            checkAll(20, Arb.string(1, 50), Arb.string(1, 50), Arb.string(1, 20)) { guid, uid, scope ->
                runTest {
                    val dataSource = MockGroupMembersDataSource()
                    dataSource.changeScopeResult = Result.success("Success")
                    
                    val repository = GroupMembersRepositoryImpl(dataSource)
                    val result = repository.changeMemberScope(guid, uid, scope)
                    
                    result.isSuccess shouldBe true
                }
            }
        }
    }


    // ========================================
    // Test hasMore delegation
    // ========================================
    
    context("hasMore delegation") {
        
        /**
         * **Property 1: Pagination State Consistency**
         * 
         * Repository should correctly delegate hasMore() to data source.
         * 
         * **Validates: Requirements 1.2, 7.1-7.4**
         */
        test("hasMore should delegate to data source") {
            val dataSource = MockGroupMembersDataSource()
            dataSource.hasMore = true
            
            val repository = GroupMembersRepositoryImpl(dataSource)
            
            repository.hasMore() shouldBe true
        }
        
        test("hasMore should return false when data source returns false") {
            val dataSource = MockGroupMembersDataSource()
            dataSource.hasMore = false
            
            val repository = GroupMembersRepositoryImpl(dataSource)
            
            repository.hasMore() shouldBe false
        }
        
        test("hasMore should reflect data source state changes") {
            val dataSource = MockGroupMembersDataSource()
            val repository = GroupMembersRepositoryImpl(dataSource)
            
            dataSource.hasMore = true
            repository.hasMore() shouldBe true
            
            dataSource.hasMore = false
            repository.hasMore() shouldBe false
            
            dataSource.hasMore = true
            repository.hasMore() shouldBe true
        }
        
        /**
         * Property-based test: Pagination state should be consistent
         * across repository and data source.
         * 
         * **Property 1: Pagination State Consistency**
         * **Validates: Requirements 1.2, 7.1-7.4**
         */
        test("Property 1: hasMore should always match data source state") {
            checkAll(20, Arb.int(0, 50)) { memberCount ->
                runTest {
                    val dataSource = MockGroupMembersDataSource()
                    val repository = GroupMembersRepositoryImpl(dataSource)
                    
                    val mockMembers = if (memberCount > 0) createMockGroupMembers(memberCount) else emptyList()
                    dataSource.fetchResult = Result.success(mockMembers)
                    
                    // Fetch members
                    repository.fetchGroupMembers("test_group", 30, null)
                    
                    // Repository hasMore should match data source hasMore
                    repository.hasMore() shouldBe dataSource.hasMoreMembers()
                }
            }
        }
    }

    // ========================================
    // Test pagination consistency
    // ========================================
    
    context("pagination consistency") {
        
        /**
         * Test that repository maintains pagination state correctly
         * across multiple fetch operations.
         * 
         * **Property 1: Pagination State Consistency**
         * **Validates: Requirements 1.2, 7.1-7.4**
         */
        test("Sequential fetches should maintain correct pagination state") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val repository = GroupMembersRepositoryImpl(dataSource)
                
                // First batch - has members
                dataSource.fetchResult = Result.success(createMockGroupMembers(5))
                dataSource.hasMore = true
                repository.fetchGroupMembers("test_group", 30, null)
                repository.hasMore() shouldBe true
                
                // Second batch - has members
                dataSource.fetchResult = Result.success(createMockGroupMembers(3))
                dataSource.hasMore = true
                repository.fetchGroupMembers("test_group", 30, null)
                repository.hasMore() shouldBe true
                
                // Third batch - empty (end of list)
                dataSource.fetchResult = Result.success(emptyList())
                dataSource.hasMore = false
                repository.fetchGroupMembers("test_group", 30, null)
                repository.hasMore() shouldBe false
            }
        }
        
        test("Error during fetch should not affect pagination state") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val repository = GroupMembersRepositoryImpl(dataSource)
                
                // Initial state
                dataSource.hasMore = true
                repository.hasMore() shouldBe true
                
                // Fetch fails
                dataSource.fetchResult = Result.failure(CometChatException("ERR", "Error"))
                val result = repository.fetchGroupMembers("test_group", 30, null)
                result.isFailure shouldBe true
                
                // Pagination state should remain unchanged
                repository.hasMore() shouldBe true
            }
        }
        
        test("Repository should handle data source pagination state correctly") {
            runTest {
                val dataSource = MockGroupMembersDataSource()
                val repository = GroupMembersRepositoryImpl(dataSource)
                
                // Simulate data source behavior: hasMore becomes false after empty result
                dataSource.fetchResult = Result.success(createMockGroupMembers(5))
                dataSource.hasMore = true
                repository.fetchGroupMembers("test_group", 30, null)
                repository.hasMore() shouldBe true
                
                // Data source sets hasMore to false after empty result
                dataSource.fetchResult = Result.success(emptyList())
                dataSource.hasMore = false
                repository.fetchGroupMembers("test_group", 30, null)
                repository.hasMore() shouldBe false
            }
        }
    }
})


/**
 * Helper function to create mock GroupMember instances for testing.
 * 
 * @param count Number of members to create
 * @param startId Starting ID for the members (default 1)
 */
private fun createMockGroupMembers(count: Int, startId: Int = 1): List<GroupMember> {
    return (startId until startId + count).map { index ->
        GroupMember("user_$index", "participant").apply {
            name = "User $index"
        }
    }
}

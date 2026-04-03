package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.domain.repository.GroupMembersRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for FetchGroupMembersUseCase.
 * 
 * Feature: group-members-component
 * Tests verify use case invocation calls repository methods correctly.
 */
class FetchGroupMembersUseCaseTest : StringSpec({

    "invoke should return success with group members when repository succeeds" {
        runTest {
            // Arrange
            val mockMembers = listOf<GroupMember>() // Empty list for simplicity
            
            val mockRepository = object : GroupMembersRepository {
                override suspend fun fetchGroupMembers(
                    guid: String,
                    limit: Int,
                    searchKeyword: String?
                ): Result<List<GroupMember>> {
                    return Result.success(mockMembers)
                }
                override suspend fun kickMember(guid: String, uid: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override suspend fun banMember(guid: String, uid: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override suspend fun changeMemberScope(guid: String, uid: String, scope: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override fun hasMore(): Boolean = true
                override fun resetRequest() {}
            }
            val useCase = FetchGroupMembersUseCase(mockRepository)

            // Act
            val result = useCase(guid = "test-group", limit = 30, searchKeyword = null)

            // Assert
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe mockMembers
        }
    }

    "invoke should return failure when repository fails" {
        runTest {
            // Arrange
            val exception = CometChatException("NETWORK_ERROR", "Network error", "Network unavailable")
            val mockRepository = object : GroupMembersRepository {
                override suspend fun fetchGroupMembers(
                    guid: String,
                    limit: Int,
                    searchKeyword: String?
                ): Result<List<GroupMember>> {
                    return Result.failure(exception)
                }
                override suspend fun kickMember(guid: String, uid: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override suspend fun banMember(guid: String, uid: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override suspend fun changeMemberScope(guid: String, uid: String, scope: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override fun hasMore(): Boolean = false
                override fun resetRequest() {}
            }
            val useCase = FetchGroupMembersUseCase(mockRepository)

            // Act
            val result = useCase(guid = "test-group", limit = 30, searchKeyword = null)

            // Assert
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
        }
    }

    "invoke should return empty list when repository returns empty" {
        runTest {
            // Arrange
            val mockRepository = object : GroupMembersRepository {
                override suspend fun fetchGroupMembers(
                    guid: String,
                    limit: Int,
                    searchKeyword: String?
                ): Result<List<GroupMember>> {
                    return Result.success(emptyList())
                }
                override suspend fun kickMember(guid: String, uid: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override suspend fun banMember(guid: String, uid: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override suspend fun changeMemberScope(guid: String, uid: String, scope: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override fun hasMore(): Boolean = false
                override fun resetRequest() {}
            }
            val useCase = FetchGroupMembersUseCase(mockRepository)

            // Act
            val result = useCase(guid = "test-group", limit = 30, searchKeyword = null)

            // Assert
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe emptyList()
        }
    }

    "invoke should pass search keyword to repository" {
        runTest {
            // Arrange
            var capturedSearchKeyword: String? = null
            val mockRepository = object : GroupMembersRepository {
                override suspend fun fetchGroupMembers(
                    guid: String,
                    limit: Int,
                    searchKeyword: String?
                ): Result<List<GroupMember>> {
                    capturedSearchKeyword = searchKeyword
                    return Result.success(emptyList())
                }
                override suspend fun kickMember(guid: String, uid: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override suspend fun banMember(guid: String, uid: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override suspend fun changeMemberScope(guid: String, uid: String, scope: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override fun hasMore(): Boolean = false
                override fun resetRequest() {}
            }
            val useCase = FetchGroupMembersUseCase(mockRepository)

            // Act
            useCase(guid = "test-group", limit = 30, searchKeyword = "john")

            // Assert
            capturedSearchKeyword shouldBe "john"
        }
    }

    "invoke should use default limit when not specified" {
        runTest {
            // Arrange
            var capturedLimit: Int? = null
            val mockRepository = object : GroupMembersRepository {
                override suspend fun fetchGroupMembers(
                    guid: String,
                    limit: Int,
                    searchKeyword: String?
                ): Result<List<GroupMember>> {
                    capturedLimit = limit
                    return Result.success(emptyList())
                }
                override suspend fun kickMember(guid: String, uid: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override suspend fun banMember(guid: String, uid: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override suspend fun changeMemberScope(guid: String, uid: String, scope: String): Result<Unit> {
                    return Result.success(Unit)
                }
                override fun hasMore(): Boolean = false
                override fun resetRequest() {}
            }
            val useCase = FetchGroupMembersUseCase(mockRepository)

            // Act
            useCase(guid = "test-group")

            // Assert
            capturedLimit shouldBe 30
        }
    }

    "hasMore should delegate to repository hasMore" {
        // Arrange - hasMore returns true
        val mockRepositoryWithMore = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(
                guid: String,
                limit: Int,
                searchKeyword: String?
            ): Result<List<GroupMember>> {
                return Result.success(emptyList())
            }
            override suspend fun kickMember(guid: String, uid: String): Result<Unit> {
                return Result.success(Unit)
            }
            override suspend fun banMember(guid: String, uid: String): Result<Unit> {
                return Result.success(Unit)
            }
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String): Result<Unit> {
                return Result.success(Unit)
            }
            override fun hasMore(): Boolean = true
            override fun resetRequest() {}
        }
        val useCaseWithMore = FetchGroupMembersUseCase(mockRepositoryWithMore)

        // Assert
        useCaseWithMore.hasMore() shouldBe true

        // Arrange - hasMore returns false
        val mockRepositoryNoMore = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(
                guid: String,
                limit: Int,
                searchKeyword: String?
            ): Result<List<GroupMember>> {
                return Result.success(emptyList())
            }
            override suspend fun kickMember(guid: String, uid: String): Result<Unit> {
                return Result.success(Unit)
            }
            override suspend fun banMember(guid: String, uid: String): Result<Unit> {
                return Result.success(Unit)
            }
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String): Result<Unit> {
                return Result.success(Unit)
            }
            override fun hasMore(): Boolean = false
            override fun resetRequest() {}
        }
        val useCaseNoMore = FetchGroupMembersUseCase(mockRepositoryNoMore)

        // Assert
        useCaseNoMore.hasMore() shouldBe false
    }
})

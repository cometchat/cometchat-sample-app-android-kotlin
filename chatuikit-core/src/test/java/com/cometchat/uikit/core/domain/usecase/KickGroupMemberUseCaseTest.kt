package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.domain.repository.GroupMembersRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for KickGroupMemberUseCase.
 * 
 * Feature: group-members-component
 * Tests verify use case invocation calls repository methods correctly.
 */
class KickGroupMemberUseCaseTest : StringSpec({

    "invoke should return success when repository succeeds" {
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
            val useCase = KickGroupMemberUseCase(mockRepository)

            // Act
            val result = useCase(guid = "test-group", uid = "user123")

            // Assert
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe Unit
        }
    }

    "invoke should return failure when repository fails" {
        runTest {
            // Arrange
            val exception = CometChatException("PERMISSION_DENIED", "Permission denied", "User not authorized")
            val mockRepository = object : GroupMembersRepository {
                override suspend fun fetchGroupMembers(
                    guid: String,
                    limit: Int,
                    searchKeyword: String?
                ): Result<List<GroupMember>> {
                    return Result.success(emptyList())
                }
                override suspend fun kickMember(guid: String, uid: String): Result<Unit> {
                    return Result.failure(exception)
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
            val useCase = KickGroupMemberUseCase(mockRepository)

            // Act
            val result = useCase(guid = "test-group", uid = "user123")

            // Assert
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<CometChatException>()
        }
    }

    "invoke should pass correct parameters to repository" {
        runTest {
            // Arrange
            var capturedGuid: String? = null
            var capturedUid: String? = null
            val mockRepository = object : GroupMembersRepository {
                override suspend fun fetchGroupMembers(
                    guid: String,
                    limit: Int,
                    searchKeyword: String?
                ): Result<List<GroupMember>> {
                    return Result.success(emptyList())
                }
                override suspend fun kickMember(guid: String, uid: String): Result<Unit> {
                    capturedGuid = guid
                    capturedUid = uid
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
            val useCase = KickGroupMemberUseCase(mockRepository)

            // Act
            useCase(guid = "group-456", uid = "user-789")

            // Assert
            capturedGuid shouldBe "group-456"
            capturedUid shouldBe "user-789"
        }
    }

    "invoke should handle network errors" {
        runTest {
            // Arrange
            val exception = CometChatException("NETWORK_ERROR", "Network error", "Connection timeout")
            val mockRepository = object : GroupMembersRepository {
                override suspend fun fetchGroupMembers(
                    guid: String,
                    limit: Int,
                    searchKeyword: String?
                ): Result<List<GroupMember>> {
                    return Result.success(emptyList())
                }
                override suspend fun kickMember(guid: String, uid: String): Result<Unit> {
                    return Result.failure(exception)
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
            val useCase = KickGroupMemberUseCase(mockRepository)

            // Act
            val result = useCase(guid = "test-group", uid = "user123")

            // Assert
            result.isFailure shouldBe true
            val error = result.exceptionOrNull() as CometChatException
            error.code shouldBe "NETWORK_ERROR"
        }
    }

    "invoke should handle member not found errors" {
        runTest {
            // Arrange
            val exception = CometChatException("MEMBER_NOT_FOUND", "Member not found", "User is not a member")
            val mockRepository = object : GroupMembersRepository {
                override suspend fun fetchGroupMembers(
                    guid: String,
                    limit: Int,
                    searchKeyword: String?
                ): Result<List<GroupMember>> {
                    return Result.success(emptyList())
                }
                override suspend fun kickMember(guid: String, uid: String): Result<Unit> {
                    return Result.failure(exception)
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
            val useCase = KickGroupMemberUseCase(mockRepository)

            // Act
            val result = useCase(guid = "test-group", uid = "nonexistent-user")

            // Assert
            result.isFailure shouldBe true
            val error = result.exceptionOrNull() as CometChatException
            error.code shouldBe "MEMBER_NOT_FOUND"
        }
    }
})

package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.domain.repository.UsersRepository
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for FetchUsersUseCase and SearchUsersUseCase.
 *
 * Use cases are thin delegation layers. We mock the Repository interface
 * and verify parameters are forwarded and results are returned unchanged.
 */
class UserUseCasesTest : FunSpec({

    // ==================== FetchUsersUseCase ====================

    context("FetchUsersUseCase") {

        lateinit var repository: UsersRepository
        lateinit var useCase: FetchUsersUseCase

        beforeTest {
            repository = mock()
            useCase = FetchUsersUseCase(repository)
        }

        test("invoke should delegate to repository.getUsers with same request") {
            runTest {
                val request = mock<UsersRequest>()
                val users = MockFactory.createUsers(3)
                whenever(repository.getUsers(request)).thenReturn(Result.success(users))

                val result = useCase(request)

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe users
                verify(repository).getUsers(request)
            }
        }

        test("invoke should propagate Result.failure unchanged") {
            runTest {
                val request = mock<UsersRequest>()
                val exception = MockFactory.createCometChatException()
                whenever(repository.getUsers(request)).thenReturn(Result.failure(exception))

                val result = useCase(request)

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
            }
        }

        test("hasMore should delegate to repository.hasMoreUsers") {
            whenever(repository.hasMoreUsers()).thenReturn(true)
            useCase.hasMore() shouldBe true

            whenever(repository.hasMoreUsers()).thenReturn(false)
            useCase.hasMore() shouldBe false
        }
    }

    // ==================== SearchUsersUseCase ====================

    context("SearchUsersUseCase") {

        lateinit var repository: UsersRepository
        lateinit var useCase: SearchUsersUseCase

        beforeTest {
            repository = mock()
            useCase = SearchUsersUseCase(repository)
        }

        test("invoke should build request with search keyword and delegate to repository") {
            runTest {
                val users = MockFactory.createUsers(2)
                whenever(repository.getUsers(any())).thenReturn(Result.success(users))

                val builder = mock<UsersRequest.UsersRequestBuilder>()
                val builtRequest = mock<UsersRequest>()
                whenever(builder.setSearchKeyword("alice")).thenReturn(builder)
                whenever(builder.build()).thenReturn(builtRequest)
                whenever(repository.getUsers(builtRequest)).thenReturn(Result.success(users))

                val result = useCase("alice", builder)

                result.isSuccess shouldBe true
                result.getOrNull() shouldBe users
                verify(builder).setSearchKeyword("alice")
                verify(builder).build()
            }
        }

        test("invoke should propagate failure from repository") {
            runTest {
                val exception = MockFactory.createCometChatException("SEARCH_ERR", "Search failed")
                val builder = mock<UsersRequest.UsersRequestBuilder>()
                val builtRequest = mock<UsersRequest>()
                whenever(builder.setSearchKeyword(any())).thenReturn(builder)
                whenever(builder.build()).thenReturn(builtRequest)
                whenever(repository.getUsers(builtRequest)).thenReturn(Result.failure(exception))

                val result = useCase("test", builder)

                result.isFailure shouldBe true
                result.exceptionOrNull() shouldBe exception
            }
        }
    }
})

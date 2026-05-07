package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.core.data.datasource.UsersDataSource
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for UsersRepositoryImpl.
 *
 * This layer wraps DataSource calls in Result<T> and manages pagination state.
 * We mock the UsersDataSource interface — no SDK involved.
 */
class UsersRepositoryImplTest : FunSpec({

    lateinit var dataSource: UsersDataSource
    lateinit var repository: UsersRepositoryImpl

    beforeTest {
        dataSource = mock()
        repository = UsersRepositoryImpl(dataSource)
    }

    // ==================== Delegation Tests ====================

    test("getUsers should delegate to dataSource.fetchUsers and wrap in Result.success") {
        runTest {
            val users = MockFactory.createUsers(3)
            val request = mock<UsersRequest>()
            whenever(dataSource.fetchUsers(request)).thenReturn(users)

            val result = repository.getUsers(request)

            result.isSuccess shouldBe true
            result.getOrNull() shouldBe users
            verify(dataSource).fetchUsers(request)
        }
    }

    test("getUsers should wrap CometChatException in Result.failure") {
        runTest {
            val request = mock<UsersRequest>()
            val exception = MockFactory.createCometChatException("ERR", "Failed")
            whenever(dataSource.fetchUsers(request)).thenAnswer { throw exception }

            val result = repository.getUsers(request)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
        }
    }

    test("getUsers should wrap generic Exception in Result.failure") {
        runTest {
            val request = mock<UsersRequest>()
            val exception = RuntimeException("Unexpected error")
            whenever(dataSource.fetchUsers(any())).thenAnswer { throw exception }

            val result = repository.getUsers(request)

            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
        }
    }

    // ==================== Pagination State Tests ====================

    test("hasMoreUsers should be true initially") {
        repository.hasMoreUsers() shouldBe true
    }

    test("hasMoreUsers should be true when dataSource returns non-empty list") {
        runTest {
            val users = MockFactory.createUsers(5)
            val request = mock<UsersRequest>()
            whenever(dataSource.fetchUsers(request)).thenReturn(users)

            repository.getUsers(request)

            repository.hasMoreUsers() shouldBe true
        }
    }

    test("hasMoreUsers should be false when dataSource returns empty list") {
        runTest {
            val request = mock<UsersRequest>()
            whenever(dataSource.fetchUsers(request)).thenReturn(emptyList())

            repository.getUsers(request)

            repository.hasMoreUsers() shouldBe false
        }
    }

    test("hasMoreUsers should remain true when dataSource throws exception") {
        runTest {
            val request = mock<UsersRequest>()
            whenever(dataSource.fetchUsers(request)).thenAnswer { throw CometChatException("ERR", "Fail") }

            repository.getUsers(request)

            // Exception path doesn't update hasMore — it stays true
            repository.hasMoreUsers() shouldBe true
        }
    }

    test("pagination state should track across multiple fetches") {
        runTest {
            val ds = mock<UsersDataSource>()
            val repo = UsersRepositoryImpl(ds)

            val request = mock<UsersRequest>()
            val nonEmptyUsers = MockFactory.createUsers(10)

            // First fetch: non-empty → hasMore = true
            whenever(ds.fetchUsers(any())).thenReturn(nonEmptyUsers)
            repo.getUsers(request)
            repo.hasMoreUsers() shouldBe true

            // Second fetch: empty → hasMore = false (end of list)
            whenever(ds.fetchUsers(any())).thenReturn(emptyList())
            repo.getUsers(request)
            repo.hasMoreUsers() shouldBe false
        }
    }
})

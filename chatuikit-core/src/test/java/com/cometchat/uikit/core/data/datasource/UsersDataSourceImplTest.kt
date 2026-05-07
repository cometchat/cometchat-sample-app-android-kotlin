package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for UsersDataSourceImpl.
 *
 * This is the lowest layer — it bridges CometChat SDK callbacks to coroutines.
 * We mock the UsersRequest object and simulate SDK callback behavior.
 */
class UsersDataSourceImplTest : FunSpec({

    lateinit var dataSource: UsersDataSourceImpl

    beforeTest {
        dataSource = UsersDataSourceImpl()
    }

    test("fetchUsers should return users when SDK callback succeeds") {
        runTest {
            val mockRequest = mock<UsersRequest>()
            val expectedUsers = MockFactory.createUsers(3)

            // Simulate SDK success callback
            whenever(mockRequest.fetchNext(any())).thenAnswer { invocation ->
                @Suppress("UNCHECKED_CAST")
                val callback = invocation.getArgument<CometChat.CallbackListener<List<User>>>(0)
                callback.onSuccess(expectedUsers)
            }

            val result = dataSource.fetchUsers(mockRequest)

            result shouldBe expectedUsers
            result.size shouldBe 3
        }
    }

    test("fetchUsers should throw CometChatException when SDK callback fails") {
        runTest {
            val mockRequest = mock<UsersRequest>()
            val expectedException = MockFactory.createCometChatException("ERR_FETCH", "Network error")

            // Simulate SDK error callback
            whenever(mockRequest.fetchNext(any())).thenAnswer { invocation ->
                @Suppress("UNCHECKED_CAST")
                val callback = invocation.getArgument<CometChat.CallbackListener<List<User>>>(0)
                callback.onError(expectedException)
            }

            val thrown = shouldThrow<CometChatException> {
                dataSource.fetchUsers(mockRequest)
            }

            thrown.code shouldBe "ERR_FETCH"
            thrown.message shouldBe "Network error"
        }
    }

    test("fetchUsers should return empty list when SDK returns empty") {
        runTest {
            val mockRequest = mock<UsersRequest>()

            whenever(mockRequest.fetchNext(any())).thenAnswer { invocation ->
                @Suppress("UNCHECKED_CAST")
                val callback = invocation.getArgument<CometChat.CallbackListener<List<User>>>(0)
                callback.onSuccess(emptyList())
            }

            val result = dataSource.fetchUsers(mockRequest)

            result shouldBe emptyList()
        }
    }
})

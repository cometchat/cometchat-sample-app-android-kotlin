package com.cometchat.uikit.core.factory

import com.cometchat.uikit.core.data.datasource.UsersDataSource
import com.cometchat.uikit.core.data.repository.UsersRepositoryImpl
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import org.mockito.kotlin.mock

/**
 * Tests for CometChatUsersViewModelFactory.
 * Verifies correct ViewModel creation and error handling.
 */
class CometChatUsersViewModelFactoryTest : FunSpec({

    test("create should return CometChatUsersViewModel for correct class") {
        // Use a mock DataSource to avoid real SDK calls
        val mockDataSource = mock<UsersDataSource>()
        val repository = UsersRepositoryImpl(mockDataSource)
        val factory = CometChatUsersViewModelFactory(
            repository = repository,
            enableListeners = false // Disable SDK listeners for testing
        )

        val viewModel = factory.create(CometChatUsersViewModel::class.java)

        viewModel.shouldBeInstanceOf<CometChatUsersViewModel>()
    }

    test("create should throw IllegalArgumentException for unsupported ViewModel class") {
        val mockDataSource = mock<UsersDataSource>()
        val repository = UsersRepositoryImpl(mockDataSource)
        val factory = CometChatUsersViewModelFactory(
            repository = repository,
            enableListeners = false
        )

        shouldThrow<IllegalArgumentException> {
            factory.create(CometChatConversationsViewModel::class.java)
        }
    }

    test("create should pass custom repository through to ViewModel") {
        val mockDataSource = mock<UsersDataSource>()
        val customRepository = UsersRepositoryImpl(mockDataSource)
        val factory = CometChatUsersViewModelFactory(
            repository = customRepository,
            enableListeners = false
        )

        // Should not throw — custom repository is accepted
        val viewModel = factory.create(CometChatUsersViewModel::class.java)
        viewModel.shouldBeInstanceOf<CometChatUsersViewModel>()
    }
})

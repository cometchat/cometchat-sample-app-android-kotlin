package com.cometchat.uikit.core.factory

import com.cometchat.uikit.core.data.datasource.ConversationListDataSource
import com.cometchat.uikit.core.data.repository.ConversationListRepositoryImpl
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import org.mockito.kotlin.mock

/**
 * Tests for CometChatConversationsViewModelFactory.
 * Verifies correct ViewModel creation and error handling.
 *
 * Reference: CometChatUsersViewModelFactoryTest.kt
 */
class CometChatConversationsViewModelFactoryTest : FunSpec({

    test("create should return CometChatConversationsViewModel for correct class") {
        // Use a mock DataSource to avoid real SDK calls
        val mockDataSource = mock<ConversationListDataSource>()
        val repository = ConversationListRepositoryImpl(mockDataSource)
        val factory = CometChatConversationsViewModelFactory(
            repository = repository,
            enableListeners = false // Disable SDK listeners for testing
        )

        val viewModel = factory.create(CometChatConversationsViewModel::class.java)

        viewModel.shouldBeInstanceOf<CometChatConversationsViewModel>()
    }

    test("create should throw IllegalArgumentException for unsupported ViewModel class") {
        val mockDataSource = mock<ConversationListDataSource>()
        val repository = ConversationListRepositoryImpl(mockDataSource)
        val factory = CometChatConversationsViewModelFactory(
            repository = repository,
            enableListeners = false
        )

        shouldThrow<IllegalArgumentException> {
            factory.create(CometChatUsersViewModel::class.java)
        }
    }

    test("create should pass custom repository through to ViewModel") {
        val mockDataSource = mock<ConversationListDataSource>()
        val customRepository = ConversationListRepositoryImpl(mockDataSource)
        val factory = CometChatConversationsViewModelFactory(
            repository = customRepository,
            enableListeners = false
        )

        // Should not throw — custom repository is accepted
        val viewModel = factory.create(CometChatConversationsViewModel::class.java)
        viewModel.shouldBeInstanceOf<CometChatConversationsViewModel>()
    }
})

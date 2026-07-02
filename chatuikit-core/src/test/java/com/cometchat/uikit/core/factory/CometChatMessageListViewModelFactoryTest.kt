package com.cometchat.uikit.core.factory

import com.cometchat.uikit.core.data.datasource.MessageListDataSource
import com.cometchat.uikit.core.data.repository.MessageListRepositoryImpl
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import org.mockito.kotlin.mock

/**
 * Tests for CometChatMessageListViewModelFactory.
 * Verifies correct ViewModel creation and error handling.
 *
 * Layer 4 — Factory tests verify that the ViewModelProvider.Factory
 * correctly instantiates the ViewModel with injected dependencies.
 *
 * Reference: CometChatConversationsViewModelFactoryTest.kt
 */
class CometChatMessageListViewModelFactoryTest : FunSpec({

    test("create should return CometChatMessageListViewModel for correct class") {
        println("── Step 1: Create mock DataSource to avoid real SDK calls")
        val mockDataSource = mock<MessageListDataSource>()

        println("── Step 2: Build real repository with mock DataSource")
        val repository = MessageListRepositoryImpl(mockDataSource)

        println("── Step 3: Create factory with repository and enableListeners=false")
        val factory = CometChatMessageListViewModelFactory(
            repository = repository,
            enableListeners = false
        )

        println("── Step 4: Call factory.create with CometChatMessageListViewModel class")
        val viewModel = factory.create(CometChatMessageListViewModel::class.java)

        println("── Step 5: Verify returned instance is CometChatMessageListViewModel")
        viewModel.shouldBeInstanceOf<CometChatMessageListViewModel>()
    }

    test("create with custom repository and enableListeners=false should succeed") {
        println("── Step 1: Create mock DataSource")
        val mockDataSource = mock<MessageListDataSource>()

        println("── Step 2: Build custom repository with mock DataSource")
        val customRepository = MessageListRepositoryImpl(mockDataSource)

        println("── Step 3: Create factory with custom repository and listeners disabled")
        val factory = CometChatMessageListViewModelFactory(
            repository = customRepository,
            enableListeners = false
        )

        println("── Step 4: Call factory.create — should not throw")
        val viewModel = factory.create(CometChatMessageListViewModel::class.java)

        println("── Step 5: Verify ViewModel instance created successfully")
        viewModel.shouldBeInstanceOf<CometChatMessageListViewModel>()
    }

    test("create should throw IllegalArgumentException for unsupported ViewModel class") {
        println("── Step 1: Create mock DataSource")
        val mockDataSource = mock<MessageListDataSource>()

        println("── Step 2: Build repository with mock DataSource")
        val repository = MessageListRepositoryImpl(mockDataSource)

        println("── Step 3: Create factory")
        val factory = CometChatMessageListViewModelFactory(
            repository = repository,
            enableListeners = false
        )

        println("── Step 4: Call factory.create with unsupported ViewModel class (CometChatUsersViewModel)")
        println("── Step 5: Verify IllegalArgumentException is thrown")
        shouldThrow<IllegalArgumentException> {
            factory.create(CometChatUsersViewModel::class.java)
        }
    }
})

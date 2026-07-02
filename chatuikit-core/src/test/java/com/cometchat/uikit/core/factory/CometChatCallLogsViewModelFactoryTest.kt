package com.cometchat.uikit.core.factory

import com.cometchat.uikit.core.data.datasource.CallLogsDataSource
import com.cometchat.uikit.core.data.repository.CallLogsRepositoryImpl
import com.cometchat.uikit.core.viewmodel.CometChatCallLogsViewModel
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import org.mockito.kotlin.mock

/**
 * Tests for CometChatCallLogsViewModelFactory.
 * Verifies correct ViewModel creation and error handling.
 *
 * Reference: CometChatConversationsViewModelFactoryTest.kt
 */
class CometChatCallLogsViewModelFactoryTest : FunSpec({

    test("create should return CometChatCallLogsViewModel for correct class") {
        // Use a mock DataSource to avoid real SDK calls
        val mockDataSource = mock<CallLogsDataSource>()
        val repository = CallLogsRepositoryImpl(mockDataSource)
        val factory = CometChatCallLogsViewModelFactory(
            repository = repository,
            enableListeners = false // Disable SDK listeners for testing
        )

        val viewModel = factory.create(CometChatCallLogsViewModel::class.java)

        viewModel.shouldBeInstanceOf<CometChatCallLogsViewModel>()
    }

    test("create should throw IllegalArgumentException for unsupported ViewModel class") {
        val mockDataSource = mock<CallLogsDataSource>()
        val repository = CallLogsRepositoryImpl(mockDataSource)
        val factory = CometChatCallLogsViewModelFactory(
            repository = repository,
            enableListeners = false
        )

        shouldThrow<IllegalArgumentException> {
            factory.create(CometChatConversationsViewModel::class.java)
        }
    }

    test("create should pass custom repository through to ViewModel") {
        val mockDataSource = mock<CallLogsDataSource>()
        val customRepository = CallLogsRepositoryImpl(mockDataSource)
        val factory = CometChatCallLogsViewModelFactory(
            repository = customRepository,
            enableListeners = false
        )

        // Should not throw — custom repository is accepted
        val viewModel = factory.create(CometChatCallLogsViewModel::class.java)
        viewModel.shouldBeInstanceOf<CometChatCallLogsViewModel>()
    }
})

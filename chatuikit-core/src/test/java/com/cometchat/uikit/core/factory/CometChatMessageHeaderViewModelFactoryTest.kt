package com.cometchat.uikit.core.factory

import com.cometchat.uikit.core.data.datasource.MessageHeaderDataSource
import com.cometchat.uikit.core.data.repository.MessageHeaderRepositoryImpl
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import org.mockito.kotlin.mock

/**
 * Tests for CometChatMessageHeaderViewModelFactory.
 * Layer 5 — verifies correct ViewModel creation and error handling.
 *
 * Reference: CometChatConversationsViewModelFactoryTest.kt
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CometChatMessageHeaderViewModelFactoryTest"
 */
class CometChatMessageHeaderViewModelFactoryTest : FunSpec({

    beforeTest { println("  🧪 ${it.name.testName}") }
    afterTest { println() }

    test("create should return CometChatMessageHeaderViewModel for correct class") {
        val mockDataSource = mock<MessageHeaderDataSource>()
        val repository = MessageHeaderRepositoryImpl(mockDataSource)
        val factory = CometChatMessageHeaderViewModelFactory(
            repository = repository,
            enableListeners = false
        )
        println("    → Creating ViewModel via factory with enableListeners=false")

        val viewModel = factory.create(CometChatMessageHeaderViewModel::class.java)

        println("    → Created: ${viewModel::class.java.simpleName}")
        viewModel.shouldBeInstanceOf<CometChatMessageHeaderViewModel>()
    }

    test("create should throw IllegalArgumentException for unsupported ViewModel class") {
        val mockDataSource = mock<MessageHeaderDataSource>()
        val repository = MessageHeaderRepositoryImpl(mockDataSource)
        val factory = CometChatMessageHeaderViewModelFactory(
            repository = repository,
            enableListeners = false
        )
        println("    → Attempting to create unsupported ViewModel class")

        val exception = shouldThrow<IllegalArgumentException> {
            factory.create(FakeHeaderViewModel::class.java)
        }
        println("    → Exception thrown: ${exception.message}")
    }

    test("create should use default repository when none provided") {
        // Factory with default constructor should not throw during creation
        // (it will use MessageHeaderDataSourceImpl internally)
        val factory = CometChatMessageHeaderViewModelFactory(enableListeners = false)
        println("    → Creating factory with default repository")

        val viewModel = factory.create(CometChatMessageHeaderViewModel::class.java)

        println("    → Created: ${viewModel::class.java.simpleName}")
        viewModel.shouldBeInstanceOf<CometChatMessageHeaderViewModel>()
    }
})

/** Fake ViewModel class for testing unsupported class error. */
private class FakeHeaderViewModel : androidx.lifecycle.ViewModel()

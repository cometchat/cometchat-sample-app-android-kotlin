package com.cometchat.uikit.core.factory

import com.cometchat.uikit.core.viewmodel.CometChatCallButtonsViewModel
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for CometChatCallButtonsViewModelFactory.
 * Verifies correct ViewModel creation and error handling.
 *
 * The factory creates the full dependency chain internally:
 * DataSourceImpl → RepositoryImpl → UseCases → ViewModel
 *
 * Reference: CometChatCallLogsViewModelFactoryTest.kt
 *
 * Validates: Requirements 5.1, 5.2
 *
 * Run with:
 * ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CometChatCallButtonsViewModelFactoryTest"
 */
class CometChatCallButtonsViewModelFactoryTest : FunSpec({

    test("create should return CometChatCallButtonsViewModel for correct class") {
        val factory = CometChatCallButtonsViewModelFactory()

        println("  → Testing factory creates CometChatCallButtonsViewModel")

        val viewModel = factory.create(CometChatCallButtonsViewModel::class.java)

        viewModel.shouldBeInstanceOf<CometChatCallButtonsViewModel>()
        println("  ✅ Factory created CometChatCallButtonsViewModel successfully")
    }

    test("create should throw IllegalArgumentException for unsupported ViewModel class") {
        val factory = CometChatCallButtonsViewModelFactory()

        println("  → Testing factory throws for unsupported ViewModel class")

        shouldThrow<IllegalArgumentException> {
            factory.create(CometChatConversationsViewModel::class.java)
        }
        println("  ✅ IllegalArgumentException thrown for CometChatConversationsViewModel")
    }
})

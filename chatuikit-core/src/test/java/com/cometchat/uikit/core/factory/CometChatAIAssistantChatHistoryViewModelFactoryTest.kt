package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import com.cometchat.uikit.core.viewmodel.CometChatAIAssistantChatHistoryViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for CometChatAIAssistantChatHistoryViewModelFactory.
 *
 * Verifies:
 * - Correct ViewModel creation for CometChatAIAssistantChatHistoryViewModel class
 * - enableListeners parameter is passed correctly
 * - IllegalArgumentException thrown for unsupported ViewModel class
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "...CometChatAIAssistantChatHistoryViewModelFactoryTest"
 */
class CometChatAIAssistantChatHistoryViewModelFactoryTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        println()
    }

    test("create should return CometChatAIAssistantChatHistoryViewModel for correct class") {
        val factory = CometChatAIAssistantChatHistoryViewModelFactory(enableListeners = false)

        val viewModel = factory.create(CometChatAIAssistantChatHistoryViewModel::class.java)

        viewModel.shouldBeInstanceOf<CometChatAIAssistantChatHistoryViewModel>()
        println("    → factory.create(CometChatAIAssistantChatHistoryViewModel::class.java)")
        println("    ✅ Returns correct ViewModel instance")
    }

    test("create with enableListeners=false should create ViewModel that does not register listeners") {
        val factory = CometChatAIAssistantChatHistoryViewModelFactory(enableListeners = false)

        val viewModel = factory.create(CometChatAIAssistantChatHistoryViewModel::class.java)

        // ViewModel should not throw when addListeners/removeListeners called
        viewModel.addListeners()
        viewModel.removeListeners()
        println("    → enableListeners=false → addListeners/removeListeners are no-ops")
        println("    ✅ ViewModel created with listeners disabled")
    }

    test("create with enableListeners=true should create ViewModel with listeners enabled") {
        val factory = CometChatAIAssistantChatHistoryViewModelFactory(enableListeners = true)

        val viewModel = factory.create(CometChatAIAssistantChatHistoryViewModel::class.java)

        viewModel.shouldBeInstanceOf<CometChatAIAssistantChatHistoryViewModel>()
        println("    → enableListeners=true → listeners will be registered on addListeners()")
        println("    ✅ ViewModel created with listeners enabled")
    }

    test("create should throw IllegalArgumentException for unsupported ViewModel class") {
        val factory = CometChatAIAssistantChatHistoryViewModelFactory(enableListeners = false)

        val exception = shouldThrow<IllegalArgumentException> {
            factory.create(UnsupportedAIAssistantViewModel::class.java)
        }

        println("    → factory.create(UnsupportedAIAssistantViewModel::class.java)")
        println("    → exception.message='${exception.message}'")
        exception.message shouldBe "Unknown ViewModel class: ${UnsupportedAIAssistantViewModel::class.java.name}"
        println("    ✅ IllegalArgumentException thrown with correct message")
    }

    test("default factory constructor should have enableListeners=true") {
        val factory = CometChatAIAssistantChatHistoryViewModelFactory()

        val viewModel = factory.create(CometChatAIAssistantChatHistoryViewModel::class.java)

        viewModel.shouldBeInstanceOf<CometChatAIAssistantChatHistoryViewModel>()
        println("    → Default constructor: enableListeners defaults to true")
        println("    ✅ Default factory creates ViewModel with listeners enabled")
    }
})

/**
 * Dummy ViewModel class used to test factory rejection of unsupported types.
 */
private class UnsupportedAIAssistantViewModel : ViewModel()

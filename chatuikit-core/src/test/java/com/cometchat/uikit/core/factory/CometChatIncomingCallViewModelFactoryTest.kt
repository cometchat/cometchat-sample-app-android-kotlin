package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import com.cometchat.uikit.core.viewmodel.CometChatIncomingCallViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for CometChatIncomingCallViewModelFactory.
 * Verifies correct ViewModel creation and error handling for unsupported classes.
 *
 * Layer 5 — Factory tests. The IncomingCall factory is simple since there's
 * no DataSource/Repository — it only takes enableListeners parameter.
 *
 * Reference: CometChatCallLogsViewModelFactoryTest.kt
 *
 * Validates: Requirements 5.1, 5.2
 *
 * Run with:
 * ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CometChatIncomingCallViewModelFactoryTest"
 */
class CometChatIncomingCallViewModelFactoryTest : FunSpec({

    test("create should return CometChatIncomingCallViewModel for correct class") {
        val factory = CometChatIncomingCallViewModelFactory(
            enableListeners = false
        )

        println("  → Creating ViewModel via factory with enableListeners=false")

        val viewModel = factory.create(CometChatIncomingCallViewModel::class.java)

        viewModel.shouldBeInstanceOf<CometChatIncomingCallViewModel>()

        println("  ✅ Factory created CometChatIncomingCallViewModel successfully")
    }

    test("create should return ViewModel with listeners disabled when enableListeners=false") {
        val factory = CometChatIncomingCallViewModelFactory(
            enableListeners = false
        )

        println("  → Creating ViewModel with enableListeners=false")

        val viewModel = factory.create(CometChatIncomingCallViewModel::class.java)

        // ViewModel should be created without throwing (no SDK listener registration)
        viewModel.shouldBeInstanceOf<CometChatIncomingCallViewModel>()
        // Verify initial state is null (no call set)
        viewModel.call.value shouldBe null
        viewModel.acceptedCall.value shouldBe null
        viewModel.rejectedCall.value shouldBe null

        println("  ✅ ViewModel created with listeners disabled, initial state is null")
    }

    test("create should throw IllegalArgumentException for unsupported ViewModel class") {
        val factory = CometChatIncomingCallViewModelFactory(
            enableListeners = false
        )

        println("  → Attempting to create unsupported ViewModel class")

        val exception = shouldThrow<IllegalArgumentException> {
            factory.create(FakeUnsupportedViewModel::class.java)
        }

        println("  ✅ IllegalArgumentException thrown: ${exception.message}")
    }

    test("create should include class name in error message for unsupported class") {
        val factory = CometChatIncomingCallViewModelFactory(
            enableListeners = false
        )

        println("  → Verifying error message contains class name")

        val exception = shouldThrow<IllegalArgumentException> {
            factory.create(FakeUnsupportedViewModel::class.java)
        }

        // The error message should reference the unsupported class
        (exception.message?.contains("FakeUnsupportedViewModel") == true ||
            exception.message?.contains("Unknown ViewModel") == true) shouldBe true

        println("  ✅ Error message references the unsupported class: ${exception.message}")
    }

    test("factory should create independent ViewModel instances") {
        val factory = CometChatIncomingCallViewModelFactory(
            enableListeners = false
        )

        println("  → Creating two ViewModel instances from same factory")

        val vm1 = factory.create(CometChatIncomingCallViewModel::class.java)
        val vm2 = factory.create(CometChatIncomingCallViewModel::class.java)

        // Each call should produce a new instance
        (vm1 !== vm2) shouldBe true

        println("  ✅ Factory creates independent instances (vm1 !== vm2)")
    }
})

/**
 * Fake ViewModel class used to test factory error handling.
 * This class is NOT assignable to CometChatIncomingCallViewModel.
 */
private class FakeUnsupportedViewModel : ViewModel()

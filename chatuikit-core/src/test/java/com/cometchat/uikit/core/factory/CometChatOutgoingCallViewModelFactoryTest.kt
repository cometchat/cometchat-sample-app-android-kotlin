package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import com.cometchat.uikit.core.viewmodel.CometChatOutgoingCallViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for CometChatOutgoingCallViewModelFactory.
 * Verifies correct ViewModel creation and error handling for unsupported classes.
 *
 * Layer 5 — Factory tests. The OutgoingCall factory is simple since there's
 * no DataSource/Repository — it only takes enableListeners parameter.
 *
 * Reference: CometChatIncomingCallViewModelFactoryTest.kt
 *
 * Validates: Requirements 13.4, 13.5, 13.6
 *
 * Run with:
 * ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CometChatOutgoingCallViewModelFactoryTest"
 */
class CometChatOutgoingCallViewModelFactoryTest : FunSpec({

    test("create should return CometChatOutgoingCallViewModel for correct class") {
        val factory = CometChatOutgoingCallViewModelFactory(
            enableListeners = false
        )

        println("  → Creating ViewModel via factory with enableListeners=false")

        val viewModel = factory.create(CometChatOutgoingCallViewModel::class.java)

        viewModel.shouldBeInstanceOf<CometChatOutgoingCallViewModel>()

        println("  ✅ Factory created CometChatOutgoingCallViewModel successfully")
    }

    test("create should return ViewModel with listeners disabled when enableListeners=false") {
        val factory = CometChatOutgoingCallViewModelFactory(
            enableListeners = false
        )

        println("  → Creating ViewModel with enableListeners=false")

        val viewModel = factory.create(CometChatOutgoingCallViewModel::class.java)

        // ViewModel should be created without throwing (no SDK listener registration)
        viewModel.shouldBeInstanceOf<CometChatOutgoingCallViewModel>()
        // Verify initial state is null (no call set)
        viewModel.call.value shouldBe null
        viewModel.acceptedCall.value shouldBe null
        viewModel.rejectedCall.value shouldBe null
        viewModel.endCallButtonEnabled.value shouldBe true

        println("  ✅ ViewModel created with listeners disabled, initial state correct")
    }

    test("create should throw IllegalArgumentException for unsupported ViewModel class") {
        val factory = CometChatOutgoingCallViewModelFactory(
            enableListeners = false
        )

        println("  → Attempting to create unsupported ViewModel class")

        val exception = shouldThrow<IllegalArgumentException> {
            factory.create(FakeUnsupportedOutgoingViewModel::class.java)
        }

        println("  ✅ IllegalArgumentException thrown: ${exception.message}")
    }

    test("create should include class name in error message for unsupported class") {
        val factory = CometChatOutgoingCallViewModelFactory(
            enableListeners = false
        )

        println("  → Verifying error message contains class name")

        val exception = shouldThrow<IllegalArgumentException> {
            factory.create(FakeUnsupportedOutgoingViewModel::class.java)
        }

        // The error message should reference the unsupported class
        (exception.message?.contains("FakeUnsupportedOutgoingViewModel") == true ||
            exception.message?.contains("Unknown ViewModel") == true) shouldBe true

        println("  ✅ Error message references the unsupported class: ${exception.message}")
    }

    test("factory should create independent ViewModel instances") {
        val factory = CometChatOutgoingCallViewModelFactory(
            enableListeners = false
        )

        println("  → Creating two ViewModel instances from same factory")

        val vm1 = factory.create(CometChatOutgoingCallViewModel::class.java)
        val vm2 = factory.create(CometChatOutgoingCallViewModel::class.java)

        // Each call should produce a new instance
        (vm1 !== vm2) shouldBe true

        println("  ✅ Factory creates independent instances (vm1 !== vm2)")
    }
})

/**
 * Fake ViewModel class used to test factory error handling.
 * This class is NOT assignable to CometChatOutgoingCallViewModel.
 */
private class FakeUnsupportedOutgoingViewModel : ViewModel()

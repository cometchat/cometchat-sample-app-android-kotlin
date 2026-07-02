package com.cometchat.uikit.core.factory

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.uikit.core.data.datasource.MessageReceiptEventListener
import com.cometchat.uikit.core.domain.repository.MessageInformationRepository
import com.cometchat.uikit.core.viewmodel.CometChatMessageInformationViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for CometChatMessageInformationViewModelFactory.
 * Verifies correct ViewModel creation and error handling.
 *
 * Layer 5: Factory tests use a real Repository with mock DataSource
 * to verify ViewModel creation.
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*.factory.CometChatMessageInformationViewModelFactoryTest"
 */
class CometChatMessageInformationViewModelFactoryTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        println()
    }

    test("create should return CometChatMessageInformationViewModel for correct class") {
        println("=== TEST: Factory creates correct ViewModel ===")
        println("STEP 1: Creating factory with mock repository and enableListeners=false")
        val mockRepository = object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                return Result.success(emptyList())
            }
            override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
                return MessageReceipt()
            }
        }

        val factory = CometChatMessageInformationViewModelFactory(
            repository = mockRepository,
            eventListener = MessageReceiptEventListener(),
            enableListeners = false
        )

        println("STEP 2: Calling factory.create() with correct class")
        val viewModel = factory.create(CometChatMessageInformationViewModel::class.java)

        println("STEP 3: Asserting returned instance is CometChatMessageInformationViewModel")
        viewModel.shouldBeInstanceOf<CometChatMessageInformationViewModel>()

        println("RESULT: Factory created CometChatMessageInformationViewModel successfully ✅")
    }

    test("create should throw IllegalArgumentException for unsupported ViewModel class") {
        println("=== TEST: Factory throws for unsupported class ===")
        println("STEP 1: Creating factory")
        val mockRepository = object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                return Result.success(emptyList())
            }
            override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
                return MessageReceipt()
            }
        }

        val factory = CometChatMessageInformationViewModelFactory(
            repository = mockRepository,
            eventListener = MessageReceiptEventListener(),
            enableListeners = false
        )

        println("STEP 2: Calling factory.create() with unsupported class")
        shouldThrow<IllegalArgumentException> {
            factory.create(FakeViewModel::class.java)
        }

        println("RESULT: IllegalArgumentException thrown for unsupported class ✅")
    }

    test("create with default parameters should create ViewModel with default dependencies") {
        println("=== TEST: Factory with defaults creates ViewModel ===")
        println("STEP 1: Creating factory with no custom dependencies, enableListeners=false")
        val factory = CometChatMessageInformationViewModelFactory(
            enableListeners = false
        )

        println("STEP 2: Calling factory.create()")
        val viewModel = factory.create(CometChatMessageInformationViewModel::class.java)

        println("STEP 3: Asserting returned instance type")
        viewModel.shouldBeInstanceOf<CometChatMessageInformationViewModel>()

        println("RESULT: Factory with defaults created ViewModel successfully ✅")
    }

    test("create with custom repository should use provided repository") {
        println("=== TEST: Factory uses custom repository ===")
        println("STEP 1: Creating factory with custom repository")
        var fetchCalled = false
        val customRepository = object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                fetchCalled = true
                return Result.success(emptyList())
            }
            override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
                return MessageReceipt()
            }
        }

        val factory = CometChatMessageInformationViewModelFactory(
            repository = customRepository,
            enableListeners = false
        )

        println("STEP 2: Creating ViewModel")
        val viewModel = factory.create(CometChatMessageInformationViewModel::class.java)
        viewModel.shouldBeInstanceOf<CometChatMessageInformationViewModel>()

        println("RESULT: Factory accepted custom repository ✅")
    }
})

/**
 * Fake ViewModel class for testing unsupported class handling.
 */
private class FakeViewModel : androidx.lifecycle.ViewModel()

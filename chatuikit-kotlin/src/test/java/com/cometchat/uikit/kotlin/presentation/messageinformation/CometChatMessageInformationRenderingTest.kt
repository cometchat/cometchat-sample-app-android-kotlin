package com.cometchat.uikit.kotlin.presentation.messageinformation

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.repository.MessageInformationRepository
import com.cometchat.uikit.core.data.datasource.MessageReceiptEventListener
import com.cometchat.uikit.core.state.MessageInformationUIState
import com.cometchat.uikit.core.viewmodel.CometChatMessageInformationViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Rendering tests for CometChatMessageInformation Kotlin component.
 *
 * Verifies that the ViewModel produces the correct UIState for each scenario,
 * which the View observes to show/hide UI elements.
 *
 * States:
 * - Loading → shimmer/progress
 * - Loaded → receipt list visible
 * - Empty → empty state visible
 * - Error → error state visible
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*.messageinformation.CometChatMessageInformationRenderingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageInformationRenderingTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }

    // ==================== Helper Functions ====================

    fun createRepository(
        receipts: List<MessageReceipt> = emptyList(),
        error: CometChatException? = null
    ): MessageInformationRepository {
        return object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                return if (error != null) Result.failure(error) else Result.success(receipts)
            }
            override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
                return MessageReceipt().apply {
                    sender = if (message.receiver is User) message.receiver as User else null
                    readAt = message.readAt
                    deliveredAt = if (message.deliveredAt == 0L) message.readAt else message.deliveredAt
                    messageId = message.id
                }
            }
        }
    }

    fun createViewModel(repository: MessageInformationRepository): CometChatMessageInformationViewModel {
        return CometChatMessageInformationViewModel(
            repository = repository,
            eventListener = MessageReceiptEventListener(),
            enableListeners = false
        )
    }

    fun createGroupMessage(messageId: Long): BaseMessage {
        return TextMessage("group-1", "Test message", CometChatConstants.RECEIVER_TYPE_GROUP).apply {
            id = messageId
            sender = User().apply { uid = "sender-1"; name = "Sender" }
        }
    }

    fun createUserMessage(messageId: Long, deliveredAt: Long = 1000L, readAt: Long = 2000L): BaseMessage {
        val receiver = User().apply { uid = "receiver-1"; name = "Receiver" }
        return TextMessage(receiver.uid, "Test message", CometChatConstants.RECEIVER_TYPE_USER).apply {
            id = messageId
            this.deliveredAt = deliveredAt
            this.readAt = readAt
            this.receiver = receiver
            sender = User().apply { uid = "sender-1"; name = "Sender" }
        }
    }

    fun createMockReceipts(count: Int, messageId: Long = 100L): List<MessageReceipt> {
        return (1..count).map { i ->
            MessageReceipt().apply {
                this.messageId = messageId
                this.deliveredAt = 1000L + i * 100
                this.readAt = 2000L + i * 100
                sender = User().apply { uid = "user-$i"; name = "User $i" }
            }
        }
    }

    // ==================== Rendering State Tests ====================

    test("GROUP message with receipts → Loaded state") {
        println("=== TEST: GROUP message with receipts → Loaded ===")
        runTest {
            val receipts = createMockReceipts(3)
            val repository = createRepository(receipts = receipts)
            val viewModel = createViewModel(repository)

            println("STEP 1: Setting GROUP message")
            viewModel.setMessage(createGroupMessage(100L))
            advanceUntilIdle()

            println("STEP 2: Asserting Loaded state with 3 receipts")
            viewModel.state.value shouldBe MessageInformationUIState.Loaded
            viewModel.listData.value.size shouldBe 3
            println("RESULT: state=Loaded, receipts=3 ✅")
        }
    }

    test("GROUP message with empty receipts → Empty state") {
        println("=== TEST: GROUP message empty → Empty ===")
        runTest {
            val repository = createRepository(receipts = emptyList())
            val viewModel = createViewModel(repository)

            println("STEP 1: Setting GROUP message")
            viewModel.setMessage(createGroupMessage(100L))
            advanceUntilIdle()

            println("STEP 2: Asserting Empty state")
            viewModel.state.value shouldBe MessageInformationUIState.Empty
            viewModel.listData.value.size shouldBe 0
            println("RESULT: state=Empty ✅")
        }
    }

    test("GROUP message with fetch error → Error state") {
        println("=== TEST: GROUP message error → Error ===")
        runTest {
            val exception = CometChatException("NET_ERR", "Network error")
            val repository = createRepository(error = exception)
            val viewModel = createViewModel(repository)

            println("STEP 1: Setting GROUP message")
            viewModel.setMessage(createGroupMessage(100L))
            advanceUntilIdle()

            println("STEP 2: Asserting Error state")
            viewModel.state.value.shouldBeInstanceOf<MessageInformationUIState.Error>()
            viewModel.exception.value?.code shouldBe "NET_ERR"
            println("RESULT: state=Error, code='NET_ERR' ✅")
        }
    }

    test("USER message with deliveredAt > 0 → Loaded state with 1 receipt") {
        println("=== TEST: USER message delivered → Loaded ===")
        runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)

            println("STEP 1: Setting USER message with deliveredAt=1000")
            viewModel.setMessage(createUserMessage(100L, deliveredAt = 1000L, readAt = 2000L))
            advanceUntilIdle()

            println("STEP 2: Asserting Loaded state with 1 receipt")
            viewModel.state.value shouldBe MessageInformationUIState.Loaded
            viewModel.listData.value.size shouldBe 1
            println("RESULT: state=Loaded, receipts=1 ✅")
        }
    }

    test("USER message with deliveredAt = 0 → Empty state") {
        println("=== TEST: USER message not delivered → Empty ===")
        runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)

            println("STEP 1: Setting USER message with deliveredAt=0")
            viewModel.setMessage(createUserMessage(100L, deliveredAt = 0L, readAt = 0L))
            advanceUntilIdle()

            println("STEP 2: Asserting Empty state")
            viewModel.state.value shouldBe MessageInformationUIState.Empty
            viewModel.listData.value.size shouldBe 0
            println("RESULT: state=Empty (not delivered) ✅")
        }
    }

    // ==================== PBT: Receipt count drives state ====================

    test("PBT: Any receipt count > 0 produces Loaded, 0 produces Empty for GROUP") {
        println("=== PBT: Receipt count → state ===")
        checkAll(20, Arb.int(0..10)) { receiptCount ->
            runTest {
                val receipts = createMockReceipts(receiptCount)
                val repository = createRepository(receipts = receipts)
                val viewModel = createViewModel(repository)

                viewModel.setMessage(createGroupMessage(100L))
                advanceUntilIdle()

                if (receiptCount == 0) {
                    viewModel.state.value shouldBe MessageInformationUIState.Empty
                } else {
                    viewModel.state.value shouldBe MessageInformationUIState.Loaded
                    viewModel.listData.value.size shouldBe receiptCount
                }
                println("  [Iteration] receiptCount=$receiptCount → ${if (receiptCount == 0) "Empty" else "Loaded"} ✅")
            }
        }
    }

    // ==================== Conversation Type Determination ====================

    test("GROUP message sets conversationType to GROUP") {
        println("=== TEST: GROUP conversationType ===")
        runTest {
            val repository = createRepository(receipts = emptyList())
            val viewModel = createViewModel(repository)

            viewModel.setMessage(createGroupMessage(100L))
            advanceUntilIdle()

            viewModel.getConversationType() shouldBe CometChatConstants.RECEIVER_TYPE_GROUP
            println("RESULT: conversationType=group ✅")
        }
    }

    test("USER message sets conversationType to USER") {
        println("=== TEST: USER conversationType ===")
        runTest {
            val repository = createRepository()
            val viewModel = createViewModel(repository)

            viewModel.setMessage(createUserMessage(100L))
            advanceUntilIdle()

            viewModel.getConversationType() shouldBe CometChatConstants.RECEIVER_TYPE_USER
            println("RESULT: conversationType=user ✅")
        }
    }
})
